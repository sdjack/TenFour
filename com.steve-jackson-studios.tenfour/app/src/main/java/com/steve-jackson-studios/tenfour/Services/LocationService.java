package com.steve-jackson-studios.tenfour.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonSyntaxException;
import com.steve-jackson-studios.tenfour.MainActivity;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "LocationService";
    private static final int UPDATE_INTERVAL = 60000;
    private static final int FASTEST_INTERVAL = 60000;
    private static final int LOCATION_DISPLACEMENT = 100;
    private static final int ACCURACY_THRESHOLD = 60;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private boolean currentlyProcessingLocation = false;
    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!currentlyProcessingLocation) {
            currentlyProcessingLocation = true;
            startTracking();
        }

        return START_NOT_STICKY;
    }

    private void startTracking() {
        //Log.d(TAG, "startTracking");

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    protected void saveLocation(Location location) {
        //Log.d(TAG, "saveLocation");
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("UserID", "");
        if (TextUtils.isEmpty(userId)) return;
        float latitude = (float) location.getLatitude();
        float longitude = (float) location.getLongitude();

        JSONObject postContent = new JSONObject();
        try {
            postContent.put("userId", userId);
            postContent.put("userLat", latitude);
            postContent.put("userLon", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.POST, AppConstants.SERVICE_URL + AppConstants.SERVICE_LOCATIONUPDATE, postContent, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        new ApiResponseTask(response).execute((Void) null);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        //Volley.newRequestQueue(getBaseContext()).add(jsonRequest);
    }

    private synchronized void saveResponseData(String loc, float lat, float lon) {
        //Log.d(TAG, "saveUserData values :::: ");
        try {
            SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("LocationID", loc);
            editor.putFloat("previousLatitude", lat);
            editor.putFloat("previousLongitude", lon);
            editor.apply();

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Notification n = new Notification.Builder(this)
                .setSmallIcon(R.drawable.tenfour_notification)
                .setContentTitle("MainActivity")
                .setContentText("There are new conversations close by!")
                .setContentIntent(resultPendingIntent)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (!location.hasAccuracy()) {
                return;
            }
            if (location.getAccuracy() <= ACCURACY_THRESHOLD) {
                currentlyProcessingLocation = false;
                saveLocation(location);
                stopLocationUpdates();
            }
        }
    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        //Log.d(TAG, "onConnected");

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(LOCATION_DISPLACEMENT);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Log.e(TAG, "onConnectionFailed");

        stopLocationUpdates();
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient connection has been suspend");
    }

    private class ApiResponseTask extends AsyncTask<Void, Void, Boolean> {

        private JSONObject resultObject = null;
        private String loc;
        private float lat;
        private float lon;

        ApiResponseTask(JSONObject response) {
            resultObject = response;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (resultObject != null) {
                try {
                    if (!resultObject.isNull("data")) {
                        final JSONArray data = resultObject.getJSONArray("data");
                        if (data.length() > 0) {
                            JSONObject row = (JSONObject) data.get(0);
                            if (!row.isNull("LOCATION_ID")) {
                                loc = row.getString("LOCATION_ID");
                                lat = (float)row.getDouble("LATITUDE");
                                lon = (float)row.getDouble("LONGITUDE");
                                return true;
                            }
                        }
                    }
                } catch (IllegalStateException | JsonSyntaxException ex) {
                    Log.e(TAG, "IllegalStateException", ex);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                saveResponseData(loc,lat,lon);
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
