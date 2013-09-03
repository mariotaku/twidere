/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.CUSTOM_TABS_ICON_NAME_MAP;
import static org.mariotaku.twidere.util.Utils.CUSTOM_TABS_TYPE_NAME_MAP;
import static org.mariotaku.twidere.util.Utils.buildArguments;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getTabTypeName;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AutoCompleteAdapter;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.ParseUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class EditCustomTabActivity extends BasePreferenceActivity {

	private String mType, mName, mIcon, mText1, mText2;
	private long mAccountId;

	private Preference mTabTypePreference, mAccountPreference, mNamePreference, mTabIconPreference;
	private Text1Preference mText1Preference;
	private Text2Preference mText2Preference;

	private boolean mBackPressed = false, mHasUnsavedChanges = false;

	private final Handler mBackPressedHandler = new BackPressedHandler(this);;

	private static final int MESSAGE_ID_BACK_TIMEOUT = 0;

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_edit_tab, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK: {
				if (!backPressed()) return true;
				break;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				if (backPressed()) {
					finish();
				}
				break;
			}
			case MENU_SAVE: {
				if (mName == null || mType == null) {
					Toast.makeText(this, R.string.invalid_settings, Toast.LENGTH_SHORT).show();
					return false;
				}
				final Bundle args = new Bundle();
				args.putString(INTENT_KEY_SCREEN_NAME, mText1);
				if (AUTHORITY_LIST_TIMELINE.equals(mType) || AUTHORITY_LIST_MEMBERS.equals(mType)
						|| AUTHORITY_LISTS.equals(mType)) {
					if (isEmpty(mText1) || isEmpty(mText2)) {
						Toast.makeText(this, R.string.invalid_settings, Toast.LENGTH_SHORT).show();
						return false;
					}
					args.putString(INTENT_KEY_LIST_NAME, mText2);
				} else if (AUTHORITY_SEARCH_TWEETS.equals(mType) || AUTHORITY_SEARCH_USERS.equals(mType)) {
					if (isEmpty(mText1)) {
						Toast.makeText(this, R.string.invalid_settings, Toast.LENGTH_SHORT).show();
						return false;
					}
					args.remove(INTENT_KEY_SCREEN_NAME);
					args.putString(INTENT_KEY_QUERY, mText1);
				} else if (AUTHORITY_SAVED_SEARCHES.equals(mType)) {
					args.remove(INTENT_KEY_SCREEN_NAME);
				}
				args.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
				final Intent intent = new Intent();
				final Bundle extras = new Bundle();
				extras.putString(INTENT_KEY_ARGUMENTS, buildArguments(args));
				extras.putString(INTENT_KEY_NAME, mName);
				extras.putString(INTENT_KEY_TYPE, mType);
				extras.putString(INTENT_KEY_ICON, mIcon);
				intent.putExtras(extras);
				setResult(RESULT_OK, intent);
				finish();
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_PICK_FILE: {
				if (resultCode == RESULT_OK) {
					final Uri uri = data != null ? data.getData() : null;
					if (uri != null) {
						mIcon = uri.getPath();
						mTabIconPreference.setSummary(R.string.customize);
						mHasUnsavedChanges = true;
					}
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		final String action = intent.getAction();
		final Bundle extras = intent.getExtras();
		if (!INTENT_ACTION_NEW_CUSTOM_TAB.equals(action) && !INTENT_ACTION_EDIT_CUSTOM_TAB.equals(action)) {
			finish();
			return;
		}
		if (INTENT_ACTION_EDIT_CUSTOM_TAB.equals(action)) {
			setTitle(R.string.edit_tab);
		}
		if (savedInstanceState != null) {
			mName = savedInstanceState.getString(INTENT_KEY_NAME);
			mType = savedInstanceState.getString(INTENT_KEY_TYPE);
			mAccountId = savedInstanceState.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			mText1 = savedInstanceState.getString(INTENT_KEY_TEXT1);
			mText2 = savedInstanceState.getString(INTENT_KEY_TEXT2);
		} else if (extras != null && INTENT_ACTION_EDIT_CUSTOM_TAB.equals(action)) {
			mType = extras.getString(INTENT_KEY_TYPE);
			mName = extras.getString(INTENT_KEY_NAME);
			mIcon = extras.getString(INTENT_KEY_ICON);
			final Bundle args = ParseUtils.parseArguments(extras.getString(INTENT_KEY_ARGUMENTS));
			if (args.containsKey(INTENT_KEY_ACCOUNT_ID)) {
				mAccountId = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			}
			if (args.containsKey(INTENT_KEY_SCREEN_NAME)) {
				mText1 = args.getString(INTENT_KEY_SCREEN_NAME);
			} else if (args.containsKey(INTENT_KEY_QUERY)) {
				mText1 = args.getString(INTENT_KEY_QUERY);
			}
			if (args.containsKey(INTENT_KEY_LIST_NAME)) {
				mText2 = args.getString(INTENT_KEY_LIST_NAME);
			}
		}
		addPreferencesFromResource(R.xml.edit_tab);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mText1Preference = new Text1Preference(this, R.string.unused);
		mText2Preference = new Text2Preference(this, R.string.unused);
		mTabIconPreference = new TabIconPreference(this);
		mNamePreference = new NamePreference(this);
		mAccountPreference = new AccountPreference(this);
		mTabTypePreference = new TabTypePreference(this);
		final PreferenceScreen screen = (PreferenceScreen) findPreference("edit_tab");
		screen.addPreference(mNamePreference);
		screen.addPreference(mTabIconPreference);
		screen.addPreference(mTabTypePreference);
		screen.addPreference(mAccountPreference);
		screen.addPreference(mText1Preference);
		screen.addPreference(mText2Preference);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(INTENT_KEY_NAME, mName);
		outState.putString(INTENT_KEY_TYPE, mType);
		outState.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
		outState.putString(INTENT_KEY_TEXT1, mText1);
		outState.putString(INTENT_KEY_TEXT2, mText2);
	}

	private boolean backPressed() {
		if (!mHasUnsavedChanges) return true;
		mBackPressedHandler.removeMessages(MESSAGE_ID_BACK_TIMEOUT);
		if (!mBackPressed) {
			Toast.makeText(this, R.string.unsaved_change_back_pressed, Toast.LENGTH_SHORT).show();
			mBackPressed = true;
			mBackPressedHandler.sendEmptyMessageDelayed(MESSAGE_ID_BACK_TIMEOUT, 2000L);
			return false;
		}
		mBackPressed = false;
		return true;
	}

	private void setPreferencesByType(final String type) {
		if (type == null || mText1Preference == null || mText2Preference == null) return;
		mAccountPreference.setEnabled(true);
		mAccountPreference.setTitle(R.string.account);
		mText1Preference.setEnabled(true);
		mText1Preference.setTitle(R.string.screen_name);
		mText1Preference.setShouldCompleteUserName(true);
		mText2Preference.setEnabled(false);
		mText2Preference.setTitle(R.string.unused);
		if (AUTHORITY_LIST_TIMELINE.equals(type) || AUTHORITY_LIST_MEMBERS.equals(type) || AUTHORITY_LISTS.equals(type)) {
			mText2Preference.setEnabled(true);
			mText2Preference.setTitle(R.string.list_name);
		} else if (AUTHORITY_SEARCH_TWEETS.equals(type) || AUTHORITY_SEARCH_USERS.equals(type)) {
			mText1Preference.setTitle(R.string.keywords);
			mText1Preference.setShouldCompleteUserName(false);
		} else if (AUTHORITY_SAVED_SEARCHES.equals(type) || AUTHORITY_TRENDS.equals(type)
				|| AUTHORITY_ACTIVITIES_ABOUT_ME.equals(type) || AUTHORITY_ACTIVITIES_BY_FRIENDS.equals(type)) {
			mText1Preference.setEnabled(false);
			mText1Preference.setTitle(R.string.unused);
			mText1Preference.setShouldCompleteUserName(false);
		}
	}

	class AccountPreference extends Preference implements Constants, OnPreferenceClickListener, OnClickListener {

		private AlertDialog mDialog;
		private final Account[] mAccounts;
		private int mSelectedPos = -1;

		public AccountPreference(final Context context) {
			super(context);
			final List<Account> accounts = Account.getAccounts(getContext(), false);
			mAccounts = accounts.toArray(new Account[accounts.size()]);
			setTitle(R.string.account);
			if (mAccountId > 0) {
				setSummary(getAccountScreenName(getContext(), mAccountId));
			}
			setOnPreferenceClickListener(this);
		}

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			mAccountId = mAccounts[which].account_id;
			setSummary(mAccounts[which].screen_name);
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}
			mHasUnsavedChanges = true;
		}

		@Override
		public boolean onPreferenceClick(final Preference preference) {
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}
			final int length = mAccounts.length;
			if (mAccountId > 0) {
				for (int i = 0; i < length; i++) {
					if (mAccounts[i].account_id == mAccountId) {
						mSelectedPos = i;
					}
				}
			}
			final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle(getTitle());
			final String[] screen_names = new String[length];
			for (int i = 0; i < length; i++) {
				screen_names[i] = mAccounts[i].screen_name;
			}
			builder.setSingleChoiceItems(screen_names, mSelectedPos, this);
			mDialog = builder.show();
			return true;
		}
	}

	abstract class AdditionalPreference extends Preference implements Constants, OnPreferenceClickListener,
			OnClickListener {

		private AlertDialog mDialog;

		private AutoCompleteTextView mEditText;

		public AdditionalPreference(final Context context, final int title) {
			super(context);
			setEnabled(false);
			setTitle(title);
			setOnPreferenceClickListener(this);
		}

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					final String text = ParseUtils.parseString(mEditText.getText());
					onTextSet(text);
					setSummary(text);
					break;
				}
				case DialogInterface.BUTTON_NEGATIVE: {
					break;
				}
			}
		}

		@Override
		public boolean onPreferenceClick(final Preference preference) {
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}
			final View view = LayoutInflater.from(getContext()).inflate(R.layout.auto_complete_textview_default_style,
					null);
			mEditText = (AutoCompleteTextView) view.findViewById(R.id.edit_text);
			mEditText.setAdapter(shouldCompleteUserName() ? new AutoCompleteAdapter(getContext()) : null);
			mEditText.setText(getTextToSet());
			final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle(getTitle());
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			mDialog = builder.show();
			return true;
		}

		public abstract void onTextSet(String text);

		public abstract boolean shouldCompleteUserName();

		abstract String getTextToSet();
	}

	static class BackPressedHandler extends Handler {

		private final EditCustomTabActivity mActivity;

		public BackPressedHandler(final EditCustomTabActivity activity) {
			mActivity = activity;
		}

		@Override
		public void handleMessage(final Message msg) {
			mActivity.mBackPressed = false;
		}

	}

	class NamePreference extends Text2Preference {

		public NamePreference(final Context context) {
			super(context, R.string.name);
			setEnabled(true);
			setSummary(mName);
		}

		@Override
		public String getTextToSet() {
			return mName;
		}

		@Override
		public void onTextSet(final String text) {
			mName = text;
			mHasUnsavedChanges = true;
		}

	}

	class TabIconPreference extends Preference implements Constants, OnPreferenceClickListener, OnClickListener {

		private AlertDialog mDialog;
		private final String[] mKeys, mNames;
		private int mSelectedPos = -1;

		public TabIconPreference(final Context context) {
			super(context);
			final Set<String> keys = CUSTOM_TABS_ICON_NAME_MAP.keySet();
			mKeys = keys.toArray(new String[keys.size()]);
			Arrays.sort(mKeys);
			final int length = mKeys.length;
			mNames = new String[length];
			for (int i = 0; i < length; i++) {
				final String key = mKeys[i];
				if (ICON_SPECIAL_TYPE_CUSTOMIZE.equals(key)) {
					mNames[i] = getString(R.string.customize);
				} else {
					mNames[i] = key.substring(0, 1).toUpperCase(Locale.US) + key.substring(1, key.length());
				}
			}
			setTitle(R.string.icon);
			if (mIcon != null) {
				if (mIcon.contains("/")) {
					setSummary(R.string.customize);
				} else {
					final int idx = ArrayUtils.indexOf(mKeys, mIcon);
					if (idx >= 0) {
						setSummary(mNames[idx]);
					}
				}
			}

			setOnPreferenceClickListener(this);
		}

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			mSelectedPos = which;
			final String name = mNames[which];
			final String key = mKeys[which];
			if (ICON_SPECIAL_TYPE_CUSTOMIZE.equals(key)) {
				mIcon = null;
				final Intent intent = new Intent(INTENT_ACTION_PICK_FILE);
				intent.setClass(getContext(), FilePickerActivity.class);
				final Bundle extras = new Bundle();
				extras.putStringArray(INTENT_KEY_FILE_EXTENSIONS, new String[] { "jpg", "png", "bmp", "gif" });
				intent.putExtras(extras);
				startActivityForResult(intent, REQUEST_PICK_FILE);
			} else {
				mIcon = key;
				mHasUnsavedChanges = true;
				setSummary(name);
			}
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}
		}

		@Override
		public boolean onPreferenceClick(final Preference preference) {
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}
			final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle(getTitle());
			builder.setSingleChoiceItems(mNames, mSelectedPos, this);
			mDialog = builder.show();
			return true;
		}
	}

	class TabTypePreference extends Preference implements Constants, OnPreferenceClickListener, OnClickListener {

		private AlertDialog mDialog;
		private final String[] mKeys, mNames;
		private int mSelectedPos = -1;

		public TabTypePreference(final Context context) {
			super(context);
			final Set<String> keys = CUSTOM_TABS_TYPE_NAME_MAP.keySet();
			mKeys = keys.toArray(new String[keys.size()]);
			Arrays.sort(mKeys);
			final int length = mKeys.length;
			mNames = new String[length];
			for (int i = 0; i < length; i++) {
				final String key = mKeys[i];
				mNames[i] = getTabTypeName(getContext(), key);
			}
			setTitle(R.string.tab_type);
			if (mType != null) {
				setSummary(getTabTypeName(getContext(), mType));
				setPreferencesByType(mType);
			}
			setOnPreferenceClickListener(this);
		}

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			mType = mKeys[which];
			setPreferencesByType(mType);
			final String name = mNames[which];
			setSummary(name);
			if (isEmpty(mName)) {
				mName = name;
				mNamePreference.setSummary(name);
			}
			mHasUnsavedChanges = true;
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}
		}

		@Override
		public boolean onPreferenceClick(final Preference preference) {
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}
			final int length = mKeys.length;
			for (int i = 0; i < length; i++) {
				final String key = mKeys[i];
				if (key.equals(mType)) {
					mSelectedPos = i;
				}
			}
			final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle(getTitle());
			builder.setSingleChoiceItems(mNames, mSelectedPos, this);
			mDialog = builder.show();
			return true;
		}
	}

	class Text1Preference extends AdditionalPreference {

		boolean mShouldCompleteUserName;

		public Text1Preference(final Context context, final int title) {
			super(context, title);
			setSummary(mText1);
		}

		@Override
		public String getTextToSet() {
			return mText1;
		}

		@Override
		public void onTextSet(final String text) {
			mText1 = text;
			mHasUnsavedChanges = true;
		}

		public void setShouldCompleteUserName(final boolean complete) {
			mShouldCompleteUserName = complete;
		}

		@Override
		public boolean shouldCompleteUserName() {
			return mShouldCompleteUserName;
		}

	}

	class Text2Preference extends AdditionalPreference {

		public Text2Preference(final Context context, final int title) {
			super(context, title);
			setSummary(mText2);
		}

		@Override
		public String getTextToSet() {
			return mText2;
		}

		@Override
		public void onTextSet(final String text) {
			mText2 = text;
			mHasUnsavedChanges = true;
		}

		@Override
		public final boolean shouldCompleteUserName() {
			return false;
		}

	}
}
