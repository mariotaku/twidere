package org.mariotaku.twidere.preference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.mariotaku.twidere.R;

public final class WizardPageHeaderPreference extends Preference {

	public WizardPageHeaderPreference(final Context context) {
		this(context, null);
	}

	public WizardPageHeaderPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public WizardPageHeaderPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setLayoutResource(R.layout.wizard_page_header_item);
		setSelectable(false);
	}

	@Override
	protected void onBindView(final View view) {
		super.onBindView(view);
		final TextView title = (TextView) view.findViewById(android.R.id.title);
		final TextView summary = (TextView) view.findViewById(android.R.id.summary);
		title.setText(getTitle());
		summary.setText(getSummary());
	}

}