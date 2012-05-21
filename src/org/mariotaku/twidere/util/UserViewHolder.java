package org.mariotaku.twidere.util;

import org.mariotaku.twidere.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class UserViewHolder {

	public final ImageView profile_image;
	public final TextView user_name, bio, screen_name;

	public UserViewHolder(View view) {
		profile_image = (ImageView) view.findViewById(R.id.profile_image);
		screen_name = (TextView) view.findViewById(R.id.screen_name);
		user_name = (TextView) view.findViewById(R.id.user_name);
		bio = (TextView) view.findViewById(R.id.bio);

	}

}
