package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.restartActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

import org.mariotaku.twidere.util.ThemeUtils;

public abstract class BaseThemedActivity extends Activity {

	private int mCurrentThemeResource;

	protected final int getCurrentThemeResource() {
		return mCurrentThemeResource;
	}

	protected abstract int getThemeResource();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		setTheme();
		super.onCreate(savedInstanceState);
		setActionBarBackground();
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

	private final void setActionBarBackground() {
		final ActionBar ab = getActionBar();
		if (ab == null) return;
		ab.setBackgroundDrawable(ThemeUtils.getActionBarBackground(this));
	}

	private final void setTheme() {
		mCurrentThemeResource = getThemeResource();
		setTheme(mCurrentThemeResource);
	}
}
