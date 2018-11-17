package com.steve-jackson-studios.tenfour.Media;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ExifInterface;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;

import java.security.MessageDigest;

/**
 * Created by sjackson on 6/2/2017.
 * ImageOrientation
 */

public class ImageOrientation extends BitmapTransformation {

    private int mOrientation;

    public ImageOrientation(Context context, int orientation) {
        super(context);
        mOrientation = orientation;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        int exifOrientationDegrees = getExifOrientationDegrees(mOrientation);
        return TransformationUtils.rotateImageExif(pool, toTransform, exifOrientationDegrees);
    }

    private int getExifOrientationDegrees(int orientation) {
        int exifInt;
        switch (orientation) {
            case 90:
                exifInt = ExifInterface.ORIENTATION_ROTATE_90;
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                exifInt = -90;
                break;
            case ExifInterface.ORIENTATION_NORMAL:
                exifInt = 0;
                break;
            default:
                exifInt = orientation;
                break;
        }
        return exifInt;
    }

    public String getId() {
        return "com.steve-jackson-studios.tenfour.Media.ImageOrientation";
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {

    }
}
