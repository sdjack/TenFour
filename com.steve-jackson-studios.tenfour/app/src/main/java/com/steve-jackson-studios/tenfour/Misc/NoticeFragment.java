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
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 4/19/2017.
 * NoticeFragment
 */

public class NoticeFragment extends DialogFragment {

    private final Handler handler = new Handler();
    private RelativeLayout header;
    private RelativeLayout body;
    private RelativeLayout footer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notice_layout, container, false);
        this.header = (RelativeLayout) view.findViewById(R.id.notice_header);
        this.body = (RelativeLayout) view.findViewById(R.id.notice_body);
        this.footer = (RelativeLayout) view.findViewById(R.id.notice_footer);

        FrameLayout frame = (FrameLayout) view.findViewById(R.id.notice_frame);
        frame.setOnClickListener(new View.OnClickListener() {
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
