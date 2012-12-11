package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.createAlphaGradientBanner;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.ProfileNameBannerContainer;
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class EditUserProfileActivity extends BaseDialogWhenLargeActivity implements OnSizeChangedListener, TextWatcher {

	private ProfileNameBannerContainer mProfileNameBannerContainer;
	private EditText mEditName, mEditDescription, mEditLocation, mEditUrl;

	private boolean mHasUnsavedChanges, mBackPressed;

	private final Handler mHandler = new Handler();

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
		mProfileNameBannerContainer = (ProfileNameBannerContainer) findViewById(R.id.profile_name_banner_container);
		mEditName = (EditText) findViewById(R.id.name);
		mEditDescription = (EditText) findViewById(R.id.description);
		mEditLocation = (EditText) findViewById(R.id.location);
		mEditUrl = (EditText) findViewById(R.id.url);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_user_profile);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mProfileNameBannerContainer.setOnSizeChangedListener(this);
		mEditName.addTextChangedListener(this);
		mEditDescription.addTextChangedListener(this);
		mEditLocation.addTextChangedListener(this);
		mEditUrl.addTextChangedListener(this);
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
	public void onSizeChanged(final View view, final int w, final int h, final int oldw, final int oldh) {
		final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.profile_banner_sample);
		mProfileNameBannerContainer.setBanner(createAlphaGradientBanner(bitmap));
	}

	@Override
	public void onTextChanged(final CharSequence s, final int length, final int start, final int end) {
		mHasUnsavedChanges = true;
	}

	private boolean backPressed() {
		if (!mHasUnsavedChanges) return true;
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

}
