package uk.co.senab.actionbarpulltorefresh.library.viewdelegates;

import android.view.View;

import com.origamilabs.library.views.StaggeredGridView;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.ViewDelegate;

public class StaggeredGridViewDelegate extends ViewDelegate {

	public static final Class<StaggeredGridView> SUPPORTED_VIEW_CLASS = StaggeredGridView.class;

	@Override
	public boolean isScrolledToTop(final View view) {
		final StaggeredGridView gridView = (StaggeredGridView) view;
		if (gridView.getItemCount() == 0)
			return true;
		else if (gridView.getFirstPosition() == 0) {
			final View firstVisibleChild = gridView.getChildAt(0);
			return firstVisibleChild != null && firstVisibleChild.getTop() >= 0;
		}
		return false;
	}

}
