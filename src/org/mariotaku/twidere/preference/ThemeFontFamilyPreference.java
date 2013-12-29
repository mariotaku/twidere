package org.mariotaku.twidere.preference;

import android.content.Context;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ParseUtils;

public class ThemeFontFamilyPreference extends AutoInvalidateListPreference implements Constants {

	private static final int[] ENTRIES_RES = { R.string.font_family_regular, R.string.font_family_condensed,
			R.string.font_family_light };
	private static final String[] VALUES = { FONT_FAMILY_REGULAR, FONT_FAMILY_CONDENSED, FONT_FAMILY_LIGHT };

	public ThemeFontFamilyPreference(final Context context) {
		this(context, null);
	}

	public ThemeFontFamilyPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			setEnabled(false);
			return;
		}
		final CharSequence[] entries = new CharSequence[VALUES.length];
		for (int i = 0, j = entries.length; i < j; i++) {
			final SpannableString str = new SpannableString(context.getString(ENTRIES_RES[i]));
			str.setSpan(new TypefaceSpan(VALUES[i]), 0, str.length(), 0);
			entries[i] = str;
		}
		setEntries(entries);
		setEntryValues(VALUES);
	}

	@Override
	protected void onBindView(final View view) {
		super.onBindView(view);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;
		final TextView summary = (TextView) view.findViewById(android.R.id.summary);
		summary.setVisibility(View.VISIBLE);
		final String defEntry = getContext().getString(R.string.font_family_regular);
		final SpannableString str = new SpannableString(ParseUtils.parseString(getEntry(), defEntry));
		str.setSpan(new TypefaceSpan(getValue()), 0, str.length(), 0);
		summary.setText(str);
	}

	@Override
	protected void onClick() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;
		super.onClick();
	}
}
