package org.mariotaku.twidere.util;

import android.content.ContextWrapper;

public class ServiceToken {

	ContextWrapper mWrappedContext;

	ServiceToken(ContextWrapper context) {

		mWrappedContext = context;
	}
}