package com.steve-jackson-studios.tenfour.Data;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by sjackson on 7/28/2017.
 * FriendData
 */

public class FriendData {

    private static final HashMap<String, FriendEntity> DATA = new HashMap<>();
    private static final ArrayList<DataLockListener> LISTENERS = new ArrayList<>();
    private static boolean LOCKED = false;

    private FriendData() {}

    public static abstract class Fields {
        public static final String ID = "ID";
        public static final String LOCATION = "LOCATION";
        public static final String SUBDIVISION_ID = "SUBDIVISION_ID";
        public static final String USERNAME = "USERNAME";
        public static final String DISPLAY_INITIALS = "DISPLAY_INITIALS";
        public static final String AVATAR = "AVATAR";
        public static final String AVATAR_TYPE = "AVATAR_TYPE";
        public static final String AVATAR_COLOR = "AVATAR_COLOR";
        public static final String STATUS = "STATUS";
        public static final String LATITUDE = "LATITUDE";
        public static final String LONGITUDE = "LONGITUDE";
        public static final String LAST_UPDATE = "LAST_UPDATE";
        public static final String KARMA = "KARMA";
        public static final String ONLINE = "ONLINE";
        public static final String REQUEST = "REQUEST";
    }

    public static void loadFriendData(JSONObject data) {
        if (LOCKED) return;
        lock();
        try {
            if (!data.isNull(Fields.ID)) {
                Iterator<?> keys = data.keys();
                while( keys.hasNext() ) {
                    String key = (String)keys.next();
                    if (data.get(key) instanceof JSONObject) {
                        JSONObject friend = data.getJSONObject(key);
                        if (DATA.get(key) == null) {
                            DATA.put(key, new FriendEntity(friend));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            unlock();
        }
    }

    public static void loadFriendData(JSONArray data) {
        //if (LOCKED) return;
        lock();
        new AsyncDataLoader(data).execute();
    }

    public static HashMap<String, FriendEntity> getAll() {
        return DATA;
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
        LOCKED = false;
        for (int i = 0; i < LISTENERS.size(); i++) {
            LISTENERS.get(i).onUnlock();
        }

        Dispatch.triggerEvent(ObservedEvents.NOTIFY_AVAILABLE_FRIENDS_DATA);
    }

    public interface DataLockListener {
        void onLock();
        void onUnlock();
    }

    public static String getUsableUUID(JSONObject obj, String key) throws JSONException {
        String value = (!obj.isNull(key)) ? obj.getString(key) : "";
        if (TextUtils.isEmpty(value)) {
            value = "TEMP" + UUID.randomUUID().toString();
            obj.put(key, value);
        }
        return value;
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
                // Log.d("FriendData", source.toString());
                int maxCount = source.length();
                if (maxCount > 0) {
                    for (int i = 0; i < maxCount; i++) {
                        JSONObject data = (JSONObject) source.get(i);
                        if (!data.isNull(Fields.ID)) {
                            String key = data.getString(Fields.ID);
                            if (DATA.get(key) == null) {
                                DATA.put(key, new FriendEntity(data));
                            } else {
                                FriendEntity friend = DATA.get(key);
                                friend.update(data);
                            }
                        }
                    }
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                unlock();
            }
            return false;
        }
    }
}
