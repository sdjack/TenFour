package com.steve-jackson-studios.tenfour.Services;

import android.content.Context;
import android.location.Location;

import com.steve-jackson-studios.tenfour.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by sjackson on 4/27/2018.
 *
 */

public class LocationServiceUtils {
    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    public static String getLocationTitle(Context context) {
        return context.getString(R.string.gps_location,
                DateFormat.getDateTimeInstance().format(new Date()));
    }
}
