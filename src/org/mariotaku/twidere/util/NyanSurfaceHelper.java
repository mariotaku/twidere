package org.mariotaku.twidere.util;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.util.Timer;
import java.util.TimerTask;

public final class NyanSurfaceHelper implements SurfaceHolder.Callback {

	private SurfaceHolder mHolder;
	private Timer mTimer;
	private final NyanDrawingHelper mNyanDrawingHelper;

	public NyanSurfaceHelper(final Context context) {
		mNyanDrawingHelper = new NyanDrawingHelper(context);
	}

	public void setScale(final float scale) {
		mNyanDrawingHelper.setScale(scale);
	}

	public void start() {
		if (mTimer != null || mHolder == null) return;
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new DrawingTask(mHolder, mNyanDrawingHelper), 0, 66);
	}

	public void stop() {
		if (mTimer != null) {
			mTimer.cancel();
		}
		mTimer = null;
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

	private static class DrawingTask extends TimerTask {

		private final SurfaceHolder mHolder;
		private final NyanDrawingHelper mHelper;

		DrawingTask(final SurfaceHolder holder, final NyanDrawingHelper helper) {
			mHolder = holder;
			mHelper = helper;
		}

		@Override
		public void run() {
			final Canvas c = mHolder.lockCanvas();
			if (c == null) return;
			if (mHelper != null) {
				mHelper.dispatchDraw(c);
			}
			mHolder.unlockCanvasAndPost(c);
		}

	}
}
