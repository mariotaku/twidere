package org.mariotaku.twidere.fragment;

import java.io.File;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.ComposeActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.ServiceInterface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ComposeFragment extends BaseFragment implements OnClickListener, TextWatcher,
		LocationListener {

	private String mText;
	private Uri mImageUri;
	private EditText mEditText;
	private TextView mTextCount;
	private ImageButton mSendButton, mSelectAccount;
	private boolean mIsImageAttached, mIsPhotoAttached;
	private long[] mAccountIds;
	private ServiceInterface mInterface;
	private Location mRecentLocation;
	private LocationManager mLocationManager;
	private SharedPreferences mPreferences;

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mLocationManager = (LocationManager) getSherlockActivity().getSystemService(
				Context.LOCATION_SERVICE);
		mPreferences = getSherlockActivity().getSharedPreferences(PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		mInterface = ((TwidereApplication) getSherlockActivity().getApplication())
				.getServiceInterface();
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		getLocation();
		Bundle bundle = savedInstanceState != null ? savedInstanceState : getArguments();
		long[] activated_ids = bundle != null ? bundle.getLongArray(INTENT_KEY_USER_IDS) : null;
		if (bundle != null && bundle.getString(INTENT_KEY_TEXT) != null)
			mText = bundle.getString(INTENT_KEY_TEXT, "");

		mAccountIds = activated_ids == null ? CommonUtils
				.getActivatedAccounts(getSherlockActivity()) : activated_ids;

		View view = getView();
		mEditText = (EditText) view.findViewById(R.id.edit_text);
		mTextCount = (TextView) view.findViewById(R.id.text_count);
		mSendButton = (ImageButton) view.findViewById(R.id.send);
		mSelectAccount = (ImageButton) view.findViewById(R.id.select_account);
		mSendButton.setOnClickListener(this);
		mSelectAccount.setOnClickListener(this);
		mEditText.setMovementMethod(ArrowKeyMovementMethod.getInstance());
		mEditText.addTextChangedListener(this);
		if (mText != null) mEditText.setText(mText);
		int length = mEditText.length();
		mTextCount.setText(String.valueOf(length));
		mSendButton.setEnabled(length > 0 && length <= 140);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		switch (requestCode) {
			case REQUEST_TAKE_PHOTO:
				if (resultCode == Activity.RESULT_OK) {
					File file = new File(mImageUri.getPath());
					if (file.exists()) {
						mIsImageAttached = false;
						mIsPhotoAttached = true;
					} else {
						mIsPhotoAttached = false;
					}
					getSherlockActivity().invalidateOptionsMenu();
				}
				break;
			case REQUEST_ADD_IMAGE:
				if (resultCode == Activity.RESULT_OK) {
					Uri uri = intent.getData();
					File file = uri == null ? null : new File(CommonUtils.getImagePathFromUri(
							getSherlockActivity(), uri));
					if (file != null && file.exists()) {
						mImageUri = uri;
						mIsPhotoAttached = false;
						mIsImageAttached = true;
					} else {
						mIsImageAttached = false;
					}
					getSherlockActivity().invalidateOptionsMenu();
				}
				break;
			case REQUEST_SELECT_ACCOUNT:
				if (resultCode == Activity.RESULT_OK) {
					Bundle bundle = intent.getExtras();
					if (bundle == null) {
						break;
					}
					long[] user_ids = bundle.getLongArray(INTENT_KEY_USER_IDS);
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
				boolean attach_location = mPreferences.getBoolean(PREFERENCE_KEY_ATTACH_LOCATION,
						false);
				mInterface.updateStatus(mAccountIds, content, attach_location ? mRecentLocation
						: null, mImageUri, -1);
				if (getSherlockActivity() instanceof ComposeActivity) {
					getSherlockActivity().finish();
				}
				break;
			case R.id.select_account:
				Intent intent = new Intent(INTENT_ACTION_SELECT_ACCOUNT);
				Bundle bundle = new Bundle();
				bundle.putLongArray(INTENT_KEY_USER_IDS, mAccountIds);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_SELECT_ACCOUNT);
				break;
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_compose, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.compose, container, false);
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
				if (getSherlockActivity() instanceof ComposeActivity) {
					getSherlockActivity().finish();
				}
				break;
			case MENU_TAKE_PHOTO:
				takePhoto();
				break;
			case MENU_ADD_IMAGE:
				pickImage();
				break;
			case MENU_ADD_LOCATION:
				boolean attach_location = mPreferences.getBoolean(PREFERENCE_KEY_ATTACH_LOCATION,
						false);
				mPreferences.edit().putBoolean(PREFERENCE_KEY_ATTACH_LOCATION, !attach_location)
						.commit();
				getSherlockActivity().invalidateOptionsMenu();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
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

		boolean attach_location = mPreferences.getBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false);
		if (attach_location) {
			menu.findItem(MENU_ADD_LOCATION).getIcon()
					.setColorFilter(activated_color, Mode.MULTIPLY);
		} else {
			menu.findItem(MENU_ADD_LOCATION).getIcon().clearColorFilter();
		}
		super.onPrepareOptionsMenu(menu);
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
		outState.putLongArray(INTENT_KEY_USER_IDS, mAccountIds);
		outState.putString(INTENT_KEY_TEXT, mText);
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

		mRecentLocation = mLocationManager.getLastKnownLocation(provider);
	}

	private void pickImage() {
		Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, REQUEST_ADD_IMAGE);
	}

	private void takePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File file = new File(getSherlockActivity().getExternalCacheDir(), "tmp_photo_"
				+ System.currentTimeMillis() + ".jpg");
		mImageUri = Uri.fromFile(file);
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageUri);
		startActivityForResult(intent, REQUEST_TAKE_PHOTO);

	}
}
