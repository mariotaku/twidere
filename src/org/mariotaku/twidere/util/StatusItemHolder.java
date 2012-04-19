package org.mariotaku.twidere.util;

import org.mariotaku.twidere.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusItemHolder {

	public ImageView profile_image;
	public TextView user_name, screen_name, tweet_content, tweet_time, in_reply_to;
	public long status_id, account_id;
	private View root, content, gap_text;
	private boolean is_gap;

	public StatusItemHolder(View view) {
		root = view;
		content = view.findViewById(R.id.content);
		gap_text = view.findViewById(R.id.list_gap_text);
		profile_image = (ImageView) view.findViewById(R.id.profile_image);
		user_name = (TextView) view.findViewById(R.id.user_name);
		screen_name = (TextView) view.findViewById(R.id.screen_name);
		tweet_content = (TextView) view.findViewById(R.id.text);
		tweet_time = (TextView) view.findViewById(R.id.tweet_time);
		in_reply_to = (TextView) view.findViewById(R.id.in_reply_to);

	}

	public boolean isGap() {
		return is_gap;
	}

	public void setIsGap(boolean is_gap) {
		this.is_gap = is_gap;
		root.setBackgroundResource(is_gap ? R.drawable.bg_list_gap : 0);
		content.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		gap_text.setVisibility(!is_gap ? View.GONE : View.VISIBLE);
	}

}