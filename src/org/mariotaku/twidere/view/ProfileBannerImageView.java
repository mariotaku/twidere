package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import org.mariotaku.twidere.view.iface.IExtendedView;

public class ProfileBannerImageView extends ClickableImageView implements IExtendedView {

	private final Paint mPaint = new Paint();
	
	private LinearGradient mShader;
	private OnSizeChangedListener mOnSizeChangedListener;
	
	public ProfileBannerImageView(final Context context) {
		this(context, null);
	}

	public ProfileBannerImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ProfileBannerImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		ViewCompat.setLayerType(this, LAYER_TYPE_SOFTWARE, null);
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		if (mShader == null) return;
		final int width = getWidth(), height = getHeight();
		mPaint.setShader(mShader);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		super.onDraw(canvas);
		canvas.drawRect(0, 0, width, height, mPaint);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec), height = width / 2;
		setMeasuredDimension(width, height);
		if (width > 0) {		
			mShader = new LinearGradient(width / 2, 0, width / 2, height, 0xffffffff, 0x00ffffff, Shader.TileMode.CLAMP);
		}
		super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
	}

	@Override
	public final void setOnSizeChangedListener(final OnSizeChangedListener listener) {
		mOnSizeChangedListener = listener;
	}

	@Override
	protected final void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mOnSizeChangedListener != null) {
			mOnSizeChangedListener.onSizeChanged(this, w, h, oldw, oldh);
		}
	}
}
