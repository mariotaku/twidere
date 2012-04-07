package org.mariotaku.twidere.util;

import org.mariotaku.twidere.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusItemHolder {

	public TextView user_name;
	public TextView screen_name;
	public TextView tweet_content;
	public TextView tweet_time;
	public ImageView profile_image;
	public TextView gap_text;
	public long status_id;
	public long account_id;
	private View root;
	private boolean is_gap;

	public StatusItemHolder(View view) {
		root = view;
		user_name = (TextView) view.findViewById(R.id.user_name);
		screen_name = (TextView) view.findViewById(R.id.screen_name);
		tweet_content = (TextView) view.findViewById(R.id.tweet_content);
		tweet_time = (TextView) view.findViewById(R.id.tweet_time);
		gap_text = (TextView) view.findViewById(R.id.list_gap_text);
		profile_image = (ImageView) view.findViewById(R.id.profile_image);
		// retweet_fav_indicator = (ImageView)
		// view.findViewById(R.id.retweet_fav_indicator);
	}

	public boolean isGap() {
		return is_gap;
	}

	public void setIsGap(boolean is_gap) {
		this.is_gap = is_gap;
		root.setBackgroundResource(is_gap ? R.drawable.bg_list_gap : 0);
		user_name.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		screen_name.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		tweet_content.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		tweet_time.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		profile_image.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		gap_text.setVisibility(!is_gap ? View.GONE : View.VISIBLE);
	}

}