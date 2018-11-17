package com.steve-jackson-studios.tenfour.Data;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by sjackson on 8/2/2017.
 * EventData
 */

public class EventData {

    private static final String TAG = EventData.class.getName();
    private static final HashMap<String, HashMap<String,EventDataModel>> DATA = new HashMap<>();
    private static final ArrayList<DataLockListener> LISTENERS = new ArrayList<>();
    private static boolean LOCKED = false;
    private static boolean NOTIFY_EVENT_CREATED_BY_ME = false;

    private EventData() {}

    public static abstract class Fields {
        public static final String ID = "ID";
        public static final String EVENT_ID = "EVENT_ID";
        public static final String LOCATION = "LOCATION";
        public static final String CREATED_BY = "CREATED_BY";
        public static final String CATEGORY = "CATEGORY";
        public static final String DESCRIPTION = "DESCRIPTION";
        public static final String LATITUDE = "LATITUDE";
        public static final String LONGITUDE = "LONGITUDE";
        public static final String POPULATION = "POPULATION";
        public static final String ENGAGEMENT = "ENGAGEMENT";
        public static final String SCORE = "SCORE";
        public static final String LAST_UPDATE = "LAST_UPDATE";
        public static final String CREATED_ON = "CREATED_ON";
        public static final String ARCHIVE_ON = "ARCHIVE_ON";
    }

    public static void loadEventData(JSONObject data) {
        if (LOCKED) return;
        lock();
        try {
            if (!data.isNull(Fields.ID) && !data.isNull(Fields.LOCATION)) {
                String eventId = data.getString(Fields.ID);
                String locationId = data.getString(Fields.LOCATION);
                String userId = data.getString(Fields.CREATED_BY);
                NOTIFY_EVENT_CREATED_BY_ME = (userId.equals(UserData.ID));
                HashMap<String,EventDataModel> eventList = getEventData(locationId);
                if (eventList.get(eventId) == null) {
                    eventList.put(eventId, new EventDataModel(eventId, locationId, data));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            unlock();
        }
    }

    public static void loadEventData(JSONArray source) {
        if (LOCKED) return;
        lock();
        new AsyncDataLoader(source).execute();
    }

    public static HashMap<String,EventDataModel> getEventData() {
        if (DATA.get(AppConstants.LOCATION_ID) == null) {
            DATA.put(AppConstants.LOCATION_ID, new HashMap<String,EventDataModel>());
        }

        return DATA.get(AppConstants.LOCATION_ID);
    }

    public static HashMap<String,EventDataModel> getEventData(String locationId) {
        if (DATA.get(locationId) == null) {
            DATA.put(locationId, new HashMap<String,EventDataModel>());
        }

        return DATA.get(locationId);
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
        Log.d("EventData", "Triggered ObservedEvents.NOTIFY_AVAILABLE_EVENT_DATA");
        if (NOTIFY_EVENT_CREATED_BY_ME) {
            NOTIFY_EVENT_CREATED_BY_ME = false;
            Dispatch.triggerEvent(ObservedEvents.NOTIFY_EVENT_CREATED_BY_ME);
        } else {
            Dispatch.triggerEvent(ObservedEvents.NOTIFY_AVAILABLE_EVENT_DATA);
        }
    }

    public interface DataLockListener {
        void onLock();
        void onUnlock();
    }

    public static boolean hasUsableValue(JSONObject obj, String key) throws JSONException {
        String value = (!obj.isNull(key)) ? obj.getString(key) : "";
        return (!TextUtils.isEmpty(value));
    }

    public static String getUsableValue(JSONObject obj, String key, String fallback) throws JSONException {
        String value = (!obj.isNull(key)) ? obj.getString(key) : "";
        return (!TextUtils.isEmpty(value)) ? value : fallback;
    }

    public static String getUsableValue(JSONObject obj, String key1, String key2, String fallback) throws JSONException {
        String value = (!obj.isNull(key1)) ? obj.getString(key1) : (!obj.isNull(key2)) ? obj.getString(key2) : "";
        return (!TextUtils.isEmpty(value)) ? value : fallback;
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
                //Log.d("ChatData", "[AsyncDataLoader]: maxCount = " + maxCount);
                if (maxCount > 0) {
                    for (int i = 0; i < maxCount; i++) {
                        //Log.d("ChatData", "[AsyncDataLoader]: loop index = " + i);
                        if (!(source.get(i) instanceof JSONArray)) {
                            JSONObject data = (JSONObject) source.get(i);
                            if (!data.isNull(Fields.ID) && !data.isNull(Fields.LOCATION)) {
                                String eventId = data.getString(Fields.ID);
                                String locationId = data.getString(Fields.LOCATION);
                                HashMap<String,EventDataModel> eventList = getEventData(locationId);
                                if (eventList.get(eventId) == null) {
                                    eventList.put(eventId, new EventDataModel(eventId, locationId, data));
                                }
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
}
