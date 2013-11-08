package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;
import org.mariotaku.jsonserializer.JSONSerializer;

import twitter4j.UserMentionEntity;

public class ParcelableUserMention implements Parcelable, JSONParcelable {

	public static final Parcelable.Creator<ParcelableUserMention> CREATOR = new Parcelable.Creator<ParcelableUserMention>() {
		@Override
		public ParcelableUserMention createFromParcel(final Parcel in) {
			return new ParcelableUserMention(in);
		}

		@Override
		public ParcelableUserMention[] newArray(final int size) {
			return new ParcelableUserMention[size];
		}
	};

	public static final JSONParcelable.Creator<ParcelableUserMention> JSON_CREATOR = new JSONParcelable.Creator<ParcelableUserMention>() {
		@Override
		public ParcelableUserMention createFromParcel(final JSONParcel in) {
			return new ParcelableUserMention(in);
		}

		@Override
		public ParcelableUserMention[] newArray(final int size) {
			return new ParcelableUserMention[size];
		}
	};

	public long id;
	public String name, screen_name;

	public ParcelableUserMention(final JSONParcel in) {
		id = in.readLong("id");
		name = in.readString("name");
		screen_name = in.readString("screen_name");
	}

	public ParcelableUserMention(final Parcel in) {
		id = in.readLong();
		name = in.readString();
		screen_name = in.readString();
	}

	public ParcelableUserMention(final UserMentionEntity entity) {
		id = entity.getId();
		name = entity.getName();
		screen_name = entity.getScreenName();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ParcelableUserMention)) return false;
		final ParcelableUserMention other = (ParcelableUserMention) obj;
		if (id != other.id) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ id >>> 32);
		return result;
	}

	@Override
	public String toString() {
		return "ParcelableUserMention{id=" + id + ", name=" + name + ", screen_name=" + screen_name + "}";
	}

	@Override
	public void writeToParcel(final JSONParcel out) {
		out.writeLong("id", id);
		out.writeString("name", name);
		out.writeString("screen_name", screen_name);
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(screen_name);
	}

	public static ParcelableUserMention[] fromJSONString(final String json) {
		if (TextUtils.isEmpty(json)) return null;
		try {
			return JSONSerializer.arrayFromJSON(JSON_CREATOR, new JSONArray(json));
		} catch (final JSONException e) {
			return null;
		}
	}

	public static ParcelableUserMention[] fromUserMentionEntities(final UserMentionEntity[] entities) {
		if (entities == null) return null;
		final ParcelableUserMention[] mentions = new ParcelableUserMention[entities.length];
		for (int i = 0, j = entities.length; i < j; i++) {
			mentions[i] = new ParcelableUserMention(entities[i]);
		}
		return mentions;
	}
}