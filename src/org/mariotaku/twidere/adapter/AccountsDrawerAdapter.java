package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.view.iface.IColorLabelView;

import java.util.Arrays;

public class AccountsDrawerAdapter extends BaseExpandableListAdapter implements Constants, OnCheckedChangeListener {

	public static final int GROUP_ID_ACCOUNTS = 0;
	public static final int GROUP_ID_ACCOUNT_OPTIONS = 1;
	public static final int GROUP_ID_MENU = 2;

	private static final GroupItem[] GROUPS = new GroupItem[3];
	private static final OptionItem[] DEFAULT_ACCOUNT_OPTIONS = new OptionItem[8];
	private static final OptionItem[] ACCOUNT_OPTIONS = new OptionItem[9];
	private static final OptionItem[] MORE_OPTION_ITEMS = new OptionItem[4];

	static {
		DEFAULT_ACCOUNT_OPTIONS[0] = new OptionItem(R.string.view_user_profile, R.drawable.ic_menu_profile,
				MENU_VIEW_PROFILE);
		DEFAULT_ACCOUNT_OPTIONS[1] = new OptionItem(R.string.statuses, R.drawable.ic_menu_quote, MENU_STATUSES);
		DEFAULT_ACCOUNT_OPTIONS[2] = new OptionItem(R.string.favorites, R.drawable.ic_menu_star, MENU_FAVORITES);
		DEFAULT_ACCOUNT_OPTIONS[3] = new OptionItem(R.string.users_lists, R.drawable.ic_menu_list, MENU_LISTS);
		DEFAULT_ACCOUNT_OPTIONS[4] = new OptionItem(R.string.lists_following_user, R.drawable.ic_menu_list,
				MENU_LIST_MEMBERSHIPS);
		DEFAULT_ACCOUNT_OPTIONS[5] = new OptionItem(R.string.edit_profile, android.R.drawable.ic_menu_edit, MENU_EDIT);
		DEFAULT_ACCOUNT_OPTIONS[6] = new OptionItem(R.string.set_color, R.drawable.ic_menu_color_palette,
				MENU_SET_COLOR);
		DEFAULT_ACCOUNT_OPTIONS[7] = new OptionItem(R.string.delete, android.R.drawable.ic_menu_delete, MENU_DELETE);
		ACCOUNT_OPTIONS[0] = new OptionItem(R.string.view_user_profile, R.drawable.ic_menu_profile, MENU_VIEW_PROFILE);
		ACCOUNT_OPTIONS[1] = new OptionItem(R.string.statuses, R.drawable.ic_menu_quote, MENU_STATUSES);
		ACCOUNT_OPTIONS[2] = new OptionItem(R.string.favorites, R.drawable.ic_menu_star, MENU_FAVORITES);
		ACCOUNT_OPTIONS[3] = new OptionItem(R.string.users_lists, R.drawable.ic_menu_list, MENU_LISTS);
		ACCOUNT_OPTIONS[4] = new OptionItem(R.string.lists_following_user, R.drawable.ic_menu_list,
				MENU_LIST_MEMBERSHIPS);
		ACCOUNT_OPTIONS[5] = new OptionItem(R.string.edit_profile, android.R.drawable.ic_menu_edit, MENU_EDIT);
		ACCOUNT_OPTIONS[6] = new OptionItem(R.string.set_color, R.drawable.ic_menu_color_palette, MENU_SET_COLOR);
		ACCOUNT_OPTIONS[7] = new OptionItem(R.string.set_as_default, R.drawable.ic_menu_mark, MENU_SET_AS_DEFAULT);
		ACCOUNT_OPTIONS[8] = new OptionItem(R.string.delete, android.R.drawable.ic_menu_delete, MENU_DELETE);
		MORE_OPTION_ITEMS[0] = new OptionItem(android.R.string.search_go, android.R.drawable.ic_menu_search,
				MENU_SEARCH);
		MORE_OPTION_ITEMS[1] = new OptionItem(R.string.add_account, android.R.drawable.ic_menu_add, MENU_ADD_ACCOUNT);
		MORE_OPTION_ITEMS[2] = new OptionItem(R.string.filters, R.drawable.ic_menu_mute, MENU_FILTERS);
		MORE_OPTION_ITEMS[3] = new OptionItem(R.string.settings, android.R.drawable.ic_menu_preferences, MENU_SETTINGS);
		GROUPS[0] = new GroupItem(R.string.accounts, R.layout.accounts_drawer_item_child_accounts, GROUP_ID_ACCOUNTS);
		GROUPS[1] = new GroupItem(R.string.account_options, R.layout.menu_list_item, GROUP_ID_ACCOUNT_OPTIONS);
		GROUPS[2] = new GroupItem(R.string.more, R.layout.menu_list_item, GROUP_ID_MENU);
	}

	private final ImageLoaderWrapper mImageLoader;

	private final LayoutInflater mInflater;
	private final Context mContext;

	private Cursor mCursor;

	private Account.Indices mIndices;
	private long mSelectedAccountId, mDefaultAccountId;

	private OnAccountActivateStateChangeListener mOnAccountActivateStateChangeListener;

	public AccountsDrawerAdapter(final Context context) {
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mImageLoader = app.getImageLoaderWrapper();
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public Object getChild(final int groupPosition, final int childPosition) {
		final GroupItem groupItem = getGroup(groupPosition);
		switch (groupItem.getId()) {
			case GROUP_ID_ACCOUNTS: {
				mCursor.moveToPosition(childPosition);
				return new Account(mCursor, mIndices);
			}
			case GROUP_ID_ACCOUNT_OPTIONS: {
				return getOptionsForAccount()[childPosition];
			}
			case GROUP_ID_MENU: {
				return MORE_OPTION_ITEMS[childPosition];
			}
		}
		return null;
	}

	@Override
	public long getChildId(final int groupPosition, final int childPosition) {
		return Arrays.hashCode(new Object[] { getGroupId(groupPosition), getChild(groupPosition, childPosition) });
	}

	@Override
	public int getChildrenCount(final int groupPosition) {
		final GroupItem groupItem = getGroup(groupPosition);
		switch (groupItem.getId()) {
			case GROUP_ID_ACCOUNTS: {
				return mCursor != null && !mCursor.isClosed() ? mCursor.getCount() : 0;
			}
			case GROUP_ID_ACCOUNT_OPTIONS: {
				if (mSelectedAccountId > 0) return getOptionsForAccount().length;
				return 0;
			}
			case GROUP_ID_MENU: {
				return MORE_OPTION_ITEMS.length;
			}
		}
		return 0;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild,
			final View convertView, final ViewGroup parent) {
		final GroupItem groupItem = getGroup(groupPosition);
		final View view;
		if (convertView == null || !groupItem.equals(convertView.getTag())) {
			view = mInflater.inflate(groupItem.getChildLayoutRes(), parent, false);
		} else {
			view = convertView;
		}
		view.setTag(groupItem);
		switch (groupItem.getId()) {
			case GROUP_ID_ACCOUNTS: {
				final Account account = (Account) getChild(groupPosition, childPosition);
				final CompoundButton toggle = (CompoundButton) view.findViewById(R.id.toggle);
				final TextView name = (TextView) view.findViewById(R.id.name);
				final TextView screen_name = (TextView) view.findViewById(R.id.screen_name);
				final TextView default_indicator = (TextView) view.findViewById(R.id.default_indicator);
				final ImageView profile_image = (ImageView) view.findViewById(R.id.profile_image);
				name.setText(account.name);
				screen_name.setText(String.format("@%s", account.screen_name));
				default_indicator.setVisibility(account.account_id == mDefaultAccountId ? View.VISIBLE : View.GONE);
				mImageLoader.displayProfileImage(profile_image, account.profile_image_url);
				toggle.setChecked(account.is_activated);
				toggle.setTag(account);
				toggle.setOnCheckedChangeListener(this);
				view.setActivated(account.account_id == mSelectedAccountId);
				((IColorLabelView) view).drawEnd(account.user_color);
				break;
			}
			case GROUP_ID_ACCOUNT_OPTIONS:
			case GROUP_ID_MENU: {
				final OptionItem option = (OptionItem) getChild(groupPosition, childPosition);
				final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
				final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
				text1.setText(option.getName());
				icon.setImageResource(option.getIcon());
				break;
			}
		}
		return view;
	}

	@Override
	public GroupItem getGroup(final int groupPosition) {
		return GROUPS[groupPosition];
	}

	@Override
	public int getGroupCount() {
		return GROUPS.length;
	}

	@Override
	public long getGroupId(final int groupPosition) {
		return getGroup(groupPosition).hashCode();
	}

	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded, final View convertView,
			final ViewGroup parent) {
		final TextView view = convertView != null ? (TextView) convertView : new TextView(mContext, null,
				android.R.attr.listSeparatorTextViewStyle);
		view.setClickable(true);
		final GroupItem groupItem = getGroup(groupPosition);
		view.setText(groupItem.getTitle());
		return view;
	}

	public long getSelectedAccountId() {
		return mSelectedAccountId;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(final int groupPosition, final int childPosition) {
		final GroupItem groupItem = getGroup(groupPosition);
		if (groupItem.getId() == GROUP_ID_ACCOUNTS) {
			final Account account = (Account) getChild(groupPosition, childPosition);
			return account.account_id != mSelectedAccountId;
		}
		return true;
	}

	@Override
	public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
		final Account account = (Account) buttonView.getTag();
		if (mOnAccountActivateStateChangeListener != null) {
			mOnAccountActivateStateChangeListener.onAccountActivateStateChanged(account, isChecked);
		}

	}

	public void setAccountsCursor(final Cursor cursor) {
		mCursor = cursor;
		mIndices = cursor != null ? new Account.Indices(cursor) : null;
		notifyDataSetChanged();
	}

	public void setDefaultAccountId(final long account_id) {
		if (mDefaultAccountId == account_id) return;
		mDefaultAccountId = account_id;
		notifyDataSetChanged();
	}

	public void setOnAccountActivateStateChangeListener(final OnAccountActivateStateChangeListener listener) {
		mOnAccountActivateStateChangeListener = listener;
	}

	public void setSelectedAccountId(final long account_id) {
		mSelectedAccountId = account_id;
		notifyDataSetChanged();
	}

	private OptionItem[] getOptionsForAccount() {
		final boolean is_default = mSelectedAccountId == mDefaultAccountId;
		return is_default ? DEFAULT_ACCOUNT_OPTIONS : ACCOUNT_OPTIONS;
	}

	public static class GroupItem {
		private final int title;
		private final int childLayoutRes;
		private final int id;

		public GroupItem(final int title, final int childLayoutRes, final int id) {
			this.id = id;
			this.title = title;
			this.childLayoutRes = childLayoutRes;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (!(obj instanceof GroupItem)) return false;
			final GroupItem other = (GroupItem) obj;
			if (childLayoutRes != other.childLayoutRes) return false;
			if (id != other.id) return false;
			if (title != other.title) return false;
			return true;
		}

		public int getChildLayoutRes() {
			return childLayoutRes;
		}

		public int getId() {
			return id;
		}

		public int getTitle() {
			return title;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + childLayoutRes;
			result = prime * result + id;
			result = prime * result + title;
			return result;
		}

		@Override
		public String toString() {
			return "GroupItem{title=" + title + ", childLayoutRes=" + childLayoutRes + ", id=" + id + "}";
		}
	}

	public static interface OnAccountActivateStateChangeListener {
		void onAccountActivateStateChanged(Account account, boolean activated);
	}

	public static class OptionItem {

		private final int name, icon, id;

		OptionItem(final int name, final int icon, final int id) {
			this.name = name;
			this.icon = icon;
			this.id = id;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (!(obj instanceof OptionItem)) return false;
			final OptionItem other = (OptionItem) obj;
			if (icon != other.icon) return false;
			if (id != other.id) return false;
			if (name != other.name) return false;
			return true;
		}

		public int getIcon() {
			return icon;
		}

		public int getId() {
			return id;
		}

		public int getName() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + icon;
			result = prime * result + id;
			result = prime * result + name;
			return result;
		}

		@Override
		public String toString() {
			return "AccountOption{name=" + name + ", icon=" + icon + ", id=" + id + "}";
		}
	}

}
