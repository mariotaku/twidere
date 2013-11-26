package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.ActivityViewHolder;

public class ParcelableActivitiesAboutMeAdapter extends BaseParcelableActivitiesAdapter {

	public ParcelableActivitiesAboutMeAdapter(final Context context) {
		super(context);
	}

	@Override
	public void bindView(final int position, final ActivityViewHolder holder, final ParcelableActivity item) {
		if (item == null) return;
		final ParcelableUser[] sources = item.sources;
		final ParcelableStatus[] target_statuses = item.target_statuses;
		final int sources_length = sources != null ? sources.length : 0;
		final int target_statuses_length = target_statuses != null ? target_statuses.length : 0;
		final int action = item.action;
		final boolean mDisplayProfileImage = shouldDisplayProfileImage();
		final Context mContext = getContext();
		holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		if (sources_length > 0) {
			final ParcelableUser firstSource = sources[0];
			final ParcelableStatus[] target_objects = item.target_object_statuses;
			final String sourceName = getName(firstSource);
			switch (action) {
				case ParcelableActivity.ACTION_FAVORITE: {
					holder.profile_image.setVisibility(View.GONE);
					if (target_statuses_length > 0) {
						final ParcelableStatus status = target_statuses[0];
						holder.text.setSingleLine(true);
						holder.text.setEllipsize(TruncateAt.END);
						holder.text.setText(status.text_unescaped);
					}
					if (sources_length == 1) {
						holder.name.setText(mContext.getString(R.string.activity_about_me_favorite, sourceName));
					} else {
						holder.name.setText(mContext.getString(R.string.activity_about_me_favorite_multi, sourceName,
								sources_length - 1));
					}
					setUserProfileImages(holder, sources);
					break;
				}
				case ParcelableActivity.ACTION_FOLLOW: {
					holder.profile_image.setVisibility(View.GONE);
					holder.text.setVisibility(View.GONE);
					if (sources_length == 1) {
						holder.name.setText(mContext.getString(R.string.activity_about_me_follow, sourceName));
					} else {
						holder.name.setText(mContext.getString(R.string.activity_about_me_follow_multi, sourceName,
								sources_length - 1));
					}
					setUserProfileImages(holder, sources);
					break;
				}
				case ParcelableActivity.ACTION_MENTION: {
					holder.name.setText(sourceName);
					if (target_objects != null && target_objects.length > 0) {
						final ParcelableStatus status = target_objects[0];
						holder.text.setText(status.text_unescaped);
						if (status.in_reply_to_status_id > 0 && !TextUtils.isEmpty(status.in_reply_to_screen_name)) {
							holder.reply_status.setVisibility(View.VISIBLE);
							holder.reply_status.setText(mContext.getString(R.string.in_reply_to,
									status.in_reply_to_screen_name));
							holder.reply_status.setCompoundDrawablesWithIntrinsicBounds(
									R.drawable.ic_indicator_conversation, 0, 0, 0);
						}
					}
					setProfileImage(holder, firstSource);
					break;
				}
				case ParcelableActivity.ACTION_REPLY: {
					holder.name.setText(sourceName);
					if (target_statuses_length > 0) {
						final ParcelableStatus status = target_statuses[0];
						holder.text.setText(status.text_unescaped);
						if (status.in_reply_to_status_id > 0 && !TextUtils.isEmpty(status.in_reply_to_screen_name)) {
							holder.reply_status.setVisibility(View.VISIBLE);
							holder.reply_status.setText(mContext.getString(R.string.in_reply_to,
									status.in_reply_to_screen_name));
							holder.reply_status.setCompoundDrawablesWithIntrinsicBounds(
									R.drawable.ic_indicator_conversation, 0, 0, 0);
						}
					}
					setProfileImage(holder, firstSource);
					break;
				}
				case ParcelableActivity.ACTION_RETWEET: {
					holder.profile_image.setVisibility(View.GONE);
					if (target_objects != null && target_objects.length > 0) {
						final ParcelableStatus status = target_objects[0];
						holder.text.setSingleLine(true);
						holder.text.setEllipsize(TruncateAt.END);
						holder.text.setText(status.text_unescaped);
					}
					if (sources_length == 1) {
						holder.name.setText(mContext.getString(R.string.activity_about_me_retweet, sourceName));
					} else {
						holder.name.setText(mContext.getString(R.string.activity_about_me_retweet_multi, sourceName,
								sources_length - 1));
					}
					setUserProfileImages(holder, sources);
					break;
				}
				case ParcelableActivity.ACTION_LIST_MEMBER_ADDED: {
					holder.profile_image.setVisibility(View.GONE);
					holder.text.setVisibility(View.GONE);
					if (sources_length == 1) {
						holder.name.setText(mContext.getString(R.string.activity_about_me_list_member_added,
								sourceName));
					} else {
						holder.name.setText(mContext.getString(R.string.activity_about_me_list_member_added_multi,
								sourceName, sources_length - 1));
					}
					setUserProfileImages(holder, sources);
					break;
				}
			}
		}
	}

}
