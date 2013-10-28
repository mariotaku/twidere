package org.mariotaku.twidere.fragment.iface;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public interface PullToRefreshAttacherActivity {

	public void addRefreshingState(IBasePullToRefreshFragment fragment);

	public PullToRefreshAttacher getPullToRefreshAttacher();

	public boolean isRefreshing(IBasePullToRefreshFragment fragment);

	public void setPullToRefreshEnabled(final IBasePullToRefreshFragment fragment, final boolean enabled);

	public void setRefreshComplete(final IBasePullToRefreshFragment fragment);

	public void setRefreshing(final IBasePullToRefreshFragment fragment, final boolean refreshing);
}