package org.mariotaku.twidere.widget;

import java.util.LinkedHashMap;
import java.util.Map;

import org.mariotaku.twidere.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

public class SeparatedListAdapter extends BaseAdapter {

	private final Map<String, Adapter> sections = new LinkedHashMap<String, Adapter>();
	private final ArrayAdapter<String> headers;

	private final static int TYPE_SECTION_HEADER = 0;

	public SeparatedListAdapter(Context context) {
		headers = new ArrayAdapter<String>(context, R.layout.list_header);
	}

	public void addSection(String section, Adapter adapter) {
		headers.add(section);
		sections.put(section, adapter);
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	@Override
	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for (Adapter adapter : sections.values()) {
			total += adapter.getCount() + 1;
		}
		return total;
	}

	@Override
	public Object getItem(int position) {
		for (Object section : sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) return section;
			if (position < size) return adapter.getItem(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		int type = 1;
		for (Object section : sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

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
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionnum = 0;
		for (Object section : sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) return headers.getView(sectionnum, convertView, parent);
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
		for (Adapter adapter : sections.values()) {
			total += adapter.getViewTypeCount();
		}
		return total;
	}

	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) != TYPE_SECTION_HEADER;
	}

}