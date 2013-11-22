package org.mariotaku.twidere.annotation;

public @interface PreferenceType {
	public static final int BOOLEAN = 1;
	public static final int INT = 2;
	public static final int LONG = 3;
	public static final int FLOAT = 4;
	public static final int STRING = 5;
	public static final int NULL = 0;
	public static final int INVALID = -1;

	int value();
}
