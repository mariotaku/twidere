package org.mariotaku.twidere.app;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.ProfileImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;

import android.app.Application;

public class TwidereApplication extends Application implements Constants {

	private ProfileImageLoader mProfileImageLoader;
	private AsyncTaskManager mAsyncTaskManager = new AsyncTaskManager();

	public AsyncTaskManager getAsyncTaskManager() {
		return mAsyncTaskManager;
	}

	public ProfileImageLoader getProfileImageLoader() {
		return mProfileImageLoader;
	}

	public ServiceInterface getServiceInterface() {
		return ServiceInterface.getInstance(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mProfileImageLoader = new ProfileImageLoader(this, R.drawable.ic_profile_image_default, getResources()
				.getDimensionPixelSize(R.dimen.profile_image_size));
	}

	@Override
	public void onLowMemory() {
		mProfileImageLoader.clearMemoryCache();
		super.onLowMemory();
	}

}
