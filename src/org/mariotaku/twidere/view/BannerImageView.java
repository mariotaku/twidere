package org.mariotaku.twidere.view;

import org.mariotaku.twidere.view.iface.IExtendedView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BannerImageView extends ImageView implements IExtendedView {

	private final Paint mPaint = new Paint();
	
	private LinearGradient mShader;
	private OnSizeChangedListener mOnSizeChangedListener;
	
	public BannerImageView(final Context context) {
		this(context, null);
	}

	public BannerImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BannerImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		ViewCompat.setLayerType(this, LAYER_TYPE_SOFTWARE, null);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mShader == null) return;
		final int width = getWidth(), height = getHeight();
		mPaint.setShader(mShader);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		canvas.drawRect(0, 0, width, height, mPaint);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec), mode = MeasureSpec.getMode(heightMeasureSpec);
		setMeasuredDimension(width, width / 2);
	}

	@Override
	public final void setOnSizeChangedListener(final OnSizeChangedListener listener) {
		mOnSizeChangedListener = listener;
	}

	@Override
	protected final void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mShader= new LinearGradient(w / 2, 0, w / 2, h, 0xffffffff, 0x00ffffff, Shader.TileMode.CLAMP);
		if (mOnSizeChangedListener != null) {
			mOnSizeChangedListener.onSizeChanged(this, w, h, oldw, oldh);
		}
	}
}
