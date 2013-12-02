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
	public final String text;
	public final ParcelableLocation location;
	public final Uri media_uri;
	public final long in_reply_to_status_id;
	public final boolean is_possibly_sensitive;
	public final int media_type;

	public ParcelableStatusUpdate(final DraftItem draft) {
		account_ids = draft.account_ids;
		text = draft.text;
		location = draft.location;
		media_uri = draft.media_uri != null ? Uri.parse(draft.media_uri) : null;
		media_type = draft.media_type;
		in_reply_to_status_id = draft.in_reply_to_status_id;
		is_possibly_sensitive = draft.is_possibly_sensitive;
	}

	/**
	 * @deprecated It has too much arguments to call, use
	 *             <b>ParcelableStatusUpdate.Builder</b> instead.
	 */
	@Deprecated
	public ParcelableStatusUpdate(final long[] account_ids, final String text, final ParcelableLocation location,
			final Uri media_uri, final int media_type, final long in_reply_to_status_id,
			final boolean is_possibly_sensitive) {
		this.account_ids = account_ids;
		this.text = text;
		this.location = location;
		this.media_uri = media_uri;
		this.media_type = media_type;
		this.in_reply_to_status_id = in_reply_to_status_id;
		this.is_possibly_sensitive = is_possibly_sensitive;
	}

	public ParcelableStatusUpdate(final Parcel in) {
		account_ids = in.createLongArray();
		text = in.readString();
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
		return "ParcelableStatusUpdate{account_ids=" + Arrays.toString(account_ids) + ", content=" + text
				+ ", location=" + location + ", media_uri=" + media_uri + ", in_reply_to_status_id="
				+ in_reply_to_status_id + ", is_possibly_sensitive=" + is_possibly_sensitive + ", media_type="
				+ media_type + "}";
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeLongArray(account_ids);
		dest.writeString(text);
		dest.writeParcelable(location, flags);
		dest.writeParcelable(media_uri, flags);
		dest.writeInt(media_type);
		dest.writeLong(in_reply_to_status_id);
		dest.writeInt(is_possibly_sensitive ? 1 : 0);
	}

	public static final class Builder {

		private long[] account_ids;
		private String text;
		private ParcelableLocation location;
		private Uri media_uri;
		private int media_type;
		private long in_reply_to_status_id;
		private boolean is_possibly_sensitive;

		public Builder() {

		}

		public Builder(final ParcelableStatusUpdate base) {
			accountIds(base.account_ids);
			text(base.text);
			media(base.media_uri, base.media_type);
			location(base.location);
			inReplyToStatusId(base.in_reply_to_status_id);
			isPossiblySensitive(base.is_possibly_sensitive);
		}

		public Builder accountIds(final long[] account_ids) {
			this.account_ids = account_ids;
			return this;
		}

		public ParcelableStatusUpdate build() {
			return new ParcelableStatusUpdate(account_ids, text, location, media_uri, media_type,
					in_reply_to_status_id, is_possibly_sensitive);
		}

		public Builder inReplyToStatusId(final long in_reply_to_status_id) {
			this.in_reply_to_status_id = in_reply_to_status_id;
			return this;
		}

		public Builder isPossiblySensitive(final boolean is_possibly_sensitive) {
			this.is_possibly_sensitive = is_possibly_sensitive;
			return this;
		}

		public Builder location(final ParcelableLocation location) {
			this.location = location;
			return this;
		}

		public Builder media(final Uri media_uri, final int media_type) {
			this.media_uri = media_uri;
			this.media_type = media_type;
			return this;
		}

		public Builder text(final String text) {
			this.text = text;
			return this;
		}
	}

}
