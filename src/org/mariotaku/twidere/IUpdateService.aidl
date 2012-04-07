package org.mariotaku.twidere;

interface IUpdateService {
	void refreshHomeTimeline(in long[] account_ids, in long[] max_ids);
	void refreshMentions(in long[] account_ids, in long[] max_ids);
	void refreshMessages(in long[] account_ids, in long[] max_ids);
	boolean isHomeTimelineRefreshing();
	boolean isMentionsRefreshing();
}