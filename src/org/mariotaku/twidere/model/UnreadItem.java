package org.mariotaku.twidere.model;

import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;

public class UnreadItem implements JSONParcelable {

	public static final JSONParcelable.Creator<UnreadItem> JSON_CREATOR = new JSONParcelable.Creator<UnreadItem>() {
		@Override
		public UnreadItem createFromParcel(final JSONParcel in) {
			return new UnreadItem(in);
		}

		@Override
		public UnreadItem[] newArray(final int size) {
			return new UnreadItem[size];
		}
	};

	public final long id, account_id;

	public UnreadItem(final JSONParcel in) {
		id = in.readLong("id");
		account_id = in.readLong("account_id");
	}

	public UnreadItem(final long id, final long account_id) {
		this.id = id;
		this.account_id = account_id;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof UnreadItem)) return false;
		final UnreadItem other = (UnreadItem) obj;
		if (account_id != other.account_id) return false;
		if (id != other.id) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (account_id ^ account_id >>> 32);
		result = prime * result + (int) (id ^ id >>> 32);
		return result;
	}

	@Override
	public String toString() {
		return "UnreadItem{id=" + id + ", account_id=" + account_id + "}";
	}

	@Override
	public void writeToParcel(final JSONParcel out) {
		out.writeLong("id", id);
		out.writeLong("account_id", account_id);
	}
}