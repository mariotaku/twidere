package org.mariotaku.twidere.activity;

import java.io.File;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.ServiceInterface;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff.Mode;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ComposeActivity extends BaseActivity implements OnClickListener, TextWatcher,
		LocationListener {

	private ActionBar mActionBar;
	private Uri mImageUri;
	private EditText mEditText;
	private TextView mTextCount;
	private ImageButton mSendButton, mSelectAccount;
	private boolean mIsImageAttached, mIsPhotoAttached, mIsLocationAttached;
	private long[] mAccountIds;
	private ServiceInterface mInterface;
	private Location mRecentLocation;
	private LocationManager mLocationManager;

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
				if (resultCode == RESULT_OK) {
					File file = new File(mImageUri.getPath());
					if (file.exists()) {
						mIsImageAttached = false;
						mIsPhotoAttached = true;
					} else {
						mIsPhotoAttached = false;
					}
					invalidateOptionsMenu();
				}
				break;
			case REQUEST_ADD_IMAGE:
				if (resultCode == RESULT_OK) {
					Uri uri = intent.getData();
					File file = uri == null ? null : new File(CommonUtils.getImagePathFromUri(this,
							uri));
					if (file != null && file.exists()) {
						mImageUri = uri;
						mIsPhotoAttached = false;
						mIsImageAttached = true;
					} else {
						mIsImageAttached = false;
					}
					invalidateOptionsMenu();
				}
				break;
			case REQUEST_SELECT_ACCOUNT:
				if (resultCode == RESULT_OK) {
					Bundle bundle = intent.getExtras();
					if (bundle == null) {
						break;
					}
					long[] user_ids = bundle.getLongArray(Accounts.USER_IDS);
					if (user_ids != null) {
						mAccountIds = user_ids;
					}
				}
				break;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.send:
				String content = mEditText != null ? mEditText.getText().toString() : null;
				mInterface.updateStatus(mAccountIds, content, mRecentLocation, mImageUri, -1);
				finish();
				break;
			case R.id.select_account:
				Intent intent = new Intent(INTENT_ACTION_SELECT_ACCOUNT);
				Bundle bundle = new Bundle();
				bundle.putLongArray(Accounts.USER_IDS, mAccountIds);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_SELECT_ACCOUNT);
				break;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		setContentView(R.layout.compose);
		mEditText = (EditText) findViewById(R.id.edit_text);
		mTextCount = (TextView) findViewById(R.id.text_count);
		mSendButton = (ImageButton) findViewById(R.id.send);
		mSelectAccount = (ImageButton) findViewById(R.id.select_account);
		getLocation();
		mInterface = ((TwidereApplication) getApplication()).getServiceInterface();
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mSendButton.setOnClickListener(this);
		mSelectAccount.setOnClickListener(this);
		mEditText.setMovementMethod(ArrowKeyMovementMethod.getInstance());
		mEditText.addTextChangedListener(this);
		int length = mEditText.length();
		mTextCount.setText(String.valueOf(length));
		mSendButton.setEnabled(length > 0 && length <= 140);

		Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
		long[] activated_ids = bundle != null ? bundle.getLongArray(Accounts.USER_IDS) : null;

		if (activated_ids == null) {
			Cursor cur = getContentResolver().query(Accounts.CONTENT_URI,
					new String[] { Accounts.USER_ID }, Accounts.IS_ACTIVATED + "=1", null, null);
			if (cur != null && cur.getCount() > 0) {
				int userid_idx = cur.getColumnIndexOrThrow(Accounts.USER_ID);
				mAccountIds = new long[cur.getCount()];
				cur.moveToFirst();
				int idx = 0;
				while (!cur.isAfterLast()) {
					mAccountIds[idx] = cur.getLong(userid_idx);
					idx++;
					cur.moveToNext();
				}
			}
			if (cur != null) {
				cur.close();
			}
		} else {
			mAccountIds = activated_ids;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_compose, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/** Sets the mRecentLocation object to the current location of the device **/
	@Override
	public void onLocationChanged(Location location) {
		mRecentLocation = location;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME:
				finish();
				break;
			case MENU_TAKE_PHOTO:
				takePhoto();
				break;
			case MENU_ADD_IMAGE:
				pickImage();
				break;
			case MENU_ADD_LOCATION:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		int activated_color = getResources().getColor(android.R.color.holo_blue_bright);
		if (mIsImageAttached && !mIsPhotoAttached) {
			menu.findItem(MENU_ADD_IMAGE).getIcon().setColorFilter(activated_color, Mode.MULTIPLY);
		} else {
			menu.findItem(MENU_ADD_IMAGE).getIcon().clearColorFilter();
		}
		if (!mIsImageAttached && mIsPhotoAttached) {
			menu.findItem(MENU_TAKE_PHOTO).getIcon().setColorFilter(activated_color, Mode.MULTIPLY);
		} else {
			menu.findItem(MENU_TAKE_PHOTO).getIcon().clearColorFilter();
		}

		if (mIsLocationAttached) {
			menu.findItem(MENU_ADD_LOCATION).getIcon()
					.setColorFilter(activated_color, Mode.MULTIPLY);
		} else {
			menu.findItem(MENU_ADD_LOCATION).getIcon().clearColorFilter();
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * The following methods are only necessary because WebMapActivity
	 * implements LocationListener
	 **/
	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putLongArray(Accounts.USER_IDS, mAccountIds);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s != null) {
			int length = s.length();
			mTextCount.setText(String.valueOf(length));
			mSendButton.setEnabled(length > 0 && length <= 140);
		}

	}

	/**
	 * The Location Manager manages location providers. This code searches for
	 * the best provider of data (GPS, WiFi/cell phone tower lookup, some other
	 * mechanism) and finds the last known location.
	 **/
	private void getLocation() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = mLocationManager.getBestProvider(criteria, true);

		// In order to make sure the device is getting location, request
		// updates. locationManager.requestLocationUpdates(provider, 1, 0,
		// this);
		mRecentLocation = mLocationManager.getLastKnownLocation(provider);
	}

	private void pickImage() {
		Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, REQUEST_ADD_IMAGE);
	}

	private void takePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File file = new File(getExternalCacheDir(), "tmp_photo_" + System.currentTimeMillis()
				+ ".jpg");
		mImageUri = Uri.fromFile(file);
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageUri);
		startActivityForResult(intent, REQUEST_TAKE_PHOTO);

	}
}
