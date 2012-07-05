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

import java.io.Serializable;

import twitter4j.api.AccountMethods;
import twitter4j.api.BlockMethods;
import twitter4j.api.DirectMessageMethods;
import twitter4j.api.FavoriteMethods;
import twitter4j.api.FriendsFollowersMethods;
import twitter4j.api.FriendshipMethods;
import twitter4j.api.GeoMethods;
import twitter4j.api.HelpMethods;
import twitter4j.api.LegalResources;
import twitter4j.api.ListMembersMethods;
import twitter4j.api.ListMethods;
import twitter4j.api.ListSubscribersMethods;
import twitter4j.api.LocalTrendsMethods;
import twitter4j.api.NewTwitterMethods;
import twitter4j.api.NotificationMethods;
import twitter4j.api.SavedSearchesMethods;
import twitter4j.api.SearchMethods;
import twitter4j.api.SpamReportingMethods;
import twitter4j.api.StatusMethods;
import twitter4j.api.TimelineMethods;
import twitter4j.api.TrendsMethods;
import twitter4j.api.UserMethods;
import twitter4j.auth.OAuthSupport;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.0
 */
public interface Twitter extends Serializable, OAuthSupport, TwitterBase, SearchMethods, TrendsMethods,
		TimelineMethods, StatusMethods, UserMethods, ListMethods, ListMembersMethods, ListSubscribersMethods,
		DirectMessageMethods, FriendshipMethods, FriendsFollowersMethods, AccountMethods, FavoriteMethods,
		NotificationMethods, BlockMethods, SpamReportingMethods, SavedSearchesMethods, LocalTrendsMethods, GeoMethods,
		LegalResources, NewTwitterMethods, HelpMethods {
}
