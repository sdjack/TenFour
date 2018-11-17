package com.steve-jackson-studios.tenfour.Misc;


import android.app.Fragment;
import android.app.ListFragment;
import android.os.Handler;

import com.steve-jackson-studios.tenfour.AppResolver;

/**
 * Created by sjackson on 8/16/2017.
 * ResolverListFragment
 */

public abstract class ResolverListFragment extends ListFragment {

    protected final Handler handler = new Handler();

    protected AppResolver resolver;

    protected void setResolver(AppResolver appResolver) {
        this.resolver = appResolver;
    }

    public void refresh() {}
}