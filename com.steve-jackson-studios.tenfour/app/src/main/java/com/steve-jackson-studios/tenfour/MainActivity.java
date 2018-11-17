package com.steve-jackson-studios.tenfour;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.steve-jackson-studios.tenfour.Data.DataHelper;
import com.steve-jackson-studios.tenfour.IO.NetworkMonitor;
import com.steve-jackson-studios.tenfour.Login.AuthActivity;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

/**
 * Created by sjackson on 8/1/2016.
 * MainActivity
 */
public class MainActivity extends FragmentActivity implements NetworkMonitor.CallbackListener,
        Dispatch.Listener,
        Dispatch.MessageListener {

    private static final String TAG = MainActivity.class.getName();

    private NetworkMonitor netMonitor;
    private ImageView networkIndicator;
    private ImageView servicesIndicator;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataHelper.loadDataBundleFile(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_layout);

        ImageView logo = (ImageView) findViewById(R.id.logo_anim);
        AnimationDrawable logoAnimation = (AnimationDrawable) logo.getBackground();
        logoAnimation.start();

        if (savedInstanceState == null) {
            AppConstants.INITIALIZATION_COMPLETE = false;
        }

        Dispatch.register(this);

        netMonitor = new NetworkMonitor(this);
        netMonitor.setCallbackListener(this);
        errorText = (TextView) findViewById(R.id.error_text);
        networkIndicator = (ImageView) findViewById(R.id.network_indicator);
        servicesIndicator = (ImageView) findViewById(R.id.services_indicator);

        networkIndicator.setVisibility(View.GONE);
        servicesIndicator.setVisibility(View.GONE);

        checkRequestPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "ACTIVITY CODE: "+requestCode);
        switch (requestCode) {
            case AppConstants.AUTHACTIVITY_RESULT:
                dispatchAppActivity();
                break;
            case AppConstants.APPACTIVITY_RESULT:
                dispatchAuthActivity();
                break;
            case AppConstants.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(MainActivity.this, "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case AppConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(android.Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.WRITE_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                        && perms.get(android.Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    dispatchAuthActivity();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkRequestPermissions() {
        final List<String> permissionsList = new ArrayList<>();
        addPermission(permissionsList, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        addPermission(permissionsList, android.Manifest.permission.ACCESS_FINE_LOCATION);
        addPermission(permissionsList, android.Manifest.permission.READ_CONTACTS);
        addPermission(permissionsList, android.Manifest.permission.WRITE_CONTACTS);
        addPermission(permissionsList, android.Manifest.permission.CAMERA);
        addPermission(permissionsList, android.Manifest.permission.BLUETOOTH);
        addPermission(permissionsList, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        addPermission(permissionsList, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionsList.size() > 0) {
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    AppConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);

            return;
        }
        dispatchAuthActivity();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
        }
    }

    @Override
    public void onNetworkAvailable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                networkIndicator.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onNetworkUnavailable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                networkIndicator.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onServicesAvailable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                servicesIndicator.setVisibility(View.GONE);
            }
        });
    }

    public void onServicesUnavailable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                servicesIndicator.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        netMonitor.resume();
    }

    @Override
    public void onStop() {
        netMonitor.stop();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        netMonitor.resume();
    }

    @Override
    public void onPause() {
        netMonitor.stop();
        networkIndicator.setVisibility(View.GONE);
        servicesIndicator.setVisibility(View.GONE);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Dispatch.unregister(this);
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onTransportNotification(int eventID) {
        switch (eventID) {
            case ObservedEvents.USER_LOGOUT:
                dispatchAuthActivity();
                break;
            case ObservedEvents.NETWORK_AVAILABLE:
                onNetworkAvailable();
                break;
            case ObservedEvents.NETWORK_UNAVAILABLE:
                onNetworkUnavailable();
                break;
            case ObservedEvents.SERVICES_AVAILABLE:
                onServicesAvailable();
                break;
            case ObservedEvents.SERVICES_UNAVAILABLE:
                onServicesUnavailable();
                break;
        }
    }

    @Override
    public void onTransportNotification(int eventID, String... args) {
        switch (eventID) {
            case ObservedEvents.NOTIFY_ERROR_MESSAGE:
                final String msg = args[0];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText(msg);
                        AppConstants.STATUS_MESSAGE = "";
                    }
                });
                break;
        }
    }

    private void dispatchAuthActivity() {
        AppConstants.ATTEMPTED_LOGIN_TYPE = 0;
        Intent intent = new Intent(this, AuthActivity.class);
        startActivityForResult(intent, AppConstants.AUTHACTIVITY_RESULT);
    }

    private void dispatchAppActivity() {
        Intent intent = new Intent(this, AppActivity.class);
        startActivityForResult(intent, AppConstants.APPACTIVITY_RESULT);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}
