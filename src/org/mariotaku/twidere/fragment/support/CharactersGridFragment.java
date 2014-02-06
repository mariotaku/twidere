package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.atermenji.android.iconicdroid.IconicFontDrawable;
import com.atermenji.android.iconicdroid.icon.Icon;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.graphic.icon.CharacterIcon;
import org.mariotaku.twidere.util.accessor.ViewAccessor;

public class CharactersGridFragment extends BaseSupportFragment {

	private GridView mGridView;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mGridView.setAdapter(new CharactersAdapter(getActivity(), 0x1F000, 0x1F6FF));
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_characters_grid, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mGridView = (GridView) view.findViewById(android.R.id.list);
	}

	public static class CharactersAdapter extends BaseAdapter {

		private final Context mContext;

		private final LayoutInflater mInflater;

		private final int mCodePointStart, mCodePointEnd;

		private final int mIconColor;

		private final int mIconPadding;

		public CharactersAdapter(final Context context, final int codePointStart, final int codePointEnd) {
			mContext = context;
			mInflater = LayoutInflater.from(context);
			mCodePointStart = codePointStart;
			mCodePointEnd = codePointEnd;
			final TypedArray ta = context.obtainStyledAttributes(new int[] { android.R.attr.colorForeground });
			mIconColor = ta.getColor(0, Color.GRAY);
			mIconPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources()
					.getDisplayMetrics()));
			ta.recycle();
		}

		@Override
		public int getCount() {
			return mCodePointEnd - mCodePointStart;
		}

		@Override
		public final Integer getItem(final int position) {
			return mCodePointStart + position;
		}

		@Override
		public long getItemId(final int position) {
			return position;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = convertView != null ? convertView : mInflater.inflate(R.layout.grid_item_character,
					parent, false);
			final Icon icon = new CharacterIcon(getItem(position));
			final IconicFontDrawable drawable = new IconicFontDrawable(mContext, icon);
			drawable.setIconColor(mIconColor);
			drawable.setIconPadding(mIconPadding);
			ViewAccessor.setBackground(view, drawable);
			return view;
		}

	}
}
