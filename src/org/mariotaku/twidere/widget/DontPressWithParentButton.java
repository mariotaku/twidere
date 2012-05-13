/*
 * Copyright (C) 2009 The Android Open Source Project
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

package org.mariotaku.twidere.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;

/**
 * Special class to to allow the parent to be pressed without being pressed
 * itself. This way the time in the list can be pressed without changing the
 * background of the indicator.
 */
public class DontPressWithParentButton extends Button {

	public DontPressWithParentButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setPressed(boolean pressed) {
		// If the parent is pressed, do not set to pressed.
		ViewParent parent = getParent();
		if (pressed) {
			if (parent instanceof View) {
				if (((View) parent).isPressed()) return;
			}
		}
		super.setPressed(pressed);
	}
}
