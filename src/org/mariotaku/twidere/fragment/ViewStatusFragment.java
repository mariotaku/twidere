package org.mariotaku.twidere.fragment;

import java.net.MalformedURLException;
import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.LazyImageLoader;

import roboguice.inject.InjectExtra;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewStatusFragment extends BaseFragment {

	@InjectExtra(Statuses.ACCOUNT_ID) private long mAccountId;
	@InjectExtra(Statuses.STATUS_ID) private long mStatusId;
	@InjectExtra(TweetStore.KEY_TYPE) private int mType;
	private ContentResolver mResolver;
	private TextView mName, mScreenName, mText;
	private ImageView mProfileImage;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		View view = getView();
		mName = (TextView) view.findViewById(R.id.name);
		mScreenName = (TextView) view.findViewById(R.id.screen_name);
		mText = (TextView) view.findViewById(R.id.text);
		mProfileImage =(ImageView)view.findViewById(R.id.profile_image);
		mResolver = getSherlockActivity().getContentResolver();
		Uri uri;
		String[] cols;
		String where;
		switch (mType) {
			case TweetStore.VALUE_TYPE_MENTION:
				uri = Mentions.CONTENT_URI;
				cols = Mentions.COLUMNS;
				where = Mentions.STATUS_ID + "=" + mStatusId;
				break;
			case TweetStore.VALUE_TYPE_STATUS:
			default:
				uri = Statuses.CONTENT_URI;
				cols = Statuses.COLUMNS;
				where = Statuses.STATUS_ID + "=" + mStatusId;
				break;
		}
		
		Cursor cur = mResolver.query(uri, cols, where, null, null);
		if (cur != null && cur.getCount() > 0) {
			cur.moveToFirst();
			String name = cur.getString(cur.getColumnIndexOrThrow(Statuses.NAME));
			mName.setText(name != null ? name : "");
			String screen_name = cur.getString(cur.getColumnIndexOrThrow(Statuses.SCREEN_NAME));
			mScreenName.setText(screen_name != null ? "@" + screen_name : "");
			String text = cur.getString(cur.getColumnIndexOrThrow(Statuses.TEXT));
			mText.setText(text != null ? text : "");
			
			LazyImageLoader imageloader = ((TwidereApplication) getSherlockActivity().getApplication())
					.getListProfileImageLoader();
			String profile_image_url = cur.getString(cur.getColumnIndexOrThrow(Statuses.PROFILE_IMAGE_URL));
			URL url = null;
			try {
				url = new URL(profile_image_url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			imageloader.displayImage(url, mProfileImage);
		}
		if (cur != null) cur.close();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.view_status, container, false);
	}

}
