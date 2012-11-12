package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.activity.FiltersActivity;
import org.mariotaku.twidere.model.Panes;

import android.os.Bundle;

public class FiltersListFragment extends ActivityHostFragment<FiltersActivity> implements Panes.Right {

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	protected Class<FiltersActivity> getActivityClass() {
		return FiltersActivity.class;
	}
}
