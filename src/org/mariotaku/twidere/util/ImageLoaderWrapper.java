/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.Utils.getBestBannerType;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

public class ImageLoaderWrapper implements Constants {

	private final ImageLoader mImageLoader;
	private final DisplayImageOptions mProfileImageDisplayOptions, mImageDisplayOptions, mBannerDisplayOptions;

	public ImageLoaderWrapper(final ImageLoader loader) {
		mImageLoader = loader;
		final DisplayImageOptions.Builder profileOptsNuilder = new DisplayImageOptions.Builder();
		profileOptsNuilder.cacheInMemory(true);
		profileOptsNuilder.cacheOnDisc(true);
		profileOptsNuilder.showImageForEmptyUri(R.drawable.ic_profile_image_default);
		profileOptsNuilder.showImageOnFail(R.drawable.ic_profile_image_default);
		profileOptsNuilder.showImageOnLoading(R.drawable.ic_profile_image_default);
		profileOptsNuilder.bitmapConfig(Bitmap.Config.ARGB_8888);
		profileOptsNuilder.resetViewBeforeLoading(true);
		final DisplayImageOptions.Builder imageOptsBuilder = new DisplayImageOptions.Builder();
		imageOptsBuilder.cacheInMemory(true);
		imageOptsBuilder.cacheOnDisc(true);
		imageOptsBuilder.bitmapConfig(Bitmap.Config.RGB_565);
		imageOptsBuilder.resetViewBeforeLoading(true);
		final DisplayImageOptions.Builder bannerOptsBuilder = new DisplayImageOptions.Builder();
		bannerOptsBuilder.cacheInMemory(true);
		bannerOptsBuilder.cacheOnDisc(true);
		bannerOptsBuilder.bitmapConfig(Bitmap.Config.RGB_565);
		bannerOptsBuilder.resetViewBeforeLoading(true);

		mProfileImageDisplayOptions = profileOptsNuilder.build();
		mImageDisplayOptions = imageOptsBuilder.build();
		mBannerDisplayOptions = bannerOptsBuilder.build();
	}

	public void clearFileCache() {
		mImageLoader.clearDiscCache();
	}

	public void clearMemoryCache() {
		mImageLoader.clearMemoryCache();
	}

	public void displayPreviewImage(final ImageView view, final String url) {
		mImageLoader.displayImage(url, view, mImageDisplayOptions);
	}

	public void displayPreviewImage(final ImageView view, final String url, final ImageLoadingHandler loadingHandler) {
		mImageLoader.displayImage(url, view, mImageDisplayOptions, loadingHandler, loadingHandler);
	}

	public void displayProfileBanner(final ImageView view, final String base_url, final int width) {
		final String type = getBestBannerType(width);
		final String url = TextUtils.isEmpty(base_url) ? null : base_url + "/" + type;
		mImageLoader.displayImage(url, view, mBannerDisplayOptions);
	}

	public void displayProfileImage(final ImageView view, final String url) {
		mImageLoader.displayImage(url, view, mProfileImageDisplayOptions);
	}

	public void loadProfileImage(final String url, final ImageLoadingListener listener) {
		mImageLoader.loadImage(url, mProfileImageDisplayOptions, listener);
	}
}
