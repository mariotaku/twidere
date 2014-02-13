package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;

public class StatusShortenResult implements Parcelable {

	public static final Parcelable.Creator<StatusShortenResult> CREATOR = new Parcelable.Creator<StatusShortenResult>() {

		@Override
		public StatusShortenResult createFromParcel(final Parcel source) {
			return new StatusShortenResult(source);
		}

		@Override
		public StatusShortenResult[] newArray(final int size) {
			return new StatusShortenResult[size];
		}
	};

	public final String shortened;

	public StatusShortenResult(final Parcel src) {
		shortened = src.readString();
	}

	public StatusShortenResult(final String shortened) {
		this.shortened = shortened;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeString(shortened);
	}

}
