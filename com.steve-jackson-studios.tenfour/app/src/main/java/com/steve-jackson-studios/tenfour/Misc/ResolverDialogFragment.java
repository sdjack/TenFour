package com.steve-jackson-studios.tenfour.Misc;

import android.app.DialogFragment;
import android.os.Handler;

import com.steve-jackson-studios.tenfour.AppResolver;

/**
 * Created by sjackson on 8/16/2017.
 * ResolverDialogFragment
 */

public class ResolverDialogFragment extends DialogFragment {

    protected final Handler handler = new Handler();

    protected AppResolver resolver;

    protected void setResolver(AppResolver appResolver) {
        this.resolver = appResolver;
    }
}
