package com.steve-jackson-studios.tenfour.Receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import com.steve-jackson-studios.tenfour.AppConstants;

/**
 * Created by sjackson on 7/25/2017.
 * GeoDataReceiver
 */

public class GeoDataReceiver extends ResultReceiver {

    public GeoDataReceiver(Handler handler) {
        super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        Log.d("GeoDataReceiver", "onReceiveResult = " + resultCode);
        if (resultCode == AppConstants.SUCCESS_RESULT) {
            String country = resultData.getString(AppConstants.RESULT_GEODATA_COUNTRY);
            AppConstants.GPS_COUNTRY = (country.equals("US")) ? "USA" : country;
            AppConstants.GPS_ADMIN = resultData.getString(AppConstants.RESULT_GEODATA_ADMIN);
        }
    }
}
