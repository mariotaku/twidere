package org.mariotaku.twidere.util;

public class NoDuplicatesStateSavedList<E, State> extends NoDuplicatesArrayList<E> {

	private static final long serialVersionUID = -4185989579743700797L;
	private State state;

	public State getState() {
		return state;
	}

	public void setState(final State state) {
		this.state = state;
	}
}
