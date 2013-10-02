package org.mariotaku.twidere.animation;

import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * An animation that rotates the view on the Y axis between two specified
 * angles. This animation also adds a translation on the Z axis (depth) to
 * improve the effect.
 */
public class Rotate3dAnimation extends Animation {
	private final float mFromDegrees;
	private final float mToDegrees;
	private final float mCenterX;
	private final float mCenterY;
	private final float mDepthZ;
	private final boolean mReverse;
	private Camera mCamera;
	private int mWidth;
	private int mHeight;

	/**
	 * Creates a new 3D rotation on the Y axis. The rotation is defined by its
	 * start angle and its end angle. Both angles are in degrees. The rotation
	 * is performed around a center point on the 2D space, definied by a pair of
	 * X and Y coordinates, called centerX and centerY. When the animation
	 * starts, a translation on the Z axis (depth) is performed. The length of
	 * the translation can be specified, as well as whether the translation
	 * should be reversed in time.
	 * 
	 * @param fromDegrees the start angle of the 3D rotation
	 * @param toDegrees the end angle of the 3D rotation
	 * @param centerX the X center of the 3D rotation
	 * @param centerY the Y center of the 3D rotation
	 * @param reverse true if the translation should be reversed, false
	 *            otherwise
	 */
	public Rotate3dAnimation(final float fromDegrees, final float toDegrees, final float centerX, final float centerY,
			final float depthZ, final boolean reverse) {
		super.setBackgroundColor(Color.WHITE);
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;
		mCenterX = centerX;
		mCenterY = centerY;
		mDepthZ = depthZ;
		mReverse = reverse;
	}

	@Override
	public void initialize(final int width, final int height, final int parentWidth, final int parentHeight) {
		super.setBackgroundColor(Color.WHITE);
		super.initialize(width, height, parentWidth, parentHeight);
		mCamera = new Camera();
		mWidth = width;
		mHeight = height;
	}

	@Override
	protected void applyTransformation(final float interpolatedTime, final Transformation t) {
		if (mCamera == null) return;
		super.setBackgroundColor(Color.WHITE);
		final float fromDegrees = mFromDegrees;
		final float degrees = fromDegrees + (mToDegrees - fromDegrees) * interpolatedTime;

		final float centerX = mCenterX * mWidth;
		final float centerY = mCenterY * mHeight;
		final Camera camera = mCamera;
		final Matrix matrix = t.getMatrix();

		camera.save();
		if (mReverse) {
			camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
		} else {
			camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
		}
		camera.rotateX(degrees);
		camera.getMatrix(matrix);
		camera.restore();

		matrix.preTranslate(-centerX, -centerY);
		matrix.postTranslate(centerX, centerY);

	}
}
