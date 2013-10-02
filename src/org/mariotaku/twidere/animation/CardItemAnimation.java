
package org.mariotaku.twidere.animation;

import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

public class CardItemAnimation extends AnimationSet {

    public CardItemAnimation() {
        super(false);
        final TranslateAnimation translate = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        addAnimation(translate);
        final Rotate3dAnimation rotate = new Rotate3dAnimation(10, 0, 0.5f, 1, 0, false);
        addAnimation(rotate);
        setDuration(500);
    }

}
