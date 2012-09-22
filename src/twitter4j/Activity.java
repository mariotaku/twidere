package twitter4j;

import java.io.Serializable;
import java.util.Date;

public interface Activity extends TwitterResponse, Comparable<Activity>, Serializable {

	public Action getAction();

	public Date getCreatedAt();

	public long getMaxPosition();

	public long getMinPosition();

	public User[] getSources();

	public int getSourcesSize();

	public Status[] getTargetObjects();

	public int getTargetObjectsSize();

	public int getTargetsSize();

	public Status[] getTargetStatuses();

	public User[] getTargetUsers();

	public static enum Action implements Serializable {
		FAVORITE(0x1), FOLLOW(0x2), MENTION(0x3), REPLY(0x4), RETWEET(0x5);

		public final static int ACTION_FAVORITE = 0x01;
		public final static int ACTION_FOLLOW = 0x02;
		public final static int ACTION_MENTION = 0x03;
		public final static int ACTION_REPLY = 0x04;
		public final static int ACTION_RETWEET = 0x05;

		private final int actionId;

		private Action(int action) {
			actionId = action;
		}

		public int getActionId() {
			return actionId;
		}

		public static Action fromString(String string) {
			if ("favorite".equalsIgnoreCase(string)) return FAVORITE;
			if ("follow".equalsIgnoreCase(string)) return FOLLOW;
			if ("mention".equalsIgnoreCase(string)) return MENTION;
			if ("reply".equalsIgnoreCase(string)) return REPLY;
			if ("retweet".equalsIgnoreCase(string)) return RETWEET;
			throw new IllegalArgumentException("Unknown action " + string);
		}
	}
}
