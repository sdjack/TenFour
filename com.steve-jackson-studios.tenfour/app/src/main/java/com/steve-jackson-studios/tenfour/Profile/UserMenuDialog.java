package com.steve-jackson-studios.tenfour.Profile;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.AppResolver;
import com.steve-jackson-studios.tenfour.Misc.ResolverDialogFragment;
import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 2/24/2017.
 * UserMenuDialog
 */

public class UserMenuDialog extends ResolverDialogFragment {

    public static UserMenuDialog newInstance(AppResolver appResolver) {

        UserMenuDialog instance = new UserMenuDialog();
        instance.setResolver(appResolver);

        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_menu_layout, container, false);
        LinearLayout mpc = (LinearLayout) view.findViewById(R.id.user_options_container);
        mpc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        TextView title = (TextView) view.findViewById(R.id.user_options_title);
        title.setText(AppConstants.ACTIVE_MENU_TITLE);
        TextView info = (TextView) view.findViewById(R.id.user_options_info);
        info.setText(AppConstants.ACTIVE_MENU_CONTENT);
        TextView link = (TextView) view.findViewById(R.id.user_options_link);
        link.setText(AppConstants.ACTIVE_MENU_LINK);

        if (AppConstants.ACTIVE_MENU_LAYOUT != 0) {
            LinearLayout uml = (LinearLayout) view.findViewById(R.id.user_options_content);
            View contentLayout = inflater.inflate(AppConstants.ACTIVE_MENU_LAYOUT, container, false);
            uml.addView(contentLayout);
        }

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
