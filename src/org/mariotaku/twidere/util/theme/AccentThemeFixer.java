package org.mariotaku.twidere.util.theme;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory2;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.content.TwidereContextThemeWrapper;
import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Field;

public class AccentThemeFixer {

	public static boolean fixActionBar(final ActionBar actionBar, final Context baseContext) {
		if (actionBar == null) return false;
		try {
			final Context themedContext = getThemedContext(baseContext);
			// setFieldValue(actionBar, "mContext", themedContext);
			setFieldValue(actionBar, "mThemedContext", themedContext);
			return true;
		} catch (final NoSuchFieldException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean fixWindow(final Activity activity) {
//		try {
//			final LayoutInflater inflater = new DelegatedLayoutInflater(activity);
//			inflater.setFactory2(new FactoryWrapper(activity));
//			setFieldValue(activity.getWindow(), "mLayoutInflater", inflater);
//			return true;
//		} catch (final NoSuchFieldException e) {
//			e.printStackTrace();
//		} catch (final IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (final IllegalArgumentException e) {
//			e.printStackTrace();
//		}
		return false;
	}

	public static Context getThemedContext(final Context baseContext) {
		final TypedValue outValue = new TypedValue();
		final Resources.Theme currentTheme = baseContext.getTheme();
		currentTheme.resolveAttribute(android.R.attr.actionBarWidgetTheme, outValue, true);
		final int targetThemeRes = outValue.resourceId;
		final int baseThemeRes;
		if (baseContext instanceof IThemedActivity) {
			baseThemeRes = ((IThemedActivity) baseContext).getCurrentThemeResourceId();
		} else {
			baseThemeRes = 0;
		}
		if (targetThemeRes != 0 && baseThemeRes != targetThemeRes)
			return new TwidereContextThemeWrapper(baseContext, targetThemeRes, Color.WHITE, true);
		else
			return baseContext;
	}

	private static void setFieldValue(final Object target, final String fieldName, final Object value)
			throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException {
		final Class<?> cls = target.getClass();
		final Field mThemedContextField = cls.getDeclaredField(fieldName);
		mThemedContextField.setAccessible(true);
		mThemedContextField.set(target, value);
	}

	public static class FactoryWrapper implements Factory2 {

		private final Activity activity;

		public FactoryWrapper(final Activity activity) {
			this.activity = activity;
		}

		@Override
		public View onCreateView(final String name, final Context context, final AttributeSet attrs) {
			Log.d("Twidere", String.format("onCreateView %s", name));
			return activity.onCreateView(name, context, attrs);
		}

		@Override
		public View onCreateView(final View parent, final String name, final Context context, final AttributeSet attrs) {
			return null;
		}

	}

	static class DelegatedLayoutInflater extends PhoneLayoutInflater {

		private static final String LOGTAG = "Twidere";

		protected DelegatedLayoutInflater(final Context context) {
			super(context);
		}

		@Override
		public View inflate(final int resource, final ViewGroup root) {
			Log.d(LOGTAG, String.format("inflate, %s", getContext().getResources().getResourceName(resource)));
			return super.inflate(resource, root);
		}

		@Override
		public View inflate(final int resource, final ViewGroup root, final boolean attachToRoot) {
			Log.d(LOGTAG, String.format("inflate, %s", getContext().getResources().getResourceName(resource)));
			return super.inflate(resource, root, attachToRoot);
		}

		@Override
		public View inflate(final XmlPullParser parser, final ViewGroup root) {
			Log.d(LOGTAG, String.format("inflate, %s", parser.getName()));
			return super.inflate(parser, root);
		}

		@Override
		public View inflate(final XmlPullParser parser, final ViewGroup root, final boolean attachToRoot) {
			Log.d(LOGTAG, String.format("inflate, %s", parser.getName()));
			return super.inflate(parser, root, attachToRoot);
		}

	}
}
