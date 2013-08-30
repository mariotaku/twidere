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

import static twitter4j.internal.util.InternalParseUtil.getBoolean;
import static twitter4j.internal.util.InternalParseUtil.getHTMLUnescapedString;
import static twitter4j.internal.util.InternalParseUtil.getLong;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

/**
 * A data class that has detailed information about a relationship between two
 * users
 * 
 * @author Perry Sakkaris - psakkaris at gmail.com
 * @see <a href="https://dev.twitter.com/docs/api/1.1/get/friendships/show">GET
 *      friendships/show | Twitter Developers</a>
 * @since Twitter4J 2.1.0
 */
/* package */class RelationshipJSONImpl extends TwitterResponseImpl implements Relationship {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2816753598969317818L;
	private final long targetUserId;
	private final String targetUserScreenName;
	private final boolean sourceBlockingTarget;
	private final boolean sourceNotificationsEnabled;
	private final boolean sourceFollowingTarget;
	private final boolean sourceFollowedByTarget;
	private final long sourceUserId;
	private final String sourceUserScreenName;

	/* package */RelationshipJSONImpl(final HttpResponse res, final Configuration conf) throws TwitterException {
		this(res, res.asJSONObject());
	}

	/* package */RelationshipJSONImpl(final HttpResponse res, final JSONObject json) throws TwitterException {
		super(res);
		try {
			final JSONObject relationship = json.getJSONObject("relationship");
			final JSONObject sourceJson = relationship.getJSONObject("source");
			final JSONObject targetJson = relationship.getJSONObject("target");
			sourceUserId = getLong("id", sourceJson);
			targetUserId = getLong("id", targetJson);
			sourceUserScreenName = getHTMLUnescapedString("screen_name", sourceJson);
			targetUserScreenName = getHTMLUnescapedString("screen_name", targetJson);
			sourceBlockingTarget = getBoolean("blocking", sourceJson);
			sourceFollowingTarget = getBoolean("following", sourceJson);
			sourceFollowedByTarget = getBoolean("followed_by", sourceJson);
			sourceNotificationsEnabled = getBoolean("notifications_enabled", sourceJson);
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

	/* package */RelationshipJSONImpl(final JSONObject json) throws TwitterException {
		this(null, json);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof Relationship)) return false;

		final Relationship that = (Relationship) o;

		if (sourceUserId != that.getSourceUserId()) return false;
		if (targetUserId != that.getTargetUserId()) return false;
		if (!sourceUserScreenName.equals(that.getSourceUserScreenName())) return false;
		if (!targetUserScreenName.equals(that.getTargetUserScreenName())) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSourceUserId() {
		return sourceUserId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSourceUserScreenName() {
		return sourceUserScreenName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getTargetUserId() {
		return targetUserId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTargetUserScreenName() {
		return targetUserScreenName;
	}

	@Override
	public int hashCode() {
		int result = (int) (targetUserId ^ targetUserId >>> 32);
		result = 31 * result + (targetUserScreenName != null ? targetUserScreenName.hashCode() : 0);
		result = 31 * result + (sourceBlockingTarget ? 1 : 0);
		result = 31 * result + (sourceNotificationsEnabled ? 1 : 0);
		result = 31 * result + (sourceFollowingTarget ? 1 : 0);
		result = 31 * result + (sourceFollowedByTarget ? 1 : 0);
		result = 31 * result + (int) (sourceUserId ^ sourceUserId >>> 32);
		result = 31 * result + (sourceUserScreenName != null ? sourceUserScreenName.hashCode() : 0);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSourceBlockingTarget() {
		return sourceBlockingTarget;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSourceFollowedByTarget() {
		return sourceFollowedByTarget;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSourceFollowingTarget() {
		return sourceFollowingTarget;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSourceNotificationsEnabled() {
		return sourceNotificationsEnabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTargetFollowedBySource() {
		return sourceFollowingTarget;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTargetFollowingSource() {
		return sourceFollowedByTarget;
	}

	@Override
	public String toString() {
		return "RelationshipJSONImpl{" + "sourceUserId=" + sourceUserId + ", targetUserId=" + targetUserId
				+ ", sourceUserScreenName='" + sourceUserScreenName + '\'' + ", targetUserScreenName='"
				+ targetUserScreenName + '\'' + ", sourceFollowingTarget=" + sourceFollowingTarget
				+ ", sourceFollowedByTarget=" + sourceFollowedByTarget + ", sourceNotificationsEnabled="
				+ sourceNotificationsEnabled + '}';
	}

	/* package */
	static ResponseList<Relationship> createRelationshipList(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		try {
			final JSONArray list = res.asJSONArray();
			final int size = list.length();
			final ResponseList<Relationship> relationships = new ResponseListImpl<Relationship>(size, res);
			for (int i = 0; i < size; i++) {
				final JSONObject json = list.getJSONObject(i);
				final Relationship relationship = new RelationshipJSONImpl(json);
				relationships.add(relationship);
			}
			return relationships;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}
}
