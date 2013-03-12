package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ImageView;
import org.mariotaku.twidere.view.iface.IExtendedView;
import org.mariotaku.twidere.util.ViewAccessor;

public class BannerImageView extends ImageView implements IExtendedView {

	private OnSizeChangedListener mOnSizeChangedListener;
	
	public BannerImageView(final Context context) {
		this(context, null);
	}

	public BannerImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BannerImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		ViewAccessor.setLayerType(this, LAYER_TYPE_SOFTWARE, null);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		final int width = getWidth(), height = getHeight();
		final Paint paint = new Paint();
		final LinearGradient shader = new LinearGradient(width / 2, 0, width / 2, height, 0xffffffff, 0x00ffffff,
				Shader.TileMode.CLAMP);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		canvas.drawRect(0, 0, width, height, paint);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(width, width / 2);
		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
