package com.steve-jackson-studios.tenfour.Misc;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;
import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 7/26/2017.
 * SplashScreen
 */

public class SplashScreen extends DialogFragment {

    private Animation zoomAnimation;
    private FrameLayout frame;
    private boolean isAnimating;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context context = getActivity();

        View view = inflater.inflate(R.layout.splash_layout, container, false);

        frame = (FrameLayout) view.findViewById(R.id.splash);
        ImageView logo = (ImageView) view.findViewById(R.id.logo_anim);
        AnimationDrawable logoAnimation = (AnimationDrawable) logo.getBackground();
        logoAnimation.start();

        zoomAnimation = AnimationUtils.loadAnimation(context, R.anim.splash_zoom);
        zoomAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimating = false;
                Dispatch.triggerEvent(ObservedEvents.NOTIFY_SPLASH_SCREEN_FINISHED);
                dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                isAnimating = true;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void toggle() {
        if (isAnimating) return;
        frame.startAnimation(zoomAnimation);
    }
}
