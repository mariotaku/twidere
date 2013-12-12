package org.mariotaku.twidere.service;

import android.content.Intent;
import android.content.res.Resources;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.activity.support.HomeActivity;
import org.mariotaku.twidere.provider.TweetStore.UnreadCounts;
import org.mariotaku.twidere.util.UnreadCountUtils;

public class DashClockHomeUnreadCountService extends DashClockExtension implements TwidereConstants {

	private static final String[] URIS = { UnreadCounts.CONTENT_URI.toString() };

	@Override
	protected void onInitialize(final boolean isReconnect) {
		super.onInitialize(isReconnect);
		addWatchContentUris(URIS);
	}

	@Override
	protected void onUpdateData(final int reason) {
		final ExtensionData data = new ExtensionData();
		final int count = UnreadCountUtils.getUnreadCount(this, TAB_TYPE_HOME_TIMELINE);
		final Resources res = getResources();
		data.visible(count > 0);
		data.icon(R.drawable.ic_extension_twidere);
		data.status(Integer.toString(count));
		data.expandedTitle(res.getQuantityString(R.plurals.N_new_statuses, count, count));
		data.clickIntent(new Intent(this, HomeActivity.class));
		publishUpdate(data);
	}
}
