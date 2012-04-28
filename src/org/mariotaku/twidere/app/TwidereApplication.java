package org.mariotaku.twidere.app;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class TwidereApplication extends Application implements Constants {

	private LazyImageLoader mListProfileImageLoader;
	private CommonUtils mCommonUtils;
	private ServiceInterface mServiceInterface;
	private AsyncTaskManager mAsyncTaskManager = new AsyncTaskManager();

	public AsyncTaskManager getAsyncTaskManager() {
		return mAsyncTaskManager;
	}

	public CommonUtils getCommonUtils() {
		return mCommonUtils;
	}

	public LazyImageLoader getListProfileImageLoader() {
		return mListProfileImageLoader;
	}

	public ServiceInterface getServiceInterface() {
		return mServiceInterface;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mListProfileImageLoader = new LazyImageLoader(this, R.drawable.ic_profile_image_default,
				getResources().getDimensionPixelSize(R.dimen.profile_image_size));
		mCommonUtils = new CommonUtils(this);
		mServiceInterface = new ServiceInterface(this);
	}

	@Override
	public void onTerminate() {
		mListProfileImageLoader.clearMemoryCache();
		
		ContentResolver resolver = getContentResolver();
		String[] cols = new String[0];
		Uri[] uris = new Uri[]{Statuses.CONTENT_URI, Mentions.CONTENT_URI};
		int item_limit = this.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).getInt(PREFERENCE_KEY_ITEM_LIMIT, PREFERENCE_DEFAULT_ITEM_LIMIT);
		
		for (long account_id : CommonUtils.getAccounts(this)) {
			// Clean statuses.
			for (Uri uri : uris) {
				Cursor cur = resolver.query(uri, cols, Statuses.ACCOUNT_ID + "=" + account_id, null, Statuses.DEFAULT_SORT_ORDER);
				if (cur != null && cur.getCount() > item_limit) {
					cur.moveToPosition(item_limit - 1);
					int _id = cur.getInt(cur.getColumnIndexOrThrow(Statuses._ID));
					resolver.delete(uri, Statuses._ID + "<" + _id, null);
				}
				if (cur != null) cur.close();
			}
		}
		
		super.onTerminate();
	}
	
	@Override
	public void onLowMemory() {
		mListProfileImageLoader.clearMemoryCache();
		super.onLowMemory();
	}

}
