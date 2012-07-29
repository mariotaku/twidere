/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mariotaku.twidere.view;

import org.mariotaku.twidere.R;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This widget implements the dynamic action bar tab behavior that can change
 * across different configurations or circumstances.
 */
public class TabPageIndicator extends HorizontalScrollView implements ExtendedViewPager.OnPageChangeListener {

	private Runnable mTabSelector;
	private int mCurrentItem;
	private TitleProvider mAdapter;

	private OnClickListener mTabClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			if (!mPagingEnabled) return;
			final TabView tabView = (TabView) view;
			if (mCurrentItem == tabView.getIndex()) {
				mAdapter.onPageReselected(mCurrentItem);
			}
			mCurrentItem = tabView.getIndex();
			mViewPager.setCurrentItem(mCurrentItem);
		}
	};

	private LinearLayout mTabLayout;
	private ExtendedViewPager mViewPager;
	private ExtendedViewPager.OnPageChangeListener mListener;

	private LayoutInflater mInflater;

	int mMaxTabWidth;
	private int mSelectedTabIndex;

	private boolean mPagingEnabled = true;

	public TabPageIndicator(Context context) {
		super(context);
	}

	public TabPageIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		setHorizontalScrollBarEnabled(false);

		mInflater = LayoutInflater.from(context);

		mTabLayout = new LinearLayout(getContext());
		addView(mTabLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
	}

	public void notifyDataSetChanged() {
		mTabLayout.removeAllViews();
		mAdapter = (TitleProvider) mViewPager.getAdapter();
		final int count = ((PagerAdapter) mAdapter).getCount();
		for (int i = 0; i < count; i++) {
			final String title = mAdapter.getTitle(i);
			final Integer icon = mAdapter.getIcon(i);
			if (title != null && icon != null) {
				addTab(title, icon, i);
			} else if (title == null && icon != null) {
				addTab(icon, i);
			} else if (title != null && icon == null) {
				addTab(title, i);
			}
		}
		if (mSelectedTabIndex > count) {
			mSelectedTabIndex = count - 1;
		}
		setCurrentItem(mSelectedTabIndex);
		requestLayout();
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mTabSelector != null) {
			// Re-post the selector we saved
			post(mTabSelector);
		}
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mTabSelector != null) {
			removeCallbacks(mTabSelector);
		}
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final boolean lockedExpanded = widthMode == MeasureSpec.EXACTLY;
		setFillViewport(lockedExpanded);

		final int childCount = mTabLayout.getChildCount();
		if (childCount > 1 && (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST)) {
			if (childCount > 2) {
				mMaxTabWidth = (int) (MeasureSpec.getSize(widthMeasureSpec) * 0.4f);
			} else {
				mMaxTabWidth = MeasureSpec.getSize(widthMeasureSpec) / 2;
			}
		} else {
			mMaxTabWidth = -1;
		}

		final int oldWidth = getMeasuredWidth();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int newWidth = getMeasuredWidth();

		if (lockedExpanded && oldWidth != newWidth) {
			// Recenter the tab display if we're at a new (scrollable) size.
			setCurrentItem(mSelectedTabIndex);
		}
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		if (mListener != null) {
			mListener.onPageScrolled(arg0, arg1, arg2);
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		if (mListener != null) {
			mListener.onPageScrollStateChanged(arg0);
		}
	}

	@Override
	public void onPageSelected(int position) {
		setCurrentItem(position);
		mAdapter.onPageSelected(position);
		if (mListener != null) {
			mListener.onPageSelected(position);

		}
	}

	public void setCurrentItem(int item) {
		if (mViewPager == null) return;
		// throw new IllegalStateException("ViewPager has not been bound.");
		mCurrentItem = item;
		mViewPager.setCurrentItem(item);
		mSelectedTabIndex = item;
		final int tabCount = mTabLayout.getChildCount();
		for (int i = 0; i < tabCount; i++) {
			final View child = mTabLayout.getChildAt(i);
			final boolean isSelected = i == item;
			child.setSelected(isSelected);
			if (isSelected) {
				animateToTab(item);
			}
		}
	}

	public void setOnPageChangeListener(ExtendedViewPager.OnPageChangeListener listener) {
		mListener = listener;
	}

	public void setPagingEnabled(boolean enabled) {
		mViewPager.setPagingEnabled(enabled);
		mPagingEnabled = enabled;
	}

	public void setViewPager(ExtendedViewPager pager) {
		final PagerAdapter adapter = pager.getAdapter();
		if (adapter == null) return;
		// throw new IllegalStateException("ViewPager has not been bound.");
		if (!(adapter instanceof TitleProvider))
			throw new IllegalStateException(
					"ViewPager adapter must implement TitleProvider to be used with TitlePageIndicator.");
		mViewPager = pager;
		pager.setOnPageChangeListener(this);
		notifyDataSetChanged();
	}

	public void setViewPager(ExtendedViewPager pager, int initialPosition) {
		setViewPager(pager);
		setCurrentItem(initialPosition);
	}

	private void addTab(int icon, int index) {
		// Workaround for not being able to pass a defStyle on pre-3.0
		final TabView tabView = (TabView) mInflater.inflate(R.layout.vpi__tab, null);
		tabView.init(this, icon, index);
		tabView.setFocusable(true);
		tabView.setOnClickListener(mTabClickListener);

		mTabLayout.addView(tabView, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
	}

	private void addTab(String text, int index) {
		// Workaround for not being able to pass a defStyle on pre-3.0
		final TabView tabView = (TabView) mInflater.inflate(R.layout.vpi__tab, null);
		tabView.init(this, text, index);
		tabView.setFocusable(true);
		tabView.setOnClickListener(mTabClickListener);

		mTabLayout.addView(tabView, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
	}

	private void addTab(String text, int icon, int index) {
		// Workaround for not being able to pass a defStyle on pre-3.0
		final TabView tabView = (TabView) mInflater.inflate(R.layout.vpi__tab, null);
		tabView.init(this, text, icon, index);
		tabView.setFocusable(true);
		tabView.setOnClickListener(mTabClickListener);

		mTabLayout.addView(tabView, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
	}

	private void animateToTab(final int position) {
		final View tabView = mTabLayout.getChildAt(position);
		if (mTabSelector != null) {
			removeCallbacks(mTabSelector);
		}
		mTabSelector = new Runnable() {

			@Override
			public void run() {
				final int scrollPos = tabView.getLeft() - (getWidth() - tabView.getWidth()) / 2;
				smoothScrollTo(scrollPos, 0);
				mTabSelector = null;
			}
		};
		post(mTabSelector);
	}

	public static class TabView extends LinearLayout {

		private TabPageIndicator mParent;
		private int mIndex;

		public TabView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public int getIndex() {
			return mIndex;
		}

		public void init(TabPageIndicator parent, int icon, int index) {
			mParent = parent;
			mIndex = index;

			final ImageView imageView = (ImageView) findViewById(android.R.id.icon);
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageResource(icon);

			final TextView textView = (TextView) findViewById(android.R.id.text1);
			textView.setVisibility(View.GONE);
		}

		public void init(TabPageIndicator parent, String text, int index) {
			mParent = parent;
			mIndex = index;

			final ImageView imageView = (ImageView) findViewById(android.R.id.icon);
			imageView.setVisibility(View.GONE);

			final TextView textView = (TextView) findViewById(android.R.id.text1);
			textView.setVisibility(View.VISIBLE);
			textView.setText(text);
		}

		public void init(TabPageIndicator parent, String text, int icon, int index) {
			mParent = parent;
			mIndex = index;

			final ImageView imageView = (ImageView) findViewById(android.R.id.icon);
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageResource(icon);

			final TextView textView = (TextView) findViewById(android.R.id.text1);
			textView.setVisibility(View.VISIBLE);
			textView.setText(text);
		}

		@Override
		public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			// Re-measure if we went beyond our maximum size.
			if (mParent.mMaxTabWidth > 0 && getMeasuredWidth() > mParent.mMaxTabWidth) {
				super.onMeasure(MeasureSpec.makeMeasureSpec(mParent.mMaxTabWidth, MeasureSpec.EXACTLY),
						heightMeasureSpec);
			}
		}
	}

	/**
	 * A TitleProvider provides the title to display according to a view.
	 */
	public interface TitleProvider {

		/**
		 * Returns the icon of the view at position
		 * 
		 * @param position
		 * @return
		 */
		public Integer getIcon(int position);

		/**
		 * Returns the title of the view at position
		 * 
		 * @param position
		 * @return
		 */
		public String getTitle(int position);

		public void onPageReselected(int position);

		public void onPageSelected(int position);
	}
}
