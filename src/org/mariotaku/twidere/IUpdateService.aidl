package org.mariotaku.twidere;

import android.net.Uri;

interface IUpdateService {
	void refreshHomeTimeline(in long[] account_ids, in long[] max_ids);
	void refreshMentions(in long[] account_ids, in long[] max_ids);
	void refreshMessages(in long[] account_ids, in long[] max_ids);
	void updateStatus(in long[] account_ids, String content, in Uri image_uri);
	boolean isHomeTimelineRefreshing();
	boolean isMentionsRefreshing();
}