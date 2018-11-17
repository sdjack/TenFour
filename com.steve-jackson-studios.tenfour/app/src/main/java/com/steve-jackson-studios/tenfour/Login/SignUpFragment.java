package com.steve-jackson-studios.tenfour.Login;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;
import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 3/17/2017.
 * SignUpFragment
 */

public class SignUpFragment extends DialogFragment {

    private EditText usernameField;
    private EditText passwordField;
    private EditText firstnameField;
    private EditText lastnameField;
    private EditText emailField;
    private EditText phoneField;

    protected AuthActivity resolver;

    public static SignUpFragment newInstance(AuthActivity appResolver) {

        SignUpFragment instance = new SignUpFragment();
        instance.setResolver(appResolver);

        return instance;
    }

    private void setResolver(AuthActivity appResolver) {
        this.resolver = appResolver;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.auth_signup_layout, container, false);

        usernameField = (EditText) view.findViewById(R.id.signup_username);
        passwordField = (EditText) view.findViewById(R.id.signup_password);
        firstnameField = (EditText) view.findViewById(R.id.signup_firstname);
        lastnameField = (EditText) view.findViewById(R.id.signup_lastname);
        emailField = (EditText) view.findViewById(R.id.signup_email);
        phoneField = (EditText) view.findViewById(R.id.signup_phone);
        Button loginButton = (Button) view.findViewById(R.id.signup_submit);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = usernameField.getText().toString();
                String passToken = passwordField.getText().toString();
                String firstName = firstnameField.getText().toString();
                String lastName = lastnameField.getText().toString();
                String email = emailField.getText().toString();
                String phone = phoneField.getText().toString();
                int google = (AppConstants.ATTEMPTED_LOGIN_TYPE == AppConstants.REQUEST_GOOGLE_CREATE) ? 1 : 0;
                int facebook = (AppConstants.ATTEMPTED_LOGIN_TYPE == AppConstants.REQUEST_FACEBOOK_CREATE) ? 1 : 0;
                int twitter = (AppConstants.ATTEMPTED_LOGIN_TYPE == AppConstants.REQUEST_TWITTER_CREATE) ? 1 : 0;
                resolver.sendSignupRequest(userName, passToken, email, firstName, lastName, phone, google, facebook, twitter);
            }
        });
        Button googleButton = (Button) view.findViewById(R.id.signup_google);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolver.onCreateGoogleAuth();
            }
        });
        Button facebookButton = (Button) view.findViewById(R.id.signup_facebook);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolver.onCreateFacebookAuth();
            }
        });
        Button twitterButton = (Button) view.findViewById(R.id.signup_twitter);
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolver.onCreateTwitterAuth();
            }
        });
        Button switchButton = (Button) view.findViewById(R.id.signup_to_login);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolver.showLoginForm();
            }
        });

        if (!AppConstants.STATUS_MESSAGE.equals("")) {
            Dispatch.triggerEvent(ObservedEvents.NOTIFY_ERROR_MESSAGE, AppConstants.STATUS_MESSAGE);
        }

        return view;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
