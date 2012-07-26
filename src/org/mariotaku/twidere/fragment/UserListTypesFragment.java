package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.openUserListCreated;
import static org.mariotaku.twidere.util.Utils.openUserListMemberships;
import static org.mariotaku.twidere.util.Utils.openUserListSubscriptions;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ListAction;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class UserListTypesFragment extends BaseListFragment implements OnItemClickListener {

	private UserProfileActionAdapter mAdapter;
	private ListView mListView;
	private long mAccountId, mUserId;
	private String mScreenName;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Bundle args = getArguments();
		if (args != null) {
			mAccountId = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			mUserId = args.getLong(INTENT_KEY_USER_ID, -1);
			mScreenName = args.getString(INTENT_KEY_SCREEN_NAME);
		}
		mAdapter = new UserProfileActionAdapter(getActivity());
		mAdapter.add(new UserCreatedListAction());
		mAdapter.add(new UserFollowedListAction());
		mAdapter.add(new ListsFollowingUserAction());
		setListAdapter(null);
		mListView = getListView();
		mListView.setOnItemClickListener(this);
		setListAdapter(mAdapter);

	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		final ListAction action = mAdapter.findItem(id);
		if (action != null) {
			action.onClick();
		}
	}

	private class ListsFollowingUserAction extends ListAction {

		@Override
		public String getName() {
			return getString(R.string.list_following_user);
		}

		@Override
		public void onClick() {
			openUserListMemberships(getActivity(), mAccountId, mUserId, mScreenName);
		}
	}

	private class UserCreatedListAction extends ListAction {

		@Override
		public String getName() {
			return getString(R.string.list_created_by_user);
		}

		@Override
		public void onClick() {
			openUserListCreated(getActivity(), mAccountId, mUserId, mScreenName);
		}
	}

	private class UserFollowedListAction extends ListAction {

		@Override
		public String getName() {
			return getString(R.string.list_user_followed);
		}

		@Override
		public void onClick() {
			openUserListSubscriptions(getActivity(), mAccountId, mUserId, mScreenName);
		}
	}

	private class UserProfileActionAdapter extends ArrayAdapter<ListAction> {

		public UserProfileActionAdapter(Context context) {
			super(context, R.layout.user_action_list_item, android.R.id.text1);
		}

		public ListAction findItem(long id) {
			final int count = getCount();
			for (int i = 0; i < count; i++) {
				if (id == getItemId(i)) return getItem(i);
			}
			return null;
		}
	}
}
