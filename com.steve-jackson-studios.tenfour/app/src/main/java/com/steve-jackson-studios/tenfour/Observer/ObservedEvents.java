package com.steve-jackson-studios.tenfour.Observer;

/**
 * Created by sjackson on 5/25/2017.
 * ObservedEvents
 */

public class ObservedEvents {
    /**
     * Event IDs for high frequency events
     */
    public static final int GLOBAL_GPS_UPDATE = 1000;
    public static final int GLOBAL_SENSOR_UPDATE = 1100;
    /**
     * Event IDs for all standard events 0-99
     */
    public static final int INITIALIZED = 0;
    public static final int PERMISSIONS_GRANTED = 1;
    public static final int USER_LOGIN_SUCCESSFUL = 2;
    public static final int USER_LOGIN_FAILURE = 3;
    public static final int USER_LOGIN_UNNECESSARY = 4;
    public static final int USER_LOGOUT = 5;
    public static final int GOOGLE_CREDENTIALS_CREATED = 6;
    public static final int FACEBOOK_CREDENTIALS_CREATED = 7;
    public static final int TWITTER_CREDENTIALS_CREATED = 8;
    public static final int NETWORK_UNAVAILABLE = 9;
    public static final int SERVICES_UNAVAILABLE = 10;
    public static final int SOCKET_UNAVAILABLE = 11;
    public static final int NETWORK_AVAILABLE = 12;
    public static final int SERVICES_AVAILABLE = 13;
    public static final int SOCKET_AVAILABLE = 14;
    public static final int APP_LOADING_COMPLETE = 15;

    public static final int MAP_READY = 30;
    public static final int PROFILE_READY = 31;
    public static final int CHAT_READY = 32;
    public static final int MAP_CLOSED = 33;
    public static final int PROFILE_CLOSED = 34;
    public static final int CHAT_CLOSED = 35;
    /**
     * Event IDs for all message carrying events 100-199
     */
    public static final int CHAT_MESSAGE_SEND = 100;
    public static final int CHAT_MESSAGE_RECEIVE = 101;
    public static final int CHAT_MESSAGE_VOTE = 102;
    public static final int CHAT_MEDIA_UPLOADED = 103;
    /**
     * Event IDs for global broadcast requests 200-299
     */
    public static final int REQUEST_LOGIN_FORM = 200;
    public static final int REQUEST_SIGNUP_FORM = 201;
    public static final int REQUEST_LAST_LOCATION = 250;
    public static final int REQUEST_DATA_UPDATE_USERS = 252;
    public static final int REQUEST_MAPJSON_DATA = 253;
    public static final int REQUEST_CLEAR_NODE_DATA = 254;
    public static final int REQUEST_CLEAR_IMAGE_DATA = 255;
    public static final int REQUEST_CLOSE_KEYBOARD = 256;
    public static final int REQUEST_CREATE_EVENT = 257;
    public static final int REQUEST_OPEN_PREVIEW = 258;
    public static final int REQUEST_CLOSE_PREVIEW = 259;
    public static final int REQUEST_MAP_PREVIEW = 260;
    public static final int REQUEST_OPEN_CHAT = 261;
    /**
     * Event IDs for global broadcast notifications 300-399
     */
    public static final int NOTIFY_AVAILABLE_NODE_DATA = 300;
    public static final int NOTIFY_AVAILABLE_CHAT_DATA = 301;
    public static final int NOTIFY_AVAILABLE_PROFILE_DATA = 302;
    public static final int NOTIFY_AVAILABLE_MAPJSON_DATA = 303;
    public static final int NOTIFY_AVAILABLE_PREVIEW_DATA = 304;
    public static final int NOTIFY_AVAILABLE_EVENT_DATA = 305;
    public static final int NOTIFY_AVAILABLE_CHAT_POPUPS = 306;
    public static final int NOTIFY_AVAILABLE_FRIENDS_DATA = 307;
    public static final int NOTIFY_EVENT_CREATED_BY_ME = 308;
    public static final int NOTIFY_EVENT_CREATE_FAILED = 309;
    public static final int NOTIFY_MAP_CAMERA_LOCKED = 310;
    public static final int NOTIFY_MAP_CAMERA_UNLOCKED = 311;
    public static final int NOTIFY_MY_LOCATION_UPDATE = 312;
    public static final int NOTIFY_SPLASH_SCREEN_FINISHED = 313;
    public static final int NOTIFY_USER_LOGGED_OUT = 314;
    public static final int NOTIFY_PREVIEW_CLOSED = 315;
    public static final int NOTIFY_FILTERS_CHANGED = 316;

    public static final int NOTIFY_MAJOR_LOCATION_CHANGED = 500;
    public static final int NOTIFY_LOCATION_ID_INVALID = 501;

    public static final int NOTIFY_ERROR_MESSAGE = 666;

    private ObservedEvents() {
    }
}
