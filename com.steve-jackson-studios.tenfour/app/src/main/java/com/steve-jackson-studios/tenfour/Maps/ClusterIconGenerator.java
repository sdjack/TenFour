package com.steve-jackson-studios.tenfour.Maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.maps.android.ui.RotationLayout;
import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 7/28/2017.
 * ClusterIconGenerator
 */

public class ClusterIconGenerator {

    private ViewGroup mContainer;
    private TextView mTextView;

    public ClusterIconGenerator(Context context) {
        this.mContainer = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.map_pin_layout, (ViewGroup)null);

        Drawable background = ResourcesCompat.getDrawable(context.getResources(), R.drawable.map_cluster_fg, null);
        this.mContainer.setBackground(background);
        if(background != null) {
            Rect rect = new Rect();
            background.getPadding(rect);
            this.mContainer.setPadding(rect.left, rect.top, rect.right, rect.bottom);
        }
        RotationLayout mRotationLayout = (RotationLayout)this.mContainer.getChildAt(0);
        this.mTextView = (TextView) mRotationLayout.findViewById(R.id.amu_text);
    }

    public Bitmap makeIcon(CharSequence text) {
        if(this.mTextView != null) {
            this.mTextView.setText(text);
        }

        return this.makeIcon();
    }

    public Bitmap makeIcon() {
        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        this.mContainer.measure(measureSpec, measureSpec);
        int measuredWidth = this.mContainer.getMeasuredWidth();
        int measuredHeight = this.mContainer.getMeasuredHeight();
        this.mContainer.layout(0, 0, measuredWidth, measuredHeight);

        Bitmap r = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        r.eraseColor(0);
        Canvas canvas = new Canvas(r);
        this.mContainer.draw(canvas);
        return r;
    }
}
