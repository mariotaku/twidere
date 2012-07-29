package org.mariotaku.twidere.model;

import static org.mariotaku.twidere.util.Utils.parseDouble;
import twitter4j.GeoLocation;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableLocation implements Parcelable {

	public final double latitude, longitude;

	public static final Parcelable.Creator<ParcelableUserList> CREATOR = new Parcelable.Creator<ParcelableUserList>() {
		@Override
		public ParcelableUserList createFromParcel(Parcel in) {
			return new ParcelableUserList(in);
		}

		@Override
		public ParcelableUserList[] newArray(int size) {
			return new ParcelableUserList[size];
		}
	};

	public ParcelableLocation(GeoLocation location) {
		if (location == null) {
			latitude = -1;
			longitude = -1;
			return;
		}
		latitude = location.getLatitude();
		longitude = location.getLongitude();
	}

	public ParcelableLocation(Parcel in) {
		latitude = in.readDouble();
		longitude = in.readDouble();
	}

	public ParcelableLocation(String location_string) {
		if (location_string == null) {
			latitude = -1;
			longitude = -1;
			return;
		}
		final String[] longlat = location_string.split(",");
		if (longlat == null || longlat.length != 2) {
			latitude = -1;
			longitude = -1;
		} else {
			latitude = parseDouble(longlat[0]);
			longitude = parseDouble(longlat[1]);
		}
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	public boolean isValid() {
		return latitude >= 0 || longitude >= 0;
	}

	@Override
	public String toString() {
		if (!isValid()) return null;
		return latitude + "," + longitude;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeDouble(latitude);
		out.writeDouble(longitude);
	}

	public static boolean isValidLocation(ParcelableLocation location) {
		return location != null && location.isValid();
	}
}
