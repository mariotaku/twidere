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

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.mariotaku.twidere.util.Utils.formatSameDayTime;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.openUserFollowers;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.parseString;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.adapter.iface.IBaseAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.loader.ActivitiesAboutMeLoader;
import org.mariotaku.twidere.loader.Twitter4JActivitiesLoader;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.view.holder.ActivityViewHolder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.text.TextUtils;

public class ActivitiesAboutMeFragment extends BaseActivitiesListFragment {

	@Override
	public Loader<List<ParcelableActivity>> onCreateLoader(final int id, final Bundle args) {
		setProgressBarIndeterminateVisibility(true);
		final long account_id = args != null ? args.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
		return new ActivitiesAboutMeLoader(getActivity(), account_id, getData(), getSavedActivitiesFileArgs(), getTabPosition());
	}

	protected String[] getSavedActivitiesFileArgs() {
		final Bundle args = getArguments();
		if (args == null) return null;
		final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
		return new String[] { AUTHORITY_MY_ACTIVITIES, "account" +  account_id};
	}

}
