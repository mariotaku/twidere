package org.mariotaku.twidere.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import org.mariotaku.twidere.R;

@SuppressLint("Registered")
public class BaseDialogWhenLargeActivity extends BaseActivity {

	private View mActivityContent;

	@Override
	public View findViewById(final int id) {
		if (shouldDisableDialogWhenLargeMode()) return super.findViewById(id);
		return mActivityContent.findViewById(id);
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		setActionBarBackground();
	}
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarBackground();
	}
	
	@Override
	public void setContentView(final int layoutResID) {
		if (shouldDisableDialogWhenLargeMode()) {
			super.setContentView(layoutResID);
			return;
		}
		final LayoutInflater inflater = getLayoutInflater();
		final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.dialogwhenlarge, null);
		final ViewGroup content = (ViewGroup) root.findViewById(R.id.activity_content);
		mActivityContent = inflater.inflate(layoutResID, content, true);
		super.setContentView(root);
	}

	@Override
	public void setContentView(final View view) {
		if (shouldDisableDialogWhenLargeMode()) {
			super.setContentView(view);
			return;
		}
		setContentView(view, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	@Override
	public void setContentView(final View view, final LayoutParams params) {
		if (shouldDisableDialogWhenLargeMode()) {
			super.setContentView(view, params);
			return;
		}
		final LayoutInflater inflater = getLayoutInflater();
		final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.dialogwhenlarge, null);
		final ViewGroup content = (ViewGroup) root.findViewById(R.id.activity_content);
		content.addView(mActivityContent = view, params);
		super.setContentView(root);
	}

	@Override
	protected int getDarkThemeRes() {
		if (shouldDisableDialogWhenLargeMode()) return super.getDarkThemeRes();
		return R.style.Theme_Twidere_DialogWhenLarge;
	}

	@Override
	protected int getLightThemeRes() {
		if (shouldDisableDialogWhenLargeMode()) return super.getLightThemeRes();
		return R.style.Theme_Twidere_Light_DialogWhenLarge;
	}

	protected boolean shouldDisableDialogWhenLargeMode() {
		return false;
	}

	@Override
	protected boolean shouldSetBackground() {
		if (shouldDisableDialogWhenLargeMode()) return super.shouldSetBackground();
		return getResources().getBoolean(R.bool.should_set_background);
	}
}
