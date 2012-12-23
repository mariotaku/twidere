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

package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.Utils.openImage;
import static org.mariotaku.twidere.util.Utils.openTweetSearch;
import static org.mariotaku.twidere.util.Utils.openUserListDetails;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import edu.ucdavis.earlybird.ProfilingUtil;

public class OnLinkClickHandler implements OnLinkClickListener {

	private final Activity activity;
	private final long account_id;
	private final boolean is_possibly_sensitive;

	public OnLinkClickHandler(final Context context, final long account_id) {
		this(context, account_id, false);
	}

	public OnLinkClickHandler(final Context context, final long account_id, final boolean is_possibly_sensitive) {
		activity = context instanceof Activity ? (Activity) context : null;
		this.account_id = account_id;
		this.is_possibly_sensitive = is_possibly_sensitive;
	}

	@Override
	public void onLinkClick(final String link, final int type) {

		// UCD
		ProfilingUtil.profile(activity, account_id, "Click, " + link + ", " + type);

		if (activity == null) return;
		switch (type) {
			case TwidereLinkify.LINK_TYPE_MENTION_LIST: {
				openUserProfile(activity, account_id, -1, link);
				break;
			}
			case TwidereLinkify.LINK_TYPE_HASHTAG: {
				openTweetSearch(activity, account_id, link);
				break;
			}
			case TwidereLinkify.LINK_TYPE_LINK_WITH_IMAGE_EXTENSION: {
				openImage(activity, Uri.parse(link), is_possibly_sensitive);
				break;
			}
			case TwidereLinkify.LINK_TYPE_LINK: {
				final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
				activity.startActivity(intent);
				break;
			}
			case TwidereLinkify.LINK_TYPE_LIST: {
				final String[] mention_list = link.split("\\/");
				if (mention_list == null || mention_list.length != 2) {
					break;
				}
				openUserListDetails(activity, account_id, -1, -1, mention_list[0], mention_list[1]);
				break;
			}
			case TwidereLinkify.LINK_TYPE_CASHTAG: {
				openTweetSearch(activity, account_id, link);
				break;
			}
		}
	}
}
