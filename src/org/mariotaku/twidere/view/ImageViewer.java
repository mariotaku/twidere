package org.mariotaku.twidere.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;

public class ImageViewer extends View {

	private Bitmap mBitmap;
	private final Paint mPaint = new Paint(), mBackgroundPaint = new Paint();

	private volatile int mDx = 0, mDy = 0;
	private volatile float mZoomFactor = 1.0f, mMinZoomFactor;

	private boolean mMotionControl;

	private int mSavedX, mSavedY;

	private float mStartPinchDistance2 = -1, mStartZoomFactor;

	private boolean isWaitingForDoubleTap = false;

	private static final int ZOOM_IN_1 = 1, ZOOM_OUT_1 = 2, DOUBLE_TAP_TIMEOUT = 3, ZOOM_IN_2 = 4, ZOOM_OUT_2 = 5;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ZOOM_IN_1: {
					mHandler.removeMessages(ZOOM_IN_1);
					if (mZoomFactor < mMinZoomFactor) {
						final long delay = (long) (mMinZoomFactor / mZoomFactor) / 4;
						mZoomFactor *= 1.1f;
						if (mZoomFactor > mMinZoomFactor) {
							mZoomFactor = mMinZoomFactor;
						}
						mHandler.sendEmptyMessageDelayed(ZOOM_IN_1, delay > 0 ? delay : 1);
					} else {
						mZoomFactor = mMinZoomFactor;
					}
					invalidate();
					break;
				}
				case ZOOM_OUT_1: {
					mHandler.removeMessages(ZOOM_OUT_1);
					if (mZoomFactor > 1.0f) {
						final long delay = (long) mZoomFactor / 4;
						mZoomFactor /= 1.1f;
						if (mZoomFactor < 1.0f) {
							mZoomFactor = 1.0f;
						}
						mHandler.sendEmptyMessageDelayed(ZOOM_OUT_1, delay > 0 ? delay : 1);
					} else {
						mZoomFactor = 1.0f;
					}
					invalidate();
					break;
				}
				case DOUBLE_TAP_TIMEOUT: {
					mHandler.removeMessages(DOUBLE_TAP_TIMEOUT);
					isWaitingForDoubleTap = false;
					break;
				}
				case ZOOM_IN_2: {
					mHandler.removeMessages(ZOOM_IN_2);
					if (mZoomFactor < 1.0f) {
						final long delay = (long) mZoomFactor / 4;
						mZoomFactor *= 1.2f;
						if (mZoomFactor > 1.0f) {
							mZoomFactor = 1.0f;
						}
						mHandler.sendEmptyMessageDelayed(ZOOM_IN_2, delay > 0 ? delay : 1);
					} else {
						mZoomFactor = 1.0f;
					}
					invalidate();
					break;
				}
				case ZOOM_OUT_2: {
					mHandler.removeMessages(ZOOM_OUT_2);
					if (mZoomFactor > mMinZoomFactor) {
						final long delay = (long) (mMinZoomFactor / mZoomFactor) / 4;
						mZoomFactor /= 1.2f;
						if (mZoomFactor < mMinZoomFactor) {
							mZoomFactor = mMinZoomFactor;
						}
						mHandler.sendEmptyMessageDelayed(ZOOM_OUT_2, delay > 0 ? delay : 1);
					} else {
						mZoomFactor = mMinZoomFactor;
					}
					invalidate();
					break;
				}
			}
		}

	};

	public ImageViewer(Context context) {
		this(context, null);
	}

	public ImageViewer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImageViewer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPaint.setColor(Color.BLACK);
		mBackgroundPaint.setColor(Color.TRANSPARENT);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int pointer_count = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR ? GetPointerCountAccessor
				.getPointerCount(event) : 1;
		if (pointer_count > 1)
			return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR ? OnMultiTouchEventCompat.onMultiTouchEvent(
					this, event) : true;
		else if (pointer_count == 1) return onSingleTouchEvent(event);
		return false;
	}

	public void recycle() {
		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
			mBitmap = null;
		}
	}

	public void setBitmap(Bitmap bitmap) {
		recycle();
		mBitmap = bitmap;
		mMinZoomFactor = getMinZoom(bitmap);
		mZoomFactor = mMinZoomFactor;
		invalidate();
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(final Canvas canvas) {
		final int w = getWidth();
		final int h = getHeight();
		canvas.drawRect(0, 0, w, h, mBackgroundPaint);
		if (mBitmap == null || mBitmap.isRecycled()) return;

		final int bw = (int) (mBitmap.getWidth() * mZoomFactor);
		final int bh = (int) (mBitmap.getHeight() * mZoomFactor);

		final Rect src = new Rect(0, 0, (int) (w / mZoomFactor), (int) (h / mZoomFactor));
		final Rect dst = new Rect(0, 0, w, h);
		if (bw <= w) {
			src.left = 0;
			src.right = mBitmap.getWidth();
			dst.left = (w - bw) / 2;
			dst.right = dst.left + bw;
		} else {
			final int bWidth = mBitmap.getWidth();
			final int pWidth = (int) (w / mZoomFactor);
			src.left = Math.min(bWidth - pWidth, Math.max((bWidth - pWidth) / 2 - mDx, 0));
			src.right += src.left;
		}
		if (bh <= h) {
			src.top = 0;
			src.bottom = mBitmap.getHeight();
			dst.top = (h - bh) / 2;
			dst.bottom = dst.top + bh;
		} else {
			final int bHeight = mBitmap.getHeight();
			final int pHeight = (int) (h / mZoomFactor);
			src.top = Math.min(bHeight - pHeight, Math.max((bHeight - pHeight) / 2 - mDy, 0));
			src.bottom += src.top;
		}
		canvas.drawBitmap(mBitmap, src, dst, mPaint);
	}

	private float getMinZoom(Bitmap bitmap) {
		float zoom = 1.0f;
		if (bitmap != null) {
			zoom = Math.min((float) getWidth() / bitmap.getWidth(), (float) getHeight() / bitmap.getHeight());
			if (zoom >= 1) {
				zoom = 1.0f;
			}
		}
		return zoom;
	}

	private boolean onSingleTouchEvent(MotionEvent event) {
		final int x = (int) event.getX();
		final int y = (int) event.getY();

		switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				mMotionControl = false;
				break;
			case MotionEvent.ACTION_DOWN:
				mMotionControl = true;
				mSavedX = x;
				mSavedY = y;
				if (!isWaitingForDoubleTap) {
					isWaitingForDoubleTap = true;
					mHandler.sendEmptyMessageDelayed(DOUBLE_TAP_TIMEOUT, 300L);
				} else {
					if (mZoomFactor <= mMinZoomFactor) {
						mHandler.sendEmptyMessage(ZOOM_IN_2);
					} else if (mZoomFactor <= 1.0f) {
						mHandler.sendEmptyMessage(ZOOM_OUT_2);
					}
					isWaitingForDoubleTap = false;
					mHandler.removeMessages(DOUBLE_TAP_TIMEOUT);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (mMotionControl) {
					shift((int) ((x - mSavedX) / mZoomFactor), (int) ((y - mSavedY) / mZoomFactor));
				}
				mMotionControl = true;
				mSavedX = x;
				mSavedY = y;
				break;
		}
		return true;
	}

	private void shift(int dx, int dy) {
		if (mBitmap == null || mBitmap.isRecycled()) return;

		final int w = (int) (getWidth() / mZoomFactor);
		final int h = (int) (getHeight() / mZoomFactor);
		final int bw = mBitmap.getWidth();
		final int bh = mBitmap.getHeight();

		final int newDx, newDy;

		if (w < bw) {
			final int delta = (bw - w) / 2;
			newDx = Math.max(-delta, Math.min(delta, mDx + dx));
		} else {
			newDx = mDx;
		}
		if (h < bh) {
			final int delta = (bh - h) / 2;
			newDy = Math.max(-delta, Math.min(delta, mDy + dy));
		} else {
			newDy = mDy;
		}

		if (newDx != mDx || newDy != mDy) {
			mDx = newDx;
			mDy = newDy;
			postInvalidate();
		}
	}

	private static class GetPointerCountAccessor {
		@TargetApi(5)
		public static int getPointerCount(MotionEvent event) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) return event.getPointerCount();
			return 1;
		}
	}

	@TargetApi(5)
	private static class OnMultiTouchEventCompat {

		private static boolean onMultiTouchEvent(ImageViewer viewer, MotionEvent event) {
			viewer.isWaitingForDoubleTap = false;
			final int pointer_count = event.getPointerCount();
			final float diffX = event.getX(0) - event.getX(1), diffY = event.getY(0) - event.getY(1);

			final int x = (int) ((event.getX(0) + event.getX(1)) / 2);
			int y = (int) ((event.getY(0) + event.getY(1)) / 2);

			for (int i = 0; i < pointer_count; i++) {
				y += event.getX(i);
			}
			y /= pointer_count;

			for (int i = 0; i < pointer_count; i++) {
				y += event.getX(i);
			}
			y /= pointer_count;

			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_POINTER_UP:
					viewer.mStartPinchDistance2 = -1;
					viewer.mMotionControl = false;
					if (viewer.mZoomFactor < viewer.mMinZoomFactor) {
						viewer.mHandler.sendEmptyMessage(ZOOM_IN_1);
					} else if (viewer.mZoomFactor > 1.0f) {
						viewer.mHandler.sendEmptyMessage(ZOOM_OUT_1);
					}
					break;
				case MotionEvent.ACTION_POINTER_DOWN: {
					viewer.mHandler.removeCallbacksAndMessages(null);
					viewer.mMotionControl = true;
					viewer.mSavedX = x;
					viewer.mSavedY = y;
					viewer.mStartPinchDistance2 = Math.max(diffX * diffX + diffY * diffY, 10f);
					viewer.mStartZoomFactor = viewer.mZoomFactor;
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					if (viewer.mMotionControl) {
						viewer.shift((int) ((x - viewer.mSavedX) / viewer.mZoomFactor),
								(int) ((y - viewer.mSavedY) / viewer.mZoomFactor));
					}
					viewer.mMotionControl = true;
					viewer.mSavedX = x;
					viewer.mSavedY = y;
					final float distance2 = Math.max(diffX * diffX + diffY * diffY, 10f);
					if (viewer.mStartPinchDistance2 < 0) {
						viewer.mStartPinchDistance2 = distance2;
						viewer.mStartZoomFactor = viewer.mZoomFactor;
					} else {
						viewer.mZoomFactor = viewer.mStartZoomFactor
								* FloatMath.sqrt(distance2 / viewer.mStartPinchDistance2);
						if (viewer.mZoomFactor < viewer.mMinZoomFactor * 0.75f) {
							viewer.mZoomFactor = viewer.mMinZoomFactor * 0.75f;
						} else if (viewer.mZoomFactor > 1.25f) {
							viewer.mZoomFactor = 1.25f;
						}
						viewer.postInvalidate();
					}
				}
					break;
			}
			return true;
		}
	}
}