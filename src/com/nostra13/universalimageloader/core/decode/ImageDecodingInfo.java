/*******************************************************************************
 * Copyright 2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.nostra13.universalimageloader.core.decode;

import android.annotation.TargetApi;
import android.graphics.BitmapFactory.Options;
import android.os.Build;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.MemoryCacheUtil;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

/**
 * Contains needed information for decoding image to Bitmap
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.8.3
 */
public class ImageDecodingInfo {

	private final String imageKey;
	private final String imageUri;
	private final ImageSize targetSize;

	private final ImageScaleType imageScaleType;
	private final ViewScaleType viewScaleType;

	private final ImageDownloader downloader;
	private final Object extraForDownloader;

	private final Options decodingOptions;

	public ImageDecodingInfo(final String imageKey, final String imageUri, final ImageSize targetSize,
			final ViewScaleType viewScaleType, final ImageDownloader downloader,
			final DisplayImageOptions displayOptions) {
		this.imageKey = imageKey;
		this.imageUri = imageUri;
		this.targetSize = targetSize;

		imageScaleType = displayOptions.getImageScaleType();
		this.viewScaleType = viewScaleType;

		this.downloader = downloader;
		extraForDownloader = displayOptions.getExtraForDownloader();

		decodingOptions = new Options();
		copyOptions(displayOptions.getDecodingOptions(), decodingOptions);
	}

	/** @return Decoding options */
	public Options getDecodingOptions() {
		return decodingOptions;
	}

	/** @return Downloader for image loading */
	public ImageDownloader getDownloader() {
		return downloader;
	}

	/** @return Auxiliary object for downloader */
	public Object getExtraForDownloader() {
		return extraForDownloader;
	}

	/**
	 * @return Original
	 *         {@linkplain MemoryCacheUtil#generateKey(String, ImageSize) image
	 *         key} (used in memory cache).
	 */
	public String getImageKey() {
		return imageKey;
	}

	/**
	 * @return {@linkplain ImageScaleType Scale type for image sampling and
	 *         scaling}. This parameter affects result size of decoded bitmap.
	 */
	public ImageScaleType getImageScaleType() {
		return imageScaleType;
	}

	/** @return Image URI for decoding (usually image from disc cache) */
	public String getImageUri() {
		return imageUri;
	}

	/**
	 * @return Target size for image. Decoded bitmap should close to this size
	 *         according to {@linkplain ImageScaleType image scale type} and
	 *         {@linkplain ViewScaleType view scale type}.
	 */
	public ImageSize getTargetSize() {
		return targetSize;
	}

	/**
	 * @return {@linkplain ViewScaleType View scale type}. This parameter
	 *         affects result size of decoded bitmap.
	 */
	public ViewScaleType getViewScaleType() {
		return viewScaleType;
	}

	private void copyOptions(final Options srcOptions, final Options destOptions) {
		destOptions.inDensity = srcOptions.inDensity;
		destOptions.inDither = srcOptions.inDither;
		destOptions.inInputShareable = srcOptions.inInputShareable;
		destOptions.inJustDecodeBounds = srcOptions.inJustDecodeBounds;
		destOptions.inPreferredConfig = srcOptions.inPreferredConfig;
		destOptions.inPurgeable = srcOptions.inPurgeable;
		destOptions.inSampleSize = srcOptions.inSampleSize;
		destOptions.inScaled = srcOptions.inScaled;
		destOptions.inScreenDensity = srcOptions.inScreenDensity;
		destOptions.inTargetDensity = srcOptions.inTargetDensity;
		destOptions.inTempStorage = srcOptions.inTempStorage;
		BitmapFactoryOptionAccessor.copyOptions(srcOptions, destOptions);
	}

	private static class BitmapFactoryOptionAccessor {

		private static void copyOptions(final Options srcOptions, final Options destOptions) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
				BitmapFactoryOptionAccessorSDK10.copyOptions(srcOptions, destOptions);
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				BitmapFactoryOptionAccessorSDK11.copyOptions(srcOptions, destOptions);
			}
		}

		@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
		private static class BitmapFactoryOptionAccessorSDK10 {

			private static void copyOptions(final Options srcOptions, final Options destOptions) {
				destOptions.inPreferQualityOverSpeed = srcOptions.inPreferQualityOverSpeed;
			}
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		private static class BitmapFactoryOptionAccessorSDK11 {

			private static void copyOptions(final Options srcOptions, final Options destOptions) {
				destOptions.inBitmap = srcOptions.inBitmap;
				destOptions.inMutable = srcOptions.inMutable;
			}
		}
	}

}