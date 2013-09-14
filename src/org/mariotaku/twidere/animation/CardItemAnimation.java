package org.mariotaku.twidere.animation;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class CardItemAnimation extends TranslateAnimation {

	public CardItemAnimation() {
		super(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		setDuration(500);
	}

}
