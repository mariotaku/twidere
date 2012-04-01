package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.ComposeActivity;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.widget.RefreshableListView;
import org.mariotaku.twidere.widget.RefreshableListView.OnRefreshListener;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class HomeTabFragment extends SherlockListFragment implements Constants, OnRefreshListener,
		LoaderCallbacks<Cursor> {

	private StatusesAdapter mAdapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		mAdapter = new StatusesAdapter(getSherlockActivity(), R.layout.tweet_list_item, null, null,
				null, 0);
		((RefreshableListView) getListView()).setOnRefreshListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] cols = TweetStore.Statuses.COLUMNS;
		Uri uri = TweetStore.Statuses.CONTENT_URI;
		return new CursorLoader(getSherlockActivity(), uri, cols, null, null, null);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.timeline, null, false);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
		setListAdapter(null);

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		Log.d("debug", "cols = " + data.getColumnCount());

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.compose:
				startActivity(new Intent(getSherlockActivity(), ComposeActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRefresh() {

	}

	private class StatusesAdapter extends SimpleCursorAdapter {

		public StatusesAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to,
				int flags) {
			super(context, layout, cursor, from, to, flags);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			ViewHolder viewholder = (ViewHolder) view.getTag();

			if (viewholder == null) return;

			String user_name = cursor.getString(cursor
					.getColumnIndexOrThrow(TweetStore.Statuses.USER_NAME));
			String screen_name = cursor.getString(cursor
					.getColumnIndexOrThrow(TweetStore.Statuses.SCREEN_NAME));

			viewholder.user_name.setText(user_name);
			viewholder.screen_name.setText(screen_name);

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

			View view = super.newView(context, cursor, parent);
			ViewHolder viewholder = new ViewHolder(view);
			view.setTag(viewholder);
			return view;
		}

		private class ViewHolder {

			TextView user_name;
			TextView screen_name;
			ImageView profile_image;

			public ViewHolder(View view) {
				user_name = (TextView) view.findViewById(R.id.user_name);
				screen_name = (TextView) view.findViewById(R.id.screen_name);
				profile_image = (ImageView) view.findViewById(R.id.profile_image);
			}

		}

	}
}
