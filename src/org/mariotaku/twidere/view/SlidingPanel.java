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
import android.widget.Button;

public class SlidingPanel extends ViewGroup {
//	private static final String TAG = "Su.SlidingPanel";

	private final int mButtonId;
	private View mButton;
	private final int mAnchorId;
	private View mAnchor;
	private final int mContentId;
	private View mContent;

	private final int mOpenOverlap;
	private final int mClosedLimit;

	private boolean mAnimating = false;
	private int mFillOffset = 0;

	private boolean mExpanded = true;

	private Toggler mToggler;

	public SlidingPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidingPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingPanel,
				defStyle, 0);

		final int buttonId = a.getResourceId(R.styleable.SlidingPanel_button, 0);
		if (buttonId == 0) {
			throw new IllegalArgumentException("The button attribute is required and must refer" +
					" to a valid child");
		}

		final int anchorId = a.getResourceId(R.styleable.SlidingPanel_anchor, 0);
		if (anchorId == buttonId) {
			throw new IllegalArgumentException("The anchor attribute is required and must refer" +
					" to a different child");
		}

		final int contentId = a.getResourceId(R.styleable.SlidingPanel_content, 0);
		if (contentId == anchorId || contentId == buttonId) {
			throw new IllegalArgumentException("The content attribute is required and must refer" +
					" to a different child");
		}

		mOpenOverlap = a.getDimensionPixelSize(R.styleable.SlidingPanel_openOverlap, 0);
		mClosedLimit = a.getDimensionPixelSize(R.styleable.SlidingPanel_closedLimit, 0);

		a.recycle();

		mButtonId = buttonId;
		mAnchorId = anchorId;
		mContentId = contentId;

		mToggler = new Toggler();
	}

	@Override
	protected void onFinishInflate() {
		mButton = findViewById(mButtonId);
		((Button)mButton).setText(">");
		if (mButton == null) {
			throw new IllegalArgumentException("The handle attribute must refer to a child");
		}
		
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
		
		final View button = mButton;
		measureChild(button, MeasureSpec.makeMeasureSpec(anchor.getMeasuredWidth(),
				MeasureSpec.EXACTLY),
				heightMeasureSpec);
		button.setOnClickListener(mToggler);
		
		final View content = mContent;
		int contentWidth = width - mClosedLimit;
		content.measure(MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
		
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final View button = mButton;
		button.layout(0, 0, button.getMeasuredWidth(), button.getMeasuredHeight());
		
		final View anchor = mAnchor;
		anchor.layout(0, button.getMeasuredHeight(), anchor.getMeasuredWidth(),
				button.getMeasuredHeight() + anchor.getMeasuredHeight());
		
		final View content = mContent;
		int contentLeft;
		if (mAnimating) {
			contentLeft = content.getLeft();
		} else if (mExpanded) {
			contentLeft = mClosedLimit;
		} else {
			contentLeft = anchor.getRight() - mOpenOverlap;
		}
		content.layout(contentLeft, 0,
				contentLeft + content.getMeasuredWidth(),
				content.getMeasuredHeight());
	}

	public void toggle() {
		final View content = mContent;
		
		final View anchor = mAnchor;
		final int offset = anchor.getMeasuredWidth() - mOpenOverlap - mClosedLimit;
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
		anim.setDuration(300);
		anim.setInterpolator(new AccelerateDecelerateInterpolator());
		anim.setAnimationListener(new AnimationFiller());
		content.startAnimation(anim);
	}
	
	private class Toggler implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (!mAnimating) {
				toggle();
			}
		}
	}
	
	private class AnimationFiller implements AnimationListener {

		@Override
		public void onAnimationEnd(Animation animation) {
			mAnimating = false;
			final View content = mContent;
			content.offsetLeftAndRight(mFillOffset);
			final Button button = (Button) mButton;
			if (mExpanded) {
				button.setText(">");
			} else {
				button.setText("<");
			}
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
