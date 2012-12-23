package org.mariotaku.gallery3d.ui;

import org.mariotaku.gallery3d.app.ImageViewerGLActivity;
import org.mariotaku.gallery3d.ui.GLRoot.OnGLIdleListener;

import android.os.ConditionVariable;

public class PreparePageFadeoutTexture implements OnGLIdleListener {
	private static final long TIMEOUT = 200;
	public static final String KEY_FADE_TEXTURE = "fade_texture";

	private RawTexture mTexture;
	private final ConditionVariable mResultReady = new ConditionVariable(false);
	private boolean mCancelled = false;
	private GLView mRootPane;

	public PreparePageFadeoutTexture(final GLView rootPane) {
		final int w = rootPane.getWidth();
		final int h = rootPane.getHeight();
		if (w == 0 || h == 0) {
			mCancelled = true;
			return;
		}
		mTexture = new RawTexture(w, h, true);
		mRootPane = rootPane;
	}

	public synchronized RawTexture get() {
		if (mCancelled)
			return null;
		else if (mResultReady.block(TIMEOUT))
			return mTexture;
		else {
			mCancelled = true;
			return null;
		}
	}

	public boolean isCancelled() {
		return mCancelled;
	}

	@Override
	public boolean onGLIdle(final GLCanvas canvas, final boolean renderRequested) {
		if (!mCancelled) {
			try {
				canvas.beginRenderTarget(mTexture);
				mRootPane.render(canvas);
				canvas.endRenderTarget();
			} catch (final RuntimeException e) {
				mTexture = null;
			}
		} else {
			mTexture = null;
		}
		mResultReady.open();
		return false;
	}

	public static void prepareFadeOutTexture(final ImageViewerGLActivity activity, final GLView rootPane) {
		final PreparePageFadeoutTexture task = new PreparePageFadeoutTexture(rootPane);
		if (task.isCancelled()) return;
		final GLRoot root = activity.getGLRoot();
		root.unlockRenderThread();
		try {
			root.addOnGLIdleListener(task);
		} finally {
			root.lockRenderThread();
		}

	}
}
