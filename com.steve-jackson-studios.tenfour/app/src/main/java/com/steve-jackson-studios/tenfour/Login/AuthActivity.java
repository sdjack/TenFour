package com.steve-jackson-studios.tenfour.Login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonSyntaxException;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.IO.RestClient;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;
import com.steve-jackson-studios.tenfour.R;
import com.steve-jackson-studios.tenfour.Data.UserData;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import io.fabric.sdk.android.Fabric;

/**
 * Created by sjackson on 8/16/2017.
 * AuthActivity
 */

public class AuthActivity extends FragmentActivity implements Dispatch.Listener, RestClient.ResponseListener {

    private static final String TAG = AuthActivity.class.getName();

    private RestClient httpClient;

    private LoginFragment loginFragment;
    private SignUpFragment signUpFragment;
    private PhoneFragment phoneFragment;

    protected GoogleSignInClient googleClient;
    protected GoogleApiClient googleApiClient;
    private CallbackManager facebookClient;
    private TwitterAuthClient twitterClient;
    private ImageView networkIndicator;
    private ImageView servicesIndicator;
    private TextView errorText;
    private SharedPreferences preferences;

    public final String[] signUpInfo = new String[5];
    public final int[] signUpFlags = new int[3];

    private String errorMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_layout);

        ImageView logo = (ImageView) findViewById(R.id.logo_anim);
        AnimationDrawable logoAnimation = (AnimationDrawable) logo.getBackground();
        logoAnimation.start();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        googleClient = GoogleSignIn.getClient(this, gso);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleApiClient.connect();

        facebookClient = CallbackManager.Factory.create();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(AppConstants.TWITTER_KEY, AppConstants.TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        twitterClient = new TwitterAuthClient();

        errorText = (TextView) findViewById(R.id.error_text);
        networkIndicator = (ImageView) findViewById(R.id.network_indicator);
        servicesIndicator = (ImageView) findViewById(R.id.services_indicator);

        networkIndicator.setVisibility(View.GONE);
        servicesIndicator.setVisibility(View.GONE);

        httpClient = new RestClient(this);
        preferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        Dispatch.register(this);

        getSavedUserData();
        //showLoginForm();
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, "onActivityResult REQUEST: " + requestCode + ",  RESULT: " + resultCode);
        switch (requestCode) {
            case AppConstants.REQUEST_GOOGLE_LOGIN:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleGoogleSignInResult(task);

                break;

            case AppConstants.REQUEST_FACEBOOK_LOGIN:
                break;

            case AppConstants.REQUEST_TWITTER_LOGIN:
                break;

            case AppConstants.REQUEST_GOOGLE_CREATE:
                GoogleSignInResult createResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (createResult.isSuccess()) {
                    GoogleSignInAccount acct = createResult.getSignInAccount();
                    if (acct != null) {
                        AppConstants.ATTEMPTED_LOGIN_TYPE = AppConstants.REQUEST_GOOGLE_CREATE;
                        String userName = acct.getDisplayName();
                        userName = userName != null ? userName.replace(" ", "") : null;
                        signUpInfo[0] = userName;
                        signUpInfo[1] = acct.getId();
                        signUpInfo[2] = acct.getEmail();
                        signUpInfo[3] = acct.getGivenName();
                        signUpInfo[4] = acct.getFamilyName();

                        signUpFlags[0] = 1;
                        signUpFlags[1] = 0;
                        signUpFlags[2] = 0;
                        showPhoneForm();
                    } else {
                        Dispatch.triggerEvent(ObservedEvents.USER_LOGIN_FAILURE);
                    }
                } else {
                    Dispatch.triggerEvent(ObservedEvents.USER_LOGIN_FAILURE);
                }
                break;

            case AppConstants.REQUEST_FACEBOOK_CREATE:
                break;

            case AppConstants.REQUEST_TWITTER_CREATE:
                break;

            default:
                if ((requestCode == AppConstants.REQUEST_CODE_RECOVER_FROM_AUTH_ERROR ||
                        requestCode == AppConstants.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
                        && resultCode == RESULT_OK) {
                    Dispatch.triggerEvent(ObservedEvents.USER_LOGIN_FAILURE);
                }
                break;
        }
        facebookClient.onActivityResult(requestCode, resultCode, data);
        twitterClient.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        httpClient.destroy();
        Dispatch.unregister(this);
        super.onDestroy();
    }

    public boolean networkAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void sendLoginRequest() {
        if (!AppConstants.LOGGED_IN) {
            if (!AppConstants.LOGIN_PENDING) {
                if (AppConstants.hasUsableValue(UserData.USERNAME) && (AppConstants.hasUsableValue(UserData.PASSWORD) || AppConstants.hasUsableValue(UserData.AUTH_TOKEN))) {
                    if (UserData.AUTH_TYPE > 0 || (!AppConstants.hasUsableValue(UserData.PASSWORD) && AppConstants.hasUsableValue(UserData.AUTH_TOKEN))) {
                        sendTokenLoginRequest(UserData.USERNAME, UserData.AUTH_TOKEN, false);
                    } else {
                        sendLoginRequest(UserData.USERNAME, UserData.PASSWORD, false);
                    }
                } else {
                    Dispatch.triggerEvent(ObservedEvents.USER_LOGIN_FAILURE);
                }
            }
        } else {
            Dispatch.triggerEvent(ObservedEvents.USER_LOGIN_SUCCESSFUL);
        }
    }

    public void sendLoginRequest(final String userName, final String passToken, boolean saveValue) {
        if (!AppConstants.LOGGED_IN) {
            AppConstants.LOGIN_PENDING = true;
            if (saveValue) {
                UserData.PASSWORD = passToken;
            }
            JSONObject row = new JSONObject();
            try {
                row.put("userName", userName.replace(" ", ""));
                row.put("passWord", passToken);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpClient.request(AppConstants.SERVICE_LOGIN, row);
            hideKeyboard();
        }
    }

    public void sendTokenLoginRequest(final String userName, final String authToken, boolean saveValue) {
        if (!AppConstants.LOGGED_IN) {
            AppConstants.LOGIN_PENDING = true;
            if (saveValue) {
                UserData.AUTH_TOKEN = authToken;
            }
            JSONObject row = new JSONObject();
            try {
                row.put("userName", userName.replace(" ", ""));
                row.put("token", authToken);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpClient.request(AppConstants.SERVICE_TOKEN_LOGIN, row);
            hideKeyboard();
        }
    }

    public void sendSignupRequest(String un, String pw, String em, String ph, String fn, String ln, int gf, int ff, int tf) {
        signUpInfo[0] = un;
        signUpInfo[1] = pw;
        signUpInfo[2] = em;
        signUpInfo[3] = fn;
        signUpInfo[4] = ln;
        signUpFlags[0] = gf;
        signUpFlags[1] = ff;
        signUpFlags[2] = tf;
        sendSignupRequest(ph);
    }

    public void sendSignupRequest(String phone) {
        Dispatch.triggerEvent(ObservedEvents.REQUEST_CLOSE_KEYBOARD);
        if (!AppConstants.LOGGED_IN) {
            AppConstants.LOGIN_PENDING = true;
            String endpoint = AppConstants.SERVICE_SIGNUP;
            JSONObject row = new JSONObject();
            try {
                row.put("userName", signUpInfo[0].replace(" ", ""));
                if ((signUpFlags[0] + signUpFlags[1] + signUpFlags[2]) > 0) {
                    row.put("token", signUpInfo[1]);
                    endpoint = AppConstants.SERVICE_TOKEN_SIGNUP;
                } else {
                    row.put("passWord", signUpInfo[1]);
                }
                row.put("firstName", signUpInfo[3]);
                row.put("lastName", signUpInfo[4]);
                row.put("email", signUpInfo[2]);
                row.put("phone", phone);
                row.put("isGoogle", signUpFlags[0]);
                row.put("isFacebook", signUpFlags[1]);
                row.put("isTwitter", signUpFlags[2]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpClient.request(endpoint, row);
        }
    }

    public void saveUserData(JSONObject data) {
        try {
            UserData.saveUserData(data);
            UserData.setAccountType(data.getInt("GOOGLE"), data.getInt("FACEBOOK"), data.getInt("TWITTER"));
            if (UserData.AUTH_TYPE > 0) {
                UserData.PASSWORD = data.getString("AUTH_TOKEN");
            } else {
                UserData.AUTH_TOKEN = data.getString("AUTH_TOKEN");
            }
        } catch (IllegalStateException | JsonSyntaxException | JSONException ex) {
            ex.printStackTrace();
        }
        //Log.d(TAG, "PASSTOKEN TEST >>> saveUserData >>> AUTH_TOKEN = " + UserData.AUTH_TOKEN);
        AppConstants.setLoggedInState();
    }

    public synchronized void getSavedUserData() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String userName = preferences.getString("UserName", null);
        UserData.USERNAME = (userName != null) ? userName.replace(" ", "") : null;
        UserData.ID = preferences.getString("UserID", null);
        UserData.PASSWORD = preferences.getString("PassWord", null);
        UserData.AUTH_TOKEN = preferences.getString("AuthToken", null);
        UserData.EMAIL = preferences.getString("UserEmail", null);
        UserData.PHONE = preferences.getString("UserPhone", null);
        UserData.FIRST_NAME = preferences.getString("UserFirstName", "");
        UserData.LAST_NAME = preferences.getString("UserLastName", "");
        UserData.AVATAR = preferences.getString("UserAvatarUrl", AppConstants.DEFAULT_AVATAR);
        UserData.AVATAR_TYPE = preferences.getInt("UserAvatarType", 0);
        UserData.AVATAR_COLOR = preferences.getInt("UserAvatarColor", AppConstants.AVATAR_COLORS[0]);
        UserData.AVATAR_URI = Uri.parse(UserData.AVATAR);
        UserData.STATUS = preferences.getString("UserStatus", "");
        UserData.STATUS = (TextUtils.isEmpty(UserData.STATUS)) ? "Click To Set Status" : UserData.STATUS;
        UserData.HOMETOWN = preferences.getString("UserHomeTown", "");
        UserData.ACTIVITY = preferences.getString("UserActivity", "");
        UserData.CONTACTS = preferences.getString("UserContacts", "");
        UserData.TROPHIES = preferences.getString("UserTrophies", "");
        UserData.HEATMAP_ENABLED = preferences.getBoolean("FilterShowHeatmap", true);
        UserData.FRIENDMAP_ENABLED = preferences.getBoolean("FilterShowFriendmap", true);
        UserData.VISIBILITY_ENABLED = preferences.getBoolean("FilterAllowVisibility", true);
        UserData.STICKERS_ENABLED = preferences.getBoolean("FilterShowStickers", true);
        int authType = preferences.getInt("AuthType", 0);
        UserData.AUTH_TYPE = (authType == 0 && UserData.PASSWORD == null && UserData.AUTH_TOKEN != null) ? 1 : authType;
        UserData.KARMA = preferences.getInt("UserKarma", 0);
        if (UserData.FIRST_NAME.length() > 0 && UserData.LAST_NAME.length() > 0) {
            UserData.INITIALS = UserData.FIRST_NAME.substring(0,1) + UserData.LAST_NAME.substring(0,1);
        }
        AppConstants.GPS_LATITUDE = preferences.getFloat("previousLatitude", 0);
        AppConstants.GPS_LONGITUDE = preferences.getFloat("previousLongitude", 0);
        AppConstants.MAP_ACTIVE_LOCATION_ID = AppConstants.LOCATION_ID;
        AppConstants.MAP_ACTIVE_EVENT_ID = AppConstants.MAP_ACTIVE_LOCATION_ID;
        Log.d(TAG, "PASSTOKEN TEST >>> getSavedUserData >>> PASSWORD = " + UserData.PASSWORD);
        Log.d(TAG, "PASSTOKEN TEST >>> getSavedUserData >>> AUTH_TOKEN = " + UserData.AUTH_TOKEN);
        Log.d(TAG, "PASSTOKEN TEST >>> getSavedUserData >>> AUTH_TYPE = " + UserData.AUTH_TYPE);
        sendLoginRequest();
    }

    public synchronized void clearAuthenticatedData() {
        //Log.d(TAG, "PASSTOKEN TEST >>> clearAuthenticatedData");
        AppConstants.LOGGED_IN = false;
        UserData.resetUserData();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("UserID", null);
        editor.putString("LocationID", null);
        editor.putString("SubDivisionID", null);
        editor.putString("UserName", null);
        editor.putString("PassWord", null);
        editor.putString("AuthToken", null);
        editor.putString("UserFirstName", null);
        editor.putString("UserLastName", null);
        editor.putString("UserEmail", null);
        editor.putString("UserPhone", null);
        editor.putString("UserAvatarUrl", null);
        editor.putInt("UserAvatarType", 0);
        editor.putInt("UserAvatarColor", AppConstants.AVATAR_COLORS[0]);
        editor.putString("UserStatus", null);
        editor.putString("UserActivity", null);
        editor.putString("UserContacts", null);
        editor.putString("UserTrophies", null);
        editor.putInt("AuthType", 0);
        editor.putInt("UserKarma", 0);
        editor.putBoolean("UserIsLoggedIn", false);
        editor.apply();
    }

    public void showLoginForm() {
        runOnUiThread(new Runnable() {
            @SuppressLint("WrongConstant")
            @Override
            public void run() {
                if (errorMessage.length() < 1) {
                    errorText.setText("");
                    errorText.setVisibility(View.GONE);
                }
                if (signUpFragment != null) {
                    signUpFragment.dismiss();
                }
                loginFragment = LoginFragment.newInstance(AuthActivity.this);
                getFragmentManager().beginTransaction()
                        .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .add(R.id.content_container, loginFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            }
        });
    }

    public void showSignUpForm() {
        runOnUiThread(new Runnable() {
            @SuppressLint("WrongConstant")
            @Override
            public void run() {
                if (errorMessage.length() < 1) {
                    errorText.setText("");
                    errorText.setVisibility(View.GONE);
                }
                if (loginFragment != null) {
                    loginFragment.dismiss();
                }
                signUpFragment = SignUpFragment.newInstance(AuthActivity.this);
                getFragmentManager().beginTransaction()
                        .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .add(R.id.content_container, signUpFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            }
        });
    }

    public void showPhoneForm() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errorMessage.length() < 1) {
                    errorText.setText("");
                    errorText.setVisibility(View.GONE);
                }
                if (loginFragment != null) {
                    loginFragment.dismiss();
                }
                if (signUpFragment != null) {
                    signUpFragment.dismiss();
                }
                phoneFragment = PhoneFragment.newInstance(AuthActivity.this);
                getFragmentManager().beginTransaction()
                        .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .add(R.id.content_container, phoneFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            }
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Log.d(TAG, "onActivityResult REQUEST_GOOGLE_LOGIN acct: " + account.toString());
            if (account != null) {
                AppConstants.ATTEMPTED_LOGIN_TYPE = AppConstants.REQUEST_GOOGLE_LOGIN;
                String userName = account.getDisplayName();
                userName = userName != null ? userName.replace(" ", "") : null;
                signUpInfo[0] = userName;
                signUpInfo[1] = account.getId();
                signUpInfo[2] = account.getEmail();
                signUpInfo[3] = account.getGivenName();
                signUpInfo[4] = account.getFamilyName();

                signUpFlags[0] = 1;
                signUpFlags[1] = 0;
                signUpFlags[2] = 0;
                Log.d(TAG, "onActivityResult REQUEST_GOOGLE_LOGIN signUpInfo: " + signUpInfo.toString());
                if (signUpInfo[0] != null && signUpInfo[1] != null) {
                    sendTokenLoginRequest(signUpInfo[0], signUpInfo[1], true);
                } else {
                    Dispatch.triggerEvent(ObservedEvents.USER_LOGIN_FAILURE);
                }
            } else {
                Dispatch.triggerEvent(ObservedEvents.USER_LOGIN_FAILURE);
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Dispatch.triggerEvent(ObservedEvents.USER_LOGIN_FAILURE);
        }
    }

    public void onSelectGoogleAuth() {
        if (networkAvailable()) {
            googleClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent signInIntent = googleClient.getSignInIntent();
                            startActivityForResult(signInIntent, AppConstants.REQUEST_GOOGLE_LOGIN);
                        }
                    });
        } else {
            Dispatch.triggerEvent(ObservedEvents.NETWORK_UNAVAILABLE);
            Toast.makeText(this, R.string.not_online, Toast.LENGTH_LONG).show();
        }
    }

    public void onCreateGoogleAuth() {
        if (networkAvailable()) {
            googleClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent signInIntent = googleClient.getSignInIntent();
                            startActivityForResult(signInIntent, AppConstants.REQUEST_GOOGLE_CREATE);
                        }
                    });
        } else {
            Dispatch.triggerEvent(ObservedEvents.NETWORK_UNAVAILABLE);
            Toast.makeText(this, R.string.not_online, Toast.LENGTH_LONG).show();
        }
//        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
//        int code = api.isGooglePlayServicesAvailable(this);
//        if (code != ConnectionResult.SUCCESS) {
//            if(api.isUserResolvableError(code)) {
//                api.getErrorDialog(this, code, AppConstants.REQUEST_PLAY_SERVICES_RESOLUTION).show();
//
//            } else {
//                String str = GoogleApiAvailability.getInstance().getErrorString(code);
//                Toast.makeText(this, str, Toast.LENGTH_LONG).show();
//            }
//        }
    }

    public void onSelectFacebookAuth() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_photos", "email", "public_profile", "user_posts", "AccessToken"));
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));
        LoginManager.getInstance().registerCallback(facebookClient,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        loginResult.getAccessToken();

                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
    }

    public void onCreateFacebookAuth() {
        Toast.makeText(this, "Facebook Login Unavailable", Toast.LENGTH_SHORT).show();
    }

    public void onSelectTwitterAuth() {
        twitterClient.authorize(this, new Callback<TwitterSession>() {
            @Override
            public void success(final Result<TwitterSession> result) {
                final TwitterSession sessionData = result.data;
                // Do something with the returned TwitterSession (contains the user token and secret)

            }

            @Override
            public void failure(final TwitterException e) {
                // Do something on fail
            }
        });
    }

    public void onCreateTwitterAuth() {
        Toast.makeText(this, "Twitter Login Unavailable", Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    public void setErrorText(final String message) {
        errorMessage = message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                errorText.setText(errorMessage);
                errorText.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onTransportNotification(int eventID) {
        switch (eventID) {
            case ObservedEvents.USER_LOGIN_SUCCESSFUL:
                Intent intent = new Intent();
                setResult(AppConstants.AUTHACTIVITY_RESULT, intent);
                finish();
                break;
            case ObservedEvents.USER_LOGIN_FAILURE:
                clearAuthenticatedData();
                AppConstants.ATTEMPTED_LOGIN_TYPE = 0;
                switch (AppConstants.AUTH_STATUS_CODE) {
                    case 2:
                        showSignUpForm();
                        break;
                    case 3:
                        showSignUpForm();
                        break;
                    case 4:
                        showSignUpForm();
                        break;
                    case 5:
                        showSignUpForm();
                        break;
                    default:
                        showLoginForm();
                        break;
                }
                break;
        }
    }

    private void autoSignUp(int eventID) {
        switch (eventID) {
            case AppConstants.REQUEST_GOOGLE_LOGIN:
                showPhoneForm();
                break;
            case AppConstants.REQUEST_FACEBOOK_LOGIN:
                break;
            case AppConstants.REQUEST_TWITTER_LOGIN:
                break;
        }
    }

    @Override
    public void onServiceResponse(int responseId, JSONArray responseData) {
        if (!AppConstants.SERVICES_ONLINE) {
            AppConstants.SERVICES_ONLINE = true;
            Dispatch.triggerEvent(ObservedEvents.SERVICES_AVAILABLE);
        }
        //Log.d(TAG, "RESPONSE_ID: "+responseId);
        switch (responseId) {
            case AppConstants.EVENT_SERVICE_LOGIN:
                AppConstants.LOGIN_PENDING = false;
                AppConstants.AUTH_STATUS_CODE = 1;
                errorMessage = "";
                try {
                    //Log.d(TAG, responseData.toString());
                    if (responseData.length() > 0) {
                        JSONObject row = (JSONObject) responseData.get(0);
                        if (!row.isNull("STATUS_CODE")) {
                            AppConstants.AUTH_STATUS_CODE = row.getInt("STATUS_CODE");
                            if (AppConstants.AUTH_STATUS_CODE == 0) {
                                saveUserData(row);
                                return;
                            } else {
                                if (AppConstants.AUTH_STATUS_CODE == 2 && AppConstants.ATTEMPTED_LOGIN_TYPE != 0) {
                                    autoSignUp(AppConstants.ATTEMPTED_LOGIN_TYPE);
                                } else {
                                    setErrorText(row.getString("STATUS_MESSAGE"));
                                    Dispatch.triggerEvent(ObservedEvents.USER_LOGIN_FAILURE);
                                }
                                return;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                AppConstants.setLoggedInState();
                break;
            case AppConstants.EVENT_SERVICE_SIGNUP:
                AppConstants.LOGIN_PENDING = false;
                AppConstants.AUTH_STATUS_CODE = 1;
                errorMessage = "";
                try {
                    //Log.d(TAG, data.toString());
                    if (responseData.length() > 0) {
                        JSONObject row = (JSONObject) responseData.get(0);
                        if (!row.isNull("STATUS_CODE")) {
                            AppConstants.AUTH_STATUS_CODE = row.getInt("STATUS_CODE");
                            if (AppConstants.AUTH_STATUS_CODE == 0) {
                                saveUserData(row);
                                //CloudMedia.copyAvatar(URI.create(AppConstants.DEFAULT_AVATAR), UserData.USERNAME + ".png");
                                return;
                            } else {
                                setErrorText(row.getString("STATUS_MESSAGE"));
                                Dispatch.triggerEvent(ObservedEvents.USER_LOGIN_FAILURE);
                                return;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                AppConstants.setLoggedInState();
                break;
        }
    }

    @Override
    public void onErrorResponse() {

    }
}
