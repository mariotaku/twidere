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

import org.mariotaku.twidere.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;

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

	public void pullToRefresh() {
		if (mArrowRotated) {
			mHeaderArrow.startAnimation(mRotateAnimation);
			rotateArrow();
			mArrowRotated = false;
		}
		mHeaderText.setText(Html.fromHtml(mPullLabel));
	}

	public void refreshing() {
		mHeaderText.setText(Html.fromHtml(mRefreshingLabel));
		mHeaderArrow.setVisibility(View.INVISIBLE);
		mHeaderProgress.setVisibility(View.VISIBLE);
		mSubHeaderText.setVisibility(View.GONE);
	}

	public void releaseToRefresh() {
		if (!mArrowRotated) {
			mHeaderArrow.startAnimation(mRotateAnimation);
			rotateArrow();
			mArrowRotated = true;
		}
		mHeaderText.setText(Html.fromHtml(mReleaseLabel));
	}

	public void reset() {
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
	}

	public void setRefreshingLabel(final String refreshingLabel) {
		mRefreshingLabel = refreshingLabel;
	}

	public void setReleaseLabel(final String releaseLabel) {
		mReleaseLabel = releaseLabel;
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
}
