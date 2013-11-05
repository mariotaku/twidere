/*
 * 				Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.preference;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import org.mariotaku.twidere.activity.KumaKichiActivity;
import org.mariotaku.twidere.activity.MainActivity;

public class ChatterBoxPreference extends CheckBoxPreference {

	public Handler mHandler = new Handler();
	protected int mClickCount;

	private final Runnable mResetCounterRunnable = new Runnable() {

		@Override
		public void run() {
			mClickCount = 0;
		}
	};

	public ChatterBoxPreference(final Context context) {
		this(context, null);
	}

	public ChatterBoxPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.checkBoxPreferenceStyle);
	}

	public ChatterBoxPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onClick() {
		super.onClick();
		mHandler.removeCallbacks(mResetCounterRunnable);
		mClickCount++;
		if (mClickCount >= 11) {
			mClickCount = 0;
			final Context context = getContext();
			final PackageManager pm = context.getPackageManager();
			final ComponentName main = new ComponentName(context, MainActivity.class);
			final ComponentName kumakichi = new ComponentName(context, KumaKichiActivity.class);
			final boolean main_disabled = pm.getComponentEnabledSetting(main) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
			pm.setComponentEnabledSetting(main, main_disabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
					: PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
			pm.setComponentEnabledSetting(kumakichi, main_disabled ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
					: PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
			if (main_disabled) {
				Toast.makeText(context, "Restore!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(context, "@HondaJOJO!", Toast.LENGTH_SHORT).show();
			}
		}
		mHandler.postDelayed(mResetCounterRunnable, 3000);
	}

}
