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

import java.util.ArrayList;

import org.mariotaku.gallery3d.anim.Animation;
import org.mariotaku.gallery3d.data.Path;

import android.graphics.Rect;
import android.graphics.RectF;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class PhotoFallbackEffect extends Animation {

	private static final int ANIM_DURATION = 300;
	private static final Interpolator ANIM_INTERPOLATE = new DecelerateInterpolator(1.5f);

	private final RectF mSource = new RectF();

	private final RectF mTarget = new RectF();

	private float mProgress;
	private PositionProvider mPositionProvider;
	private final ArrayList<Entry> mList = new ArrayList<Entry>();

	public PhotoFallbackEffect() {
		setDuration(ANIM_DURATION);
		setInterpolator(ANIM_INTERPOLATE);
	}

	public void addEntry(final Path path, final Rect rect, final RawTexture texture) {
		mList.add(new Entry(path, rect, texture));
	}

	public boolean draw(final GLCanvas canvas) {
		final boolean more = calculate(AnimationTime.get());
		for (int i = 0, n = mList.size(); i < n; ++i) {
			final Entry entry = mList.get(i);
			if (entry.index < 0) {
				continue;
			}
			entry.dest = mPositionProvider.getPosition(entry.index);
			drawEntry(canvas, entry);
		}
		return more;
	}

	public Entry getEntry(final Path path) {
		for (int i = 0, n = mList.size(); i < n; ++i) {
			final Entry entry = mList.get(i);
			if (entry.path == path) return entry;
		}
		return null;
	}

	public void setPositionProvider(final PositionProvider provider) {
		mPositionProvider = provider;
		if (mPositionProvider != null) {
			for (int i = 0, n = mList.size(); i < n; ++i) {
				final Entry entry = mList.get(i);
				entry.index = mPositionProvider.getItemIndex(entry.path);
			}
		}
	}

	@Override
	protected void onCalculate(final float progress) {
		mProgress = progress;
	}

	private void drawEntry(final GLCanvas canvas, final Entry entry) {
		if (!entry.texture.isLoaded()) return;

		final int w = entry.texture.getWidth();
		final int h = entry.texture.getHeight();

		final Rect s = entry.source;
		final Rect d = entry.dest;

		// the following calculation is based on d.width() == d.height()

		final float p = mProgress;

		final float fullScale = (float) d.height() / Math.min(s.width(), s.height());
		final float scale = fullScale * p + 1 * (1 - p);

		final float cx = d.centerX() * p + s.centerX() * (1 - p);
		final float cy = d.centerY() * p + s.centerY() * (1 - p);

		final float ch = s.height() * scale;
		final float cw = s.width() * scale;

		if (w > h) {
			// draw the center part
			mTarget.set(cx - ch / 2, cy - ch / 2, cx + ch / 2, cy + ch / 2);
			mSource.set((w - h) / 2, 0, (w + h) / 2, h);
			canvas.drawTexture(entry.texture, mSource, mTarget);

			canvas.save(GLCanvas.SAVE_FLAG_ALPHA);
			canvas.multiplyAlpha(1 - p);

			// draw the left part
			mTarget.set(cx - cw / 2, cy - ch / 2, cx - ch / 2, cy + ch / 2);
			mSource.set(0, 0, (w - h) / 2, h);
			canvas.drawTexture(entry.texture, mSource, mTarget);

			// draw the right part
			mTarget.set(cx + ch / 2, cy - ch / 2, cx + cw / 2, cy + ch / 2);
			mSource.set((w + h) / 2, 0, w, h);
			canvas.drawTexture(entry.texture, mSource, mTarget);

			canvas.restore();
		} else {
			// draw the center part
			mTarget.set(cx - cw / 2, cy - cw / 2, cx + cw / 2, cy + cw / 2);
			mSource.set(0, (h - w) / 2, w, (h + w) / 2);
			canvas.drawTexture(entry.texture, mSource, mTarget);

			canvas.save(GLCanvas.SAVE_FLAG_ALPHA);
			canvas.multiplyAlpha(1 - p);

			// draw the upper part
			mTarget.set(cx - cw / 2, cy - ch / 2, cx + cw / 2, cy - cw / 2);
			mSource.set(0, 0, w, (h - w) / 2);
			canvas.drawTexture(entry.texture, mSource, mTarget);

			// draw the bottom part
			mTarget.set(cx - cw / 2, cy + cw / 2, cx + cw / 2, cy + ch / 2);
			mSource.set(0, (w + h) / 2, w, h);
			canvas.drawTexture(entry.texture, mSource, mTarget);

			canvas.restore();
		}
	}

	public static class Entry {
		public int index;
		public Path path;
		public Rect source;
		public Rect dest;
		public RawTexture texture;

		public Entry(final Path path, final Rect source, final RawTexture texture) {
			this.path = path;
			this.source = source;
			this.texture = texture;
		}
	}

	public interface PositionProvider {
		public int getItemIndex(Path path);

		public Rect getPosition(int index);
	}
}
