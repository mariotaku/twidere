package org.mariotaku.actionbarcompat;

import android.graphics.drawable.Drawable;
import android.view.View;

public interface ActionBar {

	public View getCustomView();

	public int getHeight();

	public CharSequence getSubtitle();

	public CharSequence getTitle();

	public void setBackgroundDrawable(Drawable paramDrawable);

	public void setCustomView(int resId);

	public void setCustomView(View view);

	public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp);

	public void setDisplayShowCustomEnabled(boolean showCustom);

	public void setDisplayShowHomeEnabled(boolean sShowHome);

	public void setDisplayShowTitleEnabled(boolean showTitle);

	public void setSubtitle(CharSequence subtitle);

	public void setSubtitle(int resId);

	public void setTitle(CharSequence title);

	public void setTitle(int resId);

}
