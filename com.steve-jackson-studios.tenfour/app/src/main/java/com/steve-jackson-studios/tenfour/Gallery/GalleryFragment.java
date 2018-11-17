package com.steve-jackson-studios.tenfour.Gallery;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.steve-jackson-studios.tenfour.Navigation.NavigationFragmentCallback;
import com.steve-jackson-studios.tenfour.R;
import com.steve-jackson-studios.tenfour.Misc.GridFragment;

/**
 * Created by sjackson on 1/20/2017.
 * GalleryFragment
 */

public class GalleryFragment extends GridFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 102;
    private GalleryCursorAdapter adapter;
    private String[] projection = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gallery_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new GalleryCursorAdapter(getActivity(),
                R.layout.gallery_list_item,
                null,
                new String[] { MediaStore.Images.Thumbnails._ID },
                new int[] { R.id.image },
                0);
        adapter.setCallbackListener(callbackListener);
        setListAdapter(adapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                //+ " OR "
                //+ MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                //+ MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
        ;

        Uri queryUri = MediaStore.Files.getContentUri("external");

        return new CursorLoader(getActivity(),
                queryUri,
                projection,
                selection,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (adapter != null && loader != null) {
            adapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (adapter != null) {
            adapter.swapCursor(null);
        }
    }
}
