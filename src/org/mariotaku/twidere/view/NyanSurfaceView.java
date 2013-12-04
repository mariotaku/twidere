package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.mariotaku.twidere.R;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class NyanSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	private final IDrawingHelper mStarsHelper;
	private final DrawableDrawingHelper mRainbowHelper, mSakamotoHelper;
	private Timer mTimer;
	private final int mBackgroundColor;

	private static final int[] RAINBOW_FRAMES = { R.drawable.nyan_rainbow_frame00_tile,
			R.drawable.nyan_rainbow_frame01_tile, R.drawable.nyan_rainbow_frame02_tile,
			R.drawable.nyan_rainbow_frame03_tile, R.drawable.nyan_rainbow_frame04_tile,
			R.drawable.nyan_rainbow_frame05_tile, R.drawable.nyan_rainbow_frame06_tile,
			R.drawable.nyan_rainbow_frame07_tile, R.drawable.nyan_rainbow_frame08_tile,
			R.drawable.nyan_rainbow_frame09_tile, R.drawable.nyan_rainbow_frame10_tile,
			R.drawable.nyan_rainbow_frame11_tile };

	public NyanSurfaceView(final Context context) {
		this(context, null);
	}

	public NyanSurfaceView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NyanSurfaceView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final Resources res = getResources();
		final int starRows = res.getInteger(R.integer.nyan_star_rows);
		final int starCols = res.getInteger(R.integer.nyan_star_cols);
		final int starDotSize = res.getDimensionPixelSize(R.dimen.nyan_star_dot_size);
		mStarsHelper = new StarsDrawingHelper(starRows, starCols, starDotSize, Color.WHITE);
		mRainbowHelper = new DrawableDrawingHelper(createRainbowDrawable());
		mSakamotoHelper = new DrawableDrawingHelper(res.getDrawable(R.drawable.nyan_sakamoto));
		mBackgroundColor = res.getColor(R.color.nyan_background);
		getHolder().addCallback(this);
	}

	@Override
	public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
		mStarsHelper.dispatchSizeChanged(width, height);
		mSakamotoHelper.dispatchSizeChanged(width, height);
		final int centerX = width / 2, centerY = height / 2;
		final int sakamotoW = mSakamotoHelper.getIntrinsicWidth(), sakamotoH = mSakamotoHelper.getIntrinsicHeight();
		final int sakamotoLeft = centerX - sakamotoW / 2, sakamotoBottom = centerY + sakamotoH / 2;
		mSakamotoHelper.setBounds(sakamotoLeft, sakamotoBottom - sakamotoH, sakamotoLeft + sakamotoW, sakamotoBottom);
		final int rainbowH = mRainbowHelper.getIntrinsicHeight();
		final int rainbowTop = centerY + rainbowH / 5;
		mRainbowHelper.setBounds(0, rainbowTop, centerX - sakamotoW / 4, rainbowTop + rainbowH);
	}

	@Override
	public void surfaceCreated(final SurfaceHolder holder) {
		final IDrawingHelper[] helpers = { mStarsHelper, mRainbowHelper, mSakamotoHelper };
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new DrawingTask(getHolder(), mBackgroundColor, helpers), 0, 66);
	}

	@Override
	public void surfaceDestroyed(final SurfaceHolder holder) {
		if (mTimer != null) {
			mTimer.cancel();
		}
		mTimer = null;
	}

	private Drawable createRainbowDrawable() {
		final AnimationDrawable ad = new AnimationDrawable();
		ad.setOneShot(false);
		final Resources res = getResources();
		for (final int frameRes : RAINBOW_FRAMES) {
			final Bitmap b = BitmapFactory.decodeResource(res, frameRes);
			final MyBitmapDrawable bd = new MyBitmapDrawable(res, b);
			bd.setTileModeX(TileMode.REPEAT);
			bd.setTileModeY(TileMode.REPEAT);
			ad.addFrame(bd, 70);
		}
		return ad;
	}

	public static interface IDrawingHelper {
		public void dispatchOnDraw(final Canvas canvas);

		public void dispatchSizeChanged(final int w, final int h);
	}

	static final class DrawableDrawingHelper implements IDrawingHelper {

		private final Drawable mDrawable;
		private final int mAnimationFrames;
		private int mCurrentFrame = 0;

		DrawableDrawingHelper(final Drawable drawable) {
			mDrawable = drawable;
			if (drawable instanceof AnimationDrawable) {
				mAnimationFrames = ((AnimationDrawable) drawable).getNumberOfFrames();
			} else {
				mAnimationFrames = -1;
			}
		}

		@Override
		public void dispatchOnDraw(final Canvas canvas) {
			if (mAnimationFrames > 0) {
				final AnimationDrawable ad = (AnimationDrawable) mDrawable;
				final Drawable frame = ad.getFrame(mCurrentFrame++);
				frame.draw(canvas);
				if (mCurrentFrame >= mAnimationFrames) {
					mCurrentFrame = 0;
				}
			} else {
				mDrawable.draw(canvas);
			}
		}

		@Override
		public void dispatchSizeChanged(final int w, final int h) {
		}

		public Drawable getDrawable() {
			return mDrawable;
		}

		public int getIntrinsicHeight() {
			return mDrawable.getIntrinsicHeight();
		}

		public int getIntrinsicWidth() {
			return mDrawable.getIntrinsicWidth();
		}

		public int getMinimumHeight() {
			return mDrawable.getMinimumHeight();
		}

		public int getMinimumWidth() {
			return mDrawable.getMinimumWidth();
		}

		public void setBounds(final int left, final int top, final int right, final int bottom) {
			if (mAnimationFrames > 0) {
				for (int i = 0; i < mAnimationFrames; i++) {
					final Drawable frame = ((AnimationDrawable) mDrawable).getFrame(i);
					frame.setBounds(left, top, right, bottom);
				}
			} else {
				mDrawable.setBounds(left, top, right, bottom);
			}
		}

	}

	static class DrawingTask extends TimerTask {

		private final int mBackground;
		private final SurfaceHolder mHolder;
		private final IDrawingHelper[] mHelpers;

		DrawingTask(final SurfaceHolder holder, final int background, final IDrawingHelper... helpers) {
			mBackground = background;
			mHolder = holder;
			mHelpers = helpers;
		}

		@Override
		public void run() {
			final Canvas c = mHolder.lockCanvas();
			if (c == null) return;
			c.drawColor(mBackground);
			if (mHelpers != null) {
				for (final IDrawingHelper h : mHelpers) {
					h.dispatchOnDraw(c);
				}
			}
			mHolder.unlockCanvasAndPost(c);
		}

	}

	static class MyBitmapDrawable extends BitmapDrawable {
		private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
		private final Matrix mMatrix = new Matrix();

		private boolean mRebuildShader = true;

		public MyBitmapDrawable(final Resources res, final Bitmap bitmap) {
			super(res, bitmap);
		}

		@Override
		public void draw(final Canvas canvas) {
			final Bitmap bitmap = getBitmap();
			if (bitmap == null) return;

			if (mRebuildShader) {
				mPaint.setShader(new BitmapShader(bitmap, getTileMode(getTileModeX()), getTileMode(getTileModeY())));
				mRebuildShader = false;
			}

			final Rect bounds = getBounds();
			final int height = bounds.bottom - bounds.top;
			// Translate down by the remainder
			mMatrix.setTranslate(0, bounds.top);
			canvas.save();
			canvas.setMatrix(mMatrix);
			canvas.drawRect(bounds.left, 0, bounds.right, height, mPaint);
			canvas.restore();
		}

		private static TileMode getTileMode(final TileMode mode) {
			return mode != null ? mode : TileMode.CLAMP;
		}
	}

	static final class StarsDrawingHelper implements IDrawingHelper {

		private final int mStarRows, mStarCols, mStarDotSize;

		private final Paint mPaint;

		private final ArrayList<Star> mStars = new ArrayList<Star>();

		private final Random mRandom = new Random();

		public StarsDrawingHelper(final int starRows, final int starCols, final int starDotSize, final int starColor) {
			mStarRows = starRows;
			mStarCols = starCols;
			mStarDotSize = starDotSize;
			mPaint = new Paint();
			mPaint.setColor(starColor);
		}

		@Override
		public void dispatchOnDraw(final Canvas canvas) {
			final int w = canvas.getWidth(), h = canvas.getHeight();
			if (w <= 0 || h <= 0) return;
			for (final Star star : mStars.toArray(new Star[mStars.size()])) {
				final int col = star.nextColumn(), row = star.nextRow();
				final float y = (row + 0.5f) * (h / mStarRows), x = (col + 0.5f) * (w / mStarCols);
				drawStar(canvas, x, y, star.nextFrame());
			}
		}

		@Override
		public void dispatchSizeChanged(final int w, final int h) {
			mStars.clear();
			if (w <= 0 || h <= 0) return;
			for (int i = 0; i < mStarRows; i++) {
				final int frame = mRandom.nextInt(7);
				final int col = mRandom.nextInt(mStarCols);
				final Star star;
				if (mRandom.nextBoolean()) {
					star = new Star1(frame, col, i, mStarCols, mStarRows);
				} else {
					star = new Star2(frame, col, i, mStarCols, mStarRows);
				}
				mStars.add(star);
			}
		}

		private void drawStar(final Canvas canvas, final float x, final float y, final byte[][] frame) {
			final int rows = frame.length;
			for (int row = 0; row < rows; row++) {
				final int cols = frame[row].length;
				final float top = y + mStarDotSize * row - mStarDotSize * rows / 2;
				for (int col = 0; col < cols; col++) {
					final byte point = frame[row][col];
					if (point != 0) {
						final float left = x + mStarDotSize * col - mStarDotSize * cols / 2;
						canvas.drawRect(left, top, left + mStarDotSize, top + mStarDotSize, mPaint);
					}
				}
			}
		}

		private static abstract class Star implements StarAnimFrames {

			private final int mMaxColumn, mMaxRow;
			private int mCurrentFrame, mCurrentColumn, mCurrentRow;

			Star(final int initialFrame, final int initialColumn, final int initialRow, final int maxColumn,
					final int maxRow) {
				setFrame(initialFrame);
				mMaxColumn = maxColumn;
				mMaxRow = maxRow;
				setColumn(initialColumn);
				setRow(initialRow);
			}

			public abstract byte[][][] getFrames();

			public final int length() {
				return getFrames().length;
			}

			public final int nextColumn() {
				final int column = mCurrentColumn;
				mCurrentColumn--;
				if (mCurrentColumn < 0) {
					mCurrentColumn = mMaxColumn - 1;
				}
				return column;
			}

			public final byte[][] nextFrame() {
				final byte[][] frame = getFrames()[mCurrentFrame];
				mCurrentFrame++;
				if (mCurrentFrame >= length()) {
					mCurrentFrame = 0;
				}
				return frame;
			}

			public final int nextRow() {
				return mCurrentRow;
			}

			public void setColumn(final int column) {
				if (column < 0 || column >= mMaxColumn) {
					mCurrentColumn = 0;
				} else {
					mCurrentColumn = column;
				}
			}

			public void setFrame(final int frame) {
				if (frame < 0 || frame >= length()) {
					mCurrentFrame = 0;
				} else {
					mCurrentFrame = frame;
				}
			}

			public void setRow(final int row) {
				if (row < 0 || row >= mMaxRow) {
					mCurrentRow = 0;
				} else {
					mCurrentRow = row;
				}
			}
		}

		private static final class Star1 extends Star {

			private static final byte[][][] FRAMES = new byte[][][] { FRAME1, FRAME2, FRAME3, FRAME4, FRAME5, FRAME6 };

			public Star1(final int initialFrame, final int initialColumn, final int initialRow, final int maxColumn,
					final int maxRow) {
				super(initialFrame, initialColumn, initialRow, maxColumn, maxRow);
			}

			@Override
			public byte[][][] getFrames() {
				return FRAMES;
			}
		}

		private static final class Star2 extends Star {
			private static final byte[][][] FRAMES = new byte[][][] { FRAME1, FRAME6, FRAME5, FRAME4, FRAME3, FRAME2 };

			public Star2(final int initialFrame, final int initialColumn, final int initialRow, final int maxColumn,
					final int maxRow) {
				super(initialFrame, initialColumn, initialRow, maxColumn, maxRow);
			}

			@Override
			public byte[][][] getFrames() {
				return FRAMES;
			}
		}

		private static interface StarAnimFrames {
			/*
			 * @formatter:off
			 */
			static final byte[][] FRAME1 = {
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							0, 0, 0, 1, 0, 0, 0
					},
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							0, 0, 0, 0, 0, 0, 0
					}
			};
			static final byte[][] FRAME2 = {
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							0, 0, 0, 1, 0, 0, 0
					},
					{
							0, 0, 1, 0, 1, 0, 0
					},
					{
							0, 0, 0, 1, 0, 0, 0
					},
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							0, 0, 0, 0, 0, 0, 0
					}
			};
			static final byte[][] FRAME3 = {
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							0, 0, 0, 1, 0, 0, 0
					},
					{
							0, 0, 0, 1, 0, 0, 0
					},
					{
							0, 1, 1, 0, 1, 1, 0
					},
					{
							0, 0, 0, 1, 0, 0, 0
					},
					{
							0, 0, 0, 1, 0, 0, 0
					},
					{
							0, 0, 0, 0, 0, 0, 0
					}
			};
			static final byte[][] FRAME4 = {
					{
							0, 0, 0, 1, 0, 0, 0
					},
					{
							0, 0, 0, 1, 0, 0, 0
					},
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							1, 1, 0, 1, 0, 1, 1
					},
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							0, 0, 0, 1, 0, 0, 0
					},
					{
							0, 0, 0, 1, 0, 0, 0
					}
			};
			static final byte[][] FRAME5 = {
					{
							0, 0, 0, 1, 0, 0, 0
					},
					{
							0, 1, 0, 0, 0, 1, 0
					},
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							1, 0, 0, 0, 0, 0, 1
					},
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							0, 1, 0, 0, 0, 1, 0
					},
					{
							0, 0, 0, 1, 0, 0, 0
					}
			};
			static final byte[][] FRAME6 = {
					{
							0, 0, 0, 1, 0, 0, 0
					},
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							1, 0, 0, 0, 0, 0, 1
					},
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							0, 0, 0, 0, 0, 0, 0
					},
					{
							0, 0, 0, 1, 0, 0, 0
					}
			};

			/*
			 * @formatter:on
			 */
		}
	}
}
