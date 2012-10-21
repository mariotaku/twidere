package edu.ucdavis.earlybird;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Request location ONCE per WAKE_PERIOD_IN_MILLI.
 */
public class UCDService extends Service {

	public static final long LOCATION_PERIOD_IN_MILLI = 15 * 60 * 1000;
	public static final String ACTION_GET_LOCATION = "edu.ucdavis.earlybird.GET_LOCATION";
	private LocationManager mLocationManager;
	private FineLocationListener mFineLocationListener;
	private AlarmManager mAlarmManager;
	private AlarmReceiver mAlarmReceiver;
	private PendingIntent StartIntent;

	@Override
	public IBinder onBind(final Intent intent) {
		throw new IllegalStateException("Not implemented.");
	}

	@Override
	public void onCreate() {
		super.onCreate();

		ProfilingUtil.log("onCreate");
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mFineLocationListener = new FineLocationListener();
		mAlarmManager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);

		mAlarmReceiver = new AlarmReceiver();
		final IntentFilter myFilter = new IntentFilter();
		myFilter.addAction(ACTION_GET_LOCATION);
		registerReceiver(mAlarmReceiver, myFilter);

		final Intent intent = new Intent(ACTION_GET_LOCATION);
		StartIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), LOCATION_PERIOD_IN_MILLI,
				StartIntent);

		// Upload Service
		final Intent i = new Intent(UploadReceiver.ACTION_UPLOAD_PROFILE);
		final PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 12 * 60 * 60 * 1000, pi);
	}

	private final class AlarmReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			ProfilingUtil.log("AlarmReceiver");
			final Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			final String provider = mLocationManager.getBestProvider(criteria, true);
			mLocationManager.requestLocationUpdates(provider, 0, 0, mFineLocationListener);
		}
	}

	private final class FineLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(final Location location) {
			ProfilingUtil.profiling(UCDService.this, ProfilingUtil.FILE_NAME_LOCATION, location.getTime() + ","
					+ location.getLatitude() + "," + location.getLongitude() + "," + location.getProvider());
			ProfilingUtil.log(location.getTime() + "," + location.getLatitude() + "," + location.getLongitude() + ","
					+ location.getProvider());

			mLocationManager.removeUpdates(mFineLocationListener);
		}

		@Override
		public void onProviderDisabled(final String provider) {
			ProfilingUtil.log("onProviderDisabled");
		}

		@Override
		public void onProviderEnabled(final String provider) {
			ProfilingUtil.log("onProviderEnabled");
		}

		@Override
		public void onStatusChanged(final String provider, final int status, final Bundle extras) {
			ProfilingUtil.log("onStatusChanged");
		}
	}
}
