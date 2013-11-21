package org.mariotaku.twidere.fragment.iface;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public interface IBasePullToRefreshFragment {

	public PullToRefreshLayout getPullToRefreshLayout();

	public boolean isRefreshing();

	public void onRefreshStarted();

	public void setPullToRefreshEnabled(boolean enabled);

	public void setRefreshComplete();

	public void setRefreshing(boolean refreshing);

	boolean isPullToRefreshEnabled();
}
