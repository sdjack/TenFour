package com.steve-jackson-studios.tenfour.Data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by sjackson on 7/31/2017.
 * ChatSubdivisionData
 */
public class ChatSubdivisionData implements ChatData.DataLockListener {
    public final String id;
    private final HashMap<String, ChatLocationData> locations;
    private boolean pending = false;

    public ChatSubdivisionData(String id) {
        this.id = id;
        this.locations = new HashMap<>();
        ChatData.setDataLockListener(this);
    }

    public void load(int orderId, String locationId, JSONObject data) throws JSONException {
        ChatLocationData location;
        if (null == locations.get(locationId)) {
            location = new ChatLocationData(locationId, id);
            locations.put(locationId, location);
        } else {
            location = locations.get(locationId);
        }
        this.pending = true;
        ChatPostData entity = new ChatPostData(data);
        if (entity.isSticker == 0) {
            MapData.loadPopupData(locationId, entity);
        }
        location.load(orderId, entity);
    }

    public void loadTemp(String locationId, JSONObject data) throws JSONException {
        ChatLocationData location;
        if (null == locations.get(locationId)) {
            location = new ChatLocationData(locationId, id);
            locations.put(locationId, location);
        } else {
            location = locations.get(locationId);
        }
        this.pending = true;
        ChatPostData entity = new ChatPostData(data);
        if (entity.isSticker == 0) {
            MapData.loadPopupData(locationId, entity);
        }
        location.loadTemp(entity);
    }


    public void calcWeight(String locationId) {
        if (null != locations.get(locationId)) {
            double w = locations.get(locationId).size();
            MapData.updateHeatMapData(locationId, w);
        }
    }

    public ChatLocationData getLocation(String locationId) {
        return locations.get(locationId);
    }

    public Map fetch() {
        TreeMap<Integer, ChatPostData> data = new TreeMap<>();
        Set set = locations.entrySet();
        for (Object aSet : set) {
            Map.Entry entry = (Map.Entry) aSet;
            ChatLocationData location = (ChatLocationData) entry.getValue();
            location.collect(data);
        }
        return ChatData.sortByValues(data);
    }

    @Override
    public void onLock() {
//        Set set = locations.entrySet();
//        for (Object aSet : set) {
//            Map.Entry entry = (Map.Entry) aSet;
//            ChatLocationData location = (ChatLocationData) entry.getValue();
//            location.lock();
//        }
    }

    @Override
    public void onUnlock() {
        if (this.pending) {
//            Set set = locations.entrySet();
//            for (Object aSet : set) {
//                Map.Entry entry = (Map.Entry) aSet;
//                ChatLocationData location = (ChatLocationData) entry.getValue();
//                location.unlock();
//            }
        }
        this.pending = false;
    }
}
