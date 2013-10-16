package org.mariotaku.twidere.model;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;
import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;
import org.mariotaku.twidere.Constants;

import java.util.HashMap;
import java.util.Map;

public class SettingsBackupData implements JSONParcelable, Constants {

	public static final Creator<SettingsBackupData> JSON_CREATOR = new Creator<SettingsBackupData>() {

		@Override
		public SettingsBackupData createFromParcel(final JSONParcel in) {
			return new SettingsBackupData(in);
		}

		@Override
		public SettingsBackupData[] newArray(final int size) {
			return new SettingsBackupData[size];
		}
	};

	private final Map<String, Object> settings_map = new HashMap<String, Object>();

	public SettingsBackupData(final Context context) {
		final SharedPreferences settings = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		settings_map.putAll(settings.getAll());
	}

	public SettingsBackupData(final JSONParcel in) {
	}

	@Override
	public void writeToParcel(final JSONParcel out) {
		final JSONObject settings = new JSONObject();
	}

	private static boolean isTypeSupported(final Object object) {
		return object instanceof Boolean || object instanceof Integer || object instanceof Float
				|| object instanceof Long || object instanceof Double || object instanceof String;
	}

}
