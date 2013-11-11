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

import static org.mariotaku.twidere.util.Utils.getDisplayName;
import static org.mariotaku.twidere.util.Utils.getStatusTypeIconRes;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.ShortTimeView;
import org.mariotaku.twidere.view.iface.IColorLabelView;

public class StatusViewHolder extends CardViewHolder {

	public final ImageView my_profile_image, profile_image, image_preview;
	public final TextView name, screen_name, reply_retweet_status;
	public final ShortTimeView time;
	public final TextView text;
	public final View image_preview_container;
	public final ProgressBar image_preview_progress;
	private final View gap_indicator;
	private final View card;
	private final IColorLabelView content;

	private final float density;
	private final boolean is_rtl;
	public boolean show_as_gap;
	private boolean account_color_enabled;
	private float text_size;
	private boolean nickname_only, name_first;

	public StatusViewHolder(final View view) {
		super(view);
		final Context context = getContext();
		card = findViewById(R.id.card);
		content = (IColorLabelView) findViewById(R.id.content);
		gap_indicator = findViewById(R.id.list_gap_text);
		image_preview_container = findViewById(R.id.image_preview_container);
		profile_image = (ImageView) findViewById(R.id.profile_image);
		my_profile_image = (ImageView) findViewById(R.id.my_profile_image);
		image_preview = (ImageView) findViewById(R.id.image_preview);
		image_preview_progress = (ProgressBar) findViewById(R.id.image_preview_progress);
		name = (TextView) findViewById(R.id.name);
		screen_name = (TextView) findViewById(R.id.screen_name);
		text = (TextView) findViewById(R.id.text);
		time = (ShortTimeView) findViewById(R.id.time);
		reply_retweet_status = (TextView) findViewById(R.id.reply_retweet_status);
		show_as_gap = gap_indicator != null ? gap_indicator.isShown() : false;
		is_rtl = Utils.isRTL(context);
		density = context.getResources().getDisplayMetrics().density;
	}

	public void setAccountColor(final int color) {
		content.drawEnd(account_color_enabled && !show_as_gap ? color : Color.TRANSPARENT);
	}

	public void setAccountColorEnabled(final boolean enabled) {
		account_color_enabled = enabled && !show_as_gap;
		if (!account_color_enabled) {
			content.drawEnd(Color.TRANSPARENT);
		}
	}

	public void setDisplayNameFirst(final boolean name_first) {
		this.name_first = name_first;
	}

	public void setHighlightColor(final int color) {
		content.drawBackground(show_as_gap ? Color.TRANSPARENT : color);
	}

	public void setIsMyStatus(final boolean my_status) {
		profile_image.setVisibility(my_status ? View.GONE : View.VISIBLE);
		my_profile_image.setVisibility(my_status ? View.VISIBLE : View.GONE);
		final MarginLayoutParams lp = (MarginLayoutParams) time.getLayoutParams();
		if (is_rtl) {
			lp.leftMargin = (int) (my_status ? 6 * density : 0);
		} else {
			lp.rightMargin = (int) (my_status ? 6 * density : 0);
		}
	}

	public void setIsReplyRetweet(final boolean is_reply, final boolean is_retweet) {
		reply_retweet_status.setVisibility(is_retweet || is_reply ? View.VISIBLE : View.GONE);
	}

	public void setNicknameOnly(final boolean nickname_only) {
		this.nickname_only = nickname_only;
	}

	public void setReplyTo(final long user_id, final String name, final String screen_name) {
		final String display_name = getDisplayName(getContext(), user_id, name, screen_name, name_first, nickname_only,
				false);
		reply_retweet_status.setText(getString(R.string.in_reply_to, display_name));
		reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_reply, 0, 0, 0);
	}

	public void setRetweetedBy(final long count, final long user_id, final String name, final String screen_name) {
		final String display_name = getDisplayName(getContext(), user_id, name, screen_name, name_first, nickname_only,
				false);
		reply_retweet_status.setText(count > 1 ? getString(R.string.retweeted_by_with_count, display_name, count - 1)
				: getString(R.string.retweeted_by, display_name));
		reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_retweet, 0, 0, 0);
	}

	public void setShowAsGap(final boolean show_gap) {
		show_as_gap = show_gap;
		if (card != null) {
			card.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		}
		if (gap_indicator != null) {
			gap_indicator.setVisibility(!show_gap ? View.GONE : View.VISIBLE);
		}
		if (item_menu != null) {
			item_menu.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		}
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
		content.drawStart(show_as_gap ? Color.TRANSPARENT : color);
	}

	public void setUserType(final boolean is_verified, final boolean is_protected) {
		final int res = getUserTypeIconRes(is_verified, is_protected);
		name.setCompoundDrawablesWithIntrinsicBounds(0, 0, res, 0);
	}
}
