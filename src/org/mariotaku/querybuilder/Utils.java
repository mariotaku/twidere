package org.mariotaku.querybuilder;

public class Utils {

	public static String toString(final SQLLang[] array) {
		final StringBuilder builder = new StringBuilder();
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			final String id_string = array[i].getSQL();
			if (id_string != null) {
				if (i > 0) {
					builder.append(", ");
				}
				builder.append(id_string);
			}
		}
		return builder.toString();
	}

}
