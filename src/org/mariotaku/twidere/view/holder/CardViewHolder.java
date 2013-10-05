package org.mariotaku.twidere.view.holder;

import android.view.View;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.animation.CardItemAnimation;

public class CardViewHolder extends ViewHolder {

	public final CardItemAnimation item_animation;
	public final View item_menu;

	public CardViewHolder(final View view) {
		super(view);
		item_animation = new CardItemAnimation();
		item_menu = view.findViewById(R.id.item_menu);
	}

}
