/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import static java.lang.System.currentTimeMillis;

import java.util.List;

import twitter4j.Trends;

public class MemCache {

	private static MemCache sInstance;

	private TrendsCache daily_trends, weekly_trends;

	public void cacheDailyTrends(List<Trends> trends) {
		daily_trends = new TrendsCache(trends);
	}

	public void cacheWeeklyTrends(List<Trends> trends) {
		weekly_trends = new TrendsCache(trends);
	}

	public List<Trends> getCachedDailyTrends() {

		if (daily_trends != null) {
			if (currentTimeMillis() < daily_trends.timestamp + 24 * 60 * 60 * 1000) return daily_trends.trends;
		}
		return null;
	}

	public List<Trends> getCachedWeeklyTrends() {

		if (weekly_trends != null) {
			if (currentTimeMillis() < weekly_trends.timestamp + 24 * 60 * 60 * 1000) return weekly_trends.trends;
		}
		return null;
	}

	public static MemCache getInstance() {
		if (sInstance == null) {
			sInstance = new MemCache();
		}
		return sInstance;
	}

	private class TrendsCache {
		final List<Trends> trends;
		final long timestamp;

		TrendsCache(List<Trends> trends) {
			this.trends = trends;
			timestamp = currentTimeMillis();
		}
	}
}
