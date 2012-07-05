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

import static twitter4j.internal.util.z_T4JInternalParseUtil.getDate;
import static twitter4j.internal.util.z_T4JInternalParseUtil.getLong;
import static twitter4j.internal.util.z_T4JInternalParseUtil.getRawString;
import static twitter4j.internal.util.z_T4JInternalParseUtil.getUnescapedString;

import java.util.Arrays;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Annotations;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.Tweet;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import twitter4j.conf.Configuration;

/**
 * A data class representing a Tweet in the search response
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
@SuppressWarnings("deprecation")
final class TweetJSONImpl implements Tweet, java.io.Serializable {
	private static final long serialVersionUID = 3019285230338056113L;
	private String text;
	private long toUserId = -1;
	private String toUser = null;
	private String fromUser;
	private long id;
	private long fromUserId;
	private String isoLanguageCode = null;
	private String source;
	private String profileImageUrl;
	private Date createdAt;
	private String location;
	private Place place;

	private GeoLocation geoLocation = null;
	private Annotations annotations = null;
	private UserMentionEntity[] userMentionEntities;
	private URLEntity[] urlEntities;
	private HashtagEntity[] hashtagEntities;
	private MediaEntity[] mediaEntities;

	/* package */TweetJSONImpl(JSONObject tweet) throws TwitterException {
		text = getUnescapedString("text", tweet);
		toUserId = getLong("to_user_id", tweet);
		toUser = getRawString("to_user", tweet);
		fromUser = getRawString("from_user", tweet);
		id = getLong("id", tweet);
		fromUserId = getLong("from_user_id", tweet);
		isoLanguageCode = getRawString("iso_language_code", tweet);
		source = getUnescapedString("source", tweet);
		profileImageUrl = getUnescapedString("profile_image_url", tweet);
		createdAt = getDate("created_at", tweet, "EEE, dd MMM yyyy HH:mm:ss z");
		location = getRawString("location", tweet);
		geoLocation = z_T4JInternalJSONImplFactory.createGeoLocation(tweet);
		if (!tweet.isNull("annotations")) {
			try {
				final JSONArray annotationsArray = tweet.getJSONArray("annotations");
				annotations = new Annotations(annotationsArray);
			} catch (final JSONException ignore) {
			}
		}
		if (!tweet.isNull("place")) {
			try {
				place = new PlaceJSONImpl(tweet.getJSONObject("place"));
			} catch (final JSONException jsone) {
				throw new TwitterException(jsone);
			}
		} else {
			place = null;
		}
		if (!tweet.isNull("entities")) {
			try {
				final JSONObject entities = tweet.getJSONObject("entities");
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
	}

	/* package */TweetJSONImpl(JSONObject tweet, Configuration conf) throws TwitterException {
		this(tweet);
		if (conf.isJSONStoreEnabled()) {
			DataObjectFactoryUtil.registerJSONObject(this, tweet);
		}
	}

	@Override
	public int compareTo(Tweet that) {
		final long delta = id - that.getId();
		if (delta < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		else if (delta > Integer.MAX_VALUE) return Integer.MAX_VALUE;
		return (int) delta;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Tweet)) return false;

		final Tweet tweet = (Tweet) o;

		if (id != tweet.getId()) return false;

		return true;
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
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFromUser() {
		return fromUser;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getFromUserId() {
		return fromUserId;
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
	public String getIsoLanguageCode() {
		return isoLanguageCode;
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
	public MediaEntity[] getMediaEntities() {
		return mediaEntities;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Place getPlace() {
		return place;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProfileImageUrl() {
		return profileImageUrl;
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
	public String getToUser() {
		return toUser;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getToUserId() {
		return toUserId;
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
	public UserMentionEntity[] getUserMentionEntities() {
		return userMentionEntities;
	}

	@Override
	public int hashCode() {
		int result = text != null ? text.hashCode() : 0;
		result = 31 * result + (int) (toUserId ^ toUserId >>> 32);
		result = 31 * result + (toUser != null ? toUser.hashCode() : 0);
		result = 31 * result + (fromUser != null ? fromUser.hashCode() : 0);
		result = 31 * result + (int) (id ^ id >>> 32);
		result = 31 * result + (int) (fromUserId ^ fromUserId >>> 32);
		result = 31 * result + (isoLanguageCode != null ? isoLanguageCode.hashCode() : 0);
		result = 31 * result + (source != null ? source.hashCode() : 0);
		result = 31 * result + (profileImageUrl != null ? profileImageUrl.hashCode() : 0);
		result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
		result = 31 * result + (location != null ? location.hashCode() : 0);
		result = 31 * result + (place != null ? place.hashCode() : 0);
		result = 31 * result + (geoLocation != null ? geoLocation.hashCode() : 0);
		result = 31 * result + (annotations != null ? annotations.hashCode() : 0);
		result = 31 * result + (userMentionEntities != null ? Arrays.hashCode(userMentionEntities) : 0);
		result = 31 * result + (urlEntities != null ? Arrays.hashCode(urlEntities) : 0);
		result = 31 * result + (hashtagEntities != null ? Arrays.hashCode(hashtagEntities) : 0);
		result = 31 * result + (mediaEntities != null ? Arrays.hashCode(mediaEntities) : 0);
		return result;
	}

	@Override
	public String toString() {
		return "TweetJSONImpl{" + "text='" + text + '\'' + ", toUserId=" + toUserId + ", toUser='" + toUser + '\''
				+ ", fromUser='" + fromUser + '\'' + ", id=" + id + ", fromUserId=" + fromUserId
				+ ", isoLanguageCode='" + isoLanguageCode + '\'' + ", source='" + source + '\'' + ", profileImageUrl='"
				+ profileImageUrl + '\'' + ", createdAt=" + createdAt + ", location='" + location + '\'' + ", place="
				+ place + ", geoLocation=" + geoLocation + ", annotations=" + annotations + ", userMentionEntities="
				+ (userMentionEntities == null ? null : Arrays.asList(userMentionEntities)) + ", urlEntities="
				+ (urlEntities == null ? null : Arrays.asList(urlEntities)) + ", hashtagEntities="
				+ (hashtagEntities == null ? null : Arrays.asList(hashtagEntities)) + ", mediaEntities="
				+ (mediaEntities == null ? null : Arrays.asList(mediaEntities)) + '}';
	}
}
