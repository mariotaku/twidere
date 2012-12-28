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

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusViewHolder implements Constants {

	public final ImageView profile_image, image_preview;
	public final TextView name, text, time, reply_retweet_status;
	public final View image_preview_frame;
	private final Context context;
	private final Resources res;
	private final View gap_indicator;
	private final ColorLabelRelativeLayout content;
	private final UserBadgeImageGetter getter;
	public boolean show_as_gap;
	private boolean account_color_enabled;
	private float text_size;

	public StatusViewHolder(final View view) {
		content = (ColorLabelRelativeLayout) view;
		context = view.getContext();
		res = context.getResources();
		getter = new UserBadgeImageGetter(res);
		gap_indicator = view.findViewById(R.id.list_gap_text);
		image_preview_frame = view.findViewById(R.id.image_preview_frame);
		profile_image = (ImageView) view.findViewById(R.id.profile_image);
		image_preview = (ImageView) view.findViewById(R.id.image_preview);
		name = (TextView) view.findViewById(R.id.name);
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

	public void setName(final String name, final String screen_name, final int name_display_option,
			final boolean is_verified, final boolean is_protected, final boolean is_rtl) {
		if (name_display_option == NAME_DISPLAY_OPTION_CODE_NAME) {
			this.name.setText(Html.fromHtml("<b>" + name + "</b>" + getUserType(is_verified, is_protected), getter,
					null));
		} else if (name_display_option == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME) {
			this.name.setText(Html.fromHtml(
					"<b>" + "@" + screen_name + "</b>" + getUserType(is_verified, is_protected), getter, null));
		} else {
			if (is_rtl) {
				this.name.setText(Html.fromHtml(
						"<small>" + "@" + screen_name + "</small>" + " " + getUserType(is_verified, is_protected)
								+ "<b>" + name + "</b>", getter, null));
			} else {
				this.name.setText(Html.fromHtml("<b>" + name + "</b>" + getUserType(is_verified, is_protected) + " "
						+ "<small>" + "@" + screen_name + "</small>", getter, null));
			}
		}
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
		image_preview_frame.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		name.setVisibility(show_gap ? View.GONE : View.VISIBLE);
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
			time.setTextSize(text_size * 0.65f);
			reply_retweet_status.setTextSize(text_size * 0.65f);
		}
	}

	public void setUserColor(final int color) {
		content.drawLeft(show_as_gap ? Color.TRANSPARENT : color);
	}

	private static String getUserType(final boolean is_verified, final boolean is_protected) {
		if (is_verified)
			return "<img src=\"v\"/>";
		else if (is_protected) return "<img src=\"p\"/>";
		return "";
	}

	static final class UserBadgeImageGetter implements ImageGetter {

		final Resources res;

		UserBadgeImageGetter(final Resources res) {
			this.res = res;
		}

		@Override
		public Drawable getDrawable(final String source) {
			final Bitmap b;
			if ("p".equals(source)) {
				b = BitmapFactory.decodeResource(res, R.drawable.ic_indicator_is_protected);
			} else if ("v".equals(source)) {
				b = BitmapFactory.decodeResource(res, R.drawable.ic_indicator_verified);
			} else
				return null;
			final BitmapDrawable d = new BitmapDrawable(res, b);
			d.setBounds(0, -d.getIntrinsicHeight() * 3 / 2, d.getIntrinsicWidth(), 0);
			d.setGravity(Gravity.TOP);
			return d;
		}
	}

}
