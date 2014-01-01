/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import android.content.Context;
import android.util.SparseIntArray;

import org.mariotaku.twidere.R;

public class TwitterErrorCodes {

	public static final int PAGE_NOT_FOUND = 34;
	public static final int RATE_LIMIT_EXCEDDED = 88;
	public static final int NOT_AUTHORIZED = 179;
	public static final int STATUS_IS_DUPLICATE = 187;

	private static final SparseIntArray ERROR_CODE_MESSAGES = new SparseIntArray();

	static {
		ERROR_CODE_MESSAGES.put(32, R.string.error_32);
		ERROR_CODE_MESSAGES.put(PAGE_NOT_FOUND, R.string.error_34);
		ERROR_CODE_MESSAGES.put(RATE_LIMIT_EXCEDDED, R.string.error_88);
		ERROR_CODE_MESSAGES.put(89, R.string.error_89);
		ERROR_CODE_MESSAGES.put(64, R.string.error_64);
		ERROR_CODE_MESSAGES.put(130, R.string.error_130);
		ERROR_CODE_MESSAGES.put(131, R.string.error_131);
		ERROR_CODE_MESSAGES.put(135, R.string.error_135);
		ERROR_CODE_MESSAGES.put(162, R.string.error_162);
		ERROR_CODE_MESSAGES.put(172, R.string.error_172);
		ERROR_CODE_MESSAGES.put(NOT_AUTHORIZED, R.string.error_179);
		ERROR_CODE_MESSAGES.put(STATUS_IS_DUPLICATE, R.string.error_187);
		ERROR_CODE_MESSAGES.put(193, R.string.error_193);
		ERROR_CODE_MESSAGES.put(215, R.string.error_215);
	}

	public static String getErrorMessage(final Context context, final int error_code) {
		if (context == null) return null;
		final int res_id = ERROR_CODE_MESSAGES.get(error_code, -1);
		if (res_id > 0) return context.getString(res_id);
		return null;
	}

}
