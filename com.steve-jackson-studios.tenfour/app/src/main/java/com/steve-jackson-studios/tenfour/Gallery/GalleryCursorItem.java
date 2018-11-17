package com.steve-jackson-studios.tenfour.Gallery;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by sjackson on 7/12/2017.
 * GalleryCursorItem
 */

public class GalleryCursorItem {
    public final String fileName;
    public final Uri uri;

    public GalleryCursorItem(Cursor cursor) {
        int image_column_index = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
        int type_column_index = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);

        int id = cursor.getInt(image_column_index);
        String mime_type = cursor.getString(type_column_index);

        Uri uri;
        if(!mime_type.contains("video")) {
            uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
        } else {
            uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
        }

        this.fileName = mime_type;
        this.uri = uri;

    }
}
