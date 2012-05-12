package org.mariotaku.twidere.util;

import org.mariotaku.twidere.R;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusViewHolder {

	public final ImageView profile_image;
	public final TextView name, text, tweet_time, reply_retweet_status;
	private final View content, gap_indicator;
	public boolean show_as_gap;
	private boolean account_color_enabled;

	public StatusViewHolder(View view) {
		content = view;
		gap_indicator = view.findViewById(R.id.list_gap_text);
		profile_image = (ImageView) view.findViewById(R.id.profile_image);
		name = (TextView) view.findViewById(R.id.name);
		text = (TextView) view.findViewById(R.id.text);
		tweet_time = (TextView) view.findViewById(R.id.time);
		reply_retweet_status = (TextView) view.findViewById(R.id.reply_retweet_status);

	}

	public void setAccountColor(int color) {
		if (!show_as_gap && account_color_enabled) {
			Drawable background = content.getBackground();
			if (background != null) {
				background.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
			}
		}
	}

	public void setAccountColorEnabled(boolean enabled) {
		account_color_enabled = enabled;
		if (!show_as_gap) {
			content.setBackgroundResource(enabled ? R.drawable.ic_label_color : 0);
		}
	}

	public void setShowAsGap(boolean is_gap) {
		show_as_gap = is_gap;
		content.setBackgroundResource(is_gap ? R.drawable.ic_list_gap
				: account_color_enabled ? R.drawable.ic_label_color : 0);
		profile_image.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		name.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		text.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		tweet_time.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		reply_retweet_status.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		gap_indicator.setVisibility(!is_gap ? View.GONE : View.VISIBLE);
	}

}
