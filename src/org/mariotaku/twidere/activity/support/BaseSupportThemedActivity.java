package org.mariotaku.twidere.activity.support;

import static org.mariotaku.twidere.util.Utils.restartActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;

import com.negusoft.holoaccent.AccentHelper;

import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.theme.TwidereAccentHelper;
import org.mariotaku.twidere.util.CompareUtils;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;

public abstract class BaseSupportThemedActivity extends FragmentActivity implements IThemedActivity {

	private int mCurrentThemeResource, mCurrentThemeColor;

	private String mCurrentThemeFontFamily;

	private AccentHelper mAccentHelper;

	@Override
	public void finish() {
		super.finish();
		overrideCloseAnimationIfNeeded();
	}

	@Override
	public final int getCurrentThemeResourceId() {
		return mCurrentThemeResource;
	}

	@Override
	public final Resources getDefaultResources() {
		return super.getResources();
	}

	@Override
	public Resources getResources() {
		return getThemedResources();
	}

	@Override
	public abstract int getThemeColor();

	@Override
	public final Resources getThemedResources() {
		if (mAccentHelper == null) {
			mAccentHelper = new TwidereAccentHelper(ThemeUtils.getUserThemeColor(this));
		}
		return mAccentHelper.getResources(this, super.getResources());
	}

	@Override
	public String getThemeFontFamily() {
		return ThemeUtils.getThemeFontFamily(this);
	}

	@Override
	public abstract int getThemeResourceId();

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

	protected boolean isThemeChanged() {
		return getThemeResourceId() != mCurrentThemeResource || getThemeColor() != mCurrentThemeColor
				|| !CompareUtils.objectEquals(getThemeFontFamily(), mCurrentThemeFontFamily);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		if (Utils.isDebugBuild()) {
			StrictModeUtils.detectAllVmPolicy();
			StrictModeUtils.detectAllThreadPolicy();
		}

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
		mCurrentThemeResource = getThemeResourceId();
		mCurrentThemeColor = getThemeColor();
		mCurrentThemeFontFamily = getThemeFontFamily();
		setTheme(mCurrentThemeResource);
	}
}