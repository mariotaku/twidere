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
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class PhotoFallbackEffect extends Animation {

	private static final int ANIM_DURATION = 300;
	private static final Interpolator ANIM_INTERPOLATE = new DecelerateInterpolator(1.5f);

	private PositionProvider mPositionProvider;
	private final ArrayList<Entry> mList = new ArrayList<Entry>();

	public PhotoFallbackEffect() {
		setDuration(ANIM_DURATION);
		setInterpolator(ANIM_INTERPOLATE);
	}

	public void addEntry(final Path path, final Rect rect, final RawTexture texture) {
		mList.add(new Entry(path, rect, texture));
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
