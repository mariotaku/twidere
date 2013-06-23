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

import java.io.File;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

public final class EnvironmentAccessor {
	/**
	 * Standard directory in which to place any audio files that should be in
	 * the list of alarms that the user can select (not as regular music).
	 **/
	public static String DIRECTORY_ALARMS;
	/**
	 * The traditional location for pictures and videos when mounting the device
	 * as a camera.
	 **/
	public static String DIRECTORY_DCIM;
	/**
	 * Standard directory in which to place files that have been downloaded by
	 * the user.
	 **/
	public static String DIRECTORY_DOWNLOADS;
	/**
	 * Standard directory in which to place movies that are available to the
	 * user.
	 **/
	public static String DIRECTORY_MOVIES;
	/**
	 * Standard directory in which to place any audio files that should be in
	 * the regular list of music for the user.
	 **/
	public static String DIRECTORY_MUSIC;
	/**
	 * Standard directory in which to place any audio files that should be in
	 * the list of notifications that the user can select (not as regular
	 * music).
	 **/
	public static String DIRECTORY_NOTIFICATIONS;
	/**
	 * Standard directory in which to place pictures that are available to the
	 * user.
	 **/
	public static String DIRECTORY_PICTURES;
	/**
	 * Standard directory in which to place any audio files that should be in
	 * the list of podcasts that the user can select (not as regular music).
	 **/
	public static String DIRECTORY_PODCASTS;
	/**
	 * Standard directory in which to place any audio files that should be in
	 * the list of ringtones that the user can select (not as regular music).
	 */
	public static String DIRECTORY_RINGTONES;

	static {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			EnvironmentAccessorSDK8.init();
		} else {
			DIRECTORY_ALARMS = "Alarms";
			DIRECTORY_DCIM = "DCIM";
			DIRECTORY_DOWNLOADS = "Download";
			DIRECTORY_MOVIES = "Movies";
			DIRECTORY_MUSIC = "Music";
			DIRECTORY_NOTIFICATIONS = "Notifications";
			DIRECTORY_PICTURES = "Pictures";
			DIRECTORY_PODCASTS = "Podcasts";
			DIRECTORY_RINGTONES = "Ringtones";
		}
	}

	public static File getExternalCacheDir(final Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
			return EnvironmentAccessorSDK8.getExternalCacheDir(context);
		final File ext_storage_dir = Environment.getExternalStorageDirectory();
		if (ext_storage_dir != null && ext_storage_dir.isDirectory()) {
			final String ext_cache_path = ext_storage_dir.getAbsolutePath() + "/Android/data/"
					+ context.getPackageName() + "/cache/";
			final File ext_cache_dir = new File(ext_cache_path);
			if (ext_cache_dir.isDirectory() || ext_cache_dir.mkdirs()) return ext_cache_dir;
		}
		return null;
	}

	public static File getExternalStoragePublicDirectory(final String type) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
			return EnvironmentAccessorSDK8.getExternalStoragePublicDirectory(type);
		else {
			final File ext_dir = Environment.getExternalStorageDirectory();
			if (ext_dir == null || type == null) return null;
			return new File(ext_dir, type);
		}
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private static class EnvironmentAccessorSDK8 {

		private static File getExternalCacheDir(final Context context) {
			return context.getExternalCacheDir();
		}

		private static File getExternalStoragePublicDirectory(final String type) {
			return Environment.getExternalStoragePublicDirectory(type);
		}

		static void init() {
			DIRECTORY_ALARMS = Environment.DIRECTORY_ALARMS;
			DIRECTORY_DCIM = Environment.DIRECTORY_DCIM;
			DIRECTORY_DOWNLOADS = Environment.DIRECTORY_DOWNLOADS;
			DIRECTORY_MOVIES = Environment.DIRECTORY_MOVIES;
			DIRECTORY_MUSIC = Environment.DIRECTORY_MUSIC;
			DIRECTORY_NOTIFICATIONS = Environment.DIRECTORY_NOTIFICATIONS;
			DIRECTORY_PICTURES = Environment.DIRECTORY_PICTURES;
			DIRECTORY_PODCASTS = Environment.DIRECTORY_PODCASTS;
			DIRECTORY_RINGTONES = Environment.DIRECTORY_RINGTONES;
		}
	}
}
