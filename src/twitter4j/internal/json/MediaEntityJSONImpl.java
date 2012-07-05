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
package twitter4j.internal.json;

import static twitter4j.internal.util.z_T4JInternalParseUtil.getLong;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.MediaEntity;
import twitter4j.TwitterException;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.3
 */
public class MediaEntityJSONImpl implements MediaEntity {
	private static final long serialVersionUID = -5196438049688518593L;
	private long id;
	private int start = -1;
	private int end = -1;
	private URL url;
	private URL mediaURL;
	private URL mediaURLHttps;
	private URL expandedURL;
	private String displayURL;
	private Map<Integer, MediaEntity.Size> sizes;

	public MediaEntityJSONImpl(JSONObject json) throws TwitterException {
		try {
			final JSONArray indicesArray = json.getJSONArray("indices");
			start = indicesArray.getInt(0);
			end = indicesArray.getInt(1);
			id = getLong("id", json);

			try {
				url = new URL(json.getString("url"));
			} catch (final MalformedURLException ignore) {
			}

			if (!json.isNull("expanded_url")) {
				try {
					expandedURL = new URL(json.getString("expanded_url"));
				} catch (final MalformedURLException ignore) {
				}
			}
			if (!json.isNull("media_url")) {
				try {
					mediaURL = new URL(json.getString("media_url"));
				} catch (final MalformedURLException ignore) {
				}
			}
			if (!json.isNull("media_url_https")) {
				try {
					mediaURLHttps = new URL(json.getString("media_url_https"));
				} catch (final MalformedURLException ignore) {
				}
			}
			if (!json.isNull("display_url")) {
				displayURL = json.getString("display_url");
			}
			final JSONObject sizes = json.getJSONObject("sizes");
			this.sizes = new HashMap<Integer, MediaEntity.Size>(4);
			this.sizes.put(MediaEntity.Size.LARGE, new Size(sizes.getJSONObject("large")));
			this.sizes.put(MediaEntity.Size.MEDIUM, new Size(sizes.getJSONObject("medium")));
			this.sizes.put(MediaEntity.Size.SMALL, new Size(sizes.getJSONObject("small")));
			this.sizes.put(MediaEntity.Size.THUMB, new Size(sizes.getJSONObject("thumb")));
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}

	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MediaEntityJSONImpl)) return false;

		final MediaEntityJSONImpl that = (MediaEntityJSONImpl) o;

		if (id != that.id) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisplayURL() {
		return displayURL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getEnd() {
		return end;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URL getExpandedURL() {
		return expandedURL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URL getMediaURL() {
		return mediaURL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URL getMediaURLHttps() {
		return mediaURLHttps;
	}

	@Override
	public Map<Integer, MediaEntity.Size> getSizes() {
		return sizes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStart() {
		return start;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URL getURL() {
		return url;
	}

	@Override
	public int hashCode() {
		return (int) (id ^ id >>> 32);
	}

	@Override
	public String toString() {
		return "MediaEntityJSONImpl{" + "id=" + id + ", start=" + start + ", end=" + end + ", url=" + url
				+ ", mediaURL=" + mediaURL + ", mediaURLHttps=" + mediaURLHttps + ", expandedURL=" + expandedURL
				+ ", displayURL='" + displayURL + '\'' + ", sizes=" + sizes + '}';
	}

	static class Size implements MediaEntity.Size {
		private static final long serialVersionUID = 8681853416159361581L;
		int width;
		int height;
		int resize;

		Size(JSONObject json) throws JSONException {
			width = json.getInt("w");
			height = json.getInt("h");
			resize = "fit".equals(json.getString("resize")) ? MediaEntity.Size.FIT : MediaEntity.Size.CROP;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Size)) return false;

			final Size size = (Size) o;

			if (height != size.height) return false;
			if (resize != size.resize) return false;
			if (width != size.width) return false;

			return true;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getResize() {
			return resize;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int hashCode() {
			int result = width;
			result = 31 * result + height;
			result = 31 * result + resize;
			return result;
		}

		@Override
		public String toString() {
			return "Size{" + "width=" + width + ", height=" + height + ", resize=" + resize + '}';
		}
	}
}
