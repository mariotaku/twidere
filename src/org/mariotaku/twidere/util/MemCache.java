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
