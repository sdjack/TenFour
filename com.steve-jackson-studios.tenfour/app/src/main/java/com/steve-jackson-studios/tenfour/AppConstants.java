package com.steve-jackson-studios.tenfour;

import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.steve-jackson-studios.tenfour.Data.ChatPostData;
import com.steve-jackson-studios.tenfour.Data.UserData;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.spatial4j.distance.DistanceCalculator;
import org.locationtech.spatial4j.distance.GeodesicSphereDistCalc;
import org.locationtech.spatial4j.shape.Point;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Stack;
import java.util.TreeMap;

/**
 * Created by sjackson on 4/19/2017.
 * AppConstants
 */

public class AppConstants {
    public static final boolean DEBUGGING = true;
    /**
     * SERVICE_URLS.
     */
    private static final String SERVICE_PORT = ":3333";
    private static final String SERVICE_DEFAULT = "http://api0001.tenfour.services";

    private static final String ENDPOINT_MAPS = "/maps";
    private static final String ENDPOINT_CHAT = "/chat";
    private static final String ENDPOINT_PROFILE = "/users";

    public static final String SERVICE_URL = SERVICE_DEFAULT + SERVICE_PORT;

    public static final String SERVICE_LOGIN = "/auth/login";
    public static final String SERVICE_LOGOUT = "/auth/login/logout";
    public static final String SERVICE_TOKEN_LOGIN = "/auth/login/token";
    public static final String SERVICE_SIGNUP = "/auth/signup";
    public static final String SERVICE_TOKEN_SIGNUP = "/auth/signup/token";

    public static final String SERVICE_CHATDATA = ENDPOINT_CHAT + "/info";
    public static final String SERVICE_CHATUPDATE = ENDPOINT_CHAT + "/update";
    public static final String SERVICE_CHATPREVIEW = ENDPOINT_CHAT + "/info/preview";

    public static final String SERVICE_DATA_BUNDLE = ENDPOINT_MAPS + "/info/bundle";

    public static final String SERVICE_MAPDATA = ENDPOINT_MAPS + "/info";
    public static final String SERVICE_MAPUPDATE = ENDPOINT_MAPS + "/update";
    public static final String SERVICE_MAPSUBDIVISION = ENDPOINT_MAPS + "/info/subdivision";
    public static final String SERVICE_HEATMAP = ENDPOINT_MAPS + "/info/heatmap";
    public static final String SERVICE_MAPLOCATION = ENDPOINT_MAPS + "/info/location";
    public static final String SERVICE_EVENTDATA = ENDPOINT_MAPS + "/info/events";
    public static final String SERVICE_CREATE_EVENT = ENDPOINT_MAPS + "/update/newevent";

    public static final String SERVICE_PROFILEDATA = ENDPOINT_PROFILE + "/info";
    public static final String SERVICE_PROFILEUPDATE = ENDPOINT_PROFILE + "/update";
    public static final String SERVICE_LOCATIONUPDATE = ENDPOINT_PROFILE + "/update/location";
    public static final String SERVICE_AVATARUPDATE = ENDPOINT_PROFILE + "/update/avatar";
    public static final String SERVICE_STATUSUPDATE = ENDPOINT_PROFILE + "/update/status";
    public static final String SERVICE_HOMETOWNUPDATE = ENDPOINT_PROFILE + "/update/hometown";
    public static final String SERVICE_SENDFRIENDREQUEST = ENDPOINT_PROFILE + "/update/sendfriendrequest";
    public static final String SERVICE_UPDATEFRIENDREQUEST = ENDPOINT_PROFILE + "/update/updatefriendrequest";
    public static final String SERVICE_GETFRIENDS = ENDPOINT_PROFILE + "/info/friends";
    public static final String SERVICE_GETFRIENDREQUESTS = ENDPOINT_PROFILE + "/info/friendrequests";

    public static final int EVENT_SERVICE_LOGIN = 1;
    public static final int EVENT_SERVICE_LOGOUT = 2;
    public static final int EVENT_SERVICE_SIGNUP = 3;

    public static final int EVENT_SERVICE_MAPDATA = 5;
    public static final int EVENT_SERVICE_CHATDATA = 6;
    public static final int EVENT_SERVICE_EVENTDATA = 7;
    public static final int EVENT_SERVICE_PROFILE = 8;
    public static final int EVENT_SERVICE_AVATARUPDATE = 9;
    public static final int EVENT_SERVICE_MAPLOCATION = 10;
    public static final int EVENT_SERVICE_HEATMAP = 11;
    public static final int EVENT_SERVICE_CHATPREVIEW = 12;
    public static final int EVENT_SERVICE_CREATE_EVENT = 13;
    public static final int EVENT_SERVICE_STATUSUPDATE = 15;
    public static final int EVENT_SERVICE_HOMETOWNUPDATE = 16;
    public static final int EVENT_SERVICE_GETFRIENDS = 19;
    public static final int EVENT_SERVICE_GETFRIENDREQUESTS = 20;
    public static final int EVENT_SERVICE_SENDFRIENDREQUEST = 21;
    public static final int EVENT_SERVICE_UPDATEFRIENDREQUEST = 22;
    public static final int EVENT_SERVICE_DATA_BUNDLE = 98;
    public static final int EVENT_SERVICE_LOCATIONUPDATE = 99;

    public static final HashMap<String, Integer> SERVICE_MAP = new HashMap<String, Integer>();

    static {
        SERVICE_MAP.put(SERVICE_LOGIN, EVENT_SERVICE_LOGIN);
        SERVICE_MAP.put(SERVICE_LOGOUT, EVENT_SERVICE_LOGOUT);
        SERVICE_MAP.put(SERVICE_TOKEN_LOGIN, EVENT_SERVICE_LOGIN);
        SERVICE_MAP.put(SERVICE_SIGNUP, EVENT_SERVICE_SIGNUP);
        SERVICE_MAP.put(SERVICE_TOKEN_SIGNUP, EVENT_SERVICE_SIGNUP);
        SERVICE_MAP.put(SERVICE_LOCATIONUPDATE, EVENT_SERVICE_LOCATIONUPDATE);
        SERVICE_MAP.put(SERVICE_MAPDATA, EVENT_SERVICE_MAPDATA);
        SERVICE_MAP.put(SERVICE_CHATDATA, EVENT_SERVICE_CHATDATA);
        SERVICE_MAP.put(SERVICE_EVENTDATA, EVENT_SERVICE_EVENTDATA);
        SERVICE_MAP.put(SERVICE_PROFILEDATA, EVENT_SERVICE_PROFILE);
        SERVICE_MAP.put(SERVICE_AVATARUPDATE, EVENT_SERVICE_AVATARUPDATE);
        SERVICE_MAP.put(SERVICE_MAPLOCATION, EVENT_SERVICE_MAPLOCATION);
        SERVICE_MAP.put(SERVICE_HEATMAP, EVENT_SERVICE_HEATMAP);
        SERVICE_MAP.put(SERVICE_CHATPREVIEW, EVENT_SERVICE_CHATPREVIEW);
        SERVICE_MAP.put(SERVICE_CREATE_EVENT, EVENT_SERVICE_CREATE_EVENT);
        SERVICE_MAP.put(SERVICE_MAPSUBDIVISION, EVENT_SERVICE_MAPDATA);
        SERVICE_MAP.put(SERVICE_STATUSUPDATE, EVENT_SERVICE_STATUSUPDATE);
        SERVICE_MAP.put(SERVICE_HOMETOWNUPDATE, EVENT_SERVICE_HOMETOWNUPDATE);
        SERVICE_MAP.put(SERVICE_GETFRIENDS, EVENT_SERVICE_GETFRIENDS);
        SERVICE_MAP.put(SERVICE_GETFRIENDREQUESTS, EVENT_SERVICE_GETFRIENDREQUESTS);
        SERVICE_MAP.put(SERVICE_SENDFRIENDREQUEST, EVENT_SERVICE_SENDFRIENDREQUEST);
        SERVICE_MAP.put(SERVICE_UPDATEFRIENDREQUEST, EVENT_SERVICE_UPDATEFRIENDREQUEST);
        SERVICE_MAP.put(SERVICE_DATA_BUNDLE, EVENT_SERVICE_DATA_BUNDLE);
    }

    private static final String BLOB_DEFAULT = "https://mbcutestfaaa001.blob.core.windows.net/";
    private static final String BLOB_AVATARS = "defaultavatars/";
    private static final String BLOB_USERAVATARS = "useravatars/";

    public static final String BLOB_AVATARS_URL = BLOB_DEFAULT + BLOB_AVATARS;
    public static final String BLOB_USERAVATARS_URL = BLOB_DEFAULT + BLOB_USERAVATARS;

    public static final String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    public static final String LOCATION_KEY = "location-key";
    public static final String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    public static final String PHOTO_PATH_STRING_KEY = "photo-path-string-key";
    public static final String TWITTER_KEY = "39224f3ccc43099c1018f8d0aa31a5b2616f8b8e";
    public static final String TWITTER_SECRET = "";

    public static final int UPDATE_INTERVAL = 5000;
    public static final int FATEST_INTERVAL = 1000;
    public static final int LOCATION_DISPLACEMENT = 10;
    public static final int LOCATION_THRESHOLD = 25;
    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    public static final int REQUEST_CHECK_SETTINGS = 1111;
    public static final int REQUEST_RESOLVE_ERROR = 666;
    public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    public static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
    public static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1002;
    public static final int REQUEST_IMAGE_CAPTURE = 8800;
    public static final int REQUEST_ENABLE_BT = 8801;
    public static final int REQUEST_PLAY_SERVICES_RESOLUTION = 9000;
    public static final int REQUEST_PLACE_PICKER = 9001;
    public static final int REQUEST_GOOGLE_LOGIN = 9911;
    public static final int REQUEST_GOOGLE_CREATE = 9912;
    public static final int REQUEST_FACEBOOK_LOGIN = 9913;
    public static final int REQUEST_FACEBOOK_CREATE = 9914;
    public static final int REQUEST_TWITTER_LOGIN = 9915;
    public static final int REQUEST_TWITTER_CREATE = 9916;
    /**
     *
     */
    public static int THEME_ID = 0;
    public enum THEME_SETTINGS {

        DEFAULT(0, R.raw.map_style, R.drawable.splash_bg, R.drawable.splash_fg),
        DARK(1, R.raw.map_style_dark, R.drawable.splash_bg, R.drawable.splash_fg);

        public final int id;
        public final int map;
        public final int bgImage;
        public final int fgImage;

        THEME_SETTINGS(int themeId, int mapStyle, int splashBg, int splashFg) {
            id = themeId;
            map = mapStyle;
            bgImage = splashBg;
            fgImage = splashFg;
        }
    }
    /**
     *
     */
    public static final Integer[] CATEGORY_ICONS = new Integer[]{
            R.drawable.category,
            R.drawable.category,
            R.drawable.category,
            R.drawable.category,
            R.drawable.category,
            R.drawable.category,
            R.drawable.category,
            R.drawable.category,
            R.drawable.category,
            R.drawable.category,
            R.drawable.category,
            R.drawable.category,
            R.drawable.category,
            R.drawable.category,
            R.drawable.category
    };

    public static final Integer[] CATEGORY_COLORS = new Integer[]{
            0xFFFFFFFF,
            0xFFDF4F08,
            0xFFFF3F2F,
            0XFF5ADAE6,
            0XFF2DDDFF,
            0XFF2FEDAD,
            0XFF73B1CC,
            0XFF9151A8,
            0XFFFF861C,
            0XFFFF3636,
            0XFF35D47C,
            0XFFFFDD30,
            0XFFD12D38,
            0XFF9C85FF,
            0XFFA1FFB9
    };

    public static final Integer[] AVATAR_COLORS = new Integer[]{
            0xFFFF3626,
            0XFFFF861C,
            0XFFFFDD30,
            0XFF2FEDAD,
            0XFF2DDDFF,
            0XFF9335E0,
            0XFFFF30FB,
            0XFF9F9F9F,
            0XFF3F3F3F
    };

    public static final Integer[] MAPDATA_ACTIVE_COLORS = new Integer[]{
            0xEFAAE0FF,
            0xEFFFAF00,
            0xEFFF2F00
    };

    public static final Integer[] MAPDATA_INACTIVE_COLORS = new Integer[]{
            0x8FAAE0FF,
            0x8FFFAF00,
            0x8FFF2F00
    };

    public static final Integer[] AURA_COLORS = new Integer[]{
            0x8F2FFF00,
            0x8F2FFFFF,
            0x8FFF2F00
    };

    private static final String TAG = "AppConstants";
    private static int SENSOR_BUFFER = 0;
    private static final int SENSOR_BUFFER_LIMIT = 7;

    public static final int MAP_MAX_POPUP_LEVEL = 10;
    public static final int MAP_LOW_ZOOM_LEVEL = 16;
    public static final int MAP_DEFAULT_ZOOM_LEVEL = 17;
    public static final int MAP_HIGH_ZOOM_LEVEL = 19;
    public static final int MAP_PREVIEW_ZOOM_LEVEL = 20;
    public static final int MAP_MIN_POPUP_LEVEL = 20;

    public static boolean PLATFORM_HAS_PERMISSIONS = false;
    public static boolean RECENTLY_POSTED = false;
    public static boolean CHAT_IS_OPEN = false;
    public static boolean LOGGED_IN = false;
    public static boolean LOGIN_PENDING = false;
    public static boolean NETWORK_ONLINE = true;
    public static boolean SERVICES_ONLINE = true;
    public static boolean INITIALIZATION_COMPLETE = false;
    public static boolean LOCALIZE_MY_POSTS = false;
    public static boolean INIT_LOCATION_FOUND = false;
    public static boolean POPUPS_ENABLED = true;
    public static boolean MAP_CHANGES_LOCKED = false;
    public static boolean CHAT_ENABLED = true;

    public static boolean MAP_FOLLOW_MODE = true;
    public static boolean MAP_RESET_ALLOWED = false;

    public static float GPS_LATITUDE = 0.0f;
    public static float GPS_LONGITUDE = 0.0f;
    public static float GPS_ALTITUDE = 0.0f;
    public static float COMPASS_ANGLE = 0.0f;
    public static float TILT_ANGLE = 0.0f;
    public static String GPS_COUNTRY = "";
    public static String GPS_ADMIN = "";

    public static String MAP_ACTIVE_SUBDIVISION_ID = "";
    public static String MAP_ACTIVE_LOCATION_ID = "";
    public static String MAP_ACTIVE_EVENT_ID = "";
    public static ArrayList<String> MAP_ACTIVE_LOCATIONS = new ArrayList<>();

    public static Point POINT_OF_ORIGIN;

    public static int REPLY_LEVEL = 0;
    public static int AUTH_STATUS_CODE = 0;
    public static int LAST_CHAT_ID = 0;
    public static String STATUS_MESSAGE = "";
    public static ArrayList<String> ACTIVE_LOCATIONS = new ArrayList<>();

    public static String SOCKET_ID = "";
    public static String CHANNEL_ID = "";
    public static String SUBDIVISION_ID = null;
    public static String LOCATION_ID = null;
    public static String EVENT_ID = null;
    public static String DEFAULT_CHAT_TITLE = "General Discussion";
    public static String CHAT_TITLE = DEFAULT_CHAT_TITLE;
    public static String REPLY_ID = null;
    public static String BLOB_CONTAINER = "images";

    public static String ACTIVE_MENU_TITLE = "";
    public static String ACTIVE_MENU_LINK = "";
    public static String ACTIVE_MENU_CONTENT = "";
    public static int ACTIVE_MENU_LAYOUT = 0;

    public static String DEFAULT_AVATAR = "https://mbcutestfaaa001.blob.core.windows.net/defaultavatars/unisex_type1.png";

    public static DistanceCalculator geoCalculator = new GeodesicSphereDistCalc.Haversine();

    private static Stack<ChatPostData> REPLY_CACHE = new Stack<>();

    public static JSONArray FRIEND_REQUESTS;


    public static String PENDING_POST_TEXT = "";
    public static String PENDING_POST_FILENAME = "";
    public static Uri PENDING_POST_IMG_URI;
    public static Bitmap PENDING_POST_BITMAP;
    public static int PENDING_POST_STICKER = 0;
    public static String PENDING_LOCATION;
    public static String PENDING_SUBDIVISION_ID;
    public static boolean PENDING_IMAGE_FROM_GALLERY;

    public static ChatPostData REPLY_ENTITY = null;

    public static int ATTEMPTED_LOGIN_TYPE = 0;

    public static int SCREEN_HEIGHT = 0;
    public static int SCREEN_BOTTOM = 0;
    public static int SCREEN_WIDTH = 0;

    public static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);
    public static Date LAST_LOCATION_UPDATE = new Date(0);

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final int AUTHACTIVITY_RESULT = 8882;
    public static final int APPACTIVITY_RESULT = 8883;
    public static final int MEDIAACTIVITY_RESULT = 8884;
    public static final int SELECTACTIVITY_RESULT = 8885;

    private static final String PACKAGE_NAME =
            "com.steve-jackson-studios.tenfour";

    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_GEODATA_COUNTRY = PACKAGE_NAME + ".RESULT_GEODATA_COUNTRY";
    public static final String RESULT_GEODATA_ADMIN = PACKAGE_NAME + ".RESULT_GEODATA_ADMIN";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    public static final TreeMap<String, TreeMap<String, JSONObject>> GEOFENCE_NAMES = new TreeMap();


    private AppConstants() {
        PENDING_POST_BITMAP = null;
    }

    public static void resetPendingBitmap() {
        if (PENDING_POST_BITMAP != null) {
            PENDING_POST_BITMAP.recycle();
        }
        PENDING_POST_BITMAP = null;
    }

    public static void resetPendingInputValues() {
        resetPendingBitmap();
        PENDING_POST_FILENAME = null;
        PENDING_POST_IMG_URI = null;
        PENDING_POST_TEXT = "";
        PENDING_POST_STICKER = 0;
    }

    public static void resetPendingNodeValues() {
        PENDING_LOCATION = null;
    }

    public static boolean replyStepOut() {
        boolean canClose = false;
        if (!REPLY_CACHE.empty()) {
            REPLY_LEVEL = REPLY_CACHE.size();
            ChatPostData replyEntity = REPLY_CACHE.pop();
            setReplyMessage(replyEntity);
        } else {
            if (REPLY_LEVEL == 0) {
                canClose = true;
            }
            REPLY_LEVEL = 0;
            unsetReplyMessage();
        }
        return canClose;
    }

    public static void replyStepIn(ChatPostData replyEntity) {
        if (REPLY_LEVEL == 0 || REPLY_ID == null || !REPLY_ID.equals(replyEntity.postId)) {
            if (REPLY_LEVEL > 0) {
                REPLY_CACHE.push(replyEntity);
                REPLY_LEVEL = REPLY_CACHE.size() + 1;
            } else {
                REPLY_LEVEL = 1;
            }
            setReplyMessage(replyEntity);
        }
    }

    private static void setReplyMessage(ChatPostData replyEntity) {
        REPLY_ID = replyEntity.postId;
        REPLY_ENTITY = replyEntity;
    }

    public static void unsetReplyMessage() {
        REPLY_ID = null;
        REPLY_ENTITY = null;
    }

    public static void setSensorData(float direction, float tilt) {
        COMPASS_ANGLE = direction;
        TILT_ANGLE = tilt;
        if (SENSOR_BUFFER == 0) {
            SENSOR_BUFFER++;
            Dispatch.triggerEvent(ObservedEvents.GLOBAL_SENSOR_UPDATE);
        } else {
            if (SENSOR_BUFFER < SENSOR_BUFFER_LIMIT) {
                SENSOR_BUFFER++;
            } else {
                SENSOR_BUFFER = 0;
            }
        }
    }

    public static void resetReplyState() {
        REPLY_ID = null;
        REPLY_LEVEL = 0;
        REPLY_CACHE.clear();
    }

    public static boolean readyForUse() {
        return (
                PLATFORM_HAS_PERMISSIONS
                        && LOGGED_IN
                        && NETWORK_ONLINE
                        && SERVICES_ONLINE
        );
    }

    public static boolean hasUsableValue(String source) {
        String value = (source != null) ? source : "";
        return (!TextUtils.isEmpty(value));
    }

    public static void setLoggedInState() {
        boolean isLoggedIn = (UserData.ID != null && hasUsableValue(UserData.USERNAME) && hasUsableValue(UserData.AUTH_TOKEN));
        //Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>> setLoggedInState LOGGED_IN = " + isLoggedIn + " <<<<<<<<<<<<<<<<<<<<<<<<");
        if (LOGGED_IN != isLoggedIn) {
            LOGGED_IN = isLoggedIn;
            MAP_ACTIVE_LOCATION_ID = LOCATION_ID;
            MAP_ACTIVE_EVENT_ID = MAP_ACTIVE_LOCATION_ID;

            if (isLoggedIn) {
                AUTH_STATUS_CODE = 0;
                //Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>> setLoggedInState USER_LOGIN_SUCCESSFUL <<<<<<<<<<<<<<<<<<<<<<<<");
                Dispatch.triggerEvent(ObservedEvents.USER_LOGIN_SUCCESSFUL);
            } else {
                //Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>> setLoggedInState USER_LOGIN_FAILURE <<<<<<<<<<<<<<<<<<<<<<<<");
                Dispatch.triggerEvent(ObservedEvents.USER_LOGIN_FAILURE);
            }
        }
    }

    public static boolean locationCanUpdate() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR);
        int minute = now.get(Calendar.MINUTE) - 10;
        Date date = parseDate(hour + ":" + minute);
        return LAST_LOCATION_UPDATE.before(date);
    }

    public static void locationUpdated() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR);
        int minute = now.get(Calendar.MINUTE);
        LAST_LOCATION_UPDATE = parseDate(hour + ":" + minute);
    }

    private static Date parseDate(String date) {

        try {
            return TIMESTAMP_FORMAT.parse(date);
        } catch (java.text.ParseException e) {
            return new Date(0);
        }
    }

    public static int getRandomColor() {
        int index = (int) Math.floor(Math.random() * 15);
        return CATEGORY_COLORS[index];
    }
}
