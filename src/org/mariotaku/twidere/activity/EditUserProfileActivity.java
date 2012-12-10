package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.createAlphaGradientBanner;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.ExtendedFrameLayout;
import org.mariotaku.twidere.view.ExtendedViewInterface.OnSizeChangedListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;

public class EditUserProfileActivity extends BaseDialogWhenLargeActivity implements OnSizeChangedListener {

	private ExtendedFrameLayout mProfileNameBannerContainer;

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mProfileNameBannerContainer = (ExtendedFrameLayout) findViewById(R.id.profile_name_banner_container);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_user_profile);
		mProfileNameBannerContainer.setOnSizeChangedListener(this);
	}

	@Override
	public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {
		final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.profile_banner_sample);
		mProfileNameBannerContainer.setBackgroundDrawable(new BitmapDrawable(getResources(),
				createAlphaGradientBanner(bitmap)));

	}

}
