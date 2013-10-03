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
import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.getNameDisplayOptionInt;
import static org.mariotaku.twidere.util.Utils.getUserNickname;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.view.holder.UserListViewHolder;

import java.util.List;
import java.util.Locale;

public class ParcelableUserListsAdapter extends ArrayAdapter<ParcelableUserList> implements IBaseCardAdapter,
		OnClickListener {

	private final Context mContext;
	private final ImageLoaderWrapper mProfileImageLoader;
	private final MultiSelectManager mMultiSelectManager;
	private final Locale mLocale;

	private boolean mDisplayProfileImage, mNicknameOnly;
	private float mTextSize;
	private boolean mDisplayName;

	private MenuButtonClickListener mListener;

	public ParcelableUserListsAdapter(final Context context) {
		super(context, R.layout.user_list_list_item);
		mContext = context;
		mLocale = context.getResources().getConfiguration().locale;
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mProfileImageLoader = app.getImageLoaderWrapper();
		mMultiSelectManager = app.getMultiSelectManager();
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
		final UserListViewHolder holder;
		if (tag instanceof UserListViewHolder) {
			holder = (UserListViewHolder) tag;
		} else {
			holder = new UserListViewHolder(view);
			holder.profile_image.setOnClickListener(this);
			holder.item_menu.setOnClickListener(this);
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
		holder.setTextSize(mTextSize);
		holder.name.setText(user_list.name);
		holder.created_by.setText(mContext.getString(R.string.created_by, created_by));
		holder.description.setVisibility(TextUtils.isEmpty(user_list.description) ? View.GONE : View.VISIBLE);
		holder.description.setText(user_list.description);
		holder.members_count.setText(getLocalizedNumber(mLocale, user_list.members_count));
		holder.subscribers_count.setText(getLocalizedNumber(mLocale, user_list.subscribers_count));
		holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		if (mDisplayProfileImage) {
			mProfileImageLoader.displayProfileImage(holder.profile_image, user_list.user_profile_image_url);
		}
		holder.profile_image.setTag(position);
		holder.item_menu.setTag(position);
		return view;
	}

	@Override
	public void onClick(final View view) {
		if (mMultiSelectManager.isActive()) return;
		final Object tag = view.getTag();
		final int position = tag instanceof Integer ? (Integer) tag : -1;
		if (position == -1) return;
		switch (view.getId()) {
			case R.id.profile_image: {
				if (mContext instanceof Activity) {
					final ParcelableUserList item = getItem(position);
					openUserProfile((Activity) mContext, item.account_id, item.user_id, item.user_screen_name);
				}
				break;
			}
			case R.id.item_menu: {
				if (position == -1 || mListener == null) return;
				mListener.onMenuButtonClick(view, position, getItemId(position));
				break;
			}
		}
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
	public void setMenuButtonClickListener(final MenuButtonClickListener listener) {
		mListener = listener;
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
		if (text_size != mTextSize) {
			mTextSize = text_size;
			notifyDataSetChanged();
		}
	}
}
