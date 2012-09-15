package org.mariotaku.twidere.util;
import android.webkit.WebView;
import java.util.Map;
import android.os.Build;

public class WebViewLoadUrlAccessor {

	public static void loadUrl(WebView w, String url, Map<String, String> headers) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO || w == null) return;
		w.loadUrl(url, headers);
	}
}
