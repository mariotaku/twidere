package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.isMyActivatedUserName;

import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.loader.UserListCreatedLoader;
import org.mariotaku.twidere.model.ParcelableUserList;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class UserListCreatedFragment extends BaseUserListsFragment implements OnClickListener {

	private View mHeaderView;

	@Override
	public void addHeaders(ListView list) {
		mHeaderView = LayoutInflater.from(getActivity()).inflate(R.layout.user_list_created_header, null);
		mHeaderView.setOnClickListener(this);
		list.addHeaderView(mHeaderView, null, false);
	}

	@Override
	public Loader<List<ParcelableUserList>> newLoaderInstance(long account_id, long user_id, String screen_name) {
		return new UserListCreatedLoader(getActivity(), account_id, user_id, screen_name, getCursor(), getData());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (mHeaderView != null) {
			mHeaderView.setVisibility(getAccountId() == getUserId()
					|| isMyActivatedUserName(getActivity(), getScreenName()) ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.add: {
				break;
			}
		}
		
	}
}
