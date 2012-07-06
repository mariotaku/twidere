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

package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;

import twitter4j.Twitter;
import twitter4j.DirectMessage;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import java.util.Collections;
import java.util.Comparator;
import twitter4j.ResponseList;
import twitter4j.Paging;
import android.content.SharedPreferences;
import twitter4j.TwitterException;

public abstract class DirectMessagesLoader extends AsyncTaskLoader<List<DirectMessage>> implements Constants {

	private final Twitter mTwitter;
	private final long mAccountId, mMaxId;
	private final List<DirectMessage> mData;

	public DirectMessagesLoader(Context context, long account_id, long max_id, List<DirectMessage> data) {
		super(context);
		mTwitter = getTwitterInstance(context, account_id, true);
		mAccountId = account_id;
		mMaxId = max_id;
		mData = data != null ? data : new ArrayList<DirectMessage>();
	}
	
	public static final Comparator<DirectMessage> TWITTER4J_DIRECT_MESSAGE_COMPARATOR = new Comparator<DirectMessage>() {

		@Override
		public int compare(DirectMessage object1, DirectMessage object2) {
			final long diff = object2.getId() - object1.getId();
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public boolean containsStatus(long message_id) {
		for (final DirectMessage message : mData) {
			if (message.getId() == message_id) return true;
		}
		return false;
	}

	public boolean deleteStatus(long message_id) {
		for (final DirectMessage message : mData) {
			if (message.getId() == message_id) return mData.remove(message);
		}
		return false;
	}

	public long getAccountId() {
		return mAccountId;
	}

	public List<DirectMessage> getData() {
		return mData;
	}

	public Twitter getTwitter() {
		return mTwitter;
	}
	
	public abstract ResponseList<DirectMessage> getDirectMessages(Paging paging) throws TwitterException;

	@Override
	public List<DirectMessage> loadInBackground(){
		ResponseList<DirectMessage> statuses = null;
		try {
			final Paging paging = new Paging();
			final SharedPreferences prefs = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME,
																			  Context.MODE_PRIVATE);
			final int load_item_limit = prefs
				.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT, PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
			paging.setCount(load_item_limit);
			if (mMaxId > 0) {
				paging.setMaxId(mMaxId);
			}
			statuses = getDirectMessages(paging);
		} catch (final TwitterException e) {
			e.printStackTrace();
		}
		if (statuses != null) {
			mData.addAll(statuses);			
		}
		Collections.sort(mData, TWITTER4J_DIRECT_MESSAGE_COMPARATOR);
		return mData;
		
		
	};

	@Override
	public void onStartLoading() {
		forceLoad();
	}

}
