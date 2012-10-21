/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.ftp.parser;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.net.ftp.Configurable;
import org.apache.commons.net.ftp.FTPClientConfig;

/**
 * Default implementation of the {@link FTPTimestampParser FTPTimestampParser}
 * interface also implements the {@link org.apache.commons.net.ftp.Configurable
 * Configurable} interface to allow the parsing to be configured from the
 * outside.
 * 
 * @see ConfigurableFTPFileEntryParserImpl
 * @since 1.4
 */
public class FTPTimestampParserImpl implements FTPTimestampParser, Configurable {

	private SimpleDateFormat defaultDateFormat;
	private SimpleDateFormat recentDateFormat;
	private boolean lenientFutureDates = false;

	/**
	 * The only constructor for this class.
	 */
	public FTPTimestampParserImpl() {
		setDefaultDateFormat(DEFAULT_SDF);
		setRecentDateFormat(DEFAULT_RECENT_SDF);
	}

	/**
	 * Implementation of the {@link Configurable Configurable} interface.
	 * Configures this <code>FTPTimestampParser</code> according to the
	 * following logic:
	 * <p>
	 * Set up the
	 * {@link FTPClientConfig#setDefaultDateFormatStr(java.lang.String)
	 * defaultDateFormat} and optionally the
	 * {@link FTPClientConfig#setRecentDateFormatStr(String) recentDateFormat}
	 * to values supplied in the config based on month names configured as
	 * follows:
	 * </p>
	 * <p>
	 * <ul>
	 * <li>If a {@link FTPClientConfig#setShortMonthNames(String)
	 * shortMonthString} has been supplied in the <code>config</code>, use that
	 * to parse parse timestamps.</li>
	 * <li>Otherwise, if a {@link FTPClientConfig#setServerLanguageCode(String)
	 * serverLanguageCode} has been supplied in the <code>config</code>, use the
	 * month names represented by that
	 * {@link FTPClientConfig#lookupDateFormatSymbols(String) language} to parse
	 * timestamps.</li>
	 * <li>otherwise use default English month names</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Finally if a
	 * {@link org.apache.commons.net.ftp.FTPClientConfig#setServerTimeZoneId(String)
	 * serverTimeZoneId} has been supplied via the config, set that into all
	 * date formats that have been configured.
	 * </p>
	 */
	@Override
	public void configure(final FTPClientConfig config) {
		DateFormatSymbols dfs = null;

		final String languageCode = config.getServerLanguageCode();
		final String shortmonths = config.getShortMonthNames();
		if (shortmonths != null) {
			dfs = FTPClientConfig.getDateFormatSymbols(shortmonths);
		} else if (languageCode != null) {
			dfs = FTPClientConfig.lookupDateFormatSymbols(languageCode);
		} else {
			dfs = FTPClientConfig.lookupDateFormatSymbols("en");
		}

		final String recentFormatString = config.getRecentDateFormatStr();
		if (recentFormatString == null) {
			recentDateFormat = null;
		} else {
			recentDateFormat = new SimpleDateFormat(recentFormatString, dfs);
			recentDateFormat.setLenient(false);
		}

		final String defaultFormatString = config.getDefaultDateFormatStr();
		if (defaultFormatString == null) throw new IllegalArgumentException("defaultFormatString cannot be null");
		defaultDateFormat = new SimpleDateFormat(defaultFormatString, dfs);
		defaultDateFormat.setLenient(false);

		setServerTimeZone(config.getServerTimeZoneId());

		lenientFutureDates = config.isLenientFutureDates();
	}

	/**
	 * @return Returns the defaultDateFormat.
	 */
	public SimpleDateFormat getDefaultDateFormat() {
		return defaultDateFormat;
	}

	/**
	 * @return Returns the defaultDateFormat pattern string.
	 */
	public String getDefaultDateFormatString() {
		return defaultDateFormat.toPattern();
	}

	/**
	 * @return Returns the recentDateFormat.
	 */
	public SimpleDateFormat getRecentDateFormat() {
		return recentDateFormat;
	}

	/**
	 * @return Returns the recentDateFormat.
	 */
	public String getRecentDateFormatString() {
		return recentDateFormat.toPattern();
	}

	/**
	 * @return Returns the serverTimeZone used by this parser.
	 */
	public TimeZone getServerTimeZone() {
		return defaultDateFormat.getTimeZone();
	}

	/**
	 * @return returns an array of 12 strings representing the short month names
	 *         used by this parse.
	 */
	public String[] getShortMonths() {
		return defaultDateFormat.getDateFormatSymbols().getShortMonths();
	}

	/**
	 * Implements the one {@link FTPTimestampParser#parseTimestamp(String)
	 * method} in the {@link FTPTimestampParser FTPTimestampParser} interface
	 * according to this algorithm:
	 * 
	 * If the recentDateFormat member has been defined, try to parse the
	 * supplied string with that. If that parse fails, or if the
	 * recentDateFormat member has not been defined, attempt to parse with the
	 * defaultDateFormat member. If that fails, throw a ParseException.
	 * 
	 * This method allows a {@link Calendar} instance to be passed in which
	 * represents the current (system) time.
	 * 
	 * @see org.apache.commons.net.ftp.parser.FTPTimestampParser#parseTimestamp(java.lang.String)
	 * 
	 * @param timestampStr The timestamp to be parsed
	 */
	@Override
	public Calendar parseTimestamp(final String timestampStr) throws ParseException {
		final Calendar now = Calendar.getInstance();
		return parseTimestamp(timestampStr, now);
	}

	/**
	 * Implements the one {@link FTPTimestampParser#parseTimestamp(String)
	 * method} in the {@link FTPTimestampParser FTPTimestampParser} interface
	 * according to this algorithm:
	 * 
	 * If the recentDateFormat member has been defined, try to parse the
	 * supplied string with that. If that parse fails, or if the
	 * recentDateFormat member has not been defined, attempt to parse with the
	 * defaultDateFormat member. If that fails, throw a ParseException.
	 * 
	 * @see org.apache.commons.net.ftp.parser.FTPTimestampParser#parseTimestamp(java.lang.String)
	 * @param timestampStr The timestamp to be parsed
	 * @param serverTime The current time for the server
	 * @since 1.5
	 */
	public Calendar parseTimestamp(final String timestampStr, final Calendar serverTime) throws ParseException {
		final Calendar now = (Calendar) serverTime.clone();// Copy this, because
															// we may change it
		now.setTimeZone(getServerTimeZone());
		final Calendar working = (Calendar) now.clone();
		working.setTimeZone(getServerTimeZone()); // is this needed?
		ParsePosition pp = new ParsePosition(0);

		Date parsed = null;
		if (recentDateFormat != null) {
			if (lenientFutureDates) {
				// add a day to "now" so that "slop" doesn't cause a date
				// slightly in the future to roll back a full year. (Bug 35181
				// => NET-83)
				now.add(Calendar.DATE, 1);
			}
			parsed = recentDateFormat.parse(timestampStr, pp);
		}
		if (parsed != null && pp.getIndex() == timestampStr.length()) {
			working.setTime(parsed);
			working.set(Calendar.YEAR, now.get(Calendar.YEAR));

			if (working.after(now)) {
				working.add(Calendar.YEAR, -1);
			}
		} else {
			// Temporarily add the current year to the short date time
			// to cope with short-date leap year strings.
			// e.g. Java's DateFormatter will assume that "Feb 29 12:00" refers
			// to
			// Feb 29 1970 (an invalid date) rather than a potentially valid
			// leap year date.
			// This is pretty bad hack to work around the deficiencies of the
			// JDK date/time classes.
			if (recentDateFormat != null) {
				pp = new ParsePosition(0);
				final int year = now.get(Calendar.YEAR);
				final String timeStampStrPlusYear = timestampStr + " " + year;
				final SimpleDateFormat hackFormatter = new SimpleDateFormat(recentDateFormat.toPattern() + " yyyy",
						recentDateFormat.getDateFormatSymbols());
				hackFormatter.setLenient(false);
				hackFormatter.setTimeZone(recentDateFormat.getTimeZone());
				parsed = hackFormatter.parse(timeStampStrPlusYear, pp);
			}
			if (parsed != null && pp.getIndex() == timestampStr.length() + 5) {
				working.setTime(parsed);
			} else {
				pp = new ParsePosition(0);
				parsed = defaultDateFormat.parse(timestampStr, pp);
				// note, length checks are mandatory for us since
				// SimpleDateFormat methods will succeed if less than
				// full string is matched. They will also accept,
				// despite "leniency" setting, a two-digit number as
				// a valid year (e.g. 22:04 will parse as 22 A.D.)
				// so could mistakenly confuse an hour with a year,
				// if we don't insist on full length parsing.
				if (parsed != null && pp.getIndex() == timestampStr.length()) {
					working.setTime(parsed);
				} else
					throw new ParseException("Timestamp could not be parsed with older or recent DateFormat",
							pp.getErrorIndex());
			}
		}
		return working;
	}

	/**
	 * @param defaultDateFormat The defaultDateFormat to be set.
	 */
	private void setDefaultDateFormat(final String format) {
		if (format != null) {
			defaultDateFormat = new SimpleDateFormat(format);
			defaultDateFormat.setLenient(false);
		}
	}

	/**
	 * @param recentDateFormat The recentDateFormat to set.
	 */
	private void setRecentDateFormat(final String format) {
		if (format != null) {
			recentDateFormat = new SimpleDateFormat(format);
			recentDateFormat.setLenient(false);
		}
	}

	/**
	 * sets a TimeZone represented by the supplied ID string into all of the
	 * parsers used by this server.
	 * 
	 * @param serverTimeZone Time Id java.util.TimeZone id used by the ftp
	 *            server. If null the client's local time zone is assumed.
	 */
	private void setServerTimeZone(final String serverTimeZoneId) {
		TimeZone serverTimeZone = TimeZone.getDefault();
		if (serverTimeZoneId != null) {
			serverTimeZone = TimeZone.getTimeZone(serverTimeZoneId);
		}
		defaultDateFormat.setTimeZone(serverTimeZone);
		if (recentDateFormat != null) {
			recentDateFormat.setTimeZone(serverTimeZone);
		}
	}

	/**
	 * @return Returns the lenientFutureDates.
	 */
	boolean isLenientFutureDates() {
		return lenientFutureDates;
	}

	/**
	 * @param lenientFutureDates The lenientFutureDates to set.
	 */
	void setLenientFutureDates(final boolean lenientFutureDates) {
		this.lenientFutureDates = lenientFutureDates;
	}
}
