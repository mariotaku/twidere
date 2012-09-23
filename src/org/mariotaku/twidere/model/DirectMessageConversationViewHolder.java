/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.model;

import org.mariotaku.twidere.R;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DirectMessageConversationViewHolder {

	public final ImageView profile_image_left, profile_image_right;
	public final TextView name, text, time;
	private float text_size;

	public DirectMessageConversationViewHolder(final View view, final Context context) {
		profile_image_left = (ImageView) view.findViewById(R.id.profile_image_left);
		profile_image_right = (ImageView) view.findViewById(R.id.profile_image_right);
		name = (TextView) view.findViewById(R.id.name);
		text = (TextView) view.findViewById(R.id.text);
		time = (TextView) view.findViewById(R.id.time);

	}

	public void setTextSize(final float text_size) {
		if (this.text_size != text_size) {
			this.text_size = text_size;
			text.setTextSize(text_size);
			name.setTextSize(text_size * 1.05f);
			time.setTextSize(text_size * 0.75f);
		}
	}
}
