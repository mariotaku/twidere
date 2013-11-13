package org.mariotaku.twidere.fragment.support;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class SupportWebViewFragment extends Fragment {
	
	static final int INTERNAL_WEBVIEW_ID = 0x00ff1001;

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = new WebView(getActivity());
		final ViewGroup.LayoutParams lp = container.generateLayoutParams(null);
		lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
		lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
		view.setId(INTERNAL_WEBVIEW_ID);
		view.setLayoutParams(lp);
		return view;
	}

	public final WebView getWebView() {
		final View view = getView();
		return (WebView) view.findViewById(INTERNAL_WEBVIEW_ID);
	}
	
}
