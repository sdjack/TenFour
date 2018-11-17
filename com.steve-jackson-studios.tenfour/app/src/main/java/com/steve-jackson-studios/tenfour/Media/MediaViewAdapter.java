package com.steve-jackson-studios.tenfour.Media;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.steve-jackson-studios.tenfour.R;

import java.util.ArrayList;

/**
 * Created by sjackson on 2/1/2017.
 * MediaViewAdapter
 */

class MediaViewAdapter extends ArrayAdapter<MediaViewItem> {

    MediaViewAdapter(Context context, ArrayList<MediaViewItem> data) {
        super(context, R.layout.media_gallery_item, data);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        MediaViewAdapter.ViewHolder holder;
        MediaViewItem item = getItem(position);
        Context c = getContext();

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(c);
            convertView = inflater.inflate(R.layout.media_gallery_item, parent, false);
            holder = new MediaViewAdapter.ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.text);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(holder);
        } else {
            holder = (MediaViewAdapter.ViewHolder) convertView.getTag();
        }

        if (item != null && !item.isDummy) {
            if (!item.isSticker) {
                holder.text.setText(item.animatedPath);
                holder.image.setContentDescription(item.animatedPath);
                GlideApp.with(c).asGif().load(Uri.parse(item.stillPath))
                        .centerCrop()
                        .thumbnail(0.5F)
                        .dontAnimate()
                        .placeholder(R.drawable.placeholder_image)
                        .into(holder.image);
            } else {
                holder.text.setText(item.animatedPath);
                holder.image.setContentDescription(item.animatedPath);
                GlideApp.with(c).asGif().load(Uri.parse(item.stillPath))
                        .fitCenter()
                        .thumbnail(0.5F)
                        .dontAnimate()
                        .placeholder(R.drawable.placeholder_image)
                        .into(holder.image);
            }
        } else {
            GlideApp.with(c).load(R.drawable.giphy_splash)
                    .centerCrop()
                    .thumbnail(0.5F)
                    .dontAnimate()
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.image);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView text;
        ImageView image;
    }
}
