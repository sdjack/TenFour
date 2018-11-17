package com.steve-jackson-studios.tenfour.Chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import com.steve-jackson-studios.tenfour.Gallery.SaveImageTask;
import com.steve-jackson-studios.tenfour.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sjackson on 9/1/2017.
 * WebViewDialog
 */

public class WebViewDialog extends DialogFragment {
    private static String webUrl;
    private WebView webView;
    private ImageButton imageButton;

    public static WebViewDialog newInstance(String fileName) {
        webUrl = fileName;
        return new WebViewDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.webview_dialog, container, false);

        webView = (WebView) view.findViewById(R.id.webview_container);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    view.loadUrl(request.getUrl().toString());
                    return true;
                }
            });
        } else {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
        }
        webView.loadUrl(webUrl);

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
                showLongClickDialog();
                return true;
            }
        });

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

    public void showLongClickDialog() {
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
                        new SaveImageTask(getActivity(), webUrl).execute();
                        break;
                    case 1:
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        //Text seems to be necessary for Facebook and Twitter
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, webUrl);
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
}
