package org.mariotaku.twidere.service;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;
import static org.mariotaku.twidere.util.Utils.getImageUploadStatus;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.twitter.Extractor;
import com.twitter.Validator;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.provider.TweetStore.CachedHashtags;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentLengthInputStream;
import org.mariotaku.twidere.util.ContentLengthInputStream.ReadListener;
import org.mariotaku.twidere.util.ImageUploaderInterface;
import org.mariotaku.twidere.util.ListUtils;
import org.mariotaku.twidere.util.MessagesManager;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.TweetShortenerInterface;
import org.mariotaku.twidere.util.TwitterErrorCodes;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdateStatusService extends IntentService implements Constants {

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

	public UpdateStatusService() {
		super("update_status");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		final TwidereApplication app = TwidereApplication.getInstance(this);
		mHandler = new Handler();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		mResolver = getContentResolver();
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mTwitter = app.getTwitterWrapper();
		mBuilder = new NotificationCompat.Builder(this);
		mMessagesManager = app.getMessagesManager();
		final String uploader_component = mPreferences.getString(PREFERENCE_KEY_IMAGE_UPLOADER, null);
		final String shortener_component = mPreferences.getString(PREFERENCE_KEY_TWEET_SHORTENER, null);
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
		handleUpdateStatusIntent(intent);
	}

	protected List<SingleResponse<ParcelableStatus>> updateStatus(final ParcelableStatusUpdate pstatus) {
		final ArrayList<ContentValues> hashtag_values = new ArrayList<ContentValues>();
		final List<String> hashtags = extractor.extractHashtags(pstatus.content);
		for (final String hashtag : hashtags) {
			final ContentValues values = new ContentValues();
			values.put(CachedHashtags.NAME, hashtag);
			hashtag_values.add(values);
		}
		mResolver.bulkInsert(CachedHashtags.CONTENT_URI,
				hashtag_values.toArray(new ContentValues[hashtag_values.size()]));

		final List<SingleResponse<ParcelableStatus>> results = new ArrayList<SingleResponse<ParcelableStatus>>();

		if (pstatus.account_ids.length == 0) return Collections.emptyList();

		try {
			if (mUseUploader && mUploader == null) throw new ImageUploaderNotFoundException(this);
			if (mUseShortener && mShortener == null) throw new TweetShortenerNotFoundException(this);

			final String image_path = getImagePathFromUri(this, pstatus.image_uri);
			final File image_file = image_path != null ? new File(image_path) : null;

			final Uri upload_result_uri;
			try {
				if (mUploader != null) {
					mUploader.waitForService();
				}
				upload_result_uri = image_file != null && image_file.exists() && mUploader != null ? mUploader.upload(
						Uri.fromFile(image_file), pstatus.content) : null;
			} catch (final Exception e) {
				throw new ImageUploadException(this);
			}
			if (mUseUploader && image_file != null && image_file.exists() && upload_result_uri == null)
				throw new ImageUploadException(this);

			final String unshortened_content = mUseUploader && upload_result_uri != null ? getImageUploadStatus(this,
					upload_result_uri.toString(), pstatus.content) : pstatus.content;

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

			for (final long account_id : pstatus.account_ids) {
				final StatusUpdate status = new StatusUpdate(should_shorten && mUseShortener ? shortened_content
						: unshortened_content);
				status.setInReplyToStatusId(pstatus.in_reply_to_status_id);
				if (pstatus.location != null) {
					status.setLocation(ParcelableLocation.toGeoLocation(pstatus.location));
				}
				if (!mUseUploader && image_file != null && image_file.exists()) {
					try {
						final ContentLengthInputStream is = new ContentLengthInputStream(image_file);
						is.setReadListener(new ReadListener() {

							int percent;

							@Override
							public void onRead(final int length, final int available) {
								final int percent = length > 0 ? (length - available) * 100 / length : 0;
								if (this.percent != percent) {
									mNotificationManager.notify(NOTIFICATION_ID_UPDATE_STATUS,
											updateUpdateStatusNotificaion(percent, pstatus));
								}
								this.percent = percent;
							}
						});
						status.setMedia(image_file.getAbsolutePath(), is);
					} catch (final FileNotFoundException e) {
						status.setMedia(image_file);
					}
				}
				status.setPossiblySensitive(pstatus.is_possibly_sensitive);

				final Twitter twitter = getTwitterInstance(this, account_id, false, true);
				if (twitter == null) {
					results.add(new SingleResponse<ParcelableStatus>(null, new NullPointerException()));
					continue;
				}
				try {
					final ParcelableStatus result = new ParcelableStatus(twitter.updateStatus(status), account_id,
							false, false);
					results.add(new SingleResponse<ParcelableStatus>(result, null));
				} catch (final TwitterException e) {
					final SingleResponse<ParcelableStatus> response = SingleResponse.exceptionOnly(e);
					results.add(response);
				}
			}
		} catch (final UpdateStatusException e) {
			final SingleResponse<ParcelableStatus> response = SingleResponse.exceptionOnly(e);
			results.add(response);
		}
		return results;
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
		int defaults = 0;
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_SOUND, false)) {
			final Uri def_ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			final String path = mPreferences.getString(PREFERENCE_KEY_NOTIFICATION_RINGTONE, "");
			builder.setSound(isEmpty(path) ? def_ringtone : Uri.parse(path), Notification.STREAM_DEFAULT);
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_VIBRATION, false)) {
			defaults |= Notification.DEFAULT_VIBRATE;
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_LIGHTS, false)) {
			final int color_def = getResources().getColor(android.R.color.holo_blue_dark);
			final int color = mPreferences.getInt(PREFERENCE_KEY_NOTIFICATION_LIGHT_COLOR, color_def);
			builder.setLights(color, 1000, 2000);
		}
		builder.setDefaults(defaults);
		return builder.build();
	}

	private void handleUpdateStatusIntent(final Intent intent) {
		final ParcelableStatusUpdate status = intent.getParcelableExtra(EXTRA_STATUS);
		if (status == null) return;
		startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotificaion(0, status));
		final List<SingleResponse<ParcelableStatus>> result = updateStatus(status);
		boolean failed = false;
		Exception exception = null;
		final List<Long> failed_account_ids = ListUtils.fromArray(status.account_ids);

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
			saveDrafts(status, failed_account_ids);
			showErrorMessage(R.string.updating_status, getString(R.string.no_account_selected), false);
		} else if (failed) {
			// If the status is a duplicate, there's no need to save it to
			// drafts.
			if (exception instanceof TwitterException
					&& ((TwitterException) exception).getErrorCode() == TwitterErrorCodes.STATUS_IS_DUPLICATE) {
				showErrorMessage(getString(R.string.status_is_duplicate), false);
			} else {
				saveDrafts(status, failed_account_ids);
				showErrorMessage(R.string.updating_status, exception, true);
			}
		} else {
			showOkMessage(R.string.status_updated, false);
			if (status.image_uri != null && status.delete_image) {
				final String path = getImagePathFromUri(this, status.image_uri);
				if (path != null) {
					new File(path).delete();
				}
			}
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_AFTER_TWEET, false)) {
			mTwitter.refreshAll();
		}
		stopForeground(true);
	}

	private void saveDrafts(final ParcelableStatusUpdate status, final List<Long> account_ids) {
		final ContentValues values = new ContentValues();
		values.put(Drafts.ACCOUNT_IDS, ListUtils.toString(account_ids, ';', false));
		values.put(Drafts.IN_REPLY_TO_STATUS_ID, status.in_reply_to_status_id);
		values.put(Drafts.TEXT, status.content);
		if (status.image_uri != null) {
			final int image_type = status.delete_image ? ATTACHED_IMAGE_TYPE_PHOTO : ATTACHED_IMAGE_TYPE_IMAGE;
			values.put(Drafts.ATTACHED_IMAGE_TYPE, image_type);
			values.put(Drafts.IMAGE_URI, ParseUtils.parseString(status.image_uri));
		}
		mResolver.insert(Drafts.CONTENT_URI, values);
		final String title = getString(R.string.tweet_not_sent);
		final String message = getString(R.string.tweet_not_sent_summary);
		final Intent intent = new Intent(INTENT_ACTION_DRAFTS);
		final Notification notification = buildNotification(title, message, R.drawable.ic_stat_twitter, intent, null);
		mNotificationManager.notify(NOTIFICATION_ID_DRAFTS, notification);
	}

	private Notification updateUpdateStatusNotificaion(final int progress, final ParcelableStatusUpdate status) {
		mBuilder.setContentTitle(getString(R.string.updating_status_notification));
		mBuilder.setContentText(status.content);
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
