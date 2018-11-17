package com.steve-jackson-studios.tenfour.Media;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.R;

import java.util.Collections;
import java.util.List;

/**
 * Created by sjackson on 9/1/2017.
 * MediaSelectActivity
 */

public class MediaSelectActivity extends Activity implements GiphyApi.Monitor {
    private static final String EXTRA_RESULT_TYPE = "result_type";
    private GifAdapter adapter;
    private int galleryType;

    public static Intent getIntent(Context context, int result) {
        Intent intent = new Intent(context, MediaSelectActivity.class);
        intent.putExtra(EXTRA_RESULT_TYPE, result);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_select_layout);

        galleryType = getIntent().getIntExtra(EXTRA_RESULT_TYPE, GiphyApi.TYPE_GIFS);

        ImageView giphyLogoView = (ImageView) findViewById(R.id.giphy_logo_view);
        GlideApp.with(this)
                .load(R.raw.large_giphy_logo)
                .into(giphyLogoView);

        final EditText searchText = (EditText) findViewById(R.id.giphy_search_field);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchValue = searchText.getText().toString();
                    searchText.clearFocus();
                    onGifSearch(searchValue);
                    handled = true;
                }
                return handled;
            }
        });
        searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        Button searchButton = (Button) findViewById(R.id.giphy_search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchValue = searchText.getText().toString();
                searchText.clearFocus();
                onGifSearch(searchValue);
            }
        });
        Button closeButton = (Button) findViewById(R.id.media_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(AppConstants.MEDIAACTIVITY_RESULT, intent);
                finish();
            }
        });

        RecyclerView gifList = (RecyclerView) findViewById(R.id.gif_list);
        GridAutofitLayoutManager layoutManager = new GridAutofitLayoutManager(this, 480);
        gifList.setLayoutManager(layoutManager);

        RequestBuilder<Drawable> gifItemRequest = GlideApp.with(this)
                .asDrawable();

        ViewPreloadSizeProvider<GiphyApi.GifResult> preloadSizeProvider =
                new ViewPreloadSizeProvider<>();
        adapter = new GifAdapter(this, gifItemRequest, preloadSizeProvider);
        gifList.setAdapter(adapter);
       ;
        RecyclerViewPreloader<GiphyApi.GifResult> preloader =
                new RecyclerViewPreloader<>(GlideApp.with(this), adapter, preloadSizeProvider, 4);
        gifList.addOnScrollListener(preloader);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AppConstants.SELECTACTIVITY_RESULT:
                imageSelected();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GiphyApi.get().addMonitor(this);
        if (adapter.getItemCount() == 0) {
            AppConstants.PENDING_POST_STICKER = (galleryType == GiphyApi.TYPE_STICKERS)? 1 : 0;
            GiphyApi.get().getGifs(galleryType);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        GiphyApi.get().removeMonitor(this);
    }

    @Override
    public void onSearchComplete(GiphyApi.SearchResult result) {
        adapter.setResults(result.data);
    }

    private void onGifSearch(String searchTerm) {
        GiphyApi.get().search(galleryType, searchTerm.trim());
    }

    private void imageSelected() {
        Intent intent = new Intent();
        setResult(AppConstants.MEDIAACTIVITY_RESULT, intent);
        finish();
    }

    private class GifAdapter extends RecyclerView.Adapter<GifViewHolder>
            implements ListPreloader.PreloadModelProvider<GiphyApi.GifResult> {
        private final GiphyApi.GifResult[] EMPTY_RESULTS = new GiphyApi.GifResult[0];

        private final Activity activity;
        private RequestBuilder<Drawable> requestBuilder;
        private ViewPreloadSizeProvider<GiphyApi.GifResult> preloadSizeProvider;
        private GiphyApi.GifResult[] results = EMPTY_RESULTS;

        GifAdapter(Activity activity, RequestBuilder<Drawable> requestBuilder,
                   ViewPreloadSizeProvider<GiphyApi.GifResult> preloadSizeProvider) {
            this.activity = activity;
            this.requestBuilder = requestBuilder;
            this.preloadSizeProvider = preloadSizeProvider;
        }

        void setResults(GiphyApi.GifResult[] results) {
            if (results != null) {
                this.results = results;
            } else {
                this.results = EMPTY_RESULTS;
            }
            notifyDataSetChanged();
        }

        @Override
        public GifViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = activity.getLayoutInflater().inflate(R.layout.gif_list_item, parent, false);
            return new GifViewHolder(view);
        }

        @Override
        public void onBindViewHolder(GifViewHolder holder, int position) {
            final GiphyApi.GifResult result = results[position];
            holder.gifView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    ClipboardManager clipboard =
//                            (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
//                    ClipData clip =
//                            ClipData.newPlainText("giphy_url", result.images.fixed_height.url);
//                    clipboard.setPrimaryClip(clip);

                    String selectedUrl = result.images.fixed_height.url;
                    AppConstants.PENDING_POST_FILENAME = selectedUrl.split("[?]")[0];
                    //Intent fullscreenIntent = FullscreenActivity.getIntent(activity, result);
                    //activity.startActivityForResult(fullscreenIntent, AppConstants.SELECTACTIVITY_RESULT);
                    imageSelected();
                }
            });

            requestBuilder.load(result).into(holder.gifView);

            preloadSizeProvider.setView(holder.gifView);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return results.length;
        }

        @Override
        public List<GiphyApi.GifResult> getPreloadItems(int position) {
            return Collections.singletonList(results[position]);
        }

        @Override
        public RequestBuilder<Drawable> getPreloadRequestBuilder(GiphyApi.GifResult item) {
            return requestBuilder.load(item);
        }
    }

    private static class GifViewHolder extends RecyclerView.ViewHolder {
        private final ImageView gifView;

        GifViewHolder(View itemView) {
            super(itemView);
            gifView = (ImageView) itemView.findViewById(R.id.gif_view);
        }
    }
}
