package com.steve-jackson-studios.tenfour.Misc;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 7/13/2017.
 * DebuggingFragment
 */

public class DebuggingFragment extends DialogFragment {

    private final Handler handler = new Handler();
    private TextView debugInboundGPS;
    private TextView debugSavedGPS;
    private TextView debugID;
    private TextView debugPreviewID;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.debugger_layout, container, false);

        debugInboundGPS = (TextView) view.findViewById(R.id.debug_incoming_gps);
        debugSavedGPS = (TextView) view.findViewById(R.id.debug_saved_gps);
        debugID = (TextView) view.findViewById(R.id.debug_id);
        debugPreviewID = (TextView) view.findViewById(R.id.debug_previewid);
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

    public void updateValues() {
        String outbound = "" + AppConstants.GPS_LATITUDE + "  -  " + AppConstants.GPS_LONGITUDE + "";
        debugSavedGPS.setText(outbound);
        debugID.setText(AppConstants.LOCATION_ID);
        debugPreviewID.setText(AppConstants.MAP_ACTIVE_LOCATION_ID);
    }

    public void updateValues(float latitude, float longitude) {
        String inbound = "" + latitude + "  -  " + longitude + "";
        String outbound = "" + AppConstants.GPS_LATITUDE + "  -  " + AppConstants.GPS_LONGITUDE + "";
        debugInboundGPS.setText(inbound);
        debugSavedGPS.setText(outbound);
        debugID.setText(AppConstants.LOCATION_ID);
        debugPreviewID.setText(AppConstants.MAP_ACTIVE_LOCATION_ID);
    }
}