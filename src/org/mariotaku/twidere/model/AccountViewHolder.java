package org.mariotaku.twidere.model;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class AccountViewHolder {

	public final ImageView profile_image;
	public final TextView name, screen_name;
	public final CheckBox checkbox;
	private final ColorLabelRelativeLayout content;
	private final View default_indicator;

	public AccountViewHolder(final View view) {
		content = (ColorLabelRelativeLayout) view;
		name = (TextView) view.findViewById(android.R.id.text1);
		screen_name = (TextView) view.findViewById(android.R.id.text2);
		profile_image = (ImageView) view.findViewById(android.R.id.icon);
		default_indicator = view.findViewById(R.id.default_indicator);
		checkbox = (CheckBox) view.findViewById(R.id.checkbox);
	}

	public void setAccountColor(final int color) {
		content.drawRight(color);
	}

	public void setIsDefault(final boolean is_default) {
		default_indicator.setVisibility(is_default ? View.VISIBLE : View.GONE);
	}
}