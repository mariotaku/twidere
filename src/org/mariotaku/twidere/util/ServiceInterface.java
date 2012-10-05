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

import static org.mariotaku.twidere.util.ServiceUtils.bindToService;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.ITwidereService;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

public final class ServiceInterface implements Constants, ITwidereService {

	private ITwidereService mService;

	private final ServiceConnection mConntecion = new ServiceConnection() {

		@Override
		public void onServiceConnected(final ComponentName service, final IBinder obj) {
			mService = ITwidereService.Stub.asInterface(obj);
		}

		@Override
		public void onServiceDisconnected(final ComponentName service) {
			mService = null;
		}
	};

	private static ServiceInterface sInstance;

	private static final String TWIDERE_PACKAGE_NAME = "org.mariotaku.twidere";

	private ServiceInterface(final Context context) {
		final Intent intent = new Intent(INTENT_ACTION_SERVICE);
		intent.setPackage(TWIDERE_PACKAGE_NAME);
		bindToService(context, intent, mConntecion);
	}

	@Override
	public int addUserListMember(final long account_id, final int list_id, final long user_id, final String screen_name) {
		if (mService == null) return -1;
		try {
			return mService.addUserListMember(account_id, list_id, user_id, screen_name);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public IBinder asBinder() {
		// Useless here
		return mService.asBinder();
	}

	@Override
	public void clearNotification(final int id) {
		if (mService == null) return;
		try {
			mService.clearNotification(id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}

	}

	@Override
	public int createBlock(final long account_id, final long user_id) {
		if (mService == null) return -1;
		try {
			return mService.createBlock(account_id, user_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int createFavorite(final long account_id, final long status_id) {
		if (mService == null) return -1;
		try {
			return mService.createFavorite(account_id, status_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int createFriendship(final long account_id, final long user_id) {
		if (mService == null) return -1;
		try {
			return mService.createFriendship(account_id, user_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int createMultiBlock(final long account_id, final long[] user_ids) {
		if (mService == null) return -1;
		try {
			return mService.createMultiBlock(account_id, user_ids);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int createUserList(final long account_id, final String list_name, final boolean is_public,
			final String description) {
		if (mService == null) return -1;
		try {
			return mService.createUserList(account_id, list_name, is_public, description);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int createUserListSubscription(final long account_id, final int list_id) {
		if (mService == null) return -1;
		try {
			return mService.createUserListSubscription(account_id, list_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int deleteUserListMember(final long account_id, final int list_id, final long user_id) {
		if (mService == null) return -1;
		try {
			return mService.deleteUserListMember(account_id, list_id, user_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int destroyBlock(final long account_id, final long user_id) {
		if (mService == null) return -1;
		try {
			return mService.destroyBlock(account_id, user_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int destroyDirectMessage(final long account_id, final long message_id) {
		if (mService == null) return -1;
		try {
			return mService.destroyDirectMessage(account_id, message_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int destroyFavorite(final long account_id, final long status_id) {
		if (mService == null) return -1;
		try {
			return mService.destroyFavorite(account_id, status_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int destroyFriendship(final long account_id, final long user_id) {
		if (mService == null) return -1;
		try {
			return mService.destroyFriendship(account_id, user_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int destroyStatus(final long account_id, final long status_id) {
		if (mService == null) return -1;
		try {
			return mService.destroyStatus(account_id, status_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int destroyUserList(final long account_id, final int list_id) {
		if (mService == null) return -1;
		try {
			return mService.destroyUserList(account_id, list_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int destroyUserListSubscription(final long account_id, final int list_id) {
		if (mService == null) return -1;
		try {
			return mService.destroyUserListSubscription(account_id, list_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getHomeTimeline(final long[] account_ids, final long[] max_ids) {
		if (mService == null) return -1;
		try {
			return mService.getHomeTimeline(account_ids, max_ids);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getHomeTimelineWithSinceIds(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		if (mService == null) return -1;
		try {
			return mService.getHomeTimelineWithSinceIds(account_ids, max_ids, since_ids);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getLocalTrends(final long account_id, final int woeid) {
		if (mService == null) return -1;
		try {
			return mService.getLocalTrends(account_id, woeid);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getMentions(final long[] account_ids, final long[] max_ids) {
		if (mService == null) return -1;
		try {
			return mService.getMentions(account_ids, max_ids);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getMentionsWithSinceIds(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		if (mService == null) return -1;
		try {
			return mService.getMentionsWithSinceIds(account_ids, max_ids, since_ids);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getReceivedDirectMessages(final long[] account_ids, final long[] max_ids) {
		if (mService == null) return -1;
		try {
			return mService.getReceivedDirectMessages(account_ids, max_ids);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getReceivedDirectMessagesWithSinceIds(final long[] account_ids, final long[] max_ids,
			final long[] since_ids) {
		if (mService == null) return -1;
		try {
			return mService.getReceivedDirectMessagesWithSinceIds(account_ids, max_ids, since_ids);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getSentDirectMessages(final long[] account_ids, final long[] max_ids) {
		if (mService == null) return -1;
		try {
			return mService.getSentDirectMessages(account_ids, max_ids);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getSentDirectMessagesWithSinceIds(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		if (mService == null) return -1;
		try {
			return mService.getSentDirectMessagesWithSinceIds(account_ids, max_ids, since_ids);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public boolean hasActivatedTask() {
		if (mService == null) return false;
		try {
			return mService.hasActivatedTask();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isHomeTimelineRefreshing() {
		if (mService == null) return false;
		try {
			return mService.isHomeTimelineRefreshing();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isLocalTrendsRefreshing() {
		if (mService == null) return false;
		try {
			return mService.isLocalTrendsRefreshing();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isMentionsRefreshing() {
		if (mService == null) return false;
		try {
			return mService.isMentionsRefreshing();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isReceivedDirectMessagesRefreshing() {
		if (mService == null) return false;
		try {
			return mService.isReceivedDirectMessagesRefreshing();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isSentDirectMessagesRefreshing() {
		if (mService == null) return false;
		try {
			return mService.isSentDirectMessagesRefreshing();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int refreshAll() {
		if (mService == null) return -1;
		try {
			return mService.refreshAll();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int reportMultiSpam(final long account_id, final long[] user_ids) {
		if (mService == null) return -1;
		try {
			return mService.reportMultiSpam(account_id, user_ids);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int reportSpam(final long account_id, final long user_id) {
		if (mService == null) return -1;
		try {
			return mService.reportSpam(account_id, user_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int retweetStatus(final long account_id, final long status_id) {
		if (mService == null) return -1;
		try {
			return mService.retweetStatus(account_id, status_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int sendDirectMessage(final long account_id, final String screen_name, final long user_id,
			final String message) {
		if (mService == null) return -1;
		try {
			return mService.sendDirectMessage(account_id, screen_name, user_id, message);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public void shutdownService() {
		if (mService == null) return;
		try {
			mService.shutdownService();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean startAutoRefresh() {
		if (mService == null) return false;
		try {
			return mService.startAutoRefresh();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void stopAutoRefresh() {
		if (mService == null) return;
		try {
			mService.stopAutoRefresh();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean test() {
		if (mService == null) return false;
		try {
			return mService.test();
		} catch (final RemoteException e) {
			// Maybe service died, so we return false value to let
			// ServiceInterface restart the service.
		}
		return false;
	}

	@Override
	public int updateProfile(final long account_id, final String name, final String url, final String location,
			final String description) {
		if (mService == null) return -1;
		try {
			return mService.updateProfile(account_id, name, url, location, description);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int updateProfileImage(final long account_id, final Uri image_uri, final boolean delete_image) {
		if (mService == null) return -1;
		try {
			return mService.updateProfileImage(account_id, image_uri, delete_image);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int updateStatus(final long[] account_ids, final String content, final Location location,
			final Uri image_uri, final long in_reply_to, final boolean delete_image) {
		if (mService == null) return -1;
		try {
			return mService.updateStatus(account_ids, content, location, image_uri, in_reply_to, delete_image);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int updateUserListDetails(final long account_id, final int list_id, final boolean is_public,
			final String name, final String description) {
		if (mService == null) return -1;
		try {
			return mService.updateUserListDetails(account_id, list_id, is_public, name, description);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void waitForService() {
		while (mService == null) {
			try {
				Thread.sleep(100L);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static ServiceInterface getInstance(final Application application) {
		if (sInstance == null || !sInstance.test()) {
			sInstance = new ServiceInterface(application);
		}
		return sInstance;
	}

}
