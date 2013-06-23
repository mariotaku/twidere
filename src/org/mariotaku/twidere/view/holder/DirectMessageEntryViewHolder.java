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

package org.mariotaku.twidere.view.holder;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DirectMessageEntryViewHolder {

	public final ImageView profile_image;
	public final TextView name, screen_name, text, time;
	private final ColorLabelRelativeLayout content;
	private float text_size;
	private boolean account_color_enabled;

	public DirectMessageEntryViewHolder(final View view) {
		content = (ColorLabelRelativeLayout) view;
		profile_image = (ImageView) view.findViewById(R.id.profile_image);
		name = (TextView) view.findViewById(R.id.name);
		screen_name = (TextView) view.findViewById(R.id.screen_name);
		text = (TextView) view.findViewById(R.id.text);
		time = (TextView) view.findViewById(R.id.time);
	}

	public void setAccountColor(final int color) {
		content.drawRight(account_color_enabled ? color : Color.TRANSPARENT);
	}

	public void setAccountColorEnabled(final boolean enabled) {
		if (account_color_enabled == enabled) return;
		account_color_enabled = enabled;
		if (!account_color_enabled) {
			content.drawRight(Color.TRANSPARENT);
		}
	}

	public void setTextSize(final float text_size) {
		if (this.text_size == text_size) return;
			this.text_size = text_size;
			text.setTextSize(text_size);
			name.setTextSize(text_size * 1.25f);
			screen_name.setTextSize(text_size);
			time.setTextSize(text_size * 0.65f);
	}

	public void setUserColor(final int color) {
		content.drawLeft(color);
	}
}
