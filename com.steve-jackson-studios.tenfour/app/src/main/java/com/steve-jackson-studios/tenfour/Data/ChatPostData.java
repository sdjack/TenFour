package com.steve-jackson-studios.tenfour.Data;

import android.support.annotation.NonNull;

import com.steve-jackson-studios.tenfour.AppConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by sjackson on 7/27/2017.
 * ChatPostData
 */

public class ChatPostData implements Comparable<ChatPostData> {

    private final String TAG = "ChatPostData";

    public final int order;
    public final int postScore;
    public final int karmaScore;
    public final int replyCount;
    public final int myVote;
    public final int isSticker;
    public final int avatarType;
    public final int avatarColor;
    public final double latitude;
    public final double longitude;
    public final String locationId;
    public final String postId;
    public final String replyId;
    public final String userName;
    public final String displayName;
    public final String displayInitials;
    public final String userStatus;
    public final String message;
    public final String image;
    public final String timeStamp;
    public final boolean isMe;

    public ChatPostData() {
        isMe = false;
        order = 0;
        locationId = "TBD";
        postId = "TDB";
        replyId = "TDB";
        userName = "TDB";
        displayName = "TDB";
        displayInitials = "ZZ";
        message = "No one seems to be here...";
        image = "";
        latitude = 0;
        longitude = 0;
        isSticker = 0;
        karmaScore = 0;
        replyCount = 0;
        timeStamp = "2099-08-09 18:55:05.883";
        userStatus = "";
        postScore = 0;
        myVote = 0;
        avatarType = 0;
        avatarColor = AppConstants.AVATAR_COLORS[0];
    }

    public ChatPostData(JSONObject data) throws JSONException {
        // Log.d(TAG, data.toString());
        locationId = data.getString(ChatData.Fields.LOCATION);
        postId = ChatData.getUsableUUID(data, "ID");
        order = data.getInt(ChatData.Fields.ORDER_ID);
        replyId = (!data.isNull(ChatData.Fields.REPLY_ID)) ? data.getString(ChatData.Fields.REPLY_ID) : "";
        userName = data.getString(ChatData.Fields.USERNAME);
        isMe = userName.equals(UserData.USERNAME);
        displayName = data.getString(ChatData.Fields.DISPLAY_NAME);
        displayInitials = data.getString(ChatData.Fields.DISPLAY_INITIALS);
        avatarType = data.getInt(ChatData.Fields.AVATAR_TYPE);
        avatarColor = data.getInt(ChatData.Fields.AVATAR_COLOR);
        message = data.getString(ChatData.Fields.MESSAGE);
        image = data.getString(ChatData.Fields.IMAGE);
        latitude = data.getDouble(ChatData.Fields.LATITUDE);
        longitude = data.getDouble(ChatData.Fields.LONGITUDE);
        if (!data.isNull(ChatData.Fields.STICKER)) {
            isSticker = data.getInt(ChatData.Fields.STICKER);
        } else {
            isSticker = 0;
        }

        karmaScore = data.getInt(ChatData.Fields.KARMA);
        replyCount = data.getInt(ChatData.Fields.REPLY_COUNT);

        if (!data.isNull(ChatData.Fields.CREATED_ON)) {
            timeStamp = data.getString(ChatData.Fields.CREATED_ON);
        } else if (!data.isNull(ChatData.Fields.LAST_UPDATE)) {
            timeStamp = data.getString(ChatData.Fields.LAST_UPDATE);
        } else {
            timeStamp = "2099-08-09 18:55:05.883";
        }

        if (!data.isNull(ChatData.Fields.DISPLAY_STATUS)) {
            userStatus = data.getString(ChatData.Fields.DISPLAY_STATUS);
        } else {
            userStatus = "Hi there!";
        }

        int totalScore = 0;
        int myvote = 0;
        if (!data.isNull(ChatData.Fields.REACTIONS)) {
            JSONObject content = new JSONObject(data.getString(ChatData.Fields.REACTIONS));
            Iterator<?> keys = content.keys();
            while(keys.hasNext() ) {
                String key = (String)keys.next();
                if ( content.get(key) instanceof JSONObject ) {
                    JSONObject xx = new JSONObject(content.get(key).toString());
                    if (key.equals(UserData.USERNAME)) {
                        myvote = xx.getInt("score");
                    }
                    totalScore = totalScore + xx.getInt("score");
                }
            }
        } else {
            totalScore = data.getInt(ChatData.Fields.SCORE);
        }
        postScore = totalScore;
        myVote = myvote;
    }

    public boolean isNearby() {
        return (AppConstants.LOCATION_ID != null && locationId.equals(AppConstants.LOCATION_ID));
    }

    @Override
    public int compareTo(@NonNull ChatPostData o) {
        return (this.order < o.order) ? 1 : -1;
    }
}
