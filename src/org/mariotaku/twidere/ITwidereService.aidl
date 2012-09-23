/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere;

interface ITwidereService {
	int getHomeTimeline(in long[] account_ids, in long[] max_ids);
	int getMentions(in long[] account_ids, in long[] max_ids);
	int getReceivedDirectMessages(in long[] account_ids, in long[] max_ids);
	int getSentDirectMessages(in long[] account_ids, in long[] max_ids);
	int refreshAll();
	int getDailyTrends(long account_id);
	int getWeeklyTrends(long account_id);
	int getLocalTrends(long account_id, int woeid);
	int updateStatus(in long[] account_ids, String content, in Location location, in Uri image_uri, long in_reply_to, boolean delete_image);
	int destroyStatus(long account_id, long status_id);
	int sendDirectMessage(long account_id, String screen_name, long user_id, String message);
	int destroyDirectMessage(long account_id, long message_id);
	int retweetStatus(long account_ids, long status_id);
	int cancelRetweet(long account_id, long status_id);
	int createFavorite(long account_ids, long status_id);
	int destroyFavorite(long account_id, long status_id);
	int createFriendship(long account_id, long user_id);
	int destroyFriendship(long account_id, long user_id);
	int createUserListSubscription(long account_id, int list_id);
	int destroyUserListSubscription(long account_id, int list_id);
	int createUserList(long account_id, String list_name, boolean is_public, String description);
	int destroyUserList(long account_id, int list_id);
	int createBlock(long account_id, long user_id);
	int destroyBlock(long account_id, long user_id);
	int reportSpam(long account_id, long max_id);
	int createMultiBlock(long account_id, in long[] user_ids);
	int reportMultiSpam(long account_id, in long[] max_ids);
	int addUserListMember(long account_id, int list_id, long user_id, String screen_name);
	int deleteUserListMember(long account_id, int list_id, long user_id);
	int updateProfile(long account_id, String name, String url, String location, String description);
	int updateProfileImage(long account_id, in Uri image_uri, boolean delete_image);
	int updateUserListDetails(long account_id, int list_id, boolean is_public, String name, String description);
	boolean isHomeTimelineRefreshing();
	boolean isMentionsRefreshing();
	boolean isReceivedDirectMessagesRefreshing();
	boolean isSentDirectMessagesRefreshing();
	boolean isLocalTrendsRefreshing();
	boolean isDailyTrendsRefreshing();
	boolean isWeeklyTrendsRefreshing();
	boolean hasActivatedTask();
	boolean test();
	boolean startAutoRefresh();
	void stopAutoRefresh();
	void shutdownService();
	void clearNotification(int id);
}
