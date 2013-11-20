package org.mariotaku.twidere.activity.support;

import static org.mariotaku.twidere.util.Utils.restartActivity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;

import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.ThemeUtils;

public abstract class BaseSupportThemedActivity extends FragmentActivity implements IThemedActivity {

	private int mCurrentThemeResource, mCurrentThemeColor;

	@Override
	public void finish() {
		super.finish();
		overrideCloseAnimationIfNeeded();
	}

	@Override
	public final int getCurrentThemeResource() {
		return mCurrentThemeResource;
	}

	@Override
	public void navigateUpFromSameTask() {
		NavUtils.navigateUpFromSameTask(this);
		overrideCloseAnimationIfNeeded();
	}

	@Override
	public void overrideCloseAnimationIfNeeded() {
		if (shouldOverrideActivityAnimation()) {
			ThemeUtils.overrideActivityCloseAnimation(this);
		} else {
			ThemeUtils.overrideNormalActivityCloseAnimation(this);
		}
	}

	@Override
	public boolean shouldOverrideActivityAnimation() {
		return true;
	}

	protected abstract int getThemeColor();

	protected abstract int getThemeResource();

	protected final boolean isThemeChanged() {
		return getThemeResource() != mCurrentThemeResource || getThemeColor() != mCurrentThemeColor;
	}

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

	private final void setActionBarBackground() {
		ThemeUtils.applyActionBarBackground(getActionBar(), this);
	}

	private final void setTheme() {
		mCurrentThemeResource = getThemeResource();
		mCurrentThemeColor = getThemeColor();
		setTheme(mCurrentThemeResource);
	}
}