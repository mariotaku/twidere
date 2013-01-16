package org.mariotaku.twidere.util;

import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;

public class URLFileNameGenerator implements FileNameGenerator {

	public String generate(String imageUri) {
		if (imageUri == null) return null;
		return imageUri.replaceFirst("https?:\\/\\/", "").replaceAll("[^\\w\\d]", "_");
	}
	
}
