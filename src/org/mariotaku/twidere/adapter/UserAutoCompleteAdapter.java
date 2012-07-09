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

import static org.mariotaku.twidere.util.Utils.parseURL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.ProfileImageLoader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;

public class UserAutoCompleteAdapter extends SimpleCursorAdapter {

	private Cursor mCursor;

	private final ContentResolver mResolver;
	private final ProfileImageLoader mImageLoader;
	private static final String[] FROM = new String[] { CachedUsers.NAME, CachedUsers.SCREEN_NAME };
	private static final int[] TO = new int[] { android.R.id.text1, android.R.id.text2 };

	private int mProfileImageUrlIdx, mScreenNameIdx;

	private boolean mCursorClosed = false;

	public UserAutoCompleteAdapter(Context context) {
		super(context, R.layout.user_autocomplete_list_item, null, FROM, TO, 0);
		mResolver = context.getContentResolver();
		mImageLoader = ((TwidereApplication) context.getApplicationContext()).getProfileImageLoader();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (mCursorClosed) return;
		final ImageView image_view = (ImageView) view.findViewById(android.R.id.icon);
		mImageLoader.displayImage(parseURL(cursor.getString(mProfileImageUrlIdx)), image_view);
		super.bindView(view, context, cursor);
	}

	@Override
	public void changeCursor(Cursor cursor) {
		if (mCursorClosed) return;
		if (cursor != null) {
			mProfileImageUrlIdx = cursor.getColumnIndexOrThrow(Statuses.PROFILE_IMAGE_URL);
			mScreenNameIdx = cursor.getColumnIndexOrThrow(CachedUsers.SCREEN_NAME);
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
	public CharSequence convertToString(Cursor cursor) {
		if (mCursorClosed) return null;
		return cursor.getString(mScreenNameIdx);
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
		constraint = constraint != null ? constraint.toString().replaceAll("_", "\\_") : null;
		where.append(CachedUsers.SCREEN_NAME + " LIKE '" + constraint + "%' ESCAPE '\\'");
		where.append(" OR ");
		where.append(CachedUsers.NAME + " LIKE '" + constraint + "%' ESCAPE '\\'");
		return mResolver.query(CachedUsers.CONTENT_URI, CachedUsers.COLUMNS, constraint != null ? where.toString()
				: null, null, null);
	}

}
