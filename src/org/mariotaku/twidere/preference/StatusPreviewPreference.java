package org.mariotaku.twidere.preference;

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.mariotaku.twidere.util.Utils.formatSameDayTime;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.StatusViewHolder;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StatusPreviewPreference extends Preference implements Constants, OnSharedPreferenceChangeListener {

	final LayoutInflater mInflater;
	StatusViewHolder mHolder;
	final SharedPreferences mPreferences;
	
	public StatusPreviewPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mInflater = LayoutInflater.from(context);
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	public StatusPreviewPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StatusPreviewPreference(Context context) {
		this(context, null);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		return mInflater.inflate(R.layout.status_list_item, null);
	}
	
	private void setImagePreview() {
		if (mHolder == null) return;
		mHolder.image_preview.setVisibility(mPreferences.getBoolean(PREFERENCE_KEY_INLINE_IMAGE_PREVIEW, false) ? View.VISIBLE : View.GONE);
	}
	
	private void setProfileImage() {
		if (mHolder == null) return;
		mHolder.profile_image.setVisibility(mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true) ? View.VISIBLE : View.GONE);
	}
	
	private void setTextSize() {
		if (mHolder == null) return;
		mHolder.setTextSize(mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE));
	}

	private void showTime() {
		if (mHolder == null) return;
		if (mPreferences.getBoolean(PREFERENCE_KEY_SHOW_ABSOLUTE_TIME, false)) {
			mHolder.time.setText(formatSameDayTime(getContext(), System.currentTimeMillis() - 360000));
		} else {
			mHolder.time.setText(getRelativeTimeSpanString(System.currentTimeMillis() - 360000));
		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
		if (mHolder == null) return;
		if (PREFERENCE_KEY_TEXT_SIZE.equals(key)) {
			setTextSize();
		} else if (PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE.equals(key)) {
			setProfileImage();
		} else if (PREFERENCE_KEY_INLINE_IMAGE_PREVIEW.equals(key)) {
			setImagePreview();
		} else if (PREFERENCE_KEY_SHOW_ABSOLUTE_TIME.equals(key)) {
			showTime();
		}
	}

	@Override
	protected void onBindView(View view) {
		mHolder = new StatusViewHolder(view);
		mHolder.profile_image.setImageResource(R.drawable.ic_launcher);
		mHolder.image_preview.setImageResource(R.drawable.twidere_promotional_graphic);
		mHolder.name.setText("Twidere Project");
		mHolder.screen_name.setText("@twidere_project");
		mHolder.text.setText("Twidere is an open source twitter client for Android.");
		setImagePreview();
		setProfileImage();
		setTextSize();
		showTime();
		super.onBindView(view);
	}

}
