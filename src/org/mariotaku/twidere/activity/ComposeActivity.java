package org.mariotaku.twidere.activity;

import java.io.File;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ComposeActivity extends SherlockFragmentActivity implements Constants, OnClickListener {

	private final static int TAKE_PICTURE = 1;
	private final static int PICK_IMAGE = 2;
	private final static int SELECT_ACCOUNTS = 3;

	private ActionBar mActionBar;
	private Uri mImageCaptureUri;
	private EditText mEditText;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		switch (requestCode) {
			case TAKE_PICTURE:
				break;
			case PICK_IMAGE:
				if (resultCode == RESULT_OK) {
					String path = getRealPathFromURI(intent.getData());
					if (path != null) {
					}
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
					Log.d(LOGTAG, "user_ids.length = " + user_ids.length);

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.compose, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
			case R.id.take_photo:
				takePhoto();
				break;
			case R.id.add_image:
				pickImage();
				break;
		}
		return super.onOptionsItemSelected(item);
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
		mImageCaptureUri = Uri.fromFile(file);
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
		startActivityForResult(intent, TAKE_PICTURE);

	}
}
