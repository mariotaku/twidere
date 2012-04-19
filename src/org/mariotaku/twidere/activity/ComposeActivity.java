package org.mariotaku.twidere.activity;

import java.io.File;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.ServiceInterface;

import roboguice.inject.ContentView;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.inject.Inject;

@ContentView(R.layout.compose)
public class ComposeActivity extends BaseActivity implements OnClickListener, TextWatcher,
		LocationListener {

	private ActionBar mActionBar;
	private Uri mImageUri;
	@InjectView(R.id.edit_text) private EditText mEditText;
	@InjectView(R.id.text_count) private TextView mTextCount;
	@InjectView(R.id.send) private ImageButton mSendButton;
	@InjectView(R.id.select_account) private ImageButton mSelectAccount;
	@InjectResource(R.color.holo_blue_bright) private int mActivedMenuColor;
	private boolean mIsImageAttached, mIsPhotoAttached, mIsLocationAttached;
	private long[] mAccountIds;
	private ServiceInterface mInterface;
	private Location mostRecentLocation;
	@Inject private LocationManager mLocationManager;

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
					if (user_ids == null) {
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
				mInterface.updateStatus(mAccountIds, content, mostRecentLocation, mImageUri, -1);
				finish();
				break;
			case R.id.select_account:
				startActivityForResult(new Intent(INTENT_ACTION_SELECT_ACCOUNT),
						REQUEST_SELECT_ACCOUNT);
				break;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLocation();
		mInterface = ((TwidereApplication) getApplication()).getServiceInterface();
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mSendButton.setOnClickListener(this);
		mSelectAccount.setOnClickListener(this);
		mEditText.addTextChangedListener(this);
		int length = mEditText.length();
		mTextCount.setText(String.valueOf(length));
		mSendButton.setEnabled(length > 0 && length <= 140);
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_compose, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/** Sets the mostRecentLocation object to the current location of the device **/
	@Override
	public void onLocationChanged(Location location) {
		mostRecentLocation = location;
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
		if (mIsImageAttached && !mIsPhotoAttached) {
			menu.findItem(MENU_ADD_IMAGE).getIcon()
					.setColorFilter(mActivedMenuColor, Mode.MULTIPLY);
		} else {
			menu.findItem(MENU_ADD_IMAGE).getIcon().clearColorFilter();
		}
		if (!mIsImageAttached && mIsPhotoAttached) {
			menu.findItem(MENU_TAKE_PHOTO).getIcon()
					.setColorFilter(mActivedMenuColor, Mode.MULTIPLY);
		} else {
			menu.findItem(MENU_TAKE_PHOTO).getIcon().clearColorFilter();
		}

		if (mIsLocationAttached) {
			menu.findItem(MENU_ADD_LOCATION).getIcon()
					.setColorFilter(mActivedMenuColor, Mode.MULTIPLY);
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
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

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
		mostRecentLocation = mLocationManager.getLastKnownLocation(provider);
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
