package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ComposeActivity extends SherlockFragmentActivity implements Constants {

	private final static int TAKE_PICTURE = 1;
	private final static int PICK_IMAGE = 2;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		switch (requestCode) {
			case TAKE_PICTURE:
				break;
			case PICK_IMAGE:
				if (resultCode == RESULT_OK) {
					Uri selectedImage = intent.getData();
					String[] filePathColumn = { MediaStore.Images.Media.DATA };

					Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null,
							null, null);
					if (cursor == null) {
						break;
					}
					int idx = cursor.getColumnIndex(filePathColumn[0]);
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						String filePath = cursor.getString(idx);
						cursor.moveToNext();
					}
					cursor.close();
				}
				break;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.compose, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.take_photo:
				takePhoto();
				break;
			case R.id.add_image:
				pickImage();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void pickImage() {
		Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, PICK_IMAGE);
	}

	private void takePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, TAKE_PICTURE);

	}
}
