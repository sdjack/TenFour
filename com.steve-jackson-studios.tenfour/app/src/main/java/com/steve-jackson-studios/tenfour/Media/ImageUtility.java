package com.steve-jackson-studios.tenfour.Media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Azure.CloudMedia;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by sjackson on 6/2/2017.
 * ImageUtility
 */

public class ImageUtility {

    public static final int ROTATE_NONE = 0;
    public static final int ROTATE_LEFT = 1;
    public static final int ROTATE_RIGHT = 2;
    public static final int ROTATE_FLIP = 3;

    private ImageUtility() {

    }

    /**
     * This method is responsible for solving the rotation issue if exist. Also scale the images to
     * 1024x1024 resolution
     *
     * @param context       The current context
     * @param selectedImage The Image URI
     * @return Bitmap image results
     * @throws IOException
     */
    public static Bitmap GetBitmapFromUri(Context context, Uri filePath) throws IOException {

        if (filePath == null) {return null;}
        Bitmap bitmap = null;

        String path = filePath.toString().split("[?]")[0];
        Uri newUri = Uri.parse(path);

        try {
            InputStream input = context.getContentResolver().openInputStream(newUri);
            if (null != input) {
                BufferedInputStream bis = new BufferedInputStream(input);
                ByteArrayBuffer baf = new ByteArrayBuffer(5000);
                int current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                bitmap = BitmapFactory.decodeByteArray(baf.toByteArray(), 0, baf.toByteArray().length, options);
                bis.close();
                input.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap RotateBitmap(Bitmap source, int direction)
    {
        if (source == null) return null;

        float angle = 0.0F;
        switch (direction) {
            case ROTATE_LEFT:
                angle = -90.0F;
                break;
            case ROTATE_RIGHT:
                angle = 90.0F;
                break;
            case ROTATE_FLIP:
                angle = 180.0F;
                break;
        }

        Matrix matrix = new Matrix();
        matrix.setRotate(angle);

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static File CreateGifFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
        String filename = ("MAMBO" + timeStamp + ".gif");
        AppConstants.PENDING_POST_FILENAME = filename;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/TenFour");
        if (!storageDir.exists()){
            storageDir.mkdirs();
        }
        return File.createTempFile(
                filename,  /* prefix */
                ".gif",         /* suffix */
                storageDir      /* directory */
        );
    }

    public static File CreateImageFile() throws IOException {
        // Create an image file name
        String filename = CloudMedia.getQualifiedFilename();
        AppConstants.PENDING_POST_FILENAME = filename;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/TenFour");
        if (!storageDir.exists()){
            storageDir.mkdirs();
        }
        return File.createTempFile(
                filename,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    public static File CreateImageFile(String filename) throws IOException {
        // Create an image file name
        AppConstants.PENDING_POST_FILENAME = filename;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/TenFour");
        if (!storageDir.exists()){
            storageDir.mkdirs();
        }
        String extension = (filename.toLowerCase().endsWith("gif")) ? ".gif" : ".jpg";
        return File.createTempFile(
                filename,  /* prefix */
                extension,         /* suffix */
                storageDir      /* directory */
        );
    }

    public static final class ImageError extends Throwable {

        private int errorCode;
        public static final int ERROR_GENERAL_EXCEPTION = -1;
        public static final int ERROR_INVALID_FILE = 0;
        public static final int ERROR_DECODE_FAILED = 1;
        public static final int ERROR_FILE_EXISTS = 2;
        public static final int ERROR_PERMISSION_DENIED = 3;
        public static final int ERROR_IS_DIRECTORY = 4;


        public ImageError(@NonNull String message) {
            super(message);
        }

        public ImageError(@NonNull Throwable error) {
            super(error.getMessage(), error.getCause());
            this.setStackTrace(error.getStackTrace());
        }

        public ImageError setErrorCode(int code) {
            this.errorCode = code;
            return this;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }
}
