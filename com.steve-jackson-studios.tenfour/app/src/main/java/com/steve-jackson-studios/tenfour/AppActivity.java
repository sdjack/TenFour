package com.steve-jackson-studios.tenfour;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.MapsInitializer;
import com.steve-jackson-studios.tenfour.Azure.CloudUploadTask;
import com.steve-jackson-studios.tenfour.Chat.ChatFeedFragment;
import com.steve-jackson-studios.tenfour.Chat.ChatPreviewFragment;
import com.steve-jackson-studios.tenfour.Chat.ImageViewDialog;
import com.steve-jackson-studios.tenfour.Chat.WebViewDialog;
import com.steve-jackson-studios.tenfour.Data.ChatPostData;
import com.steve-jackson-studios.tenfour.Gallery.GalleryFragment;
import com.steve-jackson-studios.tenfour.IO.FileIO;
import com.steve-jackson-studios.tenfour.IO.NetworkMonitor;
import com.steve-jackson-studios.tenfour.IO.RestClient;
import com.steve-jackson-studios.tenfour.IO.SocketIO;
import com.steve-jackson-studios.tenfour.Maps.GoogleMapFragment;
import com.steve-jackson-studios.tenfour.Media.GiphyApi;
import com.steve-jackson-studios.tenfour.Media.ImageUtility;
import com.steve-jackson-studios.tenfour.Media.MediaSelectActivity;
import com.steve-jackson-studios.tenfour.Misc.SplashScreen;
import com.steve-jackson-studios.tenfour.Navigation.NavigationFragment;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;
import com.steve-jackson-studios.tenfour.Profile.FilterFragment;
import com.steve-jackson-studios.tenfour.Profile.ProfileFragment;
import com.steve-jackson-studios.tenfour.Profile.ViewProfileDialog;
import com.steve-jackson-studios.tenfour.Receivers.GeoDataReceiver;
import com.steve-jackson-studios.tenfour.Receivers.GpsTrackerAlarmReceiver;
import com.steve-jackson-studios.tenfour.Sensors.Compass;
import com.steve-jackson-studios.tenfour.Services.FetchGeoDataIntentService;
import com.steve-jackson-studios.tenfour.Services.LocationUpdatesService;
import com.steve-jackson-studios.tenfour.Widgets.SlidingDrawer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.impl.PointImpl;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/**
 * Created by sjackson on 8/24/2017.
 * AppActivity
 */

public class AppActivity extends FragmentActivity implements RestClient.ResponseListener,
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener,
        NetworkMonitor.CallbackListener,
//        LocationListener,
        ImageViewDialog.CallbackListener,
        ChatFeedFragment.CallbackListener,
        NavigationFragment.CallbackListener,
        Dispatch.Listener,
        Dispatch.MessageListener {

    private static final String TAG = AppActivity.class.getName();

    private boolean preInitComplete = false;
    private boolean postInitComplete = false;
    private boolean mCreatingEvent = false;
    private boolean mResolvingError = false;
    private boolean pendingMediaSelected = false;
    private boolean cameraIntentActive = false;
    private boolean imageViewDialogActive = false;
    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;

    private AsyncLoadingTask loadingTask;
    private AsyncCameraTask cameraTask;
    private AppResolver appResolver;
    private NetworkMonitor netMonitor;
    private LocationManager locationManager;
    private SplashScreen splashScreen;
    private GoogleMapFragment mapFragment;
    private ChatFeedFragment chatFragment;
    private ProfileFragment profileFragment;
    private FilterFragment filterFragment;
    private ChatPreviewFragment previewFragment;
    private ImageViewDialog imageViewDialog;
    private WebViewDialog webViewDialog;
    private GeoDataReceiver geoDataReceiver;

    private View chatContainer;
    private View chatOverlay;
    private View previewContainer;

//    private GoogleApiClient googleApiClient;
    private LocationUpdatesService locationUpdatesService = null;
    private boolean serviceBound = false;
    private Compass compass;
    private Location lastLocation;
//    private LocationRequest locationRequest;
    private LocationReceiver locationReceiver;

    private SlidingDrawer verticalDrawerLayout;
    private DrawerLayout horizontalDrawerLayout;
    private LinearLayout profileDrawer;
    private LinearLayout filterDrawer;
    private LinearLayout verticalDrawerHandle;
    private RelativeLayout chatImageWrapper;
    private ImageView chatInputImage;
    private Button profileToggle;
    private Button searchToggle;
    private Button filterToggle;
    private Button locationToggle;
    private ImageButton chatToggle;

    private NavigationFragment nav;
    private ImageView networkIndicator;
    private ImageView servicesIndicator;
    private TextView gpsLoading;
    private TextView errorText;

    private String pendingImgPath;
    private Uri imageUri;
    private float ctMargin;

    //private BackgroundPowerSaver backgroundPowerSaver;

    private static final String EXTRA_RESULT_MEDIA = "result_media";

    public static Intent getIntent(Context context, String result) {
        Intent intent = new Intent(context, AppActivity.class);
        intent.putExtra(EXTRA_RESULT_MEDIA, result);
        return intent;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            locationUpdatesService = binder.getService();
            serviceBound = true;
            locationUpdatesService.requestLocationUpdates();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationUpdatesService = null;
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.app_layout);

        ImageView logo = (ImageView) findViewById(R.id.logo_anim);
        AnimationDrawable logoAnimation = (AnimationDrawable) logo.getBackground();
        logoAnimation.start();
        //Log.d(TAG, " STATETEST >>>>>>>>>>>>>>>>>>>> onCreate");

        if (savedInstanceState == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            //FacebookSdk.sdkInitialize(getApplicationContext());
            //AppEventsLogger.activateApp(this);

            mRequestingLocationUpdates = false;
            mLastUpdateTime = "";

            appResolver = new AppResolver(this);

            netMonitor = new NetworkMonitor(this);
            netMonitor.setCallbackListener(this);

            chatContainer = findViewById(R.id.chat_container);
            chatOverlay = findViewById(R.id.chat_overlay);
            chatImageWrapper = (RelativeLayout) findViewById(R.id.input_image_wrapper);
            chatInputImage = (ImageView) findViewById(R.id.input_image);
            previewContainer = findViewById(R.id.preview_container);
            verticalDrawerLayout = (SlidingDrawer) findViewById(R.id.vertical_drawer);
            horizontalDrawerLayout = (DrawerLayout) findViewById(R.id.app_drawer);
            errorText = (TextView) findViewById(R.id.error_text);
            profileDrawer = (LinearLayout) findViewById(R.id.profile_drawer);
            filterDrawer = (LinearLayout) findViewById(R.id.filter_drawer);
            //toolDrawer = (LinearLayout) findViewById(R.id.tool_drawer);
            verticalDrawerHandle = (LinearLayout) findViewById(R.id.vertical_drawer_handle);
            gpsLoading = (TextView) findViewById(R.id.map_loading_label);
            networkIndicator = (ImageView) findViewById(R.id.network_indicator);
            servicesIndicator = (ImageView) findViewById(R.id.services_indicator);
        }

        Dispatch.register(this);

        networkIndicator.setVisibility(View.GONE);
        servicesIndicator.setVisibility(View.GONE);
        gpsLoading.setVisibility(View.VISIBLE);

        if (!preInitComplete) {
            preInitComplete = true;

            final Window mRootWindow = this.getWindow();
            View rootView = mRootWindow.getDecorView().getRootView();
            Rect rect = new Rect();
            rootView.getWindowVisibleDisplayFrame(rect);
            AppConstants.SCREEN_WIDTH = rect.width();
            AppConstants.SCREEN_HEIGHT = rect.height();
            AppConstants.SCREEN_BOTTOM = rect.bottom;

//        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (mBluetoothAdapter == null) {
//            Toast.makeText(AppActivity.this, "Bluetooth Unavailable", Toast.LENGTH_SHORT).show();
//        } else {
//            if (!mBluetoothAdapter.isEnabled()) {
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//            }
//        }

            profileToggle = (Button) findViewById(R.id.profile_toggle_button);
            profileToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (horizontalDrawerLayout.isDrawerOpen(Gravity.END)) {
                        horizontalDrawerLayout.closeDrawer(Gravity.END);
                    }
                    if (horizontalDrawerLayout.isDrawerOpen(Gravity.START)) {
                        horizontalDrawerLayout.closeDrawer(Gravity.START);
                    } else {
                        horizontalDrawerLayout.openDrawer(Gravity.START);
                    }
                }
            });

            searchToggle = (Button) findViewById(R.id.search_toggle_button);
            searchToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (horizontalDrawerLayout.isDrawerOpen(Gravity.START)) {
                        horizontalDrawerLayout.closeDrawer(Gravity.START);
                    }
                    if (horizontalDrawerLayout.isDrawerOpen(Gravity.END)) {
                        horizontalDrawerLayout.closeDrawer(Gravity.END);
                    }
                    showMapSearch();
                }
            });

            filterToggle = (Button) findViewById(R.id.filter_toggle_button);
            filterToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (horizontalDrawerLayout.isDrawerOpen(Gravity.START)) {
                        horizontalDrawerLayout.closeDrawer(Gravity.START);
                    }
                    if (horizontalDrawerLayout.isDrawerOpen(Gravity.END)) {
                        horizontalDrawerLayout.closeDrawer(Gravity.END);
                    } else {
                        horizontalDrawerLayout.openDrawer(Gravity.END);
                    }
                }
            });

            locationToggle = (Button) findViewById(R.id.map_reset_button);
            locationToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (horizontalDrawerLayout.isDrawerOpen(Gravity.START)) {
                        horizontalDrawerLayout.closeDrawer(Gravity.START);
                    }
                    if (horizontalDrawerLayout.isDrawerOpen(Gravity.END)) {
                        horizontalDrawerLayout.closeDrawer(Gravity.END);
                    }
                    if (mapFragment != null && mapFragment.mapReady()) {
                        mapFragment.resetCamera();
                    }
                    checkLocationState();
                }
            });

            chatToggle = (ImageButton) findViewById(R.id.chat_toggle_button);
            chatToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (verticalDrawerLayout.isOpened()) {
                        verticalDrawerLayout.animateClose();
                    }
                    openChat();
                    checkLocationState();
                }
            });

            ImageButton rotateLeft = (ImageButton) findViewById(R.id.image_rotate_left_button);
            rotateLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AppConstants.PENDING_POST_BITMAP != null) {
                        AppConstants.PENDING_POST_BITMAP = ImageUtility.RotateBitmap(AppConstants.PENDING_POST_BITMAP, ImageUtility.ROTATE_LEFT);
                        chatInputImage.setImageBitmap(AppConstants.PENDING_POST_BITMAP);
                    }
                }
            });

            ImageButton rotateRight = (ImageButton) findViewById(R.id.image_rotate_right_button);
            rotateRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AppConstants.PENDING_POST_BITMAP != null) {
                        AppConstants.PENDING_POST_BITMAP = ImageUtility.RotateBitmap(AppConstants.PENDING_POST_BITMAP, ImageUtility.ROTATE_RIGHT);
                        chatInputImage.setImageBitmap(AppConstants.PENDING_POST_BITMAP);
                    }
                }
            });

            verticalDrawerLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (verticalDrawerLayout.isOpened()) {
                        verticalDrawerLayout.animateClose();
                    }
                    return false;
                }
            });

//            ctMargin = getResources().getDimension(R.dimen.chat_toggle_margin);
//
//            verticalDrawerLayout.setOnDrawerMovedListener(new SlidingDrawer.OnDrawerMoveListener() {
//                @Override
//                public void onDrawerMoved() {
//                    chatToggle.setY(verticalDrawerHandle.getY() - ctMargin);
//                }
//            });
//
//            final RelativeLayout appLayout = (RelativeLayout) findViewById(R.id.appView);
//            verticalDrawerLayout.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
//                @Override
//                public void onDrawerClosed() {
//                    chatToggle.setY(appLayout.getBottom() - chatToggle.getMeasuredHeight());
//                }
//            });

            profileFragment = ProfileFragment.newInstance(appResolver);
            filterFragment = FilterFragment.newInstance(appResolver);

//            eventListFragment = new EventListFragment();
//            eventListFragment.setCallbackListener(this);

            nav = new NavigationFragment();
            nav.setCallbackListener(this);

            getFragmentManager().beginTransaction()
                    .replace(R.id.nav_container, nav, "nav_fragment")
                    .replace(R.id.profile_drawer, profileFragment, "profile_fragment")
                    .replace(R.id.filter_drawer, filterFragment, "filter_fragment")
                    //.replace(R.id.tool_drawer, eventListFragment, "eventlist_fragment")
                    .commit();
            //backgroundPowerSaver = new BackgroundPowerSaver(this);

            compass = new Compass(this);

            locationReceiver = new LocationReceiver();

            geoDataReceiver = new GeoDataReceiver(new Handler());

//            googleApiClient = new GoogleApiClient.Builder(this)
//                    .addApi(LocationServices.API)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .build();

            AppConstants.PLATFORM_HAS_PERMISSIONS = true;
            //startAlarmManager();

            chatContainer.animate().translationY(AppConstants.SCREEN_HEIGHT).setDuration(10);
        }

        if (!checkPermissions()) {
            requestPermissions();
        }


        if (mapFragment == null || !postInitComplete) {
            loadingTask = new AsyncLoadingTask();
            loadingTask.execute((Void) null);
            SocketIO.connectSocket();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, " STATETEST >>>>>>>>>>>>>>>>>>>> onActivityResult");
        switch (requestCode) {
            case AppConstants.MEDIAACTIVITY_RESULT:
                pendingMediaSelected = (AppConstants.PENDING_POST_FILENAME != null);
                openChat();
                break;
            case AppConstants.REQUEST_IMAGE_CAPTURE:
                cameraTask = new AsyncCameraTask();
                cameraTask.execute((Void) null);
                break;
            case AppConstants.REQUEST_PLACE_PICKER:
                searchToggle.setVisibility(View.VISIBLE);
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(data, this);
                    String toastMsg = String.format("Place: %s", place.getName());
                    Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Log.d(TAG, " STATETEST >>>>>>>>>>>>>>>>>>>> onStart");
        Dispatch.register(this);
//        startGoogleAPI();
        bindService(new Intent(this, LocationUpdatesService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        if (preInitComplete) {
            compass.start();
            netMonitor.resume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, " STATETEST >>>>>>>>>>>>>>>>>>>> onResume");
        if (preInitComplete) {
            compass.start();
            netMonitor.resume();
            if (postInitComplete) {
                if (mapFragment == null) {
                    if (loadingTask != null && !loadingTask.isCancelled()) {
                        loadingTask.cancel(true);
                    }
                    loadingTask = new AsyncLoadingTask();
                    loadingTask.execute((Void) null);
                }
//                else {
//                    closeChat();
//                }
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
//        if (googleApiClient != null) {
//            if (googleApiClient.isConnected()) {
//                if (!mRequestingLocationUpdates) {
//                    startLocationUpdates();
//                }
//            } else {
//                startGoogleAPI();
//            }
//        }
    }

    @Override
    public void onPause() {
        //Log.d(TAG, " STATETEST >>>>>>>>>>>>>>>>>>>> onPause");
        //getFragmentManager().popBackStack();
//        if (googleApiClient != null && googleApiClient.isConnected()) {
//            stopLocationUpdates();
//        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
        if (preInitComplete) {
            compass.stop();
            netMonitor.stop();
            networkIndicator.setVisibility(View.GONE);
            servicesIndicator.setVisibility(View.GONE);
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        //Log.d(TAG, " STATETEST >>>>>>>>>>>>>>>>>>>> onStop");
        //mapFragment = null;
        Dispatch.unregister(this);
        if (preInitComplete) {
            compass.stop();
            netMonitor.stop();
        }
        if (loadingTask != null) {
            loadingTask.cancel(true);
        }
//        stopGoogleAPI();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        super.onStop();
    }

//    @Override
//    public void onDestroy() {
//        //Log.d(TAG, " STATETEST >>>>>>>>>>>>>>>>>>>> onDestroy");
////        stopGoogleAPI();
//        super.onDestroy();
//    }

//    @Override
//    protected void onRestart() {
//        //Log.d(TAG, " STATETEST >>>>>>>>>>>>>>>>>>>> onRestart");
//        super.onRestart();
//    }

    @Override
    public void finish() {
        //super.finish(); // do not call super
        moveTaskToBack(true); // move back
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(AppConstants.REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        outState.putParcelable(AppConstants.LOCATION_KEY, lastLocation);
        outState.putString(AppConstants.LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(AppActivity.this,
                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                AppConstants.REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            startLocationPermissionRequest();
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(R.id.appView);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == AppConstants.REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationUpdatesService.requestLocationUpdates();
            } else {
                showSnackbar(R.string.permission_denied_explanation, R.string.menu_settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    @Override
    public void onTransportNotification(int eventID) {
        switch (eventID) {
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
            case ObservedEvents.NOTIFY_USER_LOGGED_OUT:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                //finish();
                break;
            case ObservedEvents.USER_LOGOUT:
                appResolver.logout();
                break;
            case ObservedEvents.INITIALIZED:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (splashScreen == null) {
                            splashScreen = new SplashScreen();
                            getFragmentManager()
                                    .beginTransaction()
                                    .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                    .add(R.id.appView, splashScreen)
                                    .commitAllowingStateLoss();
                        }
                        profileFragment.refresh();
                    }
                });
                break;
            case ObservedEvents.NOTIFY_AVAILABLE_CHAT_DATA:
                if (chatFragment != null) {
                    chatFragment.refresh();
                }
                break;
            case ObservedEvents.NOTIFY_AVAILABLE_PREVIEW_DATA:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (previewFragment != null && previewContainer.getVisibility() == View.VISIBLE) {
                            previewFragment.refresh();
                        }
                    }
                });
                break;
            case ObservedEvents.NOTIFY_AVAILABLE_FRIENDS_DATA:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        profileFragment.refresh();
                    }
                });
                break;
            case ObservedEvents.NOTIFY_MAP_CAMERA_LOCKED:
                // DO STUFF
                break;
            case ObservedEvents.NOTIFY_MAP_CAMERA_UNLOCKED:
                // DO STUFF
                break;
            case ObservedEvents.REQUEST_OPEN_CHAT:
                openChat();
                break;
            case ObservedEvents.REQUEST_OPEN_PREVIEW:
                openChatPreview();
                break;
            case ObservedEvents.REQUEST_CLOSE_PREVIEW:
                closeChatPreview();
                break;
            case ObservedEvents.REQUEST_MAP_PREVIEW:
                if (mapFragment != null && !AppConstants.MAP_CHANGES_LOCKED) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapFragment.lockCamera();
                        }
                    });
                }
                break;
            case ObservedEvents.NOTIFY_AVAILABLE_NODE_DATA:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (AppConstants.INIT_LOCATION_FOUND) {
                            gpsLoading.setVisibility(View.GONE);
                        }
                        if (mapFragment != null && !AppConstants.MAP_CHANGES_LOCKED) {
                            mapFragment.refresh();
                        }
                    }
                });
                appResolver.setActiveNode();
                break;
            case ObservedEvents.NOTIFY_AVAILABLE_PROFILE_DATA:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideKeyboard();
                        profileFragment.refresh();
//                        if (mapFragment != null) {
//                            mapFragment.updateUserIcon();
//                        }
                    }
                });
                break;
            case ObservedEvents.REQUEST_CLOSE_KEYBOARD:
                hideKeyboard();
                break;
            case ObservedEvents.CHAT_MEDIA_UPLOADED:
                postToChat();
                break;
            case ObservedEvents.NOTIFY_MY_LOCATION_UPDATE:
                if (mapFragment != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapFragment.refreshUserIcon();
                        }
                    });
                }
                break;
            case ObservedEvents.NOTIFY_LOCATION_ID_INVALID:
                appResolver.getCurrentGeoFence(AppConstants.GPS_LATITUDE, AppConstants.GPS_LONGITUDE);
                break;
            case ObservedEvents.NOTIFY_MAJOR_LOCATION_CHANGED:
                if (AppConstants.INITIALIZATION_COMPLETE) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeChat();
                        }
                    });
                }
                break;
            case ObservedEvents.GLOBAL_SENSOR_UPDATE:
                if (mapFragment != null && !AppConstants.MAP_CHANGES_LOCKED) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapFragment.updateCompass();
                        }
                    });
                }
                break;
            case ObservedEvents.REQUEST_LAST_LOCATION:
                updateLocation();
                break;
            case ObservedEvents.GLOBAL_GPS_UPDATE:
                if (mapFragment != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapFragment.updateCamera();
                        }
                    });
                }
                break;
            case ObservedEvents.REQUEST_MAPJSON_DATA:
                appResolver.requestHeatMapData();
                break;
            case ObservedEvents.REQUEST_CLEAR_IMAGE_DATA:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(AppActivity.this).clearDiskCache();
                        Glide.get(AppActivity.this).clearMemory();
                    }
                });
                break;
            case ObservedEvents.MAP_READY:
                refreshLayout();
                break;
            case ObservedEvents.NOTIFY_SPLASH_SCREEN_FINISHED:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (splashScreen != null) {
                            getFragmentManager()
                                    .beginTransaction()
                                    .remove(splashScreen)
                                    .commitAllowingStateLoss();

                            splashScreen = null;
                        }
                    }
                });
                break;
            case ObservedEvents.NOTIFY_FILTERS_CHANGED:
                if (mapFragment != null) {
                    mapFragment.rebuild();
                }
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

    @Override
    public void onNavigation(int position) {
        hideKeyboard();
        verticalDrawerLayout.close();
        openChat();
        checkLocationState();
    }

    @Override
    public void onSubNavigation(int position) {
        switch (position) {
            case NavigationFragment.SUB_OPTION_0:
                showKeyboard();
                break;
//            case NavigationFragment.SUB_OPTION_1:
//                hideKeyboard();
//                showGallery();
//                break;
            case NavigationFragment.SUB_OPTION_2:
                hideKeyboard();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final ImageView imageView = (ImageView) findViewById(R.id.input_image);
                        if (imageView != null) {
                            Glide.with(AppActivity.this).load(R.drawable.placeholder_image)
                                    //.asBitmap()
                                    .into(imageView);
                            try {
                                dispatchTakePictureIntent();
                            } catch (IOException e) {
                            }
                        }
                    }
                });
                break;
            case NavigationFragment.SUB_OPTION_3:
                hideKeyboard();
                showGifGallery();
                break;
            case NavigationFragment.SUB_OPTION_4:
                hideKeyboard();
                showStickerGallery();
                break;
            case NavigationFragment.SUB_OPTION_5:
                closeChat();
                break;
        }
    }

    @Override
    public void onServiceResponse(int responseId, JSONArray responseData) {

    }

    @Override
    public void onErrorResponse() {

    }

    @Override
    public void onImageViewClosed() {
        imageViewDialogActive = false;
        Log.d(TAG, "onImageViewClosed ::: " + imageViewDialogActive);
    }

    @Override
    public void onBackPressed() {
        if (horizontalDrawerLayout != null && horizontalDrawerLayout.isDrawerOpen(profileDrawer)) {
            if (profileFragment != null && profileFragment.isEditing()) {
                profileFragment.endAllEditing();
            } else {
                horizontalDrawerLayout.closeDrawer(profileDrawer);
            }
            horizontalDrawerLayout.closeDrawer(filterDrawer);
        }
        if (verticalDrawerLayout != null && verticalDrawerLayout.isOpened()) {
            verticalDrawerLayout.animateClose();
        }
        if (previewContainer.getVisibility() == View.VISIBLE) {
            closeChatPreview();
        }
        //Log.d(TAG, "onBackPressed ::: " + imageViewDialogActive);
        if (imageViewDialog != null && imageViewDialogActive) {
            imageViewDialog.dismiss();
        } else if (chatFragment != null && chatContainer.getVisibility() == View.VISIBLE) {
            chatFragment.toggle();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.d(TAG, "onKeyDown " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (horizontalDrawerLayout != null && horizontalDrawerLayout.isDrawerOpen(profileDrawer)) {
                    if (profileFragment != null && profileFragment.isEditing()) {
                        profileFragment.endAllEditing();
                    } else {
                        horizontalDrawerLayout.closeDrawer(profileDrawer);
                    }
                    horizontalDrawerLayout.closeDrawer(filterDrawer);
                }
                if (verticalDrawerLayout != null && verticalDrawerLayout.isOpened()) {
                    verticalDrawerLayout.animateClose();
                }
                if (previewContainer.getVisibility() == View.VISIBLE) {
                    closeChatPreview();
                }
                //Log.d(TAG, "onKeyDown ::: " + imageViewDialogActive);
                if (imageViewDialog != null && imageViewDialogActive) {
                    imageViewDialog.dismiss();
                    return false;
                }
                if (chatFragment != null && chatContainer.getVisibility() == View.VISIBLE) {
                    chatFragment.toggle();
                    return false;
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    // BEGIN CUSTOM OVERRIDES

    @Override
    public void onChatClosed() {
        closeChat();
    }

    @Override
    public void onChatReload() {
        if (chatFragment == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideKeyboard();
                chatOverlay.setVisibility(View.GONE);
                chatFragment.refresh();
            }
        });
    }

    @Override
    public void onReplyClicked(ChatPostData replyChatPostData) {
        AppConstants.replyStepIn(replyChatPostData);
        if (chatFragment == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatFragment.refresh();
                //chatFragment.setStickyPost(userName, text, mediaUrl);
            }
        });
    }

    @Override
    public void onViewUserProfile(final ChatPostData userChatPostData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                horizontalDrawerLayout.closeDrawer(profileDrawer);
                horizontalDrawerLayout.closeDrawer(filterDrawer);
                ViewProfileDialog dialog = new ViewProfileDialog();
                dialog.setUserInfo(userChatPostData);
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                transaction.add(R.id.appView, dialog).addToBackStack(null).commit();
            }
        });
    }

    @Override
    public void onRequestFriendship(final ChatPostData userChatPostData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appResolver.requestFriendship(userChatPostData.userName);
            }
        });
    }

    @Override
    public void onAvatarClicked(final View view, final ChatPostData userChatPostData) {
        nav.toggleUserMenu(view, userChatPostData);
    }

    @Override
    public void onKarmaClicked(final View view, final String id) {
        nav.toggleKarmaMenu(view, id);
    }

    @Override
    public void onImageClicked(final String fileName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (imageViewDialog != null) {
                    imageViewDialog.dismiss();
                }
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                imageViewDialog = new ImageViewDialog();
                imageViewDialog.setFilePath(fileName);
                imageViewDialog.setCallbackListener(AppActivity.this);
                android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                transaction.add(R.id.appView, imageViewDialog).addToBackStack(null).commit();
                imageViewDialogActive = true;
            }
        });
    }

//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        locationRequest = LocationRequest.create();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(AppConstants.UPDATE_INTERVAL);
//        locationRequest.setFastestInterval(AppConstants.FATEST_INTERVAL);
//        locationRequest.setSmallestDisplacement(AppConstants.LOCATION_DISPLACEMENT);
//
//        Log.d(TAG, "onConnected :: locationRequest SET");
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "onConnected :: PERMISSION_GRANTED");
//            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
//                    .addLocationRequest(locationRequest);
//            PendingResult<LocationSettingsResult> result =
//                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
//                            builder.build());
//            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//                @Override
//                public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
//                    final Status status = locationSettingsResult.getStatus();
//                    final LocationSettingsStates states = locationSettingsResult.getLocationSettingsStates();
//                    switch (status.getStatusCode()) {
//                        case LocationSettingsStatusCodes.SUCCESS:
//                            updateLocation();
//                            startLocationUpdates();
//                            break;
//                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                            try {
//                                status.startResolutionForResult(
//                                        AppActivity.this,
//                                        AppConstants.REQUEST_CHECK_SETTINGS);
//                            } catch (IntentSender.SendIntentException e) {
//                                // DO NOTHING
//                            }
//                            break;
//                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                            // DO EVEN MORE NOTHING
//                            break;
//                    }
//                }
//            });
//
//            checkLocationState();
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        if (!AppConstants.LOGGED_IN) return;
//        //Log.d(TAG, ">>>>>>>>>>>>>> GPS <<<<<<<<<<<<<<<<    LOCATION  :::::  " + location.toString());
//        if (location.hasAccuracy()) {
//            float accuracy = location.getAccuracy();
//            if (AppConstants.isLocationUsable(accuracy)) {
//                updateLocation(location);
//            }
//        }
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        //Log.d(TAG, "onConnectionFailed  :::::  " + connectionResult.toString());
//        if (!mResolvingError) {
//            if (connectionResult.hasResolution()) {
//                try {
//                    mResolvingError = true;
//                    connectionResult.startResolutionForResult(this, AppConstants.REQUEST_RESOLVE_ERROR);
//                } catch (IntentSender.SendIntentException e) {
//                    googleApiClient.connect();
//                }
//            } else {
//                mResolvingError = true;
//                if (connectionResult.getErrorCode() == SERVICE_VERSION_UPDATE_REQUIRED) {
//                    GoogleApiAvailability api = GoogleApiAvailability.getInstance();
//                    int code = api.isGooglePlayServicesAvailable(this);
//                    api.getErrorDialog(this, code, SERVICE_VERSION_UPDATE_REQUIRED).show();
//                }
//            }
//        }
//    }

    @Override
    public boolean onInputSubmit(String inputText) {
        if ((inputText == null || TextUtils.isEmpty(inputText)) && TextUtils.isEmpty(AppConstants.PENDING_POST_FILENAME)) {
            return false;
        }
        if (!TextUtils.isEmpty(inputText)) {
            AppConstants.PENDING_POST_TEXT = inputText.trim();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatOverlay.setVisibility(View.GONE);
                chatImageWrapper.setVisibility(View.GONE);
            }
        });
        hideKeyboard();
        if (AppConstants.PENDING_POST_BITMAP != null) {
            new CloudUploadTask(AppActivity.this).execute();
        } else {
            postToChat();
        }
        return true;
    }

    @Override
    public void onClickWebLink(final String url) {
        if (!url.startsWith("http")) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                webViewDialog = WebViewDialog.newInstance(url);
                android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                transaction.add(R.id.appView, webViewDialog).addToBackStack(null).commit();
            }
        });
    }

    @Override
    public void onImageSelected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = (ImageView) findViewById(R.id.input_image);
                final ProgressBar progressBar = (ProgressBar) findViewById(R.id.image_loading_progress);
                RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.input_image_wrapper);
                progressBar.setVisibility(View.VISIBLE);
                wrapper.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
                chatOverlay.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onImageProcessed(final String filePath) {
        AppConstants.resetPendingBitmap();
        AppConstants.PENDING_POST_IMG_URI = Uri.parse(filePath);
        AppConstants.PENDING_POST_FILENAME = filePath;
        Log.d(TAG, AppConstants.PENDING_POST_FILENAME);
        setPendingPostImage();
    }

    // END CUSTOM OVERRIDES

    // BEGIN CUSTOM FUNCTIONS

    public boolean networkAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void refreshLayout() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                errorText.setVisibility(View.GONE);
                networkIndicator.setVisibility(View.GONE);
                servicesIndicator.setVisibility(View.GONE);
                if (preInitComplete) {
                    if (postInitComplete && !AppConstants.CHAT_IS_OPEN) {
                        profileToggle.setVisibility(View.VISIBLE);
                        searchToggle.setVisibility(View.VISIBLE);
                        filterToggle.setVisibility(View.VISIBLE);
                        locationToggle.setVisibility(View.VISIBLE);
                        chatToggle.setVisibility(View.VISIBLE);
                    } else {
                        profileToggle.setVisibility(View.GONE);
                        searchToggle.setVisibility(View.GONE);
                        filterToggle.setVisibility(View.GONE);
                        locationToggle.setVisibility(View.GONE);
                        chatToggle.setVisibility(View.GONE);
                    }
                    if (splashScreen != null) {
                        splashScreen.toggle();
                    }
                    if (AppConstants.INIT_LOCATION_FOUND) {
                        gpsLoading.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void showGallery() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GalleryFragment fragment = new GalleryFragment();
                fragment.setCallbackListener(AppActivity.this);
                getFragmentManager().beginTransaction()
                        .replace(R.id.chat_overlay, fragment, "activeMediaFragment")
                        .commit();
                chatOverlay.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showGifGallery() {
        Intent intent = MediaSelectActivity.getIntent(this, GiphyApi.TYPE_GIFS);
        startActivityForResult(intent, AppConstants.MEDIAACTIVITY_RESULT);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                GifFragment fragment = new GifFragment();
//                fragment.setCallbackListener(AppActivity.this);
//                getFragmentManager().beginTransaction()
//                        .replace(R.id.chat_overlay, fragment, "activeMediaFragment")
//                        .commit();
//                chatOverlay.setVisibility(View.VISIBLE);
//            }
//        });
    }

    private void showStickerGallery() {
        Intent intent = MediaSelectActivity.getIntent(this, GiphyApi.TYPE_STICKERS);
        startActivityForResult(intent, AppConstants.MEDIAACTIVITY_RESULT);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                StickerFragment fragment = new StickerFragment();
//                fragment.setCallbackListener(AppActivity.this);
//                getFragmentManager().beginTransaction()
//                        .replace(R.id.chat_overlay, fragment, "activeMediaFragment")
//                        .commit();
//                chatOverlay.setVisibility(View.VISIBLE);
//            }
//        });
    }

    public void showMapSearch() {
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(AppActivity.this);
            startActivityForResult(intent, AppConstants.REQUEST_PLACE_PICKER);
            searchToggle.setVisibility(View.GONE);
        } catch (GooglePlayServicesRepairableException e) {
            searchToggle.setVisibility(View.VISIBLE);
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), AppActivity.this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            searchToggle.setVisibility(View.VISIBLE);
            Toast.makeText(AppActivity.this, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void openChat() {
        AppConstants.CHAT_IS_OPEN = true;
        AppConstants.MAP_CHANGES_LOCKED = true;
        SocketIO.joinChannel();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                verticalDrawerLayout.close();

                nav.showInputView();

                profileToggle.setVisibility(View.GONE);
                searchToggle.setVisibility(View.GONE);
                filterToggle.setVisibility(View.GONE);
                locationToggle.setVisibility(View.GONE);
                chatToggle.setVisibility(View.GONE);

                chatFragment = new ChatFeedFragment();
                chatFragment.setCallbackListener(AppActivity.this);
                getFragmentManager().beginTransaction()
                        .replace(R.id.chat_content, chatFragment, "chat_fragment")
                        .commit();

                chatContainer.setVisibility(View.VISIBLE);
                chatContainer.animate().translationY(0).setDuration(200);
                chatContainer.requestLayout();
                if (pendingMediaSelected) {
                    pendingMediaSelected = false;
                    ImageView imageView = (ImageView) findViewById(R.id.input_image);
                    final ProgressBar progressBar = (ProgressBar) findViewById(R.id.image_loading_progress);
                    RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.input_image_wrapper);
                    progressBar.setVisibility(View.VISIBLE);
                    wrapper.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.VISIBLE);
                    chatOverlay.setVisibility(View.GONE);
                    AppConstants.resetPendingBitmap();
                    AppConstants.PENDING_POST_IMG_URI = Uri.parse(AppConstants.PENDING_POST_FILENAME);
                    setPendingPostImage();
                }
            }
        });
    }

    public void closeChat() {
        AppConstants.CHAT_IS_OPEN = false;
        AppConstants.PENDING_POST_FILENAME = null;
        AppConstants.MAP_CHANGES_LOCKED = false;
        SocketIO.leaveChannel();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                nav.hideInputView();

                profileToggle.setVisibility(View.VISIBLE);
                searchToggle.setVisibility(View.VISIBLE);
                filterToggle.setVisibility(View.VISIBLE);
                locationToggle.setVisibility(View.VISIBLE);
                chatToggle.setVisibility(View.VISIBLE);

                verticalDrawerLayout.close();
                chatOverlay.setVisibility(View.GONE);
                chatContainer.animate().translationY(AppConstants.SCREEN_HEIGHT).setDuration(100);
                chatContainer.setVisibility(View.GONE);
                chatContainer.requestLayout();
            }
        });
        clearPostImage();
    }

    public void openChatPreview() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (previewFragment == null) {
                    previewFragment = ChatPreviewFragment.newInstance(appResolver);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.preview_container, previewFragment, "preview_fragment")
                            .commit();
                }
                previewContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    public void closeChatPreview() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                (findViewById(R.id.input_image_wrapper)).setVisibility(View.GONE);
                previewContainer.setVisibility(View.GONE);
                if (mapFragment != null) {
                    mapFragment.unlockCamera();
                }
            }
        });
    }

    private void dispatchTakePictureIntent() throws IOException {
        AppConstants.resetPendingBitmap();
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = ImageUtility.CreateImageFile();
                pendingImgPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            if (photoFile != null) {
                //closeChat();
                cameraIntentActive = true;
                imageUri = FileProvider.getUriForFile(this,
                        "com.steve-jackson-studios.tenfour.fileprovider",
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                startActivityForResult(cameraIntent, AppConstants.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void postToChat() {
        if (!TextUtils.isEmpty(AppConstants.PENDING_POST_TEXT) || !TextUtils.isEmpty(AppConstants.PENDING_POST_FILENAME)) {
            final String postText = (TextUtils.isEmpty(AppConstants.PENDING_POST_TEXT)) ? "" : AppConstants.PENDING_POST_TEXT.replace("'", "\u2019");
            final String postMedia = (TextUtils.isEmpty(AppConstants.PENDING_POST_FILENAME)) ? "" : AppConstants.PENDING_POST_FILENAME;
            final int isSticker = AppConstants.PENDING_POST_STICKER;
            appResolver.queueStreamData(postText, postMedia, isSticker);
        }
        AppConstants.resetPendingInputValues();
        clearPostImage();
    }

    private void clearPostImage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ImageView imageView = (ImageView) findViewById(R.id.input_image);
                if (imageView != null) {
                    Glide.with(AppActivity.this).load(R.drawable.placeholder_image)
                            //.asBitmap()
                            .into(imageView);
                    (findViewById(R.id.input_image_wrapper)).setVisibility(View.GONE);
                }
            }
        });
    }

    public void setPendingPostImage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = (ImageView) findViewById(R.id.input_image);
                final ProgressBar progressBar = (ProgressBar) findViewById(R.id.image_loading_progress);
                if (!AppConstants.PENDING_POST_FILENAME.endsWith("gif")) {
                    if (null == AppConstants.PENDING_POST_BITMAP) {
                        try {
                            AppConstants.PENDING_POST_BITMAP = ImageUtility.GetBitmapFromUri(AppActivity.this, AppConstants.PENDING_POST_IMG_URI);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    imageView.setImageBitmap(AppConstants.PENDING_POST_BITMAP);
                    (findViewById(R.id.image_rotate_left_button)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.image_rotate_right_button)).setVisibility(View.VISIBLE);
                } else {
                    Glide.with(AppActivity.this).load(AppConstants.PENDING_POST_IMG_URI.toString())
                            //.asBitmap()
                            //.dontAnimate()
                            .thumbnail(0.5F)
                            //.centerCrop()
                            //.placeholder(R.drawable.placeholder_image)
                            .into(imageView);
                    (findViewById(R.id.image_rotate_left_button)).setVisibility(View.GONE);
                    (findViewById(R.id.image_rotate_right_button)).setVisibility(View.GONE);
                }
            }
        });
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean checkLocationState() {
        if (!isLocationEnabled()) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Enable Location")
                    .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                            "use this app")
                    .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        }
                    });
            dialog.show();
        }
        return isLocationEnabled();
    }

//    protected void startLocationUpdates() {
//        LocationServices.FusedLocationApi.requestLocationUpdates(
//                googleApiClient, locationRequest, this);
//    }
//
//    protected void stopLocationUpdates() {
//        LocationServices.FusedLocationApi.removeLocationUpdates(
//                googleApiClient, this);
//    }

//    protected void startGoogleAPI() {
//        if (googleApiClient != null) {
//            googleApiClient.connect();
//        }
//    }
//
//    protected void stopGoogleAPI() {
//        if (googleApiClient != null) {
//            googleApiClient.disconnect();
//        }
//    }

    protected void updateLocation() {
        updateLocation(lastLocation);
    }

    protected void updateLocation(Location location) {
        if (location != null) {
            lastLocation = location;
        }
        if (lastLocation != null) {
            float lastLat = (float) lastLocation.getLatitude();
            float lastLon = (float) lastLocation.getLongitude();
            float lastAlt = (float) (lastLocation.getAltitude() * 3.28);
            if (AppConstants.GPS_LATITUDE != lastLat || AppConstants.GPS_LONGITUDE != lastLon || AppConstants.GPS_ALTITUDE != lastAlt) {
                AppConstants.GPS_LATITUDE = lastLat;
                AppConstants.GPS_LONGITUDE = lastLon;
                AppConstants.GPS_ALTITUDE = lastAlt;
                AppConstants.POINT_OF_ORIGIN = new PointImpl(lastLat, lastLon, SpatialContext.GEO);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (AppConstants.locationCanUpdate()) {
                            gpsLoading.setVisibility(View.VISIBLE);
                        }
                        if (mapFragment != null) {
                            mapFragment.updateCamera();
                        }
                    }
                });

                appResolver.updateLocation();
                //startGeoDataService();
            }
        }
    }

    private void startAlarmManager() {
        Context context = getBaseContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent gpsTrackerIntent = new Intent(context, GpsTrackerAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                3600000,
                pendingIntent);
    }

    private void startGeoDataService() {
        Intent intent = new Intent(this, FetchGeoDataIntentService.class);
        intent.putExtra(AppConstants.RECEIVER, geoDataReceiver);
        intent.putExtra(AppConstants.LOCATION_DATA_EXTRA, lastLocation);
        startService(intent);
    }

    // END CUSTOM FUNCTIONS

    /**
     * asynchronous task to fetch feed data
     */
    private class AsyncLoadingTask extends AsyncTask<Void, Void, Boolean> {

        AsyncLoadingTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (!AppConstants.readyForUse()) {
                appResolver.initialize();
                return false;
            } else {
                try {
                    JSONArray jsonArray = FileIO.loadRawJsonArray(AppActivity.this, R.raw.geonames_usa);
                    if (null != jsonArray && jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject row = (JSONObject) jsonArray.get(i);
                            if (!row.isNull("MAJOR")) {
                                String major = row.getString("MAJOR");
                                String minor = row.getString("METRO");
                                if (AppConstants.GEOFENCE_NAMES.get(major) == null) {
                                    AppConstants.GEOFENCE_NAMES.put(major, new TreeMap<String, JSONObject>());
                                }
                                AppConstants.GEOFENCE_NAMES.get(major).put(minor, row);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                appResolver.postInitialize();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            loadingTask = null;
            if (success) {
                updateLocation();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                postInitComplete = true;
                                networkIndicator.setVisibility(View.GONE);
                                servicesIndicator.setVisibility(View.GONE);

                                MapsInitializer.initialize(AppActivity.this);

                                if (mapFragment == null) {
                                    mapFragment = GoogleMapFragment.newInstance(appResolver);
                                    getFragmentManager().beginTransaction()
                                            .replace(R.id.map_container, mapFragment)
                                            .commitAllowingStateLoss();
                                }
                            }
                        });
                    }
                }, 4000);
            }
        }

        @Override
        protected void onCancelled() {
            loadingTask = null;
        }
    }

    /**
     * asynchronous task to fetch feed data
     */
    private class AsyncCameraTask extends AsyncTask<Void, Void, Boolean> {

        AsyncCameraTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                AppConstants.PENDING_POST_BITMAP = ImageUtility.GetBitmapFromUri(AppActivity.this, imageUri);
                AppConstants.PENDING_IMAGE_FROM_GALLERY = false;
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            cameraTask = null;
            if (success) {
                if (AppConstants.PENDING_POST_BITMAP != null) {
                    AppConstants.PENDING_POST_IMG_URI = imageUri;
                    AppConstants.PENDING_POST_FILENAME = imageUri.toString();
                    imageUri = null;
                    cameraIntentActive = false;
                    openChat();
                    onImageSelected();
                    setPendingPostImage();
                }
            }
        }

        @Override
        protected void onCancelled() {
            cameraTask = null;
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                updateLocation(location);
            }
        }
    }

    //    @Override
//    public void onNotifyFriendRequest() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                noticeContainer.setVisibility(View.VISIBLE);
//                NoticeFragment fragment = new NoticeFragment();
//                getFragmentManager().beginTransaction()
//                        .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
//                        .add(R.id.dialog_container, fragment)
//                        .addToBackStack(null)
//                        .commitAllowingStateLoss();
//            }
//        });
//    }

    //    private class FriendItemClickListener implements ListView.OnItemClickListener {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            selectFriendItem(position);
//        }
//    }

//    private void selectFriendItem(int position) {
//        mFriendsList.setItemChecked(position, true);
//        horizontalDrawerLayout.closeDrawer(profileDrawer);
//    }

//    private class RequestItemClickListener implements ListView.OnItemClickListener {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            selectRequestItem(position);
//        }
//    }
//
//    private void selectRequestItem(int position) {
//        mRequestList.setItemChecked(position, true);
//        horizontalDrawerLayout.closeDrawer(profileDrawer);
//    }

    //    @Override
//    public void onChatCreated(String title, int category) {
//        appResolver.createNewEvent(title, category);
//    }
//
//    @Override
//    public void onEventSelected(String eventId, String titleText) {
//        AppConstants.EVENT_ID = eventId;
//        AppConstants.CHAT_TITLE = titleText;
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                eventListFragment.refresh();
//                if (chatFragment != null && AppConstants.CHAT_IS_OPEN) {
//                    horizontalDrawerLayout.closeDrawer(toolDrawer);
//                    chatFragment.refresh();
//                }
//            }
//        });
//    }
}
