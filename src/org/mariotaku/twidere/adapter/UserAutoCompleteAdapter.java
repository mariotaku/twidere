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

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.CachedValues;
import org.mariotaku.twidere.util.LazyImageLoader;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;

public class UserAutoCompleteAdapter extends SimpleCursorAdapter implements Constants {

	private Cursor mCursor;

	private final ContentResolver mResolver;
	private final LazyImageLoader mProfileImageLoader;
	private final SharedPreferences mPreferences;
	private static final String[] FROM = new String[0];
	private static final int[] TO = new int[0];

	private int mProfileImageUrlIdx, mNameIdx, mScreenNameIdx;

	private boolean mCursorClosed = false;

	private final boolean mDisplayProfileImage, mDisplayHiResProfileImage;

	public UserAutoCompleteAdapter(final Context context) {
		super(context, R.layout.user_autocomplete_list_item, null, FROM, TO, 0);
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mResolver = context.getContentResolver();
		final Context app_context = context.getApplicationContext();
		mProfileImageLoader = app_context instanceof TwidereApplication ? ((TwidereApplication) app_context)
				.getProfileImageLoader() : null;
		mDisplayProfileImage = mPreferences != null ? mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE,
				true) : true;
		mDisplayHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		if (mCursorClosed) return;
		final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
		final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
		if (mScreenNameIdx != -1) {
			text1.setText(cursor.getString(mNameIdx));
			text2.setText("@" + cursor.getString(mScreenNameIdx));
		} else {
			text1.setText("#" + cursor.getString(mNameIdx));
			text2.setText(R.string.hashtag);
		}
		icon.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		if (mProfileImageUrlIdx != -1) {
			if (mDisplayProfileImage && mProfileImageLoader != null) {
				final String profile_image_url_string = cursor.getString(mProfileImageUrlIdx);
				mProfileImageLoader.displayImage(cursor.getString(mProfileImageUrlIdx), icon);
				if (mDisplayHiResProfileImage) {
					mProfileImageLoader.displayImage(getBiggerTwitterProfileImage(profile_image_url_string), icon);
				} else {
					mProfileImageLoader.displayImage(profile_image_url_string, icon);
				}
			} else {
				icon.setImageResource(R.drawable.ic_profile_image_default);
			}
		} else {
			//TODO show hashtag icon
		}
		super.bindView(view, context, cursor);
	}

	@Override
	public void changeCursor(final Cursor cursor) {
		if (mCursorClosed) return;
		if (cursor != null) {
			mNameIdx = cursor.getColumnIndex(CachedValues.NAME);
			mScreenNameIdx = cursor.getColumnIndex(CachedUsers.SCREEN_NAME);
			mProfileImageUrlIdx = cursor.getColumnIndex(CachedUsers.PROFILE_IMAGE_URL);
		}
		mCursor = cursor;
		super.changeCursor(mCursor);
	}

	public void closeCursor() {
		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
		}
		mCursor = null;
		mCursorClosed = true;
	}

	@Override
	public CharSequence convertToString(final Cursor cursor) {
		if (mCursorClosed) return null;
		return cursor.getString(mScreenNameIdx != -1 ? mScreenNameIdx : mNameIdx);
	}

	public boolean isCursorClosed() {
		return mCursorClosed;
	}

	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		if (mCursorClosed) return null;
		final FilterQueryProvider filter = getFilterQueryProvider();
		if (filter != null) return filter.runQuery(constraint);
		final StringBuilder where = new StringBuilder();
		constraint = constraint != null ? constraint.toString().replaceAll("_", "^_") : null;
		where.append(CachedUsers.SCREEN_NAME + " LIKE '" + constraint + "%' ESCAPE '^'");
		where.append(" OR ");
		where.append(CachedUsers.NAME + " LIKE '" + constraint + "%' ESCAPE '^'");
		return mResolver.query(CachedUsers.CONTENT_URI, CachedUsers.COLUMNS, constraint != null ? where.toString()
				: null, null, null);
	}

}
