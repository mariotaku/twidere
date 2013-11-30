package org.mariotaku.twidere.fragment.support;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.mariotaku.twidere.R;

public class SupportWebViewFragment extends Fragment {

	public final WebView getWebView() {
		final View view = getView();
		return (WebView) view.findViewById(R.id.webview);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.webview, container, false);
	}

}
