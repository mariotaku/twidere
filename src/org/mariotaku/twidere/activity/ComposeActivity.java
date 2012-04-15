package org.mariotaku.twidere.activity;

import java.io.File;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ServiceInterface;

import roboguice.inject.ContentView;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff.Mode;
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

@ContentView(R.layout.compose)
public class ComposeActivity extends BaseActivity implements OnClickListener, TextWatcher {

	private ActionBar mActionBar;
	private Uri mImageUri;
	@InjectView(R.id.edit_text) private EditText mEditText;
	@InjectView(R.id.text_count) private TextView mTextCount;
	@InjectView(R.id.select_account) private ImageButton mSelectAccount;
	@InjectResource(R.color.holo_blue_bright) private int mActivedMenuColor;
	private boolean mIsImageAttached, mIsPhotoAttached, mIsLocationAttached;
	private long[] mAccountIds;
	private ServiceInterface mInterface;

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// TODO Auto-generated method stub

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
					File file = uri == null ? null : new File(getRealPathFromURI(uri));
					if (file != null && file.exists()) {
						mImageUri = Uri.fromFile(file);
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
				mInterface.updateStatus(mAccountIds, content, mImageUri);
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
		mInterface = ((TwidereApplication) getApplication()).getServiceInterface();
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mSelectAccount.setOnClickListener(this);
		mEditText.addTextChangedListener(this);
		mTextCount.setText(String.valueOf(mEditText.length()));
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

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s != null) {
			mTextCount.setText(String.valueOf(s.length()));
		}

	}

	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);

		if (cursor == null || cursor.getCount() <= 0) return null;

		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

		cursor.moveToFirst();

		String path = cursor.getString(column_index);
		cursor.close();
		return path;
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
