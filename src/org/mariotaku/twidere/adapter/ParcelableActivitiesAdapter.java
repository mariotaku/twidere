package org.mariotaku.twidere.adapter;

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.mariotaku.twidere.util.Utils.formatSameDayTime;
import static org.mariotaku.twidere.util.Utils.getNameDisplayOptionInt;

import android.content.Context;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.List;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IBaseAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.view.holder.ActivityViewHolder;

public class ParcelableActivitiesAdapter extends ArrayAdapter<ParcelableActivity> implements IBaseAdapter {

	private boolean mDisplayProfileImage, mShowAbsoluteTime;
	private int mNameDisplayOption;
	private float mTextSize;

	private final ImageLoaderWrapper mProfileImageLoader;
	private final Context mContext;

	public ParcelableActivitiesAdapter(final Context context) {
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
			final String source_name = mNameDisplayOption == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME ?
					first_source.screen_name : first_source.name;
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
					holder.activity_profile_image_container.setVisibility(mDisplayProfileImage ? View.VISIBLE
							: View.GONE);
					setUserProfileImages(sources, holder);
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
					holder.activity_profile_image_container.setVisibility(mDisplayProfileImage ? View.VISIBLE
								: View.GONE);
					setUserProfileImages(sources, holder);
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
							holder.reply_status.setCompoundDrawablesWithIntrinsicBounds(
									R.drawable.ic_indicator_reply, 0, 0, 0);
						}
					}
					setProfileImage(holder, first_source.profile_image_url);
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
						holder.title.setText(mContext.getString(R.string.activity_about_me_retweet, source_name));
					} else {
						holder.title.setText(mContext.getString(R.string.activity_about_me_retweet_multi, source_name,
							sources_length - 1));
					}
					holder.activity_profile_image_container.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
					setUserProfileImages(sources, holder);
					break;
				}
				case ParcelableActivity.ACTION_LIST_MEMBER_ADDED: {
					holder.text.setVisibility(View.GONE);
					if (sources_length == 1) {
						holder.title.setText(mContext.getString(R.string.activity_about_me_list_member_added, source_name));
					} else {
						holder.title.setText(mContext.getString(R.string.activity_about_me_list_member_added_multi,
							source_name, sources_length - 1));
					}
					holder.activity_profile_image_container.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
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
		final int option_int = getNameDisplayOptionInt(option);
		if (option_int == mNameDisplayOption) return;
		mNameDisplayOption = option_int;
		notifyDataSetChanged();
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
