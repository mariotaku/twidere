package me.imid.swipebacklayout.lib.app;

import me.imid.swipebacklayout.lib.SwipeBackLayout;

import org.mariotaku.twidere.util.ViewAccessor;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;

public class SwipeBackActivity extends FragmentActivity {

	private SwipeBackLayout mSwipeBackLayout;

	@Override
	public View findViewById(final int id) {
		final View v = super.findViewById(id);
		if (v != null) return v;
		return mSwipeBackLayout != null ? mSwipeBackLayout.findViewById(id) : null;
	}

	public SwipeBackLayout getSwipeBackLayout() {
		return mSwipeBackLayout;
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
		if (mSwipeBackLayout != null) {
			mSwipeBackLayout.attachToActivity(this);
		}
	}
}
