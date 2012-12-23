package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.io.File;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.model.SingleResponse;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;
import android.net.Uri;

public class TwitterWrapper implements Constants {

	public static TwitterSingleResponse<Integer> updateProfileBannerImage(final Context context, final long account_id,
			final Uri image_uri, final boolean delete_image) {
		final Twitter twitter = getTwitterInstance(context, account_id, false);
		if (twitter != null && image_uri != null && "file".equals(image_uri.getScheme())) {
			try {
				final File file = new File(image_uri.getPath());
				final int value = twitter.updateProfileBannerImage(file);
				if (value >= 200 && value <= 202) {
					// Wait for 5 seconds, see
					// https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
					Thread.sleep(5000L);
					if (delete_image) {
						file.delete();
					}
				}
				return new TwitterSingleResponse<Integer>(account_id, value, null);
			} catch (final TwitterException e) {
				return new TwitterSingleResponse<Integer>(account_id, null, e);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		return new TwitterSingleResponse<Integer>(account_id, null, null);
	}

	public static SingleResponse<User> updateProfileImage(final Context context, final long account_id,
			final Uri image_uri, final boolean delete_image) {
		final Twitter twitter = getTwitterInstance(context, account_id, false);
		if (twitter != null && image_uri != null && "file".equals(image_uri.getScheme())) {
			try {
				final User user = twitter.updateProfileImage(new File(image_uri.getPath()));
				// Wait for 5 seconds, see
				// https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
				Thread.sleep(5000L);
				return new TwitterSingleResponse<User>(account_id, user, null);
			} catch (final TwitterException e) {
				return new TwitterSingleResponse<User>(account_id, null, e);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		return new TwitterSingleResponse<User>(account_id, null, null);
	}

	public static final class StatusListResponse extends TwitterListResponse<Status> {

		public StatusListResponse(final long account_id, final List<Status> list) {
			super(account_id, -1, -1, -1, list, null);
		}

		public StatusListResponse(final long account_id, final long max_id, final long since_id,
				final int load_item_limit, final List<Status> list, final Exception exception) {
			super(account_id, max_id, since_id, load_item_limit, list, exception);
		}

	}

	public static class TwitterListResponse<Data> extends ListResponse<Data> {

		public final long account_id, max_id, since_id;
		public final int load_item_limit;

		public TwitterListResponse(final long account_id, final long max_id, final long since_id,
				final int load_item_limit, final List<Data> list, final Exception exception) {
			super(list, exception);
			this.account_id = account_id;
			this.max_id = max_id;
			this.since_id = since_id;
			this.load_item_limit = load_item_limit;
		}

	}

	public static final class TwitterSingleResponse<Data> extends SingleResponse<Data> {

		public final long account_id;

		public TwitterSingleResponse(final long account_id, final Data data, final Exception exception) {
			super(data, exception);
			this.account_id = account_id;
		}

	}
}
