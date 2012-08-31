package org.mariotaku.twidere.activity;

import java.util.ArrayList;

import org.mariotaku.actionbarcompat.ActionMode;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.util.ListUtils;
import org.mariotaku.twidere.util.NoDuplicatesList;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.twitter.Extractor;

public class MultiSelectActivity extends DualPaneActivity implements ActionMode.Callback {

	private TwidereApplication mApplication;

	private ActionMode mActionMode;

	private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_MULTI_SELECT_STATE_CHANGED.equals(action)) {
				updateMultiSelectState();
			} else if (BROADCAST_MULTI_SELECT_ITEM_CHANGED.equals(action)) {
				updateMultiSelectCount();
			}
		}

	};

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		final ArrayList<Object> selected_items = mApplication.getSelectedItems();
		switch (item.getItemId()) {
			case MENU_REPLY: {
				final Extractor extractor = new Extractor();
				final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
				final Bundle bundle = new Bundle();
				final NoDuplicatesList<String> all_mentions = new NoDuplicatesList<String>();
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
				bundle.putStringArray(INTENT_KEY_MENTIONS, all_mentions.toArray(new String[all_mentions.size()]));
				intent.putExtras(bundle);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				break;
			}
			case MENU_MUTE_USER: {
				final ContentResolver resolver = getContentResolver();
				final Uri uri = Filters.Users.CONTENT_URI;
				final SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFERENCES_NAME,
						Context.MODE_PRIVATE).edit();
				final ArrayList<ContentValues> values_list = new ArrayList<ContentValues>();
				final NoDuplicatesList<String> names_list = new NoDuplicatesList<String>();
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
				resolver.delete(uri, Filters.Users.TEXT + " IN (" + ListUtils.toStringForSQL(names_list) + ")", null);
				for (final String screen_name : names_list) {
					final ContentValues values = new ContentValues();
					values.put(Filters.TEXT, screen_name);
					values_list.add(values);
				}
				resolver.bulkInsert(uri, values_list.toArray(new ContentValues[values_list.size()]));
				editor.putBoolean(PREFERENCE_KEY_ENABLE_FILTER, true).commit();
				Toast.makeText(this, R.string.users_muted, Toast.LENGTH_SHORT).show();
				break;
			}
			case MENU_BLOCK: {
				break;
			}
			case MENU_REPORT_SPAM: {
				break;
			}
		}
		if (item.getItemId() != R.id.more_submenu) {
			if (mode != null) {
				mode.finish();
			}
		}
		return true;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mApplication = getTwidereApplication();
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		new MenuInflater(this).inflate(R.menu.action_multi_select, menu);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		mApplication.stopMultiSelect();
		mActionMode = null;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
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
		if (mActionMode != null) {
			final int count = mApplication.getSelectedItems().size();
			mActionMode.setTitle(getResources().getQuantityString(R.plurals.Nitems_selected, count, count));
		}
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

}