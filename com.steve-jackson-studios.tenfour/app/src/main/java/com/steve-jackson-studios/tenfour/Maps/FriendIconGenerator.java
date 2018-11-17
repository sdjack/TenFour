package com.steve-jackson-studios.tenfour.Maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.maps.android.ui.RotationLayout;
import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 8/3/2017.
 * MarkerIconGenerator
 */

public class FriendIconGenerator {
    private final Context mContext;
    private RotationLayout mRotationLayout;
    private ViewGroup mContainer;
    private TextView mTextView;
    private LinearLayout mBody;
    private CustomBubbleDrawable mBackground;

    public FriendIconGenerator(Context context) {
        this.mContext = context;
        this.mContainer = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.map_friend_layout, (ViewGroup)null);
        this.mBackground = new CustomBubbleDrawable(this.mContext.getResources());
        this.mRotationLayout = (RotationLayout)this.mContainer.getChildAt(0);
        this.mBody = mRotationLayout.findViewById(R.id.body);
        this.mTextView = mRotationLayout.findViewById(R.id.friend_name);
        this.setBackground(this.mBackground);
    }

    public Bitmap makeIcon(String title, int color, int color2) {
        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        this.mContainer.measure(measureSpec, measureSpec);
        int measuredWidth = this.mContainer.getMeasuredWidth();
        int measuredHeight = this.mContainer.getMeasuredHeight();
        this.mContainer.layout(0, 0, measuredWidth, measuredHeight);

        this.mTextView.setText(title);
        this.mTextView.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        this.mBody.getBackground().setColorFilter(color2, PorterDuff.Mode.SRC_ATOP);

        Bitmap r = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        r.eraseColor(0);
        Canvas canvas = new Canvas(r);
        this.mContainer.draw(canvas);
        return r;
    }

    public void setContentView(View contentView) {
        this.mRotationLayout.removeAllViews();
        this.mRotationLayout.addView(contentView);
        View view = this.mRotationLayout.findViewById(R.id.friend_name);
        this.mTextView = view instanceof TextView?(TextView)view:null;
    }

    public TextView getTextView() {
        return mTextView;
    }

    public void setColor(int color) {
        this.mBackground.setColor(color);
        this.setBackground(this.mBackground);
    }

    public void setBackground(Drawable background) {
        this.mTextView.setBackground(background);
        if(background != null) {
            Rect rect = new Rect();
            background.getPadding(rect);
            this.mTextView.setPadding(rect.left, rect.top, rect.right, rect.bottom);
        } else {
            this.mTextView.setPadding(0, 0, 0, 0);
        }

    }
}
