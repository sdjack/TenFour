package com.steve-jackson-studios.tenfour.Login;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 3/1/2017.
 * LoginFragment
 */

public class LoginFragment extends DialogFragment {

    private EditText usernameField;
    private EditText passwordField;

    protected AuthActivity resolver;

    public static LoginFragment newInstance(AuthActivity appResolver) {

        LoginFragment instance = new LoginFragment();
        instance.setResolver(appResolver);

        return instance;
    }

    private void setResolver(AuthActivity appResolver) {
        this.resolver = appResolver;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.auth_login_layout, container, false);

        usernameField = (EditText) view.findViewById(R.id.login_username);
        passwordField = (EditText) view.findViewById(R.id.login_password);
        Button loginButton = (Button) view.findViewById(R.id.login_submit);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = usernameField.getText().toString();
                String passToken = passwordField.getText().toString();
                resolver.sendLoginRequest(userName, passToken, true);
            }
        });
        Button googleButton = (Button) view.findViewById(R.id.login_google);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolver.onSelectGoogleAuth();
            }
        });
        Button facebookButton = (Button) view.findViewById(R.id.login_facebook);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolver.onSelectFacebookAuth();
            }
        });
        Button twitterButton = (Button) view.findViewById(R.id.login_twitter);
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolver.onSelectTwitterAuth();
            }
        });
        Button switchButton = (Button) view.findViewById(R.id.login_to_signup);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolver.showSignUpForm();
            }
        });

        return view;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
