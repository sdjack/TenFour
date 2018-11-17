package com.steve-jackson-studios.tenfour.Data;

import android.os.AsyncTask;
import android.util.Log;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by sjackson on 7/31/2017.
 * MapData
 */

public class MapData {

    private static final String TAG = MapData.class.getName();
    private static final HashMap<String, MapSubdivisionData> DATA = new HashMap<>();
    private static final HeatmapData HEATMAP_DATA = new HeatmapData();
    private static final MapChatData POPUP_DATA = new MapChatData();
    private static boolean LOCKED = false;

    private MapData() {}

    public static abstract class Fields {
        public static final String ID = "ID";
        public static final String SUBDIVISION_ID = "SUBDIVISION_ID";
        public static final String LATITUDE = "LATITUDE";
        public static final String LONGITUDE = "LONGITUDE";
        public static final String LAT_START = "LAT_START";
        public static final String LAT_END = "LAT_END";
        public static final String LONG_START = "LONG_START";
        public static final String LONG_END = "LONG_END";
        public static final String SD_LAT_START = "SD_LAT_START";
        public static final String SD_LAT_END = "SD_LAT_END";
        public static final String SD_LONG_START = "SD_LONG_START";
        public static final String SD_LONG_END = "SD_LONG_END";
        public static final String MAP = "MAP";
        public static final String CHAT = "CHAT";
        public static final String EVENT = "EVENT";
        public static final String DISPLAY_LATITUDE = "DISPLAY_LATITUDE";
        public static final String DISPLAY_LONGITUDE = "DISPLAY_LONGITUDE";
        public static final String INTENSITY = "INTENSITY";
        // public static final String DISTANCE = "DISTANCE";
        // public static final String BEARING = "BEARING";
    }

    public static void loadMapData(JSONArray source) {
        if (LOCKED) return;
        lock();
        new AsyncDataLoader(source).execute();
    }

    public static void updateHeatMapData(String id, double weight) {
        HEATMAP_DATA.update(id, weight);
    }

    public static HeatmapData getHeatMapData(String uuid) {
        if (uuid == null || !uuid.equals(HEATMAP_DATA.uuid)) {
            return HEATMAP_DATA;
        }
        return null;
    }

    public static MapChatData getPopupData(String uuid) {
        if (uuid == null || !uuid.equals(POPUP_DATA.uuid)) {
            return POPUP_DATA;
        }
        return null;
    }

    public static MapChatData getPopupData() {
        return POPUP_DATA;
    }

    public static void loadPopupData(String id, ChatPostData chatPostData) {
        POPUP_DATA.load(id,chatPostData);
    }

    public static void loadStickerData(String id, ChatPostData[] chatStickers) {
        POPUP_DATA.loadStickers(id,chatStickers);
    }

    public static HashMap<String, MapSubdivisionData> getMapData() {
        if (LOCKED) return null;
        return DATA;
    }

    public static MapSubdivisionData getMapData(String id, JSONObject data) {
        if (DATA.get(id) == null) {
            DATA.put(id, new MapSubdivisionData(id, data));
        }

        return DATA.get(id);
    }

    public static MapSubdivisionData getMapData(String id) {
        if (DATA.get(id) != null) {
            return DATA.get(id);
        }

        return null;
    }

    private static void lock() {
        LOCKED = true;
    }

    private static void unlock() {
        LOCKED = false;
        Dispatch.triggerEvent(ObservedEvents.NOTIFY_AVAILABLE_NODE_DATA);
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
                        if (!(source.get(i) instanceof JSONArray)) {
                            JSONObject data = (JSONObject) source.get(i);
                            if (!data.isNull(Fields.SUBDIVISION_ID) && !data.isNull(Fields.SD_LAT_START)) {
                                String id = data.getString(Fields.SUBDIVISION_ID);
                                MapSubdivisionData mapSubdivisionData = getMapData(id, data);
                                if (null != mapSubdivisionData && !data.isNull(Fields.ID)) {
                                    String locationId = data.getString(Fields.ID);
                                    mapSubdivisionData.load(locationId, data);
                                    if (!data.isNull(MapData.Fields.INTENSITY)) {
                                        double lat = data.getDouble(MapData.Fields.DISPLAY_LATITUDE);
                                        double lon = data.getDouble(MapData.Fields.DISPLAY_LONGITUDE);
                                        double w = data.getDouble(MapData.Fields.INTENSITY);
                                        if (w > 0) {
                                            HEATMAP_DATA.load(locationId, lat, lon, w);
                                        }
                                    }
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
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	Converts decimal degrees to radians						    :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	Converts radians to decimal degrees						    :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
    /**
     * Gets direction.
     *
     * @param lat the lat
     * @param lon the lon
     * @return the direction
     */
    public static float findDirection(double lon, double lat) {
        double alphaLat = lat - AppConstants.GPS_LATITUDE;
        double alphaLon = (Math.cos(deg2rad(AppConstants.GPS_LATITUDE)) *
                (lon - AppConstants.GPS_LONGITUDE));
        double theta = Math.atan2(alphaLat, alphaLon);
        double angle = rad2deg(theta);
        return ((float) angle);
    }
}
