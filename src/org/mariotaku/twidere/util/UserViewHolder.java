package org.mariotaku.twidere.util;

import org.mariotaku.twidere.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class UserViewHolder {

	public final ImageView profile_image;
	public final TextView location, description, name;
	private final View content, gap_indicator;
	public boolean show_as_gap;
	private float text_size;

	public UserViewHolder(View view) {
		content = view;
		gap_indicator = view.findViewById(R.id.list_gap_text);
		profile_image = (ImageView) view.findViewById(R.id.profile_image);
		name = (TextView) view.findViewById(R.id.name);
		location = (TextView) view.findViewById(R.id.location);
		description = (TextView) view.findViewById(R.id.description);

	}

	public void setShowAsGap(boolean show_gap) {
		show_as_gap = show_gap;
		content.setBackgroundResource(show_gap ? R.drawable.ic_list_gap : 0);
		profile_image.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		name.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		location.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		description.setVisibility(show_gap ? View.GONE : View.VISIBLE);
		gap_indicator.setVisibility(!show_gap ? View.GONE : View.VISIBLE);
	}

	public void setTextSize(float text_size) {
		if (this.text_size != text_size) {
			this.text_size = text_size;
			description.setTextSize(text_size);
			name.setTextSize(text_size * 1.05f);
			location.setTextSize(text_size * 0.65f);
		}
	}

}
