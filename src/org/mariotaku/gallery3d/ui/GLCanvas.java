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

import javax.microedition.khronos.opengles.GL11;

import android.graphics.RectF;

//
// GLCanvas gives a convenient interface to draw using OpenGL.
//
// When a rectangle is specified in this interface, it means the region
// [x, x+width) * [y, y+height)
//
public interface GLCanvas {
	public static final int SAVE_FLAG_ALL = 0xFFFFFFFF;

	public static final int SAVE_FLAG_ALPHA = 0x01;
	public static final int SAVE_FLAG_MATRIX = 0x02;

	public void beginRenderTarget(RawTexture texture);

	// Clear the drawing buffers. This should only be used by GLRoot.
	public void clearBuffer();

	public void clearBuffer(float[] argb);

	// Delete the specified buffer object, similar to unloadTexture.
	public void deleteBuffer(int bufferId);

	// Delete the textures and buffers in GL side. This function should only be
	// called in the GL thread.
	public void deleteRecycledResources();

	// Draws a line using the specified paint from (x1, y1) to (x2, y2).
	// (Both end points are included).
	public void drawLine(float x1, float y1, float x2, float y2, GLPaint paint);

	public void drawMesh(BasicTexture tex, int x, int y, int xyBuffer, int uvBuffer, int indexBuffer, int indexCount);

	// Draw two textures to the specified rectangle. The actual texture used is
	// from * (1 - ratio) + to * ratio
	// The two textures must have the same size.
	public void drawMixed(BasicTexture from, int toColor, float ratio, int x, int y, int w, int h);

	// Draw a region of a texture and a specified color to the specified
	// rectangle. The actual color used is from * (1 - ratio) + to * ratio.
	// The region of the texture is defined by parameter "src". The target
	// rectangle is specified by parameter "target".
	public void drawMixed(BasicTexture from, int toColor, float ratio, RectF src, RectF target);

	// Draws a rectangle using the specified paint from (x1, y1) to (x2, y2).
	// (Both end points are included).
	public void drawRect(float x1, float y1, float x2, float y2, GLPaint paint);

	// Draw a texture with a specified texture transform.
	public void drawTexture(BasicTexture texture, float[] mTextureTransform, int x, int y, int w, int h);

	// Draws a texture to the specified rectangle.
	public void drawTexture(BasicTexture texture, int x, int y, int width, int height);

	// Draws the source rectangle part of the texture to the target rectangle.
	public void drawTexture(BasicTexture texture, RectF source, RectF target);

	// Dump statistics information and clear the counters. For debug only.
	public void dumpStatisticsAndClear();

	public void endRenderTarget();

	// Fills the specified rectangle with the specified color.
	public void fillRect(float x, float y, float width, float height, int color);

	public float getAlpha();

	// Gets the underlying GL instance. This is used only when direct access to
	// GL is needed.
	public GL11 getGLInstance();

	// (current alpha) = (current alpha) * alpha
	public void multiplyAlpha(float alpha);

	public void multiplyMatrix(float[] mMatrix, int offset);

	// Pops from the top of the stack as current configuration state (matrix,
	// alpha, and clip). This call balances a previous call to save(), and is
	// used to remove all modifications to the configuration state since the
	// last save call.
	public void restore();

	public void rotate(float angle, float x, float y, float z);

	// Pushes the configuration state (matrix, and alpha) onto
	// a private stack.
	public void save();

	// Same as save(), but only save those specified in saveFlags.
	public void save(int saveFlags);

	public void scale(float sx, float sy, float sz);

	// Sets and gets the current alpha, alpha must be in [0, 1].
	public void setAlpha(float alpha);

	// Tells GLCanvas the size of the underlying GL surface. This should be
	// called before first drawing and when the size of GL surface is changed.
	// This is called by GLRoot and should not be called by the clients
	// who only want to draw on the GLCanvas. Both width and height must be
	// nonnegative.
	public void setSize(int width, int height);

	public void translate(float x, float y);

	// Change the current transform matrix.
	public void translate(float x, float y, float z);

	// Unloads the specified texture from the canvas. The resource allocated
	// to draw the texture will be released. The specified texture will return
	// to the unloaded state. This function should be called only from
	// BasicTexture or its descendant
	public boolean unloadTexture(BasicTexture texture);
}
