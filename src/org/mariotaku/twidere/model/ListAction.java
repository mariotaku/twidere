package org.mariotaku.twidere.model;

public abstract class ListAction {
	public abstract String getName();

	public String getSummary() {
		return null;
	}

	public void onClick() {

	}

	public boolean onLongClick() {
		return false;
	}

	@Override
	public final String toString() {
		return getName();
	}
}