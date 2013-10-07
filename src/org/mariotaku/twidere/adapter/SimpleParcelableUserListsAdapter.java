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

import static org.mariotaku.twidere.util.Utils.configBaseAdapter;
import static org.mariotaku.twidere.util.Utils.getDisplayName;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IBaseAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.view.holder.TwoLineWithIconViewHolder;

import java.util.List;

public class SimpleParcelableUserListsAdapter extends ArrayAdapter<ParcelableUserList> implements IBaseAdapter {

	private final Context mContext;
	private final ImageLoaderWrapper mProfileImageLoader;

	private boolean mDisplayProfileImage, mNicknameOnly;
	private boolean mDisplayNameFirst;

	public SimpleParcelableUserListsAdapter(final Context context) {
		super(context, R.layout.two_line_list_item);
		mContext = context;
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mProfileImageLoader = app.getImageLoaderWrapper();
		configBaseAdapter(context, this);
	}

	public void appendData(final List<ParcelableUserList> data) {
		setData(data, false);
	}

	@Override
	public long getItemId(final int position) {
		return getItem(position) != null ? getItem(position).id : -1;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final Object tag = view.getTag();
		final TwoLineWithIconViewHolder holder;
		if (tag instanceof TwoLineWithIconViewHolder) {
			holder = (TwoLineWithIconViewHolder) tag;
		} else {
			holder = new TwoLineWithIconViewHolder(view);
			view.setTag(holder);
		}
		final ParcelableUserList user_list = getItem(position);
		final String display_name = getDisplayName(mContext, user_list.user_id, user_list.user_name,
				user_list.user_screen_name, mDisplayNameFirst, mNicknameOnly, false);
		holder.text1.setText(user_list.name);
		holder.text2.setText(mContext.getString(R.string.created_by, display_name));
		holder.icon.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		if (mDisplayProfileImage) {
			mProfileImageLoader.displayProfileImage(holder.icon, user_list.user_profile_image_url);
		}
		return view;
	}

	public void setData(final List<ParcelableUserList> data, final boolean clear_old) {
		if (clear_old) {
			clear();
		}
		if (data == null) return;
		for (final ParcelableUserList user : data) {
			if (clear_old || findItem(user.id) == null) {
				add(user);
			}
		}
	}

	@Override
	public void setDisplayNameFirst(final boolean name_first) {
		if (mDisplayNameFirst == name_first) return;
		mDisplayNameFirst = name_first;
		notifyDataSetChanged();
	}

	@Override
	public void setDisplayProfileImage(final boolean display) {
		if (display != mDisplayProfileImage) {
			mDisplayProfileImage = display;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setLinkHighlightOption(final String option) {

	}

	@Override
	public void setNicknameOnly(final boolean nickname_only) {
		if (mNicknameOnly == nickname_only) return;
		mNicknameOnly = nickname_only;
		notifyDataSetChanged();
	}

	@Override
	public void setTextSize(final float text_size) {
	}
}
