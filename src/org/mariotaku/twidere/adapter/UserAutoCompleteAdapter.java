package org.mariotaku.twidere.adapter;

import java.net.MalformedURLException;
import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.LazyImageLoader;

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

	private ContentResolver mContent;
	private LazyImageLoader mImageLoader;

	private int mNameIdx, mProfileImageUrlIdx, mScreenNameIdx;

	public UserAutoCompleteAdapter(Context context, LazyImageLoader imageloader) {
		super(context, R.layout.user_autocomplete_list_item, null, new String[0], new int[0], 0);
		mContent = context.getContentResolver();
		mImageLoader = imageloader;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		holder.name_view.setText(cursor.getString(mNameIdx));
		holder.screenname_view.setText("@" + cursor.getString(mScreenNameIdx));
		try {
			mImageLoader.displayImage(new URL(cursor.getString(mProfileImageUrlIdx)), holder.profile_image_view);
		} catch (MalformedURLException e) {

		}
		super.bindView(view, context, cursor);
	}

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		if (cursor != null) {
			mNameIdx = cursor.getColumnIndexOrThrow(CachedUsers.NAME);
			mProfileImageUrlIdx = cursor.getColumnIndexOrThrow(Statuses.PROFILE_IMAGE_URL);
			mScreenNameIdx = cursor.getColumnIndexOrThrow(CachedUsers.SCREEN_NAME);
		}

	}

	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(mScreenNameIdx);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);
		if (!(view.getTag() instanceof ViewHolder)) {
			ViewHolder holder = new ViewHolder(view);
			view.setTag(holder);
		}
		return view;
	}

	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		FilterQueryProvider filter = getFilterQueryProvider();
		if (filter != null) return filter.runQuery(constraint);
		StringBuilder where = new StringBuilder();
		where.append(CachedUsers.NAME + " LIKE '%" + constraint + "%'");
		where.append(" OR " + CachedUsers.SCREEN_NAME + " LIKE '%" + constraint + "%'");
		return mContent.query(CachedUsers.CONTENT_URI, CachedUsers.COLUMNS, where.toString(), null, null);
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
