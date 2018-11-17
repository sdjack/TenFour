package com.steve-jackson-studios.tenfour.Gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.steve-jackson-studios.tenfour.Media.ImageUtility;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sjackson on 7/28/2017.
 * SaveImageTask
 */

public class SaveImageTask  extends AsyncTask<Void, Integer, Boolean> {

    private Context context;
    private String path;
    private int length;
    private ImageUtility.ImageError error;

    public SaveImageTask(final Context context, String path) {
        this.context = context;
        this.path = path;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if(success) {
            Toast.makeText(context, "Image Saved", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        //mImageLoaderListener.onProgressChange(values[0]);
    }

    @Override
    protected Boolean doInBackground(Void... inputs) {
        HttpURLConnection connection = null;
        InputStream input = null;
        ByteArrayOutputStream output = null;
        try {
            connection = (HttpURLConnection) new URL(path).openConnection();
            connection.connect();
            length = connection.getContentLength();
            if (length <= 0) {
                error = new ImageUtility.ImageError("Invalid content length. The URL is probably not pointing to a file")
                        .setErrorCode(ImageUtility.ImageError.ERROR_INVALID_FILE);
                this.cancel(true);
            }
            input = new BufferedInputStream(connection.getInputStream(), 8192);
            if (path.endsWith("gif")) {
                saveGIF(input);
            } else {
                saveBitmap(input);
            }

            input.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void saveBitmap(InputStream input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte bytes[] = new byte[8192];

        int count;
        long read = 0;
        while ((count = input.read(bytes)) != -1) {
            read += count;
            out.write(bytes, 0, count);
            //publishProgress((int) ((read * 100) / length));
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());
        MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, null, null);
    }

    private void saveGIF(InputStream input) throws IOException {
        ByteArrayBuffer buffer = getFileBuffer(input);

        File file = ImageUtility.CreateGifFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(buffer.toByteArray());
        fos.flush();
        fos.close();

        MediaScannerConnection.scanFile(context, new String[] { file.getAbsolutePath() }, null, null);
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

