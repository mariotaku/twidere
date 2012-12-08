package com.robobunny;

import org.mariotaku.twidere.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {

	private final String TAG = getClass().getName();

	private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";
	private static final String ROBOBUNNYNS = "http://robobunny.com";
	private static final int DEFAULT_VALUE = 50;

	private int mMaxValue = 100;
	private int mMinValue = 0;
	private int mInterval = 1;
	private int mCurrentValue;
	private String mUnitsLeft = "";
	private String mUnitsRight = "";
	
	private SeekBar mSeekBar;
	private TextView mStatusText;

	public SeekBarPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		initPreference(context, attrs);
	}

	public SeekBarPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		initPreference(context, attrs);
	}

	@Override
	public void onBindView(final View view) {
		super.onBindView(view);

		// move our seekbar to the new view we've been given
		final ViewParent oldContainer = mSeekBar.getParent();
		final ViewGroup newContainer = (ViewGroup) view.findViewById(R.id.seekBarPrefBarContainer);

		if (oldContainer != newContainer) {
			// remove the seekbar from the old view
			if (oldContainer != null) {
				((ViewGroup) oldContainer).removeView(mSeekBar);
			}
			// remove the existing seekbar (there may not be one) and add
			// ours
			newContainer.removeAllViews();
			newContainer.addView(mSeekBar, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		updateView(view);
	}

	@Override
	public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
		int newValue = progress + mMinValue;

		if (newValue > mMaxValue) {
			newValue = mMaxValue;
		} else if (newValue < mMinValue) {
			newValue = mMinValue;
		} else if (mInterval != 1 && newValue % mInterval != 0) {
			newValue = Math.round((float) newValue / mInterval) * mInterval;
		}

		// change rejected, revert to the previous value
		if (!callChangeListener(newValue)) {
			seekBar.setProgress(mCurrentValue - mMinValue);
			return;
		}

		// change accepted, store it
		mCurrentValue = newValue;
		mStatusText.setText(String.valueOf(newValue));
		persistInt(newValue);

	}

	@Override
	public void onStartTrackingTouch(final SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(final SeekBar seekBar) {
		notifyChanged();
	}

	@Override
	protected View onCreateView(final ViewGroup parent) {
		final LayoutInflater mInflater = LayoutInflater.from(getContext());
		return mInflater.inflate(R.layout.seek_bar_preference, parent, false);

	}

	@Override
	protected Object onGetDefaultValue(final TypedArray ta, final int index) {
		final int defaultValue = ta.getInt(index, DEFAULT_VALUE);
		return defaultValue;
	}

	@Override
	protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {

		if (restoreValue) {
			mCurrentValue = getPersistedInt(mCurrentValue);
		} else {
			int temp = 0;
			try {
				temp = (Integer) defaultValue;
			} catch (final ClassCastException ex) {
				Log.e(TAG, "Invalid default value: " + defaultValue.toString());
			}

			persistInt(temp);
			mCurrentValue = temp;
		}

	}

	/**
	 * Update a SeekBarPreference view with our current state
	 * 
	 * @param view
	 */
	protected void updateView(final View view) {

		final RelativeLayout layout = (RelativeLayout) view;

		mStatusText = (TextView) layout.findViewById(R.id.seekBarPrefValue);
		mStatusText.setText(String.valueOf(mCurrentValue));
		mStatusText.setMinimumWidth(30);

		mSeekBar.setProgress(mCurrentValue - mMinValue);

		final TextView unitsRight = (TextView) layout.findViewById(R.id.seekBarPrefUnitsRight);
		unitsRight.setText(mUnitsRight);

		final TextView unitsLeft = (TextView) layout.findViewById(R.id.seekBarPrefUnitsLeft);
		unitsLeft.setText(mUnitsLeft);

	}

	private String getAttributeStringValue(final AttributeSet attrs, final String namespace, final String name,
			final String defaultValue) {
		String value = attrs.getAttributeValue(namespace, name);
		if (value == null) {
			value = defaultValue;
		}

		return value;
	}

	private void initPreference(final Context context, final AttributeSet attrs) {
		setValuesFromXml(attrs);
		mSeekBar = new SeekBar(context, attrs);
		mSeekBar.setMax(mMaxValue - mMinValue);
		mSeekBar.setOnSeekBarChangeListener(this);
	}

	private void setValuesFromXml(final AttributeSet attrs) {
		mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
		mMinValue = attrs.getAttributeIntValue(ROBOBUNNYNS, "min", 0);

		mUnitsLeft = getAttributeStringValue(attrs, ROBOBUNNYNS, "unitsLeft", "");
		final String units = getAttributeStringValue(attrs, ROBOBUNNYNS, "units", "");
		mUnitsRight = getAttributeStringValue(attrs, ROBOBUNNYNS, "unitsRight", units);

		try {
			final String newInterval = attrs.getAttributeValue(ROBOBUNNYNS, "interval");
			if (newInterval != null) {
				mInterval = Integer.parseInt(newInterval);
			}
		} catch (final NumberFormatException e) {
			Log.e(TAG, "Invalid interval value", e);
		}

	}

}
