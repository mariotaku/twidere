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
package org.mariotaku.twidere.twitter4j;

import org.mariotaku.twidere.twitter4j.api.ActivityResources;
import org.mariotaku.twidere.twitter4j.api.DirectMessagesResources;
import org.mariotaku.twidere.twitter4j.api.FavoritesResources;
import org.mariotaku.twidere.twitter4j.api.FriendsFollowersResources;
import org.mariotaku.twidere.twitter4j.api.HelpResources;
import org.mariotaku.twidere.twitter4j.api.ListsResources;
import org.mariotaku.twidere.twitter4j.api.PlacesGeoResources;
import org.mariotaku.twidere.twitter4j.api.SavedSearchesResources;
import org.mariotaku.twidere.twitter4j.api.SearchResource;
import org.mariotaku.twidere.twitter4j.api.SpamReportingResources;
import org.mariotaku.twidere.twitter4j.api.TimelinesResources;
import org.mariotaku.twidere.twitter4j.api.TrendsResources;
import org.mariotaku.twidere.twitter4j.api.TweetResources;
import org.mariotaku.twidere.twitter4j.api.UsersResources;
import org.mariotaku.twidere.twitter4j.auth.OAuthSupport;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.0
 */
public interface Twitter extends OAuthSupport, TwitterConstants, TwitterBase, SearchResource, TimelinesResources,
		TweetResources, UsersResources, ListsResources, DirectMessagesResources, FriendsFollowersResources,
		FavoritesResources, SpamReportingResources, SavedSearchesResources, TrendsResources, PlacesGeoResources,
		HelpResources, ActivityResources {
}
