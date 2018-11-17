package com.steve-jackson-studios.tenfour.Maps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.AppResolver;
import com.steve-jackson-studios.tenfour.Data.ChatPostData;
import com.steve-jackson-studios.tenfour.Data.FriendData;
import com.steve-jackson-studios.tenfour.Data.FriendEntity;
import com.steve-jackson-studios.tenfour.Data.HeatmapData;
import com.steve-jackson-studios.tenfour.Data.HeatmapLocationData;
import com.steve-jackson-studios.tenfour.Data.MapChatData;
import com.steve-jackson-studios.tenfour.Data.MapData;
import com.steve-jackson-studios.tenfour.Data.MapSubdivisionData;
import com.steve-jackson-studios.tenfour.Data.UserData;
import com.steve-jackson-studios.tenfour.Maps.Entities.EventEntity;
import com.steve-jackson-studios.tenfour.Maps.Entities.PopupEntity;
import com.steve-jackson-studios.tenfour.Maps.Entities.UserEntity;
import com.steve-jackson-studios.tenfour.Misc.ResolverFragment;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;
import com.steve-jackson-studios.tenfour.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by sjackson on 5/26/2017.
 * GoogleMapFragment
 */

public class GoogleMapFragment extends ResolverFragment implements OnMapReadyCallback {

    private static final String TAG = "GoogleMapFragment";
    private static final int HEATMAP_INTERVAL = 30;
    private static final int POPUP_INTERVAL = 20;

    private final Map<String, MapSubdivisionData> areaCache = new HashMap<>();
    private final Map<String, UserEntity> friendsCache = new HashMap<>();
    private final ArrayList<PopupEntity> popupCache = new ArrayList<>();

    private Context context;
    private MarkerIconGenerator mIconGenerator;
    private FriendIconGenerator mFriendIconGenerator;
    private EventClusterManager eventClusterManager;

    private boolean MAP_READY = false;
    private boolean CAMERA_READY = false;
    private boolean CAMERA_LOCK = false;
    private boolean PENDING_LOCK = false;
    private boolean PENDING_GESTURE = false;
    private boolean POPUPS_UNLOCKED = AppConstants.POPUPS_ENABLED;

    private String HEATMAP_UUID;
    private String POPUP_UUID;

    private float currentZoom = AppConstants.MAP_DEFAULT_ZOOM_LEVEL;
    private double zoomScale = 1;

    private MapView mapView;
    private UserEntity myEntity;
    private LatLng currentTarget;
    private CameraPosition lastCameraPosition;
    private HeatmapTileProvider heatmapProvider;
    private TileOverlay heatMap;
    private GoogleMap googleMap;
    private GoogleMap.CancelableCallback cameraCallback;
    private GoogleMap.CancelableCallback lockCameraCallback;
    private GoogleMap.CancelableCallback unlockCameraCallback;
    private ScheduledExecutorService heatmapTaskExecutor;
    private ScheduledExecutorService popupTaskExecutor;

    public static GoogleMapFragment newInstance(AppResolver appResolver) {

        GoogleMapFragment instance = new GoogleMapFragment();
        instance.setResolver(appResolver);

        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.context = getActivity();

        cameraCallback = new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                CAMERA_READY = true;
            }

            @Override
            public void onCancel() {
                CAMERA_READY = true;
            }
        };
        lockCameraCallback = new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                finalizeCameraLock();
            }

            @Override
            public void onCancel() {
                finalizeCameraLock();
            }
        };
        unlockCameraCallback = new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                finalizeCameraUnlock();
            }

            @Override
            public void onCancel() {
                finalizeCameraUnlock();
            }
        };

        mIconGenerator = new MarkerIconGenerator(context);
        View markerView = inflater.inflate(R.layout.map_pin_layout, null);
        mIconGenerator.setContentView(markerView);

        mFriendIconGenerator = new FriendIconGenerator(context);
        View markerFView = inflater.inflate(R.layout.map_friend_layout, null);
        mFriendIconGenerator.setContentView(markerFView);

        View v = inflater.inflate(R.layout.map_layout, container, false);
        mapView = (MapView) v.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        AppConstants.MAP_ACTIVE_SUBDIVISION_ID = AppConstants.SUBDIVISION_ID;
        AppConstants.MAP_ACTIVE_LOCATION_ID = AppConstants.LOCATION_ID;

        heatmapTaskExecutor = Executors.newScheduledThreadPool(5);
        heatmapTaskExecutor.scheduleAtFixedRate(new HeatmapTask(), 0, HEATMAP_INTERVAL, TimeUnit.SECONDS);

        popupTaskExecutor = Executors.newScheduledThreadPool(5);
        popupTaskExecutor.scheduleAtFixedRate(new PopupTask(), 0, POPUP_INTERVAL, TimeUnit.SECONDS);

        return v;
    }

    @Override
    public void onDestroyView() {
        heatmapTaskExecutor.shutdown();
        popupTaskExecutor.shutdown();
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
        System.gc();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * Update compass.
     */
    public void updateCompass() {
        if (!MAP_READY || !CAMERA_READY || googleMap == null) return;
        if (CAMERA_LOCK) {
            CAMERA_READY = false;
            googleMap.stopAnimation();
            CameraPosition oldPos = googleMap.getCameraPosition();
            CameraPosition currentPlace = new CameraPosition.Builder(oldPos)
                    .target(currentTarget)
                    .zoom(oldPos.zoom)
                    .bearing(AppConstants.COMPASS_ANGLE)
                    .tilt(90 - AppConstants.TILT_ANGLE)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace), 1500, cameraCallback);
            return;
        }
        if (!AppConstants.MAP_FOLLOW_MODE) {
            if (PENDING_LOCK) {
                updateCamera();
            }
            return;
        }
        CAMERA_READY = false;
        googleMap.stopAnimation();
        CameraPosition oldPos = googleMap.getCameraPosition();
        CameraPosition currentPlace = new CameraPosition.Builder(oldPos)
                //.target(myEntity.getLocation())
                .bearing(AppConstants.COMPASS_ANGLE)
                .tilt(90 - AppConstants.TILT_ANGLE)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace), 1500, cameraCallback);

    }

    /**
     * Update camera.
     */

    private void setSnapZoom() {
        currentZoom = (float)Math.floor(currentZoom * 2) / 2.0f;
    }

    public void snapZoom() {
        if (googleMap == null || CAMERA_LOCK) return;
        setSnapZoom();
        googleMap.stopAnimation();
        CameraPosition currentPlace = new CameraPosition.Builder(googleMap.getCameraPosition())
                .target(currentTarget)
                .zoom(currentZoom)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
    }

    public void updateCamera() {
        if (!MAP_READY || !CAMERA_READY || CAMERA_LOCK || googleMap == null || myEntity == null) return;
        //Log.d("DEVDEBUG", "updateCamera");
        myEntity.setLocation(new LatLng(AppConstants.GPS_LATITUDE, AppConstants.GPS_LONGITUDE));
        myEntity.update();
        googleMap.stopAnimation();
        CameraPosition oldPos = googleMap.getCameraPosition();
        CameraPosition currentPlace;
        CAMERA_READY = false;

        if (!AppConstants.MAP_FOLLOW_MODE) {
            PENDING_LOCK = false;
            currentPlace = new CameraPosition.Builder(oldPos)
                    .bearing(0)
                    .tilt(0)
                    .build();
        } else {
            currentPlace = new CameraPosition.Builder(oldPos)
                    .target(myEntity.getLocation())
                    .build();
        }
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace), 100, cameraCallback);
    }

    public void fastUpdateCamera() {
        if (googleMap == null || CAMERA_LOCK || myEntity == null) return;
        //Log.d("DEVDEBUG", "fastUpdateCamera");
        myEntity.setLocation(new LatLng(AppConstants.GPS_LATITUDE, AppConstants.GPS_LONGITUDE));
        myEntity.update();
        googleMap.stopAnimation();
        CameraPosition currentPlace = new CameraPosition.Builder(googleMap.getCameraPosition())
                .target(myEntity.getLocation())
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
    }

    /**
     * Reset camera position.
     */
    public void resetCamera() {
        if (!MAP_READY || !CAMERA_READY || googleMap == null || myEntity == null) return;
        if (CAMERA_LOCK) {
            Dispatch.triggerEvent(ObservedEvents.REQUEST_CLOSE_PREVIEW);
        } else {
            //Log.d("DEVDEBUG", "resetCamera");
            googleMap.stopAnimation();
            eventClusterManager.cluster();
            currentZoom = AppConstants.MAP_DEFAULT_ZOOM_LEVEL;
            PENDING_GESTURE = false;
            PENDING_LOCK = false;
            CAMERA_READY = false;
            AppConstants.MAP_FOLLOW_MODE = true;
            POPUPS_UNLOCKED = true;
            zoomScale = 1;
            myEntity.setLocation(new LatLng(AppConstants.GPS_LATITUDE, AppConstants.GPS_LONGITUDE));
            myEntity.update();
            if (heatmapProvider != null) {
                heatmapProvider.setOpacity(0.6f);
            }
            addHeatMap();
            CameraPosition currentPlace = new CameraPosition.Builder()
                    .target(myEntity.getLocation())
                    .bearing(0)
                    .tilt(0)
                    .zoom(currentZoom)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace), 20, cameraCallback);
        }
    }

    public void lockCamera() {
        if (googleMap == null || CAMERA_LOCK) return;
        CAMERA_LOCK = true;
        //Log.d("DEVDEBUG", "lockCamera");
        googleMap.stopAnimation();
        if (heatmapProvider != null) {
            heatmapProvider.setOpacity(0f);
        }
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomGesturesEnabled(false);
        uiSettings.setScrollGesturesEnabled(false);
        lastCameraPosition = googleMap.getCameraPosition();
        CameraPosition currentPlace = new CameraPosition.Builder()
                .target(currentTarget)
                .bearing(AppConstants.COMPASS_ANGLE)
                .tilt(90 - AppConstants.TILT_ANGLE)
                .zoom(AppConstants.MAP_PREVIEW_ZOOM_LEVEL)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace), 20, lockCameraCallback);
    }

    private void finalizeCameraLock() {
        if (googleMap == null) return;
        Dispatch.triggerEvent(ObservedEvents.NOTIFY_MAP_CAMERA_LOCKED);
    }

    public void unlockCamera() {
        if (googleMap == null || !CAMERA_LOCK) return;
        CAMERA_LOCK = false;
        //Log.d("DEVDEBUG", "unlockCamera");
        if (heatmapProvider != null) {
            heatmapProvider.setOpacity(0.6f);
        }
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setScrollGesturesEnabled(true);
        googleMap.stopAnimation();
//        setSnapZoom();
        CameraPosition currentPlace = new CameraPosition.Builder()
                .target(lastCameraPosition.target)
                .bearing(AppConstants.COMPASS_ANGLE)
                .tilt(90 - AppConstants.TILT_ANGLE)
                .zoom(currentZoom)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace), 20, unlockCameraCallback);
    }

    private void finalizeCameraUnlock() {
        if (googleMap == null) return;
        Dispatch.triggerEvent(ObservedEvents.NOTIFY_MAP_CAMERA_UNLOCKED);
    }

    private void findTargetEntity(boolean isLocking) {
        if (!MAP_READY || currentTarget == null) return;
        String activeLocation = null;
        VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();
        LatLngBounds viewBounds = visibleRegion.latLngBounds;
        LatLng updatedTarget = currentTarget;
        AppConstants.MAP_ACTIVE_LOCATIONS.clear();
        AppConstants.MAP_ACTIVE_LOCATIONS.addAll(AppConstants.ACTIVE_LOCATIONS);
        final HashMap<String, MapSubdivisionData> models = MapData.getMapData();
        if (models != null) {
            for (Object o : models.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                MapSubdivisionData mapModel = (MapSubdivisionData)entry.getValue();
                String id = mapModel.id;
                LatLng entityLocation = mapModel.getLocation();
                //Log.d(TAG, entityLocation.toString());
                if (viewBounds.contains(entityLocation)) {
                    AppConstants.MAP_ACTIVE_SUBDIVISION_ID = id;
                    String locationId = mapModel.getContainingId(currentTarget);
                    AppConstants.MAP_ACTIVE_LOCATIONS.addAll(mapModel.getLocationIds());
                    if (locationId != null) {
                        updatedTarget = entityLocation;
                        activeLocation = locationId;
                    }
                }
            }
        }

        if (activeLocation != null) {
            currentTarget = updatedTarget;
            AppConstants.MAP_ACTIVE_LOCATION_ID = activeLocation;
            if (isLocking) {
                if (activeLocation.equals(AppConstants.LOCATION_ID)) {
                    Dispatch.triggerEvent(ObservedEvents.REQUEST_OPEN_CHAT);
                } else {
                    Dispatch.triggerEvent(ObservedEvents.REQUEST_OPEN_PREVIEW);
                }
            }
//            else {
//                snapZoom();
//            }
        }
//        else {
//            snapZoom();
//        }
    }

    public boolean mapReady() {
        return (googleMap != null && MAP_READY);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap m) {
        if (m != null) {
            googleMap = m;
            try {
                boolean success;
                if (AppConstants.THEME_ID == 1) {
                    success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark));
                } else {
                    success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style));
                }

                if (!success) {
                    Log.e(TAG, "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Can't find style.", e);
            }

            myEntity = new UserEntity(AppConstants.GPS_LATITUDE, AppConstants.GPS_LONGITUDE, UserData.ID, "Me");
            Bitmap iconBitmap = mIconGenerator.makeIcon();
            BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(iconBitmap);
            myEntity.setMarkerIcon(markerIcon);
            MarkerOptions markerOpts = new MarkerOptions()
                    .anchor(0.5F,0)
                    .icon(markerIcon)
                    .position(myEntity.getLocation())
                    .zIndex(4);
            myEntity.setMarker(googleMap.addMarker(markerOpts));

            currentTarget = myEntity.getLocation();


            for (int i = 0; i < 5; i++) {
                popupCache.add(new PopupEntity(googleMap, currentTarget));
            }

            UiSettings mUiSettings = googleMap.getUiSettings();
            mUiSettings.setMapToolbarEnabled(false);
            mUiSettings.setZoomControlsEnabled(false);
            mUiSettings.setCompassEnabled(false);
            mUiSettings.setMyLocationButtonEnabled(false);
            mUiSettings.setScrollGesturesEnabled(true);
            mUiSettings.setZoomGesturesEnabled(true);
            mUiSettings.setTiltGesturesEnabled(false);
            mUiSettings.setRotateGesturesEnabled(false);

            googleMap.setMyLocationEnabled(false);
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(myEntity.getLocation())
                            .bearing(0)
                            .tilt(0)
                            .zoom(AppConstants.MAP_DEFAULT_ZOOM_LEVEL)
                            .build()));

            eventClusterManager = new EventClusterManager();
            eventClusterManager.init(context, googleMap, this);


            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    eventClusterManager.onMarkerClick(marker);
                    Dispatch.triggerEvent(ObservedEvents.REQUEST_OPEN_CHAT);
                    return false;
                }
            });

            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    eventClusterManager.onInfoWindowClick(marker);
                }
            });

            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (!CAMERA_LOCK) {
                        currentTarget = latLng;
                        CameraPosition cam = googleMap.getCameraPosition();
                        currentZoom = cam.zoom;
                        findTargetEntity(true);
                    }
                }
            });

            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    currentTarget = latLng;
                    if (!CAMERA_LOCK) {
                        CameraPosition cam = googleMap.getCameraPosition();
                        currentZoom = cam.zoom;
                        findTargetEntity(true);
                    }
                }
            });

            googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                @Override
                public void onCameraMoveStarted(int i) {
                    if (i == REASON_GESTURE && !PENDING_GESTURE && !CAMERA_LOCK) {
                        PENDING_GESTURE = true;
                        PENDING_LOCK = true;
                    }
                }
            });

            googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    CameraPosition cam = googleMap.getCameraPosition();
                    AppConstants.MAP_FOLLOW_MODE = (cam.zoom >= AppConstants.MAP_DEFAULT_ZOOM_LEVEL);
                    //Log.d(TAG, "ZOOM: " + cam.zoom);
                    POPUPS_UNLOCKED = (cam.zoom > AppConstants.MAP_MAX_POPUP_LEVEL && cam.zoom < AppConstants.MAP_MIN_POPUP_LEVEL);
                    if (!CAMERA_LOCK) {
                        if (PENDING_GESTURE) {
                            googleMap.stopAnimation();
                            currentTarget = cam.target;
                            double zoomCalc = (cam.zoom < AppConstants.MAP_HIGH_ZOOM_LEVEL) ? (((AppConstants.MAP_HIGH_ZOOM_LEVEL - cam.zoom) + 1) * 0.085) : 1;
                            zoomScale = (zoomCalc < 1) ? (1 - zoomCalc) : 1;
                            currentZoom = cam.zoom;
                            PENDING_GESTURE = false;
                            if (cam.zoom >= AppConstants.MAP_PREVIEW_ZOOM_LEVEL) {
                                findTargetEntity(true);
                            }
                            addHeatMap();
                        }
                    }
                    eventClusterManager.onCameraIdle();
                }
            });

            AppConstants.MAP_ACTIVE_LOCATIONS.clear();
            AppConstants.MAP_ACTIVE_LOCATIONS.addAll(AppConstants.ACTIVE_LOCATIONS);
            procEntities();
            addHeatMap();
            refreshPopups();
            MAP_READY = true;
            CAMERA_READY = true;
            Dispatch.triggerEvent(ObservedEvents.MAP_READY);
        }
    }

    public void rebuild() {
        if (googleMap != null && getActivity() != null) {
            MAP_READY = false;
            CAMERA_READY = false;
            handler.post(new Runnable() {
                public void run() {
                    googleMap.stopAnimation();
                    fastUpdateCamera();
                    eventClusterManager.clear();
                    rebuildHeatMap();
                    procEntities();
                    MAP_READY = true;
                    CAMERA_READY = true;
                }
            });
        }
    }

    public void refresh() {
        if (googleMap != null && getActivity() != null) {
            MAP_READY = false;
            CAMERA_READY = false;
            if (AppConstants.MAP_ACTIVE_SUBDIVISION_ID == null) {
                AppConstants.MAP_ACTIVE_SUBDIVISION_ID = AppConstants.SUBDIVISION_ID;
            }
            handler.post(new Runnable() {
                public void run() {
                    googleMap.stopAnimation();
                    fastUpdateCamera();
                    eventClusterManager.clear();
                    addHeatMap();
                    procEntities();
                    MAP_READY = true;
                    CAMERA_READY = true;
                }
            });
        }
    }

    private void procEntities() {
        if (googleMap != null) {
            final HashMap<String, MapSubdivisionData> models = MapData.getMapData();
            if (models == null) return;
            for (Object o : models.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                MapSubdivisionData mapModel = (MapSubdivisionData)entry.getValue();
                if (!mapModel.isRegistered) {
//                    LatLng[] quadrants = mapModel.getQuadrants();
//                    int color = AppConstants.getRandomColor();
//                    googleMap.addPolygon(new PolygonOptions()
//                            .add(quadrants[0], quadrants[1], quadrants[3], quadrants[2])
//                            .strokeColor(color)
//                            .fillColor(color));
                    mapModel.setRegistered(true);

                    ArrayList<EventEntity> streams = mapModel.getStreamEntities();
                    for (int x = 0; x < streams.size(); x++) {
                        EventEntity stream = streams.get(x);
                        eventClusterManager.addItem(stream);
                    }
                }
            }

            final HashMap<String, FriendEntity> friends = FriendData.getAll();
            if (friends == null) return;
            for (Object o : friends.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                FriendEntity friendEntity = (FriendEntity)entry.getValue();
                String id = friendEntity.getId();
                UserEntity entity;
                if (null == friendsCache.get(id)) {
                    entity = new UserEntity(friendEntity);
                    Bitmap iconBitmap = mFriendIconGenerator.makeIcon(friendEntity.userName, friendEntity.avatarColor, friendEntity.getAuraColor());
                    BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(iconBitmap);
                    entity.setMarkerIcon(markerIcon);
                    MarkerOptions markerOpts = new MarkerOptions()
                            .anchor(0.5F,0)
                            .icon(markerIcon)
                            .position(entity.getLocation())
                            .zIndex(4);
                    entity.setMarker(googleMap.addMarker(markerOpts));
                    friendsCache.put(id, entity);
                } else {
                    entity = friendsCache.get(id);
                }

                entity.setVisible(false);
                if (UserData.FRIENDMAP_ENABLED) {
                    entity.friendUpdate();
                }
            }

            myEntity.update();
            eventClusterManager.cluster();
        }
    }

    public void refreshUserIcon() {
        if (googleMap != null && myEntity != null) {
            myEntity.update();
        }
    }

    public void refreshPopups() {
        handler.post(new PopupTask());
    }

    public void addHeatMap() {
        if (UserData.HEATMAP_ENABLED) {
            final ArrayList<WeightedLatLng> data = readWeightedItems();
            if (data != null && data.size() > 0) {
                handler.post(new Runnable() {
                    public void run() {
                        //Log.d("DEVDEBUG", "SCALE: " + zoomScale);
                        int newRadius = (int)(80 * zoomScale);
                        float newOpacity = (float)(0.5f * zoomScale);
                        if (heatMap == null) {
                            int[] colors = {
                                    Color.rgb(0, 180, 255),
                                    Color.rgb(160, 0, 255)
                            };
                            float[] startPoints = {
                                    0.2f,
                                    1f
                            };

                            Gradient gradient = new Gradient(colors, startPoints);

                            heatmapProvider = new HeatmapTileProvider.Builder()
                                    .weightedData(data)
                                    .gradient(gradient)
                                    .opacity(newOpacity)
                                    .build();

                            heatMap = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapProvider));
                            heatmapProvider.setRadius(newRadius);
                            heatMap.clearTileCache();
                        } else {
                            heatmapProvider.setWeightedData(data);
                            heatmapProvider.setRadius(newRadius);
                            heatMap.clearTileCache();
                        }
                    }
                });
            }
        } else if (heatMap != null) {
            heatMap.remove();
        }
    }

    public void rebuildHeatMap() {
        if (UserData.HEATMAP_ENABLED) {
            final ArrayList<WeightedLatLng> data = readWeightedItems();
            if (data != null && data.size() > 0) {
                handler.post(new Runnable() {
                    public void run() {
                        //Log.d("DEVDEBUG", "SCALE: " + zoomScale);
                        int newRadius = (int)(80 * zoomScale);
                        float newOpacity = (float)(0.5f * zoomScale);
                        if (heatMap != null) {
                            heatMap.remove();
                        }
                        int[] colors = {
                                Color.rgb(0, 180, 255),
                                Color.rgb(160, 0, 255)
                        };
                        float[] startPoints = {
                                0.2f,
                                1f
                        };

                        Gradient gradient = new Gradient(colors, startPoints);

                        heatmapProvider = new HeatmapTileProvider.Builder()
                                .weightedData(data)
                                .gradient(gradient)
                                .opacity(newOpacity)
                                .build();

                        heatMap = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapProvider));
                        heatmapProvider.setRadius(newRadius);
                        heatMap.clearTileCache();
                    }
                });
            }
        } else if (heatMap != null) {
            heatMap.remove();
        }
    }

    private ArrayList<WeightedLatLng> readWeightedItems() {
        final HeatmapData hmd = MapData.getHeatMapData(HEATMAP_UUID);
        if (hmd != null) {
            HEATMAP_UUID = hmd.uuid;
            ArrayList<WeightedLatLng> list = new ArrayList<>();
            Set set = hmd.data.entrySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                HeatmapLocationData mapModel = (HeatmapLocationData)entry.getValue();
                list.add(mapModel.getWeightedLatLng());
            }
            return list;
        }
        return null;
    }

    private class HeatmapTask implements Runnable {

        @Override
        public void run() {
            if (googleMap == null || !UserData.HEATMAP_ENABLED || heatMap == null || heatmapProvider == null) return;
            ArrayList<WeightedLatLng> data = readWeightedItems();
            if (data != null && data.size() > 0) {
                heatmapProvider.setWeightedData(data);
                heatmapProvider.setRadius((int)(80 * zoomScale));
                heatMap.clearTileCache();
            }
        }
    }

    private MapChatData getMapChatData() {
        final MapChatData popupData = MapData.getPopupData(POPUP_UUID);
        if (popupData != null) {
            POPUP_UUID = popupData.uuid;
            return popupData;
        }
        return null;
    }

    private class PopupTask implements Runnable {

        @Override
        public void run() {
            if (googleMap == null || AppConstants.MAP_ACTIVE_SUBDIVISION_ID == null || !POPUPS_UNLOCKED) return;
            MapChatData popupData = MapData.getPopupData();
            if (popupData != null) {
                MapSubdivisionData model = MapData.getMapData(AppConstants.MAP_ACTIVE_SUBDIVISION_ID);
                int popIndex = 0;
                int imageSize = (int) context.getResources().getDimension(R.dimen.map_marker_image);
                int count = AppConstants.MAP_ACTIVE_LOCATIONS.size();
                Log.d("DEVDEBUG", "MAP_ACTIVE_LOCATIONS: " + count + ", MAP_ACTIVE_SUBDIVISION_ID: " + AppConstants.MAP_ACTIVE_SUBDIVISION_ID);
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        String locationId = AppConstants.MAP_ACTIVE_LOCATIONS.get(i);
                        PopupEntity popup = popupCache.get(popIndex);
                        LayoutInflater inflater = LayoutInflater.from(context);
                        if (popupData.stickers.get(locationId) != null) {
                            ChatPostData[] stickers = popupData.stickers.get(locationId);
                            if (stickers != null && stickers.length > 0 && model != null) {
                                popup.assign(model.getLocationQuadrants(locationId));
                                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.map_sticker_layout, null, false);
                                for (ChatPostData data : stickers) {
                                    if (data != null) {
                                        if (data.image.toLowerCase().startsWith("http")) {
                                            try {
                                                URL url = new URL(data.image);
                                                InputStream is = new BufferedInputStream(url.openStream());
                                                Bitmap mediaImage = BitmapFactory.decodeStream(is);
                                                if (mediaImage != null) {
                                                    if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                                        mediaImage = makeTransparent(mediaImage);
                                                    }
                                                    Bitmap bm = mediaImage;
                                                    ImageView image = (ImageView) layout.findViewById(R.id.image);
                                                    image.setMaxWidth(imageSize);
                                                    image.setMaxHeight(imageSize);
                                                    image.setImageBitmap(bm);
                                                    //txt2.setText(data.locationId);
                                                    setStickerFromLayout(layout, popup, data.postId);
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }

                            popIndex++;
                            if (popIndex > 4) {
                                break;
                            }
                        }
                        if (popupData.posts.get(locationId) != null) {
                            ChatPostData data = popupData.posts.get(locationId);
                            if (data != null) {
                                popup.assign(new LatLng(data.latitude, data.longitude));
                                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.map_item_layout, null, false);
                                if (data.image.toLowerCase().startsWith("http")) {
                                    try {
                                        URL url = new URL(data.image);
                                        InputStream is = new BufferedInputStream(url.openStream());
                                        Bitmap mediaImage = BitmapFactory.decodeStream(is);
                                        if (mediaImage != null) {
                                            TextView txt1 = (TextView) layout.findViewById(R.id.title);
                                            ImageView image = (ImageView) layout.findViewById(R.id.image);
                                            txt1.setText("");
                                            txt1.setVisibility(View.GONE);
                                            image.setVisibility(View.VISIBLE);
                                            image.setMaxWidth(imageSize*2);
                                            image.setMaxHeight(imageSize);
                                            image.setImageBitmap(mediaImage);
                                            //txt2.setText(data.locationId);
                                            setImageFromLayout(layout, popup, data.postId);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    TextView txt1 = (TextView) layout.findViewById(R.id.title);
                                    ImageView image = (ImageView) layout.findViewById(R.id.image);
                                    image.setImageResource(0);
                                    image.setVisibility(View.GONE);
                                    txt1.setVisibility(View.VISIBLE);
                                    txt1.setText(data.message);
                                    //txt2.setText(data.locationId);
                                    setImageFromLayout(layout, popup, data.postId);
                                }
                            }

                            popIndex++;
                            if (popIndex > 4) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void setStickerFromLayout(LinearLayout layout, PopupEntity entity, String postId) {
        //Log.d("DEVDEBUG", "setStickerFromLayout");
        layout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        layout.layout(0, 0, layout.getMeasuredWidth(), layout.getMeasuredHeight());

        layout.setDrawingCacheEnabled(true);
        layout.buildDrawingCache();
        Bitmap bitmap = layout.getDrawingCache();
        if (bitmap != null) {
            Bitmap newBitmap = bitmap.copy(bitmap.getConfig(), true);
            entity.showSticker(postId, newBitmap);
        }
        layout.setDrawingCacheEnabled(false);
    }

    private void setImageFromLayout(LinearLayout layout, PopupEntity entity, String postId) {
        //Log.d("DEVDEBUG", "setImageFromLayout");
        layout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        layout.layout(0, 0, layout.getMeasuredWidth(), layout.getMeasuredHeight());

        layout.setDrawingCacheEnabled(true);
        layout.buildDrawingCache();
        Bitmap bitmap = layout.getDrawingCache();
        if (bitmap != null) {
            Bitmap newBitmap = bitmap.copy(bitmap.getConfig(), true);
            entity.showMessage(postId, newBitmap);
        }
        layout.setDrawingCacheEnabled(false);
    }

    private static Bitmap makeTransparent(Bitmap bitmap) {
        int transparentColor = bitmap.getPixel(0,0);
        int width =  bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap myBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int [] allpixels = new int [ myBitmap.getHeight()*myBitmap.getWidth()];
        bitmap.getPixels(allpixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(),myBitmap.getHeight());
        myBitmap.setPixels(allpixels, 0, width, 0, 0, width, height);

        for(int i =0; i<myBitmap.getHeight()*myBitmap.getWidth();i++){
            if( allpixels[i] == transparentColor)

                allpixels[i] = Color.alpha(Color.TRANSPARENT);
        }

        myBitmap.setPixels(allpixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());
        return myBitmap;
    }

}
