package org.mariotaku.twidere.util;

import java.io.File;

import android.annotation.TargetApi;
import android.content.Context;

public class GetExternalCacheDirAccessor {

	@TargetApi(8)
	public static File getExternalCacheDir(Context context) {
		return context.getExternalCacheDir();
	}
}