package org.mariotaku.twidere.view.holder;

import android.view.View;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.animation.CardItemAnimation;
import org.mariotaku.twidere.view.iface.ICardItemView;

public class CardViewHolder extends ViewHolder {

	public final CardItemAnimation item_animation;
	public final ICardItemView content;

	public CardViewHolder(final View view) {
		super(view);
		content = (ICardItemView) view.findViewById(R.id.content);
		item_animation = new CardItemAnimation();
	}

}
