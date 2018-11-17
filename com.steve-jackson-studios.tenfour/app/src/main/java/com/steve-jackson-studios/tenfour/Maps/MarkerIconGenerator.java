package com.steve-jackson-studios.tenfour.Maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.maps.android.ui.RotationLayout;
import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 8/3/2017.
 * MarkerIconGenerator
 */

public class MarkerIconGenerator {
    private final Context mContext;
    private RotationLayout mRotationLayout;
    private ViewGroup mContainer;
    private ImageView mImageView;
    private View mContentView;
    private CustomBubbleDrawable mBackground;

    public MarkerIconGenerator(Context context) {
        this.mContext = context;
        this.mContainer = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.map_marker_layout, (ViewGroup)null);
        this.mBackground = new CustomBubbleDrawable(this.mContext.getResources());
        this.mRotationLayout = (RotationLayout)this.mContainer.getChildAt(0);
        this.mContentView = this.mImageView = (ImageView) mRotationLayout.findViewById(R.id.image);
        this.setBackground(this.mBackground);
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

    public void setContentView(View contentView) {
        this.mRotationLayout.removeAllViews();
        this.mRotationLayout.addView(contentView);
        this.mContentView = contentView;
        View view = this.mRotationLayout.findViewById(R.id.image);
        this.mImageView = view instanceof ImageView?(ImageView)view:null;
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public void setColor(int color) {
        this.mBackground.setColor(color);
        this.setBackground(this.mBackground);
    }

    public void setBackground(Drawable background) {
        this.mImageView.setBackground(background);
        if(background != null) {
            Rect rect = new Rect();
            background.getPadding(rect);
            this.mImageView.setPadding(rect.left, rect.top, rect.right, rect.bottom);
        } else {
            this.mImageView.setPadding(0, 0, 0, 0);
        }

    }
}
