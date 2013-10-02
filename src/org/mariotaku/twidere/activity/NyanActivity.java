package org.mariotaku.twidere.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import org.mariotaku.twidere.R;

public class NyanActivity extends Activity implements OnClickListener {

	@Override
	public void onClick(final View v) {
		Toast.makeText(this, R.string.nyan_sakamoto, Toast.LENGTH_SHORT).show();
	}

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
