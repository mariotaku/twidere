package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.activity.NativeMapActivity;

import android.app.Activity;

public class NativeMapFragment extends ActivityHostFragment {
    
    @Override
    protected Class<? extends Activity> getActivityClass() {
        return NativeMapActivity.class;
    }
}
