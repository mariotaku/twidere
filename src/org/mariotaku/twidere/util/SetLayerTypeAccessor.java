package org.mariotaku.twidere.util;

import android.annotation.TargetApi;
import android.graphics.Paint;
import android.view.View;

public class SetLayerTypeAccessor {

	@TargetApi(11)
	public static void setLayerType(View view, int layerType, Paint paint) {
		view.setLayerType(layerType, paint);
	}
}