/*
 * Copyright (C) 2012 The Android Open Source Project
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

package org.mariotaku.gallery3d.common;

import java.lang.reflect.Field;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.hardware.Camera;
import android.os.Build;
import android.provider.MediaStore.MediaColumns;
import android.view.View;

public class ApiHelper {
	public static final boolean USE_888_PIXEL_FORMAT = Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

	public static final boolean ENABLE_PHOTO_EDITOR = Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;

	public static final boolean HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE = hasField(View.class,
			"SYSTEM_UI_FLAG_LAYOUT_STABLE");

	public static final boolean HAS_VIEW_SYSTEM_UI_FLAG_HIDE_NAVIGATION = hasField(View.class,
			"SYSTEM_UI_FLAG_HIDE_NAVIGATION");

	public static final boolean HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT = hasField(MediaColumns.class, "WIDTH");

	public static final boolean HAS_REUSING_BITMAP_IN_BITMAP_REGION_DECODER = Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

	public static final boolean HAS_REUSING_BITMAP_IN_BITMAP_FACTORY = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

	public static final boolean HAS_SET_BEAM_PUSH_URIS = Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

	public static final boolean HAS_SET_DEFALT_BUFFER_SIZE = hasMethod("android.graphics.SurfaceTexture",
			"setDefaultBufferSize", int.class, int.class);

	public static final boolean HAS_RELEASE_SURFACE_TEXTURE = hasMethod("android.graphics.SurfaceTexture", "release");

	public static final boolean HAS_SURFACE_TEXTURE = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

	public static final boolean HAS_MTP = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1;

	public static final boolean HAS_AUTO_FOCUS_MOVE_CALLBACK = Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

	public static final boolean HAS_REMOTE_VIEWS_SERVICE = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

	public static final boolean HAS_INTENT_EXTRA_LOCAL_ONLY = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

	public static final boolean HAS_SET_SYSTEM_UI_VISIBILITY = hasMethod(View.class, "setSystemUiVisibility", int.class);

	public static final boolean HAS_FACE_DETECTION;

	static {
		boolean hasFaceDetection = false;
		try {
			final Class<?> listenerClass = Class.forName("android.hardware.Camera$FaceDetectionListener");
			hasFaceDetection = hasMethod(Camera.class, "setFaceDetectionListener", listenerClass)
					&& hasMethod(Camera.class, "startFaceDetection") && hasMethod(Camera.class, "stopFaceDetection")
					&& hasMethod(Camera.Parameters.class, "getMaxNumDetectedFaces");
		} catch (final Throwable t) {
		}
		HAS_FACE_DETECTION = hasFaceDetection;
	}
	public static final boolean HAS_GET_CAMERA_DISABLED = hasMethod(DevicePolicyManager.class, "getCameraDisabled",
			ComponentName.class);

	public static final boolean HAS_MEDIA_ACTION_SOUND = Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

	public static final boolean HAS_OLD_PANORAMA = Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;

	public static final boolean HAS_TIME_LAPSE_RECORDING = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

	public static final boolean HAS_ZOOM_WHEN_RECORDING = Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;

	public static final boolean HAS_CAMERA_FOCUS_AREA = Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;

	public static final boolean HAS_CAMERA_METERING_AREA = Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;

	public static final boolean HAS_FINE_RESOLUTION_QUALITY_LEVELS = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

	public static final boolean HAS_MOTION_EVENT_TRANSFORM = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

	public static final boolean HAS_EFFECTS_RECORDING = false;

	// "Background" filter does not have "context" input port in jelly bean.
	public static final boolean HAS_EFFECTS_RECORDING_CONTEXT_INPUT = Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1;

	public static final boolean HAS_GET_SUPPORTED_VIDEO_SIZE = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

	public static final boolean HAS_SET_ICON_ATTRIBUTE = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

	public static final boolean HAS_MEDIA_PROVIDER_FILES_TABLE = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

	public static final boolean HAS_SURFACE_TEXTURE_RECORDING = Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

	public static final boolean HAS_ACTION_BAR = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

	// Ex: View.setTranslationX.
	public static final boolean HAS_VIEW_TRANSFORM_PROPERTIES = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

	public static final boolean HAS_CAMERA_HDR = Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1;

	public static final boolean HAS_OPTIONS_IN_MUTABLE = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

	public static final boolean CAN_START_PREVIEW_IN_JPEG_CALLBACK = Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;

	public static final boolean HAS_VIEW_PROPERTY_ANIMATOR = Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1;

	public static final boolean HAS_POST_ON_ANIMATION = Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

	public static int getIntFieldIfExists(final Class<?> klass, final String fieldName, final Class<?> obj,
			final int defaultVal) {
		try {
			final Field f = klass.getDeclaredField(fieldName);
			return f.getInt(obj);
		} catch (final Exception e) {
			return defaultVal;
		}
	}

	private static boolean hasField(final Class<?> klass, final String fieldName) {
		try {
			klass.getDeclaredField(fieldName);
			return true;
		} catch (final NoSuchFieldException e) {
			return false;
		}
	}

	private static boolean hasMethod(final Class<?> klass, final String methodName, final Class<?>... paramTypes) {
		try {
			klass.getDeclaredMethod(methodName, paramTypes);
			return true;
		} catch (final NoSuchMethodException e) {
			return false;
		}
	}

	private static boolean hasMethod(final String className, final String methodName, final Class<?>... parameterTypes) {
		try {
			final Class<?> klass = Class.forName(className);
			klass.getDeclaredMethod(methodName, parameterTypes);
			return true;
		} catch (final Throwable th) {
			return false;
		}
	}

	public static interface VERSION_CODES {
		// These value are copied from Build.VERSION_CODES
		public static final int GINGERBREAD_MR1 = 10;
		public static final int HONEYCOMB = 11;
		public static final int HONEYCOMB_MR1 = 12;
		public static final int HONEYCOMB_MR2 = 13;
		public static final int ICE_CREAM_SANDWICH = 14;
		public static final int ICE_CREAM_SANDWICH_MR1 = 15;
		public static final int JELLY_BEAN = 16;
		public static final int JELLY_BEAN_MR1 = 17;
	}
}
