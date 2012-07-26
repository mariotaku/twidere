package org.mariotaku.twidere.model;

public class ImageSpec {
	public final String thumbnail_link, image_link;

	public ImageSpec(String thumbnail_link, String image_link) {
		this.thumbnail_link = thumbnail_link;
		this.image_link = image_link;
	}

	@Override
	public String toString() {
		return "ImageSpec(" + thumbnail_link + ", " + image_link + ")";
	}
}