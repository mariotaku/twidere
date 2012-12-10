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

import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.UserListViewHolder;
import org.mariotaku.twidere.util.LazyImageLoader;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class UserListsAdapter extends ArrayAdapter<ParcelableUserList> implements BaseAdapterInterface {

	private final LazyImageLoader mProfileImageLoader;

	private boolean mDisplayProfileImage;
	private float mTextSize;

	public UserListsAdapter(final Context context) {
		super(context, R.layout.user_list_list_item);
		final TwidereApplication application = TwidereApplication.getInstance(context);
		mProfileImageLoader = application.getProfileImageLoader();
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final Object tag = view.getTag();
		UserListViewHolder holder = null;
		if (tag instanceof UserListViewHolder) {
			holder = (UserListViewHolder) tag;
		} else {
			holder = new UserListViewHolder(view);
			view.setTag(holder);
		}
		final ParcelableUserList user_list = getItem(position);
		holder.setTextSize(mTextSize);
		holder.name.setText("@" + user_list.user_screen_name + "/" + user_list.name);
		holder.owner.setText(user_list.user_name);
		holder.description.setText(user_list.description);
		holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		if (mDisplayProfileImage) {
			mProfileImageLoader.displayImage(user_list.user_profile_image_url_string, holder.profile_image);
		}
		return view;
	}

	public void setData(final List<ParcelableUserList> data) {
		setData(data, false);
	}

	public void setData(final List<ParcelableUserList> data, final boolean clear_old) {
		if (clear_old) {
			clear();
		}
		if (data == null) return;
		for (final ParcelableUserList user : data) {
			if (clear_old || findItem(user.list_id) == null) {
				add(user);
			}
		}
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

	@Override
	public void setTextSize(final float text_size) {
		if (text_size != mTextSize) {
			mTextSize = text_size;
			notifyDataSetChanged();
		}
	}
}
