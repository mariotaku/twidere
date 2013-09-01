package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class NyanActivity extends FragmentActivity {

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
