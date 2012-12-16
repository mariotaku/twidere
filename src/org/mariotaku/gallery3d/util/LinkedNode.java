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

package org.mariotaku.gallery3d.util;

public class LinkedNode {
	private LinkedNode mPrev;
	private LinkedNode mNext;

	public LinkedNode() {
		mPrev = mNext = this;
	}

	public void insert(final LinkedNode node) {
		node.mNext = mNext;
		mNext.mPrev = node;
		node.mPrev = this;
		mNext = node;
	}

	public void remove() {
		if (mNext == this) throw new IllegalStateException();
		mPrev.mNext = mNext;
		mNext.mPrev = mPrev;
		mPrev = mNext = null;
	}

	public static <T extends LinkedNode> List<T> newList() {
		return new List<T>();
	}

	@SuppressWarnings("unchecked")
	public static class List<T extends LinkedNode> {
		private final LinkedNode mHead = new LinkedNode();

		public T getFirst() {
			return (T) (mHead.mNext == mHead ? null : mHead.mNext);
		}

		public T getLast() {
			return (T) (mHead.mPrev == mHead ? null : mHead.mPrev);
		}

		public void insertLast(final T node) {
			mHead.mPrev.insert(node);
		}

		public T nextOf(final T node) {
			return (T) (node.mNext == mHead ? null : node.mNext);
		}

		public T previousOf(final T node) {
			return (T) (node.mPrev == mHead ? null : node.mPrev);
		}

	}
}
