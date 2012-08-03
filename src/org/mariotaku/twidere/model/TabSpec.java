package org.mariotaku.twidere.model;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class TabSpec {

	public final String name;
	public final Integer icon;
	public final Class<? extends Fragment> cls;
	public final Bundle args;
	public final int position;

	@Deprecated
	public TabSpec(String name, Integer icon, Class<? extends Fragment> cls, Bundle args) {
		this(name, icon, cls, args, 0);
	}
	
	public TabSpec(String name, Integer icon, Class<? extends Fragment> cls, Bundle args, int position) {
		if (cls == null) throw new IllegalArgumentException("Fragment cannot be null!");
		if (name == null && icon == null)
			throw new IllegalArgumentException("You must specify a name or icon for this tab!");
		this.name = name;
		this.icon = icon;
		this.cls = cls;
		this.args = args;
		this.position = position;

	}
	
}