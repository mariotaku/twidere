/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.handmark.pulltorefresh.library.internal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import org.mariotaku.twidere.R;

public class LoadingLayout extends FrameLayout {

	static final int DEFAULT_ROTATION_ANIMATION_DURATION = 600;

	private final ImageView mHeaderArrow;
	private final ProgressBar mHeaderProgress;

	private final TextView mHeaderText;
	private final TextView mSubHeaderText;

	private String mPullLabel;
	private String mRefreshingLabel;
	private String mReleaseLabel;

	private final Mode mMode;

	private final Animation mRotateAnimation;

	private boolean mArrowRotated;

	public LoadingLayout(final Context context, final Mode mode, final TypedArray attrs) {
		super(context);
		final ViewGroup header = (ViewGroup) LayoutInflater.from(context)
				.inflate(R.layout.pull_to_refresh_header, this);
		mMode = mode;
		mHeaderText = (TextView) header.findViewById(R.id.pull_to_refresh_text);
		mSubHeaderText = (TextView) header.findViewById(R.id.pull_to_refresh_sub_text);
		mHeaderProgress = (ProgressBar) header.findViewById(R.id.pull_to_refresh_progress);
		mHeaderArrow = (ImageView) header.findViewById(R.id.pull_to_refresh_arrow);
		mRotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.pull_to_refresh_rotate);

		switch (mode) {
			case PULL_UP_TO_REFRESH:
				// Load in labels
				mPullLabel = context.getString(R.string.pull_to_refresh_from_bottom_pull_label);
				mRefreshingLabel = context.getString(R.string.pull_to_refresh_from_bottom_refreshing_label);
				mReleaseLabel = context.getString(R.string.pull_to_refresh_from_bottom_release_label);
				rotateArrow();
				break;

			case PULL_DOWN_TO_REFRESH:
			default:
				// Load in labels
				mPullLabel = context.getString(R.string.pull_to_refresh_pull_label);
				mRefreshingLabel = context.getString(R.string.pull_to_refresh_refreshing_label);
				mReleaseLabel = context.getString(R.string.pull_to_refresh_release_label);
				break;
		}

		if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderTextColor)) {
			final ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderTextColor);
			setTextColor(null != colors ? colors : ColorStateList.valueOf(0xFF000000));
		}
		if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderSubTextColor)) {
			final ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderSubTextColor);
			setSubTextColor(null != colors ? colors : ColorStateList.valueOf(0xFF000000));
		}
		if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderBackground)) {
			final Drawable background = attrs.getDrawable(R.styleable.PullToRefresh_ptrHeaderBackground);
			if (null != background) {
				setBackgroundDrawable(background);
			}
		}

		reset();
	}

	public LoadingLayout(final Context context, final TypedArray attrs, final int defStyle) {
		this(context, Mode.PULL_DOWN_TO_REFRESH, attrs);
	}
	
	private boolean mNotifyPullToRefreshCalled;

	public void resetAccessibilityState() {
		// TODO: Implement this method
		mNotifyPullToRefreshCalled = false;
	}

	public void notifyPullToRefresh() {
		if (mNotifyPullToRefreshCalled) return;
		final CharSequence text = Html.fromHtml(mPullLabel);
		notifyAccessibilityService(text);
		mNotifyPullToRefreshCalled = true;
	}

	public void pullToRefresh() {
		mNotifyPullToRefreshCalled = false;
		if (mArrowRotated) {
			mHeaderArrow.startAnimation(mRotateAnimation);
			rotateArrow();
			mArrowRotated = false;
		}
		final CharSequence text = Html.fromHtml(mPullLabel);
		mHeaderText.setText(text);
		notifyAccessibilityService(text);
	}

	public void refreshing() {
		final CharSequence text = Html.fromHtml(mRefreshingLabel);
		mHeaderText.setText(text);
		notifyAccessibilityService(text);
		mHeaderArrow.setVisibility(View.INVISIBLE);
		mHeaderProgress.setVisibility(View.VISIBLE);
		mSubHeaderText.setVisibility(View.GONE);
	}

	public void releaseToRefresh() {
		mNotifyPullToRefreshCalled = false;
		if (!mArrowRotated) {
			mHeaderArrow.startAnimation(mRotateAnimation);
			rotateArrow();
			mArrowRotated = true;
		}
		final CharSequence text = Html.fromHtml(mReleaseLabel);
		mHeaderText.setText(text);
		notifyAccessibilityService(text);
	}

	public void reset() {
		mNotifyPullToRefreshCalled = false;
		mHeaderText.setText(Html.fromHtml(mPullLabel));
		mHeaderArrow.setImageResource(R.drawable.pull_to_refresh_arrow);
		mHeaderArrow.setVisibility(View.VISIBLE);
		mHeaderProgress.setVisibility(View.INVISIBLE);
		mArrowRotated = false;
		if (mMode == Mode.PULL_UP_TO_REFRESH) {
			rotateArrow();
		}
		if (TextUtils.isEmpty(mSubHeaderText.getText())) {
			mSubHeaderText.setVisibility(View.GONE);
		} else {
			mSubHeaderText.setVisibility(View.VISIBLE);
		}
	}

	public void setPullLabel(final String pullLabel) {
		mPullLabel = pullLabel;
		if (!mArrowRotated) {
			mHeaderText.setText(Html.fromHtml(mPullLabel));
		}
	}

	public void setRefreshingLabel(final String refreshingLabel) {
		mRefreshingLabel = refreshingLabel;
	}

	public void setReleaseLabel(final String releaseLabel) {
		mReleaseLabel = releaseLabel;
		if (mArrowRotated) {
			mHeaderText.setText(Html.fromHtml(mReleaseLabel));
		}
	}

	public void setSubHeaderText(final CharSequence label) {
		if (TextUtils.isEmpty(label)) {
			mSubHeaderText.setVisibility(View.GONE);
		} else {
			mSubHeaderText.setText(label);
			mSubHeaderText.setVisibility(View.VISIBLE);
		}
	}

	public void setSubTextColor(final ColorStateList color) {
		mSubHeaderText.setTextColor(color);
	}

	public void setSubTextColor(final int color) {
		setSubTextColor(ColorStateList.valueOf(color));
	}

	public void setTextColor(final ColorStateList color) {
		mHeaderText.setTextColor(color);
		mSubHeaderText.setTextColor(color);
	}

	public void setTextColor(final int color) {
		setTextColor(ColorStateList.valueOf(color));
	}

	private void rotateArrow() {
		final Drawable drawable = mHeaderArrow.getDrawable();
		final Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
				Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);
		canvas.save();
		canvas.rotate(180.0f, canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		canvas.restore();
		mHeaderArrow.setImageBitmap(bitmap);
	}
	
	private void notifyAccessibilityService(final CharSequence text) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.DONUT) return;
		final AccessibilityManager accessibilityManager = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
		if (!accessibilityManager.isEnabled()) return;
		// Prior to SDK 16, announcements could only be made through FOCUSED
		// events. Jelly Bean (SDK 16) added support for speaking text verbatim
		// using the ANNOUNCEMENT event type.
		final int eventType;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			eventType = AccessibilityEvent.TYPE_VIEW_FOCUSED;
		} else {
			eventType = AccessibilityEventCompat.TYPE_ANNOUNCEMENT;
		}

		// Construct an accessibility event with the minimum recommended
		// attributes. An event without a class name or package may be dropped.
		final AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
		event.getText().add(text);
		event.setClassName(getClass().getName());
		event.setPackageName(getContext().getPackageName());
		event.setSource(this);

		// Sends the event directly through the accessibility manager. If your
		// application only targets SDK 14+, you should just call
		// getParent().requestSendAccessibilityEvent(this, event);
		accessibilityManager.sendAccessibilityEvent(event);
	}
}
