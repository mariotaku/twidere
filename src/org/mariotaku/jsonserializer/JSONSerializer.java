package org.mariotaku.jsonserializer;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.Constants;
import org.apache.http.protocol.HTTP;
import org.mariotaku.twidere.BuildConfig;
import java.io.FileNotFoundException;

public class JSONSerializer {

	public static final String JSON_CACHE_DIR = "json_cache";
	
	private static final String KEY_OBJECT = "object";
	private static final String KEY_CLASS = "class";

	public static <T extends JSONParcelable> JSONObject toJSON(final T parcelable) {
		final JSONObject json = new JSONObject();
		parcelable.writeToParcel(new JSONParcel(json));
		return json;
	}

	public static <T extends JSONParcelable> JSONArray toJSONArray(final T[] list) {
		final JSONArray json = new JSONArray();
		for (final T parcelable : list) {
			json.put(toJSON(parcelable));
		}
		return json;
	}

	public static <T extends JSONParcelable> T fromJSON(final JSONParcelable.Creator<T> creator, final JSONObject json) {
		return creator.createFromParcel(new JSONParcel(json));		
	}

	public static <T extends JSONParcelable> List<T> listFromJSON(final JSONParcelable.Creator<T> creator, final JSONArray json) {
		if (json == null) return null;
		final int size = json.length();
		final List<T> list = new ArrayList<T>(size);
		for (int i = 0; i < size; i++) {
			list.add(creator.createFromParcel(new JSONParcel(json.optJSONObject(i))));
		}
		return list;
	}
	
	public static <T extends JSONParcelable> void toFile(final File file, final T parcelable) throws IOException {
		if (file == null || parcelable == null) return;
		final FileWriter writer = new FileWriter(file);
		final JSONObject json = new JSONObject();
		try {
			json.put(KEY_CLASS, parcelable.getClass().getName());
			json.put(KEY_OBJECT, toJSON(parcelable));
		} catch (final JSONException e) {
			throw new IOException(e);
		}
		writer.write(json.toString());
		writer.close();
	}

	public static <T extends JSONParcelable> void toFile(final File file, final T[] array) throws IOException {
		if (file == null || array == null) return;
		final FileWriter writer = new FileWriter(file);
		final JSONObject json = new JSONObject();
		try {
			json.put(KEY_CLASS, array.getClass().getComponentType().getName());
			json.put(KEY_OBJECT, toJSONArray(array));		
			if (BuildConfig.DEBUG) {
				writer.write(json.toString(4));
			} else {
				writer.write(json.toString());
			}
		} catch (final JSONException e) {
			throw new IOException(e);
		}
		writer.close();
	}
	
	public static <T extends JSONParcelable> T fromFile(final File file) throws IOException {
		if (file == null) throw new FileNotFoundException();
		final FileInputStream is = new FileInputStream(file);
		final byte[] buffer = new byte[is.available()];
		is.read(buffer);
		is.close();
		try {
			final JSONObject json = new JSONObject(new String(buffer));
			final JSONParcelable.Creator<T> creator = getCreator(json.optString(KEY_CLASS));
			return fromJSON(creator, json.optJSONObject(KEY_OBJECT));
		} catch (JSONException e) {
			throw new IOException(e);
		}
	}

	public static <T extends JSONParcelable> List<T> listFromFile(final File file) throws IOException {
		if (file == null) throw new FileNotFoundException();
		final FileInputStream is = new FileInputStream(file);
		final byte[] buffer = new byte[is.available()];
		is.read(buffer);
		is.close();
		try {
			final JSONObject json = new JSONObject(new String(buffer));
			final JSONParcelable.Creator<T> creator = getCreator(json.optString(KEY_CLASS));
			return listFromJSON(creator, json.optJSONArray(KEY_OBJECT));
		} catch (JSONException e) {
			throw new IOException(e);
		}
	}

	private static <T extends JSONParcelable> JSONParcelable.Creator<T> getCreator(final String name) throws IOException {
		try {
			final Class<?> cls = Class.forName(name);
			return (JSONParcelable.Creator<T>) cls.getField("JSON_CREATOR").get(null);
		} catch (final Exception e) {
			throw new IOException(e);
		}
	}
	
	public static File getSerializationFile(final Context context, final Object... args) throws IOException {
		if (context == null || args == null || args.length == 0) return null;
		final File cache_dir = Utils.getBestCacheDir(context, JSON_CACHE_DIR);
		if (!cache_dir.exists()) {
			cache_dir.mkdirs();
		}
		final String filename = Utils.encodeQueryParams(ArrayUtils.toString(args, '.', false));
		final File cache_file = new File(cache_dir, filename + ".json");
		return cache_file;
	}
}
