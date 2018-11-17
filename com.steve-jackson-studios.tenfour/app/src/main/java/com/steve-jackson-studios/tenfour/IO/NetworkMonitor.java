package com.steve-jackson-studios.tenfour.IO;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Created by sjackson on 5/2/2017.
 * NetworkMonitor
 */

public class NetworkMonitor {

    private static final String TAG = "NetworkMonitor";

    // Message to notify the network request timout handler that too much time has passed.
    private static final int MESSAGE_CONNECTIVITY_TIMEOUT = 1;

    // How long the app should wait trying to connect to a sufficient high-bandwidth network before
    // asking the user to add a new Wi-Fi network.
    private static final long NETWORK_CONNECTIVITY_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10);

    // The minimum network bandwidth required by the app for high-bandwidth operations.
    private static final int MIN_NETWORK_BANDWIDTH_KBPS = 10000;
    // Tags added to the button in the UI to detect what operation the user has requested.
    // These are required since the app reuses the button for different states of the app/UI.
    // See onButtonClick() for how these tags are used.
    static final String TAG_REQUEST_NETWORK = "REQUEST_NETWORK";
    static final String TAG_RELEASE_NETWORK = "RELEASE_NETWORK";
    static final String TAG_ADD_WIFI = "ADD_WIFI";

    private CallbackListener callbackListener;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private static Handler networkChangeHandler;

    /**
     * @param callbackListener the listener
     */
    public void setCallbackListener(CallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }

    public NetworkMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        networkChangeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_CONNECTIVITY_TIMEOUT:
                        callbackListener.onNetworkUnavailable();
                        unregisterNetworkCallback();
                        break;
                }
            }
        };
    }

    public void stop() {
        releaseHighBandwidthNetwork();
    }

    public void resume() {
        if (isNetworkHighBandwidth()) {
            callbackListener.onNetworkAvailable();
        } else {
            callbackListener.onNetworkUnavailable();
        }
    }

    private void unregisterNetworkCallback() {
        if (networkCallback != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            }
            networkCallback = null;
        }
    }

    // Determine if there is a high-bandwidth network exists. Checks both the active
    // and bound networks. Returns false if no network is available (low or high-bandwidth).
    @TargetApi(Build.VERSION_CODES.M)
    private boolean isNetworkHighBandwidth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getBoundNetworkForProcess();
            network = network == null ? connectivityManager.getActiveNetwork() : network;
            if (network == null) {
                return false;
            }

            // requires android.permission.ACCESS_NETWORK_STATE
            int bandwidth = connectivityManager
                    .getNetworkCapabilities(network).getLinkDownstreamBandwidthKbps();

            return bandwidth >= MIN_NETWORK_BANDWIDTH_KBPS;

        }
        return true;
    }

    private void requestHighBandwidthNetwork() {
        // Before requesting a high-bandwidth network, ensure prior requests are invalidated.
        unregisterNetworkCallback();

        Log.d(TAG, "Requesting high-bandwidth network");

        // Requesting an unmetered network may prevent you from connecting to the cellular
        // network on the user's watch or phone; however, unless you explicitly ask for permission
        // to a access the user's cellular network, you should request an unmetered network.
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(final Network network) {
                networkChangeHandler.removeMessages(MESSAGE_CONNECTIVITY_TIMEOUT);
                if (!connectivityManager.bindProcessToNetwork(network)) {
                    callbackListener.onNetworkUnavailable();
                } else {
                    callbackListener.onNetworkAvailable();
                }
            }

            @Override
            public void onCapabilitiesChanged(Network network,
                                              NetworkCapabilities networkCapabilities) {
                Log.d(TAG, "Network capabilities changed");
            }

            @Override
            public void onLost(Network network) {
                callbackListener.onNetworkUnavailable();
            }
        };

        // requires android.permission.CHANGE_NETWORK_STATE
        connectivityManager.requestNetwork(request, networkCallback);

        networkChangeHandler.sendMessageDelayed(
                networkChangeHandler.obtainMessage(MESSAGE_CONNECTIVITY_TIMEOUT),
                NETWORK_CONNECTIVITY_TIMEOUT_MS);
    }

    private void releaseHighBandwidthNetwork() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.bindProcessToNetwork(null);
            unregisterNetworkCallback();
        }
    }

    /**
     * Interface definition for callbacks
     */
    public interface CallbackListener {
        void onNetworkAvailable();
        void onNetworkUnavailable();
    }
}
