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

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.WebViewFragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.widget.Toast;

public class BrowserActivity extends BaseActivity {

	private Uri mUri = Uri.parse("about:blank");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		mUri = getIntent().getData();
		if (mUri == null) {
			Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT);
			finish();
			return;
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment fragment = Fragment.instantiate(this, WebViewFragment.class.getName());
		Bundle bundle = new Bundle();
		bundle.putString(INTENT_KEY_URI, mUri.toString());
		fragment.setArguments(bundle);
		ft.replace(android.R.id.content, fragment);
		ft.commit();
	}
}
