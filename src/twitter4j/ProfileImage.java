/*
 * Copyright 2007 Yusuke Yamamoto
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

package twitter4j;

import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.7
 */
public interface ProfileImage extends TwitterResponse {
	ImageSize BIGGER = new ImageSize("bigger");

	ImageSize NORMAL = new ImageSize("normal");
	ImageSize MINI = new ImageSize("mini");
	ImageSize ORIGINAL = new ImageSize("original");

	String getURL();

	static class ImageSize {

		private static final Map<String, ImageSize> instances = new HashMap<String, ImageSize>();

		private final String name;

		private ImageSize() {
			throw new AssertionError();
		}

		private ImageSize(final String name) {
			this.name = name;
			instances.put(name, this);
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			final ImageSize imageSize = (ImageSize) o;

			if (!name.equals(imageSize.name)) return false;

			return true;
		}

		public String getName() {
			return name;
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public String toString() {
			return name;
		}

		private Object readResolve() throws ObjectStreamException {
			return getInstance(name);
		}

		private static ImageSize getInstance(final String name) {
			return instances.get(name);
		}
	}
}
