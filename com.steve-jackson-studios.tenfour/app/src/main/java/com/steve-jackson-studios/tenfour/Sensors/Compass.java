package com.steve-jackson-studios.tenfour.Sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.steve-jackson-studios.tenfour.AppConstants;

/**
 * The type Compass.
 */
public class Compass implements SensorEventListener {

    private static final String TAG = "Compass";

    //private GeomagneticField geomagneticField;
    private final SensorManager mSensorManager;

    private final float[] mRotationMatrix = new float[16];
    private final float[] mOrientation = new float[9];
    private final float[] azimuthCollection = new float[9];
    private int collectionIndex = 0;

    /**
     * Instantiates a new Compass.
     *
     * @param context the context
     */
    public Compass(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * Start.
     */
    public void start() {
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * Stop.
     */
    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                SensorManager.getOrientation(mRotationMatrix, mOrientation);
                getHeading();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void getHeading() {
        float currentHeading = (float) Math.round(((Math.toDegrees(mOrientation[0] + 360) % 360) * 100) / 100);
        azimuthCollection[collectionIndex] = currentHeading;

        if (collectionIndex == 8) {
            collectionIndex = 0;
        } else {
            collectionIndex++;
        }

        if (this.azimuthCollection.length >= 9) {
            float total = 0;
            for (int i = 0; i < 9; i++) {
                total += azimuthCollection[i];
            }

            float g = (float) Math.floor(Math.abs(((mOrientation[1] + 0.01) * 100) * 0.525));
            g = (g < 0) ? 0 : (g > 90) ? 90 : g;

            AppConstants.setSensorData((total / 9), g);
        }
    }

//    public void onLocationChanged(Location location) {
//        geomagneticField = new GeomagneticField((float) location.getLatitude(),
//                (float) location.getLongitude(), (float) location.getAltitude(),
//                location.getTime());
//    }
}