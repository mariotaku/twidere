package org.mariotaku.twidere.activity;

import java.io.File;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Scroller;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ComposeActivity extends SherlockFragmentActivity implements Constants, OnClickListener {

	private final static int TAKE_PICTURE = 1;
	private final static int PICK_IMAGE = 2;

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
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose);
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.select_accounts:
				break;
		}
		
	}
}
