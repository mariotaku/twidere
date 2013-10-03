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
import static org.mariotaku.twidere.util.Utils.getNameDisplayOptionInt;
import static org.mariotaku.twidere.util.Utils.getUserNickname;

import android.content.Context;
import android.text.TextUtils;
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
	private boolean mDisplayName;

	public SimpleParcelableUserListsAdapter(final Context context) {
		super(context, R.layout.simple_two_line_with_icon_list_item);
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
		final String created_by;
		if (mDisplayName) {
			created_by = "@" + user_list.user_screen_name;
		} else {
			final String nick = getUserNickname(mContext, user_list.user_id);
			created_by = TextUtils.isEmpty(nick) ? user_list.user_name : mNicknameOnly ? nick : mContext.getString(
					R.string.name_with_nickname, user_list.user_name, nick);
		}
		holder.text1.setText(user_list.name);
		holder.text2.setText(mContext.getString(R.string.created_by, created_by));
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
	public void setDisplayProfileImage(final boolean display) {
		if (display != mDisplayProfileImage) {
			mDisplayProfileImage = display;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setNameDisplayOption(final String option) {
		final int option_int = getNameDisplayOptionInt(option);
		final boolean display_name = NAME_DISPLAY_OPTION_CODE_SCREEN_NAME != option_int;
		if (display_name == mDisplayName) return;
		mDisplayName = display_name;
		notifyDataSetChanged();
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
