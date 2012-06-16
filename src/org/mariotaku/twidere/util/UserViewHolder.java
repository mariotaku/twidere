package org.mariotaku.twidere.util;

import org.mariotaku.twidere.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class UserViewHolder {

	public final ImageView profile_image;
	public final TextView user_name, bio, screen_name;
	public boolean show_as_gap;
	private float text_size;

	public UserViewHolder(View view) {
		profile_image = (ImageView) view.findViewById(R.id.profile_image);
		screen_name = (TextView) view.findViewById(R.id.screen_name);
		user_name = (TextView) view.findViewById(R.id.user_name);
		bio = (TextView) view.findViewById(R.id.bio);

	}

	public void setTextSize(float text_size) {
		if (this.text_size != text_size) {
			this.text_size = text_size;
			// text.setTextSize(text_size);
			// name.setTextSize(text_size * 1.05f);
			// tweet_time.setTextSize(text_size * 0.65f);
			// reply_retweet_status.setTextSize(text_size * 0.65f);
		}
	}

}
