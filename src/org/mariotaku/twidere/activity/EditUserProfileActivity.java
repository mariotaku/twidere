package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.createAlphaGradientBanner;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.ExtendedFrameLayout;
import org.mariotaku.twidere.view.ProfileNameBannerContainer;
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.os.Handler;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.EditText;

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
	

	public void beforeTextChanged(CharSequence s, int length, int start, int end) {
	}

	public void onTextChanged(CharSequence s, int length, int start, int end) {
		mHasUnsavedChanges = true;
	}

	public void afterTextChanged(Editable s) {
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
	public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {
		final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.profile_banner_sample);
		mProfileNameBannerContainer.setBanner(createAlphaGradientBanner(bitmap));
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
