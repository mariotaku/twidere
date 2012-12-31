package org.mariotaku.twidere.util;
import android.content.res.*;
import android.os.*;

public class GetLayoutDirectionAccessor {

	public static int getLayoutDirection(Configuration conf) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			return conf.getLayoutDirection();
		return Configuration.SCREENLAYOUT_LAYOUTDIR_LTR;
	}
}
