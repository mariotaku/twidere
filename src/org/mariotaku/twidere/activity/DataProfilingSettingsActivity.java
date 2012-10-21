package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class DataProfilingSettingsActivity extends BaseDialogActivity implements OnClickListener {

	private SharedPreferences mPreferences;

	private Button mSaveButton;

	private CheckBox mCheckBox;

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.save: {
				final SharedPreferences.Editor editor = mPreferences.edit();
				editor.putBoolean(PREFERENCE_KEY_UCD_DATA_PROFILING, mCheckBox.isChecked());
				editor.putBoolean(PREFERENCE_KEY_SHOW_UCD_DATA_PROFILING_REQUEST, false);
				editor.commit();
				finish();
				break;
			}
		}

	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mCheckBox = (CheckBox) findViewById(R.id.checkbox);
		mSaveButton = (Button) findViewById(R.id.save);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.data_profiling_settings);
		if (mPreferences.contains(PREFERENCE_KEY_UCD_DATA_PROFILING)) {
			mCheckBox.setChecked(mPreferences.getBoolean(PREFERENCE_KEY_UCD_DATA_PROFILING, false));
		}
		mSaveButton.setOnClickListener(this);
	}

}
