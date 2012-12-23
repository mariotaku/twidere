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

import static org.mariotaku.twidere.util.Utils.getAccountScreenNames;

import java.util.ArrayList;
import java.util.Arrays;

import org.mariotaku.actionbarcompat.ActionMode;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ListUtils;
import org.mariotaku.twidere.util.NoDuplicatesLinkedList;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.twitter.Extractor;

@SuppressLint("Registered")
public class MultiSelectActivity extends DualPaneActivity implements ActionMode.Callback {

	private TwidereApplication mApplication;
	private AsyncTwitterWrapper mTwitterWrapper;

	private ActionMode mActionMode;

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_MULTI_SELECT_STATE_CHANGED.equals(action)) {
				updateMultiSelectState();
			} else if (BROADCAST_MULTI_SELECT_ITEM_CHANGED.equals(action)) {
				updateMultiSelectCount();
			}
		}

	};

	@Override
	public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
		final NoDuplicatesLinkedList<Object> selected_items = mApplication.getSelectedItems();
		final int count = selected_items.size();
		if (count < 1) return false;
		switch (item.getItemId()) {
			case MENU_REPLY: {
				final Extractor extractor = new Extractor();
				final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
				final Bundle bundle = new Bundle();
				final String[] account_names = getAccountScreenNames(this);
				final NoDuplicatesLinkedList<String> all_mentions = new NoDuplicatesLinkedList<String>();
				for (final Object object : selected_items) {
					if (object instanceof ParcelableStatus) {
						final ParcelableStatus status = (ParcelableStatus) object;
						all_mentions.add(status.screen_name);
						all_mentions.addAll(extractor.extractMentionedScreennames(status.text_plain));
					} else if (object instanceof ParcelableUser) {
						final ParcelableUser user = (ParcelableUser) object;
						all_mentions.add(user.screen_name);
					}
				}
				all_mentions.removeAll(Arrays.asList(account_names));
				final Object first_obj = selected_items.getFirst();
				if (first_obj instanceof ParcelableStatus) {
					final ParcelableStatus first_status = (ParcelableStatus) first_obj;
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, first_status.account_id);
					bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, first_status.status_id);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, first_status.screen_name);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, first_status.name);
				} else if (first_obj instanceof ParcelableUser) {
					final ParcelableUser first_user = (ParcelableUser) first_obj;
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, first_user.account_id);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, first_user.screen_name);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, first_user.name);
				}
				bundle.putStringArray(INTENT_KEY_MENTIONS, all_mentions.toArray(new String[all_mentions.size()]));
				intent.putExtras(bundle);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				mode.finish();
				break;
			}
			case MENU_MUTE_USER: {
				final ContentResolver resolver = getContentResolver();
				final Uri uri = Filters.Users.CONTENT_URI;
				final ArrayList<ContentValues> values_list = new ArrayList<ContentValues>();
				final NoDuplicatesLinkedList<String> names_list = new NoDuplicatesLinkedList<String>();
				for (final Object object : selected_items) {
					if (object instanceof ParcelableStatus) {
						final ParcelableStatus status = (ParcelableStatus) object;
						names_list.add(status.screen_name);
					} else if (object instanceof ParcelableUser) {
						final ParcelableUser user = (ParcelableUser) object;
						names_list.add(user.screen_name);
					} else {
						continue;
					}
				}
				resolver.delete(uri, Filters.Users.TEXT + " IN (" + ListUtils.toStringForSQL(names_list.size()) + ")",
						names_list.toArray(new String[names_list.size()]));
				for (final String screen_name : names_list) {
					final ContentValues values = new ContentValues();
					values.put(Filters.TEXT, screen_name);
					values_list.add(values);
				}
				resolver.bulkInsert(uri, values_list.toArray(new ContentValues[values_list.size()]));
				Toast.makeText(this, R.string.users_muted, Toast.LENGTH_SHORT).show();
				mode.finish();
				break;
			}
			case MENU_BLOCK: {
				final long account_id = getFirstSelectAccountId(selected_items);
				final long[] user_ids = getSelectedUserIds(selected_items);
				if (account_id > 0 && user_ids != null) {
					mTwitterWrapper.createMultiBlock(account_id, user_ids);
				}
				mode.finish();
				break;
			}
			case MENU_REPORT_SPAM: {
				final long account_id = getFirstSelectAccountId(selected_items);
				final long[] user_ids = getSelectedUserIds(selected_items);
				if (account_id > 0 && user_ids != null) {
					mTwitterWrapper.reportMultiSpam(account_id, user_ids);
				}
				mode.finish();
				break;
			}
		}
		return true;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		mApplication = getTwidereApplication();
		mTwitterWrapper = mApplication.getTwitterWrapper();
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
		new MenuInflater(this).inflate(R.menu.action_multi_select, menu);
		return true;
	}

	@Override
	public void onDestroyActionMode(final ActionMode mode) {
		mApplication.stopMultiSelect();
		mActionMode = null;
	}

	@Override
	public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_MULTI_SELECT_STATE_CHANGED);
		filter.addAction(BROADCAST_MULTI_SELECT_ITEM_CHANGED);
		registerReceiver(mStateReceiver, filter);

		updateMultiSelectState();
		updateMultiSelectCount();
	}

	@Override
	protected void onStop() {
		unregisterReceiver(mStateReceiver);
		super.onStop();
	}

	private void updateMultiSelectCount() {
		if (mActionMode == null) return;
		final int count = mApplication.getSelectedItems().size();
		if (count == 0) {
			mActionMode.finish();
			return;
		}
		mActionMode.setTitle(getResources().getQuantityString(R.plurals.Nitems_selected, count, count));
	}

	private void updateMultiSelectState() {
		if (mApplication.isMultiSelectActive()) {
			if (mActionMode == null) {
				mActionMode = startActionMode(this);
			}
		} else {
			if (mActionMode != null) {
				mActionMode.finish();
				mActionMode = null;
			}
		}
	}

	private static long getFirstSelectAccountId(final NoDuplicatesLinkedList<Object> selected_items) {
		final Object obj = selected_items.get(0);
		if (obj instanceof ParcelableUser)
			return ((ParcelableUser) obj).account_id;
		else if (obj instanceof ParcelableStatus) return ((ParcelableStatus) obj).account_id;
		return -1;
	}

	private static long[] getSelectedUserIds(final NoDuplicatesLinkedList<Object> selected_items) {
		final ArrayList<Long> ids_list = new ArrayList<Long>();
		for (final Object item : selected_items) {
			if (item instanceof ParcelableUser) {
				ids_list.add(((ParcelableUser) item).user_id);
			} else if (item instanceof ParcelableStatus) {
				ids_list.add(((ParcelableStatus) item).user_id);
			}
		}
		return ArrayUtils.fromList(ids_list);
	}

}
