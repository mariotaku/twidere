package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import org.mariotaku.twidere.R;

public class ProfileImageBannerLayout extends ExtendedFrameLayout {

	public static final int VIEW_ID_PROFILE_IMAGE = 0x10000001;
	public static final int VIEW_ID_PROFILE_BANNER_IMAGE = 0x10000002;

	private static final double PROFILE_IMAGE_WIDTH_FACTOR = 0.1425;
	private static final double PROFILE_IMAGE_TOP_MARGIN_FACTOR = 0.0875;

	private final int mBorderWidth;
	
	private ImageView mProfileBannerImageView, mProfileImageView;

	public ProfileImageBannerLayout(final Context context) {
		this(context, null);
	}

	public ProfileImageBannerLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ProfileImageBannerLayout(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mBorderWidth = (int) (getResources().getDisplayMetrics().density * 3.5);
		mProfileBannerImageView = new ProfileBannerImageView(context);
		mProfileBannerImageView.setId(VIEW_ID_PROFILE_BANNER_IMAGE);
		mProfileBannerImageView.setBackgroundResource(R.drawable.profile_banner_default);
		addView(mProfileBannerImageView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mProfileImageView = new ProfileImageView(context, mBorderWidth);
		mProfileImageView.setId(VIEW_ID_PROFILE_IMAGE);
		addView(mProfileImageView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
	}

	public ImageView getProfileImageView() {
		return mProfileImageView;
	}

	public ImageView getProfileBannerImageView() {
		return mProfileBannerImageView;
	}
	
	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec), height = width / 2;
		setMeasuredDimension(width, height);
		super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
		if (width > 0) {			
			final int profile_size = (int) (width * PROFILE_IMAGE_WIDTH_FACTOR);
			final LayoutParams profile_lp = (FrameLayout.LayoutParams) mProfileImageView.getLayoutParams();
			profile_lp.width = profile_size + mBorderWidth * 2;
			profile_lp.height = profile_size + mBorderWidth * 2;
			profile_lp.gravity = Gravity.TOP|Gravity.CENTER_HORIZONTAL;
			profile_lp.topMargin = (int) (height * PROFILE_IMAGE_TOP_MARGIN_FACTOR) - mBorderWidth;
			mProfileImageView.setLayoutParams(profile_lp);
		}
	}

	private static class ProfileBannerImageView extends ClickableImageView {
		
		private static final int[] COLORS = new int[] { 0xFFFFFFFF, 0x00FFFFFF };
		private static final float[] POSITIONS = new float[] { 0.0f, 1.0f };
		private final Paint mPaint = new Paint();

		private LinearGradient mShader;

		private ProfileBannerImageView(final Context context) {
			super(context, null, 0);
			ViewCompat.setLayerType(this, LAYER_TYPE_SOFTWARE, null);
			setScaleType(ScaleType.FIT_XY);
		}

		@Override
		protected void onDraw(final Canvas canvas) {
			final int width = getWidth(), height = getHeight();
			if (mShader == null) return;
			super.onDraw(canvas);			
			mPaint.setShader(mShader);
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
			canvas.drawRect(0, 0, width, height, mPaint);
		}

		@Override
		protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
			final int width = MeasureSpec.getSize(widthMeasureSpec), height = width / 2;
			setMeasuredDimension(width, height);
			if (width > 0) {		
				mShader = new LinearGradient(width / 2, 0, width / 2, height, COLORS, POSITIONS, Shader.TileMode.CLAMP);
			}
			super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
		}
	}
	
	private static class ProfileImageView extends ClickableImageView {

		private final Paint mWhitePaint, mBlackPaint;
		private final int mPaddings;
		
		private ProfileImageView(final Context context, int padding) {
			super(context, null, 0);
			ViewCompat.setLayerType(this, LAYER_TYPE_SOFTWARE, null);
			//setScaleType(ScaleType.FIT_XY);
			mWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mWhitePaint.setColor(Color.WHITE);
			mBlackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mBlackPaint.setColor(Color.BLACK);
			mPaddings = padding;
			setPadding(padding, padding, padding, padding);
		}

		@Override
		protected void onDraw(final Canvas canvas) {
			canvas.drawColor(Color.WHITE);
			final int width = getWidth(), height = getHeight();
			if (width > 0 && height > 0) {
				canvas.drawRect(mPaddings, mPaddings, width - mPaddings, height - mPaddings, mBlackPaint);
				canvas.drawRect(mPaddings / 2, mPaddings / 2, width - mPaddings / 2, height - mPaddings / 2, mWhitePaint);
			}
			super.onDraw(canvas);
		}
	}
}

