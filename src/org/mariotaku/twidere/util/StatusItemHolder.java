package org.mariotaku.twidere.util;

import java.net.URL;

import org.mariotaku.twidere.R;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusItemHolder {

	public final ImageView profile_image_view;
	public final TextView name_view, text_view, tweet_time_view, in_reply_to_view;
	private final View content, gap_indicator;
	public long status_id = -1, account_id = -1, status_timestamp = -1;
	public CharSequence name, screen_name, text, in_reply_to;
	public URL profile_image_url;
	private boolean show_as_gap, account_color_enabled;
	public byte is_gap = -1, is_reply = -1, is_retweet = -2, is_favorite = -1, is_protected = -1, has_media = -1,
			has_location = -1;

	public StatusItemHolder(View view) {
		content = view;
		gap_indicator = view.findViewById(R.id.list_gap_text);
		profile_image_view = (ImageView) view.findViewById(R.id.profile_image);
		name_view = (TextView) view.findViewById(R.id.name);
		text_view = (TextView) view.findViewById(R.id.text);
		tweet_time_view = (TextView) view.findViewById(R.id.time);
		in_reply_to_view = (TextView) view.findViewById(R.id.in_reply_to);

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
		profile_image_view.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		name_view.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		text_view.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		tweet_time_view.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		in_reply_to_view.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		gap_indicator.setVisibility(!is_gap ? View.GONE : View.VISIBLE);
	}

}
