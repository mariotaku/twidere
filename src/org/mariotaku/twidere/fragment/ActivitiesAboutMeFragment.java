package org.mariotaku.twidere.fragment;

import static android.text.format.DateUtils.formatSameDayTime;
import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.parseString;
import static org.mariotaku.twidere.util.Utils.parseURL;

import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.loader.ActivitiesAboutMeLoader;
import org.mariotaku.twidere.loader.Twitter4JActivitiesLoader;
import org.mariotaku.twidere.model.ActivityViewHolder;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.BaseAdapterInterface;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.Utils;

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
		LoaderCallbacks<List<twitter4j.Activity>> {

	private ActivitiesAdapter mAdapter;
	private SharedPreferences mPreferences;
	private List<twitter4j.Activity> mData;
	private long mAccountId;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new ActivitiesAdapter(getActivity());
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, getArguments(), this);
		setListShown(false);
	}

	@Override
	public Loader<List<twitter4j.Activity>> onCreateLoader(int id, Bundle args) {
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
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mAccountId <= 0) return;
		final int adapter_pos = position - l.getHeaderViewsCount();
		final twitter4j.Activity item = mAdapter.getItem(adapter_pos);
		final User[] sources = item.getSources();
		final Status[] target_statuses = item.getTargetStatuses();
		final int sources_length = sources != null ? sources.length : 0;
		final Action action = item.getAction();
		if (sources_length > 0) {
			final User first_source = sources[0];
			final Status[] target_objects = item.getTargetObjects();
			switch (action.getActionId()) {
				case Action.ACTION_FAVORITE: {
					if (sources_length == 1) {
						Utils.openUserProfile(getActivity(), mAccountId, first_source.getId(),
								first_source.getScreenName());
					} else {
						if (target_statuses != null && target_statuses.length > 0) {
							final Status status = target_statuses[0];
							Utils.openUserRetweetedStatus(getActivity(), mAccountId, status.getId());
						}
					}
					break;
				}
				case Action.ACTION_FOLLOW: {
					if (sources_length == 1) {
						Utils.openUserProfile(getActivity(), mAccountId, first_source.getId(),
								first_source.getScreenName());
					} else {
						Utils.openUserFollowers(getActivity(), mAccountId, mAccountId, null);
					}
					break;
				}
				case Action.ACTION_MENTION: {
					if (target_objects != null && target_objects.length > 0) {
						final Status status = target_objects[0];
						Utils.openStatus(getActivity(), new ParcelableStatus(status, mAccountId, false));
					}
					break;
				}
				case Action.ACTION_REPLY: {
					if (target_statuses != null && target_statuses.length > 0) {
						final Status status = target_statuses[0];
						Utils.openStatus(getActivity(), new ParcelableStatus(status, mAccountId, false));
					}
					break;
				}
				case Action.ACTION_RETWEET: {
					if (sources_length == 1) {
						Utils.openUserProfile(getActivity(), mAccountId, first_source.getId(),
								first_source.getScreenName());
					} else {
						if (target_objects != null && target_objects.length > 0) {
							final Status status = target_objects[0];
							Utils.openUserRetweetedStatus(getActivity(), mAccountId, status.getId());
						}
					}
					break;
				}
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<List<twitter4j.Activity>> loader) {
		mAdapter.setData(null);
		mData = null;
	}

	@Override
	public void onLoadFinished(Loader<List<twitter4j.Activity>> loader, List<twitter4j.Activity> data) {
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
		final float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean hires_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_HIRES_PROFILE_IMAGE, false);
		final boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		final boolean show_absolute_time = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_ABSOLUTE_TIME, false);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setDisplayHiResProfileImage(hires_profile_image);
		mAdapter.setDisplayName(display_name);
		mAdapter.setTextSize(text_size);
		mAdapter.setShowAbsoluteTime(show_absolute_time);
	}

	static class ActivitiesAdapter extends BaseAdapter implements BaseAdapterInterface {

		private boolean mDisplayProfileImage, mDisplayHiResProfileImage, mDisplayName, mShowAccountColor,
				mShowAbsoluteTime;
		private float mTextSize;

		private final LazyImageLoader mProfileImageLoader;
		private final LayoutInflater mInflater;
		private final Context mContext;
		private List<twitter4j.Activity> mData;

		public ActivitiesAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			mContext = context;
			final TwidereApplication application = TwidereApplication.getInstance(context);
			mProfileImageLoader = application.getProfileImageLoader();
		}

		@Override
		public int getCount() {
			return mData != null ? mData.size() : 0;
		}

		@Override
		public twitter4j.Activity getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			final Object obj = getItem(position);
			return obj != null ? obj.hashCode() : 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = convertView != null ? convertView : mInflater.inflate(R.layout.activity_list_item, null);
			final Object tag = view.getTag();
			final ActivityViewHolder holder = tag instanceof ActivityViewHolder ? (ActivityViewHolder) tag
					: new ActivityViewHolder(view);
			if (!(tag instanceof ActivityViewHolder)) {
				view.setTag(holder);
			}
			holder.reset();
			holder.setTextSize(mTextSize);
			final twitter4j.Activity item = getItem(position);
			final Date created_at = item.getCreatedAt();
			if (created_at != null) {
				if (mShowAbsoluteTime) {
					holder.time.setText(formatSameDayTime(created_at.getTime(), System.currentTimeMillis(),
							DateFormat.MEDIUM, DateFormat.SHORT));
				} else {
					holder.time.setText(getRelativeTimeSpanString(created_at.getTime()));
				}
			}
			final User[] sources = item.getSources();
			final Status[] target_statuses = item.getTargetStatuses();
			final int sources_length = sources != null ? sources.length : 0;
			final int target_statuses_length = target_statuses != null ? target_statuses.length : 0;
			final Action action = item.getAction();
			holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (sources_length > 0) {
				final User first_source = sources[0];
				final Status[] target_objects = item.getTargetObjects();
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
				}
			}
			return view;
		}

		public void setData(List<twitter4j.Activity> data) {
			mData = data != null ? data : new ArrayList<twitter4j.Activity>();
			notifyDataSetChanged();
		}

		@Override
		public void setDisplayHiResProfileImage(boolean display) {
			if (display != mDisplayHiResProfileImage) {
				mDisplayHiResProfileImage = display;
				notifyDataSetChanged();
			}
		}

		@Override
		public void setDisplayName(boolean display) {
			if (display != mDisplayName) {
				mDisplayName = display;
				notifyDataSetChanged();
			}
		}

		@Override
		public void setDisplayProfileImage(boolean display) {
			if (display != mDisplayProfileImage) {
				mDisplayProfileImage = display;
				notifyDataSetChanged();
			}
		}

		public void setShowAbsoluteTime(boolean show) {
			if (show != mShowAbsoluteTime) {
				mShowAbsoluteTime = show;
				notifyDataSetChanged();
			}
		}

		@Override
		public void setTextSize(float text_size) {
			if (text_size != mTextSize) {
				mTextSize = text_size;
				notifyDataSetChanged();
			}
		}

		private void setProfileImage(URL url, ActivityViewHolder holder) {
			if (!mDisplayProfileImage) return;
			if (mDisplayHiResProfileImage) {
				mProfileImageLoader.displayImage(parseURL(getBiggerTwitterProfileImage(parseString(url))),
						holder.profile_image);
			} else {
				mProfileImageLoader.displayImage(parseURL(parseString(url)), holder.profile_image);
			}
		}

		private void setUserProfileImages(User[] users, ActivityViewHolder holder) {
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
					mProfileImageLoader.displayImage(
							parseURL(getBiggerTwitterProfileImage(parseString(user.getProfileImageURL()))),
							activity_profile_image);
				} else {
					mProfileImageLoader.displayImage(user.getProfileImageURL(), activity_profile_image);
				}
			}
		}

	}

}
