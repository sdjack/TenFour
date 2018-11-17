package com.steve-jackson-studios.tenfour.IO;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonSyntaxException;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by sjackson on 8/16/2017.
 * RestClient
 */

public class RestClient {

    private static final String TAG = RestClient.class.getName();
    private static final int ACTIVE_DELAY = 60000;

    private Context context;
    private ResponseListener callbackListener;
    private ScheduledExecutorService taskExecutor;
    private ApiResponseTask apiTask;
    private boolean PENDING_REQUEST = false;

    /**
     * Instantiates a new Api service.
     */
    public RestClient(Activity activity) {
        this.context = activity;
        this.callbackListener = (ResponseListener) activity;
        taskExecutor = Executors.newScheduledThreadPool(5);
    }

    public RestClient(Context c, ResponseListener listener) {
        this.context = c;
        this.callbackListener = listener;
        taskExecutor = Executors.newScheduledThreadPool(5);
    }

    public void destroy() {
        if (taskExecutor != null) {
            taskExecutor.shutdown();
        }
        if (apiTask != null) {
            apiTask.cancel(true);
        }
    }

    /**
     * Service request.
     */
    public void request(final String uri, final JSONObject json) {
        final String url = AppConstants.SERVICE_URL + uri;
        if (networkAvailable()) {
            if (!AppConstants.NETWORK_ONLINE) {
                AppConstants.NETWORK_ONLINE = true;
                Dispatch.triggerEvent(ObservedEvents.NETWORK_AVAILABLE);
            }
            if (!PENDING_REQUEST) {
                PENDING_REQUEST = true;
                //Log.d(TAG, "serviceRequest URL = " + url);
                //Log.d(TAG, "serviceRequest DATA = " + json.toString());

                JsonObjectRequest jsonRequest;
                if (json != null) {
                    jsonRequest = new JsonObjectRequest
                            (Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    apiTask = new ApiResponseTask(response);
                                    apiTask.execute((Void) null);
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                }
                            }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("Content-Type", "application/json");
                            return params;
                        }
                    };
                } else {
                    jsonRequest = new JsonObjectRequest
                            (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    apiTask = new ApiResponseTask(response);
                                    apiTask.execute((Void) null);
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                }
                            }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("Content-Type", "application/x-www-form-urlencoded");
                            return params;
                        }
                    };
                }

                Volley.newRequestQueue(context).add(jsonRequest);
            } else {
                taskExecutor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        request(uri, json);
                    }
                }, ACTIVE_DELAY, TimeUnit.MICROSECONDS);
            }
        } else if (AppConstants.NETWORK_ONLINE) {
            AppConstants.NETWORK_ONLINE = false;
            Dispatch.triggerEvent(ObservedEvents.NETWORK_UNAVAILABLE);
            try {
                taskExecutor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        request(uri, json);
                    }
                }, ACTIVE_DELAY, TimeUnit.MICROSECONDS);
            } catch (IllegalStateException ex) {
                Log.e(TAG, "IllegalStateException", ex);
            }
        }
    }

    private boolean networkAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * The listener interface.
     */
    public interface ResponseListener {
        void onServiceResponse(int responseId, JSONArray responseData);
        void onErrorResponse();
    }

    /**
     * asynchronous task to fetch feed data
     */
    private class ApiResponseTask extends AsyncTask<Void, Void, Boolean> {

        private JSONObject resultObject = null;

        ApiResponseTask(JSONObject response) {
            resultObject = response;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            PENDING_REQUEST = false;

            if (resultObject != null) {
                //Log.d(TAG, resultObject.toString());
                try {
                    if (!resultObject.isNull("context") && !resultObject.isNull("data")) {
                        final JSONArray jsonData = resultObject.getJSONArray("data");
                        final String serviceUri = resultObject.getString("context");
                        //Log.d(TAG, "RESPONSE " + serviceUri);
                        if (null != AppConstants.SERVICE_MAP.get(serviceUri)) {
                            final int serviceEvent = AppConstants.SERVICE_MAP.get(serviceUri);
                            callbackListener.onServiceResponse(serviceEvent, jsonData);
                        }
                    } else {
                        callbackListener.onErrorResponse();
                    }
                } catch (IllegalStateException | JsonSyntaxException ex) {
                    Log.e(TAG, "IllegalStateException", ex);
                } catch (JSONException e) {
                    callbackListener.onErrorResponse();
                    e.printStackTrace();
                }
            } else {
                callbackListener.onErrorResponse();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            apiTask = null;
            PENDING_REQUEST = false;
            callbackListener.onErrorResponse();
        }

        @Override
        protected void onCancelled() {
            apiTask = null;
            PENDING_REQUEST = false;
            callbackListener.onErrorResponse();
        }
    }
}
