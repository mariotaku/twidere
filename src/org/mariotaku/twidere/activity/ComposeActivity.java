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
import static org.mariotaku.twidere.util.Utils.getAccountUsername;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;

import java.io.File;

import org.mariotaku.actionbarcompat.ActionBar;
import org.mariotaku.menubar.MenuBar;
import org.mariotaku.menubar.MenuBar.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.GetExternalCacheDirAccessor;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.view.StatusComposeEditText;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff.Mode;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.Validator;

public class ComposeActivity extends BaseActivity implements TextWatcher, LocationListener, OnMenuItemClickListener,
		OnClickListener, OnLongClickListener {

	private ActionBar mActionBar;

	private static final int THUMBNAIL_SIZE = 36;

	private String mText;

	private Uri mImageUri;
	private StatusComposeEditText mEditText;
	private TextView mTextCount;
	private ImageView mImageThumbnailPreview;
	private MenuBar mMenuBar;
	private boolean mIsImageAttached, mIsPhotoAttached;
	private long[] mAccountIds;
	private ServiceInterface mInterface;
	private Location mRecentLocation;
	private LocationManager mLocationManager;
	private SharedPreferences mPreferences;
	private long mInReplyToStatusId = -1, mAccountId = -1;
	private String mInReplyToScreenName, mInReplyToName;
	private boolean mIsQuote;
	private final Validator mValidator = new Validator();

	private AttachedImageThumbnailTask mAttachedImageThumbnailTask;

	private static final String INTENT_KEY_IMAGE_URI = "image_uri";

	private static final String INTENT_KEY_PHOTO_ATTACHED = "photo_attached";

	private static final String INTENT_KEY_IMAGE_ATTACHED = "image_attached";

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		switch (requestCode) {
			case REQUEST_TAKE_PHOTO:
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
				}
				break;
			case REQUEST_PICK_IMAGE:
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
			case REQUEST_SELECT_ACCOUNT:
				if (resultCode == Activity.RESULT_OK) {
					final Bundle bundle = intent.getExtras();
					if (bundle == null) {
						break;
					}
					final long[] user_ids = bundle.getLongArray(INTENT_KEY_IDS);
					if (user_ids != null) {
						mAccountIds = user_ids;
					}
				}
				break;
		}

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.image_thumbnail_preview: {
				if (mImageUri != null) {
					final Intent intent = new Intent(INTENT_ACTION_VIEW_IMAGE, mImageUri);
					startActivity(intent);
				}
				break;
			}
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mInterface = ((TwidereApplication) getApplication()).getServiceInterface();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose);
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);

		mEditText = (StatusComposeEditText) findViewById(R.id.edit_text);
		mTextCount = (TextView) findViewById(R.id.text_count);
		mImageThumbnailPreview = (ImageView) findViewById(R.id.image_thumbnail_preview);
		mMenuBar = (MenuBar) findViewById(R.id.menu_bar);

		final Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
		mAccountIds = bundle != null ? bundle.getLongArray(INTENT_KEY_IDS) : null;
		mAccountId = bundle != null ? bundle.getLong(INTENT_KEY_ACCOUNT_ID) : -1;
		mInReplyToStatusId = bundle != null ? bundle.getLong(INTENT_KEY_IN_REPLY_TO_ID) : -1;
		mInReplyToScreenName = bundle != null ? bundle.getString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME) : null;
		mInReplyToName = bundle != null ? bundle.getString(INTENT_KEY_IN_REPLY_TO_NAME) : null;
		mIsImageAttached = bundle != null ? bundle.getBoolean(INTENT_KEY_IMAGE_ATTACHED) : false;
		mIsPhotoAttached = bundle != null ? bundle.getBoolean(INTENT_KEY_PHOTO_ATTACHED) : false;
		mImageUri = (Uri) (bundle != null ? bundle.getParcelable(INTENT_KEY_IMAGE_URI) : null);
		int text_selection_start = -1;
		if (mInReplyToStatusId > 0) {
			final String account_username = getAccountUsername(this, mAccountId);

			final String[] mentions = getIntent().getExtras() != null ? getIntent().getExtras().getStringArray(
					INTENT_KEY_MENTIONS) : null;

			if (bundle != null && bundle.getString(INTENT_KEY_TEXT) != null
					&& (mentions == null || mentions.length < 1)) {
				mText = bundle.getString(INTENT_KEY_TEXT);
			} else if (mentions != null) {
				final StringBuilder builder = new StringBuilder();
				for (final String mention : mentions) {
					if (mentions.length == 1 && mentions[0].equals(account_username)) {
						builder.append('@' + account_username + ' ');
					}
					if (!mention.equals(account_username)) {
						builder.append('@' + mention + ' ');
					}
				}
				mText = builder.toString();
				text_selection_start = mText.indexOf(' ') + 1;
			}

			mIsQuote = bundle != null ? bundle.getBoolean(INTENT_KEY_IS_QUOTE, false) : false;

			final boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
			final String name = display_name ? mInReplyToName : mInReplyToScreenName;
			if (name != null) {
				setTitle(getString(mIsQuote ? R.string.quote_user : R.string.reply_to, name));
			}
			if (mAccountIds == null || mAccountIds.length == 0) {
				mAccountIds = new long[] { mAccountId };
			}
		} else {
			if (mAccountIds == null || mAccountIds.length == 0) {
				mAccountIds = getActivatedAccountIds(this);
			}
			final String action = getIntent().getAction();
			if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
				setTitle(R.string.share);
				final Bundle extras = getIntent().getExtras();
				if (extras != null) {
					if (mText == null) {
						final CharSequence extra_text = extras.getCharSequence(Intent.EXTRA_TEXT);
						mText = extra_text != null ? String.valueOf(extra_text) : "";
					} else {
						mText = bundle.getString(INTENT_KEY_TEXT);
					}
					if (mImageUri == null) {
						final Uri extra_stream = extras.getParcelable(Intent.EXTRA_STREAM);
						final String content_type = getIntent().getType();
						if (extra_stream != null && content_type != null && content_type.startsWith("image/")) {
							final String real_path = getImagePathFromUri(this, extra_stream);
							final File file = new File(real_path);
							if (file.exists()) {
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
		mEditText.setMovementMethod(ArrowKeyMovementMethod.getInstance());
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_compose_actionbar, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/** Sets the mRecentLocation object to the current location of the device **/
	@Override
	public void onLocationChanged(Location location) {
		mRecentLocation = location;
	}

	@Override
	public boolean onLongClick(View view) {
		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
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
			case MENU_SELECT_ACCOUNT: {
				final Intent intent = new Intent(INTENT_ACTION_SELECT_ACCOUNT);
				final Bundle bundle = new Bundle();
				bundle.putLongArray(INTENT_KEY_IDS, mAccountIds);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_SELECT_ACCOUNT);
				break;
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				finish();
				break;
			}
			case MENU_SEND: {
				final String text = mEditText != null ? mEditText.getText().toString() : null;
				if (mValidator.isValidTweet(text)) {
					final boolean attach_location = mPreferences.getBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false);
					mInterface.updateStatus(mAccountIds, text, attach_location ? mRecentLocation : null, mImageUri,
							mInReplyToStatusId, mIsPhotoAttached && !mIsImageAttached);
					setResult(Activity.RESULT_OK);
					finish();
				}
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final String text = mEditText != null ? String.valueOf(mEditText.getText()) : null;
		if (mTextCount != null) {
			mTextCount.setText(String.valueOf(mValidator.getTweetLength(text)));
		}
		final MenuItem sendItem = menu.findItem(MENU_SEND);
		sendItem.setEnabled(mValidator.isValidTweet(text));
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		mText = mEditText.getText().toString();
		outState.putLongArray(INTENT_KEY_IDS, mAccountIds);
		outState.putString(INTENT_KEY_TEXT, mText);
		outState.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
		outState.putLong(INTENT_KEY_IN_REPLY_TO_ID, mInReplyToStatusId);
		outState.putString(INTENT_KEY_IN_REPLY_TO_NAME, mInReplyToName);
		outState.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, mInReplyToScreenName);
		outState.putBoolean(INTENT_KEY_IS_QUOTE, mIsQuote);
		outState.putBoolean(INTENT_KEY_IMAGE_ATTACHED, mIsImageAttached);
		outState.putBoolean(INTENT_KEY_PHOTO_ATTACHED, mIsPhotoAttached);
		outState.putParcelable(INTENT_KEY_IMAGE_URI, mImageUri);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		invalidateSupportOptionsMenu();
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
			mRecentLocation = mLocationManager.getLastKnownLocation(provider);
		} else {
			Toast.makeText(this, R.string.cannot_get_location, Toast.LENGTH_SHORT).show();
		}
		return provider != null;
	}

	private void pickImage() {
		final Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, REQUEST_PICK_IMAGE);
	}

	private void reloadAttachedImageThumbnail(File file) {
		if (mAttachedImageThumbnailTask != null && mAttachedImageThumbnailTask.getStatus() == AsyncTask.Status.RUNNING) {
			mAttachedImageThumbnailTask.cancel(true);
		}
		mAttachedImageThumbnailTask = new AttachedImageThumbnailTask(file);
		mAttachedImageThumbnailTask.execute();
	}

	private void setMenu(Menu menu) {
		final int activated_color = getResources().getColor(R.color.holo_blue_bright);
		final MenuItem itemAddImage = menu.findItem(MENU_ADD_IMAGE);
		if (mIsImageAttached && !mIsPhotoAttached) {
			itemAddImage.getIcon().setColorFilter(activated_color, Mode.MULTIPLY);
			itemAddImage.setTitle(R.string.remove_image);
		} else {
			itemAddImage.getIcon().clearColorFilter();
			itemAddImage.setTitle(R.string.add_image);
		}
		final MenuItem itemTakePhoto = menu.findItem(MENU_TAKE_PHOTO);
		if (!mIsImageAttached && mIsPhotoAttached) {
			itemTakePhoto.getIcon().setColorFilter(activated_color, Mode.MULTIPLY);
			itemTakePhoto.setTitle(R.string.remove_photo);
		} else {
			itemTakePhoto.getIcon().clearColorFilter();
			itemTakePhoto.setTitle(R.string.take_photo);
		}
		final MenuItem itemAttachLocation = menu.findItem(MENU_ADD_LOCATION);
		final boolean attach_location = mPreferences.getBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false);
		if (attach_location && getLocation()) {
			itemAttachLocation.getIcon().setColorFilter(activated_color, Mode.MULTIPLY);
			itemAttachLocation.setTitle(R.string.remove_location);
		} else {
			mPreferences.edit().putBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false).commit();
			itemAttachLocation.getIcon().clearColorFilter();
			itemAttachLocation.setTitle(R.string.add_location);
		}
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
			startActivityForResult(intent, REQUEST_TAKE_PHOTO);
		}
	}

	private class AttachedImageThumbnailTask extends AsyncTask<Void, Void, Bitmap> {

		private final File file;

		public AttachedImageThumbnailTask(File file) {
			this.file = file;
		}

		@Override
		protected Bitmap doInBackground(Void... args) {
			if (file != null && file.exists()) {
				final int thumbnail_size_px = (int) (THUMBNAIL_SIZE * getResources().getDisplayMetrics().density);
				final BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(file.getPath(), o);
				final int tmp_width = o.outWidth;
				final int tmp_height = o.outHeight;
				if (tmp_width == 0 || tmp_height == 0) return null;
				final BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = Math.round(Math.max(tmp_width, tmp_height) / thumbnail_size_px);
				return BitmapFactory.decodeFile(file.getPath(), o2);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			mImageThumbnailPreview.setImageBitmap(result);
			super.onPostExecute(result);
		}

	}
}
