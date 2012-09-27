/*
 * Copyright (C) 2007 Yusuke Yamamoto
 * Copyright (C) 2011 Twitter, Inc.
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

import static twitter4j.internal.http.HttpParameter.getParameterArray;
import static twitter4j.internal.util.z_T4JInternalStringUtil.replaceLast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;

import twitter4j.auth.Authorization;
import twitter4j.conf.Configuration;
import twitter4j.internal.http.HttpParameter;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.util.z_T4JInternalStringUtil;

/**
 * A java representation of the <a
 * href="https://dev.twitter.com/docs/api">Twitter REST API</a><br>
 * This class is thread safe and can be cached/re-used and used concurrently.<br>
 * Currently this class is not carefully designed to be extended. It is
 * suggested to extend this class only for mock testing purpose.<br>
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
final class TwitterImpl extends TwitterBaseImpl implements Twitter {

	private final HttpParameter INCLUDE_ENTITIES;

	private final HttpParameter INCLUDE_RTS;

	private final HttpParameter INCLUDE_MY_RETWEET;

	/* package */
	TwitterImpl(final Configuration conf, final Authorization auth) {
		super(conf, auth);
		INCLUDE_ENTITIES = new HttpParameter("include_entities", conf.isIncludeEntitiesEnabled());
		INCLUDE_RTS = new HttpParameter("include_rts", conf.isIncludeRTsEnabled());
		INCLUDE_MY_RETWEET = new HttpParameter("include_my_retweet", 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserList addUserListMember(final int listId, final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + "lists/members/create.json",
				conf.getSigningRestBaseURL() + "lists/members/create.json", new HttpParameter[] {
						new HttpParameter("user_id", userId), new HttpParameter("list_id", listId) }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserList addUserListMembers(final int listId, final long[] userIds) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + "lists/members/create_all.json",
				conf.getSigningRestBaseURL() + "lists/members/create_all.json",
				new HttpParameter[] { new HttpParameter("list_id", listId),
						new HttpParameter("user_id", z_T4JInternalStringUtil.join(userIds)) }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserList addUserListMembers(final int listId, final String[] screenNames) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + "lists/members/create_all.json",
				conf.getSigningRestBaseURL() + "lists/members/create_all.json",
				new HttpParameter[] { new HttpParameter("list_id", listId),
						new HttpParameter("screen_name", z_T4JInternalStringUtil.join(screenNames)) }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User createBlock(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "blocks/create.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&user_id=" + userId, conf.getSigningRestBaseURL() + "blocks/create.json?include_entities="
						+ conf.isIncludeEntitiesEnabled() + "&user_id=" + userId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User createBlock(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "blocks/create.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&screen_name=" + screenName, conf.getSigningRestBaseURL()
						+ "blocks/create.json?include_entities=" + conf.isIncludeEntitiesEnabled() + "&screen_name="
						+ screenName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Status createFavorite(final long id) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatus(post(conf.getRestBaseURL() + "favorites/create/" + id + ".json?include_entities="
				+ conf.isIncludeEntitiesEnabled(), conf.getSigningRestBaseURL() + "favorites/create/" + id
				+ ".json?include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User createFriendship(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "friendships/create.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&user_id=" + userId, conf.getSigningRestBaseURL()
						+ "friendships/create.json?include_entities=" + conf.isIncludeEntitiesEnabled() + "&user_id="
						+ userId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User createFriendship(final long userId, final boolean follow) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "friendships/create.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&user_id=" + userId + "&follow=" + follow, conf.getSigningRestBaseURL()
						+ "friendships/create.json?include_entities=" + conf.isIncludeEntitiesEnabled() + "&user_id="
						+ userId + "&follow=" + follow));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User createFriendship(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "friendships/create.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&screen_name=" + screenName, conf.getSigningRestBaseURL()
						+ "friendships/create.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&screen_name=" + screenName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User createFriendship(final String screenName, final boolean follow) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "friendships/create.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&screen_name=" + screenName + "&follow=" + follow, conf.getSigningRestBaseURL()
						+ "friendships/create.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&screen_name=" + screenName + "&follow=" + follow));
	}

	/* Status Methods */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Place createPlace(final String name, final String containedWithin, final String token,
			final GeoLocation location, final String streetAddress) throws TwitterException {
		ensureAuthorizationEnabled();
		final List<HttpParameter> params = new ArrayList<HttpParameter>(3);
		params.add(new HttpParameter("name", name));
		params.add(new HttpParameter("contained_within", containedWithin));
		params.add(new HttpParameter("token", token));
		params.add(new HttpParameter("lat", location.getLatitude()));
		params.add(new HttpParameter("long", location.getLongitude()));
		if (streetAddress != null) {
			params.add(new HttpParameter("attribute:street_address", streetAddress));
		}
		return factory.createPlace(post(conf.getRestBaseURL() + "geo/place.json", conf.getSigningRestBaseURL()
				+ "geo/place.json", params.toArray(new HttpParameter[params.size()])));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SavedSearch createSavedSearch(final String query) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createSavedSearch(post(conf.getRestBaseURL() + "saved_searches/create.json",
				conf.getSigningRestBaseURL() + "saved_searches/create.json", new HttpParameter[] { new HttpParameter(
						"query", query) }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserList createUserList(final String listName, final boolean isPublicList, final String description)
			throws TwitterException {
		ensureAuthorizationEnabled();
		final List<HttpParameter> httpParams = new ArrayList<HttpParameter>();
		httpParams.add(new HttpParameter("name", listName));
		httpParams.add(new HttpParameter("mode", isPublicList ? "public" : "private"));
		if (description != null) {
			httpParams.add(new HttpParameter("description", description));
		}
		return factory.createAUserList(post(conf.getRestBaseURL() + "lists/create.json", conf.getSigningRestBaseURL()
				+ "lists/create.json", httpParams.toArray(new HttpParameter[httpParams.size()])));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserList createUserListSubscription(final int listId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + "lists/subscribers/create.json",
				conf.getSigningRestBaseURL() + "lists/subscribers/create.json",
				new HttpParameter[] { new HttpParameter("list_id", listId) }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserList deleteUserListMember(final int listId, final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + "lists/members/destroy.json",
				conf.getSigningRestBaseURL() + "lists/members/destroy.json", new HttpParameter[] {
						new HttpParameter("list_id", listId), new HttpParameter("user_id", userId) }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User destroyBlock(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "blocks/destroy.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&user_id=" + userId, conf.getSigningRestBaseURL() + "blocks/destroy.json?include_entities="
						+ conf.isIncludeEntitiesEnabled() + "&user_id=" + userId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User destroyBlock(final String screen_name) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "blocks/destroy.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&screen_name=" + screen_name, conf.getSigningRestBaseURL()
						+ "blocks/destroy.json?include_entities=" + conf.isIncludeEntitiesEnabled() + "&screen_name="
						+ screen_name));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirectMessage destroyDirectMessage(final long id) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createDirectMessage(post(conf.getRestBaseURL() + "direct_messages/destroy/" + id
				+ ".json?include_entities=" + conf.isIncludeEntitiesEnabled(), conf.getSigningRestBaseURL()
				+ "direct_messages/destroy/" + id + ".json?include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Status destroyFavorite(final long id) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatus(post(conf.getRestBaseURL() + "favorites/destroy/" + id + ".json?include_entities="
				+ conf.isIncludeEntitiesEnabled(), conf.getSigningRestBaseURL() + "favorites/destroy/" + id
				+ ".json?include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User destroyFriendship(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "friendships/destroy.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&user_id=" + userId, conf.getSigningRestBaseURL()
						+ "friendships/destroy.json?include_entities=" + conf.isIncludeEntitiesEnabled() + "&user_id="
						+ userId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User destroyFriendship(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "friendships/destroy.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&screen_name=" + screenName, conf.getSigningRestBaseURL()
						+ "friendships/destroy.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&screen_name=" + screenName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SavedSearch destroySavedSearch(final int id) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createSavedSearch(post(conf.getRestBaseURL() + "saved_searches/destroy/" + id + ".json",
				conf.getSigningRestBaseURL() + "saved_searches/destroy/" + id + ".json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Status destroyStatus(final long statusId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatus(post(conf.getRestBaseURL() + "statuses/destroy/" + statusId
				+ ".json?include_entities=" + conf.isIncludeEntitiesEnabled(), conf.getSigningRestBaseURL()
				+ "statuses/destroy/" + statusId + ".json?include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserList destroyUserList(final int listId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + "lists/destroy.json", conf.getSigningRestBaseURL()
				+ "lists/destroy.json", new HttpParameter[] { new HttpParameter("list_id", listId) }));
	}

	/* Local Trends Methods */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserList destroyUserListSubscription(final int listId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + "lists/subscribers/destroy.json",
				conf.getSigningRestBaseURL() + "lists/subscribers/destroy.json",
				new HttpParameter[] { new HttpParameter("list_id", listId) }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User disableNotification(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "notifications/leave.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&user_id=" + userId, conf.getSigningRestBaseURL()
						+ "notifications/leave.json?include_entities=" + conf.isIncludeEntitiesEnabled() + "&user_id="
						+ userId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User disableNotification(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "notifications/leave.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&screen_name=" + screenName, conf.getSigningRestBaseURL()
						+ "notifications/leave.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&screen_name=" + screenName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User enableNotification(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "notifications/follow.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&user_id=" + userId, conf.getSigningRestBaseURL()
						+ "notifications/follow.json?include_entities=" + conf.isIncludeEntitiesEnabled() + "&user_id="
						+ userId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User enableNotification(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "notifications/follow.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&screen_name=" + screenName, conf.getSigningRestBaseURL()
						+ "notifications/follow.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&screen_name=" + screenName));
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		final TwitterImpl twitter = (TwitterImpl) o;

		if (!INCLUDE_ENTITIES.equals(twitter.INCLUDE_ENTITIES)) return false;
		if (!INCLUDE_RTS.equals(twitter.INCLUDE_RTS)) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean existsBlock(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		try {
			return -1 == get(conf.getRestBaseURL() + "blocks/exists.json?user_id=" + userId,
					conf.getSigningRestBaseURL() + "blocks/exists.json?user_id=" + userId).asString().indexOf(
					"<error>You are not blocking this user.</error>");
		} catch (final TwitterException te) {
			if (te.getStatusCode() == 404) return false;
			throw te;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean existsBlock(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		try {
			return -1 == get(conf.getRestBaseURL() + "blocks/exists.json?screen_name=" + screenName,
					conf.getSigningRestBaseURL() + "blocks/exists.json?screen_name=" + screenName).asString().indexOf(
					"You are not blocking this user.");
		} catch (final TwitterException te) {
			if (te.getStatusCode() == 404) return false;
			throw te;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean existsFriendship(final String userA, final String userB) throws TwitterException {
		return -1 != get(conf.getRestBaseURL() + "friendships/exists.json",
				conf.getSigningRestBaseURL() + "friendships/exists.json",
				getParameterArray("user_a", userA, "user_b", userB)).asString().indexOf("true");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountSettings getAccountSettings() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAccountSettings(get(conf.getRestBaseURL() + "account/settings.json",
				conf.getSigningRestBaseURL() + "account/settings.json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountTotals getAccountTotals() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAccountTotals(get(conf.getRestBaseURL() + "account/totals.json",
				conf.getSigningRestBaseURL() + "account/totals.json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Activity> getActivitiesAboutMe() throws TwitterException {
		return getActivitiesAboutMe(null);
	}

	@Override
	public ResponseList<Activity> getActivitiesAboutMe(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String rest_base = conf.getRestBaseURL();
		final String sign_rest_base = conf.getSigningRestBaseURL();
		final String activity_base = rest_base.endsWith("/1/") || rest_base.endsWith("/1.1/") ? replaceLast(rest_base, "\\/1(\\.1)?\\/", "/i/") : rest_base
				+ "i/";
		final String sign_activity_base = sign_rest_base.endsWith("/1/") || rest_base.endsWith("/1.1/") ? replaceLast(sign_rest_base, "\\/1(\\.1)?\\/", "/i/")
				: sign_rest_base + "i/";
		return factory.createActivityList(get(activity_base + "activity/about_me.json", sign_activity_base
				+ "activity/about_me.json",
				mergeParameters(paging != null ? paging.asPostParameterArray() : null, INCLUDE_ENTITIES)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Activity> getActivitiesByFriends() throws TwitterException {
		return getActivitiesByFriends(null);
	}

	@Override
	public ResponseList<Activity> getActivitiesByFriends(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String rest_base = conf.getRestBaseURL();
		final String sign_rest_base = conf.getSigningRestBaseURL();
		final String activity_base = rest_base.endsWith("/1/") || rest_base.endsWith("/1.1/") ? replaceLast(rest_base, "\\/1(\\.1)?\\/", "/i/") : rest_base
				+ "i/";
		final String sign_activity_base = sign_rest_base.endsWith("/1/") || rest_base.endsWith("/1.1/") ? replaceLast(sign_rest_base, "\\/1(\\.1)?\\/", "/i/")
				: sign_rest_base + "i/";
		return factory.createActivityList(get(activity_base + "activity/by_friends.json", sign_activity_base
				+ "activity/by_friends.json",
				mergeParameters(paging != null ? paging.asPostParameterArray() : null, INCLUDE_ENTITIES)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<UserList> getAllUserLists(final long userId) throws TwitterException {
		return factory.createUserListList(get(conf.getRestBaseURL() + "lists/all.json", conf.getSigningRestBaseURL()
				+ "lists/all.json", new HttpParameter[] { new HttpParameter("user_id", userId) }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<UserList> getAllUserLists(final String screenName) throws TwitterException {
		return factory.createUserListList(get(conf.getRestBaseURL() + "lists/all.json", conf.getSigningRestBaseURL()
				+ "lists/all.json", new HttpParameter[] { new HttpParameter("screen_name", screenName) }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TwitterAPIConfiguration getAPIConfiguration() throws TwitterException {
		return factory.createTwitterAPIConfiguration(get(conf.getRestBaseURL() + "help/configuration.json",
				conf.getSigningRestBaseURL() + "help/configuration.json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Location> getAvailableTrends() throws TwitterException {
		return factory.createLocationList(get(conf.getRestBaseURL() + "trends/available.json",
				conf.getSigningRestBaseURL() + "trends/available.json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Location> getAvailableTrends(final GeoLocation location) throws TwitterException {
		return factory.createLocationList(get(conf.getRestBaseURL() + "trends/available.json",
				conf.getSigningRestBaseURL() + "trends/available.json",
				new HttpParameter[] { new HttpParameter("lat", location.getLatitude()),
						new HttpParameter("long", location.getLongitude()) }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<User> getBlockingUsers() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUserList(get(
				conf.getRestBaseURL() + "blocks/blocking.json?include_entities=" + conf.isIncludeEntitiesEnabled(),
				conf.getSigningRestBaseURL() + "blocks/blocking.json?include_entities="
						+ conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<User> getBlockingUsers(final int page) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUserList(get(
				conf.getRestBaseURL() + "blocks/blocking.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&page=" + page, conf.getSigningRestBaseURL() + "blocks/blocking.json?include_entities="
						+ conf.isIncludeEntitiesEnabled() + "&page=" + page));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDs getBlockingUsersIDs() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createIDs(get(conf.getRestBaseURL() + "blocks/blocking/ids.json", conf.getSigningRestBaseURL()
				+ "blocks/blocking/ids.json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Trends> getDailyTrends() throws TwitterException {
		return factory.createTrendsList(get(conf.getRestBaseURL() + "trends/daily.json", conf.getSigningRestBaseURL()
				+ "trends/daily.json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Trends> getDailyTrends(final Date date, final boolean excludeHashTags) throws TwitterException {
		return factory.createTrendsList(get(conf.getRestBaseURL() + "trends/daily.json?date=" + toDateStr(date)
				+ (excludeHashTags ? "&exclude=hashtags" : ""), conf.getSigningRestBaseURL()
				+ "trends/daily.json?date=" + toDateStr(date) + (excludeHashTags ? "&exclude=hashtags" : "")));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<DirectMessage> getDirectMessages() throws TwitterException {
		return getDirectMessages(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<DirectMessage> getDirectMessages(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createDirectMessageList(get(conf.getRestBaseURL() + "direct_messages.json",
				conf.getSigningRestBaseURL() + "direct_messages.json",
				mergeParameters(paging != null ? paging.asPostParameterArray() : null, INCLUDE_ENTITIES)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getFavorites() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(
				conf.getRestBaseURL() + "favorites.json?include_entities=" + conf.isIncludeEntitiesEnabled(),
				conf.getSigningRestBaseURL() + "favorites.json?include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getFavorites(final int page) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "favorites.json", conf.getSigningRestBaseURL()
				+ "favorites.json", new HttpParameter[] { new HttpParameter("page", page), INCLUDE_ENTITIES }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getFavorites(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "favorites.json", conf.getSigningRestBaseURL()
				+ "favorites.json",
				mergeParameters(paging != null ? paging.asPostParameterArray() : null, INCLUDE_ENTITIES)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getFavorites(final String id) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "favorites/" + id + ".json?include_entities="
				+ conf.isIncludeEntitiesEnabled(), conf.getSigningRestBaseURL() + "favorites/" + id
				+ ".json?include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getFavorites(final String id, final int page) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "favorites/" + id + ".json",
				conf.getSigningRestBaseURL() + "favorites/" + id + ".json",
				mergeParameters(getParameterArray("page", page), INCLUDE_ENTITIES)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getFavorites(final String id, final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "favorites/" + id + ".json",
				conf.getSigningRestBaseURL() + "favorites/" + id + ".json",
				mergeParameters(paging.asPostParameterArray(), INCLUDE_ENTITIES)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDs getFollowersIDs(final long cursor) throws TwitterException {
		return factory.createIDs(get(conf.getRestBaseURL() + "followers/ids.json?cursor=" + cursor,
				conf.getSigningRestBaseURL() + "followers/ids.json?cursor=" + cursor));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDs getFollowersIDs(final long userId, final long cursor) throws TwitterException {
		return factory.createIDs(get(conf.getRestBaseURL() + "followers/ids.json?user_id=" + userId + "&cursor="
				+ cursor, conf.getSigningRestBaseURL() + "followers/ids.json?user_id=" + userId + "&cursor=" + cursor));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDs getFollowersIDs(final String screenName, final long cursor) throws TwitterException {
		return factory.createIDs(get(conf.getRestBaseURL() + "followers/ids.json?screen_name=" + screenName
				+ "&cursor=" + cursor, conf.getSigningRestBaseURL() + "followers/ids.json?screen_name=" + screenName
				+ "&cursor=" + cursor));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDs getFriendsIDs(final long cursor) throws TwitterException {
		return factory.createIDs(get(conf.getRestBaseURL() + "friends/ids.json?cursor=" + cursor,
				conf.getSigningRestBaseURL() + "friends/ids.json?cursor=" + cursor));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDs getFriendsIDs(final long userId, final long cursor) throws TwitterException {
		return factory.createIDs(get(
				conf.getRestBaseURL() + "friends/ids.json?user_id=" + userId + "&cursor=" + cursor,
				conf.getSigningRestBaseURL() + "friends/ids.json?user_id=" + userId + "&cursor=" + cursor));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDs getFriendsIDs(final String screenName, final long cursor) throws TwitterException {
		return factory.createIDs(get(conf.getRestBaseURL() + "friends/ids.json?screen_name=" + screenName + "&cursor="
				+ cursor, conf.getSigningRestBaseURL() + "friends/ids.json?screen_name=" + screenName + "&cursor="
				+ cursor));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Place getGeoDetails(final String id) throws TwitterException {
		return factory.createPlace(get(conf.getRestBaseURL() + "geo/id/" + id + ".json", conf.getSigningRestBaseURL()
				+ "geo/id/" + id + ".json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getHomeTimeline() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(
				conf.getRestBaseURL() + "statuses/home_timeline.json?include_my_retweet=1&include_entities="
						+ conf.isIncludeEntitiesEnabled(),
				conf.getSigningRestBaseURL() + "statuses/home_timeline.json?include_my_retweet=1&include_entities="
						+ conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getHomeTimeline(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(
				conf.getRestBaseURL() + "statuses/home_timeline.json",
				conf.getSigningRestBaseURL() + "statuses/home_timeline.json",
				mergeParameters(paging.asPostParameterArray(), new HttpParameter[] { INCLUDE_ENTITIES,
						INCLUDE_MY_RETWEET })));
	}

	/* List Members Methods */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDs getIncomingFriendships(final long cursor) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createIDs(get(conf.getRestBaseURL() + "friendships/incoming.json?cursor=" + cursor,
				conf.getSigningRestBaseURL() + "friendships/incoming.json?cursor=" + cursor));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Language> getLanguages() throws TwitterException {
		return factory.createLanguageList(get(conf.getRestBaseURL() + "help/languages.json",
				conf.getSigningRestBaseURL() + "help/languages.json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Trends getLocationTrends(final int woeid) throws TwitterException {
		return factory.createTrends(get(conf.getRestBaseURL() + "trends/" + woeid + ".json",
				conf.getSigningRestBaseURL() + "trends/" + woeid + ".json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<User> getMemberSuggestions(final String categorySlug) throws TwitterException {
		final HttpResponse res = get(conf.getRestBaseURL() + "users/suggestions/" + categorySlug + "/members.json",
				conf.getSigningRestBaseURL() + "users/suggestions/" + categorySlug + "/members.json");
		return factory.createUserListFromJSONArray(res);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getMentions() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(
				conf.getRestBaseURL() + "statuses/mentions.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&include_rts=" + conf.isIncludeRTsEnabled(), conf.getSigningRestBaseURL()
						+ "statuses/mentions.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&include_rts=" + conf.isIncludeRTsEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getMentions(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "statuses/mentions.json",
				conf.getSigningRestBaseURL() + "statuses/mentions.json",
				mergeParameters(new HttpParameter[] { INCLUDE_RTS, INCLUDE_ENTITIES }, paging.asPostParameterArray())));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDs getNoRetweetIds() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createIDs(get(conf.getRestBaseURL() + "friendships/no_retweet_ids.json",
				conf.getSigningRestBaseURL() + "friendships/no_retweet_ids.json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDs getOutgoingFriendships(final long cursor) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createIDs(get(conf.getRestBaseURL() + "friendships/outgoing.json?cursor=" + cursor,
				conf.getSigningRestBaseURL() + "friendships/outgoing.json?cursor=" + cursor));
	}

	/* Direct Message Methods */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPrivacyPolicy() throws TwitterException {
		try {
			return get(conf.getRestBaseURL() + "legal/privacy.json",
					conf.getSigningRestBaseURL() + "legal/privacy.json").asJSONObject().getString("privacy");
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProfileImage getProfileImage(final String screenName, final ProfileImage.ImageSize size)
			throws TwitterException {
		return factory.createProfileImage(get(conf.getRestBaseURL() + "users/profile_image/" + screenName
				+ ".json?size=" + size.getName(), conf.getSigningRestBaseURL() + "users/profile_image/" + screenName
				+ ".json?size=" + size.getName()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RateLimitStatus getRateLimitStatus() throws TwitterException {
		return factory.createRateLimitStatus(get(conf.getRestBaseURL() + "account/rate_limit_status.json",
				conf.getSigningRestBaseURL() + "account/rate_limit_status.json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RelatedResults getRelatedResults(final long statusId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createRelatedResults(get(
				conf.getRestBaseURL() + "related_results/show/" + Long.toString(statusId) + ".json",
				conf.getSigningRestBaseURL() + "related_results/show/" + Long.toString(statusId) + ".json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<User> getRetweetedBy(final long statusId) throws TwitterException {
		return getRetweetedBy(statusId, new Paging(1, 100));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<User> getRetweetedBy(final long statusId, final Paging paging) throws TwitterException {
		return factory.createUserList(get(conf.getRestBaseURL() + "statuses/" + statusId + "/retweeted_by.json",
				conf.getSigningRestBaseURL() + "statuses/" + statusId + "/retweeted_by.json",
				paging.asPostParameterArray()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDs getRetweetedByIDs(final long statusId) throws TwitterException {
		return getRetweetedByIDs(statusId, new Paging(1, 100));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDs getRetweetedByIDs(final long statusId, final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createIDs(get(conf.getRestBaseURL() + "statuses/" + statusId + "/retweeted_by/ids.json",
				conf.getSigningRestBaseURL() + "statuses/" + statusId + "/retweeted_by/ids.json",
				paging.asPostParameterArray()));
	}

	/* Direct Message Methods */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getRetweetedByMe() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "statuses/retweeted_by_me.json?include_entities="
				+ conf.isIncludeEntitiesEnabled(), conf.getSigningRestBaseURL()
				+ "statuses/retweeted_by_me.json?include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getRetweetedByMe(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "statuses/retweeted_by_me.json",
				conf.getSigningRestBaseURL() + "statuses/retweeted_by_me.json",
				mergeParameters(paging.asPostParameterArray(), INCLUDE_ENTITIES)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getRetweetedByUser(final long userId, final Paging paging) throws TwitterException {
		return factory.createStatusList(get(
				conf.getRestBaseURL() + "statuses/retweeted_by_user.json",
				conf.getSigningRestBaseURL() + "statuses/retweeted_by_user.json",
				mergeParameters(paging.asPostParameterArray(), new HttpParameter[] {
						new HttpParameter("user_id", userId), INCLUDE_ENTITIES })));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getRetweetedByUser(final String screenName, final Paging paging)
			throws TwitterException {
		return factory.createStatusList(get(
				conf.getRestBaseURL() + "statuses/retweeted_by_user.json",
				conf.getSigningRestBaseURL() + "statuses/retweeted_by_user.json",
				mergeParameters(paging.asPostParameterArray(), new HttpParameter[] {
						new HttpParameter("screen_name", screenName), INCLUDE_ENTITIES })));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getRetweetedToMe() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "statuses/retweeted_to_me.json?include_entities="
				+ conf.isIncludeEntitiesEnabled(), conf.getSigningRestBaseURL()
				+ "statuses/retweeted_to_me.json?include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getRetweetedToMe(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "statuses/retweeted_to_me.json",
				conf.getSigningRestBaseURL() + "statuses/retweeted_to_me.json",
				mergeParameters(paging.asPostParameterArray(), INCLUDE_ENTITIES)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getRetweetedToUser(final long userId, final Paging paging) throws TwitterException {
		return factory.createStatusList(get(
				conf.getRestBaseURL() + "statuses/retweeted_to_user.json",
				conf.getSigningRestBaseURL() + "statuses/retweeted_to_user.json",
				mergeParameters(paging.asPostParameterArray(), new HttpParameter[] {
						new HttpParameter("user_id", userId), INCLUDE_ENTITIES })));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getRetweetedToUser(final String screenName, final Paging paging)
			throws TwitterException {
		return factory.createStatusList(get(
				conf.getRestBaseURL() + "statuses/retweeted_to_user.json",
				conf.getSigningRestBaseURL() + "statuses/retweeted_to_user.json",
				mergeParameters(paging.asPostParameterArray(), new HttpParameter[] {
						new HttpParameter("screen_name", screenName), INCLUDE_ENTITIES })));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getRetweets(final long statusId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "statuses/retweets/" + statusId
				+ ".json?count=100&include_entities=" + conf.isIncludeEntitiesEnabled(),
				conf.getSigningRestBaseURL() + "statuses/retweets/" + statusId + ".json?count=100&include_entities="
						+ conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getRetweetsOfMe() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "statuses/retweets_of_me.json?include_entities="
				+ conf.isIncludeEntitiesEnabled(), conf.getSigningRestBaseURL()
				+ "statuses/retweets_of_me.json?include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getRetweetsOfMe(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "statuses/retweets_of_me.json",
				conf.getSigningRestBaseURL() + "statuses/retweets_of_me.json",
				mergeParameters(paging.asPostParameterArray(), INCLUDE_ENTITIES)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<SavedSearch> getSavedSearches() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createSavedSearchList(get(conf.getRestBaseURL() + "saved_searches.json",
				conf.getSigningRestBaseURL() + "saved_searches.json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<DirectMessage> getSentDirectMessages() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory
				.createDirectMessageList(get(conf.getRestBaseURL() + "direct_messages/sent.json?include_entities="
						+ conf.isIncludeEntitiesEnabled(), conf.getSigningRestBaseURL()
						+ "direct_messages/sent.json?include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<DirectMessage> getSentDirectMessages(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createDirectMessageList(get(conf.getRestBaseURL() + "direct_messages/sent.json",
				conf.getSigningRestBaseURL() + "direct_messages/sent.json",
				mergeParameters(paging.asPostParameterArray(), INCLUDE_ENTITIES)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SimilarPlaces getSimilarPlaces(final GeoLocation location, final String name, final String containedWithin,
			final String streetAddress) throws TwitterException {
		final List<HttpParameter> params = new ArrayList<HttpParameter>(3);
		params.add(new HttpParameter("lat", location.getLatitude()));
		params.add(new HttpParameter("long", location.getLongitude()));
		params.add(new HttpParameter("name", name));
		if (containedWithin != null) {
			params.add(new HttpParameter("contained_within", containedWithin));
		}
		if (streetAddress != null) {
			params.add(new HttpParameter("attribute:street_address", streetAddress));
		}
		return factory.createSimilarPlaces(get(conf.getRestBaseURL() + "geo/similar_places.json",
				conf.getSigningRestBaseURL() + "geo/similar_places.json",
				params.toArray(new HttpParameter[params.size()])));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Category> getSuggestedUserCategories() throws TwitterException {
		return factory.createCategoryList(get(conf.getRestBaseURL() + "users/suggestions.json",
				conf.getSigningRestBaseURL() + "users/suggestions.json"));
	}

	/* Social Graph Methods */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTermsOfService() throws TwitterException {
		try {
			return get(conf.getRestBaseURL() + "legal/tos.json", conf.getSigningRestBaseURL() + "legal/tos.json")
					.asJSONObject().getString("tos");
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PagableResponseList<User> getUserListMembers(final int listId, final long cursor) throws TwitterException {
		return factory.createPagableUserList(get(
				conf.getRestBaseURL() + "lists/members.json?list_id=" + listId + "&include_entities="
						+ conf.isIncludeEntitiesEnabled() + "&cursor=" + cursor,
				conf.getSigningRestBaseURL() + "lists/members.json?list_id=" + listId + "&include_entities="
						+ conf.isIncludeEntitiesEnabled() + "&cursor=" + cursor));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PagableResponseList<UserList> getUserListMemberships(final long cursor) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createPagableUserListList(get(conf.getRestBaseURL() + "lists/memberships.json?cursor=" + cursor,
				conf.getSigningRestBaseURL() + "lists/memberships.json?cursor=" + cursor));

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PagableResponseList<UserList> getUserListMemberships(final long listMemberId, final long cursor)
			throws TwitterException {
		return getUserListMemberships(listMemberId, cursor, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PagableResponseList<UserList> getUserListMemberships(final long listMemberId, final long cursor,
			final boolean filterToOwnedLists) throws TwitterException {
		if (filterToOwnedLists) {
			ensureAuthorizationEnabled();
		}
		return factory.createPagableUserListList(get(conf.getRestBaseURL() + "lists/memberships.json?user_id="
				+ listMemberId + "&cursor=" + cursor + "&filter_to_owned_lists=" + filterToOwnedLists,
				conf.getSigningRestBaseURL() + "lists/memberships.json?user_id=" + listMemberId + "&cursor=" + cursor
						+ "&filter_to_owned_lists=" + filterToOwnedLists));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PagableResponseList<UserList> getUserListMemberships(final String listMemberScreenName, final long cursor)
			throws TwitterException {
		return getUserListMemberships(listMemberScreenName, cursor, false);
	}

	/* Social Graph Methods */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PagableResponseList<UserList> getUserListMemberships(final String listMemberScreenName, final long cursor,
			final boolean filterToOwnedLists) throws TwitterException {
		if (filterToOwnedLists) {
			ensureAuthorizationEnabled();
		}
		return factory.createPagableUserListList(get(conf.getRestBaseURL() + "lists/memberships.json?screen_name="
				+ listMemberScreenName + "&cursor=" + cursor + "&filter_to_owned_lists=" + filterToOwnedLists,
				conf.getSigningRestBaseURL() + "lists/memberships.json?screen_name=" + listMemberScreenName
						+ "&cursor=" + cursor + "&filter_to_owned_lists=" + filterToOwnedLists));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<UserList> getUserLists(final long listOwnerUserId) throws TwitterException {
		return factory.createUserListList(get(conf.getRestBaseURL() + "lists/list.json", conf.getSigningRestBaseURL()
				+ "lists/list.json", new HttpParameter[] { new HttpParameter("user_id", listOwnerUserId) }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<UserList> getUserLists(final String listOwnerScreenName) throws TwitterException {
		return factory.createUserListList(get(conf.getRestBaseURL() + "lists/list.json", conf.getSigningRestBaseURL()
				+ "lists/list.json", new HttpParameter[] { new HttpParameter("screen_name", listOwnerScreenName) }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getUserListStatuses(final int listId, final Paging paging) throws TwitterException {
		return factory.createStatusList(get(
				conf.getRestBaseURL() + "lists/statuses.json",
				conf.getSigningRestBaseURL() + "lists/statuses.json",
				mergeParameters(paging.asPostParameterArray(Paging.SMCP, Paging.PER_PAGE), new HttpParameter[] {
						new HttpParameter("list_id", listId), INCLUDE_ENTITIES, INCLUDE_RTS })));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PagableResponseList<User> getUserListSubscribers(final int listId, final long cursor)
			throws TwitterException {
		return factory.createPagableUserList(get(
				conf.getRestBaseURL() + "lists/subscribers.json?list_id=" + listId + "&include_entities="
						+ conf.isIncludeEntitiesEnabled() + "&cursor=" + cursor,
				conf.getSigningRestBaseURL() + "lists/subscribers.json?list_id=" + listId + "&include_entities="
						+ conf.isIncludeEntitiesEnabled() + "&cursor=" + cursor));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PagableResponseList<UserList> getUserListSubscriptions(final String listOwnerScreenName, final long cursor)
			throws TwitterException {
		return factory.createPagableUserListList(get(conf.getRestBaseURL() + "lists/subscriptions.json?screen_name="
				+ listOwnerScreenName + "&cursor=" + cursor, conf.getSigningRestBaseURL()
				+ "lists/subscriptions.json?screen_name=" + listOwnerScreenName + "&cursor=" + cursor));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<User> getUserSuggestions(final String categorySlug) throws TwitterException {
		final HttpResponse res = get(conf.getRestBaseURL() + "users/suggestions/" + categorySlug + ".json",
				conf.getSigningRestBaseURL() + "users/suggestions/" + categorySlug + ".json");
		return factory.createUserListFromJSONArray_Users(res);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getUserTimeline() throws TwitterException {
		return getUserTimeline(new Paging());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getUserTimeline(final long userId) throws TwitterException {
		return getUserTimeline(userId, new Paging());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getUserTimeline(final long userId, final Paging paging) throws TwitterException {
		return factory.createStatusList(get(
				conf.getRestBaseURL() + "statuses/user_timeline.json",
				conf.getSigningRestBaseURL() + "statuses/user_timeline.json",
				mergeParameters(new HttpParameter[] { new HttpParameter("user_id", userId), INCLUDE_RTS,
						INCLUDE_ENTITIES, INCLUDE_MY_RETWEET }, paging.asPostParameterArray())));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getUserTimeline(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(
				conf.getRestBaseURL() + "statuses/user_timeline.json",
				conf.getSigningRestBaseURL() + "statuses/user_timeline.json",
				mergeParameters(new HttpParameter[] { INCLUDE_RTS, INCLUDE_ENTITIES, INCLUDE_MY_RETWEET },
						paging.asPostParameterArray())));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getUserTimeline(final String screenName) throws TwitterException {
		return getUserTimeline(screenName, new Paging());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Status> getUserTimeline(final String screenName, final Paging paging) throws TwitterException {
		return factory.createStatusList(get(
				conf.getRestBaseURL() + "statuses/user_timeline.json",
				conf.getSigningRestBaseURL() + "statuses/user_timeline.json",
				mergeParameters(new HttpParameter[] { new HttpParameter("screen_name", screenName), INCLUDE_RTS,
						INCLUDE_ENTITIES, INCLUDE_MY_RETWEET }, paging.asPostParameterArray())));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Trends> getWeeklyTrends() throws TwitterException {
		return factory.createTrendsList(get(conf.getRestBaseURL() + "trends/weekly.json", conf.getSigningRestBaseURL()
				+ "trends/weekly.json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Trends> getWeeklyTrends(final Date date, final boolean excludeHashTags) throws TwitterException {
		return factory.createTrendsList(get(conf.getRestBaseURL() + "trends/weekly.json?date=" + toDateStr(date)
				+ (excludeHashTags ? "&exclude=hashtags" : ""), conf.getSigningRestBaseURL()
				+ "trends/weekly.json?date=" + toDateStr(date) + (excludeHashTags ? "&exclude=hashtags" : "")));
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + INCLUDE_ENTITIES.hashCode();
		result = 31 * result + INCLUDE_RTS.hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Friendship> lookupFriendships(final long[] ids) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createFriendshipList(get(conf.getRestBaseURL() + "friendships/lookup.json?user_id="
				+ z_T4JInternalStringUtil.join(ids), conf.getSigningRestBaseURL() + "friendships/lookup.json?user_id="
				+ z_T4JInternalStringUtil.join(ids)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Friendship> lookupFriendships(final String[] screenNames) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createFriendshipList(get(conf.getRestBaseURL() + "friendships/lookup.json?screen_name="
				+ z_T4JInternalStringUtil.join(screenNames), conf.getSigningRestBaseURL()
				+ "friendships/lookup.json?screen_name=" + z_T4JInternalStringUtil.join(screenNames)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<User> lookupUsers(final long[] ids) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory
				.createUserList(get(conf.getRestBaseURL() + "users/lookup.json", conf.getSigningRestBaseURL()
						+ "users/lookup.json", new HttpParameter[] {
						new HttpParameter("user_id", z_T4JInternalStringUtil.join(ids)), INCLUDE_ENTITIES }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<User> lookupUsers(final String[] screenNames) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUserList(get(conf.getRestBaseURL() + "users/lookup.json", conf.getSigningRestBaseURL()
				+ "users/lookup.json",
				new HttpParameter[] { new HttpParameter("screen_name", z_T4JInternalStringUtil.join(screenNames)),
						INCLUDE_ENTITIES }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User reportSpam(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "report_spam.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&user_id=" + userId, conf.getSigningRestBaseURL() + "report_spam.json?include_entities="
						+ conf.isIncludeEntitiesEnabled() + "&user_id=" + userId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User reportSpam(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(
				conf.getRestBaseURL() + "report_spam.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&screen_name=" + screenName, conf.getSigningRestBaseURL()
						+ "report_spam.json?include_entities=" + conf.isIncludeEntitiesEnabled() + "&screen_name="
						+ screenName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Status retweetStatus(final long statusId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatus(post(conf.getRestBaseURL() + "statuses/retweet/" + statusId
				+ ".json?include_entities=" + conf.isIncludeEntitiesEnabled(), conf.getSigningRestBaseURL()
				+ "statuses/retweet/" + statusId + ".json?include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Place> reverseGeoCode(final GeoQuery query) throws TwitterException {
		try {
			return factory.createPlaceList(get(conf.getRestBaseURL() + "geo/reverse_geocode.json",
					conf.getSigningRestBaseURL() + "geo/reverse_geocode.json", query.asHttpParameterArray()));
		} catch (final TwitterException te) {
			if (te.getStatusCode() == 404)
				return factory.createEmptyResponseList();
			else
				throw te;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QueryResult search(final Query query) throws TwitterException {
		return factory.createQueryResult(
				get(conf.getRestBaseURL() + "search/tweets.json", conf.getSigningRestBaseURL() + "search/tweets.json",
						query.asHttpParameterArray(INCLUDE_ENTITIES)), query);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<Place> searchPlaces(final GeoQuery query) throws TwitterException {
		return factory.createPlaceList(get(conf.getRestBaseURL() + "geo/search.json", conf.getSigningRestBaseURL()
				+ "geo/search.json", query.asHttpParameterArray()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseList<User> searchUsers(final String query, final int page) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUserList(get(conf.getRestBaseURL() + "users/search.json", conf.getSigningRestBaseURL()
				+ "users/search.json", new HttpParameter[] { new HttpParameter("q", query),
				new HttpParameter("per_page", 20), new HttpParameter("page", page), INCLUDE_ENTITIES }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirectMessage sendDirectMessage(final long userId, final String text) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createDirectMessage(post(conf.getRestBaseURL() + "direct_messages/new.json",
				conf.getSigningRestBaseURL() + "direct_messages/new.json", new HttpParameter[] {
						new HttpParameter("user_id", userId), new HttpParameter("text", text), INCLUDE_ENTITIES }));
	}

	/* Block Methods */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirectMessage sendDirectMessage(final String screenName, final String text) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createDirectMessage(post(conf.getRestBaseURL() + "direct_messages/new.json",
				conf.getSigningRestBaseURL() + "direct_messages/new.json",
				new HttpParameter[] { new HttpParameter("screen_name", screenName), new HttpParameter("text", text),
						INCLUDE_ENTITIES }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirectMessage showDirectMessage(final long id) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createDirectMessage(get(conf.getRestBaseURL() + "direct_messages/show/" + id
				+ ".json?include_entities=" + conf.isIncludeEntitiesEnabled(), conf.getSigningRestBaseURL()
				+ "direct_messages/show/" + id + ".json?include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relationship showFriendship(final long sourceId, final long targetId) throws TwitterException {
		return factory.createRelationship(get(conf.getRestBaseURL() + "friendships/show.json",
				conf.getSigningRestBaseURL() + "friendships/show.json", new HttpParameter[] {
						new HttpParameter("source_id", sourceId), new HttpParameter("target_id", targetId) }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relationship showFriendship(final String sourceScreenName, final String targetScreenName)
			throws TwitterException {
		return factory.createRelationship(get(conf.getRestBaseURL() + "friendships/show.json",
				conf.getSigningRestBaseURL() + "friendships/show.json",
				getParameterArray("source_screen_name", sourceScreenName, "target_screen_name", targetScreenName)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SavedSearch showSavedSearch(final int id) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createSavedSearch(get(conf.getRestBaseURL() + "saved_searches/show/" + id + ".json",
				conf.getSigningRestBaseURL() + "saved_searches/show/" + id + ".json"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Status showStatus(final long id) throws TwitterException {
		return factory.createStatus(get(conf.getRestBaseURL() + "statuses/show/" + id + ".json?include_entities="
				+ conf.isIncludeEntitiesEnabled(), conf.getSigningRestBaseURL() + "statuses/show/" + id
				+ ".json?include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User showUser(final long userId) throws TwitterException {
		return factory.createUser(get(
				conf.getRestBaseURL() + "users/show.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&user_id=" + userId, conf.getSigningRestBaseURL() + "users/show.json?include_entities="
						+ conf.isIncludeEntitiesEnabled() + "&user_id=" + userId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User showUser(final String screenName) throws TwitterException {
		return factory.createUser(get(
				conf.getRestBaseURL() + "users/show.json?include_entities=" + conf.isIncludeEntitiesEnabled()
						+ "&screen_name=" + screenName, conf.getSigningRestBaseURL()
						+ "users/show.json?include_entities=" + conf.isIncludeEntitiesEnabled() + "&screen_name="
						+ screenName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserList showUserList(final int listId) throws TwitterException {
		return factory.createAUserList(get(conf.getRestBaseURL() + "lists/show.json?list_id=" + listId,
				conf.getSigningRestBaseURL() + "lists/show.json?list_id=" + listId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User showUserListMembership(final int listId, final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(get(conf.getRestBaseURL() + "lists/members/show.json?list_id=" + listId + "&user_id="
				+ userId + "&include_entities=" + conf.isIncludeEntitiesEnabled(),
				conf.getSigningRestBaseURL() + "lists/members/show.json?list_id=" + listId + "&user_id=" + userId
						+ "&include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User showUserListSubscription(final int listId, final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(get(conf.getRestBaseURL() + "lists/subscribers/show.json?list_id=" + listId
				+ "&user_id=" + userId + "&include_entities=" + conf.isIncludeEntitiesEnabled(),
				conf.getSigningRestBaseURL() + "lists/subscribers/show.json?list_id=" + listId + "&user_id=" + userId
						+ "&include_entities=" + conf.isIncludeEntitiesEnabled()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test() throws TwitterException {
		return -1 != get(conf.getRestBaseURL() + "help/test.json", conf.getSigningRestBaseURL() + "help/test.json")
				.asString().indexOf("ok");
	}

	@Override
	public String toString() {
		return "TwitterImpl{" + "INCLUDE_ENTITIES=" + INCLUDE_ENTITIES + ", INCLUDE_RTS=" + INCLUDE_RTS + '}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountSettings updateAccountSettings(final Integer trend_locationWoeid, final Boolean sleep_timeEnabled,
			final String start_sleepTime, final String end_sleepTime, final String time_zone, final String lang)
			throws TwitterException {

		ensureAuthorizationEnabled();

		final List<HttpParameter> profile = new ArrayList<HttpParameter>(6);
		if (trend_locationWoeid != null) {
			profile.add(new HttpParameter("trend_location_woeid", trend_locationWoeid));
		}
		if (sleep_timeEnabled != null) {
			profile.add(new HttpParameter("sleep_time_enabled", sleep_timeEnabled.toString()));
		}
		if (start_sleepTime != null) {
			profile.add(new HttpParameter("start_sleep_time", start_sleepTime));
		}
		if (end_sleepTime != null) {
			profile.add(new HttpParameter("end_sleep_time", end_sleepTime));
		}
		if (time_zone != null) {
			profile.add(new HttpParameter("time_zone", time_zone));
		}
		if (lang != null) {
			profile.add(new HttpParameter("lang", lang));
		}

		profile.add(INCLUDE_ENTITIES);
		return factory.createAccountSettings(post(conf.getRestBaseURL() + "account/settings.json",
				conf.getSigningRestBaseURL() + "account/settings.json",
				profile.toArray(new HttpParameter[profile.size()])));

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relationship updateFriendship(final long userId, final boolean enableDeviceNotification,
			final boolean retweets) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createRelationship(post(conf.getRestBaseURL() + "friendships/update.json",
				conf.getSigningRestBaseURL() + "friendships/update.json", new HttpParameter[] {
						new HttpParameter("user_id", userId), new HttpParameter("device", enableDeviceNotification),
						new HttpParameter("retweets", enableDeviceNotification) }));
	}

	/* Geo Methods */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relationship updateFriendship(final String screenName, final boolean enableDeviceNotification,
			final boolean retweets) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createRelationship(post(conf.getRestBaseURL() + "friendships/update.json",
				conf.getSigningRestBaseURL() + "friendships/update.json", new HttpParameter[] {
						new HttpParameter("screen_name", screenName),
						new HttpParameter("device", enableDeviceNotification),
						new HttpParameter("retweets", enableDeviceNotification) }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User updateProfile(final String name, final String url, final String location, final String description)
			throws TwitterException {
		ensureAuthorizationEnabled();
		final List<HttpParameter> profile = new ArrayList<HttpParameter>(4);
		addParameterToList(profile, "name", name);
		addParameterToList(profile, "url", url);
		addParameterToList(profile, "location", location);
		addParameterToList(profile, "description", description);
		profile.add(INCLUDE_ENTITIES);
		return factory.createUser(post(conf.getRestBaseURL() + "account/update_profile.json",
				conf.getSigningRestBaseURL() + "account/update_profile.json",
				profile.toArray(new HttpParameter[profile.size()])));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User updateProfileBackgroundImage(final File image, final boolean tile) throws TwitterException {
		ensureAuthorizationEnabled();
		checkFileValidity(image);
		return factory.createUser(post(conf.getRestBaseURL() + "account/update_profile_background_image.json",
				conf.getSigningRestBaseURL() + "account/update_profile_background_image.json", new HttpParameter[] {
						new HttpParameter("image", image), new HttpParameter("tile", tile), INCLUDE_ENTITIES }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User updateProfileBackgroundImage(final InputStream image, final boolean tile) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory
				.createUser(post(conf.getRestBaseURL() + "account/update_profile_background_image.json",
						conf.getSigningRestBaseURL() + "account/update_profile_background_image.json",
						new HttpParameter[] { new HttpParameter("image", "image", image),
								new HttpParameter("tile", tile), INCLUDE_ENTITIES }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User updateProfileColors(final String profileBackgroundColor, final String profileTextColor,
			final String profileLinkColor, final String profileSidebarFillColor, final String profileSidebarBorderColor)
			throws TwitterException {
		ensureAuthorizationEnabled();
		final List<HttpParameter> colors = new ArrayList<HttpParameter>(6);
		addParameterToList(colors, "profile_background_color", profileBackgroundColor);
		addParameterToList(colors, "profile_text_color", profileTextColor);
		addParameterToList(colors, "profile_link_color", profileLinkColor);
		addParameterToList(colors, "profile_sidebar_fill_color", profileSidebarFillColor);
		addParameterToList(colors, "profile_sidebar_border_color", profileSidebarBorderColor);
		colors.add(INCLUDE_ENTITIES);
		return factory.createUser(post(conf.getRestBaseURL() + "account/update_profile_colors.json",
				conf.getSigningRestBaseURL() + "account/update_profile_colors.json",
				colors.toArray(new HttpParameter[colors.size()])));
	}

	/* Legal Resources */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User updateProfileImage(final File image) throws TwitterException {
		checkFileValidity(image);
		ensureAuthorizationEnabled();
		return factory.createUser(post(conf.getRestBaseURL() + "account/update_profile_image.json",
				conf.getSigningRestBaseURL() + "account/update_profile_image.json", new HttpParameter[] {
						new HttpParameter("image", image), INCLUDE_ENTITIES }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User updateProfileImage(final InputStream image) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(conf.getRestBaseURL() + "account/update_profile_image.json",
				conf.getSigningRestBaseURL() + "account/update_profile_image.json", new HttpParameter[] {
						new HttpParameter("image", "image", image), INCLUDE_ENTITIES }));
	}

	/* #newtwitter Methods */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Status updateStatus(final StatusUpdate status) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + (status.isWithMedia() ? "statuses/update_with_media.json" : "statuses/update.json");
		final String sign_url = conf.getSigningRestBaseURL() + (status.isWithMedia() ? "statuses/update_with_media.json" : "statuses/update.json");
		return factory.createStatus(post(url, sign_url, status.asHttpParameterArray(INCLUDE_ENTITIES)));
	}

	/* Help Methods */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Status updateStatus(final String status) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory
				.createStatus(post(conf.getRestBaseURL() + "statuses/update.json", conf.getSigningRestBaseURL()
						+ "statuses/update.json", new HttpParameter[] { new HttpParameter("status", status),
						INCLUDE_ENTITIES }));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserList updateUserList(final int listId, final String newListName, final boolean isPublicList,
			final String newDescription) throws TwitterException {
		ensureAuthorizationEnabled();
		final List<HttpParameter> httpParams = new ArrayList<HttpParameter>();
		httpParams.add(new HttpParameter("list_id", listId));
		if (newListName != null) {
			httpParams.add(new HttpParameter("name", newListName));
		}
		httpParams.add(new HttpParameter("mode", isPublicList ? "public" : "private"));
		if (newDescription != null) {
			httpParams.add(new HttpParameter("description", newDescription));
		}
		return factory.createAUserList(post(conf.getRestBaseURL() + "lists/update.json", conf.getSigningRestBaseURL()
				+ "lists/update.json", httpParams.toArray(new HttpParameter[httpParams.size()])));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User verifyCredentials() throws TwitterException {
		return super.fillInIDAndScreenName();
	}

	private void addParameterToList(final List<HttpParameter> colors, final String paramName, final String color) {
		if (color != null) {
			colors.add(new HttpParameter(paramName, color));
		}
	}

	/**
	 * Check the existence, and the type of the specified file.
	 * 
	 * @param image image to be uploaded
	 * @throws TwitterException when the specified file is not found
	 *             (FileNotFoundException will be nested) , or when the
	 *             specified file object is not representing a file(IOException
	 *             will be nested).
	 */
	private void checkFileValidity(final File image) throws TwitterException {
		if (!image.exists()) // noinspection ThrowableInstanceNeverThrown
			throw new TwitterException(new FileNotFoundException(image + " is not found."));
		if (!image.isFile()) // noinspection ThrowableInstanceNeverThrown
			throw new TwitterException(new IOException(image + " is not a file."));
	}

	@SuppressWarnings("unused")
	private HttpResponse delete(final String url, final String sign_url) throws TwitterException {
		// intercept HTTP call for monitoring purposes
		HttpResponse response = null;
		final long start = System.currentTimeMillis();
		try {
			response = http.delete(url, sign_url, auth);
		} finally {
			final long elapsedTime = System.currentTimeMillis() - start;
			TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
		}
		return response;
	}

	private HttpResponse get(final String url, final String sign_url) throws TwitterException {
		return get(url, sign_url, null);
	}

	private HttpResponse get(final String url, final String sign_url, final HttpParameter[] parameters)
			throws TwitterException {
		// intercept HTTP call for monitoring purposes
		HttpResponse response = null;
		final long start = System.currentTimeMillis();
		try {
			response = http.get(url, sign_url, parameters, auth);
		} finally {
			final long elapsedTime = System.currentTimeMillis() - start;
			TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
		}
		return response;
	}

	private boolean isOk(final HttpResponse response) {
		return response != null && response.getStatusCode() < 300;
	}

	private HttpParameter[] mergeParameters(final HttpParameter[] params1, final HttpParameter params2) {
		if (params1 != null && params2 != null) {
			final HttpParameter[] params = new HttpParameter[params1.length + 1];
			System.arraycopy(params1, 0, params, 0, params1.length);
			params[params.length - 1] = params2;
			return params;
		}
		if (null == params1 && null == params2) return new HttpParameter[0];
		if (params1 != null)
			return params1;
		else
			return new HttpParameter[] { params2 };
	}

	private HttpParameter[] mergeParameters(final HttpParameter[] params1, final HttpParameter[] params2) {
		if (params1 != null && params2 != null) {
			final HttpParameter[] params = new HttpParameter[params1.length + params2.length];
			System.arraycopy(params1, 0, params, 0, params1.length);
			System.arraycopy(params2, 0, params, params1.length, params2.length);
			return params;
		}
		if (null == params1 && null == params2) return new HttpParameter[0];
		if (params1 != null)
			return params1;
		else
			return params2;
	}

	private HttpResponse post(final String url, final String sign_url) throws TwitterException {
		return post(url, sign_url, null);
	}

	private HttpResponse post(final String url, final String sign_url, final HttpParameter[] parameters)
			throws TwitterException {
		// intercept HTTP call for monitoring purposes
		HttpResponse response = null;
		final long start = System.currentTimeMillis();
		try {
			response = http.post(url, sign_url, parameters, auth);
		} finally {
			final long elapsedTime = System.currentTimeMillis() - start;
			TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
		}
		return response;
	}

	private String toDateStr(Date date) {
		if (null == date) {
			date = new Date();
		}
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}
}
