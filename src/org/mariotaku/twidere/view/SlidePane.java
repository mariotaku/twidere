/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.view;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import org.mariotaku.twidere.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SlidePane extends FrameLayout {

	private final LinearLayout mInternalLinearLayout;
	private final FrameLayout mInternalContentView;

	private int mBlankViewWidth, mContentWidth;
	
	private boolean mFirstCreate = true;
	
	private final boolean mShouldDisableScroll;
	
	public SlidePane(Context context) {
		this(context, null);
	}
	
	public SlidePane(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public SlidePane(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mInternalLinearLayout = new LinearLayout(context);
		setWillNotDraw(false);
		final Resources res = getResources();
		mShouldDisableScroll = res.getBoolean(R.bool.should_disable_scroll);
		final LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, MATCH_PARENT, res.getInteger(R.integer.pane_right_shadow_weight));
		mInternalLinearLayout.addView(new BlankView(this), lp1);
		final LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, MATCH_PARENT, res.getInteger(R.integer.pane_right_content_weight));
		mInternalLinearLayout.addView(mInternalContentView = new InternalContentView(this), lp2);
		addView(mInternalLinearLayout);
		final FrameLayout v = new FrameLayout(context);
		v.setId(R.id.right_pane);
		addContent(v);
	}
	
	public void addContent(View view) {
		mInternalContentView.addView(view);
	}

	public void addContent(View view, FrameLayout.LayoutParams params) {
		mInternalContentView.addView(view, params);
	}
	
	private Thread mThread;
	
	void abortAnimation() {
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
	}
	
	void animateContentTo(final int x) {
		if (shouldDisableScroll()) return;
		abortAnimation();
		mThread = new AnimateScrollThread(x);
		mThread.start();
	}
	
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mFirstCreate) {
			//closeNoAnimation();
			post(new CloseNoAnimationRunnable());
			mFirstCreate = false;
		}
	}
	
	class CloseNoAnimationRunnable implements Runnable {

		public void run() {
			closeNoAnimation();
		}
		
		
	}
	
	boolean shouldDisableScroll() {
		return mShouldDisableScroll;
	}

	class AnimateScrollThread extends Thread {

		final int x;

		AnimateScrollThread(int x) {
			this.x = x;
		}
		
		public void run() {
			final int move = getContentScrollX() - x;
			if (move == 0) return;
			final int max = getMaxScrollWidth();
			final int step = (int) getResources().getDisplayMetrics().density * 16;
			try {
				int scroll = getContentScrollX();
				while (scroll >= -max && scroll <= 0) {
					scroll = getContentScrollX();
					post(new ScrollRunnable(limit(getContentScrollX() - (move > 0 ? step : -step), -max, 0)));
					Thread.sleep(1L);
				}
			} catch (InterruptedException e) {
				
			}
		}
		
		class ScrollRunnable implements Runnable {

			final int x;
			
			ScrollRunnable(final int x) {
				this.x = x;
			}
			
			public void run() {
				scrollContentTo(x);
				final int scroll = getContentScrollX();
				if (scroll == 0 || scroll == -getMaxScrollWidth()) {
					abortAnimation();
				}
			}
			
		}
	}
	
	void scrollContentTo(int x) {
		if (shouldDisableScroll()) return;
		mInternalLinearLayout.scrollTo(x, 0);
		updateMainBackground();
	}

	int getContentScrollX() {
		return mInternalLinearLayout.getScrollX();
	}
	
	int getMaxScrollWidth() {
		return mContentWidth - mBlankViewWidth;
	}
	
	void setBlankViewWidth(int width) {
		mBlankViewWidth = width;
	}
	
	void setContentWidth(int width) {
		mContentWidth = width;
	}
	
	void updateMainBackground() {
		if (shouldDisableScroll()) {
			setBackgroundColor(Color.TRANSPARENT);
			return;
		}
		final int sx = getContentScrollX();
		final float percent = (float)(limit(sx + getMaxScrollWidth(), 0, getMaxScrollWidth())) / getMaxScrollWidth();
		setBackgroundColor(Color.argb((int)(percent * 0x80), 0, 0, 0));
	}
	
	static class BlankView extends View {
		
		private final SlidePane parent;
		
		public BlankView(SlidePane parent) {
			super(parent.getContext());
			this.parent = parent;
			setBackgroundResource(R.drawable.two_panes_shadow_right);
		}
		
		public void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			parent.setBlankViewWidth(w);
		}

		public boolean onTouchEvent(MotionEvent event) {
			final int action = event.getAction();
			switch (action) {
				case MotionEvent.ACTION_DOWN: {
					parent.abortAnimation();
					parent.close();
					break;
				}
			}
			return super.onTouchEvent(event);
		}
	}
	
	static class InternalContentView extends FrameLayout {
		
		private final SlidePane parent;
		private final ViewConfiguration conf;
		
		private int mTotalMove;
		private Integer mTempDelta;
		
		InternalContentView(SlidePane parent) {
			super(parent.getContext());
			this.parent = parent;
			this.conf = ViewConfiguration.get(getContext());
			setWillNotDraw(false);
			setId(R.id.right_pane_container);
		}

		public void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			parent.setContentWidth(w);
			parent.updateMainBackground();
		}
		
		public boolean onInterceptTouchEvent(MotionEvent event) {
			if (parent.shouldDisableScroll()) return false;
			final int action = event.getAction();
			switch (action) {
				case MotionEvent.ACTION_DOWN: {
					parent.abortAnimation();
					mTempDelta = null;
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					final int history_size = event.getHistorySize();
					if (history_size <= 0) break;
					final int delta = Math.round(event.getHistoricalX(0) - event.getX());
					if (delta != 0) mTempDelta = delta;
					mTotalMove += delta;
					if (Math.abs(mTotalMove) >= conf.getScaledTouchSlop()) return true;
					break;
				}
				case MotionEvent.ACTION_UP: {
					final int move = mTotalMove;
					mTotalMove = 0;	
					if (Math.abs(move) >= conf.getScaledTouchSlop()) {
						return true;
					}
					break;
				}
			}
			return false;
		}
		
		public boolean onTouchEvent(MotionEvent event) {
			if (parent.shouldDisableScroll()) return false;
			final int max = parent.getMaxScrollWidth();
			final int action = event.getAction();
			switch (action) {
				case MotionEvent.ACTION_DOWN: {
					parent.abortAnimation();
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					final int history_size = event.getHistorySize();
					if (history_size <= 0) break;
					final int delta = Math.round(event.getHistoricalX(0) - event.getX());
					if (delta != 0) mTempDelta = delta;
					mTotalMove += delta;
					if (Math.abs(mTotalMove) < conf.getScaledTouchSlop()) return false;
					final int scroll_x = parent.getContentScrollX();
					parent.scrollContentTo(limit(scroll_x + delta, -max, 0));
					return true;
				}
				case MotionEvent.ACTION_UP: {
					final Integer delta = mTempDelta;
					final int move = mTotalMove;
					mTempDelta = null;
					mTotalMove = 0;
					if (delta != null && Math.abs(move) >= conf.getScaledTouchSlop()) {
						parent.animateContentTo(delta > 0 ? 0 : -max);
						return true;
					}
					break;
				}
			}
			return true;
		}
	}
	
	
	static int limit(int value, int min, int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}
	
	public void close() {
		if (shouldDisableScroll()) return;
		animateContentTo(-getMaxScrollWidth());
	}
	
	public void open() {
		if (shouldDisableScroll()) return;
		animateContentTo(0);
	}
	
	public void closeNoAnimation() {
		if (shouldDisableScroll()) return;
		scrollContentTo(-getMaxScrollWidth());
	}
	
	public void openNoAnimation() {
		if (shouldDisableScroll()) return;
		scrollContentTo(0);
	}
}
