package org.mariotaku.twidere.util;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

public final class NyanSurfaceHelper implements SurfaceHolder.Callback {

	private SurfaceHolder mHolder;
	private DrawingThread mThread;
	private final NyanDrawingHelper mNyanDrawingHelper;

	public NyanSurfaceHelper(final Context context) {
		mNyanDrawingHelper = new NyanDrawingHelper(context);
	}

	public SurfaceHolder getHolder() {
		return mHolder;
	}

	public void setScale(final float scale) {
		mNyanDrawingHelper.setScale(scale);
	}

	public void setSkipDrawing(final boolean skipDrawing) {
		if (mThread != null) {
			mThread.setSkipDrawing(skipDrawing);
		}
	}

	public void start() {
		if (mThread != null) return;
		mThread = new DrawingThread(this, mNyanDrawingHelper);
		mThread.start();
	}

	public void stop() {
		if (mThread != null) {
			mThread.cancel();
		}
		mThread = null;
	}

	@Override
	public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
		mNyanDrawingHelper.dispatchSizeChanged(width, height);
	}

	@Override
	public void surfaceCreated(final SurfaceHolder holder) {
		mHolder = holder;
		start();
	}

	@Override
	public void surfaceDestroyed(final SurfaceHolder holder) {
		stop();
		mHolder = null;
	}

	private static class DrawingThread extends Thread {

		private final NyanSurfaceHelper mSurfaceHelper;
		private final NyanDrawingHelper mDrawingHelper;
		private boolean mCancelled;
		private boolean mSkipDrawing;

		DrawingThread(final NyanSurfaceHelper surfaceHelper, final NyanDrawingHelper drawingHelper) {
			mSurfaceHelper = surfaceHelper;
			mDrawingHelper = drawingHelper;
		}

		public void cancel() {
			mCancelled = true;
		}

		@Override
		public void run() {
			while (!mCancelled) {
				final long startTime = System.currentTimeMillis();
				drawFrame();
				final long endTime = System.currentTimeMillis();
				try {
					Thread.sleep(Math.max(0, 66 - (endTime - startTime)));
				} catch (final InterruptedException e) {

				}
			}
		}

		public void setSkipDrawing(final boolean skipDrawing) {
			mSkipDrawing = skipDrawing;
		}

		private void drawFrame() {
			final SurfaceHolder holder = mSurfaceHelper.getHolder();
			if (mSkipDrawing || holder == null || holder.isCreating()) return;
			final Canvas c = holder.lockCanvas();
			if (c == null) return;
			if (mDrawingHelper != null) {
				mDrawingHelper.dispatchDraw(c);
			}
			holder.unlockCanvasAndPost(c);
		}

	}
}