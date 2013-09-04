package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;

import android.app.Activity;
import android.os.Bundle;

public class NyanActivity extends Activity {

	@Override
	public void onContentChanged() {
		super.onContentChanged();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nyan);
	}

}
