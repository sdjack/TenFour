package com.steve-jackson-studios.tenfour.Gallery;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Azure.CloudMedia;
import com.steve-jackson-studios.tenfour.Media.ImageUtility;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sjackson on 6/2/2017.
 * SelectGifTask
 */

public class SelectGifTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private GalleryCursorAdapter adapter;
    private Uri uri;
    private String sourceFileName;

    SelectGifTask(final Context context, final GalleryCursorAdapter adapter, String name, final Uri uri) {
        this.context = context;
        this.adapter = adapter;
        this.uri = uri;
        this.sourceFileName = name;
    }

    @Override
    protected Void doInBackground(Void... inputs) {
        File photoFile = null;
        try {
            String filename = CloudMedia.getQualifiedFilename(sourceFileName);
            photoFile = ImageUtility.CreateImageFile(filename);
            InputStream input = context.getContentResolver().openInputStream(uri);
            if (null != input) {
                ByteArrayBuffer buffer = getFileBuffer(input);
                FileOutputStream output = new FileOutputStream(photoFile);
                output.write(buffer.toByteArray());
                output.close();

                final String fileName = CloudMedia.uploadFile("images", photoFile, filename);
                Log.d("SelectGifTask", fileName);
                if (fileName != null) {
                    AppConstants.PENDING_IMAGE_FROM_GALLERY = true;
                    adapter.onImageProcessed(fileName);
                } else {
                    Log.d("SelectGifTask", "CloudFile URL EMPTY");
                }

                input.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ByteArrayBuffer getFileBuffer(InputStream input) throws IOException {
        BufferedInputStream stream = new BufferedInputStream(input);
        ByteArrayBuffer buffer = new ByteArrayBuffer(5000);
        int current = 0;
        while ((current = stream.read()) != -1) {
            buffer.append((byte) current);
        }
        return buffer;
    }
}
