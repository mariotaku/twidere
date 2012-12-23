package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.isMyAccount;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.loader.ParcelableUserLoader;
import org.mariotaku.twidere.loader.UserBannerImageLoader;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.view.ProfileNameBannerContainer;
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class EditUserProfileActivity extends BaseDialogWhenLargeActivity implements OnSizeChangedListener, TextWatcher {

	private static final int LOADER_ID_USER = 1;
	private static final int LOADER_ID_BANNER = 2;

	private LazyImageLoader mProfileImageLoader;

	private ProfileNameBannerContainer mProfileNameBannerContainer;
	private ImageView mProfileImageView;
	private EditText mEditName, mEditDescription, mEditLocation, mEditUrl;
	private View mProgress, mContent;

	private boolean mBackPressed;
	private long mAccountId;
	protected int mBannerWidth;
	protected ParcelableUser mUser;

	private boolean mBannerImageLoaderInitialized;

	private final Handler mHandler = new Handler();

	private final LoaderCallbacks<Bitmap> mBannerImageCallback = new LoaderCallbacks<Bitmap>() {

		@Override
		public Loader<Bitmap> onCreateLoader(final int id, final Bundle args) {
			mProfileNameBannerContainer.setBanner(null);
			final int def_width = getResources().getDisplayMetrics().widthPixels;
			final int width = mBannerWidth > 0 ? mBannerWidth : def_width;
			return new UserBannerImageLoader(EditUserProfileActivity.this, mUser, width, true);
		}

		@Override
		public void onLoaderReset(final Loader<Bitmap> loader) {
		}

		@Override
		public void onLoadFinished(final Loader<Bitmap> loader, final Bitmap data) {
			mProfileNameBannerContainer.setBanner(data);
		}

	};

	private final LoaderCallbacks<SingleResponse<ParcelableUser>> mUserInfoLoaderCallbacks = new LoaderCallbacks<SingleResponse<ParcelableUser>>() {

		@Override
		public Loader<SingleResponse<ParcelableUser>> onCreateLoader(final int id, final Bundle args) {
			mProgress.setVisibility(View.VISIBLE);
			mContent.setVisibility(View.GONE);
			setProgressBarIndeterminateVisibility(true);
			return new ParcelableUserLoader(EditUserProfileActivity.this, mAccountId, mAccountId, null, getIntent()
					.getExtras(), false, false);
		}

		@Override
		public void onLoaderReset(final Loader<SingleResponse<ParcelableUser>> loader) {

		}

		@Override
		public void onLoadFinished(final Loader<SingleResponse<ParcelableUser>> loader,
				final SingleResponse<ParcelableUser> data) {
			mUser = data.data;
			if (data.data != null && data.data.user_id > 0) {
				mProgress.setVisibility(View.GONE);
				mContent.setVisibility(View.VISIBLE);
				final ParcelableUser user = data.data;
				mEditName.setText(user.name);
				mEditDescription.setText(user.description);
				mEditLocation.setText(user.location);
				mEditUrl.setText(user.url);
				mProfileImageLoader.displayImage(user.profile_image_url, mProfileImageView);
				getBannerImage();
			} else {
				finish();
			}
			setProgressBarIndeterminateVisibility(false);
		}

	};

	private final Runnable mBackPressTimeoutRunnable = new Runnable() {

		@Override
		public void run() {
			mBackPressed = false;
		}

	};

	@Override
	public void afterTextChanged(final Editable s) {
	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int length, final int start, final int end) {
	}

	@Override
	public void onBackPressed() {
		if (!backPressed()) return;
		super.onBackPressed();
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mProgress = findViewById(R.id.progress);
		mContent = findViewById(R.id.content);
		mProfileNameBannerContainer = (ProfileNameBannerContainer) findViewById(R.id.profile_name_banner_container);
		mProfileImageView = (ImageView) findViewById(R.id.profile_image);
		mEditName = (EditText) findViewById(R.id.name);
		mEditDescription = (EditText) findViewById(R.id.description);
		mEditLocation = (EditText) findViewById(R.id.location);
		mEditUrl = (EditText) findViewById(R.id.url);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle extras = getIntent().getExtras();
		if (extras == null || !isMyAccount(this, extras.getLong(INTENT_KEY_ACCOUNT_ID))) {
			finish();
			return;
		}
		mProfileImageLoader = TwidereApplication.getInstance(this).getProfileImageLoader();
		mAccountId = extras.getLong(INTENT_KEY_ACCOUNT_ID);
		setContentView(R.layout.edit_user_profile);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mProfileNameBannerContainer.setOnSizeChangedListener(this);
		mEditName.addTextChangedListener(this);
		mEditDescription.addTextChangedListener(this);
		mEditLocation.addTextChangedListener(this);
		mEditUrl.addTextChangedListener(this);
		getSupportLoaderManager().initLoader(LOADER_ID_USER, null, mUserInfoLoaderCallbacks);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_edit_user_profile, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				onBackPressed();
				break;
			}
			case MENU_SAVE: {
				finish();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final MenuItem save = menu.findItem(MENU_SAVE);
		if (save != null) {
			save.setEnabled(mHasUnsavedChanges());
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onSizeChanged(final View view, final int w, final int h, final int oldw, final int oldh) {
	}

	@Override
	public void onTextChanged(final CharSequence s, final int length, final int start, final int end) {
		invalidateSupportOptionsMenu();
	}

	private boolean backPressed() {
		if (!mHasUnsavedChanges()) return true;
		mHandler.removeCallbacks(mBackPressTimeoutRunnable);
		if (!mBackPressed) {
			Toast.makeText(this, R.string.unsaved_change_back_pressed, Toast.LENGTH_SHORT).show();
			mBackPressed = true;
			mHandler.postDelayed(mBackPressTimeoutRunnable, 2000);
			return false;
		}
		mBackPressed = false;
		return true;
	}

	private void getBannerImage() {
		final LoaderManager lm = getSupportLoaderManager();
		lm.destroyLoader(LOADER_ID_BANNER);
		if (mBannerImageLoaderInitialized) {
			lm.restartLoader(LOADER_ID_BANNER, null, mBannerImageCallback);
		} else {
			lm.initLoader(LOADER_ID_BANNER, null, mBannerImageCallback);
			mBannerImageLoaderInitialized = true;
		}
	}

	boolean mHasUnsavedChanges() {
		if (mUser == null) return false;
		return !stringEquals(mEditName.getText(), mUser.name)
				|| !stringEquals(mEditDescription.getText(), mUser.description)
				|| !stringEquals(mEditLocation.getText(), mUser.location)
				|| !stringEquals(mEditUrl.getText(), mUser.url);
	}

	private static boolean stringEquals(final CharSequence str1, final CharSequence str2) {
		if (str1 == null || str2 == null) return str1 == str2;
		return str1.toString().equals(str2.toString());
	}

}
