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

package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getAccountScreenName;

import org.mariotaku.twidere.loader.DummyParcelableStatusesLoader;
import org.mariotaku.twidere.loader.TweetSearchLoader;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.SynchronizedStateSavedList;
import org.mariotaku.twidere.util.Utils;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class UserMentionsFragment extends SearchTweetsFragment {

	@Override
	public Loader<SynchronizedStateSavedList<ParcelableStatus, Long>> newLoaderInstance(final Bundle args) {
		final long account_id = args != null ? args.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
		if (args == null) return new DummyParcelableStatusesLoader(getActivity(), account_id, getData());
		final long max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
		final long since_id = args.getLong(INTENT_KEY_SINCE_ID, -1);
		final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
		final boolean is_home_tab = args.getBoolean(INTENT_KEY_IS_HOME_TAB);
		getListAdapter().setMentionsHightlightDisabled(
				Utils.equals(getAccountScreenName(getActivity(), account_id), screen_name));
		if (screen_name == null) return new DummyParcelableStatusesLoader(getActivity(), account_id, getData());
		return new TweetSearchLoader(getActivity(), account_id, screen_name.startsWith("@") ? screen_name : "@"
				+ screen_name, max_id, since_id, getData(), getClass().getSimpleName(), is_home_tab);
	}

	@Override
	boolean saveStatuses() {
		if (getActivity() == null || getView() == null) return false;
		final int first_visible_position = getListView().getFirstVisiblePosition();
		final long status_id = getListAdapter().findItemIdByPosition(first_visible_position);
		TweetSearchLoader.writeSerializableStatuses(this, getActivity(), getData(), status_id, getArguments());
		return true;
	}
}
