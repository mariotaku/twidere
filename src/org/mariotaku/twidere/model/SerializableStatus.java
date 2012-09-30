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

import static org.mariotaku.twidere.util.HtmlEscapeHelper.unescape;
import static org.mariotaku.twidere.util.Utils.parseString;

import java.io.Serializable;
import java.net.URL;

public class SerializableStatus implements Serializable {

	private static final long serialVersionUID = -8496911950475055067L;

	public long retweet_id, retweeted_by_id, status_id, account_id, user_id, status_timestamp, retweet_count,
			in_reply_to_status_id;

	public boolean is_gap, is_retweet, is_favorite, is_protected, is_verified, has_media;

	public String retweeted_by_name, retweeted_by_screen_name, text_html, text_plain, name, screen_name,
			in_reply_to_screen_name, source, profile_image_url_string, image_preview_url_string, image_orig_url_string,
			location_string;
	public String location;

	public URL profile_image_url, image_preview_url;

	public SerializableStatus(final ParcelableStatus in) {
		retweet_id = in.retweet_id;
		retweeted_by_id = in.retweeted_by_id;
		status_id = in.status_id;
		account_id = in.account_id;
		user_id = in.user_id;
		status_timestamp = in.status_timestamp;
		retweet_count = in.retweet_count;
		in_reply_to_status_id = in.in_reply_to_status_id;
		is_gap = in.is_gap;
		is_retweet = in.is_retweet;
		is_favorite = in.is_favorite;
		is_protected = in.is_protected;
		is_verified = in.is_verified;
		has_media = in.has_media;
		retweeted_by_name = in.retweeted_by_name;
		retweeted_by_screen_name = in.retweeted_by_screen_name;
		text_html = in.text_html;
		text_plain = in.text_plain;
		name = in.name;
		screen_name = in.screen_name;
		in_reply_to_screen_name = in.in_reply_to_screen_name;
		source = in.source;
		profile_image_url_string = in.profile_image_url_string;
		image_preview_url_string = in.image_preview_url_string;
		image_orig_url_string = in.image_orig_url_string;
		location_string = in.location_string;
		location = parseString(in.location);
		image_preview_url = in.image_preview_url;
		profile_image_url = in.profile_image_url;
	}

	SerializableStatus() {
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof SerializableStatus)) return false;
		final SerializableStatus that = (SerializableStatus) o;
		return status_id == that.status_id;
	}

	@Override
	public String toString() {
		return unescape(text_html);
	}
}
