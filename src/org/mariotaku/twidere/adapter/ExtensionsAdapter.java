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

package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.loader.ExtensionsListLoader.ExtensionInfo;
import org.mariotaku.twidere.util.PermissionsManager;
import org.mariotaku.twidere.view.holder.TwoLineWithIconViewHolder;

import java.util.List;

public class ExtensionsAdapter extends ArrayAdapter<ExtensionInfo> implements Constants {

    private final PermissionsManager mPermissionsManager;

    public ExtensionsAdapter(final Context context) {
        super(context, R.layout.two_line_with_icon_list_item);
        mPermissionsManager = new PermissionsManager(context);
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        final TwoLineWithIconViewHolder viewholder = view.getTag() == null ? new TwoLineWithIconViewHolder(
                view)
                : (TwoLineWithIconViewHolder) view.getTag();

        final ExtensionInfo info = getItem(position);
        viewholder.checkbox.setVisibility(info.permissions != PERMISSION_INVALID ? View.VISIBLE
                : View.GONE);
        if (info.permissions != PERMISSION_INVALID) {
            viewholder.checkbox.setChecked(info.permissions != PERMISSION_DENIED
                    && mPermissionsManager.checkPermission(info.pname, info.permissions));
        }
        viewholder.text1.setText(info.label);
        viewholder.text2.setVisibility(TextUtils.isEmpty(info.description) ? View.GONE
                : View.VISIBLE);
        viewholder.text2.setText(info.description);
        viewholder.icon.setImageDrawable(info.icon);
        return view;
    }

    public void setData(final List<ExtensionInfo> data) {
        clear();
        if (data != null) {
            addAll(data);
        }
        notifyDataSetChanged();
    }

}
