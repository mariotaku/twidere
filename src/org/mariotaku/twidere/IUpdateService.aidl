package org.mariotaku.twidere;

interface IUpdateService {
	void getHomeTimeline(in long[] account_ids, in long[] max_ids);
	void getMentions(in long[] account_ids, in long[] max_ids);
	void getMessages(in long[] account_ids, in long[] max_ids);
	void updateStatus(in long[] account_ids, String content, in Location location, in Uri image_uri, long in_reply_to);
	void destroyStatus(long account_id, long status_id);
	void retweetStatus(in long[] account_ids, long status_id);
	void createFavorite(in long[] account_ids, long status_id);
	void destroyFavorite(in long[] account_id, long status_id);
	boolean isHomeTimelineRefreshing();
	boolean isMentionsRefreshing();
	boolean hasActivatedTask();
}