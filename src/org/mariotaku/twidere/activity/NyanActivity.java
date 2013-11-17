package org.mariotaku.twidere.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.CroutonLifecycleCallback;
import de.keyboardsurfer.android.widget.crouton.CroutonStyle;

import org.mariotaku.twidere.R;

public class NyanActivity extends Activity implements OnClickListener, CroutonLifecycleCallback {

	private boolean mCroutonShowing;

	@Override
	public void onClick(final View v) {
		if (mCroutonShowing) return;
		final Crouton c = Crouton.makeText(this, R.string.nyan_sakamoto, CroutonStyle.INFO);
		c.setLifecycleCallback(this);
		c.show();
	}

	@Override
	public void onDisplayed() {
		mCroutonShowing = true;
	}

	@Override
	public void onRemoved() {
		mCroutonShowing = false;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nyan);
	}

}
