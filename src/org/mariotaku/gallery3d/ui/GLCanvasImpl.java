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

package org.mariotaku.gallery3d.ui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import org.mariotaku.gallery3d.common.Utils;
import org.mariotaku.gallery3d.util.IntArray;

import android.graphics.RectF;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

public class GLCanvasImpl implements GLCanvas {
	private static final String TAG = "GLCanvasImp";

	private static final float OPAQUE_ALPHA = 0.95f;

	private static final int OFFSET_FILL_RECT = 0;
	private static final int OFFSET_DRAW_LINE = 4;
	private static final int OFFSET_DRAW_RECT = 6;
	private static final float[] BOX_COORDINATES = { 0, 0, 1, 0, 0, 1, 1, 1, // used
																				// for
																				// filling
																				// a
																				// rectangle
			0, 0, 1, 1, // used for drawing a line
			0, 0, 0, 1, 1, 1, 1, 0 }; // used for drawing the outline of a
										// rectangle

	private final GL11 mGL;

	private final float mMatrixValues[] = new float[16];
	private final float mTextureMatrixValues[] = new float[16];

	// The results of mapPoints are stored in this buffer, and the order is
	// x1, y1, x2, y2.
	private final float mMapPointsBuffer[] = new float[4];

	private final float mTextureColor[] = new float[4];

	private int mBoxCoords;

	private final GLState mGLState;
	private final ArrayList<RawTexture> mTargetStack = new ArrayList<RawTexture>();

	private float mAlpha;
	private final ArrayList<ConfigState> mRestoreStack = new ArrayList<ConfigState>();
	private ConfigState mRecycledRestoreAction;

	private final RectF mDrawTextureSourceRect = new RectF();
	private final RectF mDrawTextureTargetRect = new RectF();
	private final float[] mTempMatrix = new float[32];
	private final IntArray mUnboundTextures = new IntArray();
	private final IntArray mDeleteBuffers = new IntArray();
	private int mScreenWidth;
	private int mScreenHeight;
	private final boolean mBlendEnabled = true;
	private final int mFrameBuffer[] = new int[1];

	private RawTexture mTargetTexture;

	// Drawing statistics
	int mCountDrawLine;
	int mCountFillRect;
	int mCountDrawMesh;
	int mCountTextureRect;
	int mCountTextureOES;

	// TODO: the code only work for 2D should get fixed for 3D or removed
	private static final int MSKEW_X = 4;

	private static final int MSKEW_Y = 1;

	private static final int MSCALE_X = 0;

	private static final int MSCALE_Y = 5;

	GLCanvasImpl(final GL11 gl) {
		mGL = gl;
		mGLState = new GLState(gl);
		initialize();
	}

	@Override
	public void beginRenderTarget(final RawTexture texture) {
		save(); // save matrix and alpha
		mTargetStack.add(mTargetTexture);
		setRenderTarget(texture);
	}

	@Override
	public void clearBuffer() {
		clearBuffer(null);
	}

	@Override
	public void clearBuffer(final float[] argb) {
		if (argb != null && argb.length == 4) {
			mGL.glClearColor(argb[1], argb[2], argb[3], argb[0]);
		} else {
			mGL.glClearColor(0, 0, 0, 1);
		}
		mGL.glClear(GL10.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void deleteBuffer(final int bufferId) {
		synchronized (mUnboundTextures) {
			mDeleteBuffers.add(bufferId);
		}
	}

	@Override
	public void deleteRecycledResources() {
		synchronized (mUnboundTextures) {
			IntArray ids = mUnboundTextures;
			if (ids.size() > 0) {
				GLId.glDeleteTextures(mGL, ids.size(), ids.getInternalArray(), 0);
				ids.clear();
			}

			ids = mDeleteBuffers;
			if (ids.size() > 0) {
				GLId.glDeleteBuffers(mGL, ids.size(), ids.getInternalArray(), 0);
				ids.clear();
			}
		}
	}

	@Override
	public void drawLine(final float x1, final float y1, final float x2, final float y2, final GLPaint paint) {
		final GL11 gl = mGL;

		mGLState.setColorMode(paint.getColor(), mAlpha);
		mGLState.setLineWidth(paint.getLineWidth());

		saveTransform();
		translate(x1, y1);
		scale(x2 - x1, y2 - y1, 1);

		gl.glLoadMatrixf(mMatrixValues, 0);
		gl.glDrawArrays(GL11.GL_LINE_STRIP, OFFSET_DRAW_LINE, 2);

		restoreTransform();
		mCountDrawLine++;
	}

	@Override
	public void drawMesh(final BasicTexture tex, final int x, final int y, final int xyBuffer, final int uvBuffer,
			final int indexBuffer, final int indexCount) {
		final float alpha = mAlpha;
		if (!bindTexture(tex)) return;

		mGLState.setBlendEnabled(mBlendEnabled && (!tex.isOpaque() || alpha < OPAQUE_ALPHA));
		mGLState.setTextureAlpha(alpha);

		// Reset the texture matrix. We will set our own texture coordinates
		// below.
		setTextureCoords(0, 0, 1, 1);

		saveTransform();
		translate(x, y);

		mGL.glLoadMatrixf(mMatrixValues, 0);

		mGL.glBindBuffer(GL11.GL_ARRAY_BUFFER, xyBuffer);
		mGL.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);

		mGL.glBindBuffer(GL11.GL_ARRAY_BUFFER, uvBuffer);
		mGL.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);

		mGL.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		mGL.glDrawElements(GL11.GL_TRIANGLE_STRIP, indexCount, GL11.GL_UNSIGNED_BYTE, 0);

		mGL.glBindBuffer(GL11.GL_ARRAY_BUFFER, mBoxCoords);
		mGL.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);
		mGL.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);

		restoreTransform();
		mCountDrawMesh++;
	}

	@Override
	public void drawMixed(final BasicTexture from, final int toColor, final float ratio, final int x, final int y,
			final int w, final int h) {
		drawMixed(from, toColor, ratio, x, y, w, h, mAlpha);
	}

	@Override
	public void drawMixed(final BasicTexture from, final int toColor, final float ratio, RectF source, RectF target) {
		if (target.width() <= 0 || target.height() <= 0) return;

		if (ratio <= 0.01f) {
			drawTexture(from, source, target);
			return;
		} else if (ratio >= 1) {
			fillRect(target.left, target.top, target.width(), target.height(), toColor);
			return;
		}

		final float alpha = mAlpha;

		// Copy the input to avoid changing it.
		mDrawTextureSourceRect.set(source);
		mDrawTextureTargetRect.set(target);
		source = mDrawTextureSourceRect;
		target = mDrawTextureTargetRect;

		mGLState.setBlendEnabled(mBlendEnabled
				&& (!from.isOpaque() || !Utils.isOpaque(toColor) || alpha < OPAQUE_ALPHA));

		if (!bindTexture(from)) return;

		// Interpolate the RGB and alpha values between both textures.
		mGLState.setTexEnvMode(GL11.GL_COMBINE);
		setMixedColor(toColor, ratio, alpha);
		convertCoordinate(source, target, from);
		setTextureCoords(source);
		textureRect(target.left, target.top, target.width(), target.height());
		mGLState.setTexEnvMode(GL11.GL_REPLACE);
	}

	@Override
	public void drawRect(final float x, final float y, final float width, final float height, final GLPaint paint) {
		final GL11 gl = mGL;

		mGLState.setColorMode(paint.getColor(), mAlpha);
		mGLState.setLineWidth(paint.getLineWidth());

		saveTransform();
		translate(x, y);
		scale(width, height, 1);

		gl.glLoadMatrixf(mMatrixValues, 0);
		gl.glDrawArrays(GL11.GL_LINE_LOOP, OFFSET_DRAW_RECT, 4);

		restoreTransform();
		mCountDrawLine++;
	}

	@Override
	public void drawTexture(final BasicTexture texture, final float[] mTextureTransform, final int x, final int y,
			final int w, final int h) {
		mGLState.setBlendEnabled(mBlendEnabled && (!texture.isOpaque() || mAlpha < OPAQUE_ALPHA));
		if (!bindTexture(texture)) return;
		setTextureCoords(mTextureTransform);
		mGLState.setTextureAlpha(mAlpha);
		textureRect(x, y, w, h);
	}

	@Override
	public void drawTexture(final BasicTexture texture, final int x, final int y, final int width, final int height) {
		drawTexture(texture, x, y, width, height, mAlpha);
	}

	@Override
	public void drawTexture(final BasicTexture texture, RectF source, RectF target) {
		if (target.width() <= 0 || target.height() <= 0) return;

		// Copy the input to avoid changing it.
		mDrawTextureSourceRect.set(source);
		mDrawTextureTargetRect.set(target);
		source = mDrawTextureSourceRect;
		target = mDrawTextureTargetRect;

		mGLState.setBlendEnabled(mBlendEnabled && (!texture.isOpaque() || mAlpha < OPAQUE_ALPHA));
		if (!bindTexture(texture)) return;
		convertCoordinate(source, target, texture);
		setTextureCoords(source);
		mGLState.setTextureAlpha(mAlpha);
		textureRect(target.left, target.top, target.width(), target.height());
	}

	@Override
	public void dumpStatisticsAndClear() {
		final String line = String.format("MESH:%d, TEX_OES:%d, TEX_RECT:%d, FILL_RECT:%d, LINE:%d", mCountDrawMesh,
				mCountTextureRect, mCountTextureOES, mCountFillRect, mCountDrawLine);
		mCountDrawMesh = 0;
		mCountTextureRect = 0;
		mCountTextureOES = 0;
		mCountFillRect = 0;
		mCountDrawLine = 0;
		Log.d(TAG, line);
	}

	@Override
	public void endRenderTarget() {
		final RawTexture texture = mTargetStack.remove(mTargetStack.size() - 1);
		setRenderTarget(texture);
		restore(); // restore matrix and alpha
	}

	@Override
	public void fillRect(final float x, final float y, final float width, final float height, final int color) {
		mGLState.setColorMode(color, mAlpha);
		final GL11 gl = mGL;

		saveTransform();
		translate(x, y);
		scale(width, height, 1);

		gl.glLoadMatrixf(mMatrixValues, 0);
		gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, OFFSET_FILL_RECT, 4);

		restoreTransform();
		mCountFillRect++;
	}

	@Override
	public float getAlpha() {
		return mAlpha;
	}

	@Override
	public GL11 getGLInstance() {
		return mGL;
	}

	@Override
	public void multiplyAlpha(final float alpha) {
		Utils.assertTrue(alpha >= 0 && alpha <= 1);
		mAlpha *= alpha;
	}

	@Override
	public void multiplyMatrix(final float matrix[], final int offset) {
		final float[] temp = mTempMatrix;
		Matrix.multiplyMM(temp, 0, mMatrixValues, 0, matrix, offset);
		System.arraycopy(temp, 0, mMatrixValues, 0, 16);
	}

	@Override
	public void restore() {
		if (mRestoreStack.isEmpty()) throw new IllegalStateException();
		final ConfigState config = mRestoreStack.remove(mRestoreStack.size() - 1);
		config.restore(this);
		freeRestoreConfig(config);
	}

	@Override
	public void rotate(final float angle, final float x, final float y, final float z) {
		if (angle == 0) return;
		final float[] temp = mTempMatrix;
		Matrix.setRotateM(temp, 0, angle, x, y, z);
		Matrix.multiplyMM(temp, 16, mMatrixValues, 0, temp, 0);
		System.arraycopy(temp, 16, mMatrixValues, 0, 16);
	}

	@Override
	public void save() {
		save(SAVE_FLAG_ALL);
	}

	@Override
	public void save(final int saveFlags) {
		final ConfigState config = obtainRestoreConfig();

		if ((saveFlags & SAVE_FLAG_ALPHA) != 0) {
			config.mAlpha = mAlpha;
		} else {
			config.mAlpha = -1;
		}

		if ((saveFlags & SAVE_FLAG_MATRIX) != 0) {
			System.arraycopy(mMatrixValues, 0, config.mMatrix, 0, 16);
		} else {
			config.mMatrix[0] = Float.NEGATIVE_INFINITY;
		}

		mRestoreStack.add(config);
	}

	@Override
	public void scale(final float sx, final float sy, final float sz) {
		Matrix.scaleM(mMatrixValues, 0, sx, sy, sz);
	}

	@Override
	public void setAlpha(final float alpha) {
		Utils.assertTrue(alpha >= 0 && alpha <= 1);
		mAlpha = alpha;
	}

	@Override
	public void setSize(final int width, final int height) {
		Utils.assertTrue(width >= 0 && height >= 0);

		if (mTargetTexture == null) {
			mScreenWidth = width;
			mScreenHeight = height;
		}
		mAlpha = 1.0f;

		final GL11 gl = mGL;
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL11.GL_PROJECTION);
		gl.glLoadIdentity();
		GLU.gluOrtho2D(gl, 0, width, 0, height);

		gl.glMatrixMode(GL11.GL_MODELVIEW);
		gl.glLoadIdentity();

		final float matrix[] = mMatrixValues;
		Matrix.setIdentityM(matrix, 0);
		// to match the graphic coordinate system in android, we flip it
		// vertically.
		if (mTargetTexture == null) {
			Matrix.translateM(matrix, 0, 0, height, 0);
			Matrix.scaleM(matrix, 0, 1, -1, 1);
		}
	}

	// This is a faster version of translate(x, y, z) because
	// (1) we knows z = 0, (2) we inline the Matrix.translateM call,
	// (3) we unroll the loop
	@Override
	public void translate(final float x, final float y) {
		final float[] m = mMatrixValues;
		m[12] += m[0] * x + m[4] * y;
		m[13] += m[1] * x + m[5] * y;
		m[14] += m[2] * x + m[6] * y;
		m[15] += m[3] * x + m[7] * y;
	}

	@Override
	public void translate(final float x, final float y, final float z) {
		Matrix.translateM(mMatrixValues, 0, x, y, z);
	}

	// unloadTexture and deleteBuffer can be called from the finalizer thread,
	// so we synchronized on the mUnboundTextures object.
	@Override
	public boolean unloadTexture(final BasicTexture t) {
		synchronized (mUnboundTextures) {
			if (!t.isLoaded()) return false;
			mUnboundTextures.add(t.mId);
			return true;
		}
	}

	private boolean bindTexture(final BasicTexture texture) {
		if (!texture.onBind(this)) return false;
		final int target = texture.getTarget();
		mGLState.setTextureTarget(target);
		mGL.glBindTexture(target, texture.getId());
		return true;
	}

	private void drawBoundTexture(final BasicTexture texture, int x, int y, int width, int height) {
		// Test whether it has been rotated or flipped, if so, glDrawTexiOES
		// won't work
		if (isMatrixRotatedOrFlipped(mMatrixValues)) {
			if (texture.hasBorder()) {
				setTextureCoords(1.0f / texture.getTextureWidth(), 1.0f / texture.getTextureHeight(),
						(texture.getWidth() - 1.0f) / texture.getTextureWidth(),
						(texture.getHeight() - 1.0f) / texture.getTextureHeight());
			} else {
				setTextureCoords(0, 0, (float) texture.getWidth() / texture.getTextureWidth(),
						(float) texture.getHeight() / texture.getTextureHeight());
			}
			textureRect(x, y, width, height);
		} else {
			// draw the rect from bottom-left to top-right
			final float points[] = mapPoints(mMatrixValues, x, y + height, x + width, y);
			x = (int) (points[0] + 0.5f);
			y = (int) (points[1] + 0.5f);
			width = (int) (points[2] + 0.5f) - x;
			height = (int) (points[3] + 0.5f) - y;
			if (width > 0 && height > 0) {
				((GL11Ext) mGL).glDrawTexiOES(x, y, 0, width, height);
				mCountTextureOES++;
			}
		}
	}

	private void drawMixed(final BasicTexture from, final int toColor, final float ratio, final int x, final int y,
			final int width, final int height, final float alpha) {
		// change from 0 to 0.01f to prevent getting divided by zero below
		if (ratio <= 0.01f) {
			drawTexture(from, x, y, width, height, alpha);
			return;
		} else if (ratio >= 1) {
			fillRect(x, y, width, height, toColor);
			return;
		}

		mGLState.setBlendEnabled(mBlendEnabled
				&& (!from.isOpaque() || !Utils.isOpaque(toColor) || alpha < OPAQUE_ALPHA));

		if (!bindTexture(from)) return;

		// Interpolate the RGB and alpha values between both textures.
		mGLState.setTexEnvMode(GL11.GL_COMBINE);
		setMixedColor(toColor, ratio, alpha);

		drawBoundTexture(from, x, y, width, height);
		mGLState.setTexEnvMode(GL11.GL_REPLACE);
	}

	private void drawTexture(final BasicTexture texture, final int x, final int y, final int width, final int height,
			final float alpha) {
		if (width <= 0 || height <= 0) return;

		mGLState.setBlendEnabled(mBlendEnabled && (!texture.isOpaque() || alpha < OPAQUE_ALPHA));
		if (!bindTexture(texture)) return;
		mGLState.setTextureAlpha(alpha);
		drawBoundTexture(texture, x, y, width, height);
	}

	private void freeRestoreConfig(final ConfigState action) {
		action.mNextFree = mRecycledRestoreAction;
		mRecycledRestoreAction = action;
	}

	private void initialize() {
		final GL11 gl = mGL;

		// First create an nio buffer, then create a VBO from it.
		final int size = BOX_COORDINATES.length * Float.SIZE / Byte.SIZE;
		final FloatBuffer xyBuffer = allocateDirectNativeOrderBuffer(size).asFloatBuffer();
		xyBuffer.put(BOX_COORDINATES, 0, BOX_COORDINATES.length).position(0);

		final int[] name = new int[1];
		GLId.glGenBuffers(1, name, 0);
		mBoxCoords = name[0];

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, mBoxCoords);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, xyBuffer.capacity() * (Float.SIZE / Byte.SIZE), xyBuffer,
				GL11.GL_STATIC_DRAW);

		gl.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);
		gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);

		// Enable the texture coordinate array for Texture 1
		gl.glClientActiveTexture(GL11.GL_TEXTURE1);
		gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
		gl.glClientActiveTexture(GL11.GL_TEXTURE0);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		// mMatrixValues and mAlpha will be initialized in setSize()
	}

	// Transforms two points by the given matrix m. The result
	// {x1', y1', x2', y2'} are stored in mMapPointsBuffer and also returned.
	private float[] mapPoints(final float m[], final int x1, final int y1, final int x2, final int y2) {
		final float[] r = mMapPointsBuffer;

		// Multiply m and (x1 y1 0 1) to produce (x3 y3 z3 w3). z3 is unused.
		final float x3 = m[0] * x1 + m[4] * y1 + m[12];
		final float y3 = m[1] * x1 + m[5] * y1 + m[13];
		final float w3 = m[3] * x1 + m[7] * y1 + m[15];
		r[0] = x3 / w3;
		r[1] = y3 / w3;

		// Same for x2 y2.
		final float x4 = m[0] * x2 + m[4] * y2 + m[12];
		final float y4 = m[1] * x2 + m[5] * y2 + m[13];
		final float w4 = m[3] * x2 + m[7] * y2 + m[15];
		r[2] = x4 / w4;
		r[3] = y4 / w4;

		return r;
	}

	private ConfigState obtainRestoreConfig() {
		if (mRecycledRestoreAction != null) {
			final ConfigState result = mRecycledRestoreAction;
			mRecycledRestoreAction = result.mNextFree;
			return result;
		}
		return new ConfigState();
	}

	private void restoreTransform() {
		System.arraycopy(mTempMatrix, 0, mMatrixValues, 0, 16);
	}

	private void saveTransform() {
		System.arraycopy(mMatrixValues, 0, mTempMatrix, 0, 16);
	}

	private void setMixedColor(final int toColor, final float ratio, final float alpha) {
		//
		// The formula we want:
		// alpha * ((1 - ratio) * from + ratio * to)
		//
		// The formula that GL supports is in the form of:
		// combo * from + (1 - combo) * to * scale
		//
		// So, we have combo = alpha * (1 - ratio)
		// and scale = alpha * ratio / (1 - combo)
		//
		final float combo = alpha * (1 - ratio);
		final float scale = alpha * ratio / (1 - combo);

		// Specify the interpolation factor via the alpha component of
		// GL_TEXTURE_ENV_COLORs.
		// RGB component are get from toColor and will used as SRC1
		final float colorScale = scale * (toColor >>> 24) / (0xff * 0xff);
		setTextureColor((toColor >>> 16 & 0xff) * colorScale, (toColor >>> 8 & 0xff) * colorScale, (toColor & 0xff)
				* colorScale, combo);
		final GL11 gl = mGL;
		gl.glTexEnvfv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, mTextureColor, 0);

		gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_RGB, GL11.GL_INTERPOLATE);
		gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_ALPHA, GL11.GL_INTERPOLATE);
		gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC1_RGB, GL11.GL_CONSTANT);
		gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
		gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC1_ALPHA, GL11.GL_CONSTANT);
		gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA);

		// Wire up the interpolation factor for RGB.
		gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC2_RGB, GL11.GL_CONSTANT);
		gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND2_RGB, GL11.GL_SRC_ALPHA);

		// Wire up the interpolation factor for alpha.
		gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC2_ALPHA, GL11.GL_CONSTANT);
		gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND2_ALPHA, GL11.GL_SRC_ALPHA);

	}

	private void setRenderTarget(final RawTexture texture) {
		final GL11ExtensionPack gl11ep = (GL11ExtensionPack) mGL;

		if (mTargetTexture == null && texture != null) {
			GLId.glGenBuffers(1, mFrameBuffer, 0);
			gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, mFrameBuffer[0]);
		}
		if (mTargetTexture != null && texture == null) {
			gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);
			gl11ep.glDeleteFramebuffersOES(1, mFrameBuffer, 0);
		}

		mTargetTexture = texture;
		if (texture == null) {
			setSize(mScreenWidth, mScreenHeight);
		} else {
			setSize(texture.getWidth(), texture.getHeight());

			if (!texture.isLoaded()) {
				texture.prepare(this);
			}

			gl11ep.glFramebufferTexture2DOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES,
					GL11ExtensionPack.GL_COLOR_ATTACHMENT0_OES, GL11.GL_TEXTURE_2D, texture.getId(), 0);

			checkFramebufferStatus(gl11ep);
		}
	}

	private void setTextureColor(final float r, final float g, final float b, final float alpha) {
		final float[] color = mTextureColor;
		color[0] = r;
		color[1] = g;
		color[2] = b;
		color[3] = alpha;
	}

	private void setTextureCoords(final float left, final float top, final float right, final float bottom) {
		mGL.glMatrixMode(GL11.GL_TEXTURE);
		mTextureMatrixValues[0] = right - left;
		mTextureMatrixValues[5] = bottom - top;
		mTextureMatrixValues[10] = 1;
		mTextureMatrixValues[12] = left;
		mTextureMatrixValues[13] = top;
		mTextureMatrixValues[15] = 1;
		mGL.glLoadMatrixf(mTextureMatrixValues, 0);
		mGL.glMatrixMode(GL11.GL_MODELVIEW);
	}

	private void setTextureCoords(final float[] mTextureTransform) {
		mGL.glMatrixMode(GL11.GL_TEXTURE);
		mGL.glLoadMatrixf(mTextureTransform, 0);
		mGL.glMatrixMode(GL11.GL_MODELVIEW);
	}

	private void setTextureCoords(final RectF source) {
		setTextureCoords(source.left, source.top, source.right, source.bottom);
	}

	private void textureRect(final float x, final float y, final float width, final float height) {
		final GL11 gl = mGL;

		saveTransform();
		translate(x, y);
		scale(width, height, 1);

		gl.glLoadMatrixf(mMatrixValues, 0);
		gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, OFFSET_FILL_RECT, 4);

		restoreTransform();
		mCountTextureRect++;
	}

	private static ByteBuffer allocateDirectNativeOrderBuffer(final int size) {
		return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
	}

	private static void checkFramebufferStatus(final GL11ExtensionPack gl11ep) {
		final int status = gl11ep.glCheckFramebufferStatusOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES);
		if (status != GL11ExtensionPack.GL_FRAMEBUFFER_COMPLETE_OES) {
			String msg = "";
			switch (status) {
				case GL11ExtensionPack.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_OES:
					msg = "FRAMEBUFFER_FORMATS";
					break;
				case GL11ExtensionPack.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_OES:
					msg = "FRAMEBUFFER_ATTACHMENT";
					break;
				case GL11ExtensionPack.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_OES:
					msg = "FRAMEBUFFER_MISSING_ATTACHMENT";
					break;
				case GL11ExtensionPack.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_OES:
					msg = "FRAMEBUFFER_DRAW_BUFFER";
					break;
				case GL11ExtensionPack.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_OES:
					msg = "FRAMEBUFFER_READ_BUFFER";
					break;
				case GL11ExtensionPack.GL_FRAMEBUFFER_UNSUPPORTED_OES:
					msg = "FRAMEBUFFER_UNSUPPORTED";
					break;
				case GL11ExtensionPack.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_OES:
					msg = "FRAMEBUFFER_INCOMPLETE_DIMENSIONS";
					break;
			}
			throw new RuntimeException(msg + ":" + Integer.toHexString(status));
		}
	}

	// This function changes the source coordinate to the texture coordinates.
	// It also clips the source and target coordinates if it is beyond the
	// bound of the texture.
	private static void convertCoordinate(final RectF source, final RectF target, final BasicTexture texture) {

		final int width = texture.getWidth();
		final int height = texture.getHeight();
		final int texWidth = texture.getTextureWidth();
		final int texHeight = texture.getTextureHeight();
		// Convert to texture coordinates
		source.left /= texWidth;
		source.right /= texWidth;
		source.top /= texHeight;
		source.bottom /= texHeight;

		// Clip if the rendering range is beyond the bound of the texture.
		final float xBound = (float) width / texWidth;
		if (source.right > xBound) {
			target.right = target.left + target.width() * (xBound - source.left) / source.width();
			source.right = xBound;
		}
		final float yBound = (float) height / texHeight;
		if (source.bottom > yBound) {
			target.bottom = target.top + target.height() * (yBound - source.top) / source.height();
			source.bottom = yBound;
		}
	}

	private static boolean isMatrixRotatedOrFlipped(final float matrix[]) {
		final float eps = 1e-5f;
		return Math.abs(matrix[MSKEW_X]) > eps || Math.abs(matrix[MSKEW_Y]) > eps || matrix[MSCALE_X] < -eps
				|| matrix[MSCALE_Y] > eps;
	}

	private static class ConfigState {
		float mAlpha;
		float mMatrix[] = new float[16];
		ConfigState mNextFree;

		public void restore(final GLCanvasImpl canvas) {
			if (mAlpha >= 0) {
				canvas.setAlpha(mAlpha);
			}
			if (mMatrix[0] != Float.NEGATIVE_INFINITY) {
				System.arraycopy(mMatrix, 0, canvas.mMatrixValues, 0, 16);
			}
		}
	}

	private static class GLState {

		private final GL11 mGL;

		private int mTexEnvMode = GL11.GL_REPLACE;
		private float mTextureAlpha = 1.0f;
		private int mTextureTarget = GL11.GL_TEXTURE_2D;
		private boolean mBlendEnabled = true;
		private float mLineWidth = 1.0f;

		public GLState(final GL11 gl) {
			mGL = gl;

			// Disable unused state
			gl.glDisable(GL11.GL_LIGHTING);

			// Enable used features
			gl.glEnable(GL11.GL_DITHER);

			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glEnable(GL11.GL_TEXTURE_2D);

			gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);

			// Set the background color
			gl.glClearColor(0f, 0f, 0f, 0f);
			gl.glClearStencil(0);

			gl.glEnable(GL11.GL_BLEND);
			gl.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

			// We use 565 or 8888 format, so set the alignment to 2 bytes/pixel.
			gl.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 2);
		}

		public void setBlendEnabled(final boolean enabled) {
			if (mBlendEnabled == enabled) return;
			mBlendEnabled = enabled;
			if (enabled) {
				mGL.glEnable(GL11.GL_BLEND);
			} else {
				mGL.glDisable(GL11.GL_BLEND);
			}
		}

		public void setColorMode(final int color, final float alpha) {
			setBlendEnabled(!Utils.isOpaque(color) || alpha < OPAQUE_ALPHA);

			// Set mTextureAlpha to an invalid value, so that it will reset
			// again in setTextureAlpha(float) later.
			mTextureAlpha = -1.0f;

			setTextureTarget(0);

			final float prealpha = (color >>> 24) * alpha * 65535f / 255f / 255f;
			mGL.glColor4x(Math.round((color >> 16 & 0xFF) * prealpha), Math.round((color >> 8 & 0xFF) * prealpha),
					Math.round((color & 0xFF) * prealpha), Math.round(255 * prealpha));
		}

		public void setLineWidth(final float width) {
			if (mLineWidth == width) return;
			mLineWidth = width;
			mGL.glLineWidth(width);
		}

		public void setTexEnvMode(final int mode) {
			if (mTexEnvMode == mode) return;
			mTexEnvMode = mode;
			mGL.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, mode);
		}

		public void setTextureAlpha(final float alpha) {
			if (mTextureAlpha == alpha) return;
			mTextureAlpha = alpha;
			if (alpha >= OPAQUE_ALPHA) {
				// The alpha is need for those texture without alpha channel
				mGL.glColor4f(1, 1, 1, 1);
				setTexEnvMode(GL11.GL_REPLACE);
			} else {
				mGL.glColor4f(alpha, alpha, alpha, alpha);
				setTexEnvMode(GL11.GL_MODULATE);
			}
		}

		// target is a value like GL_TEXTURE_2D. If target = 0, texturing is
		// disabled.
		public void setTextureTarget(final int target) {
			if (mTextureTarget == target) return;
			if (mTextureTarget != 0) {
				mGL.glDisable(mTextureTarget);
			}
			mTextureTarget = target;
			if (mTextureTarget != 0) {
				mGL.glEnable(mTextureTarget);
			}
		}
	}
}
