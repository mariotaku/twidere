package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaUploadResult implements Parcelable {

	public static final Parcelable.Creator<MediaUploadResult> CREATOR = new Parcelable.Creator<MediaUploadResult>() {

		@Override
		public MediaUploadResult createFromParcel(final Parcel source) {
			return new MediaUploadResult(source);
		}

		@Override
		public MediaUploadResult[] newArray(final int size) {
			return new MediaUploadResult[size];
		}
	};

	public final String[] mediaUris;

	public MediaUploadResult(final Parcel src) {
		mediaUris = src.createStringArray();
	}

	public MediaUploadResult(final String[] mediaUris) {
		this.mediaUris = mediaUris;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeStringArray(mediaUris);
	}

}
