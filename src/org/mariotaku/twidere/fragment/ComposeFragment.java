package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getAccountUsername;
import static org.mariotaku.twidere.util.Utils.getActivatedAccounts;
import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;

import java.io.File;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.ComposeActivity;
import org.mariotaku.twidere.adapter.UserAutoCompleteAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ScreenNameTokenizer;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.widget.StatusComposeEditText;

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
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ComposeFragment extends BaseFragment implements TextWatcher, LocationListener {

	private String mText;
	private Uri mImageUri;
	private StatusComposeEditText mEditText;
	private TextView mTextCount;
	private boolean mIsImageAttached, mIsPhotoAttached;
	private long[] mAccountIds;
	private ServiceInterface mInterface;
	private Location mRecentLocation;
	private LocationManager mLocationManager;
	private SharedPreferences mPreferences;
	private long mInReplyToStatusId = -1, mAccountId = -1;
	private String mInReplyToScreenName, mInReplyToName;
	private boolean mIsQuote;

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mLocationManager = (LocationManager) getSherlockActivity().getSystemService(Context.LOCATION_SERVICE);
		mPreferences = getSherlockActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mInterface = ((TwidereApplication) getSherlockActivity().getApplication()).getServiceInterface();
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		Bundle bundle = savedInstanceState != null ? savedInstanceState : getArguments();
		long[] activated_ids = bundle != null ? bundle.getLongArray(INTENT_KEY_IDS) : null;
		mAccountId = bundle != null ? bundle.getLong(INTENT_KEY_ACCOUNT_ID) : -1;
		mInReplyToStatusId = bundle != null ? bundle.getLong(INTENT_KEY_IN_REPLY_TO_ID) : -1;
		mInReplyToScreenName = bundle != null ? bundle.getString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME) : null;
		mInReplyToName = bundle != null ? bundle.getString(INTENT_KEY_IN_REPLY_TO_NAME) : null;
		int text_selection_start = -1;
		if (mInReplyToStatusId > -1) {
			String account_username = getAccountUsername(getSherlockActivity(), mAccountId);

			String[] mentions = getArguments() != null ? getArguments().getStringArray(INTENT_KEY_MENTIONS) : null;

			if (bundle != null && bundle.getString(INTENT_KEY_TEXT) != null
					&& (mentions == null || mentions.length < 1)) {
				mText = bundle.getString(INTENT_KEY_TEXT);
			} else if (mentions != null) {
				StringBuilder builder = new StringBuilder();
				for (String mention : mentions) {
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

			boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
			String name = display_name ? mInReplyToName : mInReplyToScreenName;
			if (name != null) {
				getSherlockActivity().setTitle(getString(mIsQuote ? R.string.quote_user : R.string.reply_to, name));
			}
			mAccountIds = activated_ids == null ? new long[] { mAccountId } : activated_ids;
		} else {
			mAccountIds = activated_ids == null ? getActivatedAccounts(getSherlockActivity()) : activated_ids;
			if (bundle != null && bundle.getString(INTENT_KEY_TEXT) != null) {
				mText = bundle.getString(INTENT_KEY_TEXT);
			}
		}
		View view = getView();
		mEditText = (StatusComposeEditText) view.findViewById(R.id.edit_text);
		mTextCount = (TextView) view.findViewById(R.id.text_count);
		mEditText.setMovementMethod(ArrowKeyMovementMethod.getInstance());
		mEditText.addTextChangedListener(this);
		final LazyImageLoader imageloader = ((TwidereApplication) getSherlockActivity().getApplication())
				.getListProfileImageLoader();
		mEditText.setAdapter(new UserAutoCompleteAdapter(getSherlockActivity(), imageloader));
		mEditText.setTokenizer(new ScreenNameTokenizer(mEditText));
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
		int length = mEditText.length();
		mTextCount.setText(String.valueOf(length));
		getSendMenuItem().setEnabled(length > 0 && length <= 140);

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
			case REQUEST_PICK_IMAGE:
				if (resultCode == Activity.RESULT_OK) {
					Uri uri = intent.getData();
					File file = uri == null ? null : new File(getImagePathFromUri(getSherlockActivity(), uri));
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
					long[] user_ids = bundle.getLongArray(INTENT_KEY_IDS);
					if (user_ids != null) {
						mAccountIds = user_ids;
					}
				}
				break;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Tell the framework to try to keep this fragment around
		// during a configuration change.
		setRetainInstance(true);
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
			case MENU_HOME: {
				if (getSherlockActivity() instanceof ComposeActivity) {
					getSherlockActivity().finish();
				}
				break;
			}
			case MENU_SEND: {
				String content = mEditText != null ? mEditText.getText().toString() : null;
				boolean attach_location = mPreferences.getBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false);
				mInterface.updateStatus(mAccountIds, content, attach_location ? mRecentLocation : null, mImageUri,
						mInReplyToStatusId);
				if (getSherlockActivity() instanceof ComposeActivity) {
					getSherlockActivity().setResult(Activity.RESULT_OK);
					getSherlockActivity().finish();
				}
				break;
			}
			case MENU_TAKE_PHOTO: {
				takePhoto();
				break;
			}
			case MENU_ADD_IMAGE: {
				pickImage();
				break;
			}
			case MENU_ADD_LOCATION: {
				boolean attach_location = mPreferences.getBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false);
				if (!attach_location) {
					getLocation();
				}
				mPreferences.edit().putBoolean(PREFERENCE_KEY_ATTACH_LOCATION, !attach_location).commit();
				getSherlockActivity().invalidateOptionsMenu();
				break;
			}
			case MENU_DRAFTS: {
				Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_DRAFTS);
				startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
				break;
			}
			case MENU_SELECT_ACCOUNT: {
				Intent intent = new Intent(INTENT_ACTION_SELECT_ACCOUNT);
				Bundle bundle = new Bundle();
				bundle.putLongArray(INTENT_KEY_IDS, mAccountIds);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_SELECT_ACCOUNT);
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		int activated_color = getResources().getColor(android.R.color.holo_blue_bright);
		MenuItem itemAddImage = menu.findItem(MENU_ADD_IMAGE);
		if (mIsImageAttached && !mIsPhotoAttached) {
			itemAddImage.getIcon().setColorFilter(activated_color, Mode.MULTIPLY);
			itemAddImage.setTitle(R.string.remove_image);
		} else {
			itemAddImage.getIcon().clearColorFilter();
			itemAddImage.setTitle(R.string.add_image);
		}
		MenuItem itemTakePhoto = menu.findItem(MENU_TAKE_PHOTO);
		if (!mIsImageAttached && mIsPhotoAttached) {
			itemTakePhoto.getIcon().setColorFilter(activated_color, Mode.MULTIPLY);
			itemTakePhoto.setTitle(R.string.remove_photo);
		} else {
			itemTakePhoto.getIcon().clearColorFilter();
			itemTakePhoto.setTitle(R.string.take_photo);
		}
		MenuItem itemAttachLocation = menu.findItem(MENU_ADD_LOCATION);
		boolean attach_location = mPreferences.getBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false);
		if (attach_location && getLocation()) {
			itemAttachLocation.getIcon().setColorFilter(activated_color, Mode.MULTIPLY);
			itemAttachLocation.setTitle(R.string.remove_location);
		} else {
			mPreferences.edit().putBoolean(PREFERENCE_KEY_ATTACH_LOCATION, false).commit();
			itemAttachLocation.getIcon().clearColorFilter();
			itemAttachLocation.setTitle(R.string.add_location);
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
		outState.putLongArray(INTENT_KEY_IDS, mAccountIds);
		outState.putString(INTENT_KEY_TEXT, mText);
		outState.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
		outState.putLong(INTENT_KEY_IN_REPLY_TO_ID, mInReplyToStatusId);
		outState.putString(INTENT_KEY_IN_REPLY_TO_NAME, mInReplyToName);
		outState.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, mInReplyToScreenName);
		outState.putBoolean(INTENT_KEY_IS_QUOTE, mIsQuote);
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
			getSendMenuItem().setEnabled(length > 0 && length <= 140);
		}

	}

	/**
	 * The Location Manager manages location providers. This code searches for
	 * the best provider of data (GPS, WiFi/cell phone tower lookup, some other
	 * mechanism) and finds the last known location.
	 **/
	private boolean getLocation() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = mLocationManager.getBestProvider(criteria, true);

		if (provider != null) {
			mRecentLocation = mLocationManager.getLastKnownLocation(provider);
		} else {
			Toast.makeText(getSherlockActivity(), R.string.cannot_get_location, Toast.LENGTH_SHORT).show();
		}
		return provider != null;
	}

	private MenuItem getSendMenuItem() {
		if (getSherlockActivity() instanceof ComposeActivity)
			return ((ComposeActivity) getSherlockActivity()).getSendMenuItem();
		return null;
	}

	private void pickImage() {
		Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, REQUEST_PICK_IMAGE);
	}

	private void takePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File file = new File(getSherlockActivity().getExternalCacheDir(), "tmp_photo_" + System.currentTimeMillis()
				+ ".jpg");
		mImageUri = Uri.fromFile(file);
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageUri);
		startActivityForResult(intent, REQUEST_TAKE_PHOTO);

	}
}
