package org.mariotaku.twidere.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.Loader;
import java.util.List;
import org.mariotaku.twidere.loader.BaseCursorSupportUsersLoader;
import org.mariotaku.twidere.model.ParcelableUser;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;

public abstract class CursorSupportUsersListFragment extends BaseUsersListFragment {

	private long mNextCursor, mPrevCursor;

	public void onActivityCreated(final Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mNextCursor = savedInstanceState.getLong(INTENT_KEY_NEXT_CURSOR, -1);
			mPrevCursor = savedInstanceState.getLong(INTENT_KEY_PREV_CURSOR, -1);
		} else {			
			mNextCursor = -1;
			mPrevCursor = -1;
		}
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mNextCursor = -1;
		mPrevCursor = -1;
	}

	@Override
	public void onLoaderReset(final Loader<List<ParcelableUser>> loader) {
		super.onLoaderReset(loader);
		mNextCursor = -1;
		mPrevCursor = -1;
	}

	@Override
	public void onLoadFinished(final Loader<List<ParcelableUser>> loader, final List<ParcelableUser> data) {
		super.onLoadFinished(loader, data);
		final BaseCursorSupportUsersLoader c_loader = (BaseCursorSupportUsersLoader) loader;
		mNextCursor = c_loader.getNextCursor();
		mPrevCursor = c_loader.getPrevCursor();
		setMode(mNextCursor > 0 ? Mode.PULL_UP_TO_REFRESH : Mode.DISABLED);
	}
	
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(INTENT_KEY_NEXT_CURSOR, mNextCursor);
		outState.putLong(INTENT_KEY_PREV_CURSOR, mPrevCursor);
	}
	
	protected abstract BaseCursorSupportUsersLoader newLoaderInstance(final Context context, final Bundle args);

	protected final long getNextCursor() {
		return mNextCursor;
	}

	protected final long getPrevCursor() {
		return mPrevCursor;
	}
}
