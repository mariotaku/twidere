package org.mariotaku.twidere;

interface IUpdateService {
	void refreshHomeTimeline(in long[] account_ids, in long[] max_ids);
	void refreshMentions(in long[] account_ids, in long[] max_ids);
	void refreshMessages(in long[] account_ids, in long[] max_ids);
	void updateStatus(in long[] account_ids, String content, in Location location, in Uri image_uri, long in_reply_to);
	void retweetStatus(in long[] account_ids, long status_id) ;
	void favStatus(in long[] account_ids, long status_id) ;
	void unFavStatus(in long[] account_id, long status_id) ;
	void deleteStatus(long account_id, long status_id) ;
	boolean isHomeTimelineRefreshing();
	boolean isMentionsRefreshing();
	boolean hasActivatedTask();
}