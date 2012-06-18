package org.mariotaku.twidere.util;

import java.net.URL;
import java.util.Comparator;
import java.util.Date;

import twitter4j.User;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableUser implements Parcelable {

	public static final Parcelable.Creator<ParcelableUser> CREATOR = new Parcelable.Creator<ParcelableUser>() {
		@Override
		public ParcelableUser createFromParcel(Parcel in) {
			return new ParcelableUser(in);
		}

		@Override
		public ParcelableUser[] newArray(int size) {
			return new ParcelableUser[size];
		}
	};

	public final long account_id, user_id, created_at;

	public final int position;

	public final boolean is_protected;

	public final String description, name, screen_name, location;

	public URL profile_image_url;

	public static final Comparator<ParcelableUser> POSITION_COMPARATOR = new Comparator<ParcelableUser>() {

		@Override
		public int compare(ParcelableUser object1, ParcelableUser object2) {
			long diff = object1.position - object2.position;
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public ParcelableUser(Parcel in) {
		position = in.readInt();
		account_id = in.readLong();
		user_id = in.readLong();
		created_at = in.readLong();
		is_protected = in.readInt() == 1;
		name = in.readString();
		screen_name = in.readString();
		description = in.readString();
		location = in.readString();
		profile_image_url = (URL) in.readSerializable();
	}

	public ParcelableUser(User status, long account_id, int position) {
		this.position = position;
		this.account_id = account_id;
		user_id = status.getId();
		created_at = getTime(status.getCreatedAt());
		is_protected = status.isProtected();
		name = status.getName();
		screen_name = status.getScreenName();
		description = status.getDescription();
		location = status.getLocation();
		profile_image_url = status.getProfileImageURL();
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public String toString() {
		return description;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(position);
		out.writeLong(account_id);
		out.writeLong(user_id);
		out.writeLong(created_at);
		out.writeInt(is_protected ? 1 : 0);
		out.writeString(name);
		out.writeString(screen_name);
		out.writeString(description);
		out.writeSerializable(location);
		out.writeSerializable(profile_image_url);
	}

	private long getTime(Date date) {
		return date != null ? date.getTime() : 0;
	}
}