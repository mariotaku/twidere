/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
