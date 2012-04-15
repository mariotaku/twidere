package org.mariotaku.twidere.util;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.IUpdateService;
import org.mariotaku.twidere.app.TwidereApplication;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

public class ServiceInterface implements Constants, IUpdateService {

	private IUpdateService mService;
	private Context mContext;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_HOME_TIMELINE_REFRESHED.equals(action)) {
				for (StateListener listener : mStateListeners) {
					if (listener != null) {
						listener.onHomeTimelineRefreshed();
					}
				}
			} else if (BROADCAST_MENTIONS_REFRESHED.equals(action)) {
				for (StateListener listener : mStateListeners) {
					if (listener != null) {
						listener.onMentionsRefreshed();
					}
				}
			}
		}

	};

	private List<StateListener> mStateListeners = new ArrayList<StateListener>();

	private ServiceConnection mConntecion = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName service, IBinder obj) {
			mService = IUpdateService.Stub.asInterface(obj);
			IntentFilter filter = new IntentFilter() {

				{
					addAction(BROADCAST_HOME_TIMELINE_REFRESHED);
					addAction(BROADCAST_MENTIONS_REFRESHED);
				}
			};
			mContext.registerReceiver(mStatusReceiver, filter);
		}

		@Override
		public void onServiceDisconnected(ComponentName service) {
			mService = null;
		}
	};

	public ServiceInterface(Context context) {
		((TwidereApplication) context.getApplicationContext()).getCommonUtils().bindToService(
				mConntecion);
		mContext = context;

	}

	public void addStateListener(StateListener listener) {
		if (listener != null) {
			mStateListeners.add(listener);
		}

	}

	@Override
	public IBinder asBinder() {
		// Useless here
		return null;
	}

	@Override
	public boolean isHomeTimelineRefreshing() {
		if (mService == null) return false;
		try {
			return mService.isHomeTimelineRefreshing();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isMentionsRefreshing() {
		if (mService == null) return false;
		try {
			return mService.isMentionsRefreshing();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void refreshHomeTimeline(long[] account_ids, long[] max_ids) {
		if (mService == null) return;
		try {
			mService.refreshHomeTimeline(account_ids, max_ids);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void refreshMentions(long[] account_ids, long[] max_ids) {
		if (mService == null) return;
		try {
			mService.refreshMentions(account_ids, max_ids);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void refreshMessages(long[] account_ids, long[] max_ids) {
		if (mService == null) return;
		try {
			mService.refreshMessages(account_ids, max_ids);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void removeStateListener(StateListener listener) {
		if (listener != null) {
			mStateListeners.remove(listener);
		}
	}

	@Override
	public void updateStatus(long[] account_ids, String content, Uri image_uri) {
		if (mService == null) return;
		try {
			mService.updateStatus(account_ids, content, image_uri);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public interface StateListener {

		void onHomeTimelineRefreshed();

		void onMentionsRefreshed();

	}
}
