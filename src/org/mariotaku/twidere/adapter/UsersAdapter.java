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

package org.mariotaku.twidere.adapter;

import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getNormalTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.parseURL;

import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserViewHolder;
import org.mariotaku.twidere.util.BaseAdapterInterface;
import org.mariotaku.twidere.util.LazyImageLoader;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class UsersAdapter extends ArrayAdapter<ParcelableUser> implements BaseAdapterInterface {

	private final LazyImageLoader mProfileImageLoader;
	private boolean mDisplayProfileImage, mDisplayHiResProfileImage, mDisplayName;
	private float mTextSize;
	private boolean mForceSSLConnection;

	public UsersAdapter(Context context) {
		super(context, R.layout.user_list_item, R.id.description);
		final TwidereApplication application = (TwidereApplication) context.getApplicationContext();
		mProfileImageLoader = application.getProfileImageLoader();
		application.getServiceInterface();
	}

	public ParcelableUser findItem(long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItemId(i) == id) return getItem(i);
		}
		return null;
	}

	public ParcelableUser findItemByUserId(long user_id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			final ParcelableUser item = getItem(i);
			if (item.user_id == user_id) return item;
		}
		return null;
	}

	public int findItemPositionByUserId(long user_id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			final ParcelableUser item = getItem(i);
			if (item.user_id == user_id) return i;
		}
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final Object tag = view.getTag();
		UserViewHolder holder = null;
		if (tag instanceof UserViewHolder) {
			holder = (UserViewHolder) tag;
		} else {
			holder = new UserViewHolder(view);
			view.setTag(holder);
		}
		final ParcelableUser user = getItem(position);
		holder.setTextSize(mTextSize);
		holder.name.setText(mDisplayName ? user.name : user.screen_name);
		holder.name.setCompoundDrawablesWithIntrinsicBounds(getUserTypeIconRes(user.is_verified, user.is_protected), 0,
				0, 0);
		holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		if (mDisplayProfileImage) {
			if (mDisplayHiResProfileImage) {
				mProfileImageLoader.displayImage(parseURL(getBiggerTwitterProfileImage(user.profile_image_url_string)),
						holder.profile_image);
			} else {
				mProfileImageLoader.displayImage(parseURL(getNormalTwitterProfileImage(user.profile_image_url_string)),
						holder.profile_image);
			}
		}

		return view;
	}

	public void setData(List<ParcelableUser> data) {
		setData(data, false);
	}

	public void setData(List<ParcelableUser> data, boolean clear_old) {
		if (clear_old) {
			clear();
		}
		if (data == null) return;
		for (final ParcelableUser user : data) {
			if (clear_old || findItemByUserId(user.user_id) == null) {
				add(user);
			}
		}
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

	@Override
	public void setTextSize(float text_size) {
		if (text_size != mTextSize) {
			mTextSize = text_size;
			notifyDataSetChanged();
		}
	}
}
