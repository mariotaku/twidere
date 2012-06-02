package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ComposeFragment;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ComposeActivity extends BaseActivity implements OnClickListener, OnLongClickListener {

	private static final String TAG_COMPOSE = "compose";
	private ComposeFragment mFragment;
	private ActionBar mActionBar;
	private ImageButton mSendButton;
	private TextView mTitle;
	private MenuItem mSendMenuItem = new SendMenuItem();

	public MenuItem getSendMenuItem() {
		return mSendMenuItem;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.send: {
				mFragment.onOptionsItemSelected(new SendMenuItem());
			}
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar = getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setCustomView(R.layout.actionbar_compose);

		View view = mActionBar.getCustomView();
		mSendButton = (ImageButton) view.findViewById(R.id.send);
		mSendButton.setOnClickListener(this);
		mSendButton.setOnLongClickListener(this);
		mTitle = (TextView) view.findViewById(R.id.title);

		FragmentManager fm = getSupportFragmentManager();

		// Check to see if we have retained the worker fragment.
		mFragment = (ComposeFragment) fm.findFragmentByTag(TAG_COMPOSE);

		String action = getIntent().getAction();
		// If not retained (or first time running), we need to create it.
		if (mFragment == null) {
			mFragment = new ComposeFragment();
			Bundle args = getIntent().getExtras() == null ? new Bundle() : getIntent().getExtras();
			if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
				args.putBoolean(INTENT_KEY_IS_SHARE, true);
			}
			mFragment.setArguments(args);
			fm.beginTransaction().replace(android.R.id.content, mFragment, TAG_COMPOSE).commit();
		}
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
			case R.id.send: {
				Toast t = Toast.makeText(this, mSendMenuItem.getTitle(), Toast.LENGTH_SHORT);

				final int[] screenPos = new int[2];
				final Rect displayFrame = new Rect();
				mSendButton.getLocationOnScreen(screenPos);
				mSendButton.getWindowVisibleDisplayFrame(displayFrame);

				final int width = mSendButton.getWidth();
				final int height = mSendButton.getHeight();
				final int midy = screenPos[1] + height / 2;
				final int screenWidth = getResources().getDisplayMetrics().widthPixels;

				if (midy < displayFrame.height()) {
					// Show along the top; follow action buttons
					t.setGravity(Gravity.TOP | Gravity.RIGHT, screenWidth - screenPos[0] - width / 2, height);
				} else {
					// Show along the bottom center
					t.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
				}
				t.show();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		mTitle.setText(title);
	}

	@Override
	public void setTitle(int titleId) {
		super.setTitle(titleId);
		mTitle.setText(titleId);
	}

	private class SendMenuItem implements MenuItem {

		@Override
		public boolean collapseActionView() {
			return false;
		}

		@Override
		public boolean expandActionView() {
			return false;
		}

		@Override
		public ActionProvider getActionProvider() {
			return null;
		}

		@Override
		public View getActionView() {
			return null;
		}

		@Override
		public char getAlphabeticShortcut() {
			return 0;
		}

		@Override
		public int getGroupId() {
			return 0;
		}

		@Override
		public Drawable getIcon() {
			return mSendButton.getDrawable();
		}

		@Override
		public Intent getIntent() {
			return new Intent();
		}

		@Override
		public int getItemId() {
			return R.id.send;
		}

		@Override
		public ContextMenuInfo getMenuInfo() {
			return null;
		}

		@Override
		public char getNumericShortcut() {
			return 0;
		}

		@Override
		public int getOrder() {
			return 0;
		}

		@Override
		public SubMenu getSubMenu() {
			return null;
		}

		@Override
		public CharSequence getTitle() {
			return mSendButton.getContentDescription();
		}

		@Override
		public CharSequence getTitleCondensed() {
			return null;
		}

		@Override
		public boolean hasSubMenu() {
			return false;
		}

		@Override
		public boolean isActionViewExpanded() {
			return false;
		}

		@Override
		public boolean isCheckable() {
			return false;
		}

		@Override
		public boolean isChecked() {
			return false;
		}

		@Override
		public boolean isEnabled() {
			return mSendButton.isClickable();
		}

		@Override
		public boolean isVisible() {
			return mSendButton.getVisibility() == View.VISIBLE;
		}

		@Override
		public MenuItem setActionProvider(ActionProvider actionProvider) {
			return this;
		}

		@Override
		public MenuItem setActionView(int resId) {
			return this;
		}

		@Override
		public MenuItem setActionView(View view) {
			return this;
		}

		@Override
		public MenuItem setAlphabeticShortcut(char alphaChar) {
			return this;
		}

		@Override
		public MenuItem setCheckable(boolean checkable) {
			return this;
		}

		@Override
		public MenuItem setChecked(boolean checked) {
			return this;
		}

		@Override
		public MenuItem setEnabled(boolean enabled) {
			mSendButton.setClickable(enabled);
			mSendButton.setAlpha(enabled ? 0xFF : 0x80);
			mSendButton.setOnClickListener(enabled ? ComposeActivity.this : null);
			return this;
		}

		@Override
		public MenuItem setIcon(Drawable icon) {
			mSendButton.setImageDrawable(icon);
			return this;
		}

		@Override
		public MenuItem setIcon(int iconRes) {
			mSendButton.setImageResource(iconRes);
			return this;
		}

		@Override
		public MenuItem setIntent(Intent intent) {
			return this;
		}

		@Override
		public MenuItem setNumericShortcut(char numericChar) {
			return this;
		}

		@Override
		public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
			return this;
		}

		@Override
		public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
			return this;
		}

		@Override
		public MenuItem setShortcut(char numericChar, char alphaChar) {
			return this;
		}

		@Override
		public void setShowAsAction(int actionEnum) {

		}

		@Override
		public MenuItem setShowAsActionFlags(int actionEnum) {
			return this;
		}

		@Override
		public MenuItem setTitle(CharSequence title) {
			mSendButton.setContentDescription(title);
			return this;
		}

		@Override
		public MenuItem setTitle(int title) {
			mSendButton.setContentDescription(getString(title));
			return this;
		}

		@Override
		public MenuItem setTitleCondensed(CharSequence title) {
			return this;
		}

		@Override
		public MenuItem setVisible(boolean visible) {
			mSendButton.setVisibility(visible ? View.VISIBLE : View.GONE);
			return this;
		}

	}

}
