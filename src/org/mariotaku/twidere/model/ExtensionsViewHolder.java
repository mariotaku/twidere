package org.mariotaku.twidere.model;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public final class ExtensionsViewHolder {

	public final ImageView icon;
	public final TextView text1, text2;

	public ExtensionsViewHolder(View view) {

		icon = (ImageView) view.findViewById(android.R.id.icon);
		text1 = (TextView) view.findViewById(android.R.id.text1);
		text2 = (TextView) view.findViewById(android.R.id.text2);
	}
}