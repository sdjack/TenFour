package com.steve-jackson-studios.tenfour;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.steve-jackson-studios.tenfour.Data.ChatData;
import com.steve-jackson-studios.tenfour.Data.EventData;
import com.steve-jackson-studios.tenfour.Data.FriendData;
import com.steve-jackson-studios.tenfour.Data.MapData;
import com.steve-jackson-studios.tenfour.Data.UserData;
import com.steve-jackson-studios.tenfour.IO.RestClient;
import com.steve-jackson-studios.tenfour.IO.SocketIO;
import com.steve-jackson-studios.tenfour.Data.DataHelper;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Stack;

import io.socket.emitter.Emitter;

/**
 * Created by sjackson on 10/17/2016.
 * AppResolver
 */
public class AppResolver implements RestClient.ResponseListener {

    private static final String TAG = AppResolver.class.getName();

    private final Handler handler;
    private Context context;
    private SharedPreferences preferences;
    private RestClient httpClient;
    private Stack<JSONObject> pendingPosts = new Stack<>();
    /**
     * Instantiates a new Android resolver.
     *
     * @param c the c
     */
    public AppResolver(Context c) {

        this.context = c;
        this.handler = new Handler();
        this.httpClient = new RestClient(c, this);
        this.preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        loadAllEmitters();
        saveVerifiedUserData();
        Dispatch.triggerEvent(ObservedEvents.REQUEST_LAST_LOCATION);
    }

    public void initialize() {
        if (!AppConstants.LOGGED_IN) {
            AppConstants.setLoggedInState();
            return;
        }
        postInitialize();
    }

    public void postInitialize() {
        if (AppConstants.INITIALIZATION_COMPLETE) return;
        AppConstants.INITIALIZATION_COMPLETE = true;
        Dispatch.triggerEvent(ObservedEvents.INITIALIZED);
    }

    public void serviceRequest(String uri, JSONObject json) {
        httpClient.request(uri, json);
    }

    /**
     * Helpers used for storing credentials
     */
    public synchronized void setActiveLocationID(String subDivisionId, String locationId) {
        if (locationId != null) {
            SharedPreferences.Editor editor = preferences.edit();
            AppConstants.LOCATION_ID = locationId;
            AppConstants.ACTIVE_LOCATIONS.add(locationId);
            editor.putString("LocationID", locationId);
            editor.apply();
        }
        if (subDivisionId != null) {
            SharedPreferences.Editor editor = preferences.edit();
            AppConstants.SUBDIVISION_ID = subDivisionId;
            AppConstants.ACTIVE_LOCATIONS.add(subDivisionId);
            editor.putString("SubDivisionID", subDivisionId);
            editor.apply();
            Dispatch.triggerEvent(ObservedEvents.NOTIFY_MAJOR_LOCATION_CHANGED);
        }
    }

    public synchronized void setActiveNode() {

        if (AppConstants.PENDING_LOCATION == null || AppConstants.PENDING_SUBDIVISION_ID == null) {
            AppConstants.resetPendingNodeValues();
            return;
        }

        //Log.d("DEVDEBUG", "setActiveNode values :::: LOCATION_ID = " + AppConstants.PENDING_LOCATION + ", SUBDIVISION_ID = " + AppConstants.SUBDIVISION_ID);

        if (!AppConstants.hasUsableValue(AppConstants.LOCATION_ID)
                || !AppConstants.LOCATION_ID.equals(AppConstants.PENDING_LOCATION)
                || !AppConstants.hasUsableValue(AppConstants.SUBDIVISION_ID)
                || !AppConstants.SUBDIVISION_ID.equals(AppConstants.PENDING_SUBDIVISION_ID)) {
            boolean subDivisionChanged = (AppConstants.SUBDIVISION_ID == null || !AppConstants.SUBDIVISION_ID.equals(AppConstants.PENDING_SUBDIVISION_ID));
            AppConstants.resetReplyState();
            AppConstants.LOCATION_ID = AppConstants.PENDING_LOCATION;
            AppConstants.SUBDIVISION_ID = AppConstants.PENDING_SUBDIVISION_ID;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("LocationID", AppConstants.LOCATION_ID);
            editor.putString("SubDivisionID", AppConstants.SUBDIVISION_ID);
            editor.apply();

            if (subDivisionChanged || !DataHelper.FILE_DATA_SAVED) {
                requestDataBundle();
            } else {
                requestMapData();
            }
            Dispatch.triggerEvent(ObservedEvents.NOTIFY_MAJOR_LOCATION_CHANGED);
        }

        SocketIO.connectSocket();
        AppConstants.resetPendingNodeValues();

        if (AppConstants.hasUsableValue(AppConstants.LOCATION_ID)) {
            while (!pendingPosts.empty()) {
                try {
                    JSONObject data = pendingPosts.pop();
                    String msg = data.getString("MESSAGE");
                    String img = data.getString("IMAGE");
                    String type = data.getString("TYPE");
                    String reply = (!data.isNull("REPLY_ID")) ? data.getString("REPLY_ID"): "";
                    int sticker = data.getInt("STICKER");
                    sendStreamData(msg, img, type, reply, sticker);
                } catch (IllegalStateException | JsonSyntaxException ex) {
                    Log.e(TAG, "IllegalStateException", ex);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void saveVerifiedUserData() {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("UserID", UserData.ID);
            editor.putString("UserName", UserData.USERNAME);
            editor.putString("PassWord", UserData.PASSWORD);
            editor.putString("AuthToken", UserData.AUTH_TOKEN);
            editor.putString("UserFirstName", UserData.FIRST_NAME);
            editor.putString("UserLastName", UserData.LAST_NAME);
            editor.putString("UserEmail", UserData.EMAIL);
            editor.putString("UserPhone", UserData.PHONE);
            editor.putString("UserAvatarUrl", UserData.AVATAR);
            editor.putInt("UserAvatarType", UserData.AVATAR_TYPE);
            editor.putInt("UserAvatarColor", UserData.AVATAR_COLOR);
            editor.putString("UserStatus", UserData.STATUS);
            editor.putString("UserHomeTown", UserData.HOMETOWN);
            editor.putString("UserActivity", UserData.ACTIVITY);
            editor.putString("UserContacts", UserData.CONTACTS);
            editor.putString("UserTrophies", UserData.TROPHIES);
            editor.putInt("AuthType", UserData.AUTH_TYPE);
            editor.putInt("UserKarma", UserData.KARMA);
            editor.putBoolean("FilterShowHeatmap", UserData.HEATMAP_ENABLED);
            editor.putBoolean("FilterShowFriendmap", UserData.FRIENDMAP_ENABLED);
            editor.putBoolean("FilterShowStickers", UserData.STICKERS_ENABLED);
            editor.putBoolean("FilterAllowVisibility", UserData.VISIBILITY_ENABLED);
            editor.apply();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public synchronized void saveUserFilters() {

        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("FilterShowHeatmap", UserData.HEATMAP_ENABLED);
            editor.putBoolean("FilterShowFriendmap", UserData.FRIENDMAP_ENABLED);
            editor.putBoolean("FilterShowStickers", UserData.STICKERS_ENABLED);
            editor.putBoolean("FilterAllowVisibility", UserData.VISIBILITY_ENABLED);
            editor.apply();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void saveProfileData(JSONObject data) {
        if (!AppConstants.LOGGED_IN) return;
        try {
            UserData.saveUserData(data);
        } catch (IllegalStateException | JsonSyntaxException | JSONException ex) {
            ex.printStackTrace();
        }
        saveVerifiedUserData();
        Dispatch.triggerEvent(ObservedEvents.NOTIFY_AVAILABLE_PROFILE_DATA);
    }



//    public void saveProfileHometown(final String firstName, final String lastName, final String homeTownName, final int homeTownId) {
//        if (AppConstants.LOGGED_IN && UserData.ID != null) {
//            UserData.FIRST_NAME = firstName;
//            UserData.LAST_NAME = lastName;
//            UserData.HOMETOWN = homeTownName;
//            JSONObject row = new JSONObject();
//            try {
//                row.put("userId", UserData.ID);
//                row.put("firstName", firstName);
//                row.put("lastName", lastName);
//                row.put("homeTownId", homeTownId);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            serviceRequest(AppConstants.SERVICE_HOMETOWNUPDATE, row);
//        }
//    }

    public void saveProfileHometown(final String homeTownName, final int homeTownId) {
        if (AppConstants.LOGGED_IN && UserData.ID != null) {
            UserData.HOMETOWN = homeTownName;
            JSONObject row = new JSONObject();
            try {
                row.put("userId", UserData.ID);
                row.put("firstName", UserData.FIRST_NAME);
                row.put("lastName", UserData.LAST_NAME);
                row.put("homeTownId", homeTownId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            serviceRequest(AppConstants.SERVICE_HOMETOWNUPDATE, row);
        }
    }

    public synchronized void logout() {
        AppConstants.LOGGED_IN = false;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("UserID", null);
        editor.putString("LocationID", null);
        editor.putString("SubDivisionID", null);
        editor.putString("UserName", null);
        editor.putString("PassWord", null);
        editor.putString("AuthToken", null);
        editor.putString("UserFirstName", null);
        editor.putString("UserLastName", null);
        editor.putString("UserEmail", null);
        editor.putString("UserPhone", null);
        editor.putString("UserAvatarUrl", null);
        editor.putInt("UserAvatarType", 0);
        editor.putInt("UserAvatarColor", AppConstants.AVATAR_COLORS[0]);
        editor.putString("UserStatus", null);
        editor.putString("UserActivity", null);
        editor.putString("UserContacts", null);
        editor.putString("UserTrophies", null);
        editor.putInt("UserKarma", 0);
        editor.putBoolean("UserIsLoggedIn", false);
        editor.apply();

        JSONObject row = new JSONObject();
        try {
            row.put("userId", UserData.ID);
            row.put("userName", UserData.USERNAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serviceRequest(AppConstants.SERVICE_LOGOUT, row);
        UserData.resetUserData();

        Dispatch.triggerEvent(ObservedEvents.NOTIFY_USER_LOGGED_OUT);
    }

    public synchronized void updateLocation() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("previousLatitude", AppConstants.GPS_LATITUDE);
        editor.putFloat("previousLongitude", AppConstants.GPS_LONGITUDE);
        editor.putFloat("previousAltitude", AppConstants.GPS_ALTITUDE);
        editor.apply();

        if (AppConstants.LOGGED_IN && UserData.ID != null) {
            // && AppConstants.locationCanUpdate()
            JSONObject row = new JSONObject();
            try {
                row.put("userId", UserData.ID);
                row.put("userLat", AppConstants.GPS_LATITUDE);
                row.put("userLon", AppConstants.GPS_LONGITUDE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            serviceRequest(AppConstants.SERVICE_LOCATIONUPDATE, row);
        } else if (!AppConstants.hasUsableValue(AppConstants.LOCATION_ID)) {
            getCurrentGeoFence(AppConstants.GPS_LATITUDE, AppConstants.GPS_LONGITUDE);
        }
    }

    public void saveProfileAvatar(String avatarUrl) {
        if (!AppConstants.LOGGED_IN || !AppConstants.hasUsableValue(UserData.ID)) return;
        UserData.AVATAR_TYPE = 1;
        UserData.AVATAR = avatarUrl;
        UserData.AVATAR_URI = Uri.parse(UserData.AVATAR);

        JSONObject row = new JSONObject();
        try {
            row.put("userId", UserData.ID);
            row.put("avatarType", 1);
            row.put("avatar", avatarUrl);
            row.put("avatarColor", UserData.AVATAR_COLOR);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serviceRequest(AppConstants.SERVICE_AVATARUPDATE, row);
    }

    public void saveProfileAvatar(int avatarColor) {
        if (!AppConstants.LOGGED_IN || !AppConstants.hasUsableValue(UserData.ID)) return;
        UserData.AVATAR_TYPE = 0;
        UserData.AVATAR_COLOR = avatarColor;

        JSONObject row = new JSONObject();
        try {
            row.put("userId", UserData.ID);
            row.put("avatarType", 0);
            row.put("avatar", UserData.AVATAR);
            row.put("avatarColor", avatarColor);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serviceRequest(AppConstants.SERVICE_AVATARUPDATE, row);
    }

    public void saveProfileStatus(String newStatus) {
        UserData.STATUS = newStatus.replace("'", "''");

        JSONObject row = new JSONObject();
        try {
            row.put("userId", UserData.ID);
            row.put("userStatus", UserData.STATUS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serviceRequest(AppConstants.SERVICE_STATUSUPDATE, row);
    }

    public void getCurrentGeoFence(float lat, float lon) {
        JSONObject row = new JSONObject();
        try {
            row.put("userLat", lat);
            row.put("userLon", lon);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serviceRequest(AppConstants.SERVICE_MAPLOCATION, row);
    }

    public void requestDataBundle() {
        if (!AppConstants.LOGGED_IN || AppConstants.SUBDIVISION_ID == null) return;
        //Log.d("DEVDEBUG", "AppConstants.SUBDIVISION_ID = " + AppConstants.SUBDIVISION_ID);
        JSONObject row = new JSONObject();
        try {
            row.put("locationId", AppConstants.SUBDIVISION_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serviceRequest(AppConstants.SERVICE_DATA_BUNDLE, row);
        //Log.d("DEVDEBUG", ">>>>>>>>>>>>>>>> requestDataBundle API CALL FIRED <<<<<<<<<<<");
    }

    public void requestMapData() {
        if (!AppConstants.LOGGED_IN) {
            return;
        }
        JSONObject row = new JSONObject();
        try {
            row.put("subDivisionId", AppConstants.SUBDIVISION_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serviceRequest(AppConstants.SERVICE_MAPSUBDIVISION, row);
    }

    public void requestHeatMapData() {
        if (!AppConstants.LOGGED_IN) {
            return;
        }
        String locationId = (AppConstants.hasUsableValue(AppConstants.SUBDIVISION_ID)) ? AppConstants.SUBDIVISION_ID : AppConstants.LOCATION_ID;
        if (locationId != null) {
            JSONObject row = new JSONObject();
            try {
                row.put("sdId", locationId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            serviceRequest(AppConstants.SERVICE_HEATMAP, row);
        }
        //Log.d("DEVDEBUG", ">>>>>>>>>>>>>>>> requestHeatMapData API CALL FIRED <<<<<<<<<<<");
    }

    public void requestChatData() {
        if (!AppConstants.LOGGED_IN) {
            return;
        }
        String locationId = (AppConstants.hasUsableValue(AppConstants.SUBDIVISION_ID)) ? AppConstants.SUBDIVISION_ID : AppConstants.LOCATION_ID;
        if (locationId != null) {
            JSONObject row = new JSONObject();
            try {
                row.put("locationId", locationId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            serviceRequest(AppConstants.SERVICE_CHATDATA, row);
        }
    }

    public void requestChatPreview() {
        //Log.d("DEVDEBUG", ">>>>>>>>>>>>>>>> requestChatPreview CALLED " + AppConstants.MAP_ACTIVE_LOCATION_ID + " <<<<<<<<<<<");
        if (!AppConstants.LOGGED_IN || !AppConstants.hasUsableValue(AppConstants.MAP_ACTIVE_LOCATION_ID)) return;
        JSONObject row = new JSONObject();
        try {
            row.put("locationId", AppConstants.MAP_ACTIVE_LOCATION_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serviceRequest(AppConstants.SERVICE_CHATPREVIEW, row);
    }

    public void requestEventData() {
        if (!AppConstants.LOGGED_IN || !AppConstants.hasUsableValue(AppConstants.LOCATION_ID)) return;
        JSONObject row = new JSONObject();
        try {
            row.put("locationId", AppConstants.LOCATION_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serviceRequest(AppConstants.SERVICE_EVENTDATA, row);
    }

    public void requestEventPreview() {
        //Log.d("DEVDEBUG", ">>>>>>>>>>>>>>>> requestChatPreview CALLED " + AppConstants.MAP_ACTIVE_LOCATION_ID + " <<<<<<<<<<<");
        if (!AppConstants.LOGGED_IN || !AppConstants.hasUsableValue(AppConstants.MAP_ACTIVE_LOCATION_ID)) return;
        JSONObject row = new JSONObject();
        try {
            row.put("locationId", AppConstants.MAP_ACTIVE_LOCATION_ID);
            row.put("eventId", AppConstants.MAP_ACTIVE_EVENT_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serviceRequest(AppConstants.SERVICE_CHATPREVIEW, row);
    }

    public void requestFriendship(String userName) {
        if (!AppConstants.LOGGED_IN) {
            return;
        }
        JSONObject row = new JSONObject();
        try {
            row.put("userId", UserData.ID);
            row.put("toUser", userName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serviceRequest(AppConstants.SERVICE_SENDFRIENDREQUEST, row);
    }

    public void requestFriendData() {
        if (!AppConstants.LOGGED_IN) {
            return;
        }
        JSONObject row = new JSONObject();
        try {
            row.put("userId", UserData.ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serviceRequest(AppConstants.SERVICE_GETFRIENDS, row);
    }

    public void updateFriendRequest(String friendId, int responseState) {
        if (!AppConstants.LOGGED_IN) {
            return;
        }
        JSONObject row = new JSONObject();
        try {
            row.put("userId", UserData.ID);
            row.put("friendId", friendId);
            row.put("responseState", responseState);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serviceRequest(AppConstants.SERVICE_UPDATEFRIENDREQUEST, row);
    }

    public void createNewEvent(String title, int category) {
        //Log.d("EventCreateDialog", ">>>>>>>>>>>>>>>> createNewEvent CALLED " + AppConstants.LOCATION_ID + " STATE = " + AppConstants.LOGGED_IN + "<<<<<<<<<<<");
        if (!AppConstants.LOGGED_IN || !AppConstants.hasUsableValue(AppConstants.LOCATION_ID)) return;
        JSONObject row = new JSONObject();
        try {
            row.put("userId", UserData.ID);
            row.put("locationId", AppConstants.LOCATION_ID);
            row.put("roomId", AppConstants.SUBDIVISION_ID);
            row.put("eventDesc", title);
            row.put("eventCat", category);
            row.put("userLat", AppConstants.GPS_LATITUDE);
            row.put("userLon", AppConstants.GPS_LONGITUDE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serviceRequest(AppConstants.SERVICE_CREATE_EVENT, row);
        //SocketIO.emit("create event", row);
    }

    public void queueStreamData(final String inputText, final String mediaFile, final int isSticker) {
        AppConstants.LOCALIZE_MY_POSTS = true;
        String messageType = (AppConstants.hasUsableValue(AppConstants.REPLY_ID)) ? "reply" : "public";
        if (!AppConstants.hasUsableValue(AppConstants.EVENT_ID)) {
            AppConstants.EVENT_ID = AppConstants.LOCATION_ID;
        }
        JSONObject row = new JSONObject();
        try {
            row.put("ORDER_ID", 0);
            row.put("ID", AppConstants.LAST_CHAT_ID + 1);
            row.put("USER_ID", UserData.ID);
            row.put("AVATAR_TYPE", UserData.AVATAR_TYPE);
            row.put("AVATAR_COLOR", UserData.AVATAR_COLOR);
            row.put("LOCATION", AppConstants.LOCATION_ID);
            row.put("EVENT_ID", AppConstants.EVENT_ID);
            row.put("SUBDIVISION_ID", AppConstants.SUBDIVISION_ID);
            row.put("REPLY_ID", AppConstants.REPLY_ID);
            row.put("REPLY_COUNT", AppConstants.REPLY_LEVEL);
            row.put("REACTIONS", "{}");
            row.put("LAST_UPDATE", ChatData.getDateTime());
            row.put("LATITUDE", AppConstants.GPS_LATITUDE);
            row.put("LONGITUDE", AppConstants.GPS_LONGITUDE);
            row.put("USERNAME", UserData.USERNAME);
            row.put("DISPLAY_NAME", UserData.USERNAME);
            row.put("DISPLAY_INITIALS", UserData.INITIALS);
            row.put("MESSAGE", inputText);
            row.put("IMAGE", mediaFile);
            row.put("STICKER", isSticker);
            row.put("TYPE", messageType);
            row.put("SCORE", 0);
            row.put("KARMA", 100);

            ChatData.loadChatData(row);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!AppConstants.hasUsableValue(AppConstants.SUBDIVISION_ID)) {
            pendingPosts.push(row);
        } else {
            sendStreamData(inputText, mediaFile, messageType, AppConstants.REPLY_ID, isSticker);
        }
    }

    public void sendStreamData(final String inputText, final String mediaFile, final String messageType, final String replyId, final int isSticker) {
        if (AppConstants.hasUsableValue(AppConstants.LOCATION_ID)) {
            final String channelId = (AppConstants.SUBDIVISION_ID != null) ? AppConstants.SUBDIVISION_ID : "public";
            JSONObject row = new JSONObject();
            try {
                if (messageType.equals("reply")) {
                    row.put("replyId", replyId);
                }
                row.put("userId", UserData.ID);
                row.put("channelId", channelId);
                row.put("locationId", AppConstants.LOCATION_ID);
                row.put("eventId", AppConstants.EVENT_ID);
                row.put("userName", UserData.USERNAME);
                row.put("userPost", inputText.replace("'", "''"));
                row.put("userMedia", mediaFile);
                row.put("userLat", AppConstants.GPS_LATITUDE);
                row.put("userLon", AppConstants.GPS_LONGITUDE);
                row.put("messageType", messageType);
                row.put("isSticker", isSticker);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            SocketIO.emit("new message", row);
        }
    }

    public boolean networkAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void showToast(final CharSequence text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllEmitters() {
        SocketIO.addListener("autoreply", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //Log.d("DEVDEBUG", args[0].toString());
                try {
                    JSONArray jsonData = SocketIO.getResponseArray((JSONObject) args[0]);
                    if (null != jsonData && jsonData.length() > 0) {
                        JSONObject row = (JSONObject) jsonData.get(0);
                        row.put("ORDER_ID", 0);
                        row.put("AVATAR_TYPE", 1);
                        row.put("AVATAR_COLOR", 0x00000000);
                        row.put("LOCATION", AppConstants.LOCATION_ID);
                        row.put("SUBDIVISION_ID", AppConstants.SUBDIVISION_ID);
                        row.put("REPLY_ID", AppConstants.REPLY_ID);
                        ChatData.loadSystemMessage(row);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        SocketIO.addListener("chat reaction", new Emitter.Listener() {
            //Log.d("DEVDEBUG", "SocketIO EVENT: [[public message]] >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            @Override
            public void call(Object... args) {
                try {
                    JSONArray jsonData = SocketIO.getResponseArray((JSONObject) args[0]);
                    if (null != jsonData) {
                        ChatData.loadChatData(jsonData);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        SocketIO.addListener("event created", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONArray jsonData = SocketIO.getResponseArray((JSONObject) args[0]);
                    if (null != jsonData && jsonData.length() > 0) {
                        JSONObject row = (JSONObject) jsonData.get(0);
                        if (!row.isNull("STATUS_CODE") && row.getInt("STATUS_CODE") == 0 && !row.isNull("CREATED_BY") && !row.getString("CREATED_BY").equals(UserData.ID)) {
                            AppConstants.EVENT_ID = row.getString("ID");
                            AppConstants.CHAT_TITLE = row.getString("DESCRIPTION");
                            AppConstants.STATUS_MESSAGE = row.getString("STATUS_MESSAGE");
                            EventData.loadEventData(row);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        SocketIO.addListener("public message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //Log.d("DEVDEBUG", "SocketIO EVENT: [[public message]] >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                try {
                    JSONArray jsonData = SocketIO.getResponseArray((JSONObject) args[0]);
                    if (null != jsonData) {
                        Log.d("DEVDEBUG", jsonData.toString());
                        ChatData.loadChatData(jsonData);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        SocketIO.addListener("reply message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //Log.d("DEVDEBUG", "SocketIO EVENT: [[reply message]] >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                try {
                    JSONArray jsonData = SocketIO.getResponseArray((JSONObject) args[0]);
                    if (null != jsonData) {
                        //Log.d("DEVDEBUG", jsonData.toString());
                        ChatData.loadChatData(jsonData);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        SocketIO.addListener("request denied", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String message = data.getString("message");
                    Log.e("REQUEST DETECTED! ", " DENIED (" + message + ")");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        SocketIO.addListener("channel joined", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //Log.d("DEVDEBUG", "SocketIO EVENT: [[channel joined]] >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                try {
                    JSONArray jsonData = SocketIO.getResponseArray((JSONObject) args[0]);
                    if (null != jsonData) {
                        //Log.d("DEVDEBUG", jsonData.toString());
                        ChatData.loadChatData(jsonData);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        SocketIO.addListener("channel message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //Log.d("DEVDEBUG", "SocketIO EVENT: [[channel message]] >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                try {
                    JSONArray jsonData = SocketIO.getResponseArray((JSONObject) args[0]);
                    if (null != jsonData) {
                        //Log.d("DEVDEBUG", jsonData.toString());
                        ChatData.loadChatData(jsonData);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onServiceResponse(int serviceEvent, JSONArray data) {
        if (!AppConstants.SERVICES_ONLINE) {
            AppConstants.SERVICES_ONLINE = true;
            Dispatch.triggerEvent(ObservedEvents.SERVICES_AVAILABLE);
        }
        //Log.d("DEVDEBUG", data.toString());
        switch (serviceEvent) {
            case AppConstants.EVENT_SERVICE_DATA_BUNDLE:
                //Log.d("DEVDEBUG", data.toString());
                DataHelper.saveDataBundleFile(context, data);
                requestFriendData();
                break;
            case AppConstants.EVENT_SERVICE_LOCATIONUPDATE:
                AppConstants.INIT_LOCATION_FOUND = true;
                try {
                    if (data.length() > 0) {
                        JSONObject row = (JSONObject) data.get(0);
                        if (!row.isNull("SUBDIVISION_ID") && !row.isNull("ID")) {
                            AppConstants.PENDING_LOCATION = row.getString("ID");
                            AppConstants.PENDING_SUBDIVISION_ID = row.getString("SUBDIVISION_ID");
                            int minorIndex = row.getInt("MINOR_INDEX");
                            AppConstants.SOCKET_ID = "SOCKET_" + minorIndex;
                            AppConstants.BLOB_CONTAINER = row.getString("BLOB_CONTAINER");
                            if (!row.isNull("AREA_LIST")) {
                                JSONArray areaList = new JSONArray(row.getString("AREA_LIST"));
                                //Log.d("DEVDEBUG", areaList.toString());
                                int alCount = areaList.length();
                                if (alCount > 0) {
                                    AppConstants.ACTIVE_LOCATIONS.clear();
                                    String newSub = "";
                                    for (int i = 0; i < alCount; i++) {
                                        JSONObject subRow = (JSONObject) areaList.get(i);
                                        if (!subRow.isNull("ID")) {
                                            AppConstants.ACTIVE_LOCATIONS.add(subRow.getString("ID"));
                                        }
                                        if (!subRow.isNull("SUBDIVISION_ID")) {
                                            newSub = subRow.getString("SUBDIVISION_ID");
                                        }
                                    }
                                    if (!TextUtils.isEmpty(newSub)) {
                                        AppConstants.ACTIVE_LOCATIONS.add(newSub);
                                    }
                                }
                            }
                            AppConstants.locationUpdated();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MapData.loadMapData(data);
                //requestChatData();
                break;
            case AppConstants.EVENT_SERVICE_MAPDATA:
                MapData.loadMapData(data);
                requestChatData();
                break;
            case AppConstants.EVENT_SERVICE_CHATDATA:
                //Log.d("DEVDEBUG", data.toString());
                ChatData.loadChatData(data);
                requestFriendData();
                break;
            case AppConstants.EVENT_SERVICE_EVENTDATA:
                EventData.loadEventData(data);
                break;
            case AppConstants.EVENT_SERVICE_CHATPREVIEW:
                ChatData.loadPreviewData(data);
                break;
            case AppConstants.EVENT_SERVICE_MAPLOCATION:
                try {
                    if (data.length() > 0) {
                        JSONObject row = (JSONObject) data.get(0);
                        if (!row.isNull("SUBDIVISION_ID") && !row.isNull("LOCATION")) {
                            setActiveLocationID(row.getString("SUBDIVISION_ID"), row.getString("LOCATION"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case AppConstants.EVENT_SERVICE_CREATE_EVENT:
                try {
                    if (data.length() > 0) {
                        JSONObject row = (JSONObject) data.get(0);
                        AppConstants.STATUS_MESSAGE = row.getString("STATUS_MESSAGE");
                        if (!row.isNull("STATUS_CODE") && row.getInt("STATUS_CODE") == 0) {
                            AppConstants.EVENT_ID = row.getString("ID");
                            AppConstants.CHAT_TITLE = row.getString("DESCRIPTION");
                            EventData.loadEventData(row);
                            Dispatch.triggerEvent(ObservedEvents.NOTIFY_EVENT_CREATED_BY_ME);
                            return;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Dispatch.triggerEvent(ObservedEvents.NOTIFY_EVENT_CREATE_FAILED);
                break;
            case AppConstants.EVENT_SERVICE_HOMETOWNUPDATE:
                try {
                    if (data.length() > 0) {
                        JSONObject row = (JSONObject) data.get(0);
                        if (!row.isNull("HOMETOWN")) {
                            saveProfileData(row);
                            showToast("Hometown Updated");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case AppConstants.EVENT_SERVICE_STATUSUPDATE:
                try {
                    if (data.length() > 0) {
                        JSONObject row = (JSONObject) data.get(0);
                        if (!row.isNull("STATUS")) {
                            saveProfileData(row);
                            showToast("Status Updated");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case AppConstants.EVENT_SERVICE_AVATARUPDATE:
                try {
                    if (data.length() > 0) {
                        JSONObject row = (JSONObject) data.get(0);
                        if (!row.isNull("ID")) {
                            saveProfileData(row);
                            showToast("Avatar Updated");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case AppConstants.EVENT_SERVICE_GETFRIENDS:
                FriendData.loadFriendData(data);
                break;
            case AppConstants.EVENT_SERVICE_GETFRIENDREQUESTS:
                AppConstants.FRIEND_REQUESTS = data;
                Dispatch.triggerEvent(ObservedEvents.NOTIFY_AVAILABLE_PROFILE_DATA);
                break;
            case AppConstants.EVENT_SERVICE_SENDFRIENDREQUEST:
                showToast("Friend Request Sent");
                Dispatch.triggerEvent(ObservedEvents.NOTIFY_AVAILABLE_PROFILE_DATA);
                break;
            case AppConstants.EVENT_SERVICE_UPDATEFRIENDREQUEST:
                FriendData.loadFriendData(data);
                break;
        }
    }

    @Override
    public void onErrorResponse() {
        AppConstants.LOGIN_PENDING = false;
//        AppConstants.SERVICES_ONLINE = false;
        //Dispatch.triggerEvent(ObservedEvents.SERVICES_UNAVAILABLE);
    }
}
