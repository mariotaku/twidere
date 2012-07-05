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

import static twitter4j.internal.util.z_T4JInternalParseUtil.getBoolean;
import static twitter4j.internal.util.z_T4JInternalParseUtil.getDate;
import static twitter4j.internal.util.z_T4JInternalParseUtil.getInt;
import static twitter4j.internal.util.z_T4JInternalParseUtil.getLong;
import static twitter4j.internal.util.z_T4JInternalParseUtil.getRawString;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.PagableResponseList;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.internal.http.HttpResponse;

/**
 * A data class representing Basic user information element
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
/* package */final class UserJSONImpl extends TwitterResponseImpl implements User, java.io.Serializable {

	private long id;
	private String name;
	private String screenName;
	private String location;
	private String description;
	private boolean isContributorsEnabled;
	private String profileImageUrl;
	private String profileImageUrlHttps;
	private String url;
	private boolean isProtected;
	private int followersCount;

	private Status status;

	private String profileBackgroundColor;
	private String profileTextColor;
	private String profileLinkColor;
	private String profileSidebarFillColor;
	private String profileSidebarBorderColor;
	private boolean profileUseBackgroundImage;
	private boolean showAllInlineMedia;
	private int friendsCount;
	private Date createdAt;
	private int favouritesCount;
	private int utcOffset;
	private String timeZone;
	private String profileBackgroundImageUrl;
	private String profileBackgroundImageUrlHttps;
	private boolean profileBackgroundTiled;
	private String lang;
	private int statusesCount;
	private boolean isGeoEnabled;
	private boolean isVerified;
	private boolean translator;
	private int listedCount;
	private boolean isFollowRequestSent;
	private static final long serialVersionUID = -6345893237975349030L;

	/* package */UserJSONImpl(HttpResponse res, Configuration conf) throws TwitterException {
		super(res);
		if (conf.isJSONStoreEnabled()) {
			DataObjectFactoryUtil.clearThreadLocalMap();
		}
		final JSONObject json = res.asJSONObject();
		init(json);
		if (conf.isJSONStoreEnabled()) {
			DataObjectFactoryUtil.registerJSONObject(this, json);
		}
	}

	/* package */UserJSONImpl(JSONObject json) throws TwitterException {
		super();
		init(json);
	}

	@Override
	public int compareTo(User that) {
		return (int) (id - that.getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) return false;
		if (this == obj) return true;
		return obj instanceof User && ((User) obj).getId() == id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFavouritesCount() {
		return favouritesCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFollowersCount() {
		return followersCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFriendsCount() {
		return friendsCount;
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
	public String getLang() {
		return lang;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getListedCount() {
		return listedCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLocation() {
		return location;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProfileBackgroundColor() {
		return profileBackgroundColor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProfileBackgroundImageUrl() {
		return profileBackgroundImageUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProfileBackgroundImageUrlHttps() {
		return profileBackgroundImageUrlHttps;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URL getProfileImageURL() {
		try {
			return new URL(profileImageUrl);
		} catch (final MalformedURLException ex) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URL getProfileImageUrlHttps() {
		if (null == profileImageUrlHttps) return null;
		try {
			return new URL(profileImageUrlHttps);
		} catch (final MalformedURLException ex) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProfileLinkColor() {
		return profileLinkColor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProfileSidebarBorderColor() {
		return profileSidebarBorderColor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProfileSidebarFillColor() {
		return profileSidebarFillColor;
	}

	@Override
	public String getProfileTextColor() {
		return profileTextColor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getScreenName() {
		return screenName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Status getStatus() {
		return status;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStatusesCount() {
		return statusesCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTimeZone() {
		return timeZone;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URL getURL() {
		try {
			return new URL(url);
		} catch (final MalformedURLException ex) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getUtcOffset() {
		return utcOffset;
	}

	@Override
	public int hashCode() {
		return (int) id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isContributorsEnabled() {
		return isContributorsEnabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isFollowRequestSent() {
		return isFollowRequestSent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isGeoEnabled() {
		return isGeoEnabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isProfileBackgroundTiled() {
		return profileBackgroundTiled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isProfileUseBackgroundImage() {
		return profileUseBackgroundImage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isProtected() {
		return isProtected;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isShowAllInlineMedia() {
		return showAllInlineMedia;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTranslator() {
		return translator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isVerified() {
		return isVerified;
	}

	@Override
	public String toString() {
		return "UserJSONImpl{" + "id=" + id + ", name='" + name + '\'' + ", screenName='" + screenName + '\''
				+ ", location='" + location + '\'' + ", description='" + description + '\''
				+ ", isContributorsEnabled=" + isContributorsEnabled + ", profileImageUrl='" + profileImageUrl + '\''
				+ ", profileImageUrlHttps='" + profileImageUrlHttps + '\'' + ", url='" + url + '\'' + ", isProtected="
				+ isProtected + ", followersCount=" + followersCount + ", status=" + status
				+ ", profileBackgroundColor='" + profileBackgroundColor + '\'' + ", profileTextColor='"
				+ profileTextColor + '\'' + ", profileLinkColor='" + profileLinkColor + '\''
				+ ", profileSidebarFillColor='" + profileSidebarFillColor + '\'' + ", profileSidebarBorderColor='"
				+ profileSidebarBorderColor + '\'' + ", profileUseBackgroundImage=" + profileUseBackgroundImage
				+ ", showAllInlineMedia=" + showAllInlineMedia + ", friendsCount=" + friendsCount + ", createdAt="
				+ createdAt + ", favouritesCount=" + favouritesCount + ", utcOffset=" + utcOffset + ", timeZone='"
				+ timeZone + '\'' + ", profileBackgroundImageUrl='" + profileBackgroundImageUrl + '\''
				+ ", profileBackgroundImageUrlHttps='" + profileBackgroundImageUrlHttps + '\''
				+ ", profileBackgroundTiled=" + profileBackgroundTiled + ", lang='" + lang + '\'' + ", statusesCount="
				+ statusesCount + ", isGeoEnabled=" + isGeoEnabled + ", isVerified=" + isVerified + ", translator="
				+ translator + ", listedCount=" + listedCount + ", isFollowRequestSent=" + isFollowRequestSent + '}';
	}

	private void init(JSONObject json) throws TwitterException {
		try {
			id = getLong("id", json);
			name = getRawString("name", json);
			screenName = getRawString("screen_name", json);
			location = getRawString("location", json);
			description = getRawString("description", json);
			isContributorsEnabled = getBoolean("contributors_enabled", json);
			profileImageUrl = getRawString("profile_image_url", json);
			profileImageUrlHttps = getRawString("profile_image_url_https", json);
			url = getRawString("url", json);
			isProtected = getBoolean("protected", json);
			isGeoEnabled = getBoolean("geo_enabled", json);
			isVerified = getBoolean("verified", json);
			translator = getBoolean("is_translator", json);
			followersCount = getInt("followers_count", json);

			profileBackgroundColor = getRawString("profile_background_color", json);
			profileTextColor = getRawString("profile_text_color", json);
			profileLinkColor = getRawString("profile_link_color", json);
			profileSidebarFillColor = getRawString("profile_sidebar_fill_color", json);
			profileSidebarBorderColor = getRawString("profile_sidebar_border_color", json);
			profileUseBackgroundImage = getBoolean("profile_use_background_image", json);
			showAllInlineMedia = getBoolean("show_all_inline_media", json);
			friendsCount = getInt("friends_count", json);
			createdAt = getDate("created_at", json, "EEE MMM dd HH:mm:ss z yyyy");
			favouritesCount = getInt("favourites_count", json);
			utcOffset = getInt("utc_offset", json);
			timeZone = getRawString("time_zone", json);
			profileBackgroundImageUrl = getRawString("profile_background_image_url", json);
			profileBackgroundImageUrlHttps = getRawString("profile_background_image_url_https", json);
			profileBackgroundTiled = getBoolean("profile_background_tile", json);
			lang = getRawString("lang", json);
			statusesCount = getInt("statuses_count", json);
			listedCount = getInt("listed_count", json);
			isFollowRequestSent = getBoolean("follow_request_sent", json);
			if (!json.isNull("status")) {
				final JSONObject statusJSON = json.getJSONObject("status");
				status = new StatusJSONImpl(statusJSON);
			}
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

	/* package */
	static PagableResponseList<User> createPagableUserList(HttpResponse res, Configuration conf)
			throws TwitterException {
		try {
			if (conf.isJSONStoreEnabled()) {
				DataObjectFactoryUtil.clearThreadLocalMap();
			}
			final JSONObject json = res.asJSONObject();
			final JSONArray list = json.getJSONArray("users");
			final int size = list.length();
			@SuppressWarnings("unchecked")
			final PagableResponseList<User> users = new PagableResponseListImpl<User>(size, json, res);
			for (int i = 0; i < size; i++) {
				final JSONObject userJson = list.getJSONObject(i);
				final User user = new UserJSONImpl(userJson);
				if (conf.isJSONStoreEnabled()) {
					DataObjectFactoryUtil.registerJSONObject(user, userJson);
				}
				users.add(user);
			}
			if (conf.isJSONStoreEnabled()) {
				DataObjectFactoryUtil.registerJSONObject(users, json);
			}
			return users;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}

	/* package */
	static ResponseList<User> createUserList(HttpResponse res, Configuration conf) throws TwitterException {
		return createUserList(res.asJSONArray(), res, conf);
	}

	/* package */
	static ResponseList<User> createUserList(JSONArray list, HttpResponse res, Configuration conf)
			throws TwitterException {
		try {
			if (conf.isJSONStoreEnabled()) {
				DataObjectFactoryUtil.clearThreadLocalMap();
			}
			final int size = list.length();
			final ResponseList<User> users = new ResponseListImpl<User>(size, res);
			for (int i = 0; i < size; i++) {
				final JSONObject json = list.getJSONObject(i);
				final User user = new UserJSONImpl(json);
				users.add(user);
				if (conf.isJSONStoreEnabled()) {
					DataObjectFactoryUtil.registerJSONObject(user, json);
				}
			}
			if (conf.isJSONStoreEnabled()) {
				DataObjectFactoryUtil.registerJSONObject(users, list);
			}
			return users;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}
}
