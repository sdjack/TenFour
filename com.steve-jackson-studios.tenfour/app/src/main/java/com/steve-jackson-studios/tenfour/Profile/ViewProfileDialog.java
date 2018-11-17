package com.steve-jackson-studios.tenfour.Profile;

import android.app.Dialog;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Data.ChatPostData;
import com.steve-jackson-studios.tenfour.Media.GlideApp;
import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 4/27/2017.
 * ViewProfileDialog
 */

public class ViewProfileDialog extends DialogFragment {

    private String userName;
    private String userItnitials;
    private String userStatus;
    private int userAvatarType = 0;
    private int userAvatarColor = AppConstants.AVATAR_COLORS[0];

    public void setUserInfo(ChatPostData userChatPostData) {
        this.userName = userChatPostData.userName;
        this.userItnitials = userChatPostData.displayInitials;
        this.userStatus = userChatPostData.userStatus;
        this.userAvatarType = userChatPostData.avatarType;
        this.userAvatarColor = userChatPostData.avatarColor;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_view_layout, container, false);

        TextView userField = (TextView) view.findViewById(R.id.user_profile_name);
        userField.setText(userName);

        TextView statusField = (TextView) view.findViewById(R.id.user_profile_status);
        userStatus = (!TextUtils.isEmpty(userStatus)) ? userStatus : getString(R.string.profile_status_default);
        statusField.setText(userStatus);

        TextView avatarText = (TextView) view.findViewById(R.id.user_profile_avatar_type0);
        ImageView avatarImage = (ImageView) view.findViewById(R.id.user_profile_avatar_type1);
        if (userAvatarType == 1) {
            avatarText.setVisibility(View.GONE);
            avatarImage.setVisibility(View.VISIBLE);
            String avatarPath = AppConstants.BLOB_USERAVATARS_URL + userName + ".png";
            Uri fileUri = Uri.parse(avatarPath);
            GlideApp.with(getActivity())
                    .load(fileUri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.icon_avatar_default)
                    .error(R.drawable.icon_avatar_default)
                    .fitCenter()
                    .into(avatarImage);
        } else {
            avatarImage.setVisibility(View.GONE);
            avatarText.setVisibility(View.VISIBLE);
            avatarText.setText(userItnitials);
            avatarText.getBackground().setColorFilter(userAvatarColor, PorterDuff.Mode.SRC_ATOP);
        }

        Button cancel = (Button)view.findViewById(R.id.profile_cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
