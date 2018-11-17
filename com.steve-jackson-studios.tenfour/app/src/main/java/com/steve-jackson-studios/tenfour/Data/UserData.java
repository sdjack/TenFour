package com.steve-jackson-studios.tenfour.Data;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.steve-jackson-studios.tenfour.AppConstants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sjackson on 6/8/2017.
 * UserData
 */

public class UserData {

    public static final int AUTH_TYPE_DEFAULT = 0;
    public static final int AUTH_TYPE_GOOGLE = 1;
    public static final int AUTH_TYPE_FACEBOOK = 2;
    public static final int AUTH_TYPE_TWITTER = 3;

    public static String ID = null;
    public static String USERNAME = null;
    public static String INITIALS = null;
    public static String PASSWORD = null;
    public static String AUTH_TOKEN = null;
    public static String FIRST_NAME = null;
    public static String LAST_NAME = null;
    public static String EMAIL = null;
    public static String PHONE = null;
    public static String STATUS = "Hi!";
    public static String HOMETOWN = null;
    public static String ACTIVITY = null;
    public static String CONTACTS = null;
    public static String TROPHIES = null;
    public static int KARMA = 0;
    public static int AUTH_TYPE = 0;
    public static int AVATAR_TYPE = 0;
    public static int AVATAR_COLOR = AppConstants.AVATAR_COLORS[0];
    public static String AVATAR = AppConstants.DEFAULT_AVATAR;
    public static Uri AVATAR_URI = Uri.parse(AVATAR);
    public static boolean HEATMAP_ENABLED = true;
    public static boolean FRIENDMAP_ENABLED = true;
    public static boolean VISIBILITY_ENABLED = true;
    public static boolean STICKERS_ENABLED = true;

    private UserData() {

    }
    
    public static void resetUserData() {
        ID = null;
        USERNAME = null;
        INITIALS = null;
        PASSWORD = null;
        AUTH_TOKEN = null;
        FIRST_NAME = null;
        LAST_NAME = null;
        EMAIL = null;
        PHONE = null;
        STATUS = "Hi!";
        HOMETOWN = null;
        ACTIVITY = null;
        CONTACTS = null;
        TROPHIES = null;
        HEATMAP_ENABLED = true;
        FRIENDMAP_ENABLED = true;
        VISIBILITY_ENABLED = true;
        STICKERS_ENABLED = true;
        KARMA = 0;
        AVATAR_TYPE = 0;
        AVATAR_COLOR = AppConstants.AVATAR_COLORS[0];
        AVATAR = AppConstants.DEFAULT_AVATAR;
        AVATAR_URI = Uri.parse(AVATAR);
    }

    public static void setAccountType(int isGoogle, int isFacebook, int isTwitter) {
        if (isGoogle > 0) {
            AUTH_TYPE = AUTH_TYPE_GOOGLE;
        } else if (isFacebook > 0) {
            AUTH_TYPE = AUTH_TYPE_FACEBOOK;
        } else if (isTwitter > 0) {
            AUTH_TYPE = AUTH_TYPE_TWITTER;
        } else {
            AUTH_TYPE = AUTH_TYPE_DEFAULT;
        }
    }

    public static void saveUserData(JSONObject data) throws JSONException {
        ID = data.getString("ID");
        USERNAME = data.getString("USERNAME");
        FIRST_NAME = data.getString("FIRST_NAME");
        LAST_NAME = data.getString("LAST_NAME");
        EMAIL = data.getString("EMAIL");
        PHONE = data.getString("PHONE");
        AVATAR = data.getString("AVATAR");
        AVATAR_TYPE = (!data.isNull("AVATAR_TYPE") && data.get("AVATAR_TYPE") instanceof Integer) ? data.getInt("AVATAR_TYPE"): 0;
        AVATAR_COLOR = (!data.isNull("AVATAR_COLOR") && data.get("AVATAR_COLOR") instanceof Integer) ? data.getInt("AVATAR_COLOR"): AppConstants.AVATAR_COLORS[0];
        HOMETOWN = data.getString("HOMETOWN");
        ACTIVITY = data.getString("ACTIVITY");
        TROPHIES = data.getString("TROPHIES");
        KARMA = data.getInt("KARMA");
        AVATAR_URI = Uri.parse(AVATAR);
        INITIALS = FIRST_NAME.substring(0,1) + LAST_NAME.substring(0,1);
        STATUS = data.getString("STATUS");
        if (TextUtils.isEmpty(STATUS)) {
            STATUS = "Hi!";
        }
        CONTACTS = data.getString("CONTACTS");
    }
}
