package me.imid.swipebacklayout.lib.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import me.imid.swipebacklayout.lib.SwipeBackLayout;

import org.mariotaku.twidere.util.ViewAccessor;

@SuppressLint("Registered")
public class SwipeBackActivity extends Activity {

	private SwipeBackLayout mSwipeBackLayout;

	private boolean mOverrideExitAniamtion = true;

	private boolean mIsFinishing;

	@Override
	public View findViewById(final int id) {
		final View v = super.findViewById(id);
		if (v != null) return v;
		return mSwipeBackLayout.findViewById(id);
	}

	@Override
	public void finish() {
		if (mOverrideExitAniamtion && !mIsFinishing) {
			scrollToFinishActivity();
			mIsFinishing = true;
			return;
		}
		mIsFinishing = false;
		super.finish();
	}

	public SwipeBackLayout getSwipeBackLayout() {
		return mSwipeBackLayout;
	}

	/**
	 * Scroll out contentView and finish the activity
	 */
	public void scrollToFinishActivity() {
		mSwipeBackLayout.scrollToFinishActivity();
	}

	/**
	 * Override Exit Animation
	 * 
	 * @param override
	 */
	public void setOverrideExitAniamtion(final boolean override) {
		mOverrideExitAniamtion = override;
	}

	public void setSwipeBackEnable(final boolean enable) {
		mSwipeBackLayout.setEnableGesture(enable);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Window w = getWindow();
		w.setBackgroundDrawable(new ColorDrawable(0));
		ViewAccessor.setBackground(w.getDecorView(), null);
		mSwipeBackLayout = new SwipeBackLayout(this);
	}

	@Override
	protected void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mSwipeBackLayout.attachToActivity(this);
	}
}
