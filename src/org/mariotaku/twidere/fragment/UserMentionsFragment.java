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

import static org.mariotaku.twidere.util.CompareUtils.objectEquals;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;

import java.util.List;

import org.mariotaku.twidere.loader.TweetSearchLoader;
import org.mariotaku.twidere.model.ParcelableStatus;

import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

public class UserMentionsFragment extends SearchTweetsFragment {

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(final Context context, final Bundle args) {
		if (args == null) return null;
		final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
		if (screen_name == null) return null;
		final long account_id = args != null ? args.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
		final long max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
		final long since_id = args.getLong(INTENT_KEY_SINCE_ID, -1);
		final int tab_position = args.getInt(INTENT_KEY_TAB_POSITION, -1);
		getListAdapter().setMentionsHightlightDisabled(
				objectEquals(getAccountScreenName(getActivity(), account_id), screen_name));
		return new TweetSearchLoader(getActivity(), account_id, screen_name.startsWith("@") ? screen_name : "@"
				+ screen_name, max_id, since_id, getData(), getSavedStatusesFileArgs(), tab_position);
	}

	@Override
	protected String[] getSavedStatusesFileArgs() {
		final Bundle args = getArguments();
		if (args == null) return null;
		final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
		final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
		return new String[] { AUTHORITY_USER_MENTIONS, "account" + account_id, "screen_name" + screen_name };
	}

}
