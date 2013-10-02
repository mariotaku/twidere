/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;

/**
 * This is a fragment that will be used during transition from activities to
 * fragments.
 */
public abstract class ActivityHostFragment<A extends Activity> extends LocalActivityManagerFragment {

    private final static String ACTIVITY_TAG = "hosted";
    private A mAttachedActivity;

    public A getAttachedActivity() {
        return mAttachedActivity;
    }

    @SuppressWarnings({
            "deprecation", "unchecked"
    })
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final Intent intent = new Intent(getActivity(), getActivityClass());
        final Bundle args = getArguments();
        if (args != null) {
            intent.putExtras(args);
        }

        final Window w = getLocalActivityManager().startActivity(ACTIVITY_TAG, intent);
        mAttachedActivity = null;
        final Context context = w.getContext();
        if (context instanceof Activity) {
            try {
                mAttachedActivity = (A) context;
                if (context instanceof FragmentCallback) {
                    ((FragmentCallback<A>) context).setCallbackFragment(this);
                }
            } catch (final ClassCastException e) {
                // This should't happen.
                e.printStackTrace();
            }
        }
        final View wd = w != null ? w.getDecorView() : null;

        if (wd != null) {
            final ViewParent parent = wd.getParent();
            if (parent != null) {
                final ViewGroup v = (ViewGroup) parent;
                v.removeView(wd);
            }

            wd.setVisibility(View.VISIBLE);
            wd.setFocusableInTouchMode(true);
            if (wd instanceof ViewGroup) {
                ((ViewGroup) wd).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            }
        }
        return wd;
    }

    protected abstract Class<A> getActivityClass();

    public static interface FragmentCallback<A extends Activity> {

        void setCallbackFragment(ActivityHostFragment<A> fragment);

    }
}
