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
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;

import static org.mariotaku.twidere.util.Utils.getStatusTypeIconRes;
import static org.mariotaku.twidere.util.Utils.getThemeColor;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;

public class StatusViewHolder implements Constants {

	public final ImageView my_profile_image, profile_image, image_preview;
	public final TextView name, screen_name, text, time, reply_retweet_status;
	public final View name_container, image_preview_container;
	public final ProgressBar image_preview_progress;
	private final View gap_indicator;
	private final ColorLabelRelativeLayout content;
	private final Resources res;
	private final int theme_color, image_preview_small_width;
	private final float density;
	private final boolean is_rtl;
	public boolean show_as_gap;
	private boolean account_color_enabled;
	private float text_size;
	private int name_display_option;


	public StatusViewHolder(final View view) {
		content = (ColorLabelRelativeLayout) view;
		final int color = getThemeColor(view.getContext());
		theme_color = Color.argb(0x60, Color.red(color), Color.green(color), Color.blue(color));
		gap_indicator = view.findViewById(R.id.list_gap_text);
		image_preview_container = view.findViewById(R.id.image_preview_container);
		profile_image = (ImageView) view.findViewById(R.id.profile_image);
		my_profile_image = (ImageView) view.findViewById(R.id.my_profile_image);
		image_preview = (ImageView) view.findViewById(R.id.image_preview);
		image_preview_progress = (ProgressBar) view.findViewById(R.id.image_preview_progress);
		name_container = view.findViewById(R.id.name_container);
		name = (TextView) view.findViewById(R.id.name);
		screen_name = (TextView) view.findViewById(R.id.screen_name);
		text = (TextView) view.findViewById(R.id.text);
		time = (TextView) view.findViewById(R.id.time);
		reply_retweet_status = (TextView) view.findViewById(R.id.reply_retweet_status);
		show_as_gap = gap_indicator.isShown();
		final Context context = view.getContext();
		res = context.getResources();
		image_preview_small_width = res.getDimensionPixelSize(R.dimen.image_preview_width);
		is_rtl = Utils.isRTL(context);
		density = res.getDisplayMetrics().density;
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
	
	public void setImagePreviewDisplayOption(final int option) {
		final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) image_preview_container.getLayoutParams();
		if (option == IMAGE_PREVIEW_DISPLAY_OPTION_CODE_LARGE) {
			lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
			lp.rightMargin = 0;
			lp.leftMargin = 0;
			if (is_rtl) {
				lp.addRule(RelativeLayout.LEFT_OF, R.id.profile_image);
				lp.addRule(RelativeLayout.RIGHT_OF, R.id.my_profile_image);
			} else {
				lp.addRule(RelativeLayout.RIGHT_OF, R.id.profile_image);
				lp.addRule(RelativeLayout.LEFT_OF, R.id.my_profile_image);
			}
		} else if (option == IMAGE_PREVIEW_DISPLAY_OPTION_CODE_SMALL) {
			lp.width = image_preview_small_width;
			if (is_rtl) {
				lp.leftMargin = 0;
				lp.rightMargin = (int) (density * 16);
				lp.addRule(RelativeLayout.LEFT_OF, R.id.profile_image);
				lp.addRule(RelativeLayout.RIGHT_OF, 0);
			} else {
				lp.leftMargin = (int) (density * 16);
				lp.rightMargin = 0;
				lp.addRule(RelativeLayout.RIGHT_OF, R.id.profile_image);
				lp.addRule(RelativeLayout.LEFT_OF, 0);
			}
		}
		image_preview_container.setLayoutParams(lp);
	}

	public void setIsMyStatus(final boolean my_status) {
		profile_image.setVisibility(my_status ? View.GONE : View.VISIBLE);
		my_profile_image.setVisibility(my_status ? View.VISIBLE : View.GONE);
	}

	public void setIsReplyRetweet(final boolean is_reply, final boolean is_retweet) {
		reply_retweet_status.setVisibility(is_retweet || is_reply ? View.VISIBLE : View.GONE);
	}
	
	public void setName(final String name, final String screen_name) {
		switch (name_display_option) {
			case NAME_DISPLAY_OPTION_CODE_NAME: {
				this.name.setText(name);
				this.screen_name.setText(null);
				this.screen_name.setVisibility(View.GONE);
				break;
			}
			case NAME_DISPLAY_OPTION_CODE_SCREEN_NAME: {
				this.name.setText("@" + screen_name);
				this.screen_name.setText(null);
				this.screen_name.setVisibility(View.GONE);
				break;
			}
			default: {
				this.name.setText(name);
				this.screen_name.setText("@" + screen_name);
				this.screen_name.setVisibility(View.VISIBLE);
				break;
			}
		}
	}
	
	public void setNameDisplayOption(final int option) {
		name_display_option = option;
	}
	
	public void setReplyTo(final String in_reply_to_screen_name) {		
		reply_retweet_status.setText(res.getString(R.string.in_reply_to, in_reply_to_screen_name));
		reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_reply, 0,
				0, 0);
	}
	
	public void setRetweetedBy(final long retweet_count, final String retweeted_by_name, final String retweeted_by_screen_name) {
		if (name_display_option == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME) {
			reply_retweet_status.setText(retweet_count > 1 ? res.getString(
					R.string.retweeted_by_with_count, retweeted_by_screen_name, retweet_count - 1) : res
					.getString(R.string.retweeted_by, retweeted_by_screen_name));
		} else {
			reply_retweet_status.setText(retweet_count > 1 ? res.getString(
					R.string.retweeted_by_with_count, retweeted_by_name, retweet_count - 1) : res
					.getString(R.string.retweeted_by, retweeted_by_name));
		}
		reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_retweet, 0,
				0, 0);
	}

	public void setSelected(final boolean selected) {
		content.setBackgroundColor(selected && !show_as_gap ? theme_color : Color.TRANSPARENT);
	}

	public void setShowAsGap(final boolean show_gap) {
		show_as_gap = show_gap;
		if (show_as_gap) {
			content.setBackgroundResource(0);
			content.drawLabel(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT);
		}
		profile_image.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		my_profile_image.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		image_preview_container.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		name.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		screen_name.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		text.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		time.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		reply_retweet_status.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		gap_indicator.setVisibility(!show_gap ? View.GONE : View.VISIBLE);
	}

	public void setStatusType(final boolean is_favorite, final boolean has_location, final boolean has_media, 
			final boolean is_possibly_sensitive) {
		final int res = getStatusTypeIconRes(is_favorite, has_location, has_media, is_possibly_sensitive);
		time.setCompoundDrawablesWithIntrinsicBounds(0, 0, res, 0);
	}
	
	public void setTextSize(final float text_size) {
		if (this.text_size == text_size) return;
		this.text_size = text_size;
		text.setTextSize(text_size);
		name.setTextSize(text_size);
		screen_name.setTextSize(text_size * 0.75f);
		time.setTextSize(text_size * 0.65f);
		reply_retweet_status.setTextSize(text_size * 0.65f);
	}

	
	public void setUserColor(final int color) {
		content.drawLeft(show_as_gap ? Color.TRANSPARENT : color);
	}
	
	public void setUserType(final boolean is_verified, final boolean is_protected) {
		final int res = getUserTypeIconRes(is_verified, is_protected);
		name.setCompoundDrawablesWithIntrinsicBounds(0, 0, res, 0);
	}
}
