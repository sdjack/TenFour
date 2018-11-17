package com.steve-jackson-studios.tenfour.Gallery;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Azure.CloudMedia;
import com.steve-jackson-studios.tenfour.Media.ImageUtility;

import java.io.File;
import java.io.IOException;

/**
 * Created by failc on 2/5/2017.
 * SelectBitmapTask
 */

public class SelectBitmapTask extends AsyncTask<Void, Void, Void> {

    private GalleryCursorAdapter adapter;
    private Uri uri;

    SelectBitmapTask(final GalleryCursorAdapter adapter, final Uri uri) {
        this.adapter = adapter;
        this.uri = uri;
    }

    @Override
    protected Void doInBackground(Void... inputs) {
        File photoFile = null;

        try {
            photoFile = ImageUtility.CreateImageFile();
            final String fileName = CloudMedia.uploadFile("images", photoFile, photoFile.getAbsolutePath());
            if (fileName != null) {
                AppConstants.PENDING_IMAGE_FROM_GALLERY = true;
                adapter.onImageProcessed(uri.toString());
            } else {
                Log.d("SelectBitmapTask", "CloudFile URL EMPTY");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}