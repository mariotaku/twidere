package org.mariotaku.twidere.util;

import org.mariotaku.twidere.R;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusItemHolder {

	public final ImageView profile_image;
	public final TextView name, text, tweet_time, in_reply_to;
	private final View content, gap_text;
	public long status_id, account_id;
	private boolean is_gap, account_color_enabled;

	public StatusItemHolder(View view) {
		content = view;
		gap_text = view.findViewById(R.id.list_gap_text);
		profile_image = (ImageView) view.findViewById(R.id.profile_image);
		name = (TextView) view.findViewById(R.id.name);
		text = (TextView) view.findViewById(R.id.text);
		tweet_time = (TextView) view.findViewById(R.id.time);
		in_reply_to = (TextView) view.findViewById(R.id.in_reply_to);

	}

	public boolean isGap() {
		return is_gap;
	}

	public void setAccountColor(int color) {
		if (!is_gap && account_color_enabled) {
			Drawable background = content.getBackground();
			if (background != null) {
				background.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
			}
		}
	}

	public void setAccountColorEnabled(boolean enabled) {
		account_color_enabled = enabled;
		if (!is_gap) {
			content.setBackgroundResource(enabled ? R.drawable.ic_label_color : 0);
		}
	}

	public void setIsGap(boolean is_gap) {
		this.is_gap = is_gap;
		content.setBackgroundResource(is_gap ? R.drawable.ic_list_gap
				: account_color_enabled ? R.drawable.ic_label_color : 0);
		profile_image.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		name.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		text.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		tweet_time.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		in_reply_to.setVisibility(is_gap ? View.GONE : View.VISIBLE);
		gap_text.setVisibility(!is_gap ? View.GONE : View.VISIBLE);
	}

}
