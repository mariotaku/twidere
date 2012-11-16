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

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;
import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.getAccountColors;
import static org.mariotaku.twidere.util.Utils.getAccountIds;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;
import static org.mariotaku.twidere.util.Utils.getImageUploadStatus;
import static org.mariotaku.twidere.util.Utils.getShareStatus;
import static org.mariotaku.twidere.util.Utils.parseString;
import static org.mariotaku.twidere.util.Utils.showErrorToast;

import java.io.File;

import org.mariotaku.actionbarcompat.ActionBar;
import org.mariotaku.menubar.MenuBar;
import org.mariotaku.menubar.MenuBar.OnMenuItemClickListener;
import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.BaseDialogFragment;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.BitmapDecodeHelper;
import org.mariotaku.twidere.util.GetExternalCacheDirAccessor;
import org.mariotaku.twidere.util.ServiceInterface;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NavUtils;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.twitter.Validator;

public class ComposeActivity extends BaseActivity implements TextWatcher, LocationListener, OnMenuItemClickListener,
		OnClickListener, OnLongClickListener, PopupMenu.OnMenuItemClickListener, OnEditorActionListener,
		LoaderCallbacks<Bitmap> {

	private static final String FAKE_IMAGE_LINK = "https://www.example.com/fake_image.jpg";
	private static final String INTENT_KEY_CONTENT_MODIFIED = "content_modified";
	private static final String INTENT_KEY_IS_NAVIGATE_UP = "is_navigate_up";

	private ServiceInterface mService;
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

	private boolean mIsImageAttached, mIsPhotoAttached;
	private long[] mAccountIds;
	private String mText;
	private Uri mImageUri;
	private long mInReplyToStatusId = -1;
	private String mInReplyToScreenName, mInReplyToName;
	private boolean mIsQuote, mUploadUseExtension, mContentModified;

	private DialogFragment mUnsavedTweetDialogFragment;

	private boolean mLoaderInitialized;

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
					final File file = new File(mImageUri.getPath());
					if (file.exists()) {
						mIsImageAttached = false;
						mIsPhotoAttached = true;
						mImageThumbnailPreview.setVisibility(View.VISIBLE);
						reloadAttachedImageThumbnail(file);
					} else {
						mIsPhotoAttached = false;
					}
					setMenu(mMenuBar.getMenu());
				} else {
					mImageUri = null;
				}
				break;
			}
			case REQUEST_PICK_IMAGE: {
				if (resultCode == Activity.RESULT_OK) {
					final Uri uri = intent.getData();
					final File file = uri == null ? null : new File(getImagePathFromUri(this, uri));
					if (file != null && file.exists()) {
						mImageUri = Uri.fromFile(file);
						mIsPhotoAttached = false;
						mIsImageAttached = true;
						mImageThumbnailPreview.setVisibility(View.VISIBLE);
						reloadAttachedImageThumbnail(file);
					} else {
						mIsImageAttached = false;
					}
					setMenu(mMenuBar.getMenu());
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
						if (mInReplyToStatusId <= 0) {
							final SharedPreferences.Editor editor = mPreferences.edit();
							editor.putString(PREFERENCE_KEY_COMPOSE_ACCOUNTS,
									ArrayUtils.toString(mAccountIds, ',', false));
							editor.commit();
						}
						mColorIndicator.setColor(getAccountColors(this, account_ids));
					}
				}
				break;
			}
			case REQUEST_EDIT_IMAGE: {
				if (resultCode == Activity.RESULT_OK) {
					final Uri uri = intent.getData();
					final File file = uri == null ? null : new File(getImagePathFromUri(this, uri));
					if (file != null && file.exists()) {
						mImageUri = Uri.fromFile(file);
						reloadAttachedImageThumbnail(file);
					} else {
						break;
					}
					setMenu(mMenuBar.getMenu());
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
						mText = parseString(mEditText.getText());
					} else if (append != null) {
						mEditText.append(append);
						mText = parseString(mEditText.getText());
					}
					final File file = image_uri == null ? null : new File(getImagePathFromUri(this, image_uri));
					if (file != null && file.exists()) {
						mImageUri = Uri.fromFile(file);
						reloadAttachedImageThumbnail(file);
					}
					setMenu(mMenuBar.getMenu());
				}
				break;
			}
		}

	}

	@Override
	public void onBackPressed() {
		final String text = mEditText != null ? parseString(mEditText.getText()) : null;
		if (mContentModified && !isEmpty(text)) {
			mUnsavedTweetDialogFragment = (DialogFragment) Fragment.instantiate(this,
					UnsavedTweetDialogFragment.class.getName());
			final Bundle args = new Bundle();
			args.putBoolean(INTENT_KEY_IS_NAVIGATE_UP, false);
			mUnsavedTweetDialogFragment.setArguments(args);
			mUnsavedTweetDialogFragment.show(getSupportFragmentManager(), "unsaved_tweet");
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.image_thumbnail_preview: {
				if (mPopupMenu != null) {
					mPopupMenu.dismiss();
				}
				mPopupMenu = PopupMenu.getInstance(this, view);
				mPopupMenu.inflate(R.menu.action_attached_image);
				mPopupMenu.setOnMenuItemClickListener(this);
				mPopupMenu.show();
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
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mService = getTwidereApplication().getServiceInterface();
		mResolver = getContentResolver();
		super.onCreate(savedInstanceState);
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

		final Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
		final long account_id = bundle != null ? bundle.getLong(INTENT_KEY_ACCOUNT_ID) : -1;
		mAccountIds = bundle != null ? bundle.getLongArray(INTENT_KEY_IDS) : null;
		mInReplyToStatusId = bundle != null ? bundle.getLong(INTENT_KEY_IN_REPLY_TO_ID) : -1;
		mInReplyToScreenName = bundle != null ? bundle.getString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME) : null;
		mInReplyToName = bundle != null ? bundle.getString(INTENT_KEY_IN_REPLY_TO_NAME) : null;
		mIsImageAttached = bundle != null ? bundle.getBoolean(INTENT_KEY_IS_IMAGE_ATTACHED) : false;
		mIsPhotoAttached = bundle != null ? bundle.getBoolean(INTENT_KEY_IS_PHOTO_ATTACHED) : false;
		mImageUri = bundle != null ? (Uri) bundle.getParcelable(INTENT_KEY_IMAGE_URI) : null;
		final String[] mentions = bundle != null ? bundle.getStringArray(INTENT_KEY_MENTIONS) : null;
		final String account_screen_name = getAccountScreenName(this, account_id);
		int text_selection_start = -1;
		if (mInReplyToStatusId > 0) {
			if (bundle != null && bundle.getString(INTENT_KEY_TEXT) != null
					&& (mentions == null || mentions.length < 1)) {
				mText = bundle.getString(INTENT_KEY_TEXT);
			} else if (mentions != null) {
				final StringBuilder builder = new StringBuilder();
				for (final String mention : mentions) {
					if (mentions.length == 1 && mentions[0].equalsIgnoreCase(account_screen_name)) {
						builder.append('@' + account_screen_name + ' ');
					} else if (!mention.equalsIgnoreCase(account_screen_name)) {
						builder.append('@' + mention + ' ');
					}
				}
				mText = builder.toString();
				text_selection_start = mText.indexOf(' ') + 1;
			}

			mIsQuote = bundle != null ? bundle.getBoolean(INTENT_KEY_IS_QUOTE, false) : false;

			final boolean display_screen_name = NAME_DISPLAY_OPTION_SCREEN_NAME.equals(mPreferences.getString(
					PREFERENCE_KEY_NAME_DISPLAY_OPTION, NAME_DISPLAY_OPTION_BOTH));
			if (mInReplyToScreenName != null && mInReplyToName != null) {
				setTitle(getString(mIsQuote ? R.string.quote_user : R.string.reply_to,
						display_screen_name ? mInReplyToScreenName : mInReplyToName));
			}
			if (mAccountIds == null || mAccountIds.length == 0) {
				mAccountIds = new long[] { account_id };
			}
		} else {
			if (mentions != null) {
				final StringBuilder builder = new StringBuilder();
				for (final String mention : mentions) {
					if (mentions.length == 1 && mentions[0].equalsIgnoreCase(account_screen_name)) {
						builder.append('@' + account_screen_name + ' ');
					} else if (!mention.equalsIgnoreCase(account_screen_name)) {
						builder.append('@' + mention + ' ');
					}
				}
				mText = builder.toString();
			}
			if (mAccountIds == null || mAccountIds.length == 0) {
				final long[] ids_in_prefs = ArrayUtils.fromString(
						mPreferences.getString(PREFERENCE_KEY_COMPOSE_ACCOUNTS, null), ',');
				final long[] intersection = ArrayUtils.intersection(ids_in_prefs, account_ids);
				mAccountIds = intersection.length > 0 ? intersection : account_ids;
			}
			final String action = getIntent().getAction();
			if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
				setTitle(R.string.share);
				final Bundle extras = getIntent().getExtras();
				if (extras != null) {
					if (mText == null) {
						final CharSequence extra_subject = extras.getCharSequence(Intent.EXTRA_SUBJECT);
						final CharSequence extra_text = extras.getCharSequence(Intent.EXTRA_TEXT);
						mText = getShareStatus(this, parseString(extra_subject), parseString(extra_text));
					} else {
						mText = bundle.getString(INTENT_KEY_TEXT);
					}
					if (mImageUri == null) {
						final Uri extra_stream = extras.getParcelable(Intent.EXTRA_STREAM);
						final String content_type = getIntent().getType();
						if (extra_stream != null && content_type != null && content_type.startsWith("image/")) {
							final String real_path = getImagePathFromUri(this, extra_stream);
							final File file = real_path != null ? new File(real_path) : null;
							if (file != null && file.exists()) {
								mImageUri = Uri.fromFile(file);
								mIsImageAttached = true;
								mIsPhotoAttached = false;
							} else {
								mImageUri = null;
								mIsImageAttached = false;
							}
						}
					}
				}
			} else if (bundle != null) {
				if (bundle.getString(INTENT_KEY_TEXT) != null) {
					mText = bundle.getString(INTENT_KEY_TEXT);
				}
			}
		}

		final File image_file = mImageUri != null && "file".equals(mImageUri.getScheme()) ? new File(
				mImageUri.getPath()) : null;
		final boolean image_file_valid = image_file != null && image_file.exists();
		mImageThumbnailPreview.setVisibility(image_file_valid ? View.VISIBLE : View.GONE);
		if (image_file_valid) {
			reloadAttachedImageThumbnail(image_file);
		}

		mImageThumbnailPreview.setOnClickListener(this);
		mImageThumbnailPreview.setOnLongClickListener(this);
		mMenuBar.setOnMenuItemClickListener(this);
		mMenuBar.inflate(R.menu.menu_compose);
		setMenu(mMenuBar.getMenu());
		mMenuBar.show();
		if (mPreferences.getBoolean(PREFERENCE_KEY_QUICK_SEND, false)) {
			mEditText.setOnEditorActionListener(this);
		}
		mEditText.addTextChangedListener(this);
		if (mText != null) {
			mEditText.setText(mText);
			if (mIsQuote) {
				mEditText.setSelection(0);
			} else if (text_selection_start != -1 && text_selection_start < mEditText.length()
					&& mEditText.length() > 0) {
				mEditText.setSelection(text_selection_start, mEditText.length() - 1);
			} else if (mEditText.length() > 0) {
				mEditText.setSelection(mEditText.length());
			}
		}
		invalidateSupportOptionsMenu();
		mColorIndicator.setOrientation(ColorView.VERTICAL);
		mColorIndicator.setColor(getAccountColors(this, mAccountIds));
		mContentModified = savedInstanceState != null ? savedInstanceState.getBoolean(INTENT_KEY_CONTENT_MODIFIED)
				: false;
	}

	@Override
	public Loader<Bitmap> onCreateLoader(final int id, final Bundle args) {
		return new AttachedImageThumbnailLoader(this, args.getString(INTENT_KEY_FILENAME));
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
				send();
				return true;
			}
		}
		return false;
	}

	@Override
	public void onLoaderReset(final Loader<Bitmap> loader) {
		mImageThumbnailPreview.setVisibility(View.GONE);
		mImageThumbnailPreview.setImageBitmap(null);
	}

	@Override
	public void onLoadFinished(final Loader<Bitmap> loader, final Bitmap data) {
		mImageThumbnailPreview.setVisibility(data != null ? View.VISIBLE : View.GONE);
		mImageThumbnailPreview.setImageBitmap(data);

	}

	/** Sets the mRecentLocation object to the current location of the device **/
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
				onClick(view);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_TAKE_PHOTO: {
				takePhoto();
				break;
			}
			case MENU_ADD_IMAGE: {
				pickImage();
				break;
			}
			case MENU_ADD_LOCATION: {
				final boolean attach_location = mPreferences.getBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false);
				if (!attach_location) {
					getLocation();
				}
				mPreferences.edit().putBoolean(PREFERENCE_KEY_ATTACH_LOCATION, !attach_location).commit();
				setMenu(mMenuBar.getMenu());
				break;
			}
			case MENU_DRAFTS: {
				startActivity(new Intent(INTENT_ACTION_DRAFTS));
				break;
			}
			case MENU_DELETE: {
				if (mImageUri == null) return false;
				if (mIsImageAttached && !mIsPhotoAttached) {
					mImageUri = null;

				} else if (mIsPhotoAttached && !mIsImageAttached) {
					final File image_file = mImageUri != null && "file".equals(mImageUri.getScheme()) ? new File(
							mImageUri.getPath()) : null;
					if (image_file != null) {
						image_file.delete();
					}
					mImageUri = null;
				}
				mIsPhotoAttached = false;
				mIsImageAttached = false;
				reloadAttachedImageThumbnail(null);
				setMenu(mMenuBar.getMenu());
				break;
			}
			case MENU_EDIT: {
				if (mImageUri == null) return false;
				final Intent intent = new Intent(INTENT_ACTION_EXTENSION_EDIT_IMAGE);
				intent.setData(mImageUri);
				startActivityForResult(Intent.createChooser(intent, getString(R.string.open_with_extensions)),
						REQUEST_EDIT_IMAGE);
				break;
			}
			case MENU_VIEW: {
				if (mImageUri != null) {
					final Intent intent = new Intent(INTENT_ACTION_VIEW_IMAGE, mImageUri);
					startActivity(intent);
				}
				break;
			}
			case MENU_EXTENSIONS: {
				final Intent intent = new Intent(INTENT_ACTION_EXTENSION_COMPOSE);
				final Bundle extras = new Bundle();
				final String screen_name = mAccountIds != null && mAccountIds.length > 0 ? getAccountScreenName(this,
						mAccountIds[0]) : null;
				extras.putString(INTENT_KEY_TEXT, parseString(mEditText.getText()));
				extras.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, mInReplyToScreenName);
				extras.putString(INTENT_KEY_IN_REPLY_TO_NAME, mInReplyToName);
				extras.putString(INTENT_KEY_SCREEN_NAME, screen_name);
				extras.putLong(INTENT_KEY_IN_REPLY_TO_ID, mInReplyToStatusId);
				intent.putExtras(extras);
				startActivityForResult(Intent.createChooser(intent, getString(R.string.open_with_extensions)),
						REQUEST_EXTENSION_COMPOSE);
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
					mUnsavedTweetDialogFragment = (DialogFragment) Fragment.instantiate(this,
							UnsavedTweetDialogFragment.class.getName());
					final Bundle args = new Bundle();
					args.putBoolean(INTENT_KEY_IS_NAVIGATE_UP, true);
					mUnsavedTweetDialogFragment.setArguments(args);
					mUnsavedTweetDialogFragment.show(getSupportFragmentManager(), "unsaved_tweet");
				} else {
					NavUtils.navigateUpFromSameTask(this);
				}
				break;
			}
			case MENU_SEND: {
				send();
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
		final String text_orig = mEditText != null ? parseString(mEditText.getText()) : null;
		final String text = mIsPhotoAttached || mIsImageAttached ? mUploadUseExtension ? getImageUploadStatus(this,
				FAKE_IMAGE_LINK, text_orig) : text_orig + " " + FAKE_IMAGE_LINK : text_orig;
		if (mTextCount != null) {
			final int count = mValidator.getTweetLength(text);
			final float hue = count < Validator.MAX_TWEET_LENGTH ? count >= Validator.MAX_TWEET_LENGTH - 10 ? 5 * (Validator.MAX_TWEET_LENGTH - count)
					: 50
					: 0;
			final float[] hsv = new float[] { hue, 1.0f, 1.0f };
			mTextCount
					.setTextColor(count >= Validator.MAX_TWEET_LENGTH - 10 ? Color.HSVToColor(0x80, hsv) : 0x80808080);
			mTextCount.setText(parseString(Validator.MAX_TWEET_LENGTH - count));
		}
		final MenuItem sendItem = menu.findItem(MENU_SEND);
		sendItem.setEnabled(text_orig.length() > 0);
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
		mText = parseString(mEditText.getText());
		outState.putLongArray(INTENT_KEY_IDS, mAccountIds);
		outState.putString(INTENT_KEY_TEXT, mText);
		outState.putLong(INTENT_KEY_IN_REPLY_TO_ID, mInReplyToStatusId);
		outState.putString(INTENT_KEY_IN_REPLY_TO_NAME, mInReplyToName);
		outState.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, mInReplyToScreenName);
		outState.putBoolean(INTENT_KEY_IS_QUOTE, mIsQuote);
		outState.putBoolean(INTENT_KEY_IS_IMAGE_ATTACHED, mIsImageAttached);
		outState.putBoolean(INTENT_KEY_IS_PHOTO_ATTACHED, mIsPhotoAttached);
		outState.putParcelable(INTENT_KEY_IMAGE_URI, mImageUri);
		outState.putBoolean(INTENT_KEY_CONTENT_MODIFIED, mContentModified);
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
		values.put(Drafts.IN_REPLY_TO_NAME, mInReplyToName);
		values.put(Drafts.IN_REPLY_TO_SCREEN_NAME, mInReplyToScreenName);
		values.put(Drafts.IS_QUOTE, mIsQuote ? 1 : 0);
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
			setMenu(mMenuBar.getMenu());
		}
		final int text_size = mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mEditText.setTextSize(text_size * 1.25f);
	}

	@Override
	protected void onStop() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
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
			final Location location = mLocationManager.getLastKnownLocation(provider);
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

	private void pickImage() {
		final Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		try {
			startActivityForResult(i, REQUEST_PICK_IMAGE);
		} catch (final ActivityNotFoundException e) {
			showErrorToast(this, null, e, false);
		}
	}

	private void reloadAttachedImageThumbnail(final File file) {
		if (file == null) return;
		final LoaderManager lm = getSupportLoaderManager();
		lm.destroyLoader(0);
		final Bundle args = new Bundle();
		args.putString(INTENT_KEY_FILENAME, file.getPath());
		if (mLoaderInitialized) {
			lm.restartLoader(0, args, this);
		} else {
			lm.initLoader(0, args, this);
			mLoaderInitialized = true;
		}
	}

	private void send() {
		final String text = mEditText != null ? parseString(mEditText.getText()) : null;
		if (isEmpty(text) || isFinishing()) return;
		final boolean attach_location = mPreferences.getBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false);
		mService.updateStatus(mAccountIds, text, attach_location ? mRecentLocation : null, mImageUri,
				mInReplyToStatusId, mIsPhotoAttached && !mIsImageAttached);
		setResult(Activity.RESULT_OK);
		finish();
	}

	private void setMenu(final Menu menu) {
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
		if (itemMore != null) {
			final Cursor drafts_cur = getContentResolver().query(Drafts.CONTENT_URI, new String[0], null, null, null);
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
		invalidateSupportOptionsMenu();
		mMenuBar.invalidate();
	}

	private void takePhoto() {
		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			final File cache_dir = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? GetExternalCacheDirAccessor
					.getExternalCacheDir(this) : new File(getExternalStorageDirectory().getPath() + "/Android/data/"
					+ getPackageName() + "/cache/");
			final File file = new File(cache_dir, "tmp_photo_" + System.currentTimeMillis() + ".jpg");
			mImageUri = Uri.fromFile(file);
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageUri);
			try {
				startActivityForResult(intent, REQUEST_TAKE_PHOTO);
			} catch (final ActivityNotFoundException e) {
				showErrorToast(this, null, e, false);
			}
		}
	}

	public static final class AttachedImageThumbnailLoader extends AsyncTaskLoader<Bitmap> {

		private final File file;

		public AttachedImageThumbnailLoader(final Context context, final String path) {
			super(context);
			file = path != null ? new File(path) : null;
		}

		@Override
		public Bitmap loadInBackground() {
			if (file != null && file.exists()) {
				final int thumbnail_size_px = (int) (THUMBNAIL_SIZE * getContext().getResources().getDisplayMetrics().density);
				final BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(file.getPath(), o);
				final int tmp_width = o.outWidth;
				final int tmp_height = o.outHeight;
				if (tmp_width == 0 || tmp_height == 0) return null;
				final BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = Math.round(Math.max(tmp_width, tmp_height) / thumbnail_size_px);
				return BitmapDecodeHelper.decode(file.getPath(), o2);
			}
			return null;
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}
	}

	public static class UnsavedTweetDialogFragment extends BaseDialogFragment implements
			DialogInterface.OnClickListener {

		private boolean mIsNavigateUp;

		@Override
		public void onActivityCreated(final Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			final Bundle args = getArguments();
			if (args != null) {
				mIsNavigateUp = args.getBoolean(INTENT_KEY_IS_NAVIGATE_UP);
			}
		}

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			final FragmentActivity activity = getActivity();
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					if (activity instanceof ComposeActivity) {
						((ComposeActivity) activity).saveToDrafts();
						if (mIsNavigateUp) {
							NavUtils.navigateUpFromSameTask(activity);
						} else {
							activity.finish();
						}
					}
					break;
				}
				case DialogInterface.BUTTON_NEGATIVE: {
					if (mIsNavigateUp) {
						NavUtils.navigateUpFromSameTask(activity);
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
			builder.setNeutralButton(android.R.string.cancel, null);
			builder.setNegativeButton(R.string.discard, this);
			return builder.create();
		}

	}
}
