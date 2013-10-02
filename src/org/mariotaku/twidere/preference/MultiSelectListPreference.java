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

package org.mariotaku.twidere.preference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

abstract class MultiSelectListPreference extends DialogPreference implements
        OnMultiChoiceClickListener,
        OnClickListener {

    private final boolean[] mValues, mDefaultValues;
    private SharedPreferences mPreferences;
    private final String[] mNames, mKeys;

    private final Handler mDialogWorkaroundHandler = new Handler() {

        @Override
        public void handleMessage(final Message msg) {
            if (msg.obj instanceof Dialog) {
                final Dialog dialog = (Dialog) msg.obj;
                final View v = dialog.getWindow().getDecorView();
                final ListView lv = findListView(v);
                if (lv != null && lv.getAdapter() != null) {
                    lv.setAdapter(new WrapperAdapter(lv.getAdapter()));
                }
            }
            super.handleMessage(msg);
        }

    };

    protected MultiSelectListPreference(final Context context) {
        this(context, null);
    }

    protected MultiSelectListPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }

    protected MultiSelectListPreference(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
        mNames = getNames();
        mKeys = getKeys();
        mDefaultValues = getDefaults();
        final int length = mNames.length;
        if (length != mKeys.length || length != mDefaultValues.length)
            throw new IllegalArgumentException();
        mValues = new boolean[length];

    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        if (mPreferences == null)
            return;
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                final SharedPreferences.Editor editor = mPreferences.edit();
                final int length = mKeys.length;
                for (int i = 0; i < length; i++) {
                    editor.putBoolean(mKeys[i], mValues[i]);
                }
                editor.commit();
                break;
        }

    }

    @Override
    public void onClick(final DialogInterface dialog, final int which, final boolean isChecked) {
        mValues[which] = isChecked;
    }

    @Override
    public void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        mPreferences = getDefaultSharedPreferences();
        if (mPreferences == null)
            return;
        final int length = mKeys.length;
        for (int i = 0; i < length; i++) {
            mValues[i] = mPreferences.getBoolean(mKeys[i], mDefaultValues[i]);
        }
        builder.setPositiveButton(android.R.string.ok, this);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setMultiChoiceItems(mNames, mValues, this);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.DONUT) {
            new Thread() {
                @Override
                public void run() {
                    Dialog dialog = null;
                    while (dialog == null) {
                        dialog = getDialog();
                        if (dialog != null) {
                            final Message msg = new Message();
                            msg.obj = dialog;
                            mDialogWorkaroundHandler.sendMessage(msg);
                        }
                        try {
                            sleep(50L);
                        } catch (final InterruptedException e) {
                        }
                    }
                }
            }.start();
        }
    }

    protected abstract boolean[] getDefaults();

    protected SharedPreferences getDefaultSharedPreferences() {
        return getSharedPreferences();
    }

    protected abstract String[] getKeys();

    protected abstract String[] getNames();

    private static ListView findListView(final View view) {
        if (!(view instanceof ViewGroup))
            return null;
        if (view instanceof ListView)
            return (ListView) view;
        final ViewGroup view_group = (ViewGroup) view;
        final int child_count = view_group.getChildCount();
        for (int i = 0; i < child_count; i++) {
            final View child = view_group.getChildAt(i);
            if (child instanceof ListView)
                return (ListView) child;
            if (child instanceof ViewGroup) {
                final ListView lv = findListView(child);
                if (lv != null)
                    return lv;
            }
        }
        return null;
    }

    static final class WrapperAdapter implements ListAdapter {

        private final ListAdapter wrapped;

        public WrapperAdapter(final ListAdapter adapter) {
            wrapped = adapter;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return wrapped.areAllItemsEnabled();
        }

        @Override
        public int getCount() {
            return wrapped.getCount();
        }

        @Override
        public Object getItem(final int position) {
            return wrapped.getItem(position);
        }

        @Override
        public long getItemId(final int position) {
            return wrapped.getItemId(position);
        }

        @Override
        public int getItemViewType(final int position) {
            return wrapped.getItemViewType(position);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final View view = wrapped.getView(position, convertView, parent);
            final TextView tv = findTextView(view);
            if (tv != null) {
                tv.setTextColor(Color.BLACK);
            }
            return view;
        }

        @Override
        public int getViewTypeCount() {
            return wrapped.getViewTypeCount();
        }

        @Override
        public boolean hasStableIds() {
            return wrapped.hasStableIds();
        }

        @Override
        public boolean isEmpty() {
            return wrapped.isEmpty();
        }

        @Override
        public boolean isEnabled(final int position) {
            return wrapped.isEnabled(position);
        }

        @Override
        public void registerDataSetObserver(final DataSetObserver observer) {
            wrapped.registerDataSetObserver(observer);

        }

        @Override
        public void unregisterDataSetObserver(final DataSetObserver observer) {
            wrapped.unregisterDataSetObserver(observer);

        }

        private TextView findTextView(final View view) {
            if (view instanceof TextView)
                return (TextView) view;
            if (!(view instanceof ViewGroup))
                return null;
            final ViewGroup view_group = (ViewGroup) view;
            final int child_count = view_group.getChildCount();
            for (int i = 0; i < child_count; i++) {
                final View child = view_group.getChildAt(i);
                if (child instanceof TextView)
                    return (TextView) child;
                if (child instanceof ViewGroup) {
                    final TextView tv = findTextView(child);
                    if (tv != null)
                        return tv;
                }
            }
            return null;
        }

    }

}
