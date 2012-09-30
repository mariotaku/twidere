package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.Utils.openTweetSearch;
import static org.mariotaku.twidere.util.Utils.openUserListDetails;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

import org.mariotaku.twidere.fragment.StatusFragment;
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class OnLinkClickHandler implements OnLinkClickListener {

	private final Activity activity;
	private final long account_id;

	public OnLinkClickHandler(final Context context, final long account_id) {
		activity = context instanceof Activity ? (Activity) context : null;
		this.account_id = account_id;
	}

	@Override
	public void onLinkClick(final String link, final int type) {
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
				final Intent intent = new Intent(StatusFragment.INTENT_ACTION_VIEW_IMAGE, Uri.parse(link));
				intent.setPackage(activity.getPackageName());
				activity.startActivity(intent);
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