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

import android.app.Fragment;
import android.app.LocalActivityManager;
import android.os.Bundle;

/**
 * This is a fragment that will be used during transition from activities to
 * fragments.
 */
@SuppressWarnings("deprecation")
public class LocalActivityManagerFragment extends Fragment {

    private static final String KEY_STATE_BUNDLE = "localActivityManagerState";

    private LocalActivityManager mLocalActivityManager;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle state = null;
        if (savedInstanceState != null) {
            state = savedInstanceState.getBundle(KEY_STATE_BUNDLE);
        }

        mLocalActivityManager = new LocalActivityManager(getActivity(), true);
        mLocalActivityManager.dispatchCreate(state);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocalActivityManager.dispatchDestroy(getActivity().isFinishing());
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocalActivityManager.dispatchPause(getActivity().isFinishing());
    }

    @Override
    public void onResume() {
        super.onResume();
        mLocalActivityManager.dispatchResume();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(KEY_STATE_BUNDLE, mLocalActivityManager.saveInstanceState());
    }

    @Override
    public void onStop() {
        super.onStop();
        mLocalActivityManager.dispatchStop();
    }

    protected LocalActivityManager getLocalActivityManager() {
        return mLocalActivityManager;
    }
}
