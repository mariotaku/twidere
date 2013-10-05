package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.View;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.view.holder.ActivityViewHolder;

public class ParcelableActivitiesByFriendsAdapter extends BaseParcelableActivitiesAdapter {

	public ParcelableActivitiesByFriendsAdapter(final Context context) {
		super(context);
	}

	@Override
	public void bindView(final int position, final ActivityViewHolder holder, final ParcelableActivity item) {
		if (item == null) return;
		final ParcelableUser[] sources = item.sources;
		final ParcelableStatus[] target_statuses = item.target_statuses;
		final ParcelableUser[] target_users = item.target_users;
		final ParcelableStatus[] target_object_statuses = item.target_object_statuses;
		final ParcelableUserList[] target_user_lists = item.target_user_lists;
		final ParcelableUserList[] target_object_user_lists = item.target_object_user_lists;
		final int sources_length = sources != null ? sources.length : 0;
		final int target_statuses_length = target_statuses != null ? target_statuses.length : 0;
		final int target_users_length = target_users != null ? target_users.length : 0;
		final int target_object_user_lists_length = target_object_user_lists != null ? target_object_user_lists.length
				: 0;
		final int target_user_lists_length = target_user_lists != null ? target_user_lists.length : 0;
		final int action = item.action;
		final boolean mDisplayProfileImage = shouldDisplayProfileImage();
		final Context mContext = getContext();
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
