package org.mariotaku.twidere.text;

import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;

public class TwidereURLSpan extends URLSpan implements Constants {

	private final int type, highlight_option;
	private final long account_id;
	private final String url, orig;
	private final boolean sensitive;
	private final OnLinkClickListener listener;

	public TwidereURLSpan(final String url, final long account_id, final int type, final boolean sensitive,
			final OnLinkClickListener listener, final int highlight_style) {
		this(url, null, account_id, type, sensitive, listener, highlight_style);
	}

	public TwidereURLSpan(final String url, final String orig, final long account_id, final int type,
			final boolean sensitive, final OnLinkClickListener listener, final int highlight_option) {
		super(url);
		this.url = url;
		this.orig = orig;
		this.account_id = account_id;
		this.type = type;
		this.sensitive = sensitive;
		this.listener = listener;
		this.highlight_option = highlight_option;
	}

	@Override
	public void onClick(final View widget) {
		if (listener != null) {
			listener.onLinkClick(url, orig, account_id, type, sensitive);
		}
	}

	@Override
	public void updateDrawState(final TextPaint ds) {
		switch (highlight_option) {
			case LINK_HIGHLIGHT_OPTION_CODE_BOTH:
				ds.setUnderlineText(true);
				ds.setColor(ds.linkColor);
				break;
			case LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE:
				ds.setUnderlineText(true);
				break;
			case LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT:
				ds.setColor(ds.linkColor);
				break;
			default:
				break;
		}
	}
}