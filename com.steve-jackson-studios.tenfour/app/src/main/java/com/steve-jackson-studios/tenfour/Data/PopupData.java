package com.steve-jackson-studios.tenfour.Data;

import com.steve-jackson-studios.tenfour.AppConstants;

/**
 * Created by sjackson on 6/15/2017.
 * PopupData
 */

public class PopupData {

    public final String locationId;
    public final String message;
    public final float latitude;
    public final float longitude;

    public PopupData(String id, String m, String a, float lat, float lon) {
        this.locationId = id;
        this.message = m;
        this.latitude = lat;
        this.longitude = lon;
    }
}
