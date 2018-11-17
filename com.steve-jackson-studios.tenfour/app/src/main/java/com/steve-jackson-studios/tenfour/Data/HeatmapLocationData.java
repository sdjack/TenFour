package com.steve-jackson-studios.tenfour.Data;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;

public class HeatmapLocationData {

    private static final String TAG = HeatmapLocationData.class.getName();
    private final double latitude;
    private final double longitude;
    private WeightedLatLng weightedLatLng;
    public double weight = 0.0;

    public HeatmapLocationData(double lat, double lon, double w) {
        weight = w;
        latitude = lat;
        longitude = lon;
        LatLng latLng = new LatLng(lat, lon);
        weightedLatLng = new WeightedLatLng(latLng, w);
    }

    public void setWeight(double w) {
        weight = w;
        LatLng latLng = new LatLng(latitude, longitude);
        weightedLatLng = new WeightedLatLng(latLng, w);
    }

    public WeightedLatLng getWeightedLatLng() {
        return weightedLatLng;
    }
}
