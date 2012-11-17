package org.mariotaku.twidere.view;

import org.mariotaku.twidere.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;

public class SlidingPanel extends ViewGroup {

	private final int mAnchorId;
	private View mAnchor;
	private final int mContentId;
	private View mContent;

	private final int mClosedLimit;

	private boolean mAnimating = false;
	private int mFillOffset = 0;

	private boolean mExpanded = true;

	public SlidingPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidingPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingPanel,
				defStyle, 0);

		final int anchorId = a.getResourceId(R.styleable.SlidingPanel_anchor, 0);
		if (anchorId == 0) {
			throw new IllegalArgumentException("The anchor attribute is required and must refer" +
					" to a different child");
		}

		final int contentId = a.getResourceId(R.styleable.SlidingPanel_content, 0);
		if (contentId == anchorId || contentId == 0) {
			throw new IllegalArgumentException("The content attribute is required and must refer" +
					" to a different child");
		}

		mClosedLimit = a.getDimensionPixelSize(R.styleable.SlidingPanel_closedLimit, 0);

		a.recycle();

		mAnchorId = anchorId;
		mContentId = contentId;

	}

	@Override
	protected void onFinishInflate() {
		mAnchor = findViewById(mAnchorId);
		if (mAnchor == null) {
			throw new IllegalArgumentException("The anchor attribute must refer to a child");
		}
		
		mContent = findViewById(mContentId);
		if (mContent == null) {
			throw new IllegalArgumentException("The content attribute must refer to a child");
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = View.resolveSize(0, widthMeasureSpec);
		int height = View.resolveSize(0, heightMeasureSpec);
		
		final View anchor = mAnchor;
		measureChild(anchor, widthMeasureSpec, heightMeasureSpec);

		final View content = mContent;
		int contentWidth = width - mClosedLimit;
		content.measure(MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
		
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final View anchor = mAnchor;
		anchor.layout(0, 0, anchor.getMeasuredWidth(),
				getMeasuredHeight());
		
		final View content = mContent;
		final int contentLeft;
		if (mAnimating) {
			contentLeft = content.getLeft();
		} else if (mExpanded) {
			contentLeft = mClosedLimit;
		} else {
			contentLeft = anchor.getRight();
		}
		content.layout(contentLeft, 0,
				contentLeft + content.getMeasuredWidth(),
				content.getMeasuredHeight());
	}
	
	public void open() {
		if (mExpanded) return;
		toggle();
	}

	public void close() {
		if (!mExpanded) return;
		toggle();
	}
	
	public boolean isOpened() {
		return mExpanded;
	}
	
	public void toggle() {
		if (mAnimating) return;
		final View content = mContent;
		
		final View anchor = mAnchor;
		final int offset = anchor.getMeasuredWidth() - mClosedLimit;
		TranslateAnimation anim;
		if (mExpanded) {
			anim = new TranslateAnimation(0, offset, 0, 0);
			mFillOffset = offset;
		} else {
			anim = new TranslateAnimation(0, -offset, 0, 0);
			mFillOffset = -offset;
		}
		mExpanded = !mExpanded;
		anim.setFillEnabled(true);
		anim.setFillBefore(true);
		anim.setDuration(200);
		anim.setInterpolator(new AccelerateDecelerateInterpolator());
		anim.setAnimationListener(new AnimationFiller());
		content.startAnimation(anim);
	}
	
	private class AnimationFiller implements AnimationListener {

		@Override
		public void onAnimationEnd(Animation animation) {
			mContent.offsetLeftAndRight(mFillOffset);
			mAnimating = false;
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
			mAnimating = true;
		}
		
	}
}
