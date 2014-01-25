/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment.support;

import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.openStatuses;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.openUsers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import org.mariotaku.twidere.adapter.BaseParcelableActivitiesAdapter;
import org.mariotaku.twidere.adapter.ParcelableActivitiesByFriendsAdapter;
import org.mariotaku.twidere.loader.ActivitiesByFriendsLoader;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;

import java.util.Arrays;
import java.util.List;

public class ActivitiesByFriendsFragment extends BaseActivitiesListFragment {

	@Override
	public BaseParcelableActivitiesAdapter createListAdapter(final Context context) {
		return new ParcelableActivitiesByFriendsAdapter(context);
	}

	@Override
	public Loader<List<ParcelableActivity>> onCreateLoader(final int id, final Bundle args) {
		setProgressBarIndeterminateVisibility(true);
		final long account_id = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
		return new ActivitiesByFriendsLoader(getActivity(), account_id, getData(), getSavedActivitiesFileArgs(),
				getTabPosition());
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final int adapter_pos = position - l.getHeaderViewsCount();
		final ParcelableActivity item = getListAdapter().getItem(adapter_pos);
		if (item == null) return;
		final ParcelableUser[] sources = item.sources;
		if (sources == null || sources.length == 0) return;
		final ParcelableStatus[] targetStatuses = item.target_statuses;
		final ParcelableUser[] targetUsers = item.target_users;
		final ParcelableStatus[] target_object_statuses = item.target_object_statuses;
		switch (item.action) {
			case ParcelableActivity.ACTION_FAVORITE: {
				if (targetStatuses == null || targetStatuses.length == 0) return;
				if (targetStatuses.length == 1) {
					openStatus(getActivity(), targetStatuses[0]);
				} else {
					final List<ParcelableStatus> statuses = Arrays.asList(targetStatuses);
					openStatuses(getActivity(), statuses);
				}
				break;
			}
			case ParcelableActivity.ACTION_FOLLOW: {
				if (targetUsers == null || targetUsers.length == 0) return;
				if (targetUsers.length == 1) {
					openUserProfile(getActivity(), targetUsers[0]);
				} else {
					final List<ParcelableUser> users = Arrays.asList(targetUsers);
					openUsers(getActivity(), users);
				}
				break;
			}
			case ParcelableActivity.ACTION_MENTION: {
				if (target_object_statuses != null && target_object_statuses.length > 0) {
					openStatus(getActivity(), target_object_statuses[0]);
				}
				break;
			}
			case ParcelableActivity.ACTION_REPLY: {
				if (targetStatuses != null && targetStatuses.length > 0) {
					openStatus(getActivity(), targetStatuses[0]);
				}
				break;
			}
			case ParcelableActivity.ACTION_RETWEET: {
				if (targetStatuses == null || targetStatuses.length == 0) return;
				if (targetStatuses.length == 1) {
					openStatus(getActivity(), targetStatuses[0]);
				} else {
					final List<ParcelableStatus> statuses = Arrays.asList(targetStatuses);
					openStatuses(getActivity(), statuses);
				}
				break;
			}
		}
	}

	@Override
	protected String[] getSavedActivitiesFileArgs() {
		final Bundle args = getArguments();
		if (args == null) return null;
		final long account_id = args.getLong(EXTRA_ACCOUNT_ID, -1);
		return new String[] { AUTHORITY_ACTIVITIES_BY_FRIENDS, "account" + account_id };
	}

}
