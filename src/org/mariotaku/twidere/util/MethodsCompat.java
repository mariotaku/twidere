package org.mariotaku.twidere.util;

import java.util.Collection;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Paint;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;

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

	@TargetApi(14)
	public void setUiOptions(Window window, int uiOptions) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			window.setUiOptions(uiOptions);
		}
	}
}
