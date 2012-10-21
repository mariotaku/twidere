package org.mariotaku.twidere.util;

import org.mariotaku.twidere.R;

import android.util.SparseIntArray;

public class TwitterErrorCodes {

	static final SparseIntArray sErrorCodeMessages = new SparseIntArray();

	static {
		sErrorCodeMessages.put(32, R.string.error_32);
	}

}
