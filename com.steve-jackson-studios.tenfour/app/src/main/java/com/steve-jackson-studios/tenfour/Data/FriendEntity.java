package com.steve-jackson-studios.tenfour.Data;

import com.google.android.gms.maps.model.LatLng;
import com.steve-jackson-studios.tenfour.AppConstants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sjackson on 7/27/2017.
 * FriendEntity
 */

public class FriendEntity {

    private final String TAG = "FriendEntity";

    public final String friendId;
    public final String userName;
    public final String displayInitials;

    public String locationId;
    public String subDivisionId;
    public String avatar;
    public int avatarType;
    public int avatarColor;
    public int karmaScore;
    public int request;
    public boolean online;
    public double latitude;
    public double longitude;
    public String userStatus;
    public String timeStamp;
    public boolean blocked = false;

    public FriendEntity() {
        friendId = "TBD";
        userName = "TDB";
        displayInitials = "ZZ";
        locationId = "TBD";
        subDivisionId = "TBD";
        latitude = 0;
        longitude = 0;
        karmaScore = 0;
        request = 0;
        timeStamp = "2099-08-09 18:55:05.883";
        userStatus = "";
        avatar = "";
        avatarType = 0;
        avatarColor = AppConstants.AVATAR_COLORS[0];
        online = false;
    }

    public FriendEntity(JSONObject data) throws JSONException {
        // Log.d(TAG, data.toString());
        friendId = FriendData.getUsableUUID(data, FriendData.Fields.ID);
        userName = data.getString(FriendData.Fields.USERNAME);
        displayInitials = data.getString(FriendData.Fields.DISPLAY_INITIALS);
        locationId = FriendData.getUsableUUID(data, FriendData.Fields.LOCATION);
        subDivisionId = FriendData.getUsableUUID(data, FriendData.Fields.SUBDIVISION_ID);
        avatar = data.getString(FriendData.Fields.AVATAR);
        avatarType = data.getInt(FriendData.Fields.AVATAR_TYPE);
        avatarColor = data.getInt(FriendData.Fields.AVATAR_COLOR);
        request = data.getInt(FriendData.Fields.REQUEST);
        latitude = data.getDouble(FriendData.Fields.LATITUDE);
        longitude = data.getDouble(FriendData.Fields.LONGITUDE);
        karmaScore = data.getInt(FriendData.Fields.KARMA);
        online = data.getBoolean(FriendData.Fields.ONLINE);

        if (!data.isNull(FriendData.Fields.LAST_UPDATE)) {
            timeStamp = data.getString(FriendData.Fields.LAST_UPDATE);
        } else {
            timeStamp = "2099-08-09 18:55:05.883";
        }

        if (!data.isNull(FriendData.Fields.STATUS)) {
            userStatus = data.getString(FriendData.Fields.STATUS);
        } else {
            userStatus = "Hi there!";
        }
    }

    public void update(JSONObject data) throws JSONException {
        locationId = FriendData.getUsableUUID(data, FriendData.Fields.LOCATION);
        subDivisionId = FriendData.getUsableUUID(data, FriendData.Fields.SUBDIVISION_ID);
        avatar = data.getString(FriendData.Fields.AVATAR);
        avatarType = data.getInt(FriendData.Fields.AVATAR_TYPE);
        avatarColor = data.getInt(FriendData.Fields.AVATAR_COLOR);
        request = data.getInt(FriendData.Fields.REQUEST);
        latitude = data.getDouble(FriendData.Fields.LATITUDE);
        longitude = data.getDouble(FriendData.Fields.LONGITUDE);
        karmaScore = data.getInt(FriendData.Fields.KARMA);
        online = data.getBoolean(FriendData.Fields.ONLINE);

        if (!data.isNull(FriendData.Fields.LAST_UPDATE)) {
            timeStamp = data.getString(FriendData.Fields.LAST_UPDATE);
        } else {
            timeStamp = "2099-08-09 18:55:05.883";
        }

        if (!data.isNull(FriendData.Fields.STATUS)) {
            userStatus = data.getString(FriendData.Fields.STATUS);
        } else {
            userStatus = "Hi there!";
        }
    }

    public String getId() {
        return friendId;
    }

    public int getColor() {
        return avatarColor;
    }

    public boolean isRequest() { return (request != 0); }

    public void setBlocked(boolean isblocked) { blocked = isblocked; }

    public boolean isBlocked() { return blocked; }

    public boolean isNearby() { return (online && (AppConstants.ACTIVE_LOCATIONS.contains(locationId) || AppConstants.ACTIVE_LOCATIONS.contains(subDivisionId))); }

    public int getAuraColor() {
        int colorIndex = (karmaScore > 74) ? 1 : (karmaScore < 26) ? 2 : 0;
        return AppConstants.AURA_COLORS[colorIndex];
    }

    public String getTitle() {
        return userName;
    }

    public String getSnippet() {
        return userStatus;
    }

    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }
}
