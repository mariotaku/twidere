package org.mariotaku.twidere.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Paint;
import android.os.Build;
import android.view.View;

public class MethodsCompat {

	@TargetApi(11)
	public void recreate(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			activity.recreate();
		}
	}

	@TargetApi(11)
	public void setLayerType(View view, int layerType, Paint paint) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			view.setLayerType(layerType, paint);
		}
	}
}
