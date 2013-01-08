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

import static android.text.TextUtils.isEmpty;

import org.mariotaku.twidere.R;

import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.ListPreference;
import android.provider.MediaStore.Audio;
import android.util.AttributeSet;

public class RingtonePreference extends ListPreference {

	private int mSelectedItem;
	private final Context mContext;
	private final ContentResolver mResolver;
	private final String[] mEntries, mValues;
	private MediaPlayer mMediaPlayer;

	public RingtonePreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		mResolver = context.getContentResolver();
		final String[] cols = new String[] { Audio.Media.DATA, Audio.Media.TITLE };
		final Cursor cur = mResolver.query(Audio.Media.INTERNAL_CONTENT_URI, cols, Audio.Media.IS_NOTIFICATION + " = "
				+ 1, null, Audio.Media.DEFAULT_SORT_ORDER);
		cur.moveToFirst();
		final int count = cur.getCount();
		mEntries = new String[count + 1];
		mValues = new String[count + 1];
		mEntries[0] = context.getString(R.string.default_ringtone);
		mValues[0] = "";
		final int data_idx = cur.getColumnIndex(Audio.Media.DATA), title_idx = cur.getColumnIndex(Audio.Media.TITLE);
		while (!cur.isAfterLast()) {
			final int pos = cur.getPosition() + 1;
			mEntries[pos] = cur.getString(title_idx);
			mValues[pos] = cur.getString(data_idx);
			cur.moveToNext();
		}
		cur.close();
		setEntries(mEntries);
		setEntryValues(mValues);
	}

	public int getItem() {
		return mSelectedItem;
	}

	public void setItem(final int selected) {
		mSelectedItem = selected;
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		if (positiveResult) {
			callChangeListener(mSelectedItem);
		}
	}

	@Override
	protected void onPrepareDialogBuilder(final Builder builder) {
		builder.setSingleChoiceItems(getEntries(), mSelectedItem, new OnClickListener() {

			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				mSelectedItem = which;
				if (mMediaPlayer != null) {
					if (mMediaPlayer.isPlaying()) {
						mMediaPlayer.stop();
					}
					mMediaPlayer.release();
				}
				mMediaPlayer = new MediaPlayer();
				mMediaPlayer.setLooping(false);
				final String ringtone = mValues[mSelectedItem];
				final Uri def_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				final Uri uri = isEmpty(ringtone) ? def_uri : Uri.parse(ringtone);
				try {
					mMediaPlayer.setDataSource(mContext, uri);
					mMediaPlayer.prepare();
					mMediaPlayer.start();
				} catch (final Exception e) {
				}
			}
		});
	}
}
