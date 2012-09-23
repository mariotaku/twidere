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

public class TweetShortenerPickerPreference extends Preference implements Constants, OnPreferenceClickListener,
		OnClickListener {

	private SharedPreferences mPreferences;

	private final PackageManager mPackageManager;

	private AlertDialog mDialog;

	private TweetShortenerSpec[] mAvailableTweetShorteners;

	public TweetShortenerPickerPreference(final Context context) {
		this(context, null);
	}

	public TweetShortenerPickerPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public TweetShortenerPickerPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mPackageManager = context.getPackageManager();
		setOnPreferenceClickListener(this);
	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		final SharedPreferences.Editor editor = getEditor();
		if (editor == null) return;
		final TweetShortenerSpec spec = mAvailableTweetShorteners[which];
		if (spec != null) {
			editor.putString(PREFERENCE_KEY_TWEET_SHORTENER, spec.cls);
			editor.commit();
		}
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	@Override
	public boolean onPreferenceClick(final Preference preference) {
		mPreferences = getSharedPreferences();
		if (mPreferences == null) return false;
		final String component = mPreferences.getString(PREFERENCE_KEY_TWEET_SHORTENER, null);
		final ArrayList<TweetShortenerSpec> specs = new ArrayList<TweetShortenerSpec>();
		specs.add(new TweetShortenerSpec(getContext().getString(R.string.tweet_shortener_default), null));
		final Intent query_intent = new Intent(INTENT_ACTION_EXTENSION_SHORTEN_TWEET);
		final List<ResolveInfo> result = mPackageManager.queryIntentServices(query_intent, 0);
		for (final ResolveInfo info : result) {
			specs.add(new TweetShortenerSpec(info.loadLabel(mPackageManager).toString(), info.serviceInfo.packageName
					+ "/" + info.serviceInfo.name));
		}
		mAvailableTweetShorteners = specs.toArray(new TweetShortenerSpec[specs.size()]);
		final AlertDialog.Builder selector_builder = new AlertDialog.Builder(getContext());
		selector_builder.setTitle(getTitle());
		selector_builder.setSingleChoiceItems(mAvailableTweetShorteners, getIndex(component),
				TweetShortenerPickerPreference.this);
		selector_builder.setNegativeButton(android.R.string.cancel, null);
		mDialog = selector_builder.show();
		return true;
	}

	private int getIndex(final String cls) {
		if (mAvailableTweetShorteners == null) return -1;
		if (cls == null) return 0;
		final int count = mAvailableTweetShorteners.length;
		for (int i = 0; i < count; i++) {
			final TweetShortenerSpec spec = mAvailableTweetShorteners[i];
			if (cls.equals(spec.cls)) return i;
		}
		return -1;
	}

	static class TweetShortenerSpec implements CharSequence {
		private final String name, cls;

		TweetShortenerSpec(final String name, final String cls) {
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
