package org.mariotaku.twidere.preference;

import android.content.Context;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.text.TwidereHighLightStyle;
import org.mariotaku.twidere.util.Utils;

public class LinkHighlightPreference extends AutoInvalidateListPreference implements Constants {

	private static final int[] ENTRIES_RES = { R.string.none, R.string.highlight, R.string.underline,
			R.string.highlight_and_underline };
	private static final String[] VALUES = { LINK_HIGHLIGHT_OPTION_NONE, LINK_HIGHLIGHT_OPTION_HIGHLIGHT,
			LINK_HIGHLIGHT_OPTION_UNDERLINE, LINK_HIGHLIGHT_OPTION_BOTH };
	private static final int[] OPTIONS = { LINK_HIGHLIGHT_OPTION_CODE_NONE, LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT,
			LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE, LINK_HIGHLIGHT_OPTION_CODE_BOTH };

	public LinkHighlightPreference(final Context context) {
		this(context, null);
	}

	public LinkHighlightPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		final CharSequence[] entries = new CharSequence[VALUES.length];
		for (int i = 0, j = entries.length; i < j; i++) {
			entries[i] = getStyledEntry(OPTIONS[i], context.getString(ENTRIES_RES[i]));
		}
		setEntries(entries);
		setEntryValues(VALUES);
	}

	@Override
	protected void onBindView(final View view) {
		super.onBindView(view);
		final TextView summary = (TextView) view.findViewById(android.R.id.summary);
		summary.setVisibility(View.VISIBLE);
		summary.setText(getStyledEntry(Utils.getLinkHighlightOptionInt(getValue()), getEntry()));
	}

	private static CharSequence getStyledEntry(final int option, final CharSequence entry) {
		final SpannableString str = new SpannableString(entry);
		str.setSpan(new TwidereHighLightStyle(option), 0, str.length(), 0);
		return str;
	}
}
