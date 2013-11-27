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

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.ShortTimeView;
import org.mariotaku.twidere.view.iface.IColorLabelView;

public class ActivityViewHolder extends CardViewHolder {

	public final ImageView my_profile_image, profile_image, image_preview;
	public final ImageView activity_profile_image_1, activity_profile_image_2, activity_profile_image_3,
			activity_profile_image_4, activity_profile_image_5;
	public final ImageView[] activity_profile_images;
	public final TextView name, screen_name, text, reply_retweet_status;
	public final ShortTimeView time;
	public final ViewGroup image_preview_container;
	public final ViewGroup activity_profile_image_container;
	public final ProgressBar image_preview_progress;
	public final IColorLabelView content;
	private final View gap_indicator;

	private final float density;
	private final boolean is_rtl;
	public boolean show_as_gap;
	private boolean account_color_enabled;
	private float text_size;

	public ActivityViewHolder(final View view) {
		super(view);
		final Context context = getContext();
		content = (IColorLabelView) findViewById(R.id.content);
		gap_indicator = findViewById(R.id.gap_indicator);
		image_preview_container = (ViewGroup) findViewById(R.id.image_preview_container);
		profile_image = (ImageView) findViewById(R.id.profile_image);
		my_profile_image = (ImageView) findViewById(R.id.my_profile_image);
		image_preview = (ImageView) findViewById(R.id.image_preview);
		image_preview_progress = (ProgressBar) findViewById(R.id.image_preview_progress);
		name = (TextView) findViewById(R.id.name);
		screen_name = (TextView) findViewById(R.id.screen_name);
		text = (TextView) findViewById(R.id.text);
		time = (ShortTimeView) findViewById(R.id.time);
		reply_retweet_status = (TextView) findViewById(R.id.reply_retweet_status);
		activity_profile_image_container = (ViewGroup) findViewById(R.id.activity_profile_image_container);
		activity_profile_image_1 = (ImageView) findViewById(R.id.activity_profile_image_1);
		activity_profile_image_2 = (ImageView) findViewById(R.id.activity_profile_image_2);
		activity_profile_image_3 = (ImageView) findViewById(R.id.activity_profile_image_3);
		activity_profile_image_4 = (ImageView) findViewById(R.id.activity_profile_image_4);
		activity_profile_image_5 = (ImageView) findViewById(R.id.activity_profile_image_5);
		activity_profile_images = new ImageView[] { activity_profile_image_1, activity_profile_image_2,
				activity_profile_image_3, activity_profile_image_4, activity_profile_image_5 };
		show_as_gap = gap_indicator != null && gap_indicator.isShown();
		is_rtl = Utils.isRTL(context);
		density = context.getResources().getDisplayMetrics().density;
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

	public void setShowAsGap(final boolean show_gap) {
		show_as_gap = show_gap;
		if (content != null) {
			content.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		}
		if (gap_indicator != null) {
			gap_indicator.setVisibility(!show_gap ? View.GONE : View.VISIBLE);
		}
		if (item_menu != null) {
			item_menu.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		}
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
		content.drawStart(color);
	}

}
