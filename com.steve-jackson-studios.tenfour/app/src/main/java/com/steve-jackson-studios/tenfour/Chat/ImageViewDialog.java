package com.steve-jackson-studios.tenfour.Chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.steve-jackson-studios.tenfour.Gallery.SaveImageTask;
import com.steve-jackson-studios.tenfour.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by failcoder on 1/29/2017.
 * ImageViewDialog
 */

public class ImageViewDialog extends DialogFragment {

    private String imageFileUrl;
    private ImageView imageView;
    private ImageButton imageButton;
    private CallbackListener callbackListener;

    public void setFilePath(String path) {
        this.imageFileUrl = path;
    }

    public void setCallbackListener(CallbackListener listener) {
        this.callbackListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.imageview_dialog, container, false);

        imageView = (ImageView) view.findViewById(R.id.popup_image_view);
        Activity activity = getActivity();
        if (activity != null) {
            final Uri fileUri = Uri.parse(imageFileUrl);
            if (!imageFileUrl.toLowerCase().endsWith(".gif")) {
                Glide.with(getActivity()).load(fileUri)
                        //.asBitmap()
                        //.diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        //.placeholder(R.drawable.placeholder_image)
                        //.fitCenter()
                        .into(imageView);
            } else {
                Glide.with(getActivity()).load(fileUri)
                        //.asGif()
                        //.diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        //.placeholder(R.drawable.placeholder_image)
                        //.fitCenter()
                        .into(imageView);
            }
        }

        imageButton = (ImageButton) view.findViewById(R.id.popup_image_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        imageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showLongClickDialog(imageFileUrl);
                return true;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void dismiss() {
        callbackListener.onImageViewClosed();
        super.dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void showLongClickDialog(final String url) {
        // build dialog
        List<String> dialogItems = new ArrayList<String>();
        dialogItems.add("Save");
        dialogItems.add("Share");

        final CharSequence[] items = dialogItems.toArray(new String[dialogItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose your action");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch(item) {
                    case 0:
                        new SaveImageTask(getActivity(), url).execute();
                        break;
                    case 1:
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        //Text seems to be necessary for Facebook and Twitter
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, url);
                        startActivity(Intent.createChooser(sharingIntent,"Share using"));
                        break;
                    default:
                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Interface definition for callbacks
     */
    public interface CallbackListener {
        void onImageViewClosed();
    }
}
