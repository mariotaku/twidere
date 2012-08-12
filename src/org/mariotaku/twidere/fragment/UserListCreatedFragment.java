package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.isMyActivatedUserName;
import static org.mariotaku.twidere.util.Utils.parseString;

import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.loader.UserListCreatedLoader;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.ServiceInterface;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

public class UserListCreatedFragment extends BaseUserListsListFragment implements OnClickListener {

	private View mHeaderView;

	private DialogFragment mDialogFragment;

	@Override
	public void addHeaders(ListView list) {
		if (getAccountId() == getUserId() || isMyActivatedUserName(getActivity(), getScreenName())) {
			mHeaderView = LayoutInflater.from(getActivity()).inflate(R.layout.user_list_created_header, null);
			mHeaderView.setOnClickListener(this);
			list.addHeaderView(mHeaderView, null, false);
		}
	}

	@Override
	public Loader<List<ParcelableUserList>> newLoaderInstance(long account_id, long user_id, String screen_name) {
		return new UserListCreatedLoader(getActivity(), account_id, user_id, screen_name, getCursor(), getData());
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.add: {
				if (mDialogFragment != null && mDialogFragment.isAdded()) {
					mDialogFragment.dismiss();
				}
				mDialogFragment = new CreateUserListDialogFragment();
				final Bundle args = new Bundle();
				args.putLong(INTENT_KEY_ACCOUNT_ID, getAccountId());
				mDialogFragment.setArguments(args);
				mDialogFragment.show(getFragmentManager(), "create_list");
				break;
			}
		}

	}

	public static class CreateUserListDialogFragment extends BaseDialogFragment implements
			DialogInterface.OnClickListener {

		private EditText mEditName, mEditDescription;
		private CheckBox mPublicCheckBox;
		private String mName, mDescription;
		private long mAccountId;
		private boolean mIsPublic;
		private ServiceInterface mService;

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (mAccountId <= 0) return;
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					mName = parseString(mEditName.getText());
					mDescription = parseString(mEditDescription.getText());
					mIsPublic = mPublicCheckBox.isChecked();
					if (mName == null || mName.length() <= 0) return;
					mService.createUserList(mAccountId, mName, mIsPublic, mDescription);
					break;
				}
			}

		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			mService = getApplication().getServiceInterface();
			final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
			mAccountId = bundle != null ? bundle.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
			mName = bundle != null ? bundle.getString(INTENT_KEY_LIST_NAME) : null;
			mDescription = bundle != null ? bundle.getString(INTENT_KEY_DESCRIPTION) : null;
			mIsPublic = bundle != null ? bundle.getBoolean(INTENT_KEY_IS_PUBLIC, true) : true;
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			final View view = LayoutInflater.from(getActivity()).inflate(R.layout.user_list_detail_dialog_view, null);
			builder.setView(view);
			mEditName = (EditText) view.findViewById(R.id.name);
			mEditDescription = (EditText) view.findViewById(R.id.description);
			mPublicCheckBox = (CheckBox) view.findViewById(R.id.is_public);
			if (mName != null) {
				mEditName.setText(mName);
			}
			if (mDescription != null) {
				mEditDescription.setText(mDescription);
			}
			mPublicCheckBox.setChecked(mIsPublic);
			builder.setTitle(R.string.new_user_list);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			outState.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
			outState.putString(INTENT_KEY_LIST_NAME, mName);
			outState.putString(INTENT_KEY_DESCRIPTION, mDescription);
			outState.putBoolean(INTENT_KEY_IS_PUBLIC, mIsPublic);
			super.onSaveInstanceState(outState);
		}

	}
}
