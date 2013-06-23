package com.handmark.pulltorefresh.library;

import java.util.HashSet;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.handmark.pulltorefresh.library.internal.LoadingLayout;

public class LoadingLayoutProxy implements ILoadingLayout {

	private final HashSet<LoadingLayout> mLoadingLayouts;

	LoadingLayoutProxy() {
		mLoadingLayouts = new HashSet<LoadingLayout>();
	}

	/**
	 * This allows you to add extra LoadingLayout instances to this proxy. This
	 * is only necessary if you keep your own instances, and want to have them
	 * included in any
	 * {@link PullToRefreshBase#createLoadingLayoutProxy(boolean, boolean)
	 * createLoadingLayoutProxy(...)} calls.
	 * 
	 * @param layout - LoadingLayout to have included.
	 */
	public void addLayout(final LoadingLayout layout) {
		if (null != layout) {
			mLoadingLayouts.add(layout);
		}
	}

	@Override
	public CharSequence getPullLabel() {
		for (final LoadingLayout layout : mLoadingLayouts)
			return layout.getPullLabel();
		return null;
	}

	@Override
	public CharSequence getRefreshingLabel() {
		for (final LoadingLayout layout : mLoadingLayouts)
			return layout.getRefreshingLabel();
		return null;
	}

	@Override
	public CharSequence getReleaseLabel() {
		for (final LoadingLayout layout : mLoadingLayouts)
			return layout.getReleaseLabel();
		return null;
	}

	@Override
	public void setLastUpdatedLabel(final CharSequence label) {
		for (final LoadingLayout layout : mLoadingLayouts) {
			layout.setLastUpdatedLabel(label);
		}
	}

	@Override
	public void setLoadingDrawable(final Drawable drawable) {
		for (final LoadingLayout layout : mLoadingLayouts) {
			layout.setLoadingDrawable(drawable);
		}
	}

	@Override
	public void setPullLabel(final CharSequence label) {
		for (final LoadingLayout layout : mLoadingLayouts) {
			layout.setPullLabel(label);
		}
	}

	@Override
	public void setRefreshingLabel(final CharSequence refreshingLabel) {
		for (final LoadingLayout layout : mLoadingLayouts) {
			layout.setRefreshingLabel(refreshingLabel);
		}
	}

	@Override
	public void setReleaseLabel(final CharSequence label) {
		for (final LoadingLayout layout : mLoadingLayouts) {
			layout.setReleaseLabel(label);
		}
	}

	@Override
	public void setTextTypeface(final Typeface tf) {
		for (final LoadingLayout layout : mLoadingLayouts) {
			layout.setTextTypeface(tf);
		}
	}
}
