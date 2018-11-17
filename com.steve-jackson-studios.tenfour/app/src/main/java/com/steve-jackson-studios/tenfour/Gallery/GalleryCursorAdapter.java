package com.steve-jackson-studios.tenfour.Gallery;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.steve-jackson-studios.tenfour.Navigation.NavigationFragmentCallback;
import com.steve-jackson-studios.tenfour.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by sjackson on 7/12/2017.
 * GalleryCursorAdapter
 */

public class GalleryCursorAdapter extends SimpleCursorAdapter {

    private static final String TAG = "GalleryCursorAdapter";
    /**
     * registered listener
     */
    private NavigationFragmentCallback callbackListener;

    private static class ViewHolder {
        TextView text;
        ImageView image;
    }

    public GalleryCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public void bindView(View convertView, Context context, Cursor cursor) {

        final GalleryCursorItem item = new GalleryCursorItem(cursor);
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        String fileName = item.fileName;
        viewHolder.text.setText(fileName);
        //viewHolder.image.setImageBitmap(null);

        Uri thumbUri = item.uri;
        Glide.with(context)
                .load(thumbUri)
                //.asBitmap()
                .thumbnail(0.1F)
                //.diskCacheStrategy(DiskCacheStrategy.RESULT)
                //.placeholder(R.drawable.placeholder_image)
                .into(viewHolder.image);

        final Context c = context;
        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = item.fileName;
                //Log.d("GalleryCursorAdapter", "MIMETYPE = " + fileName);
                onImageSelected();
                if (!fileName.endsWith("gif")) {
                    new SelectBitmapTask(GalleryCursorAdapter.this, item.uri).execute();
                } else {
                    new SelectGifTask(c, GalleryCursorAdapter.this, fileName, item.uri).execute();
                }
            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(R.layout.gallery_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder();

        viewHolder.text = (TextView) convertView.findViewById(R.id.text);
        viewHolder.image = (ImageView) convertView.findViewById(R.id.image);

        convertView.setTag(viewHolder);

        return convertView;
    }

    /**
     * @param callbackListener the listener
     */
    void setCallbackListener(NavigationFragmentCallback callbackListener) {
        this.callbackListener = callbackListener;
    }

    void onImageSelected() {
        callbackListener.onImageSelected();
    }
    void onImageProcessed(String fileName) {
        callbackListener.onImageProcessed(fileName);
    }
}
