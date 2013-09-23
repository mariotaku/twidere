package org.mariotaku.twidere.model;

import java.util.Arrays;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableStatusUpdate implements Parcelable {

	public static final Parcelable.Creator<ParcelableStatusUpdate> CREATOR = new Parcelable.Creator<ParcelableStatusUpdate>() {
		@Override
		public ParcelableStatusUpdate createFromParcel(final Parcel in) {
			return new ParcelableStatusUpdate(in);
		}

		@Override
		public ParcelableStatusUpdate[] newArray(final int size) {
			return new ParcelableStatusUpdate[size];
		}
	};

	public final long[] account_ids;
	public final String content;
	public final ParcelableLocation location;
	public final Uri image_uri;
	public final long in_reply_to_status_id;
	public final boolean is_possibly_sensitive;
	public final boolean delete_image;

	public ParcelableStatusUpdate(final long[] account_ids, final String content, final ParcelableLocation location,
			final Uri image_uri, final long in_reply_to_status_id, final boolean is_possibly_sensitive,
			final boolean delete_image) {
		this.account_ids = account_ids;
		this.content = content;
		this.location = location;
		this.image_uri = image_uri;
		this.in_reply_to_status_id = in_reply_to_status_id;
		this.is_possibly_sensitive = is_possibly_sensitive;
		this.delete_image = delete_image;
	}

	public ParcelableStatusUpdate(final Parcel in) {
		account_ids = in.createLongArray();
		content = in.readString();
		location = in.readParcelable(ParcelableLocation.class.getClassLoader());
		image_uri = in.readParcelable(Uri.class.getClassLoader());
		in_reply_to_status_id = in.readLong();
		is_possibly_sensitive = in.readInt() == 1;
		delete_image = in.readInt() == 1;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeLongArray(account_ids);
		dest.writeString(content);
		dest.writeParcelable(location, flags);
		dest.writeParcelable(image_uri, flags);
		dest.writeLong(in_reply_to_status_id);
		dest.writeInt(is_possibly_sensitive ? 1 : 0);
		dest.writeInt(delete_image ? 1 : 0);
	}

	@Override
	public String toString() {
		return "ParcelableStatusUpdate{account_ids=" + Arrays.toString(account_ids) + ", content=" + content
				+ ", location=" + location + ", image_uri=" + image_uri + ", in_reply_to_status_id="
				+ in_reply_to_status_id + ", is_possibly_sensitive=" + is_possibly_sensitive + ", delete_image="
				+ delete_image + "}";
	}

}
