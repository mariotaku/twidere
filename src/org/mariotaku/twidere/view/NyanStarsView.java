
package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import org.mariotaku.twidere.R;

import java.util.ArrayList;
import java.util.Random;

public class NyanStarsView extends View {

    private final int mStarRows, mStarCols, mStarDotSize;

    private final Paint mPaint;
    private final ArrayList<Star> mStars = new ArrayList<Star>();
    private final Random mRandom = new Random();
    private final InvalidateRunnable mInvalidateRunnable;

    public NyanStarsView(final Context context) {
        this(context, null);
    }

    public NyanStarsView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NyanStarsView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_HARDWARE, null);
        final Resources res = getResources();
        mStarRows = res.getInteger(R.integer.nyan_star_rows);
        mStarCols = res.getInteger(R.integer.nyan_star_cols);
        mStarDotSize = res.getDimensionPixelSize(R.dimen.nyan_star_dot_size);
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mInvalidateRunnable = new InvalidateRunnable(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(mInvalidateRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(mInvalidateRunnable);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        final int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0)
            return;
        for (final Star star : mStars) {
            final int col = star.nextColumn(), row = star.nextRow();
            final float y = (row + 0.5f) * (h / mStarRows), x = (col + 0.5f) * (w / mStarCols);
            drawStar(canvas, x, y, star.nextFrame());
        }
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mStars.clear();
        if (w <= 0 || h <= 0)
            return;
        for (int i = 0; i < mStarRows; i++) {
            final int frame = mRandom.nextInt(7);
            final int col = mRandom.nextInt(mStarCols);
            final Star star;
            if (mRandom.nextBoolean()) {
                star = new Star1(frame, col, i, mStarCols, mStarRows);
            } else {
                star = new Star2(frame, col, i, mStarCols, mStarRows);
            }
            mStars.add(star);
        }
    }

    private void drawStar(final Canvas canvas, final float x, final float y, final byte[][] frame) {
        final int rows = frame.length;
        for (int row = 0; row < rows; row++) {
            final int cols = frame[row].length;
            final float top = y + mStarDotSize * row - mStarDotSize * rows / 2;
            for (int col = 0; col < cols; col++) {
                final byte point = frame[row][col];
                if (point != 0) {
                    final float left = x + mStarDotSize * col - mStarDotSize * cols / 2;
                    canvas.drawRect(left, top, left + mStarDotSize, top + mStarDotSize, mPaint);
                }
            }
        }
    }

    private static final class InvalidateRunnable implements Runnable {

        private final View mView;

        InvalidateRunnable(final View view) {
            mView = view;
        }

        @Override
        public void run() {
            mView.invalidate();
            mView.postDelayed(this, 70);
        }

    }

    private static abstract class Star implements StarAnimFrames {

        private final int mMaxColumn, mMaxRow;
        private int mCurrentFrame, mCurrentColumn, mCurrentRow;

        Star(final int initialFrame, final int initialColumn, final int initialRow,
                final int maxColumn,
                final int maxRow) {
            setFrame(initialFrame);
            mMaxColumn = maxColumn;
            mMaxRow = maxRow;
            setColumn(initialColumn);
            setRow(initialRow);
        }

        public abstract byte[][][] getFrames();

        public final int length() {
            return getFrames().length;
        }

        public final int nextColumn() {
            final int column = mCurrentColumn;
            mCurrentColumn--;
            if (mCurrentColumn < 0) {
                mCurrentColumn = mMaxColumn - 1;
            }
            return column;
        }

        public final byte[][] nextFrame() {
            final byte[][] frame = getFrames()[mCurrentFrame];
            mCurrentFrame++;
            if (mCurrentFrame >= length()) {
                mCurrentFrame = 0;
            }
            return frame;
        }

        public final int nextRow() {
            return mCurrentRow;
        }

        public void setColumn(final int column) {
            if (column < 0 || column >= mMaxColumn) {
                mCurrentColumn = 0;
            } else {
                mCurrentColumn = column;
            }
        }

        public void setFrame(final int frame) {
            if (frame < 0 || frame >= length()) {
                mCurrentFrame = 0;
            } else {
                mCurrentFrame = frame;
            }
        }

        public void setRow(final int row) {
            if (row < 0 || row >= mMaxRow) {
                mCurrentRow = 0;
            } else {
                mCurrentRow = row;
            }
        }
    }

    private static final class Star1 extends Star {

        private static final byte[][][] FRAMES = new byte[][][] {
                FRAME1, FRAME2, FRAME3, FRAME4, FRAME5, FRAME6
        };

        public Star1(final int initialFrame, final int initialColumn, final int initialRow,
                final int maxColumn,
                final int maxRow) {
            super(initialFrame, initialColumn, initialRow, maxColumn, maxRow);
        }

        @Override
        public byte[][][] getFrames() {
            return FRAMES;
        }
    }

    private static final class Star2 extends Star {
        private static final byte[][][] FRAMES = new byte[][][] {
                FRAME1, FRAME6, FRAME5, FRAME4, FRAME3, FRAME2
        };

        public Star2(final int initialFrame, final int initialColumn, final int initialRow,
                final int maxColumn,
                final int maxRow) {
            super(initialFrame, initialColumn, initialRow, maxColumn, maxRow);
        }

        @Override
        public byte[][][] getFrames() {
            return FRAMES;
        }
    }

    private static interface StarAnimFrames {
        /*
         * @formatter:off
         */
        static final byte[][] FRAME1 = {
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        0, 0, 0, 1, 0, 0, 0
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                }
        };
        static final byte[][] FRAME2 = {
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        0, 0, 0, 1, 0, 0, 0
                },
                {
                        0, 0, 1, 0, 1, 0, 0
                },
                {
                        0, 0, 0, 1, 0, 0, 0
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                }
        };
        static final byte[][] FRAME3 = {
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        0, 0, 0, 1, 0, 0, 0
                },
                {
                        0, 0, 0, 1, 0, 0, 0
                },
                {
                        0, 1, 1, 0, 1, 1, 0
                },
                {
                        0, 0, 0, 1, 0, 0, 0
                },
                {
                        0, 0, 0, 1, 0, 0, 0
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                }
        };
        static final byte[][] FRAME4 = {
                {
                        0, 0, 0, 1, 0, 0, 0
                },
                {
                        0, 0, 0, 1, 0, 0, 0
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        1, 1, 0, 1, 0, 1, 1
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        0, 0, 0, 1, 0, 0, 0
                },
                {
                        0, 0, 0, 1, 0, 0, 0
                }
        };
        static final byte[][] FRAME5 = {
                {
                        0, 0, 0, 1, 0, 0, 0
                },
                {
                        0, 1, 0, 0, 0, 1, 0
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        1, 0, 0, 0, 0, 0, 1
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        0, 1, 0, 0, 0, 1, 0
                },
                {
                        0, 0, 0, 1, 0, 0, 0
                }
        };
        static final byte[][] FRAME6 = {
                {
                        0, 0, 0, 1, 0, 0, 0
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        1, 0, 0, 0, 0, 0, 1
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        0, 0, 0, 0, 0, 0, 0
                },
                {
                        0, 0, 0, 1, 0, 0, 0
                }
        };

        /*
         * @formatter:on
         */
    }
}
