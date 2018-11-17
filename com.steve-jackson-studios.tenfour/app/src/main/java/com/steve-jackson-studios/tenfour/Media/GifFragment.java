package com.steve-jackson-studios.tenfour.Media;

import android.app.Fragment;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Navigation.NavigationFragmentCallback;
import com.steve-jackson-studios.tenfour.R;

import java.util.ArrayList;

/**
 * Created by sjackson on 1/23/2017.
 * GifFragment
 */

public class GifFragment extends Fragment implements View.OnClickListener,
        FragmentCompat.OnRequestPermissionsResultCallback {

    private final Handler handler = new Handler();
    private ListView listView;
    private MediaViewAdapter adapter;
    private int centerItemIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new MediaViewAdapter(getActivity(), new ArrayList<MediaViewItem>());
        new GifFetchTask(this).execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.media_gallery_layout, container, false);

        final EditText searchText = (EditText) view.findViewById(R.id.giphy_search_field);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchValue = searchText.getText().toString();
                    hideKeyboard();
                    searchText.clearFocus();
                    new GifFetchTask(GifFragment.this, searchValue).execute();
                    handled = true;
                }
                return handled;
            }
        });
        searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        ImageButton searchButton = (ImageButton) view.findViewById(R.id.giphy_search_button);
        searchButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageButton view = (ImageButton) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        view.getBackground().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        String searchValue = searchText.getText().toString();
                        hideKeyboard();
                        searchText.clearFocus();
                        new GifFetchTask(GifFragment.this, searchValue).execute();
                        break;
                    case MotionEvent.ACTION_CANCEL: {
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        break;
                    }
                }
                return true;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        listView = (ListView) view.findViewById(R.id.giphy_grid);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                callbackListener.onImageSelected();
                MediaViewItem item = (MediaViewItem) parent.getItemAtPosition(position);
                if (!item.isDummy) {
                    AppConstants.PENDING_IMAGE_FROM_GALLERY = true;
                    callbackListener.onImageProcessed(item.animatedPath);
                }
            }
        });
        final Context c = getActivity();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    resetChildGlideImages(c);
                }
                else {
                    haltChildGlideImages(c);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                centerItemIndex = getItemPositionFromRawYCoordinates(firstVisibleItem, totalItemCount);
                //Log.d("GIPHY_TEST", "centerItemIndex = " + centerItemIndex);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {

    }

    public void updateAdapterData(final ArrayList<MediaViewItem> data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                adapter.addAll(data);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public final int getItemPositionFromRawYCoordinates(int firstVisibleItem, int totalItemCount) {
        final int firstPos = listView.getFirstVisiblePosition();
        final int lastPos = listView.getLastVisiblePosition();
        final int total = lastPos - firstPos;
        final int[] coords = new int[2];
        for (int i=0; i<total; i++) {
            final View child = listView.getChildAt(i);
            child.getLocationOnScreen(coords);
            if (firstVisibleItem == 0 && i == 0 && (coords[1] > -10)) {
                return firstPos;
            } else if (firstVisibleItem == (totalItemCount - 1)) {
                return lastPos;
            }
        }
        return firstPos + 1;
    }

    public final void resetChildGlideImages(Context c) {
        final int total = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition();
        for (int i=0; i<total; i++) {
            final View child = listView.getChildAt(i);
            if (null != child) {
                ImageView iv = (ImageView) child.findViewById(R.id.image);
                if (null != iv) {
                    CharSequence animated = iv.getContentDescription();
                    if (null != animated) {
                        String path = animated.toString();
                        if ((listView.getFirstVisiblePosition()+i) != centerItemIndex) {
                            GlideApp.with(c)
                                    .load(Uri.parse(path.replace("w.gif","w_s.gif")))
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .centerCrop()
                                    .dontAnimate()
                                    .into(iv);
                        } else {
                            GlideApp.with(c).asGif()
                                    .load(Uri.parse(path))
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .centerCrop()
                                    .into(iv);
                        }
                    }
                }
            }
        }
    }

    public final void haltChildGlideImages(Context c) {
        final int total = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition();
        for (int i=0; i<total; i++) {
            final View child = listView.getChildAt(i);
            if (null != child) {
                ImageView iv = (ImageView) child.findViewById(R.id.image);
                if (null != iv) {
                    CharSequence animated = iv.getContentDescription();
                    if (null != animated) {
                        String path = animated.toString();
                        GlideApp.with(c)
                                .load(Uri.parse(path.replace("w.gif","w_s.gif")))
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .centerCrop()
                                .dontAnimate()
                                .into(iv);
                    }
                }
            }
        }
    }

    /**
     * Callbacks Interface
     */
    private NavigationFragmentCallback callbackListener;
    /**
     * @param callbackListener the listener
     */
    public void setCallbackListener(NavigationFragmentCallback callbackListener) {
        this.callbackListener = callbackListener;
    }
}
