package org.mariotaku.twidere.util;

import org.mariotaku.twidere.R;

import android.content.Context;
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
	private float text_size;

	public StatusViewHolder(View view, Context context) {
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
			final Drawable background = content.getBackground();
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

	public void setShowAsGap(boolean show_gap) {
		show_as_gap = show_gap;
		content.setBackgroundResource(show_gap ? R.drawable.ic_list_gap
				: account_color_enabled ? R.drawable.ic_label_color : 0);
		profile_image.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		name.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		text.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		tweet_time.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		reply_retweet_status.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		gap_indicator.setVisibility(!show_gap ? View.GONE : View.VISIBLE);
	}

	public void setTextSize(float text_size) {
		if (this.text_size != text_size) {
			this.text_size = text_size;
			text.setTextSize(text_size);
			name.setTextSize(text_size * 1.05f);
			tweet_time.setTextSize(text_size * 0.65f);
			reply_retweet_status.setTextSize(text_size * 0.65f);
		}
	}
}