package org.mariotaku.twidere.service;

import java.util.List;

public class ListUtils {

	public static <T> String buildString(List<T> array, char token, boolean include_space) {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < array.size(); i++) {
			final String id_string = String.valueOf(array.get(i));
			if (id_string != null) {
				if (i > 0) {
					builder.append(include_space ? token + " " : token);
				}
				builder.append(id_string);
			}
		}
		return builder.toString();
	}
}
