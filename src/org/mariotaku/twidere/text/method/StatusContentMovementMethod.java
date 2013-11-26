/*
 * Copyright (C) 2006 The Android Open Source Project
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

package org.mariotaku.twidere.text.method;

import android.text.Layout;
import android.text.NoCopySpan;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * A movement method that traverses links in the text buffer and scrolls if
 * necessary. Supports clicking on links with DPad Center or Enter.
 */
public class StatusContentMovementMethod extends ArrowKeyMovementMethod {

	private static StatusContentMovementMethod sInstance;

	private static Object FROM_BELOW = new NoCopySpan.Concrete();

	@Override
	public void initialize(final TextView widget, final Spannable text) {
		Selection.removeSelection(text);
		text.removeSpan(FROM_BELOW);
	}

	@Override
	public void onTakeFocus(final TextView view, final Spannable text, final int dir) {
		Selection.removeSelection(text);

		if ((dir & View.FOCUS_BACKWARD) != 0) {
			text.setSpan(FROM_BELOW, 0, 0, Spannable.SPAN_POINT_POINT);
		} else {
			text.removeSpan(FROM_BELOW);
		}
	}

	@Override
	public boolean onTouchEvent(final TextView widget, final Spannable buffer, final MotionEvent event) {
		final int action = event.getAction();

		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			x -= widget.getTotalPaddingLeft();
			y -= widget.getTotalPaddingTop();

			x += widget.getScrollX();
			y += widget.getScrollY();

			final Layout layout = widget.getLayout();
			final int line = layout.getLineForVertical(y);
			final int off = layout.getOffsetForHorizontal(line, x);

			final ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

			if (link.length != 0) {
				if (action == MotionEvent.ACTION_UP) {
					link[0].onClick(widget);
				} else if (action == MotionEvent.ACTION_DOWN) {
					Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
				}

				return true;
			} else {
				Selection.removeSelection(buffer);
			}
		}

		return super.onTouchEvent(widget, buffer, event);
	}

	public static MovementMethod getInstance() {
		if (sInstance == null) {
			sInstance = new StatusContentMovementMethod();
		}

		return sInstance;
	}
}
