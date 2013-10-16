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

package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.getAccountColors;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.querybuilder.Columns.Column;
import org.mariotaku.querybuilder.RawItemArray;
import org.mariotaku.querybuilder.Where;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.DraftItem;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.view.AccountsColorFrameLayout;

import java.util.ArrayList;
import java.util.List;

public class DraftsActivity extends TwidereSwipeBackActivity implements LoaderCallbacks<Cursor>, OnItemClickListener,
		MultiChoiceModeListener {

	private ContentResolver mResolver;
	private SharedPreferences mPreferences;

	private DraftsAdapter mAdapter;
	private ListView mListView;

	private PopupMenu mPopupMenu;

	private float mTextSize;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_DRAFTS_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, DraftsActivity.this);
			}
		}
	};

	@Override
	public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_DELETE: {
				// TODO confim dialog and image removal
				final Where where = Where.in(new Column(Drafts._ID), new RawItemArray(mListView.getCheckedItemIds()));
				mResolver.delete(Drafts.CONTENT_URI, where.getSQL(), null);
				break;
			}
			case MENU_SEND: {
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				if (twitter == null) return false;
				final Cursor c = mAdapter.getCursor();
				if (c == null || c.isClosed()) return false;
				final SparseBooleanArray checked = mListView.getCheckedItemPositions();
				final List<ParcelableStatusUpdate> list = new ArrayList<ParcelableStatusUpdate>();
				for (int i = 0, j = checked.size(); i < j; i++) {
					if (checked.valueAt(i)) {
						list.add(new ParcelableStatusUpdate(new DraftItem(c, checked.keyAt(i))));
					}
				}
				twitter.updateStatusesAsync(list.toArray(new ParcelableStatusUpdate[list.size()]));
				final Where where = Where.in(new Column(Drafts._ID), new RawItemArray(mListView.getCheckedItemIds()));
				mResolver.delete(Drafts.CONTENT_URI, where.getSQL(), null);
				break;
			}
			default: {
				return false;
			}
		}
		mode.finish();
		return true;
	}

	@Override
	public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
		getMenuInflater().inflate(R.menu.action_multi_select_drafts, menu);
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		final Uri uri = Drafts.CONTENT_URI;
		final String[] cols = Drafts.COLUMNS;
		return new CursorLoader(this, uri, cols, null, null, null);
	}

	@Override
	public void onDestroyActionMode(final ActionMode mode) {

	}

	@Override
	public void onItemCheckedStateChanged(final ActionMode mode, final int position, final long id,
			final boolean checked) {
		updateTitle(mode);
	}

	@Override
	public void onItemClick(final AdapterView<?> view, final View child, final int position, final long id) {
		final Cursor c = mAdapter.getCursor();
		if (c == null || c.isClosed()) return;
		editDraft(new DraftItem(c, position));
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				onBackPressed();
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
		updateTitle(mode);
		return true;
	}

	@Override
	public void onStart() {
		final IntentFilter filter = new IntentFilter(BROADCAST_DRAFTS_DATABASE_UPDATED);
		registerReceiver(mStatusReceiver, filter);
		final AsyncTwitterWrapper twitter = getTwitterWrapper();
		if (twitter != null) {
			twitter.clearNotification(NOTIFICATION_ID_DRAFTS);
		}
		super.onStart();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mResolver = getContentResolver();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mTextSize = mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, getDefaultTextSize(this));
		setContentView(android.R.layout.list_content);
		setOverrideExitAniamtion(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mAdapter = new DraftsAdapter(this);
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setDivider(null);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		mListView.setMultiChoiceModeListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		final float text_size = mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, getDefaultTextSize(this));
		mAdapter.setTextSize(text_size);
		if (mTextSize != text_size) {
			mTextSize = text_size;
			mListView.invalidateViews();
		}
	}

	@Override
	protected void onStop() {
		unregisterReceiver(mStatusReceiver);
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
	}

	private void editDraft(final DraftItem draft) {
		final Intent intent = new Intent(INTENT_ACTION_EDIT_DRAFT);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(EXTRA_DRAFT, draft);
		intent.putExtras(bundle);
		mResolver.delete(Drafts.CONTENT_URI, Drafts._ID + " = " + draft._id, null);
		startActivityForResult(intent, REQUEST_COMPOSE);
	}

	private void updateTitle(final ActionMode mode) {
		if (mListView == null || mode == null) return;
		final int count = mListView.getCheckedItemCount();
		mode.setTitle(getResources().getQuantityString(R.plurals.Nitems_selected, count, count));
	}

	static class DraftsAdapter extends SimpleCursorAdapter {

		private final ImageLoaderWrapper mImageLoader;
		private final ImageLoadingHandler mImageLoadingHandler;

		private float mTextSize;
		private int mAccountIdsIdx, mTextIdx, mImageUriIdx;

		public DraftsAdapter(final Context context) {
			super(context, R.layout.draft_list_item, null, new String[0], new int[0], 0);
			mImageLoader = TwidereApplication.getInstance(context).getImageLoaderWrapper();
			mImageLoadingHandler = new ImageLoadingHandler();
		}

		@Override
		public void bindView(final View view, final Context context, final Cursor cursor) {
			final long[] account_ids = ArrayUtils.parseLongArray(cursor.getString(mAccountIdsIdx), ',');
			final String content = cursor.getString(mTextIdx);
			final String image_uri = cursor.getString(mImageUriIdx);
			final TextView text = (TextView) view.findViewById(R.id.text);
			final ImageView image_preview = (ImageView) view.findViewById(R.id.image_preview);
			final AccountsColorFrameLayout account_colors = (AccountsColorFrameLayout) view
					.findViewById(R.id.accounts_color);
			account_colors.setColors(getAccountColors(context, account_ids));
			text.setTextSize(mTextSize);
			final boolean empty_content = TextUtils.isEmpty(content);
			if (empty_content) {
				text.setText(R.string.empty_content);
			} else {
				text.setText(content);
			}
			text.setTypeface(Typeface.DEFAULT, empty_content ? Typeface.ITALIC : Typeface.NORMAL);
			final View image_preview_container = view.findViewById(R.id.image_preview_container);
			image_preview_container.setVisibility(TextUtils.isEmpty(image_uri) ? View.GONE : View.VISIBLE);
			if (!TextUtils.isEmpty(image_uri) && !image_uri.equals(mImageLoadingHandler.getLoadingUri(image_preview))) {
				mImageLoader.displayPreviewImage(image_preview, image_uri, mImageLoadingHandler);
			}
		}

		public void setTextSize(final float text_size) {
			mTextSize = text_size;
		}

		@Override
		public Cursor swapCursor(final Cursor c) {
			final Cursor old = super.swapCursor(c);
			if (c != null) {
				mAccountIdsIdx = c.getColumnIndex(Drafts.ACCOUNT_IDS);
				mTextIdx = c.getColumnIndex(Drafts.TEXT);
				mImageUriIdx = c.getColumnIndex(Drafts.IMAGE_URI);
			}
			return old;
		}
	}
}
