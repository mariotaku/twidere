package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.restartActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public abstract class BaseThemedActivity extends FragmentActivity {

	private int mCurrentThemeResource;

	protected final int getCurrentThemeResource() {
		return mCurrentThemeResource;
	}

	protected abstract int getThemeResource();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		setTheme();
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isThemeChanged()) {
			restart();
		}
	}

	protected final void restart() {
		restartActivity(this);
	}

	private final boolean isThemeChanged() {
		return getThemeResource() != mCurrentThemeResource;
	}

	private final void setTheme() {
		mCurrentThemeResource = getThemeResource();
		setTheme(mCurrentThemeResource);
	}
}
