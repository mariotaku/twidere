package org.mariotaku.jsonserializer;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JSONSerializer {

	public static final String JSON_CACHE_DIR = "json_cache";

	private static final String KEY_OBJECT = "object";
	private static final String KEY_CLASS = "class";

	public static <T extends JSONParcelable> T[] arrayFromJSON(final JSONParcelable.Creator<T> creator,
			final JSONArray json) {
		if (json == null) return null;
		final int size = json.length();
		final T[] list = creator.newArray(size);
		for (int i = 0; i < size; i++) {
			list[i] = creator.createFromParcel(new JSONParcel(json.optJSONObject(i)));
		}
		return list;
	}

	public static <T extends JSONParcelable> T fromFile(final File file) throws IOException {
		if (file == null) throw new FileNotFoundException();
		final BufferedReader reader = new BufferedReader(new FileReader(file));
		final StringBuffer buf = new StringBuffer();
		String line = reader.readLine();
		while (line != null) {
			buf.append(line);
			buf.append('\n');
			line = reader.readLine();
		}
		reader.close();
		try {
			final JSONObject json = new JSONObject(buf.toString());
			final JSONParcelable.Creator<T> creator = getCreator(json.optString(KEY_CLASS));
			return fromJSON(creator, json.optJSONObject(KEY_OBJECT));
		} catch (final JSONException e) {
			throw new IOException();
		}
	}

	public static <T extends JSONParcelable> T[] fromJSON(final JSONParcelable.Creator<T> creator, final JSONArray json) {
		if (json == null) return null;
		final int size = json.length();
		final T[] array = creator.newArray(size);
		for (int i = 0; i < size; i++) {
			array[i] = creator.createFromParcel(new JSONParcel(json.optJSONObject(i)));
		}
		return array;
	}

	public static <T extends JSONParcelable> T fromJSON(final JSONParcelable.Creator<T> creator, final JSONObject json) {
		if (json == null) return null;
		return creator.createFromParcel(new JSONParcel(json));
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

	public static <T extends JSONParcelable> List<T> listFromFile(final File file) throws IOException {
		if (file == null) throw new FileNotFoundException();
		final BufferedReader reader = new BufferedReader(new FileReader(file));
		final StringBuffer buf = new StringBuffer();
		String line = reader.readLine();
		while (line != null) {
			buf.append(line);
			buf.append('\n');
			line = reader.readLine();
		}
		reader.close();
		try {
			final JSONObject json = new JSONObject(buf.toString());
			final JSONParcelable.Creator<T> creator = getCreator(json.optString(KEY_CLASS));
			return listFromJSON(creator, json.optJSONArray(KEY_OBJECT));
		} catch (final JSONException e) {
			throw new IOException();
		}
	}

	public static <T extends JSONParcelable> List<T> listFromJSON(final JSONParcelable.Creator<T> creator,
			final JSONArray json) {
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
		final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		final JSONObject json = new JSONObject();
		try {
			json.put(KEY_CLASS, parcelable.getClass().getName());
			json.put(KEY_OBJECT, toJSON(parcelable));
			writer.write(json.toString());
		} catch (final JSONException e) {
			throw new IOException(e.getMessage());
		} finally {
			writer.close();
		}
	}

	public static <T extends JSONParcelable> void toFile(final File file, final T[] array) throws IOException {
		if (file == null || array == null) return;
		final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
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
			throw new IOException();
		} finally {
			writer.close();
		}
	}

	public static <T extends JSONParcelable> JSONObject toJSON(final T parcelable) {
		if (parcelable == null) return null;
		final JSONObject json = new JSONObject();
		parcelable.writeToParcel(new JSONParcel(json));
		return json;
	}

	public static <T extends JSONParcelable> JSONArray toJSONArray(final T[] array) {
		if (array == null) return null;
		final JSONArray json = new JSONArray();
		for (final T parcelable : array) {
			json.put(toJSON(parcelable));
		}
		return json;
	}

	@SuppressWarnings("unchecked")
	private static <T extends JSONParcelable> JSONParcelable.Creator<T> getCreator(final String name)
			throws IOException {
		try {
			final Class<?> cls = Class.forName(name);
			return (JSONParcelable.Creator<T>) cls.getField("JSON_CREATOR").get(null);
		} catch (final Exception e) {
			throw new IOException();
		}
	}
}
