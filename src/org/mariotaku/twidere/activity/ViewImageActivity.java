/*
 *              Copyright (C) 2011 The MusicMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;

public class ViewImageActivity extends WebViewActivity implements Constants {

	private Uri mUri = Uri.parse("about:blank");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUri = getIntent().getData();
		if (mUri == null) {
			Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT);
			finish();
			return;
		}
		loadUrl(mUri.toString());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.image_view, menu);
		return super.onCreateOptionsMenu(menu);
	}
}