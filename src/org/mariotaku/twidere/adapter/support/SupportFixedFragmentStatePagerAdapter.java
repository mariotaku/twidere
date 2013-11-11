package org.mariotaku.twidere.adapter.support;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTrojan;
import android.view.ViewGroup;

public abstract class SupportFixedFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

	public SupportFixedFragmentStatePagerAdapter(final FragmentManager fm) {
		super(fm);
	}

	@Override
	public Object instantiateItem(final ViewGroup container, final int position) {
		final Fragment f = (Fragment) super.instantiateItem(container, position);
		final Bundle savedFragmentState = f != null ? FragmentTrojan.getSavedFragmentState(f) : null;
		if (savedFragmentState != null) {
			savedFragmentState.setClassLoader(f.getClass().getClassLoader());
		}
		return f;
	}

}
