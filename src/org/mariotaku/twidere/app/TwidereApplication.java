package org.mariotaku.twidere.app;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;

import android.app.Application;

public class TwidereApplication extends Application implements Constants {

	private LazyImageLoader mProfileImageLoader;
	private AsyncTaskManager mAsyncTaskManager = new AsyncTaskManager();

	public AsyncTaskManager getAsyncTaskManager() {
		return mAsyncTaskManager;
	}

	public LazyImageLoader getProfileImageLoader() {
		return mProfileImageLoader;
	}

	public ServiceInterface getServiceInterface() {
		return ServiceInterface.getInstance(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mProfileImageLoader = new LazyImageLoader(this, R.drawable.ic_profile_image_default, getResources()
				.getDimensionPixelSize(R.dimen.profile_image_size));
	}

	@Override
	public void onLowMemory() {
		mProfileImageLoader.clearMemoryCache();
		super.onLowMemory();
	}

}
