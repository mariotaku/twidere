package org.mariotaku.twidere.widget;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class UserAutoCompleteAdapter extends SimpleCursorAdapter {

	private Context mContext;
	private int mNameIdx, mProfileImageUrlIdx, mScreenNameIdx, mUserIdIdx;

	public UserAutoCompleteAdapter(Context context) {
		super(context, R.layout.user_autocomplete_list_item, null, new String[0], new int[0], 0);
		Cursor cur = context.getContentResolver().query(CachedUsers.CONTENT_URI,
				CachedUsers.COLUMNS, null, null, null);
		changeCursor(cur);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		super.bindView(view, context, cursor);
	}

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		if (cursor != null) {
			mNameIdx = cursor.getColumnIndexOrThrow(CachedUsers.NAME);
			mProfileImageUrlIdx = cursor.getColumnIndexOrThrow(Statuses.PROFILE_IMAGE_URL);
			mScreenNameIdx = cursor.getColumnIndexOrThrow(CachedUsers.SCREEN_NAME);
			mUserIdIdx = cursor.getColumnIndexOrThrow(CachedUsers.USER_ID);
		}

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		return super.newView(context, cursor, parent);
	}

	public static class ViewHolder {

		public long user_id;
		public TextView name_view, screenname_view;
		public ImageView profile_image_view;

		public ViewHolder(View view) {
			name_view = (TextView) view.findViewById(android.R.id.text1);
			screenname_view = (TextView) view.findViewById(android.R.id.text2);
		}

	}

}
