/*
 * 				Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import android.content.Context;
import android.net.Uri;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SingleResponse;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import java.io.File;
import java.util.List;

public class TwitterWrapper implements Constants {

	public static SingleResponse<Boolean> deleteProfileBannerImage(final Context context, final long account_id) {
		final Twitter twitter = getTwitterInstance(context, account_id, false);
		if (twitter == null) return new SingleResponse<Boolean>(false, null);
		try {
			twitter.removeProfileBannerImage();
			return new SingleResponse<Boolean>(true, null);
		} catch (final TwitterException e) {
			return new SingleResponse<Boolean>(false, e);
		}
	}

	public static SingleResponse<ParcelableUser> updateProfile(final Context context, final long account_id,
			final String name, final String url, final String location, final String description) {
		final Twitter twitter = getTwitterInstance(context, account_id, false);
		final boolean large_profile_image = context.getResources().getBoolean(R.bool.hires_profile_image);
		if (twitter != null) {
			try {
				final User user = twitter.updateProfile(name, url, location, description);
				return new SingleResponse<ParcelableUser>(new ParcelableUser(user, account_id, large_profile_image),
						null);
			} catch (final TwitterException e) {
				return new SingleResponse<ParcelableUser>(null, e);
			}
		}
		return SingleResponse.nullInstance();
	}

	public static SingleResponse<Boolean> updateProfileBannerImage(final Context context, final long account_id,
			final Uri image_uri, final boolean delete_image) {
		final Twitter twitter = getTwitterInstance(context, account_id, false);
		if (twitter != null && image_uri != null && "file".equals(image_uri.getScheme())) {
			try {
				final File file = new File(image_uri.getPath());
				twitter.updateProfileBannerImage(file);
				// Wait for 5 seconds, see
				// https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
				Thread.sleep(5000L);
				if (delete_image) {
					file.delete();
				}
				return new SingleResponse<Boolean>(true, null);
			} catch (final TwitterException e) {
				return new SingleResponse<Boolean>(false, e);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		return new SingleResponse<Boolean>(false, null);
	}

	public static SingleResponse<ParcelableUser> updateProfileImage(final Context context, final long account_id,
			final Uri image_uri, final boolean delete_image) {
		final Twitter twitter = getTwitterInstance(context, account_id, false);
		final boolean large_profile_image = context.getResources().getBoolean(R.bool.hires_profile_image);
		if (twitter != null && image_uri != null && "file".equals(image_uri.getScheme())) {
			try {
				final User user = twitter.updateProfileImage(new File(image_uri.getPath()));
				// Wait for 5 seconds, see
				// https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
				Thread.sleep(5000L);
				return new SingleResponse<ParcelableUser>(new ParcelableUser(user, account_id, large_profile_image),
						null);
			} catch (final TwitterException e) {
				return new SingleResponse<ParcelableUser>(null, e);
			} catch (final InterruptedException e) {
				return new SingleResponse<ParcelableUser>(null, e);
			}
		}
		return SingleResponse.nullInstance();
	}

	public static final class StatusListResponse extends TwitterListResponse<Status> {

		public final boolean truncated;

		public StatusListResponse(final long account_id, final Exception exception) {
			this(account_id, -1, -1, null, false, exception);
		}

		public StatusListResponse(final long account_id, final List<Status> list) {
			this(account_id, -1, -1, list, false, null);
		}

		public StatusListResponse(final long account_id, final long max_id, final long since_id,
				final int load_item_limit, final List<Status> list, final boolean truncated) {
			this(account_id, max_id, since_id, list, truncated, null);
		}

		StatusListResponse(final long account_id, final long max_id, final long since_id, final List<Status> list,
				final boolean truncated, final Exception exception) {
			super(account_id, max_id, since_id, list, exception);
			this.truncated = truncated;
		}

	}

	public static class TwitterListResponse<Data> extends ListResponse<Data> {

		public final long account_id, max_id, since_id;

		public TwitterListResponse(final long account_id, final Exception exception) {
			this(account_id, -1, -1, null, exception);
		}

		public TwitterListResponse(final long account_id, final long max_id, final long since_id, final List<Data> list) {
			this(account_id, max_id, since_id, list, null);
		}

		TwitterListResponse(final long account_id, final long max_id, final long since_id, final List<Data> list,
				final Exception exception) {
			super(list, exception);
			this.account_id = account_id;
			this.max_id = max_id;
			this.since_id = since_id;
		}

	}
}
