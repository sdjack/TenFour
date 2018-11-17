package com.steve-jackson-studios.tenfour.Widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 5/8/2017.
 * MenuActionButton
 */

public class IconButton extends FrameLayout {

    private final Context context;
    private ImageButton button;
    private OnTapListener listener;

    private int backgroundColor = 0xCFA0A0A0;
    private int iconColor = 0xFFFFFFFF;
    private Drawable background;
    private Drawable icon;

    public IconButton(Context context) {
        super(context);
        this.context = context;
        bindView();
    }

    public IconButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        bindView();
    }

    public IconButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        bindView();
    }

    private void bindView() {
        View view = inflate(context, R.layout.icon_action_button_template, null);
        this.button = (ImageButton) view.findViewById(R.id.iab_button);

        final IconButton parent = IconButton.this;
        this.button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        button.getBackground().setColorFilter(0x778CFFD2, PorterDuff.Mode.SRC_ATOP);
                        button.getDrawable().setColorFilter(0x773CFF82, PorterDuff.Mode.SRC_ATOP);
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        button.getBackground().clearColorFilter();
                        button.getDrawable().clearColorFilter();
                        if (listener != null) {
                            listener.onTapped(parent);
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL: {
                        button.getBackground().clearColorFilter();
                        button.getDrawable().clearColorFilter();
                        break;
                    }
                }
                return true;
            }
        });

        this.addView(view);
    }

    public void setOnTapListener(OnTapListener tapListener) {
        this.listener = tapListener;
    }

    public void setInternalTag (Object tag) {
        button.setTag(tag);
    }

    public Object getInternalTag () {
        return button.getTag();
    }

    public void setIcon(int iconId) {
        icon = ResourcesCompat.getDrawable(getResources(), iconId, null);
        button.setImageDrawable(icon);
        button.getBackground().setTint(backgroundColor);
        //invalidate();
    }

    public void setIcon(int iconId, String colorTag) {
        backgroundColor = Color.parseColor("#FF777777");
        icon = ResourcesCompat.getDrawable(getResources(), iconId, null);
        button.setImageDrawable(icon);
        button.getBackground().setTint(backgroundColor);
        //invalidate();
    }

    public ImageButton getContainer() {
        if (button.getParent() != null)
            ((ViewGroup) button.getParent()).removeView(button);
        return button;
    }

    /**
     * Interface definition for callbacks
     */
    public interface OnTapListener {
        void onTapped(final IconButton v);
    }
}
