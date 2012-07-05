package org.mariotaku.twidere.fragment;

import static android.support.v4.app.ListFragmentTrojan.INTERNAL_EMPTY_ID;
import static android.support.v4.app.ListFragmentTrojan.INTERNAL_LIST_CONTAINER_ID;
import static android.support.v4.app.ListFragmentTrojan.INTERNAL_PROGRESS_CONTAINER_ID;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class PullToRefreshListFragment extends BaseListFragment implements OnRefreshListener {

	private PullToRefreshListView mPullToRefreshListView;

	public final PullToRefreshListView getPullToRefreshListView() {
		return mPullToRefreshListView;
	}

	/**
	 * Provide default implementation to return a simple list view. Subclasses
	 * can override to replace with their own layout. If doing so, the returned
	 * view hierarchy <em>must</em> have a ListView whose id is
	 * {@link android.R.id#list android.R.id.list} and can optionally have a
	 * sibling view id {@link android.R.id#empty android.R.id.empty} that is to
	 * be shown when the list is empty.
	 * 
	 * <p>
	 * If you are overriding this method with your own custom content, consider
	 * including the standard layout {@link android.R.layout#list_content} in
	 * your layout file, so that you continue to retain all of the standard
	 * behavior of ListFragment. In particular, this is currently the only way
	 * to have the built-in indeterminant progress state be shown.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final Context context = getActivity();

		final FrameLayout root = new FrameLayout(context);

		// ------------------------------------------------------------------

		final LinearLayout pframe = new LinearLayout(context);
		pframe.setId(INTERNAL_PROGRESS_CONTAINER_ID);
		pframe.setOrientation(LinearLayout.VERTICAL);
		pframe.setVisibility(View.GONE);
		pframe.setGravity(Gravity.CENTER);

		final ProgressBar progress = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
		pframe.addView(progress, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		root.addView(pframe, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));

		// ------------------------------------------------------------------

		final FrameLayout lframe = new FrameLayout(context);
		lframe.setId(INTERNAL_LIST_CONTAINER_ID);

		final TextView tv = new TextView(getActivity());
		tv.setId(INTERNAL_EMPTY_ID);
		tv.setGravity(Gravity.CENTER);
		lframe.addView(tv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));

		final PullToRefreshListView plv = new PullToRefreshListView(context);
		plv.setOnRefreshListener(this);
		mPullToRefreshListView = plv;

		final ListView lv = plv.getRefreshableView();
		lv.setId(android.R.id.list);
		lv.setDrawSelectorOnTop(false);
		lframe.addView(plv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));

		root.addView(lframe, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));

		// ------------------------------------------------------------------

		root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));

		return root;
	}

	@Override
	public void onRefresh() {

	}

	/**
	 * Mark the current Refresh as complete. Will Reset the UI and hide the
	 * Refreshing View
	 */
	public final void onRefreshComplete() {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.onRefreshComplete();
	}

	/**
	 * Set the drawable used in the loading layout. This is the same as calling
	 * <code>setLoadingDrawable(drawable, Mode.BOTH)</code>
	 * 
	 * @param drawable - Drawable to display
	 */
	public final void setLoadingDrawable(Drawable drawable) {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.setLoadingDrawable(drawable);
	}

	/**
	 * Set the drawable used in the loading layout.
	 * 
	 * @param drawable - Drawable to display
	 * @param mode - Controls which Header/Footer Views will be updated.
	 *            <code>Mode.BOTH</code> will update all available, other values
	 *            will update the relevant View.
	 */
	public final void setLoadingDrawable(Drawable drawable, Mode mode) {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.setLoadingDrawable(drawable, mode);
	}

	/**
	 * Set Text to show when the Widget is being Pulled
	 * 
	 * @param pullLabel - String to display
	 * @param mode - Controls which Header/Footer Views will be updated.
	 *            <code>Mode.BOTH</code> will update all available, other values
	 *            will update the relevant View.
	 */
	public void setPullLabel(String pullLabel, Mode mode) {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.setPullLabel(pullLabel);
	}

	/**
	 * A mutator to enable/disable Pull-to-Refresh for the current View
	 * 
	 * @param enable Whether Pull-To-Refresh should be used
	 */
	public final void setPullToRefreshEnabled(boolean enable) {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.setPullToRefreshEnabled(enable);
	}

	/**
	 * Sets the Widget to be in the refresh State. The UI will be updated to
	 * show the 'Refreshing' view.
	 * 
	 * @param doScroll - true if you want to force a scroll to the Refreshing
	 *            view.
	 */
	public final void setRefreshing(boolean doScroll) {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.setRefreshing(doScroll);
	}

	/**
	 * Set Text to show when the Widget is refreshing
	 * <code>setRefreshingLabel(releaseLabel, Mode.BOTH)</code>
	 * 
	 * @param releaseLabel - String to display
	 */
	public void setRefreshingLabel(String refreshingLabel) {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.setRefreshingLabel(refreshingLabel);
	}

	/**
	 * Set Text to show when the Widget is refreshing
	 * 
	 * @param refreshingLabel - String to display
	 * @param mode - Controls which Header/Footer Views will be updated.
	 *            <code>Mode.BOTH</code> will update all available, other values
	 *            will update the relevant View.
	 */
	public void setRefreshingLabel(String refreshingLabel, Mode mode) {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.setRefreshingLabel(refreshingLabel, mode);
	}

	/**
	 * Set Text to show when the Widget is being pulled, and will refresh when
	 * released. This is the same as calling
	 * <code>setReleaseLabel(releaseLabel, Mode.BOTH)</code>
	 * 
	 * @param releaseLabel - String to display
	 */
	public void setReleaseLabel(String releaseLabel) {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.setReleaseLabel(releaseLabel);
	}

	/**
	 * Set Text to show when the Widget is being pulled, and will refresh when
	 * released
	 * 
	 * @param releaseLabel - String to display
	 * @param mode - Controls which Header/Footer Views will be updated.
	 *            <code>Mode.BOTH</code> will update all available, other values
	 *            will update the relevant View.
	 */
	public void setReleaseLabel(String releaseLabel, Mode mode) {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.setReleaseLabel(releaseLabel, mode);
	}

	/**
	 * Sets whether an indicator graphic should be displayed when the View is in
	 * a state where a Pull-to-Refresh can happen. An example of this state is
	 * when the Adapter View is scrolled to the top and the mode is set to
	 * {@link Mode#PULL_DOWN_TO_REFRESH}
	 * 
	 * @param showIndicator - true if the indicators should be shown.
	 */
	public final void setShowIndicator(boolean showIndicator) {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.setShowIndicator(showIndicator);
	}

	/**
	 * A mutator to enable/disable whether the 'Refreshing' View should be
	 * automatically shown when refreshing.
	 * 
	 * @param showView
	 */
	public final void setShowViewWhileRefreshing(boolean showView) {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.setShowViewWhileRefreshing(showView);
	}
}
