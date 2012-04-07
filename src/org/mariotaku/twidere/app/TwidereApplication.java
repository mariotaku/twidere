package org.mariotaku.twidere.app;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;

import android.app.Application;
import android.content.ContentResolver;

public class TwidereApplication extends Application {

	private LazyImageLoader mListProfileImageLoader;
	private CommonUtils mCommonUtils;
	private ServiceInterface mServiceInterface;

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
		ContentResolver resolver = getContentResolver();
		super.onTerminate();
	}

}
