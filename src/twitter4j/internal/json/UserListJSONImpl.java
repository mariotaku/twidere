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
import static twitter4j.internal.util.z_T4JInternalParseUtil.getInt;
import static twitter4j.internal.util.z_T4JInternalParseUtil.getRawString;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.PagableResponseList;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.conf.Configuration;
import twitter4j.internal.http.HttpResponse;

/**
 * A data class representing Basic list information element
 * 
 * @author Dan Checkoway - dcheckoway at gmail.com
 */
/* package */class UserListJSONImpl extends TwitterResponseImpl implements UserList {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2682622238509440140L;
	private int id;
	private String name;
	private String fullName;
	private String slug;
	private String description;
	private int subscriberCount;
	private int memberCount;
	private String uri;
	private boolean mode;
	private User user;
	private boolean following;

	/* package */UserListJSONImpl(final HttpResponse res, final Configuration conf) throws TwitterException {
		super(res);
		init(res.asJSONObject());
	}

	/* package */UserListJSONImpl(final JSONObject json) throws TwitterException {
		super();
		init(json);
	}

	@Override
	public int compareTo(final UserList that) {
		return id - that.getId();
	}

	@Override
	public boolean equals(final Object obj) {
		if (null == obj) return false;
		if (this == obj) return true;
		return obj instanceof UserList && ((UserList) obj).getId() == id;
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
	public String getFullName() {
		return fullName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMemberCount() {
		return memberCount;
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
	public String getSlug() {
		return slug;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSubscriberCount() {
		return subscriberCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI getURI() {
		try {
			return new URI(uri);
		} catch (final URISyntaxException ex) {
			return null;
		}
	}
	
	static PagableResponseList<UserList> createPagableUserListList(HttpResponse res, Configuration conf)
			throws TwitterException {
		try {
			final JSONObject json = res.asJSONObject();
			final JSONArray list = json.getJSONArray("lists");
			final int size = list.length();
			@SuppressWarnings("unchecked")
			final PagableResponseList<UserList> users = (PagableResponseList<UserList>) new PagableResponseListImpl<UserList>(size, json, res);
			for (int i = 0; i < size; i++) {
				final JSONObject userListJson = list.getJSONObject(i);
				final UserList userList = new UserListJSONImpl(userListJson);
				users.add(userList);
			}
			return users;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}

	/* package */


	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getUser() {
		return user;
	}

	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isFollowing() {
		return following;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPublic() {
		return mode;
	}

	@Override
	public String toString() {
		return "UserListJSONImpl{" + "id=" + id + ", name='" + name + '\'' + ", fullName='" + fullName + '\''
				+ ", slug='" + slug + '\'' + ", description='" + description + '\'' + ", subscriberCount="
				+ subscriberCount + ", memberCount=" + memberCount + ", uri='" + uri + '\'' + ", mode=" + mode
				+ ", user=" + user + ", following=" + following + '}';
	}

	private void init(final JSONObject json) throws TwitterException {
		id = getInt("id", json);
		name = getRawString("name", json);
		fullName = getRawString("full_name", json);
		slug = getRawString("slug", json);
		description = getRawString("description", json);
		subscriberCount = getInt("subscriber_count", json);
		memberCount = getInt("member_count", json);
		uri = getRawString("uri", json);
		mode = "public".equals(getRawString("mode", json));
		following = getBoolean("following", json);

		try {
			if (!json.isNull("user")) {
				user = new UserJSONImpl(json.getJSONObject("user"));
			}
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

	/* package */
	static ResponseList<UserList> createUserListList(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		try {
			final JSONArray list = res.asJSONArray();
			final int size = list.length();
			final ResponseList<UserList> users = new ResponseListImpl<UserList>(size, res);
			for (int i = 0; i < size; i++) {
				final JSONObject userListJson = list.getJSONObject(i);
				final UserList userList = new UserListJSONImpl(userListJson);
				users.add(userList);
			}
			return users;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}
}
