package org.mariotaku.twidere.model;
import java.util.Date;
import twitter4j.Activity;

public class ParcelableActivity implements Comparable<ParcelableActivity> {

	public final static int ACTION_FAVORITE = Activity.Action.ACTION_FAVORITE;
	public final static int ACTION_FOLLOW = Activity.Action.ACTION_FOLLOW;
	public final static int ACTION_MENTION = Activity.Action.ACTION_MENTION;
	public final static int ACTION_REPLY = Activity.Action.ACTION_REPLY;
	public final static int ACTION_RETWEET = Activity.Action.ACTION_RETWEET;
	public final static int ACTION_LIST_MEMBER_ADDED = Activity.Action.ACTION_LIST_MEMBER_ADDED;

	public final long account_id, activity_timestamp;
	public final int action;
	
	public final ParcelableUser[] sources, target_users;
	public final ParcelableStatus[] target_statuses;

	public final ParcelableUserList[] target_object_user_lists;
	public final ParcelableStatus[] target_object_statuses;	
	
	public ParcelableActivity(final Activity activity, final long account_id, final boolean large_profile_image) {
		this.account_id = account_id;
		this.activity_timestamp = getTime(activity.getCreatedAt());
		this.action = activity.getAction().getActionId();
		final int sources_size = activity.getSourcesSize();
		sources = new ParcelableUser[sources_size];
		for (int i = 0; i < sources_size; i++) {
			sources[i] = new ParcelableUser(activity.getSources()[i], account_id, large_profile_image);
		}
		final int targets_size = activity.getTargetsSize();
		if (action == ACTION_FOLLOW || action == ACTION_MENTION || action == ACTION_LIST_MEMBER_ADDED) {
			target_users = new ParcelableUser[targets_size];
			target_statuses = null;
			for (int i = 0; i < targets_size; i++) {
				target_users[i] = new ParcelableUser(activity.getTargetUsers()[i], account_id, large_profile_image);
			}
		} else {
			target_statuses = new ParcelableStatus[targets_size];
			target_users = null;
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
		} else {
			target_object_statuses = new ParcelableStatus[target_objects_size];
			target_object_user_lists = null;
			for (int i = 0; i < target_objects_size; i++) {
				target_object_statuses[i] = new ParcelableStatus(activity.getTargetObjectStatuses()[i], account_id, false, large_profile_image);
			}
		}
	}

	@Override
	public int compareTo(final ParcelableActivity another) {
		if (another == null) return 0;
		final long delta = activity_timestamp - another.activity_timestamp;
		if (delta < Integer.MIN_VALUE) return Integer.MIN_VALUE;
		if (delta > Integer.MAX_VALUE) return Integer.MAX_VALUE;
		return (int) delta;
	}

	private static long getTime(final Date date) {
		return date != null ? date.getTime() : 0;
	}

}
