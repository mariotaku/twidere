package org.mariotaku.twidere.adapter;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ExtensionsViewHolder;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ExtensionsAdapter extends BaseAdapter {

	private final PackageManager pm;
	private final Context context;

	private final List<ResolveInfo> mData = new ArrayList<ResolveInfo>();

	public ExtensionsAdapter(final Context context, final PackageManager pm) {
		this.pm = pm;
		this.context = context;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public ResolveInfo getItem(final int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return getItem(position).hashCode();
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = convertView != null ? convertView : LayoutInflater.from(context).inflate(
				R.layout.two_line_with_icon_list_item, parent, false);
		view.findViewById(R.id.checkbox).setVisibility(View.GONE);
		final ExtensionsViewHolder viewholder = view.getTag() == null ? new ExtensionsViewHolder(view)
				: (ExtensionsViewHolder) view.getTag();

		final ResolveInfo info = getItem(position);
		viewholder.text1.setText(info.loadLabel(pm));
		viewholder.text2.setText(info.activityInfo.applicationInfo.loadLabel(pm));
		viewholder.icon.setImageDrawable(info.loadIcon(pm));
		return view;
	}

	public void setData(final List<ResolveInfo> data) {
		mData.clear();
		if (data != null) {
			mData.addAll(data);
		}
		notifyDataSetChanged();
	}

}
