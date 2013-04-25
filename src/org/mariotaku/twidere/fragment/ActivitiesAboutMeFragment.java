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

package org.mariotaku.twidere.fragment;

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.mariotaku.twidere.util.Utils.formatSameDayTime;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.openUserFollowers;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.parseString;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.adapter.iface.IBaseAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.loader.ActivitiesAboutMeLoader;
import org.mariotaku.twidere.loader.Twitter4JActivitiesLoader;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.view.holder.ActivityViewHolder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.text.TextUtils;

public class ActivitiesAboutMeFragment extends PullToRefreshListFragment implements
		LoaderCallbacks<List<ParcelableActivity>> {

	private ActivitiesAdapter mAdapter;
	private SharedPreferences mPreferences;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new ActivitiesAdapter(getActivity());
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, getArguments(), this);
		setListShown(false);
	}

	@Override
	public Loader<List<ParcelableActivity>> onCreateLoader(final int id, final Bundle args) {
		setProgressBarIndeterminateVisibility(true);
		final long account_id = args != null ? args.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
		//final boolean is_home_tab = args != null ? args.getBoolean(INTENT_KEY_IS_HOME_TAB) : false;
		return new ActivitiesAboutMeLoader(getActivity(), account_id, null, null, 0);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final int adapter_pos = position - l.getHeaderViewsCount();
		final ParcelableActivity item = mAdapter.getItem(adapter_pos);
		if (item == null) return;
		final ParcelableUser[] sources = item.sources;
		final ParcelableStatus[] target_statuses = item.target_statuses;
		final int sources_length = sources != null ? sources.length : 0;
		if (sources_length > 0) {
			final ParcelableStatus[] target_objects = item.target_object_statuses;
			switch (item.action) {
				case ParcelableActivity.ACTION_FAVORITE: {
					if (sources_length == 1) {
						openUserProfile(getActivity(), sources[0]);
					} else {
						if (target_statuses != null && target_statuses.length > 0) {
							openStatus(getActivity(), target_statuses[0]);
						}
					}
					break;
				}
				case ParcelableActivity.ACTION_FOLLOW: {
					if (sources_length == 1) {
						openUserProfile(getActivity(), sources[0]);
					} else {
						openUserFollowers(getActivity(), item.account_id, item.account_id, null);
					}
					break;
				}
				case ParcelableActivity.ACTION_MENTION: {
					if (target_objects != null && target_objects.length > 0) {
						openStatus(getActivity(), target_objects[0]);
					}
					break;
				}
				case ParcelableActivity.ACTION_REPLY: {
					if (target_statuses != null && target_statuses.length > 0) {
						openStatus(getActivity(), target_statuses[0]);
					}
					break;
				}
				case ParcelableActivity.ACTION_RETWEET: {
					if (sources_length == 1) {
						openUserProfile(getActivity(), sources[0]);
					} else {
						if (target_objects != null && target_objects.length > 0) {
							openStatus(getActivity(), target_objects[0]);
						}
					}
					break;
				}
			}
		}
	}

	@Override
	public void onLoaderReset(final Loader<List<ParcelableActivity>> loader) {
		mAdapter.setData(null);
	}

	@Override
	public void onLoadFinished(final Loader<List<ParcelableActivity>> loader, final List<ParcelableActivity> data) {
		setProgressBarIndeterminateVisibility(false);
		mAdapter.setData(data);
		onRefreshComplete();
		setListShown(true);
	}

	@Override
	public void onPullDownToRefresh() {
		getLoaderManager().restartLoader(0, getArguments(), this);
	}

	@Override
	public void onPullUpToRefresh() {

	}

	@Override
	public void onResume() {
		super.onResume();
		final float text_size = mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean show_absolute_time = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_ABSOLUTE_TIME, false);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setTextSize(text_size);
		mAdapter.setShowAbsoluteTime(show_absolute_time);
	}

	static class ActivitiesAdapter extends ArrayAdapter<ParcelableActivity> implements IBaseAdapter {

		private boolean mDisplayProfileImage, mDisplayName, mShowAbsoluteTime;

		private float mTextSize;

		private final ImageLoaderWrapper mProfileImageLoader;
		private final Context mContext;

		public ActivitiesAdapter(final Context context) {
			super(context, R.layout.activity_list_item);
			mContext = context;
			final TwidereApplication application = TwidereApplication.getInstance(context);
			mProfileImageLoader = application.getImageLoaderWrapper();
		}

		@Override
		public long getItemId(final int position) {
			final Object obj = getItem(position);
			return obj != null ? obj.hashCode() : 0;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			final Object tag = view.getTag();
			final ActivityViewHolder holder = tag instanceof ActivityViewHolder ? (ActivityViewHolder) tag
					: new ActivityViewHolder(view);
			if (!(tag instanceof ActivityViewHolder)) {
				view.setTag(holder);
			}
			holder.reset();
			holder.setTextSize(mTextSize);
			final ParcelableActivity item = getItem(position);
			if (mShowAbsoluteTime) {
				holder.time.setText(formatSameDayTime(mContext, item.activity_timestamp));
			} else {
				holder.time.setText(getRelativeTimeSpanString(item.activity_timestamp));
			}
			final ParcelableUser[] sources = item.sources;
			final ParcelableStatus[] target_statuses = item.target_statuses;
			final int sources_length = sources != null ? sources.length : 0;
			final int target_statuses_length = target_statuses != null ? target_statuses.length : 0;
			final int action = item.action;
			holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (sources_length > 0) {
				final ParcelableUser first_source = sources[0];
				final ParcelableStatus[] target_objects = item.target_object_statuses;
				final String name = mDisplayName ? first_source.name : first_source.screen_name;
				switch (action) {
					case ParcelableActivity.ACTION_FAVORITE: {
						if (target_statuses_length > 0) {
							final ParcelableStatus status = target_statuses[0];
							holder.text.setSingleLine(true);
							holder.text.setEllipsize(TruncateAt.END);
							holder.text.setText(status.text_plain);
						}
						if (sources_length == 1) {
							holder.title.setText(mContext.getString(R.string.activity_about_me_favorite, name));
						} else {
							holder.title.setText(mContext.getString(R.string.activity_about_me_favorite_multi, name,
									sources_length - 1));
						}
						holder.activity_profile_image_container.setVisibility(mDisplayProfileImage ? View.VISIBLE
								: View.GONE);
						setUserProfileImages(sources, holder);
						break;
					}
					case ParcelableActivity.ACTION_FOLLOW: {
						holder.text.setVisibility(View.GONE);
						if (sources_length == 1) {
							holder.title.setText(mContext.getString(R.string.activity_about_me_follow, name));
						} else {
							holder.title.setText(mContext.getString(R.string.activity_about_me_follow_multi, name,
									sources_length - 1));
						}
						holder.activity_profile_image_container.setVisibility(mDisplayProfileImage ? View.VISIBLE
								: View.GONE);
						setUserProfileImages(sources, holder);
						break;
					}
					case ParcelableActivity.ACTION_MENTION: {
						holder.title.setText(name);
						if (target_objects != null && target_objects.length > 0) {
							final ParcelableStatus status = target_objects[0];
							holder.text.setText(status.text_plain);
							if (status.in_reply_to_status_id > 0 && !TextUtils.isEmpty(status.in_reply_to_screen_name)) {
								holder.reply_status.setVisibility(View.VISIBLE);
								holder.reply_status.setText(mContext.getString(R.string.in_reply_to,
										status.in_reply_to_screen_name));
								holder.reply_status.setCompoundDrawablesWithIntrinsicBounds(
										R.drawable.ic_indicator_reply, 0, 0, 0);
							}
						}
						setProfileImage(holder, first_source.profile_image_url);
						break;
					}
					case ParcelableActivity.ACTION_REPLY: {
						holder.title.setText(name);
						if (target_statuses_length > 0) {
							final ParcelableStatus status = target_statuses[0];
							holder.text.setText(status.text_plain);
							if (status.in_reply_to_status_id > 0 && !TextUtils.isEmpty(status.in_reply_to_screen_name)) {
								holder.reply_status.setVisibility(View.VISIBLE);
								holder.reply_status.setText(mContext.getString(R.string.in_reply_to,
										status.in_reply_to_screen_name));
								holder.reply_status.setCompoundDrawablesWithIntrinsicBounds(
										R.drawable.ic_indicator_reply, 0, 0, 0);
							}
						}
						setProfileImage(holder, first_source.profile_image_url);
						break;
					}
					case ParcelableActivity.ACTION_RETWEET: {
						if (target_objects != null && target_objects.length > 0) {
							final ParcelableStatus status = target_objects[0];
							holder.text.setSingleLine(true);
							holder.text.setEllipsize(TruncateAt.END);
							holder.text.setText(status.text_plain);
						}
						if (sources_length == 1) {
							holder.title.setText(mContext.getString(R.string.activity_about_me_retweet, name));
						} else {
							holder.title.setText(mContext.getString(R.string.activity_about_me_retweet_multi, name,
									sources_length - 1));
						}
						holder.activity_profile_image_container.setVisibility(mDisplayProfileImage ? View.VISIBLE
								: View.GONE);
						setUserProfileImages(sources, holder);
						break;
					}
					case ParcelableActivity.ACTION_LIST_MEMBER_ADDED: {
						holder.text.setVisibility(View.GONE);
						if (sources_length == 1) {
							holder.title
									.setText(mContext.getString(R.string.activity_about_me_list_member_added, name));
						} else {
							holder.title.setText(mContext.getString(R.string.activity_about_me_list_member_added_multi,
									name, sources_length - 1));
						}
						holder.activity_profile_image_container.setVisibility(mDisplayProfileImage ? View.VISIBLE
								: View.GONE);
						setUserProfileImages(sources, holder);
						break;
					}
				}
			}
			return view;
		}

		public void onItemSelected(final Object item) {
			notifyDataSetChanged();
		}

		public void onItemUnselected(final Object item) {
			notifyDataSetChanged();
		}

		public void setData(final List<ParcelableActivity> data) {
			clear();
			if (data == null) return;
			addAll(data);
		}

		@Override
		public void setDisplayProfileImage(final boolean display) {
			if (display != mDisplayProfileImage) {
				mDisplayProfileImage = display;
				notifyDataSetChanged();
			}
		}

		@Override
		public void setNameDisplayOption(final String option) {
			// TODO: Implement this method
		}

		public void setShowAbsoluteTime(final boolean show) {
			if (show != mShowAbsoluteTime) {
				mShowAbsoluteTime = show;
				notifyDataSetChanged();
			}
		}

		@Override
		public void setTextSize(final float text_size) {
			if (text_size != mTextSize) {
				mTextSize = text_size;
				notifyDataSetChanged();
			}
		}

		private void setProfileImage(final ActivityViewHolder holder, final String url) {
			if (mDisplayProfileImage) {
				mProfileImageLoader.displayProfileImage(holder.profile_image, url);
			} else {
				holder.profile_image.setImageDrawable(null);
			}
		}

		private void setUserProfileImages(final ParcelableUser[] users, final ActivityViewHolder holder) {
			final int length = Math.min(holder.activity_profile_images.length, users.length);
			for (int i = 0; i < length; i++) {
				final ImageView activity_profile_image = holder.activity_profile_images[i];
				if (mDisplayProfileImage) {
					mProfileImageLoader.displayProfileImage(activity_profile_image, users[i].profile_image_url);
				} else {
					activity_profile_image.setImageDrawable(null);
				}
			}
		}

	}

}
