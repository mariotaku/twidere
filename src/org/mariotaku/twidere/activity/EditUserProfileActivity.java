package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.*;

import android.os.Bundle;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.ExtendedFrameLayout;
import org.mariotaku.twidere.view.ExtendedFrameLayout.OnSizeChangedListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class EditUserProfileActivity extends BaseDialogWhenLargeActivity implements OnSizeChangedListener {

	public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {
		final LayoutParams lp = view.getLayoutParams();
		lp.height = w / 2;
		view.setLayoutParams(lp);
		final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.twidere_promotional_graphic);		
		view.setBackgroundDrawable(new BitmapDrawable(getResources(), createAlphaGradientBanner(bitmap)));
	}
	

	private ExtendedFrameLayout mProfileNameBannerContainer;

	public void onContentChanged() {
		super.onContentChanged();
		mProfileNameBannerContainer = (ExtendedFrameLayout) findViewById(R.id.profile_name_banner_container);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_user_profile);
		mProfileNameBannerContainer.setOnSizeChangedListener(this);
	}
}
