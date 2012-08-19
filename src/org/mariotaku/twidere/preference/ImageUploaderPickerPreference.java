package org.mariotaku.twidere.preference;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;

public class ImageUploaderPickerPreference extends Preference implements Constants, OnPreferenceClickListener,
		OnClickListener {

	private SharedPreferences mPreferences;

	private final PackageManager mPackageManager;

	private AlertDialog mDialog;

	private ImageUploaderSpec[] mAvailableImageUploaders;

	public ImageUploaderPickerPreference(Context context) {
		this(context, null);
	}

	public ImageUploaderPickerPreference(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public ImageUploaderPickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPackageManager = context.getPackageManager();
		setOnPreferenceClickListener(this);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		final SharedPreferences.Editor editor = getEditor();
		if (editor == null) return;
		final ImageUploaderSpec spec = mAvailableImageUploaders[which];
		if (spec != null) {
			editor.putString(PREFERENCE_KEY_IMAGE_UPLOADER, spec.cls);
			editor.commit();
		}
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		mPreferences = getSharedPreferences();
		if (mPreferences == null) return false;
		final String cls = mPreferences.getString(PREFERENCE_KEY_IMAGE_UPLOADER, null);
		final ArrayList<ImageUploaderSpec> specs = new ArrayList<ImageUploaderSpec>();
		specs.add(new ImageUploaderSpec(getContext().getString(R.string.default_provider), null));
		final Intent query_intent = new Intent(INTENT_ACTION_EXTENSION_UPLOAD_IMAGE);
		final List<ResolveInfo> result = mPackageManager.queryIntentServices(query_intent, 0);
		for (final ResolveInfo info : result) {
			specs.add(new ImageUploaderSpec(info.loadLabel(mPackageManager).toString(),
					info.serviceInfo.name));
		}
		mAvailableImageUploaders = specs.toArray(new ImageUploaderSpec[specs.size()]);
		final AlertDialog.Builder selector_builder = new AlertDialog.Builder(getContext());
		selector_builder.setTitle(getTitle());
		selector_builder.setSingleChoiceItems(mAvailableImageUploaders, getIndex(cls),
				ImageUploaderPickerPreference.this);
		selector_builder.setNegativeButton(android.R.string.cancel, null);
		mDialog = selector_builder.show();
		return true;
	}

	private int getIndex(String cls) {
		if (mAvailableImageUploaders == null) return -1;
		if (cls == null) return 0;
		final int count = mAvailableImageUploaders.length;
		for (int i = 0; i < count; i++) {
			final ImageUploaderSpec spec = mAvailableImageUploaders[i];
			if (cls.equals(spec.cls)) return i;
		}
		return -1;
	}

	static class ImageUploaderSpec implements CharSequence {
		private final String name, cls;

		ImageUploaderSpec(String name, String cls) {
			this.name = name;
			this.cls = cls;
		}

		@Override
		public char charAt(int index) {
			return name.charAt(index);
		}

		@Override
		public int length() {
			return name.length();
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return name.subSequence(start, end);
		}

		@Override
		public String toString() {
			return name;
		}
	}

}
