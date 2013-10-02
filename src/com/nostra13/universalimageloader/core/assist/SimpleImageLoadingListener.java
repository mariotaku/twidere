/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
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

package com.nostra13.universalimageloader.core.assist;

import android.graphics.Bitmap;
import android.view.View;

/**
 * A convenient class to extend when you only want to listen for a subset of all
 * the image loading events. This implements all methods in the
 * {@link ImageLoadingListener} but does nothing.
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.4.0
 */
public class SimpleImageLoadingListener implements ImageLoadingListener {
	@Override
	public void onLoadingCancelled(final String imageUri, final View view) {
		// Empty implementation
	}

	@Override
	public void onLoadingComplete(final String imageUri, final View view, final Bitmap loadedImage) {
		// Empty implementation
	}

	@Override
	public void onLoadingFailed(final String imageUri, final View view, final FailReason failReason) {
		// Empty implementation
	}

	@Override
	public void onLoadingProgressChanged(final String imageUri, final View view, final int current, final int total) {
		// Empty implementation

	}

	@Override
	public void onLoadingStarted(final String imageUri, final View view) {
		// Empty implementation
	}
}
