package org.mariotaku.twidere.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.http.HttpHost;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

/**
 * Utility class for setting WebKit proxy used by Android WebView
 */
public class WebViewProxySettings {

	public static void resetProxy(WebView webView) {
		final Context context = webView.getContext();
		try {
			final Object requestQueueObject = getRequestQueue(context);
			if (requestQueueObject != null) {
				setDeclaredField(requestQueueObject, "mProxyHost", null);
			}
		} catch (final Exception e) {
			Log.e("ProxySettings", "Exception resetting WebKit proxy: " + e.toString());
		}
	}

	public static boolean setProxy(WebView webView, String host, int port) {
		final Context context = webView.getContext();
		// PSIPHON: added support for Android 4.x WebView proxy
		try {
			final Class<?> webViewCoreClass = Class.forName("android.webkit.WebViewCore");
			final Class<?> proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
			if (webViewCoreClass != null && proxyPropertiesClass != null) {
				final Method m = webViewCoreClass.getDeclaredMethod("sendStaticMessage", Integer.TYPE, Object.class);
				final Constructor<?> c = proxyPropertiesClass.getConstructor(String.class, Integer.TYPE, String.class);

				if (m != null && c != null) {
					m.setAccessible(true);
					c.setAccessible(true);
					final Object properties = c.newInstance(host, port, null);

					// android.webkit.WebViewCore.EventHub.PROXY_CHANGED = 193;
					m.invoke(null, 193, properties);
					return true;
				}
			}
		} catch (final Exception e) {
			Log.e("ProxySettings",
					"Exception setting WebKit proxy through android.net.ProxyProperties: " + e.toString());
		}

		try {
			final Object requestQueueObject = getRequestQueue(context);
			if (requestQueueObject != null) {
				// Create Proxy config object and set it into request Q
				final HttpHost httpHost = new HttpHost(host, port, "http");
				setDeclaredField(requestQueueObject, "mProxyHost", httpHost);
				// Log.d("Webkit Setted Proxy to: " + host + ":" + port);
				return true;
			}
		} catch (final Exception e) {
			Log.e("ProxySettings", "Exception setting WebKit proxy through android.webkit.Network: " + e.toString());
		}

		return false;
	}

	private static Object getDeclaredField(Object obj, String name) throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		final Field f = obj.getClass().getDeclaredField(name);
		f.setAccessible(true);
		return f.get(obj);
	}

	@SuppressWarnings("rawtypes")
	private static Object getNetworkInstance(Context context) throws ClassNotFoundException {
		final Class networkClass = Class.forName("android.webkit.Network");
		return networkClass;
	}

	private static Object getRequestQueue(Context context) throws Exception {
		final Object networkClass = getNetworkInstance(context);
		if (networkClass != null) {
			final Object networkObj = invokeMethod(networkClass, "getInstance", new Object[] { context }, Context.class);
			if (networkObj != null) return getDeclaredField(networkObj, "mRequestQueue");
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private static Object invokeMethod(Object object, String methodName, Object[] params, Class... types)
			throws Exception {
		final Class<?> c = object instanceof Class ? (Class) object : object.getClass();

		if (types != null) {
			final Method method = c.getMethod(methodName, types);
			return method.invoke(object, params);
		} else {
			final Method method = c.getMethod(methodName);
			return method.invoke(object);
		}
	}

	private static void setDeclaredField(Object obj, String name, Object value) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		final Field f = obj.getClass().getDeclaredField(name);
		f.setAccessible(true);
		f.set(obj, value);
	}

}
