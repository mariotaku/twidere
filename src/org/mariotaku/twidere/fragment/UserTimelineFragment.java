package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getMentionedNames;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;

import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.loader.UserTimelineLoader;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ParcelableStatus;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.StatusViewHolder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class UserTimelineFragment extends BaseListFragment implements LoaderCallbacks<List<ParcelableStatus>>,
		OnItemClickListener, OnItemLongClickListener, ActionMode.Callback {

	private SharedPreferences mPreferences;
	private ServiceInterface mServiceInterface;
	private ListView mListView;
	private ParcelableStatusesAdapter mAdapter;

	private boolean mDisplayProfileImage, mDisplayName;
	private boolean mLoadMoreAutomatically, mNotReachedBottomBefore = true;
	private ParcelableStatus mSelectedStatus;
	private float mTextSize;

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		if (mSelectedStatus != null) {
			long status_id = mSelectedStatus.status_id;
			String text_plain = mSelectedStatus.text_plain;
			String screen_name = mSelectedStatus.screen_name;
			String name = mSelectedStatus.name;
			long account_id = mSelectedStatus.account_id;
			switch (item.getItemId()) {
				case MENU_SHARE: {
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_TEXT, "@" + screen_name + ": " + text_plain);
					startActivity(Intent.createChooser(intent, getString(R.string.share)));
					break;
				}
				case MENU_RETWEET: {
					mServiceInterface.retweetStatus(new long[] { account_id }, status_id);
					break;
				}
				case MENU_QUOTE: {
					Intent intent = new Intent(INTENT_ACTION_COMPOSE);
					Bundle bundle = new Bundle();
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
					bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, status_id);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, screen_name);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, name);
					bundle.putBoolean(INTENT_KEY_IS_QUOTE, true);
					bundle.putString(INTENT_KEY_TEXT, "RT @" + screen_name + ": " + text_plain);
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				}
				case MENU_REPLY: {
					Intent intent = new Intent(INTENT_ACTION_COMPOSE);
					Bundle bundle = new Bundle();
					bundle.putStringArray(INTENT_KEY_MENTIONS, getMentionedNames(screen_name, text_plain, false, true));
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
					bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, status_id);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, screen_name);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, name);
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				}
				case MENU_FAV: {
					if (mSelectedStatus.is_favorite) {
						mServiceInterface.destroyFavorite(new long[] { account_id }, status_id);
					} else {
						mServiceInterface.createFavorite(new long[] { account_id }, status_id);
					}
					break;
				}
				case MENU_DELETE: {
					mServiceInterface.destroyStatus(account_id, status_id);
					break;
				}
				default:
					return false;
			}
		}
		mode.finish();
		return true;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSherlockActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		getSherlockActivity().getContentResolver();
		mDisplayProfileImage = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mDisplayName = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		mTextSize = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mServiceInterface = ((TwidereApplication) getSherlockActivity().getApplication()).getServiceInterface();
		mDisplayProfileImage = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mDisplayName = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		LazyImageLoader imageloader = ((TwidereApplication) getSherlockActivity().getApplication())
				.getListProfileImageLoader();
		mAdapter = new ParcelableStatusesAdapter(getSherlockActivity(), imageloader);
		mListView = getListView();
		setListAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, getArguments(), this);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.action_status, menu);
		return true;
	}

	@Override
	public Loader<List<ParcelableStatus>> onCreateLoader(int id, Bundle args) {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
		if (args != null) {
			long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			long user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
			if (user_id == -1 && screen_name != null)
				return new UserTimelineLoader(getSherlockActivity(), account_id, screen_name);
			return new UserTimelineLoader(getSherlockActivity(), account_id, user_id);
		}
		return null;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {

	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		Object tag = view.getTag();
		if (tag instanceof StatusViewHolder) {
			ParcelableStatus status = mAdapter.findItem(id);
			StatusViewHolder holder = (StatusViewHolder) tag;
			if (holder.show_as_gap || position == adapter.getCount() - 1 && !mLoadMoreAutomatically) {
				// getStatuses(new long[] { status.account_id }, new long[] {
				// status.status_id });
			} else {
				Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_STATUS);
				builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(status.account_id));
				builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status.status_id));
				Intent intent = new Intent(Intent.ACTION_DEFAULT, builder.build());
				Bundle bundle = new Bundle();
				bundle.putParcelable(INTENT_KEY_STATUS, status);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		Object tag = view.getTag();
		if (tag instanceof StatusViewHolder) {
			StatusViewHolder holder = (StatusViewHolder) tag;
			if (holder.show_as_gap) return false;
			mSelectedStatus = mAdapter.findItem(id);
			getSherlockActivity().startActionMode(this);
			return true;
		}
		return false;
	}

	@Override
	public void onLoaderReset(Loader<List<ParcelableStatus>> loader) {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onLoadFinished(Loader<List<ParcelableStatus>> loader, List<ParcelableStatus> data) {
		mAdapter.clear();
		if (data != null) {
			for (ParcelableStatus status : data) {
				mAdapter.add(status);
			}
		}
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		setMenuForStatus(getSherlockActivity(), menu, mSelectedStatus);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setDisplayName(display_name);
		mAdapter.setStatusesTextSize(text_size);
		if (mDisplayProfileImage != display_profile_image || mDisplayName != display_name || mTextSize != text_size) {
			mDisplayProfileImage = display_profile_image;
			mDisplayName = display_name;
			mTextSize = text_size;
			mListView.invalidateViews();
		}
	}

}
