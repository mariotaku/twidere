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
import org.mariotaku.twidere.view.ColorView;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class UserViewHolder {

	public final ImageView profile_image;
	public final ColorView user_background;
	public final TextView name, description;
	private final View content, user_content, gap_indicator;
	public boolean show_as_gap;
	private float text_size;

	public UserViewHolder(View view) {
		content = view;
		user_content = view.findViewById(R.id.user_content);
		gap_indicator = view.findViewById(R.id.list_gap_text);
		profile_image = (ImageView) view.findViewById(R.id.profile_image);
		name = (TextView) view.findViewById(R.id.name);
		description = (TextView) view.findViewById(R.id.description);
		user_background = (ColorView) view.findViewById(R.id.user_background);
	}

	public void setAccountColor(int color) {
		final Drawable background = user_content.getBackground();
		if (background != null) {
			background.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
			user_content.invalidate();
		}
	}

	public void setAccountColorEnabled(boolean enabled) {
		user_content.setBackgroundResource(enabled ? R.drawable.ic_label_account : 0);
	}

	public void setSelected(boolean selected) {
		if (!show_as_gap) {
			content.setBackgroundResource(selected ? R.drawable.list_focused_holo : 0);
		} else {
			content.setBackgroundResource(0);
		}
	}

	public void setShowAsGap(boolean show_gap) {
		show_as_gap = show_gap;
		user_content.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		user_background.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		gap_indicator.setVisibility(!show_gap ? View.GONE : View.VISIBLE);
	}

	public void setTextSize(float text_size) {
		if (this.text_size != text_size) {
			this.text_size = text_size;
			description.setTextSize(text_size);
			name.setTextSize(text_size * 1.05f);
		}
	}

	public void setUserColor(int color) {
		final Drawable background = user_background.getBackground();
		if (background != null) {
			background.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
			user_background.invalidate();
		}
	}

}
