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

import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.ActivityViewHolder;

import java.util.List;

public abstract class BaseParcelableActivitiesAdapter extends BaseArrayAdapter<ParcelableActivity> implements
		IBaseCardAdapter {

	private final Context mContext;
	private final MultiSelectManager mMultiSelectManager;
	private final ImageLoaderWrapper mProfileImageLoader;

	private boolean mShowAbsoluteTime, mAnimationEnabled;
	private int mMaxAnimationPosition;

	private MenuButtonClickListener mListener;

	public BaseParcelableActivitiesAdapter(final Context context) {
		this(context, Utils.isCompactCards(context));
	}

	public BaseParcelableActivitiesAdapter(final Context context, final boolean compactCards) {
		super(context, getItemResource(compactCards));
		mContext = context;
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mMultiSelectManager = app.getMultiSelectManager();
		mProfileImageLoader = app.getImageLoaderWrapper();
		configBaseCardAdapter(context, this);
	}

	public abstract void bindView(final int position, final ActivityViewHolder holder, final ParcelableActivity item);

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
		holder.setTextSize(getTextSize());
		holder.my_profile_image.setVisibility(View.GONE);
		final ParcelableActivity item = getItem(position);
		if (mShowAbsoluteTime) {
			holder.time.setTime(item.activity_timestamp);
		} else {
			holder.time.setTime(item.activity_timestamp);
		}
		bindView(position, holder, item);
		return view;
	}

	public void onItemSelected(final Object item) {
		notifyDataSetChanged();
	}

	public void onItemUnselected(final Object item) {
		notifyDataSetChanged();
	}

	@Override
	public void setAnimationEnabled(final boolean anim) {
		if (mAnimationEnabled == anim) return;
		mAnimationEnabled = anim;
	}

	public void setData(final List<ParcelableActivity> data) {
		clear();
		if (data == null) return;
		addAll(data);
	}

	@Override
	public void setMaxAnimationPosition(final int position) {
		mMaxAnimationPosition = position;
	}

	@Override
	public void setMenuButtonClickListener(final MenuButtonClickListener listener) {
		mListener = listener;
	}

	public void setShowAbsoluteTime(final boolean show) {
		if (show != mShowAbsoluteTime) {
			mShowAbsoluteTime = show;
			notifyDataSetChanged();
		}
	}

	protected Context getContext() {
		return mContext;
	}

	protected String getName(final ParcelableStatus status) {
		if (status == null) return null;
		return isDisplayNameFirst() ? status.user_name : "@" + status.user_screen_name;
	}

	protected String getName(final ParcelableUser user) {
		if (user == null) return null;
		return isDisplayNameFirst() ? user.name : "@" + user.screen_name;
	}

	protected void setProfileImage(final ActivityViewHolder holder, final ParcelableStatus status) {
		if (isDisplayProfileImage()) {
			mProfileImageLoader.displayProfileImage(holder.profile_image, status.user_profile_image_url);
		} else {
			holder.profile_image.setImageDrawable(null);
		}
	}

	protected void setProfileImage(final ActivityViewHolder holder, final ParcelableUser user) {
		if (isDisplayProfileImage()) {
			mProfileImageLoader.displayProfileImage(holder.profile_image, user.profile_image_url);
		} else {
			holder.profile_image.setImageDrawable(null);
		}
	}

	protected void setUserProfileImages(final ActivityViewHolder holder, final ParcelableStatus[] statuses) {
		final int length = statuses != null ? Math.min(holder.activity_profile_images.length, statuses.length) : 0;
		final boolean should_display_images = isDisplayProfileImage() && length > 0;
		holder.activity_profile_image_container.setVisibility(should_display_images ? View.VISIBLE : View.GONE);
		if (!should_display_images) return;
		for (int i = 0, j = holder.activity_profile_images.length; i < j; i++) {
			final ImageView activity_profile_image = holder.activity_profile_images[i];
			if (j < length) {
				final String profile_image_url = statuses[i].user_profile_image_url;
				activity_profile_image.setVisibility(View.VISIBLE);
				mProfileImageLoader.displayProfileImage(activity_profile_image, profile_image_url);
			} else {
				activity_profile_image.setVisibility(View.GONE);
			}
		}
	}

	protected void setUserProfileImages(final ActivityViewHolder holder, final ParcelableUser[] users) {
		final int length = users != null ? Math.min(holder.activity_profile_images.length, users.length) : 0;
		final boolean should_display_images = isDisplayProfileImage() && length > 0;
		holder.activity_profile_image_container.setVisibility(should_display_images ? View.VISIBLE : View.GONE);
		if (!should_display_images) return;
		for (int i = 0; i < length; i++) {
			final ImageView activity_profile_image = holder.activity_profile_images[i];
			final String profile_image_url = users[i].profile_image_url;
			mProfileImageLoader.displayProfileImage(activity_profile_image, profile_image_url);
		}
	}

	protected boolean shouldDisplayProfileImage() {
		return isDisplayProfileImage();
	}

	private static int getItemResource(final boolean compactCards) {
		return compactCards ? R.layout.card_item_activity_compact : R.layout.card_item_activity;
	}

}
