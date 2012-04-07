package org.mariotaku.twidere.activity;

import java.io.File;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

public class ComposeActivity extends SherlockFragmentActivity implements Constants,
		OnClickListener, TextWatcher {

	private final static int TAKE_PICTURE = 1;
	private final static int PICK_IMAGE = 2;
	private final static int SELECT_ACCOUNTS = 3;

	private ActionBar mActionBar;
	private Uri mImageUri;
	private EditText mEditText;
	private TextView mTextCount;
	private boolean mIsImageAttached, mIsPhotoAttached, mIsLocationAttached;

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
			case TAKE_PICTURE:
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
			case PICK_IMAGE:
				if (resultCode == RESULT_OK) {
					Uri uri = intent.getData();
					File file = new File(getRealPathFromURI(uri));
					if (file.exists()) {
						mImageUri = uri;
						mIsPhotoAttached = false;
						mIsImageAttached = true;
					} else {
						mIsImageAttached = false;
					}
					invalidateOptionsMenu();
				}
				break;
			case SELECT_ACCOUNTS:
				if (resultCode == RESULT_OK) {
					Bundle bundle = intent.getExtras();
					if (bundle == null) {
						break;
					}
					long[] user_ids = bundle.getLongArray(Accounts.USER_IDS);
					if (user_ids == null) {
						break;
					}

				}
				break;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.select_accounts:
				startActivityForResult(new Intent(this, SelectAccountsActivity.class),
						SELECT_ACCOUNTS);
				break;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose);
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		findViewById(R.id.select_accounts).setOnClickListener(this);
		mEditText = (EditText) findViewById(R.id.edit_text);
		mEditText.addTextChangedListener(this);
		mTextCount = (TextView) findViewById(R.id.text_count);
		mTextCount.setText(String.valueOf(mEditText.getText().toString().length()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.compose, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME:
				finish();
				break;
			case MENU_PICK_FROM_GALLERY:
				pickImage();
				break;
			case MENU_TAKE_PHOTO:
				takePhoto();
				break;
			case MENU_PICK_FROM_MAP:
				break;
			case MENU_ADD_LOCATION:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		SubMenu imageSubMenu = menu.findItem(MENU_IMAGE).getSubMenu();
		imageSubMenu.clear();
		imageSubMenu.add(MENU_ADD_IMAGE, MENU_PICK_FROM_GALLERY, Menu.NONE,
				R.string.pick_from_gallery);
		imageSubMenu.add(MENU_ADD_IMAGE, MENU_TAKE_PHOTO, Menu.NONE, R.string.take_photo);
		if (mIsImageAttached || mIsPhotoAttached) {
			menu.findItem(MENU_IMAGE).getIcon()
					.setColorFilter(Color.CYAN, PorterDuff.Mode.LIGHTEN);
			imageSubMenu.add(MENU_ADD_IMAGE, MENU_VIEW, Menu.NONE, R.string.view);
			imageSubMenu.add(MENU_ADD_IMAGE, MENU_DELETE, Menu.NONE, R.string.delete);
		} else {
			menu.findItem(MENU_IMAGE).getIcon().clearColorFilter();
		}

		SubMenu locationSubMenu = menu.findItem(MENU_LOCATION).getSubMenu();
		locationSubMenu.clear();
		locationSubMenu.add(MENU_ADD_IMAGE, MENU_ADD_LOCATION, Menu.NONE, R.string.add_location);
		locationSubMenu.add(MENU_ADD_IMAGE, MENU_PICK_FROM_MAP, Menu.NONE, R.string.pick_from_map);
		if (mIsLocationAttached) {
			menu.findItem(MENU_LOCATION).getIcon().setColorFilter(Color.CYAN, Mode.MULTIPLY);
			locationSubMenu.add(MENU_ADD_LOCATION, MENU_VIEW, Menu.NONE, R.string.view);
			locationSubMenu.add(MENU_ADD_LOCATION, MENU_DELETE, Menu.NONE, R.string.delete);
		} else {
			menu.findItem(MENU_LOCATION).getIcon().clearColorFilter();
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
		startActivityForResult(i, PICK_IMAGE);
	}

	private void takePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File file = new File(getExternalCacheDir(), "tmp_photo_" + System.currentTimeMillis()
				+ ".jpg");
		mImageUri = Uri.fromFile(file);
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageUri);
		startActivityForResult(intent, TAKE_PICTURE);

	}
}
