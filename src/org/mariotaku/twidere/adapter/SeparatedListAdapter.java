package org.mariotaku.twidere.adapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mariotaku.twidere.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

public class SeparatedListAdapter<T extends Adapter> extends BaseAdapter {

	private final Map<String, T> mSections = new LinkedHashMap<String, T>();
	private final ArrayAdapter<String> mHeaders;
	private final static int TYPE_SECTION_HEADER = 0;

	public SeparatedListAdapter(final Context context) {
		mHeaders = new ArrayAdapter<String>(context, R.layout.section_header);
	}

	public void addSection(final String section, final T adapter) {
		mHeaders.add(section);
		mSections.put(section, adapter);
		notifyDataSetChanged();
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	public void clear() {
		mHeaders.clear();
		mSections.clear();
		notifyDataSetChanged();
	}

	public ArrayList<T> getAdapters() {
		return new ArrayList<T>(mSections.values());
	}

	@Override
	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for (final T adapter : mSections.values()) {
			total += adapter.getCount() + 1;
		}
		return total;
	}

	@Override
	public Object getItem(int position) {
		for (final Object section : mSections.keySet()) {
			final Adapter adapter = mSections.get(section);
			final int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) return section;
			if (position < size) return adapter.getItem(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		int type = 1;
		for (final Object section : mSections.keySet()) {
			final Adapter adapter = mSections.get(section);
			final int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) return TYPE_SECTION_HEADER;
			if (position < size) return type + adapter.getItemViewType(position - 1);

			// otherwise jump into next section
			position -= size;
			type += adapter.getViewTypeCount();
		}
		return -1;
	}

	@Override
	public View getView(int position, final View convertView, final ViewGroup parent) {
		int sectionnum = 0;
		for (final Object section : mSections.keySet()) {
			final Adapter adapter = mSections.get(section);
			final int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) return mHeaders.getView(sectionnum, convertView, parent);
			if (position < size) return adapter.getView(position - 1, convertView, parent);

			// otherwise jump into next section
			position -= size;
			sectionnum++;
		}
		return null;
	}

	@Override
	public int getViewTypeCount() {
		// assume that headers count as one, then total all sections
		int total = 1;
		for (final Adapter adapter : mSections.values()) {
			total += adapter.getViewTypeCount();
		}
		return total;
	}

	@Override
	public boolean isEnabled(final int position) {
		return getItemViewType(position) != TYPE_SECTION_HEADER;
	}

	@Override
	public void notifyDataSetChanged() {
		for (final T adapter : mSections.values()) {
			if (adapter instanceof BaseAdapter) {
				((BaseAdapter) adapter).notifyDataSetChanged();
			}
		}
		super.notifyDataSetChanged();
	}

}
