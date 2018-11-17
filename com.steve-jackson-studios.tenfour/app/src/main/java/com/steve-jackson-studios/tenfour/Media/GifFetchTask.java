package com.steve-jackson-studios.tenfour.Media;

import android.os.AsyncTask;
import android.util.Log;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by sjackson on 1/27/2017.
 * GifFetchTask
 */

public class GifFetchTask extends AsyncTask<Void, Void, Void> {

    private static final String API_URL = "http://api.giphy.com/";
    private static final String API_URI = "v1/gifs/";
    private static final String API_KEY = "dc6zaTOxFJmzC";
    private GifFragment fragment;
    private String method = "trending";
    private String search = "";

    public GifFetchTask(final GifFragment fragment) {
        this.fragment = fragment;
        this.method = "trending";
    }

    public GifFetchTask(final GifFragment fragment, final String searchContent) {
        this.fragment = fragment;
        this.method = "search";
        try {
            this.search = "q=" + URLEncoder.encode(searchContent, "UTF-8") + "&";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... inputs) {
        if (AppConstants.NETWORK_ONLINE) {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(buildEndpointUrl(method))
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.i("GifFetchTask", "Request Failure");
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        if (response.isSuccessful()) {
                            final ArrayList<MediaViewItem> newData = new ArrayList<>();
                            JSONObject resultObject = new JSONObject(jsonData);
                            final JSONArray data = resultObject.getJSONArray("data");
                            int count = data.length();
                            for (int i = 0; i < count; i++) {
                                JSONObject row = data.getJSONObject(i);
                                if (!row.isNull("images")) {
                                    JSONObject imageData = row.getJSONObject("images");
                                    if (!imageData.isNull("fixed_width") && !imageData.isNull("fixed_width_still")) {
                                        String animatedPath = imageData.getJSONObject("fixed_width").getString("url");
                                        String stillPath = imageData.getJSONObject("fixed_width_still").getString("url");
                                        //Log.d("GIPHY", animatedPath);
                                        newData.add(new MediaViewItem(animatedPath, stillPath));
                                    }
                                }
                            }
                            newData.add(new MediaViewItem());
                            fragment.updateAdapterData(newData);
                        } else {
                            Log.i("GifFetchTask", "Response Unsuccessful " + jsonData);
                        }
                    } catch (IOException e) {
                        Log.e("GifFetchTask", "Exception Caught: ", e);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        return null;
    }

    private String buildEndpointUrl(String method) {
        String output = API_URL + API_URI;
        output += method;
        output += "?" + search;
        output += "api_key=" + API_KEY;
        return output;
    }

}
