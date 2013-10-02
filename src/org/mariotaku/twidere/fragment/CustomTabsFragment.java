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

package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getTabIconDrawable;
import static org.mariotaku.twidere.util.Utils.getTabTypeName;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.graphic.DropShadowDrawable;
import org.mariotaku.twidere.model.CustomTabConfiguration;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.provider.TweetStore.Tabs;

import java.util.HashMap;
import java.util.Map.Entry;

public class CustomTabsFragment extends BaseListFragment implements LoaderCallbacks<Cursor>,
        OnItemLongClickListener,
        OnItemClickListener, Panes.Right {

    private ContentResolver mResolver;

    private ListView mListView;

    private PopupMenu mPopupMenu;

    private CustomTabsAdapter mAdapter;

    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (getActivity() == null || !isAdded() || isDetached())
                return;
            final String action = intent.getAction();
            if (BROADCAST_TABS_UPDATED.equals(action)) {
                getLoaderManager().restartLoader(0, null, CustomTabsFragment.this);
            }
        }

    };

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        mResolver = getContentResolver();
        final Context context = getActivity();
        mAdapter = new CustomTabsAdapter(context);
        setListAdapter(mAdapter);
        mListView = getListView();
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_TAB: {
                if (resultCode == Activity.RESULT_OK) {
                    final ContentValues values = new ContentValues();
                    values.put(Tabs.ARGUMENTS, data.getStringExtra(INTENT_KEY_ARGUMENTS));
                    values.put(Tabs.NAME, data.getStringExtra(INTENT_KEY_NAME));
                    values.put(Tabs.TYPE, data.getStringExtra(INTENT_KEY_TYPE));
                    values.put(Tabs.ICON, data.getStringExtra(INTENT_KEY_ICON));
                    values.put(Tabs.POSITION, mAdapter.getCount());
                    mResolver.insert(Tabs.CONTENT_URI, values);
                    getLoaderManager().restartLoader(0, null, this);
                }
                break;
            }
            case REQUEST_EDIT_TAB: {
                if (resultCode == Activity.RESULT_OK) {
                    final Bundle extras = data.getExtras();
                    if (extras == null) {
                        break;
                    }
                    final ContentValues values = new ContentValues();
                    values.put(Tabs.ARGUMENTS, extras.getString(INTENT_KEY_ARGUMENTS));
                    values.put(Tabs.NAME, extras.getString(INTENT_KEY_NAME));
                    values.put(Tabs.TYPE, extras.getString(INTENT_KEY_TYPE));
                    values.put(Tabs.ICON, extras.getString(INTENT_KEY_ICON));
                    // mResolver.update(Tabs.CONTENT_URI, values, Tabs._ID +
                    // " = " + c.getId(), null);
                    getLoaderManager().restartLoader(0, null, this);
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        return new CursorLoader(getActivity(), Tabs.CONTENT_URI, Tabs.COLUMNS, null, null,
                Tabs.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_custom_tabs, menu);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position,
            final long id) {
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, final View view,
            final int position, final long id) {
        return true;
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            default: {
                final Intent intent = item.getIntent();
                if (intent == null)
                    return false;
                startActivity(intent);
                return true;
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        final Resources res = getResources();
        final MenuItem itemAdd = menu.findItem(R.id.add_submenu);
        if (itemAdd != null && itemAdd.hasSubMenu()) {
            final SubMenu subMenu = itemAdd.getSubMenu();
            subMenu.clear();
            final HashMap<String, CustomTabConfiguration> map = CustomTabConfiguration
                    .getConfiguraionMap();
            for (final Entry<String, CustomTabConfiguration> entry : map.entrySet()) {
                final String type = entry.getKey();
                final CustomTabConfiguration conf = entry.getValue();
                final Intent intent = new Intent(INTENT_ACTION_EDIT_CUSTOM_TAB);
                intent.putExtra(INTENT_KEY_TYPE, type);
                final MenuItem subItem = subMenu.add(conf.getDefaultTitle());
                subItem.setIcon(new DropShadowDrawable(res, res.getDrawable(conf.getDefaultIcon()),
                        2, 0x80000000));
                subItem.setIntent(intent);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(BROADCAST_TABS_UPDATED);
        registerReceiver(mStateReceiver, filter);
    }

    @Override
    public void onStop() {
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
        unregisterReceiver(mStateReceiver);
        super.onStop();
    }

    public static class CustomTabsAdapter extends SimpleCursorAdapter implements OnClickListener {

        private final Context mContext;

        private CursorIndices mIndices;

        public CustomTabsAdapter(final Context context) {
            super(context, R.layout.two_line_with_icon_list_item, null, new String[] {
                Tabs.NAME
            },
                    new int[] {
                        android.R.id.text1
                    }, 0);
            mContext = context;
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            super.bindView(view, context, cursor);
            final CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
            checkbox.setVisibility(View.VISIBLE);
            checkbox.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_menu_refresh, 0);
            checkbox.setOnClickListener(this);
            final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
            final String type = cursor.getString(mIndices.type);
            text2.setText(getTabTypeName(context, type));
            final Drawable d = getTabIconDrawable(mContext,
                    CustomTabConfiguration.getTabIconObject(type));
            if (d != null) {
                icon.setImageDrawable(new DropShadowDrawable(context.getResources(), d, 2,
                        0x80000000));
            } else {
                icon.setImageResource(R.drawable.ic_tab_list);
            }
        }

        @Override
        public void changeCursor(final Cursor cursor) {
            super.changeCursor(cursor);
            if (cursor != null) {
                mIndices = new CursorIndices(cursor);
            }
        }

        @Override
        public void onClick(final View view) {
            // TODO Auto-generated method stub

        }

        static class CursorIndices {
            final int _id, name, icon, type, arguments;

            CursorIndices(final Cursor mCursor) {
                _id = mCursor.getColumnIndex(Tabs._ID);
                icon = mCursor.getColumnIndex(Tabs.ICON);
                name = mCursor.getColumnIndex(Tabs.NAME);
                type = mCursor.getColumnIndex(Tabs.TYPE);
                arguments = mCursor.getColumnIndex(Tabs.ARGUMENTS);
            }
        }

    }

}
