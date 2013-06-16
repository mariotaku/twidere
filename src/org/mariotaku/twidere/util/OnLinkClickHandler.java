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
import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.openTweetSearch;
import static org.mariotaku.twidere.util.Utils.openUserListDetails;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.parseLong;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import edu.ucdavis.earlybird.ProfilingUtil;

public class OnLinkClickHandler implements OnLinkClickListener, Constants {

	protected final Activity activity;

	public OnLinkClickHandler(final Context context) {
		activity = context instanceof Activity ? (Activity) context : null;
	}

	@Override
	public void onLinkClick(final String link, final String orig, final long account_id, final int type, final boolean sensitive) {
		if (activity == null) return;
		// UCD
		ProfilingUtil.profile(activity, account_id, "Click, " + link + ", " + type);

		if (activity == null) return;
		switch (type) {
			case TwidereLinkify.LINK_TYPE_MENTION: {
				openUserProfile(activity, account_id, -1, link);
				break;
			}
			case TwidereLinkify.LINK_TYPE_HASHTAG: {
				openTweetSearch(activity, account_id, link);
				break;
			}
			case TwidereLinkify.LINK_TYPE_LINK_WITH_IMAGE_EXTENSION: {
				openImage(activity, link, orig, sensitive);
				break;
			}
			case TwidereLinkify.LINK_TYPE_LINK: {
				openLink(link);
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
			case TwidereLinkify.LINK_TYPE_USER_ID: {
				openUserProfile(activity, account_id, parseLong(link), null);
				break;
			}
			case TwidereLinkify.LINK_TYPE_STATUS: {
				openStatus(activity, account_id, parseLong(link));
				break;
			}
		}
	}

	protected void openLink(final String link) {
		final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
	}
}
