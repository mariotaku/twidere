/*
 * Copyright (C) 2010-2012 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.mariotaku.twidere.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;

public class ImageViewActivity extends Activity {

	private ImageView mImageView;
	private ImageLoader mImageLoader;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Uri uri = getIntent().getData();
		if (uri == null) {
			finish();
			return;
		}
		URL url;
		try {
			url = new URL(uri.toString());
		} catch (MalformedURLException e) {
			finish();
			return;
		}
		mImageView = new ImageView(this);
		setContentView(mImageView);
		
		if (mImageLoader != null && !mImageLoader.isCancelled()) {
			mImageLoader.cancel(true);
		}
		mImageLoader = new ImageLoader(url, mImageView);
		mImageLoader.execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mImageView.recycle();
		if (mImageLoader != null && !mImageLoader.isCancelled()) {
			mImageLoader.cancel(true);
		}
	}
	
	private static class ImageLoader extends AsyncTask<Void, Void, Bitmap> {

		private final URL url;
		private final ImageView image_view;
		
		public ImageLoader(URL url, ImageView image_view) {
			this.url = url;
			this.image_view = image_view;
		}
		
		@Override
		protected Bitmap doInBackground(Void... arg0) {
			HttpURLConnection conn;
			try {
				conn = (HttpURLConnection) url.openConnection();
			} catch (IOException e) {
				return null;
			}
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = null;
			try {
				is = conn.getInputStream();
			} catch (IOException e) {
				return null;
			}
			try {
				return BitmapFactory.decodeStream(is);
			} catch (OutOfMemoryError e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			image_view.setBitmap(result);
			super.onPostExecute(result);
		}
		
	}

	private static class ImageView extends View {
		
		private Bitmap mBitmap;
		private final Paint mPaint = new Paint();
		private final Paint mBackgroundPaint = new Paint();

		private volatile int myDx = 0;
		private volatile int myDy = 0;
		private volatile float myZoomFactor = 1.0f;

		private boolean mMotionControl;

		private int mySavedX;

		private int mySavedY;

		private float myStartPinchDistance2 = -1;
		private float myStartZoomFactor;

		ImageView(Context context) {
			super(context);
		}
		
		public void setBitmap(Bitmap bitmap) {
			mBitmap = bitmap;
			invalidate();
		}
		
		public void recycle() {
			if (mBitmap != null && !mBitmap.isRecycled()){
				mBitmap.recycle();
				mBitmap = null;
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getPointerCount()) {
				case 1:
					return onSingleTouchEvent(event);
				case 2:
					return onDoubleTouchEvent(event);
				default:
					return false;
			}
		}

		@Override
		protected void onDraw(final Canvas canvas) {
			mPaint.setColor(Color.BLACK);
			mBackgroundPaint.setColor(0xA0000000);
			final int w = getWidth();
			final int h = getHeight();
			canvas.drawRect(0, 0, w, h, mBackgroundPaint);
			if (mBitmap == null || mBitmap.isRecycled()) return;

			final int bw = (int) (mBitmap.getWidth() * myZoomFactor);
			final int bh = (int) (mBitmap.getHeight() * myZoomFactor);

			final Rect src = new Rect(0, 0, (int) (w / myZoomFactor), (int) (h / myZoomFactor));
			final Rect dst = new Rect(0, 0, w, h);
			if (bw <= w) {
				src.left = 0;
				src.right = mBitmap.getWidth();
				dst.left = (w - bw) / 2;
				dst.right = dst.left + bw;
			} else {
				final int bWidth = mBitmap.getWidth();
				final int pWidth = (int) (w / myZoomFactor);
				src.left = Math.min(bWidth - pWidth, Math.max((bWidth - pWidth) / 2 - myDx, 0));
				src.right += src.left;
			}
			if (bh <= h) {
				src.top = 0;
				src.bottom = mBitmap.getHeight();
				dst.top = (h - bh) / 2;
				dst.bottom = dst.top + bh;
			} else {
				final int bHeight = mBitmap.getHeight();
				final int pHeight = (int) (h / myZoomFactor);
				src.top = Math.min(bHeight - pHeight, Math.max((bHeight - pHeight) / 2 - myDy, 0));
				src.bottom += src.top;
			}
			canvas.drawBitmap(mBitmap, src, dst, mPaint);
		}

		private boolean onDoubleTouchEvent(MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_POINTER_UP:
					myStartPinchDistance2 = -1;
					break;
				case MotionEvent.ACTION_POINTER_DOWN: {
					final float diffX = event.getX(0) - event.getX(1);
					final float diffY = event.getY(0) - event.getY(1);
					myStartPinchDistance2 = Math.max(diffX * diffX + diffY * diffY, 10f);
					myStartZoomFactor = myZoomFactor;
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					final float diffX = event.getX(0) - event.getX(1);
					final float diffY = event.getY(0) - event.getY(1);
					final float distance2 = Math.max(diffX * diffX + diffY * diffY, 10f);
					if (myStartPinchDistance2 < 0) {
						myStartPinchDistance2 = distance2;
						myStartZoomFactor = myZoomFactor;
					} else {
						myZoomFactor = myStartZoomFactor * FloatMath.sqrt(distance2 / myStartPinchDistance2);
						postInvalidate();
					}
				}
					break;
			}
			return true;
		}

		private boolean onSingleTouchEvent(MotionEvent event) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					mMotionControl = false;
					break;
				case MotionEvent.ACTION_DOWN:
					mMotionControl = true;
					mySavedX = x;
					mySavedY = y;
					break;
				case MotionEvent.ACTION_MOVE:
					if (mMotionControl) {
						shift((int) ((x - mySavedX) / myZoomFactor), (int) ((y - mySavedY) / myZoomFactor));
					}
					mMotionControl = true;
					mySavedX = x;
					mySavedY = y;
					break;
			}
			return true;
		}

		private void shift(int dx, int dy) {
			if (mBitmap == null || mBitmap.isRecycled()) return;

			final int w = (int) (getWidth() / myZoomFactor);
			final int h = (int) (getHeight() / myZoomFactor);
			final int bw = mBitmap.getWidth();
			final int bh = mBitmap.getHeight();

			final int newDx, newDy;

			if (w < bw) {
				final int delta = (bw - w) / 2;
				newDx = Math.max(-delta, Math.min(delta, myDx + dx));
			} else {
				newDx = myDx;
			}
			if (h < bh) {
				final int delta = (bh - h) / 2;
				newDy = Math.max(-delta, Math.min(delta, myDy + dy));
			} else {
				newDy = myDy;
			}

			if (newDx != myDx || newDy != myDy) {
				myDx = newDx;
				myDy = newDy;
				postInvalidate();
			}
		}
	}
}
