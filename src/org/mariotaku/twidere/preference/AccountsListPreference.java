package org.mariotaku.twidere.preference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.util.ImageLoaderWrapper;

import java.util.List;

public abstract class AccountsListPreference extends PreferenceCategory {

	public AccountsListPreference(final Context context) {
		super(context);
	}

	public AccountsListPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public AccountsListPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setAccountsData(final List<Account> accounts) {
		removeAll();
		for (final Account account : accounts) {
			final AccountItemPreference preference = new AccountItemPreference(getContext(), account);
			setupPreference(preference, account);
			addPreference(preference);
		}
	}

	@Override
	protected void onAttachedToHierarchy(final PreferenceManager preferenceManager) {
		super.onAttachedToHierarchy(preferenceManager);
		new LoadAccountsTask(this).execute();
	}

	protected abstract void setupPreference(AccountItemPreference preference, Account account);

	public static final class AccountItemPreference extends Preference implements ImageLoadingListener {
		private final Account mAccount;

		public AccountItemPreference(final Context context, final Account account) {
			super(context);
			mAccount = account;
		}

		@Override
		public void onLoadingCancelled(final String imageUri, final View view) {
			setIcon(R.drawable.ic_profile_image_default);
		}

		@Override
		public void onLoadingComplete(final String imageUri, final View view, final Bitmap loadedImage) {
			setIcon(new BitmapDrawable(getContext().getResources(), loadedImage));
		}

		@Override
		public void onLoadingFailed(final String imageUri, final View view, final FailReason failReason) {
			setIcon(R.drawable.ic_profile_image_default);
		}

		@Override
		public void onLoadingProgressChanged(final String imageUri, final View view, final int current, final int total) {

		}

		@Override
		public void onLoadingStarted(final String imageUri, final View view) {
			setIcon(R.drawable.ic_profile_image_default);
		}

		@Override
		protected void onAttachedToHierarchy(final PreferenceManager preferenceManager) {
			super.onAttachedToHierarchy(preferenceManager);
			setTitle(mAccount.name);
			setSummary(String.format("@%s", mAccount.screen_name));
			setIcon(R.drawable.ic_profile_image_default);
			final TwidereApplication app = TwidereApplication.getInstance(getContext());
			final ImageLoaderWrapper loader = app.getImageLoaderWrapper();
			loader.loadProfileImage(mAccount.profile_image_url, this);
		}

		@Override
		protected void onBindView(final View view) {
			super.onBindView(view);
		}

	}

	private static class LoadAccountsTask extends AsyncTask<Void, Void, List<Account>> {

		private final AccountsListPreference mPreference;

		public LoadAccountsTask(final AccountsListPreference preference) {
			mPreference = preference;
		}

		@Override
		protected List<Account> doInBackground(final Void... params) {
			return Account.getAccounts(mPreference.getContext(), false);
		}

		@Override
		protected void onPostExecute(final List<Account> result) {
			mPreference.setAccountsData(result);
		}

	}

}
