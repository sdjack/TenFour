package com.steve-jackson-studios.tenfour.Maps;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;

import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 7/28/2017.
 * CustomBubbleDrawable
 */

class CustomBubbleDrawable extends Drawable {
    private final Drawable mShadow;
    private final Drawable mMask;
    private int mColor = -1;

    public CustomBubbleDrawable(Resources res) {
        this.mMask = ResourcesCompat.getDrawable(res, com.google.maps.android.R.drawable.amu_bubble_mask, null);
        this.mShadow = ResourcesCompat.getDrawable(res, com.google.maps.android.R.drawable.amu_bubble_shadow, null);
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public void draw(Canvas canvas) {
        this.mMask.draw(canvas);
        canvas.drawColor(this.mColor, Mode.SRC_IN);
        this.mShadow.draw(canvas);
    }

    public void setAlpha(int alpha) {
        throw new UnsupportedOperationException();
    }

    public void setColorFilter(ColorFilter cf) {
        throw new UnsupportedOperationException();
    }

    public int getOpacity() {
        return -3;
    }

    public void setBounds(int left, int top, int right, int bottom) {
        this.mMask.setBounds(left, top, right, bottom);
        this.mShadow.setBounds(left, top, right, bottom);
    }

    public void setBounds(Rect bounds) {
        this.mMask.setBounds(bounds);
        this.mShadow.setBounds(bounds);
    }

    public boolean getPadding(Rect padding) {
        return this.mMask.getPadding(padding);
    }
}

