package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.restartActivity;

import android.app.Activity;
import android.os.Bundle;

import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.ThemeUtils;

public abstract class BaseThemedActivity extends Activity implements IThemedActivity {

	private int mCurrentThemeResource;

	@Override
	public void finish() {
		super.finish();
		if (shouldOverrideActivityAnimation()) {
			ThemeUtils.overrideActivityCloseAnimation(this);
		}
	}

	@Override
	public final int getCurrentThemeResource() {
		return mCurrentThemeResource;
	}

	@Override
	public boolean shouldOverrideActivityAnimation() {
		return true;
	}

	protected abstract int getThemeResource();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		if (shouldOverrideActivityAnimation()) {
			ThemeUtils.overrideActivityOpenAnimation(this);
		}
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
		ThemeUtils.applyActionBarBackground(getActionBar(), this);
	}

	private final void setTheme() {
		mCurrentThemeResource = getThemeResource();
		setTheme(mCurrentThemeResource);
	}
}
