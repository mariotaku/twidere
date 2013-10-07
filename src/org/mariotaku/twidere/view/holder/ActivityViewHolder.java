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

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;

public class ActivityViewHolder extends CardViewHolder {

	public final ImageView profile_image;
	public final ImageView activity_profile_image_1, activity_profile_image_2, activity_profile_image_3,
			activity_profile_image_4, activity_profile_image_5;
	public final ImageView[] activity_profile_images;
	public final TextView title, text, time, reply_status;
	public final ViewGroup activity_profile_image_container;
	private final ColorLabelRelativeLayout content;

	private boolean account_color_enabled;
	private float text_size;

	public ActivityViewHolder(final View view) {
		super(view);
		content = (ColorLabelRelativeLayout) findViewById(R.id.content);
		profile_image = (ImageView) findViewById(R.id.profile_image);
		title = (TextView) findViewById(R.id.title);
		text = (TextView) findViewById(R.id.text);
		time = (TextView) findViewById(R.id.time);
		reply_status = (TextView) findViewById(R.id.reply_status);
		activity_profile_image_container = (ViewGroup) findViewById(R.id.activity_profile_image_container);
		activity_profile_image_1 = (ImageView) findViewById(R.id.activity_profile_image_1);
		activity_profile_image_2 = (ImageView) findViewById(R.id.activity_profile_image_2);
		activity_profile_image_3 = (ImageView) findViewById(R.id.activity_profile_image_3);
		activity_profile_image_4 = (ImageView) findViewById(R.id.activity_profile_image_4);
		activity_profile_image_5 = (ImageView) findViewById(R.id.activity_profile_image_5);
		activity_profile_images = new ImageView[] { activity_profile_image_1, activity_profile_image_2,
				activity_profile_image_3, activity_profile_image_4, activity_profile_image_5 };
	}

	public void reset() {
		content.drawLabel(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT);
		profile_image.setImageDrawable(null);
		title.setText(null);
		text.setVisibility(View.VISIBLE);
		text.setText(null);
		text.setSingleLine(false);
		text.setEllipsize(null);
		time.setText(null);
		time.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		reply_status.setVisibility(View.GONE);
		reply_status.setText(null);
		reply_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		activity_profile_image_container.setVisibility(View.GONE);
		final int activity_profile_image_count = activity_profile_image_container.getChildCount();
		for (int i = 0; i < activity_profile_image_count; i++) {
			((ImageView) activity_profile_image_container.getChildAt(i)).setImageDrawable(null);
		}
	}

	public void setAccountColor(final int color) {
		content.drawEnd(account_color_enabled ? color : Color.TRANSPARENT);
	}

	public void setAccountColorEnabled(final boolean enabled) {
		account_color_enabled = enabled;
		if (!account_color_enabled) {
			content.drawEnd(Color.TRANSPARENT);
		}
	}

	public void setTextSize(final float text_size) {
		if (this.text_size != text_size) {
			this.text_size = text_size;
			text.setTextSize(text_size);
			title.setTextSize(text_size * 1.05f);
			time.setTextSize(text_size * 0.65f);
			reply_status.setTextSize(text_size * 0.65f);
		}
	}

	public void setUserColor(final int color) {
		content.drawStart(color);
	}

}
