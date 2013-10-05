package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
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
			final ParcelableUser first_source = sources[0];
			final ParcelableStatus[] target_objects = item.target_object_statuses;
			final String source_name = getName(first_source);
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

}
