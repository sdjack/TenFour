package com.steve-jackson-studios.tenfour.Services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * Created by sjackson on 7/25/2017.
 * FetchGeoDataIntentService
 */

public class FetchGeoDataIntentService extends IntentService {
    private static final String TAG = "FetchGeoDataIS";

    private ResultReceiver mReceiver;

    public FetchGeoDataIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        mReceiver = intent.getParcelableExtra(AppConstants.RECEIVER);

        if (mReceiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }

        Location location = intent.getParcelableExtra(AppConstants.LOCATION_DATA_EXTRA);

        if (location != null) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1);
            } catch (IOException ioException) {
                errorMessage = getString(R.string.service_not_available);
                Log.e(TAG, errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                errorMessage = getString(R.string.invalid_lat_long_used);
                Log.e(TAG, errorMessage + ". " +
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " + location.getLongitude(), illegalArgumentException);
            }

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String country = address.getCountryCode();
                String admin = address.getAdminArea();
                deliverResultToReceiver(AppConstants.SUCCESS_RESULT, country, admin);
            }
        }
    }

    private void deliverResultToReceiver(int resultCode, String c, String a) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.RESULT_GEODATA_COUNTRY, c);
        bundle.putString(AppConstants.RESULT_GEODATA_ADMIN, a);
        mReceiver.send(resultCode, bundle);
    }
}
