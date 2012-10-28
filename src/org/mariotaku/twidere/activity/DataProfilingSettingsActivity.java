package org.mariotaku.twidere.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.mariotaku.twidere.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import edu.ucdavis.earlybird.CSVFileFilter;

public class DataProfilingSettingsActivity extends BaseActivity implements OnClickListener {

	private SharedPreferences mPreferences;

	private Button mSaveButton, mPreviewButton;
	private CheckBox mCheckBox;
	private TextView mTextView;
	private boolean mShowingPreview;

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
			case R.id.preview: {
				if (!mShowingPreview) {
					mTextView.setText(null);
					final File dir = getFilesDir();
					for (final File file : dir.listFiles(new CSVFileFilter())) {
						mTextView.append(file.getName() + ":\n------\n");
						try {
							final BufferedReader br = new BufferedReader(new FileReader(file));
							String line = br.readLine();
							int i = 0;
							while (line != null && i < 10) {
								mTextView.append(line + "\n");
								line = br.readLine();
								i++;
							}
							mTextView.append("------------\n\n");
						} catch (final IOException e) {
							mTextView.append("Cannot read this file");
						}
					}
				} else {
					mTextView.setText(R.string.data_profiling_summary);
				}
				mShowingPreview = !mShowingPreview;
				break;
			}
		}

	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mTextView = (TextView) findViewById(android.R.id.text1);
		mCheckBox = (CheckBox) findViewById(R.id.checkbox);
		mSaveButton = (Button) findViewById(R.id.save);
		mPreviewButton = (Button) findViewById(R.id.preview);
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
		mPreviewButton.setOnClickListener(this);
	}

}
