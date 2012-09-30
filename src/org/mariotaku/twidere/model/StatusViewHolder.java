/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.model;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusViewHolder {

	public final ImageView profile_image, image_preview;
	public final TextView name, screen_name, text, time, reply_retweet_status;
	private final View gap_indicator;
	private final ColorLabelRelativeLayout content;
	public boolean show_as_gap;
	private boolean account_color_enabled;
	private float text_size;

	public StatusViewHolder(final View view) {
		content = (ColorLabelRelativeLayout) view;
		gap_indicator = view.findViewById(R.id.list_gap_text);
		profile_image = (ImageView) view.findViewById(R.id.profile_image);
		image_preview = (ImageView) view.findViewById(R.id.image_preview);
		name = (TextView) view.findViewById(R.id.name);
		screen_name = (TextView) view.findViewById(R.id.screen_name);
		text = (TextView) view.findViewById(R.id.text);
		time = (TextView) view.findViewById(R.id.time);
		reply_retweet_status = (TextView) view.findViewById(R.id.reply_retweet_status);
	}

	public void setAccountColor(final int color) {
		content.drawRight(account_color_enabled && !show_as_gap ? color : Color.TRANSPARENT);
	}

	public void setAccountColorEnabled(final boolean enabled) {
		account_color_enabled = enabled && !show_as_gap;
		if (!account_color_enabled) {
			content.drawRight(Color.TRANSPARENT);
		}
	}

	public void setHighlightColor(final int color) {
		content.drawBackground(show_as_gap ? Color.TRANSPARENT : color);
	}

	public void setSelected(final boolean selected) {
		content.setBackgroundColor(selected && !show_as_gap ? 0x600099CC : Color.TRANSPARENT);
	}

	public void setShowAsGap(final boolean show_gap) {
		show_as_gap = show_gap;
		if (show_as_gap) {
			content.setBackgroundResource(0);
			content.drawLabel(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT);
		}
		profile_image.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		image_preview.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		name.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		screen_name.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		text.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		time.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		reply_retweet_status.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		gap_indicator.setVisibility(!show_gap ? View.GONE : View.VISIBLE);
	}

	public void setTextSize(final float text_size) {
		if (this.text_size != text_size) {
			this.text_size = text_size;
			text.setTextSize(text_size);
			name.setTextSize(text_size);
			screen_name.setTextSize(text_size * 0.75f);
			time.setTextSize(text_size * 0.65f);
			reply_retweet_status.setTextSize(text_size * 0.65f);
		}
	}

	public void setUserColor(final int color) {
		content.drawLeft(show_as_gap ? Color.TRANSPARENT : color);
	}

}
