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
package twitter4j.internal.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.AccountSettings;
import twitter4j.AccountTotals;
import twitter4j.Activity;
import twitter4j.Category;
import twitter4j.DirectMessage;
import twitter4j.Friendship;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.IDs;
import twitter4j.Location;
import twitter4j.PagableResponseList;
import twitter4j.Place;
import twitter4j.ProfileImage;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.RelatedResults;
import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.SavedSearch;
import twitter4j.SimilarPlaces;
import twitter4j.Status;
import twitter4j.Trends;
import twitter4j.TwitterAPIConfiguration;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserMentionEntity;
import twitter4j.api.HelpMethods;
import twitter4j.conf.Configuration;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.util.z_T4JInternalStringUtil;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.4
 */
public class z_T4JInternalJSONImplFactory implements z_T4JInternalFactory {

	private Configuration conf;

	public z_T4JInternalJSONImplFactory(Configuration conf) {
		this.conf = conf;
	}

	@Override
	public AccountSettings createAccountSettings(HttpResponse res) throws TwitterException {
		return new AccountSettingsJSONImpl(res, conf);
	}

	@Override
	public AccountTotals createAccountTotals(HttpResponse res) throws TwitterException {
		return new AccountTotalsJSONImpl(res, conf);
	}

	@Override
	public ResponseList<Activity> createActivityList(HttpResponse res) throws TwitterException {
		return ActivityJSONImpl.createActivityList(res, conf);
	}

	@Override
	public UserList createAUserList(HttpResponse res) throws TwitterException {
		return new UserListJSONImpl(res, conf);
	}

	@Override
	public UserList createAUserList(JSONObject json) throws TwitterException {
		return new UserListJSONImpl(json);
	}

	@Override
	public ResponseList<Category> createCategoryList(HttpResponse res) throws TwitterException {
		return CategoryJSONImpl.createCategoriesList(res, conf);
	}

	@Override
	public DirectMessage createDirectMessage(HttpResponse res) throws TwitterException {
		return new DirectMessageJSONImpl(res, conf);
	}

	@Override
	public DirectMessage createDirectMessage(JSONObject json) throws TwitterException {
		return new DirectMessageJSONImpl(json);
	}

	@Override
	public ResponseList<DirectMessage> createDirectMessageList(HttpResponse res) throws TwitterException {
		return DirectMessageJSONImpl.createDirectMessageList(res, conf);
	}

	@Override
	public <T> ResponseList<T> createEmptyResponseList() {
		return new ResponseListImpl<T>(0, null);
	}

	@Override
	public ResponseList<Friendship> createFriendshipList(HttpResponse res) throws TwitterException {
		return FriendshipJSONImpl.createFriendshipList(res, conf);
	}

	@Override
	public IDs createIDs(HttpResponse res) throws TwitterException {
		return new IDsJSONImpl(res, conf);
	}

	@Override
	public ResponseList<HelpMethods.Language> createLanguageList(HttpResponse res) throws TwitterException {
		return LanguageJSONImpl.createLanguageList(res, conf);
	}

	@Override
	public ResponseList<Location> createLocationList(HttpResponse res) throws TwitterException {
		return LocationJSONImpl.createLocationList(res, conf);
	}

	@Override
	public PagableResponseList<User> createPagableUserList(HttpResponse res) throws TwitterException {
		return UserJSONImpl.createPagableUserList(res, conf);
	}

	@Override
	public PagableResponseList<UserList> createPagableUserListList(HttpResponse res) throws TwitterException {
		return UserListJSONImpl.createPagableUserListList(res, conf);
	}

	@Override
	public Place createPlace(HttpResponse res) throws TwitterException {
		return new PlaceJSONImpl(res, conf);
	}

	@Override
	public ResponseList<Place> createPlaceList(HttpResponse res) throws TwitterException {
		try {
			return PlaceJSONImpl.createPlaceList(res, conf);
		} catch (final TwitterException te) {
			if (te.getStatusCode() == 404)
				return new ResponseListImpl<Place>(0, null);
			else
				throw te;
		}
	}

	@Override
	public ProfileImage createProfileImage(HttpResponse res) throws TwitterException {
		return new ProfileImageImpl(res);
	}

	@Override
	public QueryResult createQueryResult(HttpResponse res, Query query) throws TwitterException {
		try {
			return new QueryResultJSONImpl(res, conf);
		} catch (final TwitterException te) {
			if (404 == te.getStatusCode())
				return new QueryResultJSONImpl(query);
			else
				throw te;
		}
	}

	@Override
	public RateLimitStatus createRateLimitStatus(HttpResponse res) throws TwitterException {
		return new RateLimitStatusJSONImpl(res, conf);
	}

	@Override
	public RelatedResults createRelatedResults(HttpResponse res) throws TwitterException {
		return new RelatedResultsJSONImpl(res, conf);
	}

	@Override
	public Relationship createRelationship(HttpResponse res) throws TwitterException {
		return new RelationshipJSONImpl(res, conf);
	}

	@Override
	public SavedSearch createSavedSearch(HttpResponse res) throws TwitterException {
		return new SavedSearchJSONImpl(res, conf);
	}

	@Override
	public ResponseList<SavedSearch> createSavedSearchList(HttpResponse res) throws TwitterException {
		return SavedSearchJSONImpl.createSavedSearchList(res, conf);
	}

	@Override
	public SimilarPlaces createSimilarPlaces(HttpResponse res) throws TwitterException {
		return SimilarPlacesImpl.createSimilarPlaces(res, conf);
	}

	@Override
	public Status createStatus(HttpResponse res) throws TwitterException {
		return new StatusJSONImpl(res, conf);
	}

	@Override
	public Status createStatus(JSONObject json) throws TwitterException {
		return new StatusJSONImpl(json);
	}

	@Override
	public ResponseList<Status> createStatusList(HttpResponse res) throws TwitterException {
		return StatusJSONImpl.createStatusList(res, conf);
	}

	@Override
	public Trends createTrends(HttpResponse res) throws TwitterException {
		return new TrendsJSONImpl(res, conf);
	}

	@Override
	public ResponseList<Trends> createTrendsList(HttpResponse res) throws TwitterException {
		return TrendsJSONImpl.createTrendsList(res, conf.isJSONStoreEnabled());
	}

	@Override
	public TwitterAPIConfiguration createTwitterAPIConfiguration(HttpResponse res) throws TwitterException {
		return new TwitterAPIConfigurationJSONImpl(res, conf);
	}

	@Override
	public User createUser(HttpResponse res) throws TwitterException {
		return new UserJSONImpl(res, conf);
	}

	@Override
	public User createUser(JSONObject json) throws TwitterException {
		return new UserJSONImpl(json);
	}

	@Override
	public ResponseList<User> createUserList(HttpResponse res) throws TwitterException {
		return UserJSONImpl.createUserList(res, conf);
	}

	@Override
	public ResponseList<User> createUserListFromJSONArray(HttpResponse res) throws TwitterException {
		return UserJSONImpl.createUserList(res.asJSONArray(), res, conf);
	}

	@Override
	public ResponseList<User> createUserListFromJSONArray_Users(HttpResponse res) throws TwitterException {
		try {
			return UserJSONImpl.createUserList(res.asJSONObject().getJSONArray("users"), res, conf);
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}

	@Override
	public ResponseList<UserList> createUserListList(HttpResponse res) throws TwitterException {
		return UserListJSONImpl.createUserListList(res, conf);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof z_T4JInternalJSONImplFactory)) return false;

		final z_T4JInternalJSONImplFactory that = (z_T4JInternalJSONImplFactory) o;

		if (conf != null ? !conf.equals(that.conf) : that.conf != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return conf != null ? conf.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "JSONImplFactory{" + "conf=" + conf + '}';
	}

	public static RateLimitStatus createFeatureSpecificRateLimitStatusFromResponseHeader(HttpResponse res) {
		return RateLimitStatusJSONImpl.createFeatureSpecificRateLimitStatusFromResponseHeader(res);
	}

	/**
	 * static factory method for twitter-text-java
	 * 
	 * @return hashtag entity
	 * @since Twitter4J 2.2.6
	 */
	public static HashtagEntity createHashtagEntity(int start, int end, String text) {
		return new HashtagEntityJSONImpl(start, end, text);
	}

	public static RateLimitStatus createRateLimitStatusFromResponseHeader(HttpResponse res) {
		return RateLimitStatusJSONImpl.createFromResponseHeader(res);
	}

	/**
	 * static factory method for twitter-text-java
	 * 
	 * @return url entity
	 * @since Twitter4J 2.2.6
	 */
	public static URLEntity createUrlEntity(int start, int end, String url, String expandedURL, String displayURL) {
		return new URLEntityJSONImpl(start, end, url, expandedURL, displayURL);
	}

	/**
	 * static factory method for twitter-text-java
	 * 
	 * @return user mention entity
	 * @since Twitter4J 2.2.6
	 */
	public static UserMentionEntity createUserMentionEntity(int start, int end, String name, String screenName, long id) {
		return new UserMentionEntityJSONImpl(start, end, name, screenName, id);
	}

	/* package */
	static GeoLocation[][] coordinatesAsGeoLocationArray(JSONArray coordinates) throws TwitterException {
		try {
			final GeoLocation[][] boundingBox = new GeoLocation[coordinates.length()][];
			for (int i = 0; i < coordinates.length(); i++) {
				final JSONArray array = coordinates.getJSONArray(i);
				boundingBox[i] = new GeoLocation[array.length()];
				for (int j = 0; j < array.length(); j++) {
					final JSONArray coordinate = array.getJSONArray(j);
					boundingBox[i][j] = new GeoLocation(coordinate.getDouble(1), coordinate.getDouble(0));
				}
			}
			return boundingBox;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}

	/**
	 * returns a GeoLocation instance if a "geo" element is found.
	 * 
	 * @param json JSONObject to be parsed
	 * @return GeoLocation instance
	 * @throws TwitterException when coordinates is not included in geo element
	 *             (should be an API side issue)
	 */
	/* package */
	static GeoLocation createGeoLocation(JSONObject json) throws TwitterException {
		try {
			if (!json.isNull("geo")) {
				String coordinates = json.getJSONObject("geo").getString("coordinates");
				coordinates = coordinates.substring(1, coordinates.length() - 1);
				final String[] point = z_T4JInternalStringUtil.split(coordinates, ",");
				return new GeoLocation(Double.parseDouble(point[0]), Double.parseDouble(point[1]));
			}
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
		return null;
	}
}
