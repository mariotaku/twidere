/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity;

import static android.os.Environment.getExternalStorageState;
import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.model.ParcelableLocation.isValidLocation;
import static org.mariotaku.twidere.util.ParseUtils.parseString;
import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.copyStream;
import static org.mariotaku.twidere.util.Utils.getAccountColors;
import static org.mariotaku.twidere.util.Utils.getAccountIds;
import static org.mariotaku.twidere.util.Utils.getAccountName;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;
import static org.mariotaku.twidere.util.Utils.getImageUploadStatus;
import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.getQuoteStatus;
import static org.mariotaku.twidere.util.Utils.getShareStatus;
import static org.mariotaku.twidere.util.Utils.getStatusBackground;
import static org.mariotaku.twidere.util.Utils.getStatusTypeIconRes;
import static org.mariotaku.twidere.util.Utils.getUserColor;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.openImageDirectly;
import static org.mariotaku.twidere.util.Utils.showErrorMessage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.twitter.Extractor;
import com.twitter.Validator;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.CroutonStyle;

import org.mariotaku.menubar.MenuBar;
import org.mariotaku.menubar.MenuBar.OnMenuItemClickListener;
import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.BaseDialogFragment;
import org.mariotaku.twidere.model.DraftItem;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.AsyncTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.ViewAccessor;
import org.mariotaku.twidere.view.AccountsColorFrameLayout;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Set;

public class ComposeActivity extends BaseSupportDialogActivity implements TextWatcher, LocationListener,
		OnMenuItemClickListener, OnClickListener, OnLongClickListener, PopupMenu.OnMenuItemClickListener,
		OnEditorActionListener {

	private static final String FAKE_IMAGE_LINK = "https://www.example.com/fake_image.jpg";

	private static final String EXTRA_IS_POSSIBLY_SENSITIVE = "is_possibly_sensitive";

	private static final String EXTRA_SHOULD_SAVE_ACCOUNTS = "should_save_accounts";
	private static final String EXTRA_ORIGINAL_TEXT = "original_text";
	private AsyncTwitterWrapper mTwitterWrapper;
	private LocationManager mLocationManager;

	private SharedPreferences mPreferences;
	private ParcelableLocation mRecentLocation;
	private ContentResolver mResolver;
	private final Validator mValidator = new Validator();
	private ImageLoaderWrapper mImageLoader;
	private AsyncTask<Void, Void, ?> mTask;
	private PopupMenu mPopupMenu;

	private TextView mTextCountView, mTitleView, mSubtitleView;
	private ImageView mImageThumbnailPreview;
	private MenuBar mMenuBar, mActionMenuBar;
	private AccountsColorFrameLayout mColorIndicator;
	private EditText mEditText;
	private ProgressBar mProgress;

	private boolean mIsPossiblySensitive, mShouldSaveAccounts;
	private long[] mAccountIds;
	private int mAttachedImageType;

	private Uri mImageUri, mTempPhotoUri;
	private boolean mImageUploaderUsed, mTweetShortenerUsed;
	private ParcelableStatus mInReplyToStatus;
	private ParcelableUser mMentionUser;
	private DraftItem mDraftItem;
	private long mInReplyToStatusId;
	private String mOriginalText;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_DRAFTS_DATABASE_UPDATED.equals(action)) {
				setMenu();
			}
		}
	};

	@Override
	public void afterTextChanged(final Editable s) {

	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

	}

	public boolean handleMenuItem(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_TAKE_PHOTO: {
				if (mAttachedImageType != ATTACHED_IMAGE_TYPE_PHOTO) {
					takePhoto();
				} else {
					new DeleteImageTask(this).execute();
				}
				break;
			}
			case MENU_ADD_IMAGE: {
				if (mAttachedImageType != ATTACHED_IMAGE_TYPE_IMAGE) {
					pickImage();
				} else {
					new DeleteImageTask(this).execute();
				}
				break;
			}
			case MENU_ADD_LOCATION: {
				final boolean attach_location = mPreferences.getBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false);
				if (!attach_location) {
					getLocation();
				} else {
					mLocationManager.removeUpdates(this);
				}
				mPreferences.edit().putBoolean(PREFERENCE_KEY_ATTACH_LOCATION, !attach_location).commit();
				setMenu();
				break;
			}
			case MENU_DRAFTS: {
				startActivity(new Intent(INTENT_ACTION_DRAFTS));
				break;
			}
			case MENU_DELETE: {
				new DeleteImageTask(this).execute();
				break;
			}
			case MENU_IMAGE: {
				openImageDirectly(this, ParseUtils.parseString(mImageUri), null);
				break;
			}
			case MENU_TOGGLE_SENSITIVE: {
				final boolean has_media = mAttachedImageType != ATTACHED_IMAGE_TYPE_NONE && mImageUri != null;
				if (!has_media) return false;
				mIsPossiblySensitive = !mIsPossiblySensitive;
				setMenu();
				break;
			}
			case MENU_SEND: {
				updateStatus();
				break;
			}
			case MENU_VIEW: {
				if (mInReplyToStatus == null) return false;
				final DialogFragment fragment = new ViewStatusDialogFragment();
				final Bundle args = new Bundle();
				args.putParcelable(EXTRA_STATUS, mInReplyToStatus);
				fragment.setArguments(args);
				fragment.show(getFragmentManager(), "view_status");
				break;
			}
			case MENU_SELECT_ACCOUNT: {
				final Intent intent = new Intent(INTENT_ACTION_SELECT_ACCOUNT);
				final Bundle bundle = new Bundle();
				bundle.putBoolean(EXTRA_ACTIVATED_ONLY, false);
				bundle.putLongArray(EXTRA_IDS, mAccountIds);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_SELECT_ACCOUNT);
				break;
			}
			default: {
				final Intent intent = item.getIntent();
				if (intent != null) {
					try {
						if (INTENT_ACTION_EXTENSION_COMPOSE.equals(intent.getAction())) {
							final Bundle extras = new Bundle();
							extras.putString(EXTRA_TEXT, ParseUtils.parseString(mEditText.getText()));
							extras.putLongArray(EXTRA_ACCOUNT_IDS, mAccountIds);
							if (mAccountIds != null && mAccountIds.length > 0) {
								final long account_id = mAccountIds[0];
								extras.putString(EXTRA_NAME, getAccountName(this, account_id));
								extras.putString(EXTRA_SCREEN_NAME, getAccountScreenName(this, account_id));
							}
							if (mInReplyToStatusId > 0) {
								extras.putLong(EXTRA_IN_REPLY_TO_ID, mInReplyToStatusId);
							}
							if (mInReplyToStatus != null) {
								extras.putString(EXTRA_IN_REPLY_TO_NAME, mInReplyToStatus.user_name);
								extras.putString(EXTRA_IN_REPLY_TO_SCREEN_NAME, mInReplyToStatus.user_screen_name);
							}
							intent.putExtras(extras);
							startActivityForResult(intent, REQUEST_EXTENSION_COMPOSE);
						} else if (INTENT_ACTION_EXTENSION_EDIT_IMAGE.equals(intent.getAction())) {
							final ComponentName cmp = intent.getComponent();
							if (cmp != null) {
								grantUriPermission(cmp.getPackageName(), mImageUri,
										Intent.FLAG_GRANT_READ_URI_PERMISSION);
								startActivityForResult(intent, REQUEST_EDIT_IMAGE);
							}
						} else {
							startActivity(intent);
						}
					} catch (final ActivityNotFoundException e) {
						Log.w(LOGTAG, e);
						return false;
					}
				}
				break;
			}
		}
		return true;
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		switch (requestCode) {
			case REQUEST_TAKE_PHOTO: {
				if (resultCode == Activity.RESULT_OK) {
					mAttachedImageType = ATTACHED_IMAGE_TYPE_PHOTO;
					mTask = new CopyImageTask(this, mImageUri, mTempPhotoUri, createTempImageUri(), true).execute();
				}
				break;
			}
			case REQUEST_PICK_IMAGE: {
				if (resultCode == Activity.RESULT_OK) {
					final Uri src = intent.getData();
					mAttachedImageType = ATTACHED_IMAGE_TYPE_IMAGE;
					mTask = new CopyImageTask(this, mImageUri, src, createTempImageUri(), false).execute();
				}
				break;
			}
			case REQUEST_SELECT_ACCOUNT: {
				if (resultCode == Activity.RESULT_OK) {
					final Bundle bundle = intent.getExtras();
					if (bundle == null) {
						break;
					}
					final long[] account_ids = bundle.getLongArray(EXTRA_IDS);
					if (account_ids != null) {
						mAccountIds = account_ids;
						if (mShouldSaveAccounts) {
							final SharedPreferences.Editor editor = mPreferences.edit();
							editor.putString(PREFERENCE_KEY_COMPOSE_ACCOUNTS,
									ArrayUtils.toString(account_ids, ',', false));
							editor.commit();
						}
						mColorIndicator.setColors(getAccountColors(this, account_ids));
					}
				}
				break;
			}
			case REQUEST_EDIT_IMAGE: {
				if (resultCode == Activity.RESULT_OK) {
					final Uri uri = intent.getData();
					if (uri != null) {
						mImageUri = uri;
						reloadAttachedImageThumbnail();
					} else {
						break;
					}
					setMenu();
				}
				break;
			}
			case REQUEST_EXTENSION_COMPOSE: {
				if (resultCode == Activity.RESULT_OK) {
					final Bundle extras = intent.getExtras();
					if (extras == null) {
						break;
					}
					final String text = extras.getString(EXTRA_TEXT);
					final String append = extras.getString(EXTRA_APPEND_TEXT);
					final Uri image_uri = extras.getParcelable(EXTRA_IMAGE_URI);
					if (text != null) {
						mEditText.setText(text);
					} else if (append != null) {
						mEditText.append(append);
					}
					if (image_uri != null) {
						mImageUri = image_uri;
						reloadAttachedImageThumbnail();
					}
					setMenu();
				}
				break;
			}
		}

	}

	@Override
	public void onBackPressed() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) return;
		final String option = mPreferences.getString(PREFERENCE_KEY_COMPOSE_QUIT_ACTION, COMPOSE_QUIT_ACTION_ASK);
		final String text = mEditText != null ? ParseUtils.parseString(mEditText.getText()) : null;
		final boolean text_changed = !isEmpty(text) && !text.equals(mOriginalText);
		final boolean is_editing_draft = INTENT_ACTION_EDIT_DRAFT.equals(getIntent().getAction());
		if (COMPOSE_QUIT_ACTION_DISCARD.equals(option)) {
			mTask = new DiscardTweetTask(this).execute();
		} else if (text_changed || mImageUri != null || is_editing_draft) {
			if (COMPOSE_QUIT_ACTION_SAVE.equals(option)) {
				saveToDrafts();
				Toast.makeText(this, R.string.tweet_saved_to_draft, Toast.LENGTH_SHORT).show();
				finish();
			} else {
				new UnsavedTweetDialogFragment().show(getFragmentManager(), "unsaved_tweet");
			}
		} else {
			mTask = new DiscardTweetTask(this).execute();
		}
	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.image_thumbnail_preview: {
				openImageMenu();
				break;
			}
			case R.id.close: {
				onBackPressed();
				break;
			}
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mColorIndicator = (AccountsColorFrameLayout) findViewById(R.id.account_colors);
		mEditText = (EditText) findViewById(R.id.edit_text);
		mTextCountView = (TextView) findViewById(R.id.text_count);
		mTitleView = (TextView) findViewById(R.id.actionbar_title);
		mSubtitleView = (TextView) findViewById(R.id.actionbar_subtitle);
		mImageThumbnailPreview = (ImageView) findViewById(R.id.image_thumbnail_preview);
		mMenuBar = (MenuBar) findViewById(R.id.menu_bar);
		mActionMenuBar = (MenuBar) findViewById(R.id.action_menu);
		mProgress = (ProgressBar) findViewById(R.id.actionbar_progress_indeterminate);
		ViewAccessor.setBackground(findViewById(R.id.compose_actionbar), ThemeUtils.getActionBarBackground(this));
	}

	@Override
	public boolean onEditorAction(final TextView view, final int actionId, final KeyEvent event) {
		if (event == null) return false;
		switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_ENTER: {
				updateStatus();
				return true;
			}
		}
		return false;
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (mRecentLocation == null) {
			mRecentLocation = location != null ? new ParcelableLocation(location) : null;
			setProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	public boolean onLongClick(final View view) {
		switch (view.getId()) {
			case R.id.image_thumbnail_preview: {
				openImageMenu();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		return handleMenuItem(item);
	}

	@Override
	public void onProviderDisabled(final String provider) {
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onProviderEnabled(final String provider) {
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putLongArray(EXTRA_ACCOUNT_IDS, mAccountIds);
		outState.putInt(EXTRA_ATTACHED_IMAGE_TYPE, mAttachedImageType);
		outState.putParcelable(EXTRA_IMAGE_URI, mImageUri);
		outState.putBoolean(EXTRA_IS_POSSIBLY_SENSITIVE, mIsPossiblySensitive);
		outState.putParcelable(EXTRA_STATUS, mInReplyToStatus);
		outState.putLong(EXTRA_STATUS_ID, mInReplyToStatusId);
		outState.putParcelable(EXTRA_USER, mMentionUser);
		outState.putParcelable(EXTRA_DRAFT, mDraftItem);
		outState.putBoolean(EXTRA_SHOULD_SAVE_ACCOUNTS, mShouldSaveAccounts);
		outState.putString(EXTRA_ORIGINAL_TEXT, mOriginalText);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStatusChanged(final String provider, final int status, final Bundle extras) {

	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		setMenu();
	}

	public void saveToDrafts() {
		final String text = mEditText != null ? ParseUtils.parseString(mEditText.getText()) : null;
		final ContentValues values = new ContentValues();
		values.put(Drafts.TEXT, text);
		values.put(Drafts.ACCOUNT_IDS, ArrayUtils.toString(mAccountIds, ',', false));
		values.put(Drafts.IN_REPLY_TO_STATUS_ID, mInReplyToStatusId);
		values.put(Drafts.LOCATION, ParcelableLocation.toString(mRecentLocation));
		values.put(Drafts.IS_POSSIBLY_SENSITIVE, mIsPossiblySensitive);
		if (mImageUri != null) {
			values.put(Drafts.ATTACHED_IMAGE_TYPE, mAttachedImageType);
			values.put(Drafts.IMAGE_URI, ParseUtils.parseString(mImageUri));
		}
		mResolver.insert(Drafts.CONTENT_URI, values);
	}

	@Override
	protected int getThemeResource() {
		return ThemeUtils.getComposeThemeResource(this);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mTwitterWrapper = getTwidereApplication().getTwitterWrapper();
		mResolver = getContentResolver();
		mImageLoader = getTwidereApplication().getImageLoaderWrapper();
		setContentView(R.layout.compose);
		setProgressBarIndeterminateVisibility(false);
		setFinishOnTouchOutside(false);
		final long[] account_ids = getAccountIds(this);
		if (account_ids.length <= 0) {
			final Intent intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
			intent.setClass(this, SignInActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		mImageThumbnailPreview.setOnClickListener(this);
		mImageThumbnailPreview.setOnLongClickListener(this);
		mMenuBar.setOnMenuItemClickListener(this);
		mActionMenuBar.setOnMenuItemClickListener(this);
		mEditText.setOnEditorActionListener(mPreferences.getBoolean(PREFERENCE_KEY_QUICK_SEND, false) ? this : null);
		mEditText.addTextChangedListener(this);

		final Intent intent = getIntent();
		final String action = intent.getAction();

		if (savedInstanceState != null) {
			// Restore from previous saved state
			mAccountIds = savedInstanceState.getLongArray(EXTRA_ACCOUNT_IDS);
			mAttachedImageType = savedInstanceState.getInt(EXTRA_ATTACHED_IMAGE_TYPE, ATTACHED_IMAGE_TYPE_NONE);
			mIsPossiblySensitive = savedInstanceState.getBoolean(EXTRA_IS_POSSIBLY_SENSITIVE);
			mImageUri = savedInstanceState.getParcelable(EXTRA_IMAGE_URI);
			mInReplyToStatus = savedInstanceState.getParcelable(EXTRA_STATUS);
			mInReplyToStatusId = savedInstanceState.getLong(EXTRA_STATUS_ID);
			mMentionUser = savedInstanceState.getParcelable(EXTRA_USER);
			mDraftItem = savedInstanceState.getParcelable(EXTRA_DRAFT);
			mShouldSaveAccounts = savedInstanceState.getBoolean(EXTRA_SHOULD_SAVE_ACCOUNTS);
			mOriginalText = savedInstanceState.getString(EXTRA_ORIGINAL_TEXT);
		} else {
			// The activity was first created
			final Bundle extras = intent.getExtras();
			final int notification_id = extras != null ? extras.getInt(EXTRA_NOTIFICATION_ID, -1) : -1;
			if (notification_id != -1) {
				mTwitterWrapper.clearNotification(notification_id);
			}
			if (!handleIntent(action, extras)) {
				handleDefaultIntent(intent);
			}
			if (mAccountIds == null || mAccountIds.length == 0) {
				final long[] ids_in_prefs = ArrayUtils.fromString(
						mPreferences.getString(PREFERENCE_KEY_COMPOSE_ACCOUNTS, null), ',');
				final long[] intersection = ArrayUtils.intersection(ids_in_prefs, account_ids);
				mAccountIds = intersection.length > 0 ? intersection : account_ids;
			}
			mOriginalText = ParseUtils.parseString(mEditText.getText());
		}
		if (!setComposeTitle(action)) {
			setTitle(R.string.compose);
		}

		reloadAttachedImageThumbnail();

		mMenuBar.inflate(R.menu.menu_compose);
		mActionMenuBar.inflate(R.menu.menu_compose_actionbar);
		final boolean bottom_send_button = mPreferences.getBoolean(PREFERENCE_KEY_BOTTOM_SEND_BUTTON, false);
		final Menu menu = mMenuBar.getMenu(), actionBarMenu = mActionMenuBar.getMenu();
		final MenuItem more_submenu = (bottom_send_button ? actionBarMenu : menu).findItem(R.id.more_submenu);
		if (more_submenu != null) {
			final Intent extensions_intent = new Intent(INTENT_ACTION_EXTENSION_COMPOSE);
			addIntentToMenu(this, more_submenu.getSubMenu(), extensions_intent);
		}
		final MenuItem sendItem = menu.findItem(MENU_SEND), sendActionItem = actionBarMenu.findItem(MENU_SEND);
		if (sendItem != null && sendActionItem != null) {
			sendItem.setVisible(bottom_send_button);
			sendActionItem.setVisible(!bottom_send_button);
		}
		final MenuItem moreItem = menu.findItem(R.id.more_submenu), sendMoreItem = actionBarMenu
				.findItem(R.id.more_submenu);
		if (moreItem != null && sendMoreItem != null) {
			moreItem.setVisible(!bottom_send_button);
			sendMoreItem.setVisible(bottom_send_button);
		}
		setMenu();
		mColorIndicator.setColors(getAccountColors(this, mAccountIds));
	}

	@Override
	protected void onStart() {
		super.onStart();
		final String uploader_component = mPreferences.getString(PREFERENCE_KEY_IMAGE_UPLOADER, null);
		final String shortener_component = mPreferences.getString(PREFERENCE_KEY_TWEET_SHORTENER, null);
		mImageUploaderUsed = !isEmpty(uploader_component);
		mTweetShortenerUsed = !isEmpty(shortener_component);
		if (mMenuBar != null) {
			setMenu();
		}
		final int text_size = mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, getDefaultTextSize(this));
		mEditText.setTextSize(text_size * 1.25f);
		final IntentFilter filter = new IntentFilter(BROADCAST_DRAFTS_DATABASE_UPDATED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	protected void onStop() {
		unregisterReceiver(mStatusReceiver);
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		mLocationManager.removeUpdates(this);
		super.onStop();
	}

	@Override
	protected void onTitleChanged(final CharSequence title, final int color) {
		super.onTitleChanged(title, color);
		mTitleView.setText(title);
	}

	private Uri createTempImageUri() {
		final File file = new File(getCacheDir(), "tmp_image_" + System.currentTimeMillis());
		return Uri.fromFile(file);
	}

	/**
	 * The Location Manager manages location providers. This code searches for
	 * the best provider of data (GPS, WiFi/cell phone tower lookup, some other
	 * mechanism) and finds the last known location.
	 **/
	private boolean getLocation() {
		final Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		final String provider = mLocationManager.getBestProvider(criteria, true);

		if (provider != null) {
			final Location location;
			if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			} else {
				location = mLocationManager.getLastKnownLocation(provider);
			}
			if (location == null) {
				mLocationManager.requestLocationUpdates(provider, 0, 0, this);
				setProgressVisibility(true);
			}
			mRecentLocation = location != null ? new ParcelableLocation(location) : null;
		} else {
			Crouton.showText(this, R.string.cannot_get_location, CroutonStyle.ALERT);
		}
		return provider != null;
	}

	private boolean handleDefaultIntent(final Intent intent) {
		if (intent == null) return false;
		final String action = intent.getAction();
		mShouldSaveAccounts = !Intent.ACTION_SEND.equals(action) && !Intent.ACTION_SEND_MULTIPLE.equals(action);
		final Bundle extras = intent.getExtras();
		final Uri data = intent.getData();
		if (data != null) {
			mImageUri = data;
		}
		if (extras != null) {
			final CharSequence extra_subject = extras.getCharSequence(Intent.EXTRA_SUBJECT);
			final CharSequence extra_text = extras.getCharSequence(Intent.EXTRA_TEXT);
			final Uri extra_stream = extras.getParcelable(Intent.EXTRA_STREAM);
			if (extra_stream != null) {
				new CopyImageTask(this, mImageUri, extra_stream, createTempImageUri(), false).execute();
			}
			mEditText.setText(getShareStatus(this, extra_subject, extra_text));
		}
		if (mImageUri != null) {
			mAttachedImageType = ATTACHED_IMAGE_TYPE_IMAGE;
		}
		final int selection_end = mEditText.length();
		mEditText.setSelection(selection_end);
		return true;
	}

	private boolean handleEditDraftIntent(final DraftItem draft) {
		if (draft == null) return false;
		mEditText.setText(draft.text);
		final int selection_end = mEditText.length();
		mEditText.setSelection(selection_end);
		mAccountIds = draft.account_ids;
		mImageUri = draft.media_uri != null ? Uri.parse(draft.media_uri) : null;
		mAttachedImageType = draft.attached_image_type;
		mIsPossiblySensitive = draft.is_possibly_sensitive;
		mInReplyToStatusId = draft.in_reply_to_status_id;
		return true;
	}

	private boolean handleIntent(final String action, final Bundle extras) {
		mShouldSaveAccounts = false;
		if (extras == null) return false;
		mMentionUser = extras.getParcelable(EXTRA_USER);
		mInReplyToStatus = extras.getParcelable(EXTRA_STATUS);
		mInReplyToStatusId = mInReplyToStatus != null ? mInReplyToStatus.id : -1;
		if (INTENT_ACTION_REPLY.equals(action))
			return handleReplyIntent(mInReplyToStatus);
		else if (INTENT_ACTION_QUOTE.equals(action))
			return handleQuoteIntent(mInReplyToStatus);
		else if (INTENT_ACTION_EDIT_DRAFT.equals(action)) {
			mDraftItem = extras.getParcelable(EXTRA_DRAFT);
			return handleEditDraftIntent(mDraftItem);
		} else if (INTENT_ACTION_MENTION.equals(action))
			return handleMentionIntent(mMentionUser);
		else if (INTENT_ACTION_REPLY_MULTIPLE.equals(action)) {
			final String[] screen_names = extras.getStringArray(EXTRA_SCREEN_NAMES);
			final long account_id = extras.getLong(EXTRA_ACCOUNT_ID, -1);
			final long in_reply_to_user_id = extras.getLong(EXTRA_IN_REPLY_TO_ID, -1);
			return handleReplyMultipleIntent(screen_names, account_id, in_reply_to_user_id);
		}
		// Unknown action or no intent extras
		return false;
	}

	private boolean handleMentionIntent(final ParcelableUser user) {
		if (user == null || user.id <= 0) return false;
		final String my_screen_name = getAccountScreenName(this, user.account_id);
		if (isEmpty(my_screen_name)) return false;
		mEditText.setText("@" + user.screen_name + " ");
		final int selection_end = mEditText.length();
		mEditText.setSelection(selection_end);
		mAccountIds = new long[] { user.account_id };
		return true;
	}

	private boolean handleQuoteIntent(final ParcelableStatus status) {
		if (status == null || status.id <= 0) return false;
		mEditText.setText(getQuoteStatus(this, status.user_screen_name, status.text_plain));
		mEditText.setSelection(0);
		mAccountIds = new long[] { status.account_id };
		return true;
	}

	private boolean handleReplyIntent(final ParcelableStatus status) {
		if (status == null || status.id <= 0) return false;
		final String my_screen_name = getAccountScreenName(this, status.account_id);
		if (isEmpty(my_screen_name)) return false;
		final Set<String> mentions = new Extractor().extractMentionedScreennames(status.text_plain);
		mEditText.append("@" + status.user_screen_name + " ");
		final int selection_start = mEditText.length();
		for (final String screen_name : mentions) {
			if (screen_name.equalsIgnoreCase(status.user_screen_name) || screen_name.equalsIgnoreCase(my_screen_name)) {
				continue;
			}
			mEditText.append("@" + screen_name + " ");
		}
		final int selection_end = mEditText.length();
		mEditText.setSelection(selection_start, selection_end);
		mAccountIds = new long[] { status.account_id };
		return true;
	}

	private boolean handleReplyMultipleIntent(final String[] screen_names, final long account_id,
			final long in_reply_to_status_id) {
		if (screen_names == null || screen_names.length == 0 || account_id <= 0) return false;
		final String my_screen_name = getAccountScreenName(this, account_id);
		if (isEmpty(my_screen_name)) return false;
		final int selection_start = mEditText.length();
		for (final String screen_name : screen_names) {
			if (screen_name.equalsIgnoreCase(my_screen_name)) {
				continue;
			}
			mEditText.append("@" + screen_name + " ");
		}
		final int selection_end = mEditText.length();
		mEditText.setSelection(selection_start, selection_end);
		mAccountIds = new long[] { account_id };
		mInReplyToStatusId = in_reply_to_status_id;
		return true;
	}

	private boolean hasDraftItem() {
		final Cursor drafts_cur = getContentResolver().query(Drafts.CONTENT_URI, new String[0], null, null, null);
		try {
			return drafts_cur.getCount() > 0;
		} finally {

			drafts_cur.close();
		}
	}

	private boolean noReplyContent(final String text) {
		if (text == null) return true;
		final String action = getIntent().getAction();
		final boolean is_reply = INTENT_ACTION_REPLY.equals(action) || INTENT_ACTION_REPLY_MULTIPLE.equals(action);
		return is_reply && text.equals(mOriginalText);
	}

	private void openImageMenu() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		mPopupMenu = PopupMenu.getInstance(this, mImageThumbnailPreview);
		mPopupMenu.inflate(R.menu.action_attached_image);
		final Menu menu = mPopupMenu.getMenu();
		final Intent extension_intent = new Intent(INTENT_ACTION_EXTENSION_EDIT_IMAGE);
		extension_intent.setData(mImageUri);
		addIntentToMenu(this, menu, extension_intent);
		mPopupMenu.setOnMenuItemClickListener(this);
		mPopupMenu.show();
	}

	private void pickImage() {
		final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		try {
			startActivityForResult(intent, REQUEST_PICK_IMAGE);
		} catch (final ActivityNotFoundException e) {
			showErrorMessage(this, null, e, false);
		}
	}

	private void reloadAttachedImageThumbnail() {
		mImageThumbnailPreview.setVisibility(mImageUri != null ? View.VISIBLE : View.GONE);
		mImageLoader.displayPreviewImage(mImageThumbnailPreview, mImageUri != null ? mImageUri.toString() : null);
	}

	private void setCommonMenu(final Menu menu) {
		final MenuItem itemMore = menu.findItem(R.id.more_submenu);
		if (itemMore != null) {
			final int activated_color = ThemeUtils.getThemeColor(this);
			final MenuItem itemDrafts = menu.findItem(MENU_DRAFTS);
			final MenuItem itemToggleSensitive = menu.findItem(MENU_TOGGLE_SENSITIVE);
			if (itemDrafts != null) {
				final Drawable iconMore = itemMore.getIcon().mutate();
				final Drawable iconDrafts = itemDrafts.getIcon().mutate();
				if (hasDraftItem()) {
					iconMore.setColorFilter(activated_color, Mode.MULTIPLY);
					iconDrafts.setColorFilter(activated_color, Mode.MULTIPLY);
				} else {
					iconMore.clearColorFilter();
					iconDrafts.clearColorFilter();
				}
			}
			if (itemToggleSensitive != null) {
				final boolean has_media = mImageUri != null;
				itemToggleSensitive.setVisible(has_media);
				if (has_media) {
					final Drawable iconToggleSensitive = itemToggleSensitive.getIcon().mutate();
					if (mIsPossiblySensitive) {
						itemToggleSensitive.setTitle(R.string.remove_sensitive_mark);
						iconToggleSensitive.setColorFilter(activated_color, Mode.MULTIPLY);
					} else {
						itemToggleSensitive.setTitle(R.string.mark_as_sensitive);
						iconToggleSensitive.clearColorFilter();
					}
				}
			}
		}
	}

	private boolean setComposeTitle(final String action) {
		final boolean display_screen_name = NAME_DISPLAY_OPTION_SCREEN_NAME.equals(mPreferences.getString(
				PREFERENCE_KEY_NAME_DISPLAY_OPTION, NAME_DISPLAY_OPTION_BOTH));
		if (INTENT_ACTION_REPLY.equals(action)) {
			if (mInReplyToStatus == null) return false;
			setTitle(getString(R.string.reply_to, display_screen_name ? "@" + mInReplyToStatus.user_screen_name
					: mInReplyToStatus.user_name));
		} else if (INTENT_ACTION_QUOTE.equals(action)) {
			if (mInReplyToStatus == null) return false;
			setTitle(getString(R.string.quote_user, display_screen_name ? "@" + mInReplyToStatus.user_screen_name
					: mInReplyToStatus.user_name));
			mSubtitleView.setVisibility(mInReplyToStatus.user_is_protected
					&& mInReplyToStatus.account_id != mInReplyToStatus.user_id ? View.VISIBLE : View.GONE);
		} else if (INTENT_ACTION_EDIT_DRAFT.equals(action)) {
			if (mDraftItem == null) return false;
			setTitle(R.string.edit_draft);
		} else if (INTENT_ACTION_MENTION.equals(action)) {
			if (mMentionUser == null) return false;
			setTitle(getString(R.string.mention_user, display_screen_name ? "@" + mMentionUser.screen_name
					: mMentionUser.name));
		} else if (INTENT_ACTION_REPLY_MULTIPLE.equals(action)) {
			setTitle(R.string.reply);
		} else if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
			setTitle(R.string.share);
		} else {
			setTitle(R.string.compose);
		}
		return true;
	}

	private void setMenu() {
		final String text_orig = mEditText != null ? parseString(mEditText.getText()) : null;
		final String text = mImageUri != null && text_orig != null ? mImageUploaderUsed ? getImageUploadStatus(this,
				FAKE_IMAGE_LINK, text_orig) : text_orig + " " + FAKE_IMAGE_LINK : text_orig;
		final int validated_count = text != null ? mValidator.getTweetLength(text) : 0;
		final boolean exceeded_limit = validated_count < Validator.MAX_TWEET_LENGTH;
		final boolean near_limit = validated_count >= Validator.MAX_TWEET_LENGTH - 10;
		final float hue = exceeded_limit ? near_limit ? 5 * (Validator.MAX_TWEET_LENGTH - validated_count) : 50 : 0;
		final float[] hsv = new float[] { hue, 1.0f, 1.0f };
		if (mTextCountView != null) {
			mTextCountView.setTextColor(validated_count >= Validator.MAX_TWEET_LENGTH - 10 ? Color
					.HSVToColor(0x80, hsv) : 0x80808080);
			mTextCountView
					.setText(getLocalizedNumber(Locale.getDefault(), Validator.MAX_TWEET_LENGTH - validated_count));
		}
		final Menu bottomMenu = mMenuBar.getMenu(), actionMenu = mActionMenuBar.getMenu();
		if (bottomMenu.size() == 0) return;
		final int activated_color = ThemeUtils.getThemeColor(this);
		final MenuItem itemAddImage = bottomMenu.findItem(MENU_ADD_IMAGE);
		final Drawable iconAddImage = itemAddImage.getIcon().mutate();
		if (mAttachedImageType == ATTACHED_IMAGE_TYPE_IMAGE) {
			iconAddImage.setColorFilter(activated_color, Mode.MULTIPLY);
			itemAddImage.setTitle(R.string.remove_image);
		} else {
			iconAddImage.clearColorFilter();
			itemAddImage.setTitle(R.string.add_image);
		}
		final MenuItem itemTakePhoto = bottomMenu.findItem(MENU_TAKE_PHOTO);
		final Drawable iconTakePhoto = itemTakePhoto.getIcon().mutate();
		if (mAttachedImageType == ATTACHED_IMAGE_TYPE_PHOTO) {
			iconTakePhoto.setColorFilter(activated_color, Mode.MULTIPLY);
			itemTakePhoto.setTitle(R.string.remove_photo);
		} else {
			iconTakePhoto.clearColorFilter();
			itemTakePhoto.setTitle(R.string.take_photo);
		}
		final MenuItem itemAttachLocation = bottomMenu.findItem(MENU_ADD_LOCATION);
		final Drawable iconAttachLocation = itemAttachLocation.getIcon().mutate();
		final boolean attach_location = mPreferences.getBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false);
		if (attach_location && getLocation()) {
			iconAttachLocation.setColorFilter(activated_color, Mode.MULTIPLY);
			itemAttachLocation.setTitle(R.string.remove_location);
		} else {
			setProgressVisibility(false);
			mPreferences.edit().putBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false).commit();
			iconAttachLocation.clearColorFilter();
			itemAttachLocation.setTitle(R.string.add_location);
		}
		final MenuItem viewItem = bottomMenu.findItem(MENU_VIEW);
		if (viewItem != null) {
			viewItem.setVisible(mInReplyToStatus != null);
		}
		setCommonMenu(bottomMenu);
		setCommonMenu(actionMenu);
		mActionMenuBar.show();
		mMenuBar.show();
	}

	private void setProgressVisibility(final boolean visible) {
		mProgress.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private void takePhoto() {
		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			final File cache_dir = getExternalCacheDir();
			final File file = new File(cache_dir, "tmp_photo_" + System.currentTimeMillis());
			mTempPhotoUri = Uri.fromFile(file);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPhotoUri);
			try {
				startActivityForResult(intent, REQUEST_TAKE_PHOTO);
			} catch (final ActivityNotFoundException e) {
				showErrorMessage(this, null, e, false);
			}
		}
	}

	private void updateStatus() {
		if (isFinishing()) return;
		final String text = mEditText != null ? ParseUtils.parseString(mEditText.getText()) : null;
		final int tweet_length = mValidator.getTweetLength(text);
		if (!mTweetShortenerUsed && tweet_length > Validator.MAX_TWEET_LENGTH) {
			mEditText.setError(getString(R.string.error_message_status_too_long));
			final int text_length = mEditText.length();
			mEditText.setSelection(text_length - (tweet_length - Validator.MAX_TWEET_LENGTH), text_length);
			return;
		} else if ((mImageUri != null && !mImageUploaderUsed || mImageUri == null)
				&& (isEmpty(text) || noReplyContent(text))) {
			mEditText.setError(getString(R.string.error_message_no_content));
			return;
		}
		final boolean has_media = mAttachedImageType != ATTACHED_IMAGE_TYPE_NONE && mImageUri != null;
		final boolean attach_location = mPreferences.getBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false);
		if (mRecentLocation == null && attach_location) {
			final Location location;
			if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			} else {
				location = null;
			}
			mRecentLocation = location != null ? new ParcelableLocation(location) : null;
		}
		final boolean is_quote = INTENT_ACTION_QUOTE.equals(getIntent().getAction());
		final ParcelableLocation status_loc = attach_location ? mRecentLocation : null;
		final boolean link_to_quoted_tweet = mPreferences.getBoolean(PREFERENCE_KEY_LINK_TO_QUOTED_TWEET, true);
		final long in_reply_to = !is_quote || link_to_quoted_tweet ? mInReplyToStatusId : -1;
		final boolean possibly_sensitive = has_media && mIsPossiblySensitive;
		final boolean delete_image = mAttachedImageType == ATTACHED_IMAGE_TYPE_PHOTO;
		mTwitterWrapper.updateStatus(mAccountIds, text, status_loc, mImageUri, in_reply_to, possibly_sensitive,
				delete_image);
		if (mPreferences.getBoolean(PREFERENCE_KEY_NO_CLOSE_AFTER_TWEET_SENT, false)
				&& (mInReplyToStatus == null || mInReplyToStatusId <= 0)) {
			mAttachedImageType = ATTACHED_IMAGE_TYPE_NONE;
			mIsPossiblySensitive = false;
			mShouldSaveAccounts = true;
			mImageUri = null;
			mTempPhotoUri = null;
			mInReplyToStatus = null;
			mMentionUser = null;
			mDraftItem = null;
			mInReplyToStatusId = -1;
			mOriginalText = null;
			final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
			setIntent(intent);
			setComposeTitle(intent.getAction());
			handleIntent(intent.getAction(), intent.getExtras());
			reloadAttachedImageThumbnail();
			mEditText.setText(null);
			setMenu();
		} else {
			setResult(Activity.RESULT_OK);
			finish();
		}
	}

	public static class UnsavedTweetDialogFragment extends BaseDialogFragment implements
			DialogInterface.OnClickListener {

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			final Activity activity = getActivity();
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					if (activity instanceof ComposeActivity) {
						((ComposeActivity) activity).saveToDrafts();
					}
					activity.finish();
					break;
				}
				case DialogInterface.BUTTON_NEGATIVE: {
					if (activity instanceof ComposeActivity) {
						new DiscardTweetTask((ComposeActivity) activity).execute();
					} else {
						activity.finish();
					}
					break;
				}
			}

		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.unsaved_tweet);
			builder.setPositiveButton(R.string.save, this);
			builder.setNegativeButton(R.string.discard, this);
			return builder.create();
		}
	}

	public static class ViewStatusDialogFragment extends BaseDialogFragment {

		private StatusViewHolder mHolder;

		public ViewStatusDialogFragment() {
			setStyle(STYLE_NO_TITLE, 0);
		}

		@Override
		public void onActivityCreated(final Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			final Bundle args = getArguments();
			if (args == null || args.getParcelable(EXTRA_STATUS) == null) {
				dismiss();
				return;
			}
			final TwidereApplication application = getApplication();
			final ImageLoaderWrapper loader = application.getImageLoaderWrapper();
			final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
			final ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
			mHolder.setShowAsGap(false);
			mHolder.setAccountColorEnabled(true);
			mHolder.setTextSize(prefs.getInt(PREFERENCE_KEY_TEXT_SIZE, getDefaultTextSize(getActivity())));
			mHolder.text.setText(status.text_unescaped);
			final String name_option = prefs.getString(PREFERENCE_KEY_NAME_DISPLAY_OPTION, NAME_DISPLAY_OPTION_BOTH);
			if (NAME_DISPLAY_OPTION_NAME.equals(name_option)) {
				mHolder.name.setText(status.user_name);
				mHolder.screen_name.setText(null);
				mHolder.screen_name.setVisibility(View.GONE);
			} else if (NAME_DISPLAY_OPTION_SCREEN_NAME.equals(name_option)) {
				mHolder.name.setText("@" + status.user_screen_name);
				mHolder.screen_name.setText(null);
				mHolder.screen_name.setVisibility(View.GONE);
			} else {
				mHolder.name.setText(status.user_name);
				mHolder.screen_name.setText("@" + status.user_screen_name);
				mHolder.screen_name.setVisibility(View.VISIBLE);
			}

			final String retweeted_by_name = status.retweeted_by_name;
			final String retweeted_by_screen_name = status.retweeted_by_screen_name;

			final boolean is_my_status = status.account_id == status.user_id;
			mHolder.setUserColor(getUserColor(getActivity(), status.user_id, true));
			mHolder.setHighlightColor(getStatusBackground(false, status.is_favorite, status.is_retweet));

			mHolder.setIsMyStatus(is_my_status && !prefs.getBoolean(PREFERENCE_KEY_INDICATE_MY_STATUS, true));

			mHolder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					getUserTypeIconRes(status.user_is_verified, status.user_is_protected), 0);
			mHolder.time.setTime(status.timestamp);
			final int type_icon = getStatusTypeIconRes(status.is_favorite, isValidLocation(status.location),
					status.has_media, status.is_possibly_sensitive);
			mHolder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0, type_icon, 0);
			mHolder.reply_retweet_status
					.setVisibility(status.in_reply_to_status_id != -1 || status.is_retweet ? View.VISIBLE : View.GONE);
			if (status.is_retweet && !TextUtils.isEmpty(retweeted_by_name)
					&& !TextUtils.isEmpty(retweeted_by_screen_name)) {
				if (NAME_DISPLAY_OPTION_SCREEN_NAME.equals(name_option)) {
					mHolder.reply_retweet_status.setText(status.retweet_count > 1 ? getString(
							R.string.retweeted_by_with_count, retweeted_by_screen_name, status.retweet_count - 1)
							: getString(R.string.retweeted_by, retweeted_by_screen_name));
				} else {
					mHolder.reply_retweet_status.setText(status.retweet_count > 1 ? getString(
							R.string.retweeted_by_with_count, retweeted_by_name, status.retweet_count - 1) : getString(
							R.string.retweeted_by, retweeted_by_name));
				}
				mHolder.reply_retweet_status.setText(status.retweet_count > 1 ? getString(
						R.string.retweeted_by_with_count, retweeted_by_name, status.retweet_count - 1) : getString(
						R.string.retweeted_by, retweeted_by_name));
				mHolder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_retweet,
						0, 0, 0);
			} else if (status.in_reply_to_status_id > 0 && !TextUtils.isEmpty(status.in_reply_to_screen_name)) {
				mHolder.reply_retweet_status.setText(getString(R.string.in_reply_to, status.in_reply_to_screen_name));
				mHolder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_reply, 0,
						0, 0);
			}
			if (prefs.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true)) {
				loader.displayProfileImage(mHolder.my_profile_image, status.user_profile_image_url);
				loader.displayProfileImage(mHolder.profile_image, status.user_profile_image_url);
			} else {
				mHolder.profile_image.setVisibility(View.GONE);
				mHolder.my_profile_image.setVisibility(View.GONE);
			}
			mHolder.image_preview_container.setVisibility(View.GONE);
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
			final ScrollView view = (ScrollView) inflater.inflate(R.layout.compose_view_status, parent, false);
			mHolder = new StatusViewHolder(view.getChildAt(0));
			return view;
		}

	}

	static class CopyImageTask extends AsyncTask<Void, Void, Boolean> {

		final ComposeActivity activity;
		final boolean delete_orig;
		final Uri old, src, dst;

		CopyImageTask(final ComposeActivity activity, final Uri old, final Uri src, final Uri dst,
				final boolean delete_orig) {
			this.activity = activity;
			this.old = old;
			this.src = src;
			this.dst = dst;
			this.delete_orig = delete_orig;
		}

		@Override
		protected Boolean doInBackground(final Void... params) {
			try {
				final ContentResolver resolver = activity.getContentResolver();
				final InputStream is = resolver.openInputStream(src);
				final OutputStream os = resolver.openOutputStream(dst);
				copyStream(is, os);
				os.close();
				if (old != null && !old.equals(dst) && ContentResolver.SCHEME_FILE.equals(old.getScheme())) {
					new File(old.getPath()).delete();
				}
				if (ContentResolver.SCHEME_FILE.equals(src.getScheme()) && delete_orig) {
					new File(src.getPath()).delete();
				}
			} catch (final Exception e) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean result) {
			activity.setProgressVisibility(false);
			activity.mImageUri = dst;
			activity.reloadAttachedImageThumbnail();
			activity.setMenu();
			if (!result) {
				Crouton.showText(activity, R.string.error_occurred, CroutonStyle.ALERT);
			}
		}

		@Override
		protected void onPreExecute() {
			activity.setProgressVisibility(true);
		}
	}

	static class DeleteImageTask extends AsyncTask<Uri, Void, Boolean> {

		final ComposeActivity activity;

		DeleteImageTask(final ComposeActivity activity) {
			this.activity = activity;
		}

		@Override
		protected Boolean doInBackground(final Uri... params) {
			if (params == null) return false;
			try {
				final Uri uri = activity.mImageUri;
				if (uri != null && ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
					new File(uri.getPath()).delete();
				}
				for (final Uri target : params) {
					if (target == null) {
						continue;
					}
					if (ContentResolver.SCHEME_FILE.equals(target.getScheme())) {
						new File(target.getPath()).delete();
					}
				}
			} catch (final Exception e) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean result) {
			activity.setProgressVisibility(false);
			activity.mImageUri = null;
			activity.mAttachedImageType = ATTACHED_IMAGE_TYPE_NONE;
			activity.mIsPossiblySensitive = false;
			activity.setMenu();
			activity.reloadAttachedImageThumbnail();
			if (!result) {
				Crouton.showText(activity, R.string.error_occurred, CroutonStyle.ALERT);
			}
		}

		@Override
		protected void onPreExecute() {
			activity.setProgressVisibility(true);
		}
	}

	static class DiscardTweetTask extends AsyncTask<Void, Void, Void> {

		final ComposeActivity activity;

		DiscardTweetTask(final ComposeActivity activity) {
			this.activity = activity;
		}

		@Override
		protected Void doInBackground(final Void... params) {
			final Uri uri = activity.mImageUri;
			try {
				if (uri == null) return null;
				if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
					new File(uri.getPath()).delete();
				}
			} catch (final Exception e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			activity.setProgressVisibility(false);
			activity.finish();
		}

		@Override
		protected void onPreExecute() {
			activity.setProgressVisibility(true);
		}
	}
}
