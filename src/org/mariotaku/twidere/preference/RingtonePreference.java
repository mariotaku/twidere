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

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.util.ArrayUtils;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class RingtonePreference extends ListPreference {

	private List<Ringtone> mRingtones;
	private String[] mEntries, mValues;

	private int mSelectedItem;

	public RingtonePreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public int getSelectedItem() {
		return mSelectedItem;
	}

	public Ringtone getSelectedRingtone() {
		return mRingtones.get(mSelectedItem);
	}

	public void setSelectedItem(final int selected) {
		mSelectedItem = selected >= 0 && selected < mValues.length ? selected : 0;
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		final Ringtone ringtone = getSelectedRingtone();
		if (ringtone != null && ringtone.isPlaying()) {
			ringtone.stop();
		}
		if (positiveResult && mSelectedItem >= 0 && mSelectedItem < mValues.length) {
			if (callChangeListener(mValues[mSelectedItem])) {
				persistString(mValues[mSelectedItem]);
			}
		}
	}

	@Override
	protected void onPrepareDialogBuilder(final Builder builder) {
		loadRingtones(getContext());
		setSelectedItem(ArrayUtils.indexOf(mValues, getPersistedString(null)));
		builder.setSingleChoiceItems(getEntries(), getSelectedItem(), new OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				setSelectedItem(which);
				final Ringtone ringtone = getSelectedRingtone();
				if (ringtone.isPlaying()) {
					ringtone.stop();
				}
				ringtone.play();
			}
		});
	}
	
	private void loadRingtones(final Context context) {
		final RingtoneManager manager = new RingtoneManager(context);
		manager.setType(RingtoneManager.TYPE_NOTIFICATION);
		final Cursor cur = manager.getCursor();
		cur.moveToFirst();
		final int count = cur.getCount();
		mRingtones = new ArrayList<Ringtone>(count);
		mEntries = new String[count];
		mValues = new String[count];
		for (int i = 0; i < count; i++) {
			final Ringtone ringtone = manager.getRingtone(i);
			mRingtones.add(ringtone);
			mEntries[i] = ringtone.getTitle(context);
			mValues[i] = manager.getRingtoneUri(i).toString();
		}
		setEntries(mEntries);
		setEntryValues(mValues);
		cur.close();
	}

	// static final class RingtoneNameComparator implements Comparator<Ringtone>
	// {
	//
	// private final Context context;
	//
	// RingtoneNameComparator(final Context context) {
	// this.context = context;
	// }
	//
	// @Override
	// public int compare(final Ringtone value1, final Ringtone value2) {
	// if (value1 == null || value2 == null) return 0;
	// return
	// value1.getTitle(context).compareToIgnoreCase(value2.getTitle(context));
	// }
	// }
}
