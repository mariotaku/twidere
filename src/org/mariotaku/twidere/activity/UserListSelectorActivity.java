
package org.mariotaku.twidere.activity;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.ParseUtils.parseString;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ParcelableUserListsAdapter;
import org.mariotaku.twidere.adapter.ParcelableUsersAdapter;
import org.mariotaku.twidere.adapter.UserHashtagAutoCompleteAdapter;
import org.mariotaku.twidere.fragment.CreateUserListDialogFragment;
import org.mariotaku.twidere.fragment.SupportProgressDialogFragment;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.util.AsyncTask;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.http.HttpResponseCode;

import java.util.ArrayList;
import java.util.List;

public class UserListSelectorActivity extends BaseSupportDialogActivity implements OnClickListener,
        OnItemClickListener {

    private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (BROADCAST_USER_LIST_CREATED.equals(action)) {
                getUserLists(mScreenName);
            }
        }
    };

    private AutoCompleteTextView mEditScreenName;
    private ListView mUserListsListView, mUsersListView;
    private ParcelableUserListsAdapter mUserListsAdapter;
    private ParcelableUsersAdapter mUsersAdapter;
    private View mUsersListContainer, mUserListsContainer, mCreateUserListContainer;

    private long mAccountId;
    private String mScreenName;

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.screen_name_confirm: {
                final String screen_name = parseString(mEditScreenName.getText());
                if (isEmpty(screen_name))
                    return;
                getUserLists(screen_name);
                break;
            }
            case R.id.create_list: {
                final DialogFragment f = new CreateUserListDialogFragment();
                final Bundle args = new Bundle();
                args.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
                f.setArguments(args);
                f.show(getSupportFragmentManager(), null);
                break;
            }
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mUsersListContainer = findViewById(R.id.users_list_container);
        mUserListsContainer = findViewById(R.id.user_lists_container);
        mEditScreenName = (AutoCompleteTextView) findViewById(R.id.edit_screen_name);
        mUserListsListView = (ListView) findViewById(R.id.user_lists_list);
        mUsersListView = (ListView) findViewById(R.id.users_list);
        mCreateUserListContainer = findViewById(R.id.create_list_container);
    }

    @Override
    public void onItemClick(final AdapterView<?> view, final View child, final int position,
            final long id) {
        final int view_id = view.getId();
        final ListView list = (ListView) view;
        if (view_id == R.id.users_list) {
            final ParcelableUser user = mUsersAdapter
                    .getItem(position - list.getHeaderViewsCount());
            if (user == null)
                return;
            getUserLists(user.screen_name);
        } else if (view_id == R.id.user_lists_list) {
            final Intent data = new Intent();
            data.putExtra(INTENT_KEY_USER_LIST,
                    mUserListsAdapter.getItem(position - list.getHeaderViewsCount()));
            setResult(RESULT_OK, data);
            finish();
        }
    }

    public void setUsersData(final List<ParcelableUser> data) {
        mUsersAdapter.setData(data, true);
        mUsersListContainer.setVisibility(View.VISIBLE);
        mUserListsContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(INTENT_KEY_ACCOUNT_ID)) {
            finish();
            return;
        }
        setContentView(R.layout.select_user_list);
        mAccountId = extras.getLong(INTENT_KEY_ACCOUNT_ID);
        if (savedInstanceState == null) {
            mScreenName = extras.getString(INTENT_KEY_SCREEN_NAME);
        } else {
            mScreenName = savedInstanceState.getString(INTENT_KEY_SCREEN_NAME);
        }

        final boolean selecting_user = isSelectingUser();
        if (!isEmpty(mScreenName)) {
            if (selecting_user) {
                searchUser(mScreenName);
            } else {
                getUserLists(mScreenName);
            }
        }
        mEditScreenName.setAdapter(new UserHashtagAutoCompleteAdapter(this));
        mEditScreenName.setText(mScreenName);
        mUserListsListView.setAdapter(mUserListsAdapter = new ParcelableUserListsAdapter(this));
        mUserListsListView.setDivider(null);
        mUsersListView.setAdapter(mUsersAdapter = new ParcelableUsersAdapter(this));
        mUsersListView.setDivider(null);
        mUserListsListView.setOnItemClickListener(this);
        mUsersListView.setOnItemClickListener(this);
        if (selecting_user) {
            mUsersListContainer.setVisibility(View.VISIBLE);
            mUserListsContainer.setVisibility(View.GONE);
        } else {
            mUsersListContainer.setVisibility(isEmpty(mScreenName) ? View.VISIBLE : View.GONE);
            mUserListsContainer.setVisibility(isEmpty(mScreenName) ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INTENT_KEY_SCREEN_NAME, mScreenName);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(BROADCAST_USER_LIST_CREATED);
        registerReceiver(mStatusReceiver, filter);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mStatusReceiver);
        super.onStop();
    }

    private void getUserLists(final String screen_name) {
        if (screen_name == null)
            return;
        mScreenName = screen_name;
        final GetUserListsTask task = new GetUserListsTask(this, mAccountId, screen_name);
        task.execute();
    }

    private boolean isSelectingUser() {
        return INTENT_ACTION_SELECT_USER.equals(getIntent().getAction());
    }

    private void searchUser(final String name) {
        final SearchUsersTask task = new SearchUsersTask(this, mAccountId, name);
        task.execute();
    }

    private void setUserListsData(final List<ParcelableUserList> data, final boolean is_my_account) {
        mUserListsAdapter.setData(data, true);
        mUsersListContainer.setVisibility(View.GONE);
        mUserListsContainer.setVisibility(View.VISIBLE);
        mCreateUserListContainer.setVisibility(is_my_account ? View.VISIBLE : View.GONE);
    }

    private static class GetUserListsTask extends
            AsyncTask<Void, Void, SingleResponse<List<ParcelableUserList>>> {

        private static final String FRAGMENT_TAG_GET_USER_LISTS = "get_user_lists";
        private final UserListSelectorActivity mActivity;
        private final long mAccountId;
        private final String mScreenName;
        private final boolean mLargeProfileImage;

        GetUserListsTask(final UserListSelectorActivity activity, final long account_id,
                final String screen_name) {
            mActivity = activity;
            mAccountId = account_id;
            mScreenName = screen_name;
            mLargeProfileImage = activity.getResources().getBoolean(R.bool.hires_profile_image);
        }

        @Override
        protected SingleResponse<List<ParcelableUserList>> doInBackground(final Void... params) {
            final Twitter twitter = getTwitterInstance(mActivity, mAccountId, false);
            try {
                final ResponseList<UserList> lists = twitter.getUserLists(mScreenName);
                final List<ParcelableUserList> data = new ArrayList<ParcelableUserList>();
                boolean is_my_account = mScreenName.equalsIgnoreCase(getAccountScreenName(
                        mActivity, mAccountId));
                for (final UserList item : lists) {
                    final User user = item.getUser();
                    if (user != null && mScreenName.equalsIgnoreCase(user.getScreenName())) {
                        if (!is_my_account && user.getId() == mAccountId) {
                            is_my_account = true;
                        }
                        data.add(new ParcelableUserList(item, mAccountId, mLargeProfileImage));
                    }
                }
                final SingleResponse<List<ParcelableUserList>> result = SingleResponse
                        .dataOnly(data);
                result.extras.putBoolean(INTENT_KEY_IS_MY_ACCOUNT, is_my_account);
                return result;
            } catch (final TwitterException e) {
                e.printStackTrace();
                return SingleResponse.exceptionOnly(e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<List<ParcelableUserList>> result) {
            final Fragment f = mActivity.getSupportFragmentManager().findFragmentByTag(
                    FRAGMENT_TAG_GET_USER_LISTS);
            if (f instanceof DialogFragment) {
                ((DialogFragment) f).dismiss();
            }
            if (result.data != null) {
                mActivity.setUserListsData(result.data,
                        result.extras.getBoolean(INTENT_KEY_IS_MY_ACCOUNT));
            } else if (result.exception instanceof TwitterException) {
                final TwitterException te = (TwitterException) result.exception;
                if (te.getStatusCode() == HttpResponseCode.NOT_FOUND) {
                    mActivity.searchUser(mScreenName);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            SupportProgressDialogFragment.show(mActivity, FRAGMENT_TAG_GET_USER_LISTS);
        }

    }

    private static class SearchUsersTask extends
            AsyncTask<Void, Void, SingleResponse<List<ParcelableUser>>> {

        private static final String FRAGMENT_TAG_SEARCH_USERS = "search_users";
        private final UserListSelectorActivity mActivity;
        private final long mAccountId;
        private final String mName;
        private final boolean mLargeProfileImage;

        SearchUsersTask(final UserListSelectorActivity activity, final long account_id,
                final String name) {
            mActivity = activity;
            mAccountId = account_id;
            mName = name;
            mLargeProfileImage = activity.getResources().getBoolean(R.bool.hires_profile_image);
        }

        @Override
        protected SingleResponse<List<ParcelableUser>> doInBackground(final Void... params) {
            final Twitter twitter = getTwitterInstance(mActivity, mAccountId, false);
            try {
                final ResponseList<User> lists = twitter.searchUsers(mName, 1);
                final List<ParcelableUser> data = new ArrayList<ParcelableUser>();
                for (final User item : lists) {
                    data.add(new ParcelableUser(item, mAccountId, mLargeProfileImage));
                }
                return SingleResponse.dataOnly(data);
            } catch (final TwitterException e) {
                e.printStackTrace();
                return SingleResponse.exceptionOnly(e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<List<ParcelableUser>> result) {
            final Fragment f = mActivity.getSupportFragmentManager().findFragmentByTag(
                    FRAGMENT_TAG_SEARCH_USERS);
            if (f instanceof DialogFragment) {
                ((DialogFragment) f).dismiss();
            }
            if (result.data != null) {
                mActivity.setUsersData(result.data);
            }
        }

        @Override
        protected void onPreExecute() {
            SupportProgressDialogFragment.show(mActivity, FRAGMENT_TAG_SEARCH_USERS);
        }

    }

}
