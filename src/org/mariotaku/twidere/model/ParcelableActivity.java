package org.mariotaku.twidere.model;
import java.util.Date;
import twitter4j.Activity;
import org.mariotaku.jsonserializer.JSONParcelable;
import org.mariotaku.jsonserializer.JSONParcel;

public class ParcelableActivity implements Comparable<ParcelableActivity>, JSONParcelable {

	public static final JSONParcelable.Creator<ParcelableActivity> JSON_CREATOR = new JSONParcelable.Creator<ParcelableActivity>() {
		@Override
		public ParcelableActivity createFromParcel(final JSONParcel in) {
			return new ParcelableActivity(in);
		}

		@Override
		public ParcelableActivity[] newArray(final int size) {
			return new ParcelableActivity[size];
		}
	};

	public final static int ACTION_FAVORITE = Activity.Action.ACTION_FAVORITE;
	public final static int ACTION_FOLLOW = Activity.Action.ACTION_FOLLOW;
	public final static int ACTION_MENTION = Activity.Action.ACTION_MENTION;
	public final static int ACTION_REPLY = Activity.Action.ACTION_REPLY;
	public final static int ACTION_RETWEET = Activity.Action.ACTION_RETWEET;
	public final static int ACTION_LIST_MEMBER_ADDED = Activity.Action.ACTION_LIST_MEMBER_ADDED;
	public final static int ACTION_LIST_CREATED = Activity.Action.ACTION_LIST_CREATED;

	public final long account_id, activity_timestamp, max_position, min_position;
	public final int action;
	
	public final ParcelableUser[] sources;
	public final ParcelableUser[] target_users;
	public final ParcelableStatus[] target_statuses;
	public final ParcelableUserList[] target_user_lists;

	public final ParcelableUserList[] target_object_user_lists;
	public final ParcelableStatus[] target_object_statuses;	
	
	public ParcelableActivity(final Activity activity, final long account_id, final boolean large_profile_image) {
		this.account_id = account_id;
		this.activity_timestamp = getTime(activity.getCreatedAt());
		this.action = activity.getAction().getActionId();
		this.max_position = activity.getMaxPosition();
		this.min_position = activity.getMinPosition();
		final int sources_size = activity.getSourcesSize();
		sources = new ParcelableUser[sources_size];
		for (int i = 0; i < sources_size; i++) {
			sources[i] = new ParcelableUser(activity.getSources()[i], account_id, large_profile_image);
		}
		final int targets_size = activity.getTargetsSize();
		if (action == ACTION_FOLLOW || action == ACTION_MENTION || action == ACTION_LIST_MEMBER_ADDED) {
			target_users = new ParcelableUser[targets_size];
			target_statuses = null;
			target_user_lists = null;
			for (int i = 0; i < targets_size; i++) {
				target_users[i] = new ParcelableUser(activity.getTargetUsers()[i], account_id, large_profile_image);
			}
		} else if (action == ACTION_LIST_CREATED) {
			target_user_lists = new ParcelableUserList[targets_size];
			target_statuses = null;
			target_users = null;
			for (int i = 0; i < targets_size; i++) {
				target_user_lists[i] = new ParcelableUserList(activity.getTargetUserLists()[i], account_id, large_profile_image);
			}
		} else {
			target_statuses = new ParcelableStatus[targets_size];
			target_users = null;
			target_user_lists = null;
			for (int i = 0; i < targets_size; i++) {
				target_statuses[i] = new ParcelableStatus(activity.getTargetStatuses()[i], account_id, false, large_profile_image);
			}
		}
		final int target_objects_size = activity.getTargetObjectsSize();
		if (action == ACTION_LIST_MEMBER_ADDED) {
			target_object_user_lists = new ParcelableUserList[target_objects_size];
			target_object_statuses = null;
			for (int i = 0; i < target_objects_size; i++) {
				target_object_user_lists[i] = new ParcelableUserList(activity.getTargetObjectUserLists()[i], account_id, large_profile_image);
			}
		} else if (action == ACTION_LIST_CREATED) {
			target_object_user_lists = null;
			target_object_statuses = null;
		} else {
			target_object_statuses = new ParcelableStatus[target_objects_size];
			target_object_user_lists = null;
			for (int i = 0; i < target_objects_size; i++) {
				target_object_statuses[i] = new ParcelableStatus(activity.getTargetObjectStatuses()[i], account_id, false, large_profile_image);
			}
		}
	}

	public ParcelableActivity(final JSONParcel in) {
		account_id = in.readLong("account_id");
		activity_timestamp = in.readLong("activity_timestamp");
		max_position = in.readLong("max_position");
		min_position = in.readLong("min_position");
		action = in.readInt("action");
		sources = in.readParcelableArray("sources", ParcelableUser.JSON_CREATOR);
		target_users = in.readParcelableArray("target_users", ParcelableUser.JSON_CREATOR);
		target_statuses = in.readParcelableArray("target_statuses", ParcelableStatus.JSON_CREATOR);
		target_user_lists = in.readParcelableArray("target_user_lists", ParcelableUserList.JSON_CREATOR);
		target_object_user_lists = in.readParcelableArray("target_object_user_lists", ParcelableUserList.JSON_CREATOR);
		target_object_statuses = in.readParcelableArray("target_object_statuses", ParcelableStatus.JSON_CREATOR);
	}

	@Override
	public int compareTo(final ParcelableActivity another) {
		if (another == null) return 0;
		final long delta = another.activity_timestamp - activity_timestamp;
		if (delta < Integer.MIN_VALUE) return Integer.MIN_VALUE;
		if (delta > Integer.MAX_VALUE) return Integer.MAX_VALUE;
		return (int) delta;
	}
	
	@Override
	public boolean equals(final Object that) {
		if (!(that instanceof ParcelableActivity)) return false;
		final ParcelableActivity activity = (ParcelableActivity) that;
		return max_position == activity.max_position && min_position == activity.min_position;
	}

	private static long getTime(final Date date) {
		return date != null ? date.getTime() : 0;
	}

	@Override
	public void writeToParcel(final JSONParcel out) {
		out.writeLong("account_id", account_id);
		out.writeLong("activity_timestamp", activity_timestamp);
		out.writeLong("max_position", max_position);
		out.writeLong("min_position", min_position);
		out.writeInt("action", action);
		out.writeParcelableArray("sources", sources);
		out.writeParcelableArray("target_users", target_users);
		out.writeParcelableArray("target_statuses", target_statuses);
		out.writeParcelableArray("target_user_lists", target_user_lists);
		out.writeParcelableArray("target_object_user_lists", target_object_user_lists);
		out.writeParcelableArray("target_object_statuses", target_object_statuses);
	}
	
}
