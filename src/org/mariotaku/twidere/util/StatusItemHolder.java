package org.mariotaku.twidere.util;

import org.mariotaku.twidere.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StatusItemHolder {

	public View root;
	public TextView user_name;
	public TextView screen_name;
	public TextView tweet_content;
	public TextView tweet_time;
	public ImageView profile_image;
	// ImageView retweet_fav_indicator;
	public TextView list_gap_text;
	public RelativeLayout list_content;

	public StatusItemHolder(View view) {
		root = view;
		user_name = (TextView) view.findViewById(R.id.user_name);
		screen_name = (TextView) view.findViewById(R.id.screen_name);
		tweet_content = (TextView) view.findViewById(R.id.tweet_content);
		tweet_time = (TextView) view.findViewById(R.id.tweet_time);
		profile_image = (ImageView) view.findViewById(R.id.profile_image);
		list_gap_text = (TextView) view.findViewById(R.id.list_gap_text);
		list_content = (RelativeLayout) view.findViewById(R.id.list_content);
		// retweet_fav_indicator = (ImageView)
		// view.findViewById(R.id.retweet_fav_indicator);
	}

}