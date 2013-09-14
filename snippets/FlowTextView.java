package org.mariotaku.twidere.view;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class FlowTextView extends RelativeLayout {

	private int mColor = Color.BLACK;

	private int pageHeight = 0;

	private TextPaint mTextPaint;
	private TextPaint mLinkPaint;
	private int mTextSize = 20;

	private Typeface typeFace;
	private int mDesiredHeight = 100; // height of the whole view

	private final float mSpacingMult = 1.0f;

	private final float mSpacingAdd = 0.0f;

	private float mViewWidth;
	private final ArrayList<Box> mLineboxes = new ArrayList<FlowTextView.Box>();

	private final ArrayList<Area> mAreas = new ArrayList<FlowTextView.Area>();

	Area mLargestArea;
	boolean needsMeasure = true;

	private final ArrayList<Box> boxes = new ArrayList<FlowTextView.Box>();

	private CharSequence mText = "";

	private boolean mIsHtml = false;
	// private URLSpan[] urls;

	private boolean[] charFlags;

	int charFlagSize = 0;
	int charFlagIndex = 0;

	int spanStart = 0;

	int spanEnd = 0;

	int charCounter;

	float objPixelwidth;

	SparseArray<HtmlObject> sorterMap = new SparseArray<HtmlObject>();

	float tempFloat;

	int[] sorterKeys, sortedKeys;

	String tempString;

	int temp1;

	int temp2;

	int arrayIndex = 0;

	private final ArrayList<TextPaint> mPaintHeap = new ArrayList<TextPaint>();

	private final ArrayList<HtmlLink> mLinks = new ArrayList<FlowTextView.HtmlLink>();

	private Spannable mSpannable;

	int mTextLength = 0;

	private ArrayList<BitmapSpec> bitmaps = new ArrayList<FlowTextView.BitmapSpec>();

	double mDistance = 0;

	float mTouchX1, mTouchY1, mTouchX2, mTouchY2;

	public FlowTextView(final Context context) {
		super(context);
		init(context, null);
	}

	public FlowTextView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public FlowTextView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public BitmapSpec addImage(final Bitmap bitmap, final int xOffset, final int yOffset, final int padding) {
		final BitmapSpec spec = new BitmapSpec(bitmap, xOffset, yOffset, padding);
		bitmaps.add(spec);
		return spec;
	}

	public ArrayList<BitmapSpec> getBitmaps() {
		return bitmaps;
	}

	public int getLineHeight() {
		return Math.round(mTextPaint.getFontMetricsInt(null) * mSpacingMult + mSpacingAdd);
	}

	public CharSequence getText() {
		return mText;
	}

	@Override
	public void invalidate() {
		needsMeasure = true;
		super.invalidate();
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {

		final int event_code = event.getAction();

		if (event_code == MotionEvent.ACTION_DOWN) {
			mDistance = 0;
			mTouchX1 = event.getX();
			mTouchY1 = event.getY();
		}

		if (event_code == MotionEvent.ACTION_MOVE) {
			mTouchX2 = event.getX();
			mTouchY2 = event.getY();
			mDistance = getPointDistance(mTouchX1, mTouchY1, mTouchX2, mTouchY2);
		}

		if (mDistance < 10) {
			final ClickableSpan span = getClickableSpan(event.getX(), event.getY());
			if (span == null) return false;
			if (event_code == MotionEvent.ACTION_UP) {
				span.onClick(this);
			}
			return true;
		} else
			return false;

	}

	public void setBitmaps(final ArrayList<BitmapSpec> bitmaps) {
		this.bitmaps = bitmaps;
	}

	public void setColor(final int color) {
		mColor = color;

		if (mTextPaint != null) {
			mTextPaint.setColor(mColor);
		}

		for (final TextPaint paint : mPaintHeap) {
			paint.setColor(mColor);
		}

		this.invalidate();
	}

	public void setPageHeight(final int pageHeight) {
		this.pageHeight = pageHeight;
	}

	public void setText(final CharSequence text) {
		mText = text;
		if (text instanceof Spannable) {
			mIsHtml = true;
			mSpannable = (Spannable) text;
		} else {
			mIsHtml = false;
		}
		mTextLength = mText.length();
		invalidate();
	}

	public void setTextSize(final float textSize) {
		mTextSize = sp2px(getContext(), textSize);
		mTextPaint.setTextSize(mTextSize);
		mLinkPaint.setTextSize(mTextSize);
		invalidate();
	}

	public void setTypeface(final Typeface type) {
		typeFace = type;
		mTextPaint.setTypeface(typeFace);
		mLinkPaint.setTypeface(typeFace);
		invalidate();
	}

	@Override
	protected void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		this.invalidate();
	}

	@Override
	protected void onDraw(final Canvas canvas) {

		super.onDraw(canvas);

		mViewWidth = getWidth();
		int lowestYCoord = 0;
		boxes.clear();

		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final Box box = new Box();
				box.topLeftx = child.getLeft();
				box.topLefty = child.getTop();
				box.bottomRightx = box.topLeftx + child.getWidth();
				box.bottomRighty = box.topLefty + child.getHeight();
				boxes.add(box);
				if (box.bottomRighty > lowestYCoord) {
					lowestYCoord = box.bottomRighty;
				}
			}
		}

		final String[] blocks = mText.toString().split("\n");

		int charOffsetStart = 0; // tells us where we are in the original string
		int charOffsetEnd = 0; // tells us where we are in the original string
		int lineIndex = 0;
		float xOffset = 0; // left margin off a given line
		float maxWidth = mViewWidth; // how far to the right it can strectch
		float yOffset = 0;
		String thisLineStr;
		int chunkSize;
		final int lineHeight = getLineHeight();

		final ArrayList<HtmlObject> lineObjects = new ArrayList<HtmlObject>();
		Object[] spans = new Object[0];

		HtmlObject htmlLine;// = new HtmlObject(); // reuse for single plain
							// lines

		mLinks.clear();

		for (int block_no = 0; block_no <= blocks.length - 1; block_no++) {

			String thisBlock = blocks[block_no];
			if (thisBlock.length() <= 0) {
				lineIndex++; // is a line break
				charOffsetEnd += 2;
				charOffsetStart = charOffsetEnd;
			} else {

				while (thisBlock.length() > 0) {
					lineIndex++;
					yOffset = lineIndex * lineHeight;
					final Line thisLine = getLine(yOffset, lineHeight);
					xOffset = thisLine.leftBound;
					maxWidth = thisLine.rightBound - thisLine.leftBound;
					float actualWidth = 0;

					do {
						chunkSize = getChunk(thisBlock, maxWidth);
						final int thisCharOffset = charOffsetEnd + chunkSize;

						if (chunkSize > 1) {
							thisLineStr = thisBlock.substring(0, chunkSize);
						} else {
							thisLineStr = "";
						}

						lineObjects.clear();

						if (mIsHtml) {
							spans = ((Spanned) mText).getSpans(charOffsetStart, thisCharOffset, Object.class);
							if (spans.length > 0) {
								actualWidth = parseSpans(lineObjects, spans, charOffsetStart, thisCharOffset, xOffset);
							} else {
								actualWidth = maxWidth; // if no spans then the
														// actual width will be
														// <= maxwidth anyway
							}
						} else {
							actualWidth = maxWidth;// if not html then the
													// actual width will be <=
													// maxwidth anyway
						}

						if (actualWidth > maxWidth) {
							maxWidth -= 5; // if we end up looping - start
											// slicing chars off till we get a
											// suitable size
						}

					} while (actualWidth > maxWidth);

					// chunk is ok
					charOffsetEnd += chunkSize;

					if (lineObjects.isEmpty()) {
						// no funky objects found, add the whole chunk as one
						// object
						htmlLine = new HtmlObject(thisLineStr, 0, 0, xOffset, mTextPaint);
						lineObjects.add(htmlLine);
					}

					for (final HtmlObject thisHtmlObject : lineObjects) {

						if (thisHtmlObject instanceof HtmlLink) {
							final HtmlLink thisLink = (HtmlLink) thisHtmlObject;
							final float thisLinkWidth = thisLink.paint.measureText(thisHtmlObject.content);
							addLink(thisLink, yOffset, thisLinkWidth, lineHeight);
						}

						paintObject(canvas, thisHtmlObject.content, thisHtmlObject.xOffset, yOffset,
								thisHtmlObject.paint);

						if (thisHtmlObject.recycle) {
							recyclePaint(thisHtmlObject.paint);
						}
					}

					if (chunkSize >= 1) {
						thisBlock = thisBlock.substring(chunkSize, thisBlock.length());
					}

					charOffsetStart = charOffsetEnd;
				}
			}
		}

		yOffset += lineHeight / 2;

		final View child = getChildAt(getChildCount() - 1);
		if (child != null && child.getTag() != null) {
			if (child.getTag().toString().equalsIgnoreCase("hideable")) {
				if (yOffset > pageHeight) {
					if (yOffset < boxes.get(boxes.size() - 1).topLefty - getLineHeight()) {
						child.setVisibility(View.GONE);
						// lowestYCoord = (int) yOffset;
					} else {
						// lowestYCoord = boxes.get(boxes.size()-1).bottomRighty
						// + getLineHeight();
						child.setVisibility(View.VISIBLE);
					}
				} else {
					child.setVisibility(View.GONE);
					// lowestYCoord = (int) yOffset;
				}
			}
		}

		mDesiredHeight = Math.max(lowestYCoord, (int) yOffset);
		if (needsMeasure) {
			needsMeasure = false;
			requestLayout();
		}
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width = 0;
		int height = 0;

		if (widthMode == MeasureSpec.EXACTLY) {
			// Parent has told us how big to be. So be it.
			width = widthSize;
		} else {
			width = getWidth();
		}

		if (heightMode == MeasureSpec.EXACTLY) {
			// Parent has told us how big to be. So be it.
			height = heightSize;
		} else {
			height = mDesiredHeight;
		}

		// setMeasuredDimension(width, height + getLineHeight());
		setMeasuredDimension(width, height);

		// setMeasuredDimension(800, 1400);
	}

	private void addLink(final HtmlLink thisLink, final float yOffset, final float width, final float height) {
		thisLink.yOffset = yOffset - 20;
		;
		thisLink.width = width;
		thisLink.height = height + 20;
		mLinks.add(thisLink);

	}

	private String extractText(int start, int end) {
		if (start < 0) {
			start = 0;
		}
		if (end > mTextLength - 1) {
			end = mTextLength - 1;
		}
		return mSpannable.subSequence(start, end).toString();
	}

	private int getChunk(final String text, final float maxWidth) {
		final int length = mTextPaint.breakText(text, true, maxWidth, null);
		if (length <= 0)
			return length; // if its 0 or less, return it, can't fit any chars
							// on this line
		else if (length >= text.length())
			return length; // we can fit the whole string in
		else if (text.charAt(length - 1) == ' ')
			return length; // if break char is a space -- return
		else {
			if (text.length() > length) if (text.charAt(length) == ' ') return length + 1; // or
																							// if
																							// the
																							// following
																							// char
																							// is
																							// a
																							// space
																							// then
																							// return
																							// this
																							// length
																							// -
																							// it
																							// is
																							// fine
		}

		// otherwise, count back until we hit a space and return that as the
		// break length
		int tempLength = length - 1;
		while (text.charAt(tempLength) != ' ') {

			// char test = text.charAt(tempLength);
			tempLength--;
			// if we count all the way back to 0 then this line cannot be
			// broken, just return the original break length

			if (tempLength <= 0) return length;
		}

		// char test = text.charAt(tempLength);
		// return the nicer break length which doesn't split a word up
		return tempLength + 1;

	}

	private ClickableSpan getClickableSpan(final float x, final float y) {
		for (final HtmlLink link : mLinks) {
			final float tlX = link.xOffset;
			final float tlY = link.yOffset;
			final float brX = link.xOffset + link.width;
			final float brY = link.yOffset + link.height;

			if (x > tlX && x < brX) {
				if (y > tlY && y < brY) // collision
					return link.span;
			}
		}
		return null;
	}

	private HtmlLink getHtmlLink(final ClickableSpan span, final String content, final int start, final int end,
			final float thisXOffset) {
		final HtmlLink obj = new HtmlLink(content, start, end, thisXOffset, mLinkPaint, span);
		mLinks.add(obj);
		return obj;
	}

	private HtmlObject getHtmlObject(final String content, final int start, final int end, final float thisXOffset) {
		final HtmlObject obj = new HtmlObject(content, start, end, thisXOffset, mTextPaint);
		return obj;
	}

	private Line getLine(final float lineYbottom, final int lineHeight) {

		final Line line = new Line();
		line.leftBound = 0;
		line.rightBound = mViewWidth;

		final float lineYtop = lineYbottom - lineHeight;

		mAreas.clear();
		mLineboxes.clear();

		for (final Box box : boxes) {

			if (box.topLefty > lineYbottom || box.bottomRighty < lineYtop) {

			} else {

				final Area leftArea = new Area();
				leftArea.x1 = 0;

				for (final Box innerBox : boxes) {
					if (innerBox.topLefty > lineYbottom || innerBox.bottomRighty < lineYtop) {

					} else {
						if (innerBox.topLeftx < box.topLeftx) {
							leftArea.x1 = innerBox.bottomRightx;
						}
					}
				}

				leftArea.x2 = box.topLeftx;
				leftArea.width = leftArea.x2 - leftArea.x1;

				final Area rightArea = new Area();
				rightArea.x1 = box.bottomRightx;
				rightArea.x2 = mViewWidth;

				for (final Box innerBox : boxes) {
					if (innerBox.topLefty > lineYbottom || innerBox.bottomRighty < lineYtop) {

					} else {
						if (innerBox.bottomRightx > box.bottomRightx) {
							rightArea.x2 = innerBox.topLeftx;
						}
					}
				}

				rightArea.width = rightArea.x2 - rightArea.x1;

				mAreas.add(leftArea);
				mAreas.add(rightArea);
			}
		}
		mLargestArea = null;

		if (mAreas.size() > 0) { // if there is no areas then the whole line is
									// clear, if there is areas, return the
									// largest (it means there is one or more
									// boxes colliding with this line)
			for (final Area area : mAreas) {
				if (mLargestArea == null) {
					mLargestArea = area;
				} else {
					if (area.width > mLargestArea.width) {
						mLargestArea = area;
					}
				}
			}

			line.leftBound = mLargestArea.x1;
			line.rightBound = mLargestArea.x2;
		}

		return line;
	}

	private TextPaint getPaintFromHeap() {
		if (mPaintHeap.size() > 0)
			return mPaintHeap.remove(0);
		else
			return new TextPaint(Paint.ANTI_ALIAS_FLAG);
	}

	private HtmlObject getStyledObject(final StyleSpan span, final String content, final int start, final int end,
			final float thisXOffset) {
		final TextPaint paint = getPaintFromHeap();
		paint.setTypeface(Typeface.defaultFromStyle(span.getStyle()));
		paint.setTextSize(mTextSize);
		paint.setColor(mColor);

		span.updateDrawState(paint);
		span.updateMeasureState(paint);
		final HtmlObject obj = new HtmlObject(content, start, end, thisXOffset, paint);
		obj.recycle = true;
		return obj;
	}

	private void init(final Context context, final AttributeSet attrs) {
		final TypedArray a = context.obtainStyledAttributes(attrs, styleable.TextViewStyle,
				android.R.attr.textViewStyle, 0);
		mTextSize = a.getDimensionPixelSize(styleable.TextViewStyle_textSize, 14);
		mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.density = getResources().getDisplayMetrics().density;
		mTextPaint.setTextSize(mTextSize);
		mTextPaint.setColor(a.getColor(styleable.TextViewStyle_textColor, Color.BLACK));

		mLinkPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mLinkPaint.density = getResources().getDisplayMetrics().density;
		mLinkPaint.setTextSize(mTextSize);
		mLinkPaint.setColor(a.getColor(styleable.TextViewStyle_textColorLink, Color.BLACK));
		mLinkPaint.setUnderlineText(true);
		a.recycle();
		setWillNotDraw(false);
	}

	private boolean isArrayFull(final boolean[] array) {
		for (arrayIndex = 0; arrayIndex < array.length; arrayIndex++) {
			if (array[arrayIndex] == false) return false;
		}
		return true;
	}

	private void paintObject(final Canvas canvas, final String thisLineStr, final float xOffset, final float yOffset,
			final Paint paint) {
		canvas.drawText(thisLineStr, xOffset, yOffset, paint);
	}

	private HtmlObject parseSpan(final Object span, final String content, final int start, final int end) {

		if (span instanceof ClickableSpan)
			return getHtmlLink((ClickableSpan) span, content, start, end, 0);
		else if (span instanceof StyleSpan)
			return getStyledObject((StyleSpan) span, content, start, end, 0);
		else
			return getHtmlObject(content, start, end, 0);
	}

	private float parseSpans(final ArrayList<HtmlObject> objects, final Object[] spans, final int lineStart,
			final int lineEnd, final float baseXOffset) {

		sorterMap.clear();

		charFlagSize = lineEnd - lineStart;
		charFlags = new boolean[charFlagSize];

		for (final Object span : spans) {
			spanStart = mSpannable.getSpanStart(span);
			spanEnd = mSpannable.getSpanEnd(span);

			if (spanStart < lineStart) {
				spanStart = lineStart;
			}
			if (spanEnd > lineEnd) {
				spanEnd = lineEnd;
			}

			for (charCounter = spanStart; charCounter < spanEnd; charCounter++) { // mark
																					// these
																					// characters
																					// as
																					// rendered
				charFlagIndex = charCounter - lineStart;
				charFlags[charFlagIndex] = true;
			}

			tempString = extractText(spanStart, spanEnd);
			sorterMap.put(spanStart, parseSpan(span, tempString, spanStart, spanEnd));
			// objects.add();
		}

		charCounter = 0;

		while (!isArrayFull(charFlags)) {
			while (true) {

				if (charCounter >= charFlagSize) {
					break;
				}

				if (charFlags[charCounter] == true) {
					charCounter++;
					continue;
				}

				temp1 = charCounter;
				while (true) {
					if (charCounter > charFlagSize) {
						break;
					}

					if (charCounter < charFlagSize) {
						if (charFlags[charCounter] == false) {

							charFlags[charCounter] = true;// mark as filled
							charCounter++;
							continue;

						}
					}
					temp2 = charCounter;
					spanStart = lineStart + temp1;
					spanEnd = lineStart + temp2;
					tempString = extractText(spanStart, spanEnd);
					sorterMap.put(spanStart, parseSpan(null, tempString, spanStart, spanEnd));
					break;

				}
			}
		}

		final int sorterMap_size = sorterMap.size();
		sorterKeys = new int[sorterMap_size];
		for (int i = 0; i < sorterMap_size; i++) {
			sorterKeys[i] = sorterMap.keyAt(i);
		}
		Arrays.sort(sorterKeys);

		float thisXoffset = baseXOffset;

		for (charCounter = 0; charCounter < sorterKeys.length; charCounter++) {
			final HtmlObject thisObj = sorterMap.get(sorterKeys[charCounter]);
			thisObj.xOffset = thisXoffset;
			tempFloat = thisObj.paint.measureText(thisObj.content);
			thisXoffset += tempFloat;
			objects.add(thisObj);
		}

		return thisXoffset - baseXOffset;
	}

	private void recyclePaint(final TextPaint paint) {
		mPaintHeap.add(paint);
	}

	public static int sp2px(final Context context, final float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	private static double getPointDistance(final float x1, final float y1, final float x2, final float y2) {
		final double dist = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
		return dist;

	}

	public class BitmapSpec {

		public Bitmap bitmap;
		public int xOffset;
		public int yOffset;
		public int mPadding = 10;

		public BitmapSpec(final Bitmap bitmap, final int xOffset, final int yOffset, final int mPadding) {
			super();
			this.bitmap = bitmap;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.mPadding = mPadding;
		}
	}

	public interface OnLinkClickListener {
		public void onLinkClick(String url);
	}

	private class Area {
		float x1;
		float x2;
		float width;
	}

	private class Box {
		public int topLeftx;
		public int topLefty;
		public int bottomRightx;
		public int bottomRighty;
	}

	private class Line {
		public float leftBound;
		public float rightBound;
	}

	private static class styleable {
		public static final int[] TextViewStyle = { android.R.attr.textColor, android.R.attr.textColorLink,
				android.R.attr.textSize };
		public static final int TextViewStyle_textColor = 0;
		public static final int TextViewStyle_textColorLink = 1;
		public static final int TextViewStyle_textSize = 2;
	}

	static class HtmlLink extends HtmlObject {
		public float width;
		public float height;
		public float yOffset;
		public ClickableSpan span;

		public HtmlLink(final String content, final int start, final int end, final float xOffset,
				final TextPaint paint, final ClickableSpan span) {
			super(content, start, end, xOffset, paint);
			this.span = span;
		}
	}

	static class HtmlObject {

		public String content;
		public int start;
		public int end;
		public float xOffset;
		public TextPaint paint;
		public boolean recycle = false;

		public HtmlObject(final String content, final int start, final int end, final float xOffset,
				final TextPaint paint) {
			super();
			this.content = content;
			this.start = start;
			this.end = end;
			this.xOffset = xOffset;
			this.paint = paint;
		}
	}

}
