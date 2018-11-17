package com.steve-jackson-studios.tenfour.Misc;

import android.app.Fragment;
import android.os.Handler;

import com.steve-jackson-studios.tenfour.AppResolver;

/**
 * Created by sjackson on 8/25/2017.
 * MediaResolverFragment
 */

public abstract class MediaResolverFragment extends Fragment {

    protected final Handler handler = new Handler();

    protected AppResolver resolver;

    protected void setResolver(AppResolver appResolver) {
        this.resolver = appResolver;
    }

    public void refresh() {}
}
