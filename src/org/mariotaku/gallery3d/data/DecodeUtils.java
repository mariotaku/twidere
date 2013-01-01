/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.gallery3d.data;

import java.io.FileDescriptor;

import org.mariotaku.gallery3d.common.BitmapUtils;
import org.mariotaku.gallery3d.util.ThreadPool.CancelListener;
import org.mariotaku.gallery3d.util.ThreadPool.JobContext;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.util.Log;

public class DecodeUtils {
	private static final String TAG = "DecodeUtils";

	public static BitmapRegionDecoder createBitmapRegionDecoder(final JobContext jc, final FileDescriptor fd,
			final boolean shareable) {
		try {
			return BitmapRegionDecoder.newInstance(fd, shareable);
		} catch (final Throwable t) {
			Log.w(TAG, t);
			return null;
		}
	}

	// TODO: This function should not be called directly from
	// DecodeUtils.requestDecode(...), since we don't have the knowledge
	// if the bitmap will be uploaded to GL.
	public static Bitmap ensureGLCompatibleBitmap(final Bitmap bitmap) {
		if (bitmap == null || bitmap.getConfig() != null) return bitmap;
		final Bitmap newBitmap = bitmap.copy(Config.ARGB_8888, false);
		bitmap.recycle();
		return newBitmap;
	}

	public static Bitmap requestDecode(final JobContext jc, final FileDescriptor fd, Options options,
			final int targetSize) {
		if (options == null) {
			options = new Options();
		}
		jc.setCancelListener(new DecodeCanceller(options));

		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fd, null, options);
		if (jc.isCancelled()) return null;

		options.inSampleSize = BitmapUtils.computeSampleSizeLarger(options.outWidth, options.outHeight, targetSize);
		options.inJustDecodeBounds = false;

		Bitmap result = BitmapFactory.decodeFileDescriptor(fd, null, options);
		// We need to resize down if the decoder does not support inSampleSize.
		// (For example, GIF images.)
		result = BitmapUtils.resizeDownIfTooBig(result, targetSize, true);
		return ensureGLCompatibleBitmap(result);
	}

	private static class DecodeCanceller implements CancelListener {
		Options mOptions;

		public DecodeCanceller(final Options options) {
			mOptions = options;
		}

		@Override
		public void onCancel() {
			mOptions.requestCancelDecode();
		}
	}
}
