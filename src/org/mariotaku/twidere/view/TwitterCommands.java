package org.mariotaku.twidere.view;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.io.File;

import org.mariotaku.twidere.model.SingleResponse;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;
import android.net.Uri;

public final class TwitterCommands {

	public static SingleResponse<Integer> updateProfileBannerImage(final Context context, final long account_id,
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
				return new SingleResponse<Integer>(account_id, value, null);
			} catch (final TwitterException e) {
				return new SingleResponse<Integer>(account_id, null, e);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		return new SingleResponse<Integer>(account_id, null, null);
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
				return new SingleResponse<User>(account_id, user, null);
			} catch (final TwitterException e) {
				return new SingleResponse<User>(account_id, null, e);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		return new SingleResponse<User>(account_id, null, null);
	}
}
