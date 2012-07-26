package org.mariotaku.twidere.model;

public class PreviewImage {
	public final boolean has_image;
	public final String matched_url;

	public PreviewImage(boolean has_image, String matched_url) {
		this.has_image = has_image;
		this.matched_url = matched_url;
	}

}