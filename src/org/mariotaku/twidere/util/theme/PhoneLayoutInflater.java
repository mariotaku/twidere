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

package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

public class PhoneLayoutInflater extends LayoutInflater {
	private static final String[] sClassPrefixList = { "android.widget.", "android.webkit." };

	/**
	 * Instead of instantiating directly, you should retrieve an instance
	 * through {@link Context#getSystemService}
	 * 
	 * @param context The Context in which in which to find resources and other
	 *            application-specific things.
	 * 
	 * @see Context#getSystemService
	 */
	public PhoneLayoutInflater(final Context context) {
		super(context);
	}

	protected PhoneLayoutInflater(final LayoutInflater original, final Context newContext) {
		super(original, newContext);
	}

	@Override
	public LayoutInflater cloneInContext(final Context newContext) {
		return new PhoneLayoutInflater(this, newContext);
	}

	/**
	 * Override onCreateView to instantiate names that correspond to the widgets
	 * known to the Widget factory. If we don't find a match, call through to
	 * our super class.
	 */
	@Override
	protected View onCreateView(final String name, final AttributeSet attrs) throws ClassNotFoundException {
		for (final String prefix : sClassPrefixList) {
			try {
				final View view = createView(name, prefix, attrs);
				if (view != null) return view;
			} catch (final ClassNotFoundException e) {
				// In this case we want to let the base class take a crack
				// at it.
			}
		}

		return super.onCreateView(name, attrs);
	}
}
