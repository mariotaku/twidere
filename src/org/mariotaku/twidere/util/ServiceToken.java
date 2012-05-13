package org.mariotaku.twidere.util;

import android.content.ContextWrapper;

public class ServiceToken {

	ContextWrapper wrapped_context;

	ServiceToken(ContextWrapper context) {

		wrapped_context = context;
	}
}