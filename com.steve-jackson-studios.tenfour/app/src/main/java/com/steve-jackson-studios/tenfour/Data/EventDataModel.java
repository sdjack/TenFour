package com.steve-jackson-studios.tenfour.Data;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonSyntaxException;
import com.google.maps.android.clustering.ClusterItem;
import com.steve-jackson-studios.tenfour.AppConstants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sjackson on 8/2/2017.
 * EventDataModel
 */

public class EventDataModel implements ClusterItem {

    private final String id;
    private final String locationId;
    private String title;
    private String owner;
    private int category;
    private int resIcon = AppConstants.CATEGORY_ICONS[1];
    private int resColor = AppConstants.CATEGORY_COLORS[1];
    private int population = 0;
    private int engagement = 0;
    private int score_mod = 0;
    private int scale_mod = 0;
    private int rating = 0;
    private double score = 0;
    private double[] latlng = new double[2];
    private boolean myEvent = false;

    public EventDataModel(String id, String locationId, JSONObject data) {
        this.id = id;
        this.locationId = locationId;
        try {
            this.owner = data.getString(EventData.Fields.CREATED_BY);
            this.title = data.getString(EventData.Fields.DESCRIPTION);
            this.category = data.getInt(EventData.Fields.CATEGORY);
            this.latlng[0] = data.getDouble(EventData.Fields.LATITUDE);
            this.latlng[1] = data.getDouble(EventData.Fields.LONGITUDE);
            this.population = data.getInt(EventData.Fields.POPULATION);
            this.engagement = data.getInt(EventData.Fields.ENGAGEMENT);
            this.score = data.getDouble(EventData.Fields.SCORE);
            this.resIcon = AppConstants.CATEGORY_ICONS[category];
            this.resColor = AppConstants.CATEGORY_COLORS[category];
            this.myEvent = (owner.equals(UserData.ID));
        } catch (IllegalStateException | JsonSyntaxException | JSONException ex) {
            ex.printStackTrace();
        }
        updateRatings();
    }

    public String getId() {
        return id;
    }

    public String getLocationId() {
        return locationId;
    }

    @Override
    public LatLng getPosition() {
        return null;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    public int getResIcon() {
        return resIcon;
    }

    public int getResColor() {
        return resColor;
    }

    public int getRating() {
        return rating;
    }

    public int getScalingValue() {
        return scale_mod;
    }

    public double getLatitude() {
        return latlng[0];
    }

    public double getLongitude() {
        return latlng[1];
    }

    private void updateRatings() {
        this.score_mod = (score > (engagement * 0.5)) ? 2 : 1;
        int scalebase = population + engagement;
        scalebase = (scalebase > 100) ? 100 : scalebase;
        scalebase = (int)(scalebase * 0.0125)+1;
        this.scale_mod = scalebase;
        int usage_mod = (engagement * population) * score_mod;
        usage_mod = (usage_mod > 30) ? 30 : usage_mod;
        this.rating = usage_mod / 10;
    }

    public boolean isMyEvenet() {
        return myEvent;
    }
}
