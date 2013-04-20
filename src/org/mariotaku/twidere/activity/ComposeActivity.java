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
import static org.mariotaku.twidere.util.Utils.copyStream;
import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.getAccountColors;
import static org.mariotaku.twidere.util.Utils.getAccountIds;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.getImageUploadStatus;
import static org.mariotaku.twidere.util.Utils.getQuoteStatus;
import static org.mariotaku.twidere.util.Utils.getShareStatus;
import static org.mariotaku.twidere.util.Utils.openImageDirectly;
import static org.mariotaku.twidere.util.Utils.parseString;
import static org.mariotaku.twidere.util.Utils.showErrorToast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.mariotaku.menubar.MenuBar;
import org.mariotaku.menubar.MenuBar.OnMenuItemClickListener;
import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.BaseDialogFragment;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.util.ActivityAccessor;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.BitmapDecodeHelper;
import org.mariotaku.twidere.util.EnvironmentAccessor;
import org.mariotaku.twidere.util.ImageValidator;
import org.mariotaku.twidere.view.ColorView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.twitter.Validator;
import org.mariotaku.twidere.util.AsyncTask;
import java.io.OutputStream;
import java.io.IOException;
import org.mariotaku.twidere.fragment.ProgressDialogFragment;
import android.support.v4.app.FragmentManager;
import org.mariotaku.twidere.model.ParcelableStatus;
import java.util.List;
import com.twitter.Extractor;
import java.util.Set;
import org.mariotaku.actionbarcompat.ActionBar;
import org.mariotaku.twidere.model.DraftItem;
import android.content.ComponentName;
import org.mariotaku.twidere.model.ParcelableUser;

public class ComposeActivity extends BaseDialogWhenLargeActivity implements TextWatcher, LocationListener,
		OnMenuItemClickListener, OnClickListener, OnLongClickListener, PopupMenu.OnMenuItemClickListener,
		OnEditorActionListener, LoaderCallbacks<Bitmap> {

	private static final String FAKE_IMAGE_LINK = "https://www.example.com/fake_image.jpg";
	private static final String INTENT_KEY_CONTENT_MODIFIED = "content_modified";
	private static final String INTENT_KEY_IS_POSSIBLY_SENSITIVE = "is_possibly_sensitive";	private static final String INTENT_KEY_SHOULD_SAVE_ACCOUNTS = "should_save_accounts";

	private AsyncTwitterWrapper mTwitterWrapper;
	private LocationManager mLocationManager;
	private SharedPreferences mPreferences;
	private ParcelableLocation mRecentLocation;
	private ContentResolver mResolver;
	private final Validator mValidator = new Validator();

	private ActionBar mActionBar;
	private PopupMenu mPopupMenu;

	private static final int THUMBNAIL_SIZE = 36;

	private ColorView mColorIndicator;
	private EditText mEditText;
	private TextView mTextCount;
	private ImageView mImageThumbnailPreview;
	private MenuBar mMenuBar;

	private boolean mIsImageAttached, mIsPhotoAttached, mIsPossiblySensitive, mShouldSaveAccounts;
	private long[] mAccountIds;
	private Uri mImageUri, mTempPhotoUri;
	private boolean mLoaderInitialized, mUploadUseExtension, mContentModified;
	private ParcelableStatus mInReplyToStatus;
	private ParcelableUser mMentionUser;
	private DraftItem mDraftItem;
	private long mInReplyToStatusId;

	//private DialogFragment mUnsavedTweetDialogFragment;

	@Override
	public void afterTextChanged(final Editable s) {

	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		switch (requestCode) {
			case REQUEST_TAKE_PHOTO: {
				if (resultCode == Activity.RESULT_OK) {
					mIsPhotoAttached = true;
					mIsImageAttached = false;
					new CopyImageTask(this, mImageUri, mTempPhotoUri, createTempImageUri(), true).execute();
				}
				break;
			}
			case REQUEST_PICK_IMAGE: {
				if (resultCode == Activity.RESULT_OK) {
					final Uri src = intent.getData();
					mIsPhotoAttached = false;
					mIsImageAttached = true;
					new CopyImageTask(this, mImageUri, src, createTempImageUri(), false).execute();
				}
				break;
			}
			case REQUEST_SELECT_ACCOUNT: {
				if (resultCode == Activity.RESULT_OK) {
					final Bundle bundle = intent.getExtras();
					if (bundle == null) {
						break;
					}
					final long[] account_ids = bundle.getLongArray(INTENT_KEY_IDS);
					if (account_ids != null) {
						mAccountIds = account_ids;
						if (mShouldSaveAccounts) {
							final SharedPreferences.Editor editor = mPreferences.edit();
							editor.putString(PREFERENCE_KEY_COMPOSE_ACCOUNTS,
									ArrayUtils.toString(mAccountIds, ',', false));
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
					final String text = extras.getString(INTENT_KEY_TEXT);
					final String append = extras.getString(INTENT_KEY_APPEND_TEXT);
					final Uri image_uri = extras.getParcelable(INTENT_KEY_IMAGE_URI);
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
		final String text = mEditText != null ? parseString(mEditText.getText()) : null;
		if (!isEmpty(text) || mImageUri != null) {
			new UnsavedTweetDialogFragment().show(getSupportFragmentManager(), "unsaved_tweet");
			return;
		}
		new DiscardTweetTask(this).execute();
	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.image_thumbnail_preview: {
				openImageMenu();
				break;
			}
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mColorIndicator = (ColorView) findViewById(R.id.account_colors);
		mEditText = (EditText) findViewById(R.id.edit_text);
		mTextCount = (TextView) findViewById(R.id.text_count);
		mImageThumbnailPreview = (ImageView) findViewById(R.id.image_thumbnail_preview);
		mMenuBar = (MenuBar) findViewById(R.id.menu_bar);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mTwitterWrapper = getTwidereApplication().getTwitterWrapper();
		mResolver = getContentResolver();
		super.onCreate(savedInstanceState);
		ActivityAccessor.setFinishOnTouchOutside(this, false);
		final long[] account_ids = getAccountIds(this);
		if (account_ids.length <= 0) {
			final Intent intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
			intent.setClass(this, SignInActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		setContentView(R.layout.compose);
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mImageThumbnailPreview.setOnClickListener(this);
		mImageThumbnailPreview.setOnLongClickListener(this);
		mMenuBar.setOnMenuItemClickListener(this);
		mEditText.setOnEditorActionListener(mPreferences.getBoolean(PREFERENCE_KEY_QUICK_SEND, false) ? this : null);
		mEditText.addTextChangedListener(this);

		final Intent intent = getIntent();
		final String action = intent.getAction();
		
		if (savedInstanceState != null) {
			// Restore from previous saved state
			mAccountIds = savedInstanceState.getLongArray(INTENT_KEY_IDS);
			mIsImageAttached = savedInstanceState.getBoolean(INTENT_KEY_IS_IMAGE_ATTACHED);
			mIsPhotoAttached = savedInstanceState.getBoolean(INTENT_KEY_IS_PHOTO_ATTACHED);
			mIsPossiblySensitive = savedInstanceState.getBoolean(INTENT_KEY_IS_POSSIBLY_SENSITIVE);
			mContentModified = savedInstanceState.getBoolean(INTENT_KEY_CONTENT_MODIFIED);
			mImageUri = savedInstanceState.getParcelable(INTENT_KEY_IMAGE_URI);
			mInReplyToStatus = savedInstanceState.getParcelable(INTENT_KEY_STATUS);
			mInReplyToStatusId = savedInstanceState.getLong(INTENT_KEY_STATUS_ID);
			mMentionUser = savedInstanceState.getParcelable(INTENT_KEY_USER);
			mDraftItem = savedInstanceState.getParcelable(INTENT_KEY_DRAFT);
			mShouldSaveAccounts = savedInstanceState.getBoolean(INTENT_KEY_SHOULD_SAVE_ACCOUNTS, false);
		} else {
			// The activity was first created
			final Bundle extras = intent.getExtras();
			final int notification_id = extras != null ? extras.getInt(INTENT_KEY_NOTIFICATION_ID, -1) : -1;
			if (notification_id != -1) {
				mTwitterWrapper.clearNotification(notification_id);
			}
			if (!handleIntent(action, extras)) {
				handleDefaultIntent(intent);
			}
			if (mAccountIds == null || mAccountIds.length == 0) {
				final long[] ids_in_prefs = ArrayUtils.fromString(mPreferences.getString(
						PREFERENCE_KEY_COMPOSE_ACCOUNTS, null), ',');
				final long[] intersection = ArrayUtils.intersection(ids_in_prefs, account_ids);
				mAccountIds = intersection.length > 0 ? intersection : account_ids;
				if (mAccountIds.length == 1 && mAccountIds[0] != getDefaultAccountId(this)) {
					mAccountIds[0] = getDefaultAccountId(this);
				}
			}
		}
		if (!setComposeTitle(action)) {
			setTitle(R.string.compose);
		}

		reloadAttachedImageThumbnail();

		mMenuBar.inflate(R.menu.menu_compose);
		final Menu menu = mMenuBar.getMenu();
		final MenuItem more_submenu = menu.findItem(R.id.more_submenu);
		if (more_submenu != null) {
			final Intent extensions_intent = new Intent(INTENT_ACTION_EXTENSION_COMPOSE);
			addIntentToMenu(this, more_submenu.getSubMenu(), extensions_intent);
		}
		mMenuBar.show();
		setMenu();
		mColorIndicator.setColors(getAccountColors(this, mAccountIds));
	}

	@Override
	public Loader<Bitmap> onCreateLoader(final int id, final Bundle args) {
		final Uri uri = args.getParcelable(INTENT_KEY_URI);
		return new AttachedImageThumbnailLoader(this, uri);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_compose_actionbar, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onEditorAction(final TextView view, final int actionId, final KeyEvent event) {
		if (event == null) return false;
		switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_ENTER: {
				sendStatus();
				return true;
			}
		}
		return false;
	}

	@Override
	public void onLoaderReset(final Loader<Bitmap> loader) {
		mImageThumbnailPreview.setImageBitmap(null);
	}

	@Override
	public void onLoadFinished(final Loader<Bitmap> loader, final Bitmap data) {
		mImageThumbnailPreview.setVisibility(data != null ? View.VISIBLE : View.GONE);
		mImageThumbnailPreview.setImageBitmap(data);
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (mRecentLocation == null) {
			mRecentLocation = location != null ? new ParcelableLocation(location) : null;
			setSupportProgressBarIndeterminateVisibility(false);
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
		switch (item.getItemId()) {
			case MENU_TAKE_PHOTO: {
				if (!mIsPhotoAttached) {
					takePhoto();
				} else {
					new DeleteImageTask(this).execute();
				}
				break;
			}
			case MENU_ADD_IMAGE: {
				if (!mIsImageAttached) {
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
			case MENU_VIEW: {
				openImageDirectly(this, parseString(mImageUri), null);
				break;
			}
			case MENU_TOGGLE_SENSITIVE: {
				final boolean has_media = (mIsImageAttached || mIsPhotoAttached) && mImageUri != null;
				if (!has_media) return false;
				mIsPossiblySensitive = !mIsPossiblySensitive;
				setMenu();
				break;
			}
			default: {
				final Intent intent = item.getIntent();
				if (intent != null) {
					try {
						if (INTENT_ACTION_EXTENSION_COMPOSE.equals(intent.getAction())) {
							final Bundle extras = new Bundle();
							extras.putString(INTENT_KEY_TEXT, parseString(mEditText.getText()));
							extras.putParcelable(INTENT_KEY_STATUS, mInReplyToStatus);
							intent.putExtras(extras);
							startActivityForResult(intent, REQUEST_EXTENSION_COMPOSE);
						} else if (INTENT_ACTION_EXTENSION_EDIT_IMAGE.equals(intent.getAction())) {
							final ComponentName cmp = intent.getComponent();
							if (cmp != null) {
								grantUriPermission(cmp.getPackageName(), mImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				final String text = mEditText != null ? parseString(mEditText.getText()) : null;
				if (mContentModified && !isEmpty(text)) {
					new UnsavedTweetDialogFragment().show(getSupportFragmentManager(), "unsaved_tweet");
				} else {
					// NavUtils.navigateUpFromSameTask(this);
					onBackPressed();
				}
				break;
			}
			case MENU_SEND: {
				sendStatus();
				break;
			}
			case MENU_SELECT_ACCOUNT: {
				final Intent intent = new Intent(INTENT_ACTION_SELECT_ACCOUNT);
				final Bundle bundle = new Bundle();
				bundle.putBoolean(INTENT_KEY_ACTIVATED_ONLY, false);
				bundle.putLongArray(INTENT_KEY_IDS, mAccountIds);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_SELECT_ACCOUNT);
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		if (menu == null || mEditText == null || mTextCount == null) return false;
		final String text_orig = parseString(mEditText.getText());
		final String text = mImageUri != null ? mUploadUseExtension ? getImageUploadStatus(this, FAKE_IMAGE_LINK,
				text_orig) : text_orig + " " + FAKE_IMAGE_LINK : text_orig;
		final int count = mValidator.getTweetLength(text);
		final boolean exceeded_limit = count < Validator.MAX_TWEET_LENGTH;
		final boolean near_limit = count >= Validator.MAX_TWEET_LENGTH - 10;
		final float hue = exceeded_limit ? near_limit ? 5 * (Validator.MAX_TWEET_LENGTH - count) : 50 : 0;
		final float[] hsv = new float[] { hue, 1.0f, 1.0f };
		mTextCount.setTextColor(count >= Validator.MAX_TWEET_LENGTH - 10 ? Color.HSVToColor(0x80, hsv) : 0x80808080);
		mTextCount.setText(parseString(Validator.MAX_TWEET_LENGTH - count));
		final MenuItem sendItem = menu.findItem(MENU_SEND);
		if (sendItem != null) {
			sendItem.setEnabled(text_orig.length() > 0);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onProviderDisabled(final String provider) {
		setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onProviderEnabled(final String provider) {
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putLongArray(INTENT_KEY_ACCOUNT_IDS, mAccountIds);
		outState.putBoolean(INTENT_KEY_IS_IMAGE_ATTACHED, mIsImageAttached);
		outState.putBoolean(INTENT_KEY_IS_PHOTO_ATTACHED, mIsPhotoAttached);
		outState.putParcelable(INTENT_KEY_IMAGE_URI, mImageUri);
		outState.putBoolean(INTENT_KEY_CONTENT_MODIFIED, mContentModified);
		outState.putBoolean(INTENT_KEY_IS_POSSIBLY_SENSITIVE, mIsPossiblySensitive);
		outState.putParcelable(INTENT_KEY_STATUS, mInReplyToStatus);
		outState.putLong(INTENT_KEY_STATUS_ID, mInReplyToStatusId);
		outState.putParcelable(INTENT_KEY_USER, mMentionUser);
		outState.putParcelable(INTENT_KEY_DRAFT, mDraftItem);
		outState.putBoolean(INTENT_KEY_SHOULD_SAVE_ACCOUNTS, mShouldSaveAccounts);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStatusChanged(final String provider, final int status, final Bundle extras) {

	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		invalidateSupportOptionsMenu();
		mContentModified = true;
	}

	public void saveToDrafts() {
		final String text = mEditText != null ? parseString(mEditText.getText()) : null;
		final ContentValues values = new ContentValues();
		values.put(Drafts.TEXT, text);
		values.put(Drafts.ACCOUNT_IDS, ArrayUtils.toString(mAccountIds, ',', false));
		values.put(Drafts.IN_REPLY_TO_STATUS_ID, mInReplyToStatusId);
		values.put(Drafts.LOCATION, ParcelableLocation.toString(mRecentLocation));
		values.put(Drafts.IS_POSSIBLY_SENSITIVE, mIsPossiblySensitive);
		if (mImageUri != null) {
			values.put(Drafts.IS_IMAGE_ATTACHED, mIsImageAttached);
			values.put(Drafts.IS_PHOTO_ATTACHED, mIsPhotoAttached);
			values.put(Drafts.IMAGE_URI, parseString(mImageUri));
		}
		mResolver.insert(Drafts.CONTENT_URI, values);
	}

	@Override
	protected void onStart() {
		super.onStart();
		final String uploader_component = mPreferences.getString(PREFERENCE_KEY_IMAGE_UPLOADER, null);
		mUploadUseExtension = !isEmpty(uploader_component);
		if (mMenuBar != null) {
			setMenu();
		}
		final int text_size = mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mEditText.setTextSize(text_size * 1.25f);
	}

	@Override
	protected void onStop() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		mLocationManager.removeUpdates(this);
		super.onStop();
	}

	private Uri createTempImageUri() {
		final File file = new File(getCacheDir(), "tmp_image_" + System.currentTimeMillis());
		return Uri.fromFile(file);
	}
	private boolean handleIntent(final String action, final Bundle extras) {
		mShouldSaveAccounts = false;
		if (extras == null) return false;
		mMentionUser = extras.getParcelable(INTENT_KEY_USER);
		mInReplyToStatus = extras.getParcelable(INTENT_KEY_STATUS);
		mInReplyToStatusId = mInReplyToStatus != null ? mInReplyToStatus.status_id : -1;
		if (INTENT_ACTION_REPLY.equals(action)) {
			return handleReplyIntent(mInReplyToStatus);
		} else if (INTENT_ACTION_QUOTE.equals(action)) {
			return handleQuoteIntent(mInReplyToStatus);
		} else if (INTENT_ACTION_EDIT_DRAFT.equals(action)) {
			mDraftItem = extras.getParcelable(INTENT_KEY_DRAFT);
			return handleEditDraftIntent(mDraftItem);
		} else if (INTENT_ACTION_MENTION.equals(action)) {
			return handleMentionIntent(mMentionUser);
		} else if (INTENT_ACTION_REPLY_MULTIPLE.equals(action)) {
			final String[] screen_names = extras.getStringArray(INTENT_KEY_SCREEN_NAMES);
			final long account_id = extras.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			final long in_reply_to_user_id = extras.getLong(INTENT_KEY_IN_REPLY_TO_ID, -1);
			return handleReplyMultipleIntent(screen_names, account_id, in_reply_to_user_id);
		}
		// Unknown action or no intent extras
		return false;
	}

	private boolean handleDefaultIntent(final Intent intent) {
		mShouldSaveAccounts = true;
		if (intent == null) return false;
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
			mIsImageAttached = true;
			mIsPhotoAttached = false;
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
		mIsImageAttached = draft.is_image_attached;
		mIsPhotoAttached = draft.is_photo_attached;
		mIsPossiblySensitive = draft.is_possibly_sensitive;
		return true;
	}

	private boolean handleMentionIntent(final ParcelableUser status) {
		if (status == null || status.user_id <= 0) return false;
		final String my_screen_name = getAccountScreenName(this, status.account_id);
		if (isEmpty(my_screen_name)) return false;
		mEditText.setText("@" + status.screen_name + " ");
		final int selection_end = mEditText.length();
		mEditText.setSelection(selection_end);
		mAccountIds = new long[] { status.account_id };
		return true;
	}
	
	private boolean handleQuoteIntent(final ParcelableStatus status) {
		if (status == null || status.status_id <= 0) return false;
		mEditText.setText(getQuoteStatus(this, status.screen_name, status.text_plain));
		mEditText.setSelection(0);
		mAccountIds = new long[] { status.account_id };
		return true;
	}
	
	private boolean handleReplyIntent(final ParcelableStatus status) {
		if (status == null || status.status_id <= 0) return false;
		final String my_screen_name = getAccountScreenName(this, status.account_id);
		if (isEmpty(my_screen_name)) return false;
		final Set<String> mentions = new Extractor().extractMentionedScreennames(status.text_plain);
		mEditText.append("@" + status.screen_name + " ");
		final int selection_start = mEditText.length();
		for (final String screen_name : mentions) {
			if (screen_name.equalsIgnoreCase(status.screen_name) || screen_name.equalsIgnoreCase(my_screen_name)) {
				continue;
			}
			mEditText.append("@" + screen_name + " ");
		}
		final int selection_end = mEditText.length();
		mEditText.setSelection(selection_start, selection_end);
		mAccountIds = new long[] { status.account_id };
		return true;
	}
	
	private boolean handleReplyMultipleIntent(final String[] screen_names, final long account_id, final long in_reply_to_status_id) {
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
				setSupportProgressBarIndeterminateVisibility(true);
			}
			mRecentLocation = location != null ? new ParcelableLocation(location) : null;
		} else {
			Toast.makeText(this, R.string.cannot_get_location, Toast.LENGTH_SHORT).show();
		}
		return provider != null;
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
			showErrorToast(this, null, e, false);
		}
	}

	private void reloadAttachedImageThumbnail() {
		final LoaderManager lm = getSupportLoaderManager();
		lm.destroyLoader(0);
		final Bundle args = new Bundle();
		args.putParcelable(INTENT_KEY_URI, mImageUri);
		if (mLoaderInitialized) {
			lm.restartLoader(0, args, this);
		} else {
			lm.initLoader(0, args, this);
			mLoaderInitialized = true;
		}
	}

	private void sendStatus() {
		final String text = mEditText != null ? parseString(mEditText.getText()) : null;
		if (isEmpty(text) || isFinishing()) return;
		final boolean has_media = (mIsImageAttached || mIsPhotoAttached) && mImageUri != null;
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
		mTwitterWrapper.updateStatus(mAccountIds, text, attach_location ? mRecentLocation : null, mImageUri,
				mInReplyToStatusId, has_media && mIsPossiblySensitive, mIsPhotoAttached && !mIsImageAttached);
		setResult(Activity.RESULT_OK);
		finish();
	}
	
	private boolean setComposeTitle(final String action) {
		final boolean display_screen_name = NAME_DISPLAY_OPTION_SCREEN_NAME.equals(mPreferences.getString(
				PREFERENCE_KEY_NAME_DISPLAY_OPTION, NAME_DISPLAY_OPTION_BOTH));
		if (INTENT_ACTION_REPLY.equals(action)) {
			if (mInReplyToStatus == null) return false;
			setTitle(getString(R.string.reply_to, display_screen_name ? "@" + mInReplyToStatus.screen_name : mInReplyToStatus.name));
		} else if (INTENT_ACTION_QUOTE.equals(action)) {
			if (mInReplyToStatus == null) return false;
			setTitle(getString(R.string.quote_user, display_screen_name ? "@" + mInReplyToStatus.screen_name : mInReplyToStatus.name));
			mActionBar.setSubtitle(mInReplyToStatus.is_protected && mInReplyToStatus.account_id != mInReplyToStatus.user_id ?
					getString(R.string.quote_protected_tweet_notice) : null);
		} else if (INTENT_ACTION_EDIT_DRAFT.equals(action)) {
			if (mDraftItem == null) return false;
			setTitle(R.string.edit_draft);
		} else if (INTENT_ACTION_MENTION.equals(action)) {
			if (mMentionUser == null) return false;
			setTitle(getString(R.string.mention_user, display_screen_name ? "@" + mMentionUser.screen_name : mMentionUser.name));
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
		final Menu menu = mMenuBar.getMenu();
		final int activated_color = getResources().getColor(R.color.holo_blue_bright);
		final MenuItem itemAddImage = menu.findItem(MENU_ADD_IMAGE);
		final Drawable iconAddImage = itemAddImage.getIcon().mutate();
		if (mIsImageAttached && !mIsPhotoAttached) {
			iconAddImage.setColorFilter(activated_color, Mode.MULTIPLY);
			itemAddImage.setTitle(R.string.remove_image);
		} else {
			iconAddImage.clearColorFilter();
			itemAddImage.setTitle(R.string.add_image);
		}
		final MenuItem itemTakePhoto = menu.findItem(MENU_TAKE_PHOTO);
		final Drawable iconTakePhoto = itemTakePhoto.getIcon().mutate();
		if (!mIsImageAttached && mIsPhotoAttached) {
			iconTakePhoto.setColorFilter(activated_color, Mode.MULTIPLY);
			itemTakePhoto.setTitle(R.string.remove_photo);
		} else {
			iconTakePhoto.clearColorFilter();
			itemTakePhoto.setTitle(R.string.take_photo);
		}
		final MenuItem itemAttachLocation = menu.findItem(MENU_ADD_LOCATION);
		final Drawable iconAttachLocation = itemAttachLocation.getIcon().mutate();
		final boolean attach_location = mPreferences.getBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false);
		if (attach_location && getLocation()) {
			iconAttachLocation.setColorFilter(activated_color, Mode.MULTIPLY);
			itemAttachLocation.setTitle(R.string.remove_location);
		} else {
			mPreferences.edit().putBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false).commit();
			iconAttachLocation.clearColorFilter();
			itemAttachLocation.setTitle(R.string.add_location);
		}
		final MenuItem itemMore = menu.findItem(R.id.more_submenu);
		final MenuItem itemDrafts = menu.findItem(MENU_DRAFTS);
		final MenuItem itemToggleSensitive = menu.findItem(MENU_TOGGLE_SENSITIVE);
		if (itemMore != null) {
			if (itemDrafts != null) {
				final Cursor drafts_cur = getContentResolver().query(Drafts.CONTENT_URI, new String[0], null, null,
						null);
				final Drawable iconMore = itemMore.getIcon().mutate();
				final Drawable iconDrafts = itemDrafts.getIcon().mutate();
				if (drafts_cur.getCount() > 0) {
					iconMore.setColorFilter(activated_color, Mode.MULTIPLY);
					iconDrafts.setColorFilter(activated_color, Mode.MULTIPLY);
				} else {
					iconMore.clearColorFilter();
					iconDrafts.clearColorFilter();
				}
				drafts_cur.close();
			}
			if (itemToggleSensitive != null) {
				final boolean has_media = (mIsImageAttached || mIsPhotoAttached) && mImageUri != null;
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
		mMenuBar.show();
		invalidateSupportOptionsMenu();
	}

	private void takePhoto() {
		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			final File cache_dir = EnvironmentAccessor.getExternalCacheDir(this);
			final File file = new File(cache_dir, "tmp_photo_" + System.currentTimeMillis());
			mTempPhotoUri = Uri.fromFile(file);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPhotoUri);
			try {
				startActivityForResult(intent, REQUEST_TAKE_PHOTO);
			} catch (final ActivityNotFoundException e) {
				showErrorToast(this, null, e, false);
			}
		}
	}

	public static final class AttachedImageThumbnailLoader extends AsyncTaskLoader<Bitmap> {

		private final Uri uri;

		public AttachedImageThumbnailLoader(final Context context, final Uri uri) {
			super(context);
			this.uri = uri;
		}

		@Override
		public Bitmap loadInBackground() {
			if (uri == null) return null;
			final String path = uri.getPath();
			final Context context = getContext();
			final float density = context.getResources().getDisplayMetrics().density;
			final int thumbnail_size_px = (int) (THUMBNAIL_SIZE * density);
			final BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, o);
			if (o.outWidth == 0 || o.outHeight == 0) return null;
			o.inJustDecodeBounds = false;
			o.inSampleSize = Math.round(Math.max(o.outWidth, o.outHeight) / thumbnail_size_px);
			return BitmapDecodeHelper.decode(path, o);
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}
	}

	public static class UnsavedTweetDialogFragment extends BaseDialogFragment implements
			DialogInterface.OnClickListener {

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			final FragmentActivity activity = getActivity();
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
						new DiscardTweetTask(((ComposeActivity) activity)).execute();
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

	static class CopyImageTask extends AsyncTask<Void, Void, Boolean> {

		private static final String PROGRESS_FRAGMENT_TAG = "copy_image_progress";
	
		final ComposeActivity activity;
		final boolean delete_orig;
		final Uri old, src, dst;
		
		CopyImageTask(final ComposeActivity activity, final Uri old, final Uri src, final Uri dst, final boolean delete_orig) {
			this.activity = activity;
			this.old = old;
			this.src = src;
			this.dst = dst;
			this.delete_orig = delete_orig;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
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
			} catch (Exception e) {
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPreExecute() {
			final DialogFragment f = new ProgressDialogFragment();
			f.setCancelable(false);
			f.show(activity.getSupportFragmentManager(), PROGRESS_FRAGMENT_TAG);
		}
		
		@Override
		protected void onPostExecute(final Boolean result) {
			final FragmentManager fm = activity.getSupportFragmentManager();
			final DialogFragment f = (DialogFragment) fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG);
			if (f != null) {
				f.dismiss();
			}
			activity.mImageUri = dst;
			activity.setMenu();
			activity.reloadAttachedImageThumbnail();
			if (!result) {
				Toast.makeText(activity, R.string.error_occurred, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	static class DeleteImageTask extends AsyncTask<Uri, Void, Boolean> {

		private static final String PROGRESS_FRAGMENT_TAG = "delete_file_progress";

		final ComposeActivity activity;

		DeleteImageTask(final ComposeActivity activity) {
			this.activity = activity;
		}

		@Override
		protected Boolean doInBackground(Uri... params) {
			if (params == null) return false;
			try {
				final Uri uri = activity.mImageUri;
				if (uri != null && ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
					new File(uri.getPath()).delete();
				}
				for (final Uri target : params) {
					if (target == null) continue;
					if (ContentResolver.SCHEME_FILE.equals(target.getScheme())) {
						new File(target.getPath()).delete();
					}
				}
			} catch (Exception e) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPreExecute() {
			final DialogFragment f = new ProgressDialogFragment();
			f.setCancelable(false);
			f.show(activity.getSupportFragmentManager(), PROGRESS_FRAGMENT_TAG);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			final FragmentManager fm = activity.getSupportFragmentManager();
			final DialogFragment f = (DialogFragment) fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG);
			if (f != null) {
				f.dismiss();
			}
			activity.mImageUri = null;
			activity.mIsImageAttached = false;
			activity.mIsPhotoAttached = false;
			activity.mIsPossiblySensitive = false;
			activity.setMenu();
			activity.reloadAttachedImageThumbnail();
			if (!result) {
				Toast.makeText(activity, R.string.error_occurred, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	static class DiscardTweetTask extends AsyncTask<Void, Void, Void> {

		private static final String PROGRESS_FRAGMENT_TAG = "discard_tweet_progress";

		final ComposeActivity activity;

		DiscardTweetTask(final ComposeActivity activity) {
			this.activity = activity;
		}

		@Override
		protected Void doInBackground(Void... params) {
			final Uri uri = activity.mImageUri;
			try {
				if (uri == null) return null;
				if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
					new File(uri.getPath()).delete();
				}
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			final DialogFragment f = new ProgressDialogFragment();
			f.setCancelable(false);
			f.show(activity.getSupportFragmentManager(), PROGRESS_FRAGMENT_TAG);
		}

		@Override
		protected void onPostExecute(Void result) {
			final FragmentManager fm = activity.getSupportFragmentManager();
			final DialogFragment f = (DialogFragment) fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG);
			if (f != null) {
				f.dismiss();
			}
			activity.finish();
		}
	}
}
