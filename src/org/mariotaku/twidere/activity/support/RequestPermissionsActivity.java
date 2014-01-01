/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity.support;

import static android.text.TextUtils.isEmpty;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.PermissionsManager;

public class RequestPermissionsActivity extends BaseSupportDialogActivity implements OnClickListener {

	private PermissionsManager mPermissionsManager;

	private ImageView mIconView;
	private TextView mNameView, mDescriptionView, mMessageView;
	private Button mAcceptButton, mDenyButton;

	private int mPermissions;
	private String mCallingPackage;

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.accept: {
				mPermissionsManager.accept(mCallingPackage, mPermissions);
				setResult(RESULT_OK);
				finish();
				break;
			}
			case R.id.deny: {
				setResult(RESULT_CANCELED);
				finish();
				break;
			}
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mIconView = (ImageView) findViewById(android.R.id.icon);
		mNameView = (TextView) findViewById(android.R.id.text1);
		mDescriptionView = (TextView) findViewById(android.R.id.text2);
		mMessageView = (TextView) findViewById(R.id.message);
		mAcceptButton = (Button) findViewById(R.id.accept);
		mDenyButton = (Button) findViewById(R.id.deny);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		mPermissionsManager = new PermissionsManager(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.request_permissions);
		mAcceptButton.setOnClickListener(this);
		mDenyButton.setOnClickListener(this);
		final String caller = getCallingPackage();
		if (caller == null) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		loadInfo(caller);
	}

	private void loadInfo(final String pname) {
		final PackageManager pm = getPackageManager();
		try {
			final ApplicationInfo info = pm.getApplicationInfo(pname, PackageManager.GET_META_DATA);
			final Bundle meta = info.metaData;
			if (meta == null || !meta.getBoolean(METADATA_KEY_EXTENSION)) {
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
			mIconView.setImageDrawable(info.loadIcon(pm));
			mNameView.setText(info.loadLabel(pm));
			final CharSequence desc = info.loadDescription(pm);
			mDescriptionView.setText(desc);
			mDescriptionView.setVisibility(isEmpty(desc) ? View.GONE : View.VISIBLE);
			final int permissions = mPermissions = meta.getInt(METADATA_KEY_PERMISSIONS);
			mCallingPackage = pname;
			final StringBuilder builder = new StringBuilder();
			builder.append(getString(R.string.permissions_request_message) + "<br/>");
			if (permissions == 0) {
				builder.append("<br/>" + getString(R.string.permission_description_none));
			} else {
				if (permissions % PERMISSION_PREFERENCES == 0) {
					builder.append("<br/>" + "<b><font color='#FF8000'>"
							+ getString(R.string.permission_description_preferences) + "</font></b>");
				}
				if (permissions % PERMISSION_ACCOUNTS == 0) {
					builder.append("<br/>" + "<b><font color='#FF8000'>"
							+ getString(R.string.permission_description_accounts) + "</font></b>");
				}
				if (permissions % PERMISSION_DIRECT_MESSAGES == 0) {
					builder.append("<br/>" + "<b><font color='#FF8000'>"
							+ getString(R.string.permission_description_direct_messages) + "</font></b>");
				}
				if (permissions % PERMISSION_WRITE == 0) {
					builder.append("<br/>" + "<b>" + getString(R.string.permission_description_write) + "</b>");
				}
				if (permissions % PERMISSION_READ == 0) {
					builder.append("<br/>" + getString(R.string.permission_description_read));
				}
				if (permissions % PERMISSION_REFRESH == 0) {
					builder.append("<br/>" + getString(R.string.permission_description_refresh));
				}
			}
			mMessageView.setText(Html.fromHtml(builder.toString()));
		} catch (final NameNotFoundException e) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
	}

}
