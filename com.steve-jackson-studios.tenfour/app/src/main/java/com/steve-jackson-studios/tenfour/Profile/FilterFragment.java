package com.steve-jackson-studios.tenfour.Profile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.steve-jackson-studios.tenfour.AppResolver;
import com.steve-jackson-studios.tenfour.Data.UserData;
import com.steve-jackson-studios.tenfour.Misc.ResolverFragment;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;
import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 4/19/2018.
 *
 */

public class FilterFragment extends ResolverFragment {

    private Switch filterButton1;
    private Switch filterButton2;
    private Switch filterButton3;
    private SharedPreferences preferences;

    public static FilterFragment newInstance(AppResolver appResolver) {

        FilterFragment instance = new FilterFragment();
        instance.setResolver(appResolver);

        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity context = getActivity();
        View view = inflater.inflate(R.layout.filter_layout, container, false);

        preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        filterButton1 = (Switch) view.findViewById(R.id.filter_option_1);
        filterButton1.setChecked(UserData.HEATMAP_ENABLED);
        filterButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = filterButton1.isChecked();
                UserData.HEATMAP_ENABLED = enabled;
                saveUserFilter("FilterShowHeatmap", enabled);
            }
        });

        filterButton2 = (Switch) view.findViewById(R.id.filter_option_2);
        filterButton2.setChecked(UserData.FRIENDMAP_ENABLED);
        filterButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = filterButton2.isChecked();
                UserData.FRIENDMAP_ENABLED = enabled;
                saveUserFilter("FilterShowFriendmap", enabled);
            }
        });

        filterButton3 = (Switch) view.findViewById(R.id.filter_option_3);
        filterButton3.setChecked(UserData.FRIENDMAP_ENABLED);
        filterButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = filterButton3.isChecked();
                UserData.STICKERS_ENABLED = enabled;
                saveUserFilter("FilterShowStickers", enabled);
            }
        });

        return view;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public synchronized void saveUserFilter(String key, boolean value) {

        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, value);
            editor.apply();
            Dispatch.triggerEvent(ObservedEvents.NOTIFY_FILTERS_CHANGED);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}
