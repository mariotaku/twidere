package org.mariotaku.twidere.adapter;

import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ListAction;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListActionAdapter extends ArrayAdapter<ListAction> {

	public ListActionAdapter(Context context) {
		super(context, R.layout.list_action_item, android.R.id.text1);
	}

	public ListAction findItem(long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (id == getItemId(i)) return getItem(i);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final TextView summary_view = (TextView) view.findViewById(android.R.id.text2);
		final String summary = getItem(position).getSummary();
		summary_view.setText(summary);
		summary_view.setVisibility(!isNullOrEmpty(summary) ? View.VISIBLE : View.GONE);
		return view;
	}
}
