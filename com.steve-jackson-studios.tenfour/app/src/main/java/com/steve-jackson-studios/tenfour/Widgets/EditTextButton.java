package com.steve-jackson-studios.tenfour.Widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 8/11/2017.
 * EditTextButton
 */

public class EditTextButton extends ViewGroup {

    private final int mClosedId;
    private final int mOpenedId;

    private View closedView;
    private View openedView;
    private Animation closingAnimation;
    private Animation openingAnimation;

    private Animation.AnimationListener closingListener;
    private Animation.AnimationListener openingListener;

    private boolean isOpen = false;
    private int openedWidth = 0;
    private int openedHeight = 0;
    private int openedMinWidth = 0;
    private int openedMinHeight = 0;
    private int[] openedPadding = new int[4];
    private int closedWidth = 0;
    private int closedHeight = 0;
    private int closedMinWidth = 0;
    private int closedMinHeight = 0;
    private int[] closedPadding = new int[4];

    public EditTextButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditTextButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EditTextButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.EditTextButton, defStyleAttr, defStyleRes);

        int closedId = a.getResourceId(R.styleable.EditTextButton_closed_element, 0);
        if (closedId == 0) {
            throw new IllegalArgumentException("The closed_element attribute is required and must refer "
                    + "to a valid element id.");
        }
        int openedId = a.getResourceId(R.styleable.EditTextButton_opened_element, 0);
        if (openedId == 0) {
            throw new IllegalArgumentException("The opened_element attribute is required and must refer "
                    + "to a valid element id.");
        }
        if (closedId == openedId) {
            throw new IllegalArgumentException("The closed_element and opened_element attributes must refer "
                    + "to different elements.");
        }

        this.mClosedId = closedId;
        this.mOpenedId = openedId;

        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        closedView = findViewById(mClosedId);
        if (closedView == null) {
            throw new IllegalArgumentException("The closed_element attribute is must refer to an"
                    + " existing child.");
        }
        closedPadding[0] = closedView.getPaddingStart();
        closedPadding[1] = closedView.getPaddingTop();
        closedPadding[2] = closedView.getPaddingEnd();
        closedPadding[3] = closedView.getPaddingBottom();
        closedMinHeight = closedView.getMinimumHeight();
        closedMinWidth = closedView.getMinimumWidth();

        closingAnimation = new CloseAnimation();
        closingAnimation.setDuration(200);
//        closedView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!isOpen) {
//                    isOpen = true;
//                    startAnimation(closingAnimation);
//                }
//            }
//        });

        openedView = findViewById(mOpenedId);
        if (openedView == null) {
            throw new IllegalArgumentException("The opened_element attribute is must refer to an"
                    + " existing child.");
        }
        openedPadding[0] = openedView.getPaddingStart();
        openedPadding[1] = openedView.getPaddingTop();
        openedPadding[2] = openedView.getPaddingEnd();
        openedPadding[3] = openedView.getPaddingBottom();
        openedMinHeight = openedView.getMinimumHeight();
        openedMinWidth = openedView.getMinimumWidth();

        openingAnimation = new OpenAnimation();
        openingAnimation.setDuration(200);
//        openedView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isOpen) {
//                    isOpen = false;
//                    startAnimation(openingAnimation);
//                }
//            }
//        });
        openedView.setVisibility(GONE);

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;

        //closedView.measure(0,0);
        closedView.layout(0, 0, width, height);
        closedView.setPadding(closedPadding[0], closedPadding[1], closedPadding[2], closedPadding[3]);

        openedView.measure(0,0);
        openedView.layout(0, 0, width + openedView.getMeasuredWidth(), height + openedView.getMeasuredHeight());
        openedView.setPadding(openedPadding[0], openedPadding[1], openedPadding[2], openedPadding[3]);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("EditTextButton cannot have UNSPECIFIED dimensions");
        }

        if (closedWidth == 0 || closedHeight == 0) {
            closedView.measure(0,0);
            this.closedWidth = closedView.getMeasuredWidth();
            this.closedHeight = closedView.getMeasuredHeight();
        }

        if (openedWidth == 0 || openedHeight == 0) {
            openedView.measure(0,0);
            this.openedWidth = openedView.getMeasuredWidth();
            this.openedHeight = openedView.getMeasuredHeight();
        }

        int w = (isOpen) ? (openedView.getMeasuredWidth() + openedPadding[0] + openedPadding[2]) : (closedView.getMeasuredWidth() + closedPadding[0] + closedPadding[2]);
        int h = (isOpen) ? (openedView.getMeasuredHeight() + openedPadding[1] + openedPadding[3]) : (closedView.getMeasuredHeight() + closedPadding[1] + closedPadding[3]);
        setMeasuredDimension(w, h);
    }

    public boolean isOpened() {
        return isOpen;
    }

    public void toggle() {
        if (isOpen) {
            isOpen = false;
            startAnimation(closingAnimation);
        } else {
            isOpen = true;
            startAnimation(openingAnimation);
        }
    }

    public void animateOpen() {
        if (isOpen) {
            return;
        }

        isOpen = true;
        startAnimation(openingAnimation);
    }

    public void animateClosed() {
        if (!isOpen) {
            return;
        }

        isOpen = false;
        startAnimation(closingAnimation);
    }

    public void setClosingAnimationListener(Animation.AnimationListener listener) {
        closingListener = listener;
    }

    public void setOpeningAnimationListener(Animation.AnimationListener listener) {
        openingListener = listener;
    }

    private class OpenAnimation extends Animation {

        public OpenAnimation() {
            //openedWidth = closedWidth * 2;
            //openedHeight = closedHeight * 2;

            setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (openingListener != null) {
                        openingListener.onAnimationStart(animation);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //closedView.setMinimumHeight(closedMinHeight);
                    //closedView.setMinimumWidth(closedMinWidth);
                    //closedView.setPadding(closedPadding[0], closedPadding[1], closedPadding[2], closedPadding[3]);
                    closedView.setVisibility(GONE);
//                    closedView.invalidate();
//                    closedView.requestLayout();

                    openedView.setVisibility(VISIBLE);
                    openedView.setMinimumHeight(openedMinHeight);
                    openedView.setMinimumWidth(openedMinWidth);
                    openedView.setPadding(openedPadding[0], openedPadding[1], openedPadding[2], openedPadding[3]);
//                    openedView.invalidate();
//                    openedView.requestLayout();
                    invalidate();
                    requestLayout();
                    openedView.requestFocus();
                    if (openingListener != null) {
                        openingListener.onAnimationEnd(animation);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    if (openingListener != null) {
                        openingListener.onAnimationRepeat(animation);
                    }
                }
            });
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {

            int newWidth = (int)(closedWidth + ((openedWidth - closedWidth) * interpolatedTime));
            int newHeight = (int)(closedHeight + ((openedHeight - closedHeight) * interpolatedTime));

            //closedView.setLayoutParams(new LinearLayout.LayoutParams(newWidth, newHeight));
//            closedView.invalidate();
//            closedView.requestLayout();

            openedView.setLayoutParams(new LinearLayout.LayoutParams(newWidth, newHeight));
//            openedView.invalidate();
//            openedView.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }


    }

    private class CloseAnimation extends Animation {

        public CloseAnimation() {
            setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (closingListener != null) {
                        closingListener.onAnimationStart(animation);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    openedView.setMinimumHeight(openedMinHeight);
                    openedView.setMinimumWidth(openedMinWidth);
                    openedView.setPadding(openedPadding[0], openedPadding[1], openedPadding[2], openedPadding[3]);
                    openedView.setVisibility(GONE);
//                    openedView.invalidate();
//                    openedView.requestLayout();

                    closedView.setVisibility(VISIBLE);
                    //closedView.setMinimumHeight(closedMinHeight);
                    //closedView.setMinimumWidth(closedMinWidth);
                    //closedView.setPadding(closedPadding[0], closedPadding[1], closedPadding[2], closedPadding[3]);
//                    closedView.invalidate();
//                    closedView.requestLayout();

                    invalidate();
                    requestLayout();
                    if (closingListener != null) {
                        closingListener.onAnimationEnd(animation);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    if (closingListener != null) {
                        closingListener.onAnimationRepeat(animation);
                    }
                }
            });
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {

            int newWidth = (int)(openedWidth + ((closedWidth - openedWidth) * interpolatedTime));
            int newHeight = (int)(openedHeight + ((closedHeight - openedHeight) * interpolatedTime));

            //closedView.setLayoutParams(new LinearLayout.LayoutParams(newWidth, newHeight));
//            closedView.invalidate();
//            closedView.requestLayout();

            openedView.setLayoutParams(new LinearLayout.LayoutParams(newWidth, newHeight));
//            openedView.invalidate();
//            openedView.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
