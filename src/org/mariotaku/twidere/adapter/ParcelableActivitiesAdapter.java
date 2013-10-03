/*
 * 				Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.adapter;

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.mariotaku.twidere.util.Utils.configBaseAdapter;
import static org.mariotaku.twidere.util.Utils.formatSameDayTime;
import static org.mariotaku.twidere.util.Utils.getNameDisplayOptionInt;

import android.content.Context;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.view.holder.ActivityViewHolder;

import java.util.List;

public class ParcelableActivitiesAdapter extends ArrayAdapter<ParcelableActivity> implements IBaseCardAdapter {

	private final Context mContext;
	private final MultiSelectManager mMultiSelectManager;
	private final ImageLoaderWrapper mProfileImageLoader;

	private boolean mDisplayProfileImage, mShowAbsoluteTime;
	private int mNameDisplayOption;
	private float mTextSize;

	private MenuButtonClickListener mListener;

	public ParcelableActivitiesAdapter(final Context context) {
		super(context, R.layout.activity_list_item);
		mContext = context;
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mMultiSelectManager = app.getMultiSelectManager();
		mProfileImageLoader = app.getImageLoaderWrapper();
		configBaseAdapter(context, this);
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
		if (isMyActivity(item)) {
			showActivityAboutMe(holder, item);
		} else {
			showActivityByFriends(holder, item);
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
	public void setMenuButtonClickListener(final MenuButtonClickListener listener) {
		mListener = listener;
	}

	@Override
	public void setNameDisplayOption(final String option) {
		final int option_int = getNameDisplayOptionInt(option);
		if (option_int == mNameDisplayOption) return;
		mNameDisplayOption = option_int;
		notifyDataSetChanged();
	}

	@Override
	public void setNicknameOnly(final boolean nickname_only) {
		// TODO Auto-generated method stub

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

	private String getName(final ParcelableStatus status) {
		if (status == null) return null;
		return mNameDisplayOption == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME ? status.user_screen_name : status.user_name;
	}

	private String getName(final ParcelableUser user) {
		if (user == null) return null;
		return mNameDisplayOption == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME ? user.screen_name : user.name;
	}

	private boolean isMyActivity(final ParcelableActivity activity) {
		if (activity == null) return false;
		final ParcelableStatus[] target_statuses = activity.target_statuses;
		final ParcelableUser[] target_users = activity.target_users;
		final ParcelableStatus[] target_object_statuses = activity.target_object_statuses;
		final int target_statuses_length = target_statuses != null ? target_statuses.length : 0;
		final int target_users_length = target_users != null ? target_users.length : 0;
		final int target_object_statuses_length = target_object_statuses != null ? target_object_statuses.length : 0;
		if (activity.action == ParcelableActivity.ACTION_FAVORITE)
			return target_statuses_length > 0 && target_statuses[0].user_id == activity.account_id;
		else if (activity.action == ParcelableActivity.ACTION_FOLLOW)
			return target_users_length > 0 && target_users[0].id == activity.account_id;
		else if (activity.action == ParcelableActivity.ACTION_LIST_MEMBER_ADDED)
			return target_users_length > 0 && target_users[0].id == activity.account_id;
		else if (activity.action == ParcelableActivity.ACTION_MENTION)
			return true;
		else if (activity.action == ParcelableActivity.ACTION_REPLY)
			return true;
		else if (activity.action == ParcelableActivity.ACTION_RETWEET)
			return target_object_statuses_length > 0 && target_object_statuses[0].user_id == activity.account_id;
		else if (activity.action == ParcelableActivity.ACTION_LIST_CREATED) return false;
		return false;
	}

	private void setProfileImage(final ActivityViewHolder holder, final ParcelableUser user) {
		if (mDisplayProfileImage) {
			mProfileImageLoader.displayProfileImage(holder.profile_image, user.profile_image_url);
		} else {
			holder.profile_image.setImageDrawable(null);
		}
	}

	private void setUserProfileImages(final ActivityViewHolder holder, final ParcelableStatus[] statuses) {
		final int length = statuses != null ? Math.min(holder.activity_profile_images.length, statuses.length) : 0;
		final boolean should_display_images = mDisplayProfileImage && length > 0;
		holder.activity_profile_image_container.setVisibility(should_display_images ? View.VISIBLE : View.GONE);
		if (!should_display_images) return;
		for (int i = 0; i < length; i++) {
			final ImageView activity_profile_image = holder.activity_profile_images[i];
			final String profile_image_url = statuses[i].user_profile_image_url;
			mProfileImageLoader.displayProfileImage(activity_profile_image, profile_image_url);
		}
	}

	private void setUserProfileImages(final ActivityViewHolder holder, final ParcelableUser[] users) {
		final int length = users != null ? Math.min(holder.activity_profile_images.length, users.length) : 0;
		final boolean should_display_images = mDisplayProfileImage && length > 0;
		holder.activity_profile_image_container.setVisibility(should_display_images ? View.VISIBLE : View.GONE);
		if (!should_display_images) return;
		for (int i = 0; i < length; i++) {
			final ImageView activity_profile_image = holder.activity_profile_images[i];
			final String profile_image_url = users[i].profile_image_url;
			mProfileImageLoader.displayProfileImage(activity_profile_image, profile_image_url);
		}
	}

	private void showActivityAboutMe(final ActivityViewHolder holder, final ParcelableActivity activity) {
		if (activity == null) return;
		final ParcelableUser[] sources = activity.sources;
		final ParcelableStatus[] target_statuses = activity.target_statuses;
		final int sources_length = sources != null ? sources.length : 0;
		final int target_statuses_length = target_statuses != null ? target_statuses.length : 0;
		final int action = activity.action;
		holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		if (sources_length > 0) {
			final ParcelableUser first_source = sources[0];
			final ParcelableStatus[] target_objects = activity.target_object_statuses;
			final String source_name = mNameDisplayOption == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME ? first_source.screen_name
					: first_source.name;
			switch (action) {
				case ParcelableActivity.ACTION_FAVORITE: {
					if (target_statuses_length > 0) {
						final ParcelableStatus status = target_statuses[0];
						holder.text.setSingleLine(true);
						holder.text.setEllipsize(TruncateAt.END);
						holder.text.setText(status.text_plain);
					}
					if (sources_length == 1) {
						holder.title.setText(mContext.getString(R.string.activity_about_me_favorite, source_name));
					} else {
						holder.title.setText(mContext.getString(R.string.activity_about_me_favorite_multi, source_name,
								sources_length - 1));
					}
					setUserProfileImages(holder, sources);
					break;
				}
				case ParcelableActivity.ACTION_FOLLOW: {
					holder.text.setVisibility(View.GONE);
					if (sources_length == 1) {
						holder.title.setText(mContext.getString(R.string.activity_about_me_follow, source_name));
					} else {
						holder.title.setText(mContext.getString(R.string.activity_about_me_follow_multi, source_name,
								sources_length - 1));
					}
					setUserProfileImages(holder, sources);
					break;
				}
				case ParcelableActivity.ACTION_MENTION: {
					holder.title.setText(source_name);
					if (target_objects != null && target_objects.length > 0) {
						final ParcelableStatus status = target_objects[0];
						holder.text.setText(status.text_plain);
						if (status.in_reply_to_status_id > 0 && !TextUtils.isEmpty(status.in_reply_to_screen_name)) {
							holder.reply_status.setVisibility(View.VISIBLE);
							holder.reply_status.setText(mContext.getString(R.string.in_reply_to,
									status.in_reply_to_screen_name));
							holder.reply_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_reply,
									0, 0, 0);
						}
					}
					setProfileImage(holder, first_source);
					break;
				}
				case ParcelableActivity.ACTION_REPLY: {
					holder.title.setText(source_name);
					if (target_statuses_length > 0) {
						final ParcelableStatus status = target_statuses[0];
						holder.text.setText(status.text_plain);
						if (status.in_reply_to_status_id > 0 && !TextUtils.isEmpty(status.in_reply_to_screen_name)) {
							holder.reply_status.setVisibility(View.VISIBLE);
							holder.reply_status.setText(mContext.getString(R.string.in_reply_to,
									status.in_reply_to_screen_name));
							holder.reply_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_reply,
									0, 0, 0);
						}
					}
					setProfileImage(holder, first_source);
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
						holder.title.setText(mContext.getString(R.string.activity_about_me_retweet, source_name));
					} else {
						holder.title.setText(mContext.getString(R.string.activity_about_me_retweet_multi, source_name,
								sources_length - 1));
					}
					setUserProfileImages(holder, sources);
					break;
				}
				case ParcelableActivity.ACTION_LIST_MEMBER_ADDED: {
					holder.text.setVisibility(View.GONE);
					if (sources_length == 1) {
						holder.title.setText(mContext.getString(R.string.activity_about_me_list_member_added,
								source_name));
					} else {
						holder.title.setText(mContext.getString(R.string.activity_about_me_list_member_added_multi,
								source_name, sources_length - 1));
					}
					setUserProfileImages(holder, sources);
					break;
				}
			}
		}
	}

	private void showActivityByFriends(final ActivityViewHolder holder, final ParcelableActivity activity) {
		if (activity == null) return;
		final ParcelableUser[] sources = activity.sources;
		final ParcelableStatus[] target_statuses = activity.target_statuses;
		final ParcelableUser[] target_users = activity.target_users;
		final ParcelableStatus[] target_object_statuses = activity.target_object_statuses;
		final ParcelableUserList[] target_user_lists = activity.target_user_lists;
		final ParcelableUserList[] target_object_user_lists = activity.target_object_user_lists;
		final int sources_length = sources != null ? sources.length : 0;
		final int target_statuses_length = target_statuses != null ? target_statuses.length : 0;
		final int target_users_length = target_users != null ? target_users.length : 0;
		final int target_object_user_lists_length = target_object_user_lists != null ? target_object_user_lists.length
				: 0;
		final int target_user_lists_length = target_user_lists != null ? target_user_lists.length : 0;
		final int action = activity.action;
		holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		if (sources_length > 0) {
			final ParcelableUser first_source = sources[0];
			final String first_source_name = getName(first_source);
			switch (action) {
				case ParcelableActivity.ACTION_FAVORITE: {
					if (target_statuses_length == 0) return;
					final ParcelableStatus first_status = target_statuses[0];
					if (target_statuses_length == 1) {
						holder.text.setSingleLine(true);
						holder.text.setEllipsize(TruncateAt.END);
						holder.text.setText(first_status.text_plain);
						holder.title.setText(mContext.getString(R.string.activity_by_friends_favorite,
								first_source_name, getName(first_status)));
					} else {
						holder.text.setVisibility(View.GONE);
						holder.title.setText(mContext.getString(R.string.activity_by_friends_favorite_multi,
								first_source_name, getName(first_status), target_statuses_length - 1));
					}
					setProfileImage(holder, first_source);
					setUserProfileImages(holder, target_statuses);
					break;
				}
				case ParcelableActivity.ACTION_FOLLOW: {
					holder.text.setVisibility(View.GONE);
					if (target_users_length == 0) return;
					if (target_users_length == 1) {
						holder.title.setText(mContext.getString(R.string.activity_by_friends_follow, first_source_name,
								getName(target_users[0])));
					} else {
						holder.title.setText(mContext.getString(R.string.activity_by_friends_follow_multi,
								first_source_name, getName(target_users[0]), target_users_length - 1));
					}
					setProfileImage(holder, first_source);
					setUserProfileImages(holder, target_users);
					break;
				}
				case ParcelableActivity.ACTION_RETWEET: {
					if (target_object_statuses != null && target_object_statuses.length > 0) {
						final ParcelableStatus status = target_object_statuses[0];
						holder.text.setSingleLine(true);
						holder.text.setEllipsize(TruncateAt.END);
						holder.text.setText(status.text_plain);
					}
					if (sources_length == 1) {
						holder.title.setText(mContext.getString(R.string.activity_by_friends_retweet,
								first_source_name, getName(target_statuses[0])));
					} else {
						holder.title.setText(mContext.getString(R.string.activity_about_me_retweet_multi,
								first_source_name, sources_length - 1));
					}
					setUserProfileImages(holder, sources);
					break;
				}
				case ParcelableActivity.ACTION_LIST_MEMBER_ADDED: {
					holder.text.setVisibility(View.GONE);
					if (target_object_user_lists_length == 1) {
						holder.title.setText(mContext.getString(R.string.activity_by_friends_list_member_added,
								first_source_name, getName(target_users[0])));
					} else {
						holder.title.setText(mContext.getString(R.string.activity_about_me_list_member_added_multi,
								first_source_name, sources_length - 1));
					}
					setProfileImage(holder, first_source);
					setUserProfileImages(holder, target_users);
					break;
				}
				case ParcelableActivity.ACTION_LIST_CREATED: {
					if (target_user_lists_length == 0) return;
					final ParcelableUserList user_list = target_user_lists[0];
					if (target_user_lists_length == 1) {
						holder.text.setVisibility(View.VISIBLE);
						holder.title.setText(mContext.getString(R.string.activity_by_friends_list_created,
								first_source_name, user_list.name));
						holder.text.setText(user_list.description);
					} else {
						holder.text.setVisibility(View.GONE);
						holder.title.setText(mContext.getString(R.string.activity_by_friends_list_created_multi,
								first_source_name, user_list.name, target_user_lists_length - 1));
					}
					setProfileImage(holder, first_source);
					// setUserProfileImages(holder, target_users);
					break;
				}
			}
		}
	}
}
