/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.preference;

import static org.mariotaku.twidere.Constants.PREFERENCE_KEY_NOTIFICATION_RINGTONE;
import static org.mariotaku.twidere.Constants.SHARED_PREFERENCES_NAME;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * A custom {@link Preference} that invokes a
 * {@link #setOnPreferenceClickListener(OnPreferenceClickListener)} allowing the
 * user to select a custom notification tone for Twidere
 */
public class RingtonePreference extends Preference implements OnPreferenceClickListener {

    /** The request code for {@link Activity#onActivityResult} */
    public static final int REQUEST_NOTIFICATION_TONE = 0;

    /**
     * Constructor for <code>RingtonePreference</code>
     * 
     * @param context The {@link Context} to use
     * @param attrs The attributes of the XML tag that is inflating the
     *            preference
     */
    public RingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnPreferenceClickListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        openRingtonePicker();
        return true;
    }

    private void openRingtonePicker() {
        final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        // Hide the "default" notification tone
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        // Hide the "silent" option
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        // Only show notification tones
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);

        // Check the current notification tone
        final String customUri = getSharedPreferences().getString(getKey(), null);
        if (!TextUtils.isEmpty(customUri)) {
            final Uri uri = Uri.parse(customUri);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri);
        }
        ((Activity) getContext()).startActivityForResult(intent, REQUEST_NOTIFICATION_TONE);
    }

    /**
     * @param context The {@link Context} to use
     * @param uri The notification tone uri acquired from
     *            {@link #openRingtonePicker()}
     */
    public static void persistNotificationTone(Context context, Uri uri) {
        final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        final Editor editor = prefs.edit();
        editor.putString(PREFERENCE_KEY_NOTIFICATION_RINGTONE, uri.toString());
        editor.apply();
    }

}
