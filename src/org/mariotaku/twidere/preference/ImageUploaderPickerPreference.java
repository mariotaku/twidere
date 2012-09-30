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
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class ImageUploaderPickerPreference extends DialogPreference implements Constants, OnClickListener {

	private SharedPreferences mPreferences;
	private final PackageManager mPackageManager;
	private ImageUploaderSpec[] mAvailableImageUploaders;

	public ImageUploaderPickerPreference(final Context context) {
		this(context, null);
	}

	public ImageUploaderPickerPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public ImageUploaderPickerPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mPackageManager = context.getPackageManager();
	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		final SharedPreferences.Editor editor = getEditor();
		if (editor == null) return;
		final ImageUploaderSpec spec = mAvailableImageUploaders[which];
		if (spec != null) {
			editor.putString(PREFERENCE_KEY_IMAGE_UPLOADER, spec.cls);
			editor.commit();
		}
		dialog.dismiss();
	}

	@Override
	public void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
		mPreferences = getSharedPreferences();
		super.onPrepareDialogBuilder(builder);
		if (mPreferences == null) return;
		final String component = mPreferences.getString(PREFERENCE_KEY_IMAGE_UPLOADER, null);
		final ArrayList<ImageUploaderSpec> specs = new ArrayList<ImageUploaderSpec>();
		specs.add(new ImageUploaderSpec(getContext().getString(R.string.image_uploader_default), null));
		final Intent query_intent = new Intent(INTENT_ACTION_EXTENSION_UPLOAD_IMAGE);
		final List<ResolveInfo> result = mPackageManager.queryIntentServices(query_intent, 0);
		for (final ResolveInfo info : result) {
			specs.add(new ImageUploaderSpec(info.loadLabel(mPackageManager).toString(), info.serviceInfo.packageName
					+ "/" + info.serviceInfo.name));
		}
		mAvailableImageUploaders = specs.toArray(new ImageUploaderSpec[specs.size()]);
		builder.setSingleChoiceItems(mAvailableImageUploaders, getIndex(component),
				ImageUploaderPickerPreference.this);
		builder.setNegativeButton(android.R.string.cancel, null);
	}

	private int getIndex(final String cls) {
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

		ImageUploaderSpec(final String name, final String cls) {
			this.name = name;
			this.cls = cls;
		}

		@Override
		public char charAt(final int index) {
			return name.charAt(index);
		}

		@Override
		public int length() {
			return name.length();
		}

		@Override
		public CharSequence subSequence(final int start, final int end) {
			return name.subSequence(start, end);
		}

		@Override
		public String toString() {
			return name;
		}
	}

}
