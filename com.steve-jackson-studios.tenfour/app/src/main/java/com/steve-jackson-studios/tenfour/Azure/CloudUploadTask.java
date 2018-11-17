package com.steve-jackson-studios.tenfour.Azure;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sjackson on 5/30/2017.
 * CloudUploadTask
 */

public class CloudUploadTask  extends AsyncTask<Void, Void, Void> {

    private Context context;

    public CloudUploadTask(final Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... inputs) {
        String mFileName = CloudMedia.getQualifiedFilename();
        //Log.d("CloudUploadTask", "getQualifiedFilename( FILENAME = " + mFileName + ")");
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/TenFour");
        if (!path.exists()){
            path.mkdirs();
        }
        File mFile = new File(path, mFileName);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(mFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != output) {
                try {
                    AppConstants.PENDING_POST_BITMAP.compress(Bitmap.CompressFormat.JPEG, 30, output);
                    output.flush();
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String folder = AppConstants.BLOB_CONTAINER;
            //String folder = (UserData.LOCATION_ID != null) ? UserData.LOCATION_ID : "images";
            AppConstants.PENDING_POST_FILENAME = CloudMedia.uploadFile(folder, mFile, mFileName);
            //Log.d("CloudUploadTask", "PENDING_POST_FILENAME = " + AppConstants.PENDING_POST_FILENAME);
            if (AppConstants.PENDING_POST_FILENAME != null) {
                if (!AppConstants.PENDING_IMAGE_FROM_GALLERY) {
                    MediaStore.Images.Media.insertImage(context.getContentResolver(), AppConstants.PENDING_POST_BITMAP, null, AppConstants.PENDING_POST_FILENAME);
                    MediaScannerConnection.scanFile(context, new String[] { mFile.getAbsolutePath() }, null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Dispatch.triggerEvent(ObservedEvents.CHAT_MEDIA_UPLOADED);
                        }
                    });
                } else {
                    AppConstants.PENDING_IMAGE_FROM_GALLERY = false;
                    Dispatch.triggerEvent(ObservedEvents.CHAT_MEDIA_UPLOADED);
                }
            }
        }
        return null;
    }
}
