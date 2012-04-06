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
import android.os.IBinder;
import android.os.RemoteException;

public class ServiceInterface implements Constants, IUpdateService {

	private IUpdateService mService;
	private Context mContext;

	private BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {

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
			mContext.registerReceiver(mMediaStatusReceiver, filter);
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
	public void refreshHomeTimeline(long[] account_ids, int count) {
		if (mService == null) return;
		try {
			mService.refreshHomeTimeline(account_ids, count);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void refreshMentions(long[] account_ids, int count) {
		if (mService == null) return;
		try {
			mService.refreshMentions(account_ids, count);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void refreshMessages(long[] account_ids, int count) {
		if (mService == null) return;
		try {
			mService.refreshMessages(account_ids, count);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void removeStateListener(StateListener listener) {
		if (listener != null) {
			mStateListeners.remove(listener);
		}
	}

	public interface StateListener {

		void onHomeTimelineRefreshed();

		void onMentionsRefreshed();

	}
}
