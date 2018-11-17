package com.steve-jackson-studios.tenfour.Profile;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.AppResolver;
import com.steve-jackson-studios.tenfour.Data.UserData;
import com.steve-jackson-studios.tenfour.Media.GlideApp;
import com.steve-jackson-studios.tenfour.Misc.ResolverFragment;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;
import com.steve-jackson-studios.tenfour.R;
import com.steve-jackson-studios.tenfour.Widgets.EditTextButton;

/**
 * Created by sjackson on 5/3/2017.
 * ProfileFragment
 */

public class ProfileFragment extends ResolverFragment {

    private TextView userField;
    private TextView homeTownField;
    private TextView statusField;
    private EditText statusInput;
    private LinearLayout avatarContainer;
    private ImageView avatarImage;
    private TextView avatarText;
    private Button menuButton;
    private EditTextButton etb;
    private RelativeLayout menuLayout;
    private ProfilePagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private FriendsFragment friendsFragment;
    private FriendsNearbyFragment requestsFragment;

    public static ProfileFragment newInstance(AppResolver appResolver) {

        ProfileFragment instance = new ProfileFragment();
        instance.setResolver(appResolver);

        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.profile_layout, container, false);

        menuLayout = (RelativeLayout) view.findViewById(R.id.profile_menu);

        menuButton = (Button) view.findViewById(R.id.profile_menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("menuButton", "Clicked");
                if (menuLayout.isShown()) {
                    menuLayout.setVisibility(View.GONE);
                } else {
                    menuLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        userField = (TextView) view.findViewById(R.id.profile_name);
        userField.setText(UserData.USERNAME);

        homeTownField = (TextView) view.findViewById(R.id.profile_hometown);
        homeTownField.setText(UserData.HOMETOWN);

        statusField = (TextView) view.findViewById(R.id.profile_status);
        statusField.setText(UserData.STATUS);

        statusInput = (EditText) view.findViewById(R.id.profile_status_edittext);

        etb = (EditTextButton) view.findViewById(R.id.profile_status_container);
        etb.setClosingAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                hideKeyboard();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                statusField.setText(UserData.STATUS);
                statusInput.clearFocus();
                hideKeyboard();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        etb.setOpeningAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                statusInput.setText(UserData.STATUS);
                statusInput.requestFocus();
                showKeyboard();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        statusInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    etb.animateClosed();
                }
            }
        });

        Button loginButton = (Button) view.findViewById(R.id.profile_status_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etb.animateClosed();
                String newStatus = statusInput.getText().toString();
                resolver.saveProfileStatus(newStatus);
            }
        });

        LinearLayout statusWrapper = (LinearLayout) view.findViewById(R.id.profile_status_wrapper);
        statusWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etb.animateClosed();
            }
        });

        avatarContainer = (LinearLayout) view.findViewById(R.id.profile_avatar);
        avatarText = (TextView) view.findViewById(R.id.profile_avatar_type0);
        avatarImage = (ImageView) view.findViewById(R.id.profile_avatar_type1);
        avatarContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAvatarSelector();
            }
        });
        Button editButton = (Button) view.findViewById(R.id.profile_menu_edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuButtonClick(R.id.profile_menu_edit);
            }
        });

        Button filtersButton = (Button) view.findViewById(R.id.profile_menu_settings);
        filtersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuButtonClick(R.id.profile_menu_settings);
            }
        });

        Button aboutButton = (Button) view.findViewById(R.id.profile_menu_about);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuButtonClick(R.id.profile_menu_about);
            }
        });

        Button helpButton = (Button) view.findViewById(R.id.profile_menu_help);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuButtonClick(R.id.profile_menu_help);
            }
        });

        Button logoutButton = (Button) view.findViewById(R.id.profile_menu_logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuButtonClick(R.id.profile_menu_logout);
            }
        });

        friendsFragment = FriendsFragment.newInstance(resolver);
        requestsFragment = FriendsNearbyFragment.newInstance(resolver);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.profile_tabs);
        tabLayout.addTab(tabLayout.newTab().setText("FRIENDS"));
        tabLayout.addTab(tabLayout.newTab().setText("NEAR ME"));
        tabLayout.setTabTextColors(
                ContextCompat.getColor(getContext(), R.color.white_faded),
                ContextCompat.getColor(getContext(), R.color.white)
        );

        pagerAdapter = new ProfilePagerAdapter(activity.getFragmentManager());
        viewPager = (ViewPager) view.findViewById(R.id.profile_pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        updateAvatar();

        return view;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void updateAvatar() {
        if (UserData.AVATAR_TYPE == 1) {
            avatarText.setVisibility(View.GONE);
            avatarImage.setVisibility(View.VISIBLE);
            GlideApp.with(getActivity())
                    .load(UserData.AVATAR_URI)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.icon_avatar_default)
                    .fitCenter()
                    .into(avatarImage);
        } else {
            avatarImage.setVisibility(View.GONE);
            avatarText.setVisibility(View.VISIBLE);
            avatarText.setText(UserData.INITIALS);
            avatarText.getBackground().setColorFilter(UserData.AVATAR_COLOR, PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void launchEditUpdate() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                getFragmentManager()
                        .beginTransaction()
                        .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .add(R.id.appView, ProfileEditDialog.newInstance(resolver))
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void launchAvatarSelector() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                getFragmentManager()
                        .beginTransaction()
                        .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .add(R.id.appView, ProfileSelectDialog.newInstance(resolver))
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public void refresh() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                userField.setText(UserData.USERNAME);
                statusField.setText(UserData.STATUS);
                homeTownField.setText(UserData.HOMETOWN);
                updateAvatar();
            }
        });
        friendsFragment.refresh();
        //requestsFragment.refresh();
    }

    private void loadMenuItem(int layout, String title, String content, String link) {
        AppConstants.ACTIVE_MENU_LAYOUT = layout;
        AppConstants.ACTIVE_MENU_TITLE = title;
        AppConstants.ACTIVE_MENU_CONTENT = content;
        AppConstants.ACTIVE_MENU_LINK = link;
        getFragmentManager()
                .beginTransaction()
                .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .add(R.id.appView, UserMenuDialog.newInstance(resolver))
                .addToBackStack(null)
                .commit();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public boolean onMenuButtonClick(int itemId) {
        switch (itemId) {
            case R.id.profile_menu_about:
                loadMenuItem(0, getString(R.string.about_title), getString(R.string.about_content), getString(R.string.about_link));
                return true;
            case R.id.profile_menu_help:
                loadMenuItem(0, getString(R.string.help_title), getString(R.string.help_content), getString(R.string.help_link));
                return true;
            case R.id.profile_menu_settings:
                loadMenuItem(R.layout.user_settings_layout, getString(R.string.settings_title), getString(R.string.settings_content), getString(R.string.settings_link));
                return true;
            case R.id.profile_menu_logout:
                Dispatch.triggerEvent(ObservedEvents.USER_LOGOUT);
                //openLogin();
                return true;
            case R.id.profile_menu_edit:
                launchEditUpdate();
                return true;
            default:
                return false;
        }
    }

    public boolean isEditing() {
        return etb.isOpened();
    }

    public void endAllEditing() {
        etb.animateClosed();
        statusInput.clearFocus();
        hideKeyboard();
    }

    public class ProfilePagerAdapter extends FragmentStatePagerAdapter {

        private String[] titles = {"FRIENDS", "NEAR ME" };

        public ProfilePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch(i){
                case 0:
                    friendsFragment.refresh();
                    return friendsFragment;
                case 1:
                    //requestsFragment.refresh();
                    return requestsFragment;
                default:
                    friendsFragment.refresh();
                    return friendsFragment;
            }
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}

