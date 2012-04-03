package org.mariotaku.twidere;

interface IUpdateService {
	void refreshHomeTimeline(in long[] account_ids, int count);
	void refreshMentions(in long[] account_ids, int count);
	void refreshMessages(in long[] account_ids, int count);
}