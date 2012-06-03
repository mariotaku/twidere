package org.mariotaku.twidere.util;

import android.os.Bundle;

public interface ActivityThemeChangeInterface {

	public boolean isThemeChanged();

	public void onCreate(Bundle savedInstanceState);

	public void onResume();

	public void setTheme();
}
