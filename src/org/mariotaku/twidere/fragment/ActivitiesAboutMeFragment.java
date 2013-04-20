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
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.view.holder.ActivityViewHolder;

import twitter4j.Activity;
import twitter4j.Activity.Action;
import twitter4j.Status;
import twitter4j.User;
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

public class ActivitiesAboutMeFragment extends PullToRefreshListFragment implements
		LoaderCallbacks<List<Activity>> {

	private ActivitiesAdapter mAdapter;
	private SharedPreferences mPreferences;
	private List<Activity> mData;
	private long mAccountId;

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
	public Loader<List<Activity>> onCreateLoader(final int id, final Bundle args) {
		setProgressBarIndeterminateVisibility(true);
		final long account_id = mAccountId = args != null ? args.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
		final boolean is_home_tab = args != null ? args.getBoolean(INTENT_KEY_IS_HOME_TAB) : false;
		return new ActivitiesAboutMeLoader(getActivity(), account_id, mData, getClass().getSimpleName(), is_home_tab);
	}

	@Override
	public void onDestroy() {
		Twitter4JActivitiesLoader.writeSerializableStatuses(this, getActivity(), mData, getArguments());
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		Twitter4JActivitiesLoader.writeSerializableStatuses(this, getActivity(), mData, getArguments());
		super.onDestroyView();
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		if (mAccountId <= 0) return;
		final int adapter_pos = position - l.getHeaderViewsCount();
		final Activity item = mAdapter.getItem(adapter_pos);
		final User[] sources = item.getSources();
		final Status[] target_statuses = item.getTargetStatuses();
		final int sources_length = sources != null ? sources.length : 0;
		final Action action = item.getAction();
		final boolean hires_profile_image = getResources().getBoolean(R.bool.hires_profile_image);
		if (sources_length > 0) {
			final Status[] target_objects = item.getTargetObjectStatuses();
			switch (action.getActionId()) {
				case Action.ACTION_FAVORITE: {
					if (sources_length == 1) {
						openUserProfile(getActivity(), new ParcelableUser(sources[0], mAccountId, hires_profile_image));
					} else {
						if (target_statuses != null && target_statuses.length > 0) {
							final Status status = target_statuses[0];
							openStatus(getActivity(), new ParcelableStatus(status, mAccountId, false,
									hires_profile_image));
						}
					}
					break;
				}
				case Action.ACTION_FOLLOW: {
					if (sources_length == 1) {
						openUserProfile(getActivity(), new ParcelableUser(sources[0], mAccountId, hires_profile_image));
					} else {
						openUserFollowers(getActivity(), mAccountId, mAccountId, null);
					}
					break;
				}
				case Action.ACTION_MENTION: {
					if (target_objects != null && target_objects.length > 0) {
						final Status status = target_objects[0];
						openStatus(getActivity(), new ParcelableStatus(status, mAccountId, false, hires_profile_image));
					}
					break;
				}
				case Action.ACTION_REPLY: {
					if (target_statuses != null && target_statuses.length > 0) {
						final Status status = target_statuses[0];
						openStatus(getActivity(), new ParcelableStatus(status, mAccountId, false, hires_profile_image));
					}
					break;
				}
				case Action.ACTION_RETWEET: {
					if (sources_length == 1) {
						openUserProfile(getActivity(), new ParcelableUser(sources[0], mAccountId, hires_profile_image));
					} else {
						if (target_objects != null && target_objects.length > 0) {
							final Status status = target_objects[0];
							openStatus(getActivity(), new ParcelableStatus(status, mAccountId, false,
									hires_profile_image));
						}
					}
					break;
				}
			}
		}
	}

	@Override
	public void onLoaderReset(final Loader<List<Activity>> loader) {
		mAdapter.setData(null);
		mData = null;
	}

	@Override
	public void onLoadFinished(final Loader<List<Activity>> loader, final List<Activity> data) {
		setProgressBarIndeterminateVisibility(false);
		mAdapter.setData(data);
		mData = data;
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

	static class ActivitiesAdapter extends ArrayAdapter<Activity> implements IBaseAdapter {

		private boolean mDisplayProfileImage, mDisplayName, mShowAbsoluteTime;

		private final boolean mDisplayHiResProfileImage;

		private float mTextSize;

		private final ImageLoaderWrapper mProfileImageLoader;
		private final Context mContext;

		public ActivitiesAdapter(final Context context) {
			super(context, R.layout.activity_list_item);
			mContext = context;
			final TwidereApplication application = TwidereApplication.getInstance(context);
			mProfileImageLoader = application.getImageLoaderWrapper();
			mDisplayHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
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
			final Object item = getItem(position);
			if (!(item instanceof Activity)) return view;
			final Activity activity = (Activity) item;
			final Date created_at = activity.getCreatedAt();
			if (created_at != null) {
				if (mShowAbsoluteTime) {
					holder.time.setText(formatSameDayTime(mContext, created_at.getTime()));
				} else {
					holder.time.setText(getRelativeTimeSpanString(created_at.getTime()));
				}
			}
			final User[] sources = activity.getSources();
			final Status[] target_statuses = activity.getTargetStatuses();
			final int sources_length = sources != null ? sources.length : 0;
			final int target_statuses_length = target_statuses != null ? target_statuses.length : 0;
			final Action action = activity.getAction();
			holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (sources_length > 0) {
				final User first_source = sources[0];
				final Status[] target_objects = activity.getTargetObjectStatuses();
				final String name = mDisplayName ? first_source.getName() : first_source.getScreenName();
				switch (action.getActionId()) {
					case Action.ACTION_FAVORITE: {
						if (target_statuses_length > 0) {
							final Status status = target_statuses[0];
							holder.text.setSingleLine(true);
							holder.text.setEllipsize(TruncateAt.END);
							holder.text.setText(status.getText());
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
					case Action.ACTION_FOLLOW: {
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
					case Action.ACTION_MENTION: {
						holder.title.setText(name);
						if (target_objects != null && target_objects.length > 0) {
							final Status status = target_objects[0];
							holder.text.setText(status.getText());
							if (status.getInReplyToStatusId() > 0 && status.getInReplyToScreenName() != null) {
								holder.reply_status.setVisibility(View.VISIBLE);
								holder.reply_status.setText(mContext.getString(R.string.in_reply_to,
										status.getInReplyToScreenName()));
								holder.reply_status.setCompoundDrawablesWithIntrinsicBounds(
										R.drawable.ic_indicator_reply, 0, 0, 0);
							}
						}
						setProfileImage(first_source.getProfileImageURL(), holder);
						break;
					}
					case Action.ACTION_REPLY: {
						holder.title.setText(name);
						if (target_statuses_length > 0) {
							final Status status = target_statuses[0];
							holder.text.setText(status.getText());
							if (status.getInReplyToStatusId() > 0 && status.getInReplyToScreenName() != null) {
								holder.reply_status.setVisibility(View.VISIBLE);
								holder.reply_status.setText(mContext.getString(R.string.in_reply_to,
										status.getInReplyToScreenName()));
								holder.reply_status.setCompoundDrawablesWithIntrinsicBounds(
										R.drawable.ic_indicator_reply, 0, 0, 0);
							}
						}
						setProfileImage(first_source.getProfileImageURL(), holder);
						break;
					}
					case Action.ACTION_RETWEET: {
						if (target_objects != null && target_objects.length > 0) {
							final Status status = target_objects[0];
							holder.text.setSingleLine(true);
							holder.text.setEllipsize(TruncateAt.END);
							holder.text.setText(status.getText());
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
					case Action.ACTION_LIST_MEMBER_ADDED: {
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

		public void setData(final List<Activity> data) {
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

		private void setProfileImage(final URL url, final ActivityViewHolder holder) {
			if (!mDisplayProfileImage) return;
			if (mDisplayHiResProfileImage) {
				mProfileImageLoader.displayProfileImage(holder.profile_image,
						getBiggerTwitterProfileImage(parseString(url)));
			} else {
				mProfileImageLoader.displayProfileImage(holder.profile_image, parseString(url));
			}
		}

		private void setUserProfileImages(final User[] users, final ActivityViewHolder holder) {
			if (!mDisplayProfileImage) return;
			final int length = users.length;
			for (int i = 0; i < length; i++) {
				if (i > 4) {
					break;
				}
				final User user = users[i];
				final ImageView activity_profile_image;
				switch (i) {
					case 0: {
						activity_profile_image = holder.activity_profile_image_1;
						break;
					}
					case 1: {
						activity_profile_image = holder.activity_profile_image_2;
						break;
					}
					case 2: {
						activity_profile_image = holder.activity_profile_image_3;
						break;
					}
					case 3: {
						activity_profile_image = holder.activity_profile_image_4;
						break;
					}
					case 4: {
						activity_profile_image = holder.activity_profile_image_5;
						break;
					}
					default: {
						activity_profile_image = null;
					}
				}
				if (mDisplayHiResProfileImage) {
					mProfileImageLoader.displayProfileImage(activity_profile_image,
							getBiggerTwitterProfileImage(parseString(user.getProfileImageURL())));
				} else {
					mProfileImageLoader.displayProfileImage(activity_profile_image,
							parseString(user.getProfileImageURL()));
				}
			}
		}

	}

}
