/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.service;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.ContentValuesCreator.makeDirectMessageContentValues;
import static org.mariotaku.twidere.util.ContentValuesCreator.makeDirectMessageDraftContentValues;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;
import static org.mariotaku.twidere.util.Utils.getImageUploadStatus;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.twitter.Extractor;
import com.twitter.Validator;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.Main2Activity;
import org.mariotaku.twidere.activity.MainActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.provider.TweetStore.CachedHashtags;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.ImageUploaderInterface;
import org.mariotaku.twidere.util.ListUtils;
import org.mariotaku.twidere.util.MessagesManager;
import org.mariotaku.twidere.util.TweetShortenerInterface;
import org.mariotaku.twidere.util.TwitterErrorCodes;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.io.ContentLengthInputStream;
import org.mariotaku.twidere.util.io.ContentLengthInputStream.ReadListener;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BackgroundOperationService extends IntentService implements Constants {

	private final Validator mValidator = new Validator();
	private final Extractor extractor = new Extractor();

	private Handler mHandler;
	private SharedPreferences mPreferences;
	private ContentResolver mResolver;
	private NotificationManager mNotificationManager;
	private Builder mBuilder;
	private AsyncTwitterWrapper mTwitter;
	private MessagesManager mMessagesManager;

	private ImageUploaderInterface mUploader;
	private TweetShortenerInterface mShortener;

	private boolean mUseUploader, mUseShortener;
	private boolean mLargeProfileImage;

	public BackgroundOperationService() {
		super("background_operation");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		final TwidereApplication app = TwidereApplication.getInstance(this);
		mLargeProfileImage = getResources().getBoolean(R.bool.hires_profile_image);
		mHandler = new Handler();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		mResolver = getContentResolver();
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mTwitter = app.getTwitterWrapper();
		mBuilder = new NotificationCompat.Builder(this);
		mMessagesManager = app.getMessagesManager();
		final String uploader_component = mPreferences.getString(KEY_IMAGE_UPLOADER, null);
		final String shortener_component = mPreferences.getString(KEY_TWEET_SHORTENER, null);
		mUseUploader = !isEmpty(uploader_component);
		mUseShortener = !isEmpty(shortener_component);
		mUploader = mUseUploader ? ImageUploaderInterface.getInstance(app, uploader_component) : null;
		mShortener = mUseShortener ? TweetShortenerInterface.getInstance(app, shortener_component) : null;
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	public void showErrorMessage(final CharSequence message, final boolean long_message) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mMessagesManager.showErrorMessage(message, long_message);
			}
		});
	}

	public void showErrorMessage(final int action_res, final Exception e, final boolean long_message) {

		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mMessagesManager.showErrorMessage(action_res, e, long_message);
			}
		});
	}

	public void showErrorMessage(final int action_res, final String message, final boolean long_message) {

		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mMessagesManager.showErrorMessage(action_res, message, long_message);
			}
		});
	}

	public void showOkMessage(final int message_res, final boolean long_message) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mMessagesManager.showOkMessage(message_res, long_message);
			}
		});
	}

	@Override
	protected void onHandleIntent(final Intent intent) {
		if (intent == null) return;
		final String action = intent.getAction();
		if (INTENT_ACTION_UPDATE_STATUS.equals(action)) {
			handleUpdateStatusIntent(intent);
		} else if (INTENT_ACTION_SEND_DIRECT_MESSAGE.equals(action)) {
			handleSendDirectMessageIntent(intent);
		}
	}

	private Notification buildNotification(final String title, final String message, final int icon,
			final Intent content_intent, final Intent delete_intent) {
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setTicker(message);
		builder.setContentTitle(title);
		builder.setContentText(message);
		builder.setAutoCancel(true);
		builder.setWhen(System.currentTimeMillis());
		builder.setSmallIcon(icon);
		if (delete_intent != null) {
			builder.setDeleteIntent(PendingIntent.getBroadcast(this, 0, delete_intent,
					PendingIntent.FLAG_UPDATE_CURRENT));
		}
		if (content_intent != null) {
			content_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			builder.setContentIntent(PendingIntent.getActivity(this, 0, content_intent,
					PendingIntent.FLAG_UPDATE_CURRENT));
		}
		// final Uri defRingtone =
		// RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		// final String path =
		// mPreferences.getString(PREFERENCE_KEY_NOTIFICATION_RINGTONE, "");
		// builder.setSound(isEmpty(path) ? defRingtone : Uri.parse(path),
		// Notification.STREAM_DEFAULT);
		// builder.setLights(HOLO_BLUE_LIGHT, 1000, 2000);
		// builder.setDefaults(Notification.DEFAULT_VIBRATE);
		return builder.build();
	}

	private void handleSendDirectMessageIntent(final Intent intent) {
		final long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
		final long recipientId = intent.getLongExtra(EXTRA_RECIPIENT_ID, -1);
		final String text = intent.getStringExtra(EXTRA_TEXT);
		if (accountId <= 0 || recipientId <= 0 || isEmpty(text)) return;
		final String title = getString(R.string.sending_direct_message);
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.ic_stat_send);
		builder.setProgress(100, 0, true);
		builder.setTicker(title);
		builder.setContentTitle(title);
		builder.setContentText(text);
		final Notification notification = builder.build();
		startForeground(NOTIFICATION_ID_SEND_DIRECT_MESSAGE, notification);
		final SingleResponse<ParcelableDirectMessage> result = sendDirectMessage(accountId, recipientId, text);
		if (result.data != null && result.data.id > 0) {
			final ContentValues values = makeDirectMessageContentValues(result.data);
			final String delete_where = DirectMessages.ACCOUNT_ID + " = " + accountId + " AND "
					+ DirectMessages.MESSAGE_ID + " = " + result.data.id;
			mResolver.delete(DirectMessages.Outbox.CONTENT_URI, delete_where, null);
			mResolver.insert(DirectMessages.Outbox.CONTENT_URI, values);
			showOkMessage(R.string.direct_message_sent, false);
		} else {
			final ContentValues values = makeDirectMessageDraftContentValues(accountId, recipientId, text);
			mResolver.insert(Drafts.CONTENT_URI, values);
			showErrorMessage(R.string.action_sending_direct_message, result.exception, true);
		}
		stopForeground(true);
	}

	private void handleUpdateStatusIntent(final Intent intent) {
		final ParcelableStatusUpdate status = intent.getParcelableExtra(EXTRA_STATUS);
		final Parcelable[] status_parcelables = intent.getParcelableArrayExtra(EXTRA_STATUSES);
		final ParcelableStatusUpdate[] statuses;
		if (status_parcelables != null) {
			statuses = new ParcelableStatusUpdate[status_parcelables.length];
			for (int i = 0, j = status_parcelables.length; i < j; i++) {
				statuses[i] = (ParcelableStatusUpdate) status_parcelables[i];
			}
		} else if (status != null) {
			statuses = new ParcelableStatusUpdate[1];
			statuses[0] = status;
		} else
			return;
		startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotificaion(0, null));
		for (final ParcelableStatusUpdate item : statuses) {
			updateUpdateStatusNotificaion(0, item);
			final List<SingleResponse<ParcelableStatus>> result = updateStatus(item);
			boolean failed = false;
			Exception exception = null;
			final List<Long> failed_account_ids = ListUtils.fromArray(item.account_ids);

			for (final SingleResponse<ParcelableStatus> response : result) {
				if (response.data == null) {
					failed = true;
					if (exception == null) {
						exception = response.exception;
					}
				} else if (response.data.account_id > 0) {
					failed_account_ids.remove(response.data.account_id);
				}
			}
			if (result.isEmpty()) {
				saveDrafts(item, failed_account_ids);
				showErrorMessage(R.string.action_updating_status, getString(R.string.no_account_selected), false);
			} else if (failed) {
				// If the status is a duplicate, there's no need to save it to
				// drafts.
				if (exception instanceof TwitterException
						&& ((TwitterException) exception).getErrorCode() == TwitterErrorCodes.STATUS_IS_DUPLICATE) {
					showErrorMessage(getString(R.string.status_is_duplicate), false);
				} else {
					saveDrafts(item, failed_account_ids);
					showErrorMessage(R.string.action_updating_status, exception, true);
				}
			} else {
				showOkMessage(R.string.status_updated, false);
				if (item.media_uri != null) {
					final String path = getImagePathFromUri(this, item.media_uri);
					if (path != null) {
						new File(path).delete();
					}
				}
			}
			if (mPreferences.getBoolean(KEY_REFRESH_AFTER_TWEET, false)) {
				mTwitter.refreshAll();
			}
		}
		stopForeground(true);
	}

	private void saveDrafts(final ParcelableStatusUpdate status, final List<Long> account_ids) {
		final ContentValues values = ContentValuesCreator.makeStatusDraftContentValues(status,
				ArrayUtils.fromList(account_ids));
		mResolver.insert(Drafts.CONTENT_URI, values);
		final String title = getString(R.string.status_not_updated);
		final String message = getString(R.string.status_not_updated_summary);
		final Intent intent = new Intent(INTENT_ACTION_DRAFTS);
		final Notification notification = buildNotification(title, message, R.drawable.ic_stat_twitter, intent, null);
		mNotificationManager.notify(NOTIFICATION_ID_DRAFTS, notification);
	}

	private SingleResponse<ParcelableDirectMessage> sendDirectMessage(final long accountId, final long recipientId,
			final String text) {
		final Twitter twitter = getTwitterInstance(this, accountId, true, true);
		try {
			final ParcelableDirectMessage directMessage = new ParcelableDirectMessage(twitter.sendDirectMessage(
					recipientId, text), accountId, true, mLargeProfileImage);
			return SingleResponse.withData(directMessage);
		} catch (final TwitterException e) {
			return SingleResponse.withException(e);
		}
	}

	private List<SingleResponse<ParcelableStatus>> updateStatus(final ParcelableStatusUpdate pstatus) {
		final ArrayList<ContentValues> hashtag_values = new ArrayList<ContentValues>();
		final Collection<String> hashtags = extractor.extractHashtags(pstatus.text);
		for (final String hashtag : hashtags) {
			final ContentValues values = new ContentValues();
			values.put(CachedHashtags.NAME, hashtag);
			hashtag_values.add(values);
		}
		final boolean hasEasterEggTriggerText = pstatus.text.contains(EASTER_EGG_TRIGGER_TEXT);
		final boolean hasEasterEggRestoreText = pstatus.text.contains(EASTER_EGG_RESTORE_TEXT_PART1)
				&& pstatus.text.contains(EASTER_EGG_RESTORE_TEXT_PART2)
				&& pstatus.text.contains(EASTER_EGG_RESTORE_TEXT_PART3);
		boolean mentioned_hondajojo = false;
		mResolver.bulkInsert(CachedHashtags.CONTENT_URI,
				hashtag_values.toArray(new ContentValues[hashtag_values.size()]));

		final List<SingleResponse<ParcelableStatus>> results = new ArrayList<SingleResponse<ParcelableStatus>>();

		if (pstatus.account_ids.length == 0) return Collections.emptyList();

		try {
			if (mUseUploader && mUploader == null) throw new ImageUploaderNotFoundException(this);
			if (mUseShortener && mShortener == null) throw new TweetShortenerNotFoundException(this);

			final String imagePath = getImagePathFromUri(this, pstatus.media_uri);
			final File imageFile = imagePath != null ? new File(imagePath) : null;

			final Uri uploadResultUri;
			try {
				if (mUploader != null) {
					mUploader.waitForService();
				}
				uploadResultUri = imageFile != null && imageFile.exists() && mUploader != null ? mUploader.upload(
						Uri.fromFile(imageFile), pstatus.text) : null;
			} catch (final Exception e) {
				throw new ImageUploadException(this);
			}
			if (mUseUploader && imageFile != null && imageFile.exists() && uploadResultUri == null)
				throw new ImageUploadException(this);

			final String unshortened_content = mUseUploader && uploadResultUri != null ? getImageUploadStatus(this,
					uploadResultUri.toString(), pstatus.text) : pstatus.text;

			final boolean should_shorten = mValidator.getTweetLength(unshortened_content) > Validator.MAX_TWEET_LENGTH;
			final String screen_name = getAccountScreenName(this, pstatus.account_ids[0]);
			final String shortened_content;
			try {
				if (mShortener != null) {
					mShortener.waitForService();
				}
				shortened_content = should_shorten && mUseShortener ? mShortener.shorten(unshortened_content,
						screen_name, pstatus.in_reply_to_status_id) : null;
			} catch (final Exception e) {
				throw new TweetShortenException(this);
			}

			if (should_shorten) {
				if (!mUseShortener)
					throw new StatusTooLongException(this);
				else if (unshortened_content == null) throw new TweetShortenException(this);
			}
			if (!mUseUploader && imageFile != null && imageFile.exists()) {
				Utils.downscaleImageIfNeeded(imageFile, 95);
			}
			for (final long account_id : pstatus.account_ids) {
				final StatusUpdate status = new StatusUpdate(should_shorten && mUseShortener ? shortened_content
						: unshortened_content);
				status.setInReplyToStatusId(pstatus.in_reply_to_status_id);
				if (pstatus.location != null) {
					status.setLocation(ParcelableLocation.toGeoLocation(pstatus.location));
				}
				if (!mUseUploader && imageFile != null && imageFile.exists()) {
					try {
						final ContentLengthInputStream is = new ContentLengthInputStream(imageFile);
						is.setReadListener(new ReadListener() {

							int percent;

							@Override
							public void onRead(final long length, final long position) {
								final int percent = length > 0 ? (int) (position * 100 / length) : 0;
								if (this.percent != percent) {
									mNotificationManager.notify(NOTIFICATION_ID_UPDATE_STATUS,
											updateUpdateStatusNotificaion(percent, pstatus));
								}
								this.percent = percent;
							}
						});
						status.setMedia(imageFile.getName(), is);
					} catch (final FileNotFoundException e) {
						status.setMedia(imageFile);
					}
				}
				status.setPossiblySensitive(pstatus.is_possibly_sensitive);

				final Twitter twitter = getTwitterInstance(this, account_id, true, true);
				if (twitter == null) {
					results.add(new SingleResponse<ParcelableStatus>(null, new NullPointerException()));
					continue;
				}
				try {
					final Status twitter_result = twitter.updateStatus(status);
					if (!mentioned_hondajojo) {
						final UserMentionEntity[] entities = twitter_result.getUserMentionEntities();
						if (entities != null) {
							for (final UserMentionEntity entity : entities) {
								if (entity.getId() == HONDAJOJO_ID) {
									mentioned_hondajojo = true;
								}
							}
						} else {
							mentioned_hondajojo = pstatus.text.contains(HONDAJOJO_SCREEN_NAME);
						}
					}
					final ParcelableStatus result = new ParcelableStatus(twitter_result, account_id, false, false);
					results.add(new SingleResponse<ParcelableStatus>(result, null));
				} catch (final TwitterException e) {
					final SingleResponse<ParcelableStatus> response = SingleResponse.withException(e);
					results.add(response);
				}
			}
		} catch (final UpdateStatusException e) {
			final SingleResponse<ParcelableStatus> response = SingleResponse.withException(e);
			results.add(response);
		}
		if (mentioned_hondajojo) {
			final PackageManager pm = getPackageManager();
			final ComponentName main = new ComponentName(this, MainActivity.class);
			final ComponentName main2 = new ComponentName(this, Main2Activity.class);
			if (hasEasterEggTriggerText) {
				pm.setComponentEnabledSetting(main, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
						PackageManager.DONT_KILL_APP);
				pm.setComponentEnabledSetting(main2, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
						PackageManager.DONT_KILL_APP);
			} else if (hasEasterEggRestoreText) {
				pm.setComponentEnabledSetting(main, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
						PackageManager.DONT_KILL_APP);
				pm.setComponentEnabledSetting(main2, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
						PackageManager.DONT_KILL_APP);
			}
		}
		return results;
	}

	private Notification updateUpdateStatusNotificaion(final int progress, final ParcelableStatusUpdate status) {
		mBuilder.setContentTitle(getString(R.string.updating_status_notification));
		if (status != null) {
			mBuilder.setContentText(status.text);
		}
		mBuilder.setSmallIcon(R.drawable.ic_stat_send);
		mBuilder.setProgress(100, progress, progress >= 100 || progress <= 0);
		final Notification notification = mBuilder.build();
		mNotificationManager.notify(NOTIFICATION_ID_UPDATE_STATUS, notification);
		return notification;
	}

	static class ImageUploaderNotFoundException extends UpdateStatusException {
		private static final long serialVersionUID = 1041685850011544106L;

		public ImageUploaderNotFoundException(final Context context) {
			super(context, R.string.error_message_image_uploader_not_found);
		}
	}

	static class ImageUploadException extends UpdateStatusException {
		private static final long serialVersionUID = 8596614696393917525L;

		public ImageUploadException(final Context context) {
			super(context, R.string.error_message_image_upload_failed);
		}
	}

	static class StatusTooLongException extends UpdateStatusException {
		private static final long serialVersionUID = -6469920130856384219L;

		public StatusTooLongException(final Context context) {
			super(context, R.string.error_message_status_too_long);
		}
	}

	static class TweetShortenerNotFoundException extends UpdateStatusException {
		private static final long serialVersionUID = -7262474256595304566L;

		public TweetShortenerNotFoundException(final Context context) {
			super(context, R.string.error_message_tweet_shortener_not_found);
		}
	}

	static class TweetShortenException extends UpdateStatusException {
		private static final long serialVersionUID = 3075877185536740034L;

		public TweetShortenException(final Context context) {
			super(context, R.string.error_message_tweet_shorten_failed);
		}
	}

	static class UpdateStatusException extends Exception {
		private static final long serialVersionUID = -1267218921727097910L;

		public UpdateStatusException(final Context context, final int message) {
			super(context.getString(message));
		}
	}
}
