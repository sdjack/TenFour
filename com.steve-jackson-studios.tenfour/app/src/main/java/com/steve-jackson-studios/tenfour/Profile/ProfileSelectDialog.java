package com.steve-jackson-studios.tenfour.Profile;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.AppResolver;
import com.steve-jackson-studios.tenfour.Azure.CloudMedia;
import com.steve-jackson-studios.tenfour.Media.GlideApp;
import com.steve-jackson-studios.tenfour.Misc.ResolverDialogFragment;
import com.steve-jackson-studios.tenfour.R;
import com.steve-jackson-studios.tenfour.Data.UserData;

import java.net.URI;
import java.util.ArrayList;

/**
 * Created by sjackson on 2/13/2017.
 * ProfileSelectDialog
 */

public class ProfileSelectDialog extends ResolverDialogFragment {

    private AvatarGalleryAdapter adapter;

    public static ProfileSelectDialog newInstance(AppResolver appResolver) {

        ProfileSelectDialog instance = new ProfileSelectDialog();
        instance.setResolver(appResolver);

        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ArrayList<AvatarGalleryItem> newData = new ArrayList<>();
        for (int i = 0; i < AppConstants.AVATAR_COLORS.length; i++) {
            int color = AppConstants.AVATAR_COLORS[i];
            newData.add(new AvatarGalleryItem(color));
        }
//        newData.add(new AvatarGalleryItem("https://mbcutestfaaa001.blob.core.windows.net/defaultavatars/unisex_type1.png"));
//        newData.add(new AvatarGalleryItem("https://mbcutestfaaa001.blob.core.windows.net/defaultavatars/unisex_type2.png"));
//        newData.add(new AvatarGalleryItem("https://mbcutestfaaa001.blob.core.windows.net/defaultavatars/unisex_type3.png"));
//        newData.add(new AvatarGalleryItem("https://mbcutestfaaa001.blob.core.windows.net/defaultavatars/male_type1.png"));
//        newData.add(new AvatarGalleryItem("https://mbcutestfaaa001.blob.core.windows.net/defaultavatars/male_type2.png"));
//        newData.add(new AvatarGalleryItem("https://mbcutestfaaa001.blob.core.windows.net/defaultavatars/male_type3.png"));
//        newData.add(new AvatarGalleryItem("https://mbcutestfaaa001.blob.core.windows.net/defaultavatars/female_type1.png"));
//        newData.add(new AvatarGalleryItem("https://mbcutestfaaa001.blob.core.windows.net/defaultavatars/female_type2.png"));
//        newData.add(new AvatarGalleryItem("https://mbcutestfaaa001.blob.core.windows.net/defaultavatars/female_type3.png"));
        adapter = new AvatarGalleryAdapter(getActivity(), newData);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.profile_avatar_dialog, container, false);

        Button button = (Button) view.findViewById(R.id.avatar_close_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        GridView gridView = (GridView) view.findViewById(R.id.avatar_gallery_grid);
        gridView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void sendAvatarUpdate(final String url) {
        resolver.saveProfileAvatar(url);
        dismiss();
    }

    public void sendAvatarUpdate(final int color) {
        resolver.saveProfileAvatar(color);
        dismiss();
    }

    private static class AvatarGalleryViewHolder {
        LinearLayout avatarContainer;
        ImageView avatarImage;
        TextView avatarText;
    }

    private class AvatarGalleryItem {
        final int type;
        final int color;
        final String imagePath;

        AvatarGalleryItem(int color) {
            this.type = 0;
            this.color = color;
            this.imagePath = "";
        }

        AvatarGalleryItem(String path) {
            this.type = 1;
            this.color = AppConstants.AVATAR_COLORS[0];
            this.imagePath = path;
        }
    }

    private class AvatarGalleryAdapter extends ArrayAdapter<AvatarGalleryItem> {

        AvatarGalleryAdapter(Context context, ArrayList<AvatarGalleryItem> data) {
            super(context, R.layout.profile_avatar_item, data);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            AvatarGalleryViewHolder holder;
            Context c = getContext();
            final AvatarGalleryItem item = getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(c);
                convertView = inflater.inflate(R.layout.profile_avatar_item, parent, false);
                holder = new AvatarGalleryViewHolder();
                holder.avatarContainer = (LinearLayout) convertView.findViewById(R.id.avatar_grid_item);
                holder.avatarText = (TextView) convertView.findViewById(R.id.avatar_type0);
                holder.avatarImage = (ImageView) convertView.findViewById(R.id.avatar_type1);
                convertView.setTag(holder);
            } else {
                holder = (AvatarGalleryViewHolder) convertView.getTag();
            }

            if (item != null) {

                holder.avatarContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (item.type == 1) {
                            new AvatarFetchTask(item.imagePath).execute();
                        } else {
                            sendAvatarUpdate(item.color);
                        }
                    }
                });
                if (item.type == 1) {
                    holder.avatarText.setVisibility(View.GONE);
                    holder.avatarImage.setVisibility(View.VISIBLE);
                    GlideApp.with(c)
                            .asBitmap()
                            .load(Uri.parse(item.imagePath))
                            .thumbnail(0.1f)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .placeholder(R.drawable.placeholder_image)
                            .fitCenter()
                            .into(holder.avatarImage);
                } else {
                    holder.avatarImage.setVisibility(View.GONE);
                    holder.avatarText.setVisibility(View.VISIBLE);
                    holder.avatarText.setText(UserData.INITIALS);
                    holder.avatarText.getBackground().setColorFilter(item.color, PorterDuff.Mode.SRC_ATOP);
                }

            }

            return convertView;
        }
    }

    private class AvatarFetchTask extends AsyncTask<Void, Void, Boolean> {

        private String imagePath;

        AvatarFetchTask(final String path) {
            this.imagePath = path;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                sendAvatarUpdate(imagePath);
            } else {
                dismiss();
            }
        }

        @Override
        protected Boolean doInBackground(Void... inputs) {
            String newUri = CloudMedia.copyAvatar(URI.create(imagePath), UserData.USERNAME + ".png");
            if (newUri != null) {
                imagePath = newUri;
                return true;
            }
            return false;
        }
    }
}
