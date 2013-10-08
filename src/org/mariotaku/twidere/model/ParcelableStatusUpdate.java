package org.mariotaku.twidere.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

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
	public final Uri media_uri;
	public final long in_reply_to_status_id;
	public final boolean is_possibly_sensitive;
	public final int media_type;

	public ParcelableStatusUpdate(final DraftItem draft) {
		account_ids = draft.account_ids;
		content = draft.text;
		location = draft.location;
		media_uri = draft.media_uri != null ? Uri.parse(draft.media_uri) : null;
		media_type = draft.media_type;
		in_reply_to_status_id = draft.in_reply_to_status_id;
		is_possibly_sensitive = draft.is_possibly_sensitive;
	}

	public ParcelableStatusUpdate(final long[] account_ids, final String content, final ParcelableLocation location,
			final Uri image_uri, final int image_type, final long in_reply_to_status_id,
			final boolean is_possibly_sensitive) {
		this.account_ids = account_ids;
		this.content = content;
		this.location = location;
		media_uri = image_uri;
		media_type = image_type;
		this.in_reply_to_status_id = in_reply_to_status_id;
		this.is_possibly_sensitive = is_possibly_sensitive;
	}

	public ParcelableStatusUpdate(final Parcel in) {
		account_ids = in.createLongArray();
		content = in.readString();
		location = in.readParcelable(ParcelableLocation.class.getClassLoader());
		media_uri = in.readParcelable(Uri.class.getClassLoader());
		media_type = in.readInt();
		in_reply_to_status_id = in.readLong();
		is_possibly_sensitive = in.readInt() == 1;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
		return "ParcelableStatusUpdate{account_ids=" + Arrays.toString(account_ids) + ", content=" + content
				+ ", location=" + location + ", media_uri=" + media_uri + ", in_reply_to_status_id="
				+ in_reply_to_status_id + ", is_possibly_sensitive=" + is_possibly_sensitive + ", media_type="
				+ media_type + "}";
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeLongArray(account_ids);
		dest.writeString(content);
		dest.writeParcelable(location, flags);
		dest.writeParcelable(media_uri, flags);
		dest.writeInt(media_type);
		dest.writeLong(in_reply_to_status_id);
		dest.writeInt(is_possibly_sensitive ? 1 : 0);
	}

}
