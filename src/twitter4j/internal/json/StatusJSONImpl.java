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
import static twitter4j.internal.util.z_T4JInternalParseUtil.getLong;
import static twitter4j.internal.util.z_T4JInternalParseUtil.getRawString;
import static twitter4j.internal.util.z_T4JInternalParseUtil.getUnescapedString;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Annotations;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.conf.Configuration;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.logging.Logger;

/**
 * A data class representing one single status of a user.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
@SuppressWarnings("deprecation")
final class StatusJSONImpl extends TwitterResponseImpl implements Status {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8982739445731837548L;

	private static final Logger logger = Logger.getLogger();

	private Date createdAt;
	private long id;
	private String text;
	private String rawText;
	private String source;
	private boolean isTruncated;
	private long inReplyToStatusId;
	private long inReplyToUserId;
	private boolean isFavorited;
	private String inReplyToScreenName;
	private GeoLocation geoLocation = null;
	private Place place = null;
	private long retweetCount;
	private boolean wasRetweetedByMe;

	private String[] contributors = null;
	private long[] contributorsIDs;
	private Annotations annotations = null;

	private Status retweetedStatus;
	private UserMentionEntity[] userMentionEntities;
	private URLEntity[] urlEntities;
	private HashtagEntity[] hashtagEntities;
	private MediaEntity[] mediaEntities;
	private Status myRetweetedStatus;

	private User user = null;

	/* package */StatusJSONImpl(final HttpResponse res, final Configuration conf) throws TwitterException {
		super(res);
		final JSONObject json = res.asJSONObject();
		init(json);
	}

	/* package */StatusJSONImpl(final JSONObject json) throws TwitterException {
		super();
		init(json);
	}

	@Override
	public int compareTo(final Status that) {
		final long delta = id - that.getId();
		if (delta < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		else if (delta > Integer.MAX_VALUE) return Integer.MAX_VALUE;
		return (int) delta;
	}

	@Override
	public boolean equals(final Object obj) {
		if (null == obj) return false;
		if (this == obj) return true;
		return obj instanceof Status && ((Status) obj).getId() == id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Annotations getAnnotations() {
		return annotations;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long[] getContributors() {
		if (contributors != null) {
			// http://twitter4j.org/jira/browse/TFJ-592
			// preserving serialized form compatibility with older versions
			contributorsIDs = new long[contributors.length];
			for (int i = 0; i < contributors.length; i++) {
				try {
					contributorsIDs[i] = Long.parseLong(contributors[i]);
				} catch (final NumberFormatException nfe) {
					nfe.printStackTrace();
					logger.warn("failed to parse contributors:" + nfe);
				}
			}
			contributors = null;
		}
		return contributorsIDs;
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
	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HashtagEntity[] getHashtagEntities() {
		return hashtagEntities;
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
	public String getInReplyToScreenName() {
		return inReplyToScreenName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getInReplyToStatusId() {
		return inReplyToStatusId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getInReplyToUserId() {
		return inReplyToUserId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MediaEntity[] getMediaEntities() {
		return mediaEntities;
	}

	@Override
	public Status getMyRetweetedStatus() {
		return myRetweetedStatus;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Place getPlace() {
		return place;
	}

	@Override
	public String getRawText() {
		return rawText;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getRetweetCount() {
		return retweetCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Status getRetweetedStatus() {
		return retweetedStatus;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSource() {
		return source;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URLEntity[] getURLEntities() {
		return urlEntities;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getUser() {
		return user;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserMentionEntity[] getUserMentionEntities() {
		return userMentionEntities;
	}

	@Override
	public int hashCode() {
		return (int) id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isFavorited() {
		return isFavorited;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRetweet() {
		return retweetedStatus != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRetweetedByMe() {
		return wasRetweetedByMe;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTruncated() {
		return isTruncated;
	}

	@Override
	public String toString() {
		return "StatusJSONImpl{createdAt=" + createdAt + ", id=" + id + ", text=" + text + ", rawText=" + rawText
				+ ", source=" + source + ", isTruncated=" + isTruncated + ", inReplyToStatusId=" + inReplyToStatusId
				+ ", inReplyToUserId=" + inReplyToUserId + ", isFavorited=" + isFavorited + ", inReplyToScreenName="
				+ inReplyToScreenName + ", geoLocation=" + geoLocation + ", place=" + place + ", retweetCount="
				+ retweetCount + ", wasRetweetedByMe=" + wasRetweetedByMe + ", contributors=" + contributors
				+ ", contributorsIDs=" + contributorsIDs + ", annotations=" + annotations + ", retweetedStatus="
				+ retweetedStatus + ", userMentionEntities=" + userMentionEntities + ", urlEntities=" + urlEntities
				+ ", hashtagEntities=" + hashtagEntities + ", mediaEntities=" + mediaEntities + ", myRetweetedStatus="
				+ myRetweetedStatus + ", user=" + user + "}";
	}

	private void init(final JSONObject json) throws TwitterException {
		id = getLong("id", json);
		text = getUnescapedString("text", json);
		rawText = getRawString("text", json);
		source = getUnescapedString("source", json);
		createdAt = getDate("created_at", json);
		isTruncated = getBoolean("truncated", json);
		inReplyToStatusId = getLong("in_reply_to_status_id", json);
		inReplyToUserId = getLong("in_reply_to_user_id", json);
		isFavorited = getBoolean("favorited", json);
		inReplyToScreenName = getUnescapedString("in_reply_to_screen_name", json);
		retweetCount = getLong("retweet_count", json);
		try {
			if (!json.isNull("user")) {
				user = new UserJSONImpl(json.getJSONObject("user"));
			}
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
		geoLocation = z_T4JInternalJSONImplFactory.createGeoLocation(json);
		if (!json.isNull("place")) {
			try {
				place = new PlaceJSONImpl(json.getJSONObject("place"));
			} catch (final JSONException ignore) {
				ignore.printStackTrace();
				logger.warn("failed to parse place:" + json);
			}
		}

		if (!json.isNull("retweeted_status")) {
			try {
				retweetedStatus = new StatusJSONImpl(json.getJSONObject("retweeted_status"));
			} catch (final JSONException ignore) {
				ignore.printStackTrace();
				logger.warn("failed to parse retweeted_status:" + json);
			}
		}
		if (!json.isNull("contributors")) {
			try {
				final JSONArray contributorsArray = json.getJSONArray("contributors");
				contributorsIDs = new long[contributorsArray.length()];
				for (int i = 0; i < contributorsArray.length(); i++) {
					contributorsIDs[i] = Long.parseLong(contributorsArray.getString(i));
				}
			} catch (final NumberFormatException ignore) {
				ignore.printStackTrace();
				logger.warn("failed to parse contributors:" + json);
			} catch (final JSONException ignore) {
				ignore.printStackTrace();
				logger.warn("failed to parse contributors:" + json);
			}
		} else {
			contributors = null;
		}
		if (!json.isNull("entities")) {
			try {
				final JSONObject entities = json.getJSONObject("entities");
				int len;
				if (!entities.isNull("user_mentions")) {
					final JSONArray userMentionsArray = entities.getJSONArray("user_mentions");
					len = userMentionsArray.length();
					userMentionEntities = new UserMentionEntity[len];
					for (int i = 0; i < len; i++) {
						userMentionEntities[i] = new UserMentionEntityJSONImpl(userMentionsArray.getJSONObject(i));
					}

				}
				if (!entities.isNull("urls")) {
					final JSONArray urlsArray = entities.getJSONArray("urls");
					len = urlsArray.length();
					urlEntities = new URLEntity[len];
					for (int i = 0; i < len; i++) {
						urlEntities[i] = new URLEntityJSONImpl(urlsArray.getJSONObject(i));
					}
				}

				if (!entities.isNull("hashtags")) {
					final JSONArray hashtagsArray = entities.getJSONArray("hashtags");
					len = hashtagsArray.length();
					hashtagEntities = new HashtagEntity[len];
					for (int i = 0; i < len; i++) {
						hashtagEntities[i] = new HashtagEntityJSONImpl(hashtagsArray.getJSONObject(i));
					}
				}

				if (!entities.isNull("media")) {
					final JSONArray mediaArray = entities.getJSONArray("media");
					len = mediaArray.length();
					mediaEntities = new MediaEntity[len];
					for (int i = 0; i < len; i++) {
						mediaEntities[i] = new MediaEntityJSONImpl(mediaArray.getJSONObject(i));
					}
				}
			} catch (final JSONException jsone) {
				throw new TwitterException(jsone);
			}
		}
		if (!json.isNull("annotations")) {
			try {
				final JSONArray annotationsArray = json.getJSONArray("annotations");
				annotations = new Annotations(annotationsArray);
			} catch (final JSONException ignore) {
			}
		}
		if (!json.isNull("current_user_retweet")) {
			try {
				myRetweetedStatus = new StatusJSONImpl(json.getJSONObject("current_user_retweet"));
				wasRetweetedByMe = true;
			} catch (final JSONException ignore) {
				ignore.printStackTrace();
				logger.warn("failed to parse current_user_retweet:" + json);
			}
		}
	}

	/* package */
	static ResponseList<Status> createStatusList(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		try {
			final JSONArray list = res.asJSONArray();
			final int size = list.length();
			final ResponseList<Status> statuses = new ResponseListImpl<Status>(size, res);
			for (int i = 0; i < size; i++) {
				final JSONObject json = list.getJSONObject(i);
				final Status status = new StatusJSONImpl(json);
				statuses.add(status);
			}
			return statuses;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}
}
