package org.mariotaku.twidere.adapter;

import java.net.MalformedURLException;
import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.ProfileImageLoader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;

public class UserAutoCompleteAdapter extends SimpleCursorAdapter {

	private ContentResolver mResolver;
	private ProfileImageLoader mImageLoader;

	private int mNameIdx, mProfileImageUrlIdx, mScreenNameIdx;

	public UserAutoCompleteAdapter(Context context, ProfileImageLoader imageloader) {
		super(context, R.layout.user_autocomplete_list_item, null, new String[0], new int[0], 0);
		mResolver = context.getContentResolver();
		mImageLoader = imageloader;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		holder.name_view.setText(cursor.getString(mNameIdx));
		holder.screenname_view.setText(cursor.getString(mScreenNameIdx));
		try {
			mImageLoader.displayImage(new URL(cursor.getString(mProfileImageUrlIdx)), holder.profile_image_view);
		} catch (final MalformedURLException e) {

		}
		super.bindView(view, context, cursor);
	}

	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(mScreenNameIdx);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View view = super.newView(context, cursor, parent);
		if (!(view.getTag() instanceof ViewHolder)) {
			final ViewHolder holder = new ViewHolder(view);
			view.setTag(holder);
		}
		return view;
	}

	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		final FilterQueryProvider filter = getFilterQueryProvider();
		if (filter != null) return filter.runQuery(constraint);
		final StringBuilder where = new StringBuilder();
		where.append(CachedUsers.NAME + " LIKE '%" + constraint + "%'");
		where.append(" OR " + CachedUsers.SCREEN_NAME + " LIKE '%" + constraint + "%'");
		return mResolver.query(CachedUsers.CONTENT_URI, CachedUsers.COLUMNS, where.toString(), null, null);
	}

	@Override
	public Cursor swapCursor(Cursor cursor) {
		if (cursor != null) {
			mNameIdx = cursor.getColumnIndexOrThrow(CachedUsers.NAME);
			mProfileImageUrlIdx = cursor.getColumnIndexOrThrow(Statuses.PROFILE_IMAGE_URL);
			mScreenNameIdx = cursor.getColumnIndexOrThrow(CachedUsers.SCREEN_NAME);
		}
		return super.swapCursor(cursor);
	}

	private static class ViewHolder {

		public TextView name_view, screenname_view;
		public ImageView profile_image_view;

		public ViewHolder(View view) {
			name_view = (TextView) view.findViewById(android.R.id.text1);
			screenname_view = (TextView) view.findViewById(android.R.id.text2);
			profile_image_view = (ImageView) view.findViewById(android.R.id.icon);
		}

	}

}
