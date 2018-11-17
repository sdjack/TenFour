package com.steve-jackson-studios.tenfour.Navigation;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Data.ChatPostData;
import com.steve-jackson-studios.tenfour.IO.SocketIO;
import com.steve-jackson-studios.tenfour.R;
import com.steve-jackson-studios.tenfour.Data.UserData;
import com.steve-jackson-studios.tenfour.Widgets.FloatingMenu.FloatingActionButton;
import com.steve-jackson-studios.tenfour.Widgets.FloatingMenu.FloatingActionMenu;
import com.steve-jackson-studios.tenfour.Widgets.FloatingMenu.FloatingSubActionButton;
import com.steve-jackson-studios.tenfour.Widgets.IconButton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sjackson on 1/24/2017.
 * AndroidNavigation
 */

public class NavigationFragment extends Fragment
        implements FragmentCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = "NavigationFragment";
    public static final int EXPLORE = R.drawable.map_mylocation;
    public static final int CHAT = R.drawable.icon_chat_button;
    public static final int MENU = R.drawable.icon_menu;
    public static final int SUB_OPTION_0 = R.drawable.ic_compose;
    public static final int SUB_OPTION_1 = R.drawable.ic_gallery;
    public static final int SUB_OPTION_2 = R.drawable.ic_camera;
    public static final int SUB_OPTION_3 = R.drawable.ic_gif;
    public static final int SUB_OPTION_4 = R.drawable.ic_stickers;
    public static final int SUB_OPTION_5 = R.drawable.ic_create;

    private int[] secondaryMenuIcons = {
            SUB_OPTION_0,
            //SUB_OPTION_1,
            SUB_OPTION_2,
            SUB_OPTION_3,
            SUB_OPTION_4
    };
    private int[] userMenuIcons = {
            R.drawable.button_user_view,
            R.drawable.button_user_invite,
            R.drawable.button_user_report,
            R.drawable.button_user_block
    };
    private int[] karmaMenuIds = {
            R.id.bad4,
            R.id.bad3,
            R.id.bad2,
            R.id.bad1,
            R.id.good1,
            R.id.good2,
            R.id.good3,
            R.id.good4
    };
    private String[] karmaMenuValues = {"-4","-3","-2","-1","1","2","3","4"};

    public static int ACTIVE_STATE = EXPLORE;
    /**
     * registered listener
     */
    private CallbackListener callbackListener;
    private ImageButton menuBackdrop;

    private FloatingActionButton inputMenuButton;
    private FloatingActionMenu inputMenu;
    private IconButton[] inputMenuSubOpts = new IconButton[4];

    private FloatingActionButton userMenuButton;
    private FloatingActionMenu userMenu;
    private Button[] userMenuSubOpts = new Button[4];

    private LinearLayout karmaMenu;
    private ImageButton[] karmaMenuSubOpts = new ImageButton[8];
    private Animation zoomAnimation;

    private int maxOffset = 0;
    private int karmaRadius = 0;
    private String voteId = null;
    private ChatPostData viewUserChatPostData;

    private boolean hasCameraHardware = true;

    /**
     * @param callbackListener the listener
     */
    public void setCallbackListener(CallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Context context = getActivity();

        View view = inflater.inflate(R.layout.navigation_layout, container, false);
        menuBackdrop = (ImageButton) view.findViewById(R.id.floating_menu_backdrop);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        ImageButton smib = (ImageButton) view.findViewById(R.id.media_menu_button);
        ImageButton umib = (ImageButton) view.findViewById(R.id.user_menu_button);
        karmaMenu = (LinearLayout) view.findViewById(R.id.karma_menu);

        hasCameraHardware = checkCameraHardware(context);

        int smallSize = (int) getResources().getDimension(R.dimen.menu_icon_small);
        int largeSize = (int) getResources().getDimension(R.dimen.menu_icon_large);
        FrameLayout.LayoutParams subOptionParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        FloatingSubActionButton.Builder subBuilder = new FloatingSubActionButton.Builder(context, R.dimen.sub_action_button_size);
        subBuilder.setLayoutParams(new FrameLayout.LayoutParams(smallSize, smallSize));

        int iconSize = (int) context.getResources().getDimension(R.dimen.icon_action_button_size);
        RelativeLayout.LayoutParams iconButtonParams = new RelativeLayout.LayoutParams(iconSize, iconSize);
        int actionHeight = (int) getResources().getDimension(R.dimen.menu_icon_small);
        int userMenuRadius = getResources().getDimensionPixelSize(R.dimen.chat_menu_radius);
        int karmaMenuRadius = getResources().getDimensionPixelSize(R.dimen.karma_menu_radius);
        karmaRadius = (int) (karmaMenuRadius * 0.75);
        maxOffset = displaymetrics.widthPixels - (karmaMenuRadius * 2);


        for (int i = 0; i < inputMenuSubOpts.length; i++) {
            inputMenuSubOpts[i] = new IconButton(context);
            inputMenuSubOpts[i].setOnTapListener(new IconButton.OnTapListener() {
                @Override
                public void onTapped(IconButton v) {
                    final int position = (int) v.getInternalTag();
                    updateSubOptions(position);
                }
            });
            inputMenuSubOpts[i].setInternalTag(secondaryMenuIcons[i]);
            //inputMenuSubOpts[i].setLabel(secondaryMenuLabels[i]);
            inputMenuSubOpts[i].setIcon(secondaryMenuIcons[i]);
            container.addView(inputMenuSubOpts[i], iconButtonParams);
        }

        if (smib.getParent() != null)
            ((ViewGroup) smib.getParent()).removeView(smib);

        inputMenuButton = new FloatingActionButton.Builder(context)
                .setParentView((ViewGroup) view)
                .setContentView(smib)
                .build();

        inputMenu = new FloatingActionMenu.Builder(context)
                .setStartAngle(270)
                .setEndAngle(350)
                .setRadius(getResources().getDimensionPixelSize(R.dimen.nav_menu_radius))
                .addSubActionView(subBuilder.setContentView(inputMenuSubOpts[0].getContainer(), subOptionParams).build())
                .addSubActionView(subBuilder.setContentView(inputMenuSubOpts[1].getContainer(), subOptionParams).build())
                .addSubActionView(subBuilder.setContentView(inputMenuSubOpts[2].getContainer(), subOptionParams).build())
                .addSubActionView(subBuilder.setContentView(inputMenuSubOpts[3].getContainer(), subOptionParams).build())
                .attachTo(inputMenuButton)
                .build();

        inputMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userMenu.close(false);
                zoomButtonsOut();
                if (inputMenu.toggle(true)) {
                    menuBackdrop.setVisibility(View.VISIBLE);
                } else {
                    menuBackdrop.setVisibility(View.GONE);
                }
            }
        });

        for (int i = 0; i < userMenuSubOpts.length; i++) {
            Drawable iconBackground = ResourcesCompat.getDrawable(getResources(), userMenuIcons[i], null);
            userMenuSubOpts[i] = new Button(context);
            userMenuSubOpts[i].setTag(i + 1);
            userMenuSubOpts[i].setLayoutParams(iconButtonParams);
            userMenuSubOpts[i].setBackground(iconBackground);
            userMenuSubOpts[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_UP:
                            updateUserMenu((int) v.getTag());
                            closeMenus();
                            break;
                    }
                    return true;
                }
            });
            container.addView(userMenuSubOpts[i]);
        }

        if (umib.getParent() != null)
            ((ViewGroup) umib.getParent()).removeView(umib);

        userMenuButton = new FloatingActionButton.Builder(context)
                .setParentView((ViewGroup) view)
                .setContentView(umib)
                .build();

        userMenu = new FloatingActionMenu.Builder(context)
                .setStartAngle(290)
                .setEndAngle(440)
                .setRadius(userMenuRadius)
                .addSubActionView(subBuilder.setContentView(userMenuSubOpts[0], subOptionParams).build())
                .addSubActionView(subBuilder.setContentView(userMenuSubOpts[1], subOptionParams).build())
                .addSubActionView(subBuilder.setContentView(userMenuSubOpts[2], subOptionParams).build())
                .addSubActionView(subBuilder.setContentView(userMenuSubOpts[3], subOptionParams).build())
                .attachTo(userMenuButton)
                .disableAnimations()
                .build();

        userMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenus();
            }
        });

        final View.OnTouchListener karmaTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageButton button = (ImageButton) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        button.getBackground().setColorFilter(0x778CFFD2, PorterDuff.Mode.SRC_ATOP);
                        button.getDrawable().setColorFilter(0x773CFF82, PorterDuff.Mode.SRC_ATOP);
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        button.getBackground().clearColorFilter();
                        button.getDrawable().clearColorFilter();
                        String voteValue = (String)v.getTag();
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("postId", voteId);
                            obj.put("locationId", AppConstants.LOCATION_ID);
                            obj.put("userName", UserData.USERNAME);
                            obj.put("reactionScore", voteValue);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        SocketIO.emit("reaction", obj);
                        closeMenus();
                        break;
                    case MotionEvent.ACTION_CANCEL: {
                        button.getBackground().clearColorFilter();
                        button.getDrawable().clearColorFilter();
                        break;
                    }
                }
                return true;
            }
        };

        for (int i = 0; i < karmaMenuSubOpts.length; i++) {
            karmaMenuSubOpts[i] = (ImageButton) karmaMenu.findViewById(karmaMenuIds[i]);
            karmaMenuSubOpts[i].setTag(karmaMenuValues[i]);
            karmaMenuSubOpts[i].setOnTouchListener(karmaTouchListener);
        }

        int menuSize = (int) getResources().getDimension(R.dimen.menu_icon_large);

        userMenuButton.setLayoutParams(new FrameLayout.LayoutParams(menuSize, menuSize, Gravity.CENTER));
        userMenuButton.setVisibility(View.GONE);

        FrameLayout.LayoutParams floatingSecondaryParams = new FrameLayout.LayoutParams(smallSize, smallSize, Gravity.BOTTOM);
        floatingSecondaryParams.setMargins(8, 0, 0, 8);
        inputMenuButton.setLayoutParams(floatingSecondaryParams);
        inputMenuButton.setBackgroundResource(R.drawable.ic_compose);
        inputMenuButton.setVisibility(View.GONE);

        menuBackdrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenus();
            }
        });

        return view;
    }

    private void updateSubOptions(final int position) {
        showInputView();

        switch (position) {
            case SUB_OPTION_0:
                inputMenuButton.setBackgroundResource(position);
                if (ACTIVE_STATE != CHAT) {
                    ACTIVE_STATE = CHAT;
                }
                callbackListener.onNavigation(CHAT);
                break;
//            case SUB_OPTION_1:
//                inputMenuButton.setBackgroundResource(position);
//                if (ACTIVE_STATE != CHAT) {
//                    ACTIVE_STATE = CHAT;
//                    callbackListener.onNavigation(CHAT);
//                }
//                break;
            case SUB_OPTION_2:
                if (!hasCameraHardware) {
                    return;
                }
                inputMenuButton.setBackgroundResource(position);
                if (ACTIVE_STATE != CHAT) {
                    ACTIVE_STATE = CHAT;
                    callbackListener.onNavigation(CHAT);
                }
                break;
            case SUB_OPTION_3:
                inputMenuButton.setBackgroundResource(position);
                if (ACTIVE_STATE != CHAT) {
                    ACTIVE_STATE = CHAT;
                    callbackListener.onNavigation(CHAT);
                }
                break;
            default:
                inputMenuButton.setBackgroundResource(SUB_OPTION_0);
        }

        callbackListener.onSubNavigation(position);
    }

    public void hideInputView() {inputMenu.close(true);
        userMenu.close(false);
        userMenuButton.setVisibility(View.GONE);
        menuBackdrop.setVisibility(View.GONE);
        karmaMenu.setVisibility(View.GONE);
        inputMenuButton.setVisibility(View.GONE);
    }

    public void showInputView() {inputMenu.close(true);
        userMenu.close(false);
        userMenuButton.setVisibility(View.GONE);
        menuBackdrop.setVisibility(View.GONE);
        karmaMenu.setVisibility(View.GONE);
        inputMenuButton.setVisibility(View.VISIBLE);
    }

    private void zoomButtonsIn() {
        if (zoomAnimation != null) {
            zoomAnimation.cancel();
        }
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.zoom_in);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                karmaMenu.setVisibility(View.VISIBLE);
//                for (ImageButton button : karmaMenuSubOpts) {
//                    button.setVisibility(View.VISIBLE);
//                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                zoomAnimation = null;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        zoomAnimation = anim;
        karmaMenu.setAnimation(zoomAnimation);
    }

    private void zoomButtonsOut() {
        if (zoomAnimation != null) {
            zoomAnimation.cancel();
        }
        Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_out);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                zoomAnimation = null;
//                for (ImageButton button : karmaMenuSubOpts) {
//                    button.setVisibility(View.GONE);
//                }
                karmaMenu.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        zoomAnimation = anim;
        karmaMenu.setAnimation(zoomAnimation);
    }

    public void toggleUserMenu(final View view, final ChatPostData userChatPostData) {
        viewUserChatPostData = userChatPostData;
        karmaMenu.setVisibility(View.GONE);
        userMenuButton.setVisibility(View.VISIBLE);
        Point point = getMenuAnchor(view);
        userMenuButton.setX(point.x);
        userMenuButton.setY(point.y - (karmaRadius/2));
        userMenu.toggle(false);
        menuBackdrop.setVisibility(View.VISIBLE);
    }

    public void toggleKarmaMenu(final View view, final String id) {
        voteId = id;
        userMenu.close(false);
        userMenuButton.setVisibility(View.GONE);
        Point point = getMenuAnchor(view);
        int x = Math.min(maxOffset, (point.x - karmaRadius));
        karmaMenu.setX(x);
        karmaMenu.setY(point.y - karmaRadius);
        menuBackdrop.setVisibility(View.VISIBLE);
        zoomButtonsIn();
    }

    public void updateUserMenu(int position) {
        switch (position) {
            case 1:
                callbackListener.onViewUserProfile(viewUserChatPostData);
                break;
            case 2:
                callbackListener.onRequestFriendship(viewUserChatPostData);
                Toast.makeText(getContext(), "Invite Sent", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(getContext(), "Report Sent", Toast.LENGTH_SHORT).show();
                break;
            case 4:
                Toast.makeText(getContext(), "User Blocked", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private Point getMenuAnchor(final View view) {
        int[] coords = new int[2];
        view.getLocationOnScreen(coords);
        Point point = new Point(coords[0], coords[1]);
        point.x += view.getMeasuredWidth() / 2;
        point.y -= view.getMeasuredHeight() / 2;
        return point;
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private void closeMenus() {
        zoomButtonsOut();
        inputMenu.close(true);
        userMenu.close(false);
        userMenuButton.setVisibility(View.GONE);
        menuBackdrop.setVisibility(View.GONE);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Interface definition for callbacks
     */
    public interface CallbackListener extends NavigationFragmentCallback {
        void onNavigation(final int position);

        void onSubNavigation(final int position);

        void onViewUserProfile(final ChatPostData userChatPostData);

        void onRequestFriendship(final ChatPostData userChatPostData);
    }
}
