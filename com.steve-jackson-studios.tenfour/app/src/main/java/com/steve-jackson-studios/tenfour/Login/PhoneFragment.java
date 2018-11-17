package com.steve-jackson-studios.tenfour.Login;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 3/20/2017.
 * PhoneFragment
 */

public class PhoneFragment extends DialogFragment {

    private EditText phoneField;

    protected AuthActivity resolver;

    public static PhoneFragment newInstance(AuthActivity appResolver) {

        PhoneFragment instance = new PhoneFragment();
        instance.setResolver(appResolver);

        return instance;
    }

    private void setResolver(AuthActivity appResolver) {
        this.resolver = appResolver;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.auth_phone_layout, container, false);
        phoneField = (EditText) view.findViewById(R.id.phone_input_field);
        Button loginButton = (Button) view.findViewById(R.id.phone_input_submit);
        loginButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        String phone = phoneField.getText().toString();
                        resolver.sendSignupRequest(phone);
                        dismiss();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return true;
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
