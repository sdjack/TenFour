package com.steve-jackson-studios.tenfour.Widgets.FloatingMenu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 4/13/2017.
 * FloatingSubActionButton
 */

public class FloatingSubActionButton extends FrameLayout {

    public FloatingSubActionButton(Context context, FrameLayout.LayoutParams layoutParams, Drawable backgroundDrawable, View contentView, FrameLayout.LayoutParams contentParams) {
        super(context);
        setLayoutParams(layoutParams);
        //backgroundDrawable.mutate().getConstantState().newDrawable();

        setBackgroundResource(backgroundDrawable);
        if(contentView != null) {
            setContentView(contentView, contentParams);
        }
        setClickable(true);
    }

    /**
     * Sets a content view with custom LayoutParams that will be displayed inside this SubActionButton.
     * @param contentView
     * @param params
     */
    public void setContentView(View contentView, FrameLayout.LayoutParams params) {
        if(params == null) {
            params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            final int margin = getResources().getDimensionPixelSize(R.dimen.sub_action_button_content_margin);
            params.setMargins(margin, margin, margin, margin);
        }

        contentView.setClickable(false);
        if (contentView.getParent() != null)
            ((ViewGroup) contentView.getParent()).removeView(contentView);
        this.addView(contentView, params);
    }

    /**
     * Sets a content view with default LayoutParams
     * @param contentView
     */
    public void setContentView(View contentView) {
        setContentView(contentView, null);
    }

    private void setBackgroundResource(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        }
        else {
            setBackgroundDrawable(drawable);
        }
    }

    /**
     * A builder for {@link FloatingSubActionButton} in conventional Java Builder format
     */
    public static class Builder {

        private Context context;
        private FrameLayout.LayoutParams layoutParams;
        private Drawable backgroundDrawable;
        private View contentView;
        private FrameLayout.LayoutParams contentParams;

        public Builder(Context context, int sizeRef) {
            this.context = context;

            // Default SubActionButton settings
            int size = context.getResources().getDimensionPixelSize(sizeRef);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size, Gravity.TOP | Gravity.START);
            setLayoutParams(params);
        }

        public Builder(Context context, int widthRef, int heightRef) {
            this.context = context;

            // Default SubActionButton settings
            int width = context.getResources().getDimensionPixelSize(widthRef);
            int height = context.getResources().getDimensionPixelSize(heightRef);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height, Gravity.TOP | Gravity.START);
            setLayoutParams(params);
        }

        public Builder setLayoutParams(FrameLayout.LayoutParams params) {
            this.layoutParams = params;
            return this;
        }

        public Builder setBackgroundDrawable(Drawable backgroundDrawable) {
            this.backgroundDrawable = backgroundDrawable;
            return this;
        }

        public Builder setContentView(View contentView) {
            this.contentView = contentView;
            return this;
        }

        public Builder setContentView(View contentView, FrameLayout.LayoutParams contentParams) {
            this.contentView = contentView;
            this.contentParams = contentParams;
            return this;
        }

        public FloatingSubActionButton build() {
            return new FloatingSubActionButton(context,
                    layoutParams,
                    backgroundDrawable,
                    contentView,
                    contentParams);
        }
    }
}
