package com.steve-jackson-studios.tenfour.Data;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by sjackson on 4/27/2018.
 *
 */

public class HeatmapData {
    public final HashMap<String, HeatmapLocationData> data = new HashMap<>();
    public String uuid;

    public HeatmapData() {
        uuid = UUID.randomUUID().toString();
    }

    public void load(String id, double lat, double lon, double w) {
        if (data.get(id) == null) {
            uuid = UUID.randomUUID().toString();
            data.put(id, new HeatmapLocationData(lat, lon, w));
        } else {
            HeatmapLocationData hmd = data.get(id);
            if (hmd.weight != w) {
                uuid = UUID.randomUUID().toString();
                data.put(id, new HeatmapLocationData(lat, lon, w));
            }
        }
    }

    public void update(String id, double w) {
        if (data.get(id) != null) {
            data.get(id).setWeight(w);
        }
    }
}
