package com.steve-jackson-studios.tenfour.Media;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 9/1/2017.
 * FullscreenActivity
 */

public class FullscreenActivity extends Activity {
    private static final String EXTRA_RESULT_JSON = "result_json";
    private static final String EXTRA_RESULT_URL = "result_path";
    private GifDrawable gifDrawable;
    private String selectedUrl;

    public static Intent getIntent(Context context, GiphyApi.GifResult result) {
        Intent intent = new Intent(context, FullscreenActivity.class);
        intent.putExtra(EXTRA_RESULT_JSON, new Gson().toJson(result));
        intent.putExtra(EXTRA_RESULT_URL, result.images.original.url);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_activity);

        String resultJson = getIntent().getStringExtra(EXTRA_RESULT_JSON);
        selectedUrl = getIntent().getStringExtra(EXTRA_RESULT_URL);
        final GiphyApi.GifResult result = new Gson().fromJson(resultJson, GiphyApi.GifResult.class);

        ImageView gifView = (ImageView) findViewById(R.id.fullscreen_gif);

        gifView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                ClipData clip = ClipData.newPlainText("giphy_url", result.images.original.url);
//                clipboard.setPrimaryClip(clip);

//                if (gifDrawable != null) {
//                    if (gifDrawable.isRunning()) {
//                        gifDrawable.stop();
//                    } else {
//                        gifDrawable.start();
//                    }
//                }
                selectThisImage();
            }
        });

        RequestBuilder<Drawable> thumbnailRequest = GlideApp.with(this)
                .load(result)
                .decode(Bitmap.class);

        GlideApp.with(this)
                .load(result.images.original.url)
                .thumbnail(thumbnailRequest)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target,
                                                boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                   DataSource dataSource, boolean isFirstResource) {
                        if (resource instanceof GifDrawable) {
                            gifDrawable = (GifDrawable) resource;
                        } else {
                            gifDrawable = null;
                        }
                        return false;
                    }
                })
                .into(gifView);
    }

    private void selectThisImage() {
        AppConstants.PENDING_POST_FILENAME = selectedUrl.split("/[?#]/")[0];
        Intent intent = new Intent();
        setResult(AppConstants.SELECTACTIVITY_RESULT, intent);
        finish();
    }
}
