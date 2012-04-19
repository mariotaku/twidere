package org.mariotaku.twidere;

interface IUpdateService {
	void refreshHomeTimeline(in long[] account_ids, in long[] max_ids);
	void refreshMentions(in long[] account_ids, in long[] max_ids);
	void refreshMessages(in long[] account_ids, in long[] max_ids);
	void updateStatus(in long[] account_ids, String content, in Location location, in Uri image_uri, long in_reply_to);
	boolean isHomeTimelineRefreshing();
	boolean isMentionsRefreshing();
}