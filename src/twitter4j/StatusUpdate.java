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

import twitter4j.http.HttpParameter;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.1
 */
public final class StatusUpdate implements Serializable {

	private static final long serialVersionUID = -2522880289943829826L;
	private final String status;
	private long inReplyToStatusId = -1l;
	private GeoLocation location = null;
	private String placeId = null;
	private boolean displayCoordinates = true;
	private boolean possiblySensitive;
	private String mediaName;
	private transient InputStream mediaBody;
	private File mediaFile;

	public StatusUpdate(final String status) {
		this.status = status;
	}

	public StatusUpdate displayCoordinates(final boolean displayCoordinates) {
		setDisplayCoordinates(displayCoordinates);
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final StatusUpdate that = (StatusUpdate) o;

		if (displayCoordinates != that.displayCoordinates) return false;
		if (inReplyToStatusId != that.inReplyToStatusId) return false;
		if (possiblySensitive != that.possiblySensitive) return false;
		if (location != null ? !location.equals(that.location) : that.location != null) return false;
		if (mediaBody != null ? !mediaBody.equals(that.mediaBody) : that.mediaBody != null) return false;
		if (mediaFile != null ? !mediaFile.equals(that.mediaFile) : that.mediaFile != null) return false;
		if (mediaName != null ? !mediaName.equals(that.mediaName) : that.mediaName != null) return false;
		if (placeId != null ? !placeId.equals(that.placeId) : that.placeId != null) return false;
		if (status != null ? !status.equals(that.status) : that.status != null) return false;

		return true;
	}

	public long getInReplyToStatusId() {
		return inReplyToStatusId;
	}

	public GeoLocation getLocation() {
		return location;
	}

	public String getPlaceId() {
		return placeId;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public int hashCode() {
		int result = status != null ? status.hashCode() : 0;
		result = 31 * result + (int) (inReplyToStatusId ^ inReplyToStatusId >>> 32);
		result = 31 * result + (location != null ? location.hashCode() : 0);
		result = 31 * result + (placeId != null ? placeId.hashCode() : 0);
		result = 31 * result + (displayCoordinates ? 1 : 0);
		result = 31 * result + (possiblySensitive ? 1 : 0);
		result = 31 * result + (mediaName != null ? mediaName.hashCode() : 0);
		result = 31 * result + (mediaBody != null ? mediaBody.hashCode() : 0);
		result = 31 * result + (mediaFile != null ? mediaFile.hashCode() : 0);
		return result;
	}

	public StatusUpdate inReplyToStatusId(final long inReplyToStatusId) {
		setInReplyToStatusId(inReplyToStatusId);
		return this;
	}

	public boolean isDisplayCoordinates() {
		return displayCoordinates;
	}

	/**
	 * @since Twitter4J 2.2.5
	 */
	public boolean isPossiblySensitive() {
		return possiblySensitive;
	}

	public StatusUpdate location(final GeoLocation location) {
		setLocation(location);
		return this;
	}

	/**
	 * @since Twitter4J 2.2.5
	 */
	public StatusUpdate media(final File file) {
		setMedia(file);
		return this;
	}

	/**
	 * @since Twitter4J 2.2.5
	 */
	public StatusUpdate media(final String name, final InputStream body) {
		setMedia(name, body);
		return this;
	}

	public StatusUpdate placeId(final String placeId) {
		setPlaceId(placeId);
		return this;
	}

	/**
	 * @since Twitter4J 2.2.5
	 */
	public StatusUpdate possiblySensitive(final boolean possiblySensitive) {
		setPossiblySensitive(possiblySensitive);
		return this;
	}

	public void setDisplayCoordinates(final boolean displayCoordinates) {
		this.displayCoordinates = displayCoordinates;
	}

	public void setInReplyToStatusId(final long inReplyToStatusId) {
		this.inReplyToStatusId = inReplyToStatusId;
	}

	public void setLocation(final GeoLocation location) {
		this.location = location;
	}

	/**
	 * @since Twitter4J 2.2.5
	 */
	public void setMedia(final File file) {
		mediaFile = file;
	}

	/**
	 * @since Twitter4J 2.2.5
	 */
	public void setMedia(final String name, final InputStream body) {
		mediaName = name;
		mediaBody = body;
	}

	public void setPlaceId(final String placeId) {
		this.placeId = placeId;
	}

	/**
	 * @since Twitter4J 2.2.5
	 */
	public void setPossiblySensitive(final boolean possiblySensitive) {
		this.possiblySensitive = possiblySensitive;
	}

	@Override
	public String toString() {
		return "StatusUpdate{" + "status='" + status + '\'' + ", inReplyToStatusId=" + inReplyToStatusId
				+ ", location=" + location + ", placeId='" + placeId + '\'' + ", displayCoordinates="
				+ displayCoordinates + ", possiblySensitive=" + possiblySensitive + ", mediaName='" + mediaName + '\''
				+ ", mediaBody=" + mediaBody + ", mediaFile=" + mediaFile + '}';
	}

	private void appendParameter(final String name, final double value, final List<HttpParameter> params) {
		params.add(new HttpParameter(name, String.valueOf(value)));
	}

	private void appendParameter(final String name, final long value, final List<HttpParameter> params) {
		params.add(new HttpParameter(name, String.valueOf(value)));
	}

	private void appendParameter(final String name, final String value, final List<HttpParameter> params) {
		if (value != null) {
			params.add(new HttpParameter(name, value));
		}
	}

	/* package */HttpParameter[] asHttpParameterArray(final HttpParameter includeEntities) {
		final ArrayList<HttpParameter> params = new ArrayList<HttpParameter>();
		appendParameter("status", status, params);
		if (-1 != inReplyToStatusId) {
			appendParameter("in_reply_to_status_id", inReplyToStatusId, params);
		}
		if (location != null) {
			appendParameter("lat", location.getLatitude(), params);
			appendParameter("long", location.getLongitude(), params);
		}
		appendParameter("place_id", placeId, params);
		if (!displayCoordinates) {
			appendParameter("display_coordinates", "false", params);
		}
		params.add(includeEntities);
		if (null != mediaFile) {
			params.add(new HttpParameter("media[]", mediaFile));
			params.add(new HttpParameter("possibly_sensitive", possiblySensitive));
		} else if (mediaName != null && mediaBody != null) {
			params.add(new HttpParameter("media[]", mediaName, mediaBody));
			params.add(new HttpParameter("possibly_sensitive", possiblySensitive));
		}

		final HttpParameter[] paramArray = new HttpParameter[params.size()];
		return params.toArray(paramArray);
	}

	/* package */boolean isWithMedia() {
		return mediaFile != null || mediaName != null && mediaBody != null;
	}
}
