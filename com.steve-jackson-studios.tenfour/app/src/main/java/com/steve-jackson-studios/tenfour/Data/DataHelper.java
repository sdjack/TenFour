package com.steve-jackson-studios.tenfour.Data;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.steve-jackson-studios.tenfour.IO.FileIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by sjackson on 9/14/2017.
 * DataHelper
 */

public class DataHelper {

    private static final String DATA_BUNDLE_FILE = "m_bundled_data.json";

    public static boolean FILE_DATA_LOADED = false;
    public static boolean FILE_DATA_SAVED = false;

    private DataHelper() {}

    public static void loadDataBundleFile(Context context) {
        new AsyncLoadDataBundleFile(context).execute();
    }

    public static void saveDataBundleFile(Context context, JSONArray source) {
        new AsyncSaveDataBundleFile(context, source).execute();
    }

    public static class AsyncLoadDataBundleFile extends AsyncTask<Void, Void, Boolean> {

        private final Context context;

        public AsyncLoadDataBundleFile(Context c) {
            this.context = c;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                FILE_DATA_LOADED = true;
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //Log.d("DataHelper", "AsyncLoadDataBundleFile doInBackground ");
            try {
                JSONArray bundle = FileIO.readFromJsonFile(context, DATA_BUNDLE_FILE);
                if (bundle.length() > 0) {
                    for (int i = 0; i < bundle.length(); i++) {
                        JSONObject data = (JSONObject) bundle.get(i);
                        if (!data.isNull(MapData.Fields.SUBDIVISION_ID)) {
                            if (!data.isNull(MapData.Fields.MAP)) {
                                String jsonString = data.getString(MapData.Fields.MAP);
                                if (jsonString != null && !TextUtils.isEmpty(jsonString) && !jsonString.equals("[]")) {
                                    JSONArray mapData = new JSONArray(jsonString);
                                    MapData.loadMapData(mapData);
                                }
                            }
                            if (!data.isNull(MapData.Fields.CHAT)) {
                                String jsonString = data.getString(MapData.Fields.CHAT);
                                if (jsonString != null && !TextUtils.isEmpty(jsonString) && !jsonString.equals("[]")) {
                                    JSONArray chatData = new JSONArray(jsonString);
                                    ChatData.loadChatData(chatData);
                                }
                            }
                            if (!data.isNull(MapData.Fields.EVENT)) {
                                String jsonString = data.getString(MapData.Fields.EVENT);
                                if (jsonString != null && !TextUtils.isEmpty(jsonString) && !jsonString.equals("[]")) {
                                    JSONArray eventList = new JSONArray(jsonString);
                                    EventData.loadEventData(eventList);
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

    public static class AsyncSaveDataBundleFile extends AsyncTask<Void, Void, Boolean> {

        private final Context context;
        private final JSONArray bundle;

        public AsyncSaveDataBundleFile(Context c, JSONArray source) {
            this.context = c;
            this.bundle = source;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                FILE_DATA_SAVED = true;
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Log.d("DEVDEBUG", "AsyncSaveDataBundleFile doInBackground ");
            try {
                if (bundle.length() > 0) {
                    for (int i = 0; i < bundle.length(); i++) {
                        JSONObject data = (JSONObject) bundle.get(i);
                        if (!data.isNull(MapData.Fields.SUBDIVISION_ID)) {
                            if (!data.isNull(MapData.Fields.MAP)) {
                                String jsonString = data.getString(MapData.Fields.MAP);
                                if (jsonString != null && !TextUtils.isEmpty(jsonString) && !jsonString.equals("[]")) {
                                    JSONArray mapData = new JSONArray(jsonString);
                                    MapData.loadMapData(mapData);
                                }
                            }
                            if (!data.isNull(MapData.Fields.CHAT)) {
                                String jsonString = data.getString(MapData.Fields.CHAT);
                                if (jsonString != null && !TextUtils.isEmpty(jsonString) && !jsonString.equals("[]")) {
                                    JSONArray chatData = new JSONArray(jsonString);
                                    ChatData.loadChatData(chatData);
                                }
                            }
                            if (!data.isNull(MapData.Fields.EVENT)) {
                                String jsonString = data.getString(MapData.Fields.EVENT);
                                if (jsonString != null && !TextUtils.isEmpty(jsonString) && !jsonString.equals("[]")) {
                                    JSONArray eventList = new JSONArray(jsonString);
                                    EventData.loadEventData(eventList);
                                }
                            }
                        }
                    }
                } else {
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return FileIO.saveToJsonFile(context, DATA_BUNDLE_FILE, bundle.toString());
        }
    }
}
