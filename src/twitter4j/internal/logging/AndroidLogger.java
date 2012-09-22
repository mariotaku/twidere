/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j.internal.logging;

import android.util.Log;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.1
 */
final class AndroidLogger extends Logger {

	private static final String LOGTAG = "twitter4j";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(String message) {
		if (isDebugEnabled()) {
			Log.d(LOGTAG, message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(String message, String message2) {
		if (isDebugEnabled()) {
			Log.d(LOGTAG, message + message2);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String message) {
		if (isErrorEnabled()) {
			Log.e(LOGTAG, message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String message, Throwable th) {
		if (isErrorEnabled()) {
			Log.e(LOGTAG, message, th);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(String message) {
		if (isInfoEnabled()) {
			Log.i(LOGTAG, message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(String message, String message2) {
		if (isInfoEnabled()) {
			Log.i(LOGTAG, message + message2);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDebugEnabled() {
		return false;
		// return Log.isLoggable(LOGTAG, Log.DEBUG);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isErrorEnabled() {
		return Log.isLoggable(LOGTAG, Log.ERROR);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInfoEnabled() {
		return false;
		// return Log.isLoggable(LOGTAG, Log.INFO);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWarnEnabled() {
		return Log.isLoggable(LOGTAG, Log.WARN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(String message) {
		if (isWarnEnabled()) {
			Log.w(LOGTAG, message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(String message, String message2) {
		if (isWarnEnabled()) {
			Log.w(LOGTAG, message + message2);
		}
	}
}
