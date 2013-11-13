package me.imid.swipebacklayout.lib.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import me.imid.swipebacklayout.lib.SwipeBackLayout;

public class SwipeBackActivity extends FragmentActivity implements SwipeBackActivityBase {
	private SwipeBackActivityHelper mHelper;

	@Override
	public View findViewById(final int id) {
		final View v = super.findViewById(id);
		if (v == null && mHelper != null) return mHelper.findViewById(id);
		return v;
	}

	@Override
	public SwipeBackLayout getSwipeBackLayout() {
		return mHelper.getSwipeBackLayout();
	}

	@Override
	public void scrollToFinishActivity() {
		getSwipeBackLayout().scrollToFinishActivity();
	}

	@Override
	public void setSwipeBackEnable(final boolean enable) {
		getSwipeBackLayout().setEnableGesture(enable);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new SwipeBackActivityHelper(this);
		mHelper.onActivtyCreate();
	}

	@Override
	protected void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mHelper.onPostCreate();
	}
}
