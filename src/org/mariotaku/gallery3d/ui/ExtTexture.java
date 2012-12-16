/*
 * Copyright (C) 2012 The Android Open Source Project
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

package org.mariotaku.gallery3d.ui;

import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

// ExtTexture is a texture whose content comes from a external texture.
// Before drawing, setSize() should be called.
public class ExtTexture extends BasicTexture {

	private static int[] sTextureId = new int[1];
	private static float[] sCropRect = new float[4];
	private final int mTarget;

	public ExtTexture(final int target) {
		GLId.glGenTextures(1, sTextureId, 0);
		mId = sTextureId[0];
		mTarget = target;
	}

	@Override
	public int getTarget() {
		return mTarget;
	}

	@Override
	public boolean isOpaque() {
		return true;
	}

	@Override
	public void yield() {
		// we cannot free the texture because we have no backup.
	}

	@Override
	protected boolean onBind(final GLCanvas canvas) {
		if (!isLoaded()) {
			uploadToCanvas(canvas);
		}

		return true;
	}

	private void uploadToCanvas(final GLCanvas canvas) {
		final GL11 gl = canvas.getGLInstance();

		final int width = getWidth();
		final int height = getHeight();
		// Define a vertically flipped crop rectangle for OES_draw_texture.
		// The four values in sCropRect are: left, bottom, width, and
		// height. Negative value of width or height means flip.
		sCropRect[0] = 0;
		sCropRect[1] = height;
		sCropRect[2] = width;
		sCropRect[3] = -height;

		// Set texture parameters.
		gl.glBindTexture(mTarget, mId);
		gl.glTexParameterfv(mTarget, GL11Ext.GL_TEXTURE_CROP_RECT_OES, sCropRect, 0);
		gl.glTexParameteri(mTarget, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(mTarget, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(mTarget, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		gl.glTexParameterf(mTarget, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

		setAssociatedCanvas(canvas);
		mState = STATE_LOADED;
	}
}
