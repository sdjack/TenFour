package com.steve-jackson-studios.tenfour.Data;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Created by sjackson on 7/28/2017.
 * ChatData
 */

public class ChatData {

    private static final HashMap<String, ChatSubdivisionData> DATA = new HashMap<>();
    private static final HashMap<String, ArrayList<ChatPostData>> SYSTEM_MESSAGES = new HashMap<>();
    private static final ArrayList<DataLockListener> LISTENERS = new ArrayList<>();
    private static boolean LOCKED = false;

    private ChatData() {}

    public static abstract class Fields {
        public static final String ID = "ID";
        public static final String ORDER_ID = "ORDER_ID";
        public static final String LOCATION = "LOCATION";
        public static final String EVENT_ID = "EVENT_ID";
        public static final String SUBDIVISION_ID = "SUBDIVISION_ID";
        public static final String REPLY_ID = "REPLY_ID";
        public static final String USERNAME = "USERNAME";
        public static final String DISPLAY_NAME = "DISPLAY_NAME";
        public static final String DISPLAY_INITIALS = "DISPLAY_INITIALS";
        public static final String AVATAR_TYPE = "AVATAR_TYPE";
        public static final String AVATAR_COLOR = "AVATAR_COLOR";
        public static final String SCORE = "SCORE";
        public static final String DISPLAY_STATUS = "STATUS";
        public static final String LATITUDE = "LATITUDE";
        public static final String LONGITUDE = "LONGITUDE";
        public static final String MESSAGE = "MESSAGE";
        public static final String IMAGE = "IMAGE";
        public static final String STICKER = "STICKER";
        public static final String LAST_UPDATE = "LAST_UPDATE";
        public static final String KARMA = "KARMA";
        public static final String REPLY_COUNT = "REPLY_COUNT";
        public static final String REACTIONS = "REACTIONS";
        public static final String CREATED_ON = "CREATED_ON";
    }

    public static void loadSystemMessage(JSONObject data) {
        try {
            String locationId = data.getString(Fields.LOCATION);
            ArrayList<ChatPostData> systemMessageData = getSystemMessageData(locationId);
            systemMessageData.add(new ChatPostData(data));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Dispatch.triggerEvent(ObservedEvents.NOTIFY_AVAILABLE_CHAT_DATA);
    }

    public static void loadChatData(JSONObject data) {
        //Log.d("DEVDEBUG", "loadChatData->JSONObject->LOCKED = " + LOCKED);
        if (LOCKED) return;
        lock();
        try {
            if (!data.isNull(Fields.SUBDIVISION_ID)) {
                String fenceId = data.getString(Fields.SUBDIVISION_ID);
                if (!data.isNull(Fields.LOCATION)) {
                    String locationId = data.getString(Fields.LOCATION);
                    ChatSubdivisionData model = getData(fenceId);
                    model.loadTemp(locationId, data);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            unlock();
        }
    }

    public static void loadChatData(JSONArray source) {
        //Log.d("DEVDEBUG", "loadChatData->JSONArray->LOCKED = " + LOCKED);
        //if (LOCKED) return;
        lock();
        new AsyncDataLoader(source).execute();
    }

    public static void loadPreviewData(JSONArray source) {
        if (LOCKED) return;
        lock();
        new AsyncPreviewLoader(source).execute();
    }

    public static ChatPostData[] getTopStickers(String subdivisionId, String locationId) {
        if (null == getData(subdivisionId, locationId)) {
            return null;
        }
        return getData(subdivisionId, locationId).stickers();
    }

    public static ArrayList<ChatPostData> getSystemMessageData(String locationId) {
        ArrayList<ChatPostData> data;
        if (SYSTEM_MESSAGES.get(locationId) == null) {
            data = new ArrayList<>();
            SYSTEM_MESSAGES.put(locationId, data);
        } else {
            data = SYSTEM_MESSAGES.get(locationId);
        }
        return data;
    }

    public static ChatSubdivisionData getData(String subdivisionId) {
        ChatSubdivisionData chatSubdivisionData;
        if (DATA.get(subdivisionId) == null) {
            chatSubdivisionData = new ChatSubdivisionData(subdivisionId);
            DATA.put(subdivisionId, chatSubdivisionData);
        } else {
            chatSubdivisionData = DATA.get(subdivisionId);
        }
        return chatSubdivisionData;
    }

    public static ChatLocationData getData(String subdivisionId, String locationId) {
        ChatSubdivisionData chatSubdivisionData;
        if (DATA.get(subdivisionId) == null) {
            chatSubdivisionData = new ChatSubdivisionData(subdivisionId);
            DATA.put(subdivisionId, chatSubdivisionData);
        } else {
            chatSubdivisionData = DATA.get(subdivisionId);
        }
        return chatSubdivisionData.getLocation(locationId);
    }

    public static Map getLive() {
        return getData(AppConstants.SUBDIVISION_ID).fetch();
    }

    public static Map getPreview() {
        if (null != AppConstants.MAP_ACTIVE_SUBDIVISION_ID) {
            if (null != AppConstants.MAP_ACTIVE_LOCATION_ID && null != getData(AppConstants.MAP_ACTIVE_SUBDIVISION_ID, AppConstants.MAP_ACTIVE_LOCATION_ID)) {
                return getData(AppConstants.MAP_ACTIVE_SUBDIVISION_ID, AppConstants.MAP_ACTIVE_LOCATION_ID).fetch();
            }
            return getData(AppConstants.MAP_ACTIVE_SUBDIVISION_ID).fetch();
        }
        return null;
    }

    public static void setDataLockListener(DataLockListener listener) {
        LISTENERS.add(listener);
    }

    private static void lock() {
        LOCKED = true;
        for (int i = 0; i < LISTENERS.size(); i++) {
            LISTENERS.get(i).onLock();
        }
    }

    private static void unlock() {
        for (int i = 0; i < LISTENERS.size(); i++) {
            LISTENERS.get(i).onUnlock();
        }
        LOCKED = false;

        Dispatch.triggerEvent(ObservedEvents.NOTIFY_AVAILABLE_CHAT_DATA);
    }

    private static void unlockPreview() {
        for (int i = 0; i < LISTENERS.size(); i++) {
            LISTENERS.get(i).onUnlock();
        }
        LOCKED = false;
        Dispatch.triggerEvent(ObservedEvents.NOTIFY_AVAILABLE_PREVIEW_DATA);
    }

    public interface DataLockListener {
        void onLock();
        void onUnlock();
    }

    public static int getUsableValue(JSONObject obj, String key, int fallback) throws JSONException {
        return (!obj.isNull(key)) ? obj.getInt(key) : fallback;
    }

    public static String getUsableUUID(JSONObject obj, String key) throws JSONException {
        String value = (!obj.isNull(key)) ? obj.getString(key) : "";
        if (TextUtils.isEmpty(value)) {
            value = "TEMP" + UUID.randomUUID().toString();
            obj.put(key, value);
        }
        return value;
    }

    public static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private static class AsyncDataLoader extends AsyncTask<Void, Void, Boolean> {

        private final JSONArray source;

        AsyncDataLoader(JSONArray source) {
            this.source = source;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            unlock();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                int maxCount = source.length();
                if (maxCount > 0) {
                    for (int i = 0; i < maxCount; i++) {
                        JSONObject data = (JSONObject) source.get(i);
                        if (!data.isNull(Fields.SUBDIVISION_ID)) {
                            String fenceId = data.getString(Fields.SUBDIVISION_ID);
                            if (!data.isNull(Fields.LOCATION)) {
                                int id = getUsableValue(data, Fields.ORDER_ID, 99);
                                String locationId = data.getString(Fields.LOCATION);
                                ChatSubdivisionData chatSubdivisionData = getData(fenceId);
                                chatSubdivisionData.load(id, locationId, data);
                                chatSubdivisionData.calcWeight(locationId);
                            }
                        }
                    }
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private static class AsyncPreviewLoader extends AsyncTask<Void, Void, Boolean> {

        private final JSONArray source;

        AsyncPreviewLoader(JSONArray source) {
            this.source = source;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            unlockPreview();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                int maxCount = source.length();
                if (maxCount > 0) {
                    for (int i = 0; i < maxCount; i++) {
                        JSONObject data = (JSONObject) source.get(i);
                        if (!data.isNull(Fields.SUBDIVISION_ID)) {
                            String fenceId = data.getString(Fields.SUBDIVISION_ID);
                            if (!data.isNull(Fields.LOCATION)) {
                                int id = getUsableValue(data, Fields.ORDER_ID, 99);
                                String locationId = data.getString(Fields.LOCATION);
                                ChatSubdivisionData chatSubdivisionData = getData(fenceId);
                                chatSubdivisionData.load(id, locationId, data);
                            }
                        }
                    }
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator =
                new Comparator<K>() {
                    public int compare(K k1, K k2) {
                        return map.get(k2).compareTo(map.get(k1));
                    }
                };

        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }
}
