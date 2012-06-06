package org.mariotaku.twidere.activity;

import org.mariotaku.actionbarcompat.app.ActionBar;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ComposeFragment;
import org.mariotaku.twidere.util.FakeMenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

public class ComposeActivity extends BaseActivity implements OnClickListener {

	private static final String TAG_COMPOSE = "compose";
	private ComposeFragment mFragment;
	private ActionBar mActionBar;
	private ImageButton mSendButton;
	private TextView mTitle;
	private MenuItem mSendMenuItem;

	public MenuItem getSendMenuItem() {
		return mSendMenuItem;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.send: {
				mFragment.onOptionsItemSelected(mSendMenuItem);
			}
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Simple workaround for ActionBarCompat
		setContentView(new FrameLayout(this));
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setCustomView(R.layout.actionbar_compose);

		View view = mActionBar.getCustomView();
		mSendButton = (ImageButton) view.findViewById(R.id.send);
		mSendMenuItem = new FakeMenuItem(mSendButton, this);
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

}
