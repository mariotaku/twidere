package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.view.holder.AccountDrawerGroupViewHolder;

import java.util.Arrays;

public class AccountsDrawerAdapter extends BaseExpandableListAdapter implements Constants {

	private static final float ITEM_ACTIVATED_ALPHA = 1f;
	private static final float ITEM_INACTIVATED_ALPHA = 0.5f;
	private static final int GROUP_LAYOUT = R.layout.accounts_drawer_item_group;
	private static final int CHILD_LAYOUT = R.layout.accounts_drawer_item_child;
	private static final AccountAction[] DEFAULT_ACCOUNT_ACTIONS = new AccountAction[8];
	private static final AccountAction[] ACCOUNT_ACTIONS = new AccountAction[9];

	static {
		DEFAULT_ACCOUNT_ACTIONS[0] = new AccountAction(R.string.view_user_profile, R.drawable.ic_menu_profile,
				MENU_VIEW_PROFILE);
		DEFAULT_ACCOUNT_ACTIONS[1] = new AccountAction(R.string.statuses, R.drawable.ic_menu_quote, MENU_STATUSES);
		DEFAULT_ACCOUNT_ACTIONS[2] = new AccountAction(R.string.favorites, R.drawable.ic_menu_star, MENU_FAVORITES);
		DEFAULT_ACCOUNT_ACTIONS[3] = new AccountAction(R.string.user_list, R.drawable.ic_menu_list, MENU_LISTS);
		DEFAULT_ACCOUNT_ACTIONS[4] = new AccountAction(R.string.lists_following_user, R.drawable.ic_menu_list,
				MENU_LIST_MEMBERSHIPS);
		DEFAULT_ACCOUNT_ACTIONS[5] = new AccountAction(R.string.edit_profile, android.R.drawable.ic_menu_edit,
				MENU_EDIT);
		DEFAULT_ACCOUNT_ACTIONS[6] = new AccountAction(R.string.set_color, R.drawable.ic_menu_color_palette,
				MENU_SET_COLOR);
		DEFAULT_ACCOUNT_ACTIONS[7] = new AccountAction(R.string.delete, android.R.drawable.ic_menu_delete, MENU_DELETE);
		ACCOUNT_ACTIONS[0] = new AccountAction(R.string.view_user_profile, R.drawable.ic_menu_profile,
				MENU_VIEW_PROFILE);
		ACCOUNT_ACTIONS[1] = new AccountAction(R.string.statuses, R.drawable.ic_menu_quote, MENU_STATUSES);
		ACCOUNT_ACTIONS[2] = new AccountAction(R.string.favorites, R.drawable.ic_menu_star, MENU_FAVORITES);
		ACCOUNT_ACTIONS[3] = new AccountAction(R.string.user_list, R.drawable.ic_menu_list, MENU_LISTS);
		ACCOUNT_ACTIONS[4] = new AccountAction(R.string.lists_following_user, R.drawable.ic_menu_list,
				MENU_LIST_MEMBERSHIPS);
		ACCOUNT_ACTIONS[5] = new AccountAction(R.string.edit_profile, android.R.drawable.ic_menu_edit, MENU_EDIT);
		ACCOUNT_ACTIONS[6] = new AccountAction(R.string.set_color, R.drawable.ic_menu_color_palette, MENU_SET_COLOR);
		ACCOUNT_ACTIONS[7] = new AccountAction(R.string.set_as_default, R.drawable.ic_menu_mark, MENU_SET_AS_DEFAULT);
		ACCOUNT_ACTIONS[8] = new AccountAction(R.string.delete, android.R.drawable.ic_menu_delete, MENU_DELETE);
	}

	private final ImageLoaderWrapper mImageLoader;

	private final LayoutInflater mInflater;
	private final int mDefaultBannerWidth;

	private Cursor mCursor;

	private Account.Indices mIndices;
	private int mBannerWidth;
	private long mDefaultAccountId;

	public AccountsDrawerAdapter(final Context context) {
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mImageLoader = app.getImageLoaderWrapper();
		mInflater = LayoutInflater.from(context);
		mDefaultBannerWidth = context.getResources().getDisplayMetrics().widthPixels;
	}

	@Override
	public AccountAction getChild(final int groupPosition, final int childPosition) {
		final Account account = getGroup(groupPosition);
		final boolean is_default = mDefaultAccountId == account.account_id;
		return is_default ? DEFAULT_ACCOUNT_ACTIONS[childPosition] : ACCOUNT_ACTIONS[childPosition];
	}

	@Override
	public long getChildId(final int groupPosition, final int childPosition) {
		return Arrays.hashCode(new long[] { getGroupId(groupPosition), getChild(groupPosition, childPosition).id });
	}

	@Override
	public int getChildrenCount(final int groupPosition) {
		final Account account = getGroup(groupPosition);
		final boolean is_default = mDefaultAccountId == account.account_id;
		return is_default ? DEFAULT_ACCOUNT_ACTIONS.length : ACCOUNT_ACTIONS.length;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild,
			final View convertView, final ViewGroup parent) {
		final View view = convertView != null ? convertView : mInflater.inflate(CHILD_LAYOUT, null);
		final AccountAction action = getChild(groupPosition, childPosition);
		final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
		text1.setText(action.name);
		icon.setImageResource(action.icon);
		return view;
	}

	@Override
	public Account getGroup(final int groupPosition) {
		if (mCursor == null || mCursor.isClosed()) return null;
		mCursor.moveToPosition(groupPosition);
		return new Account(mCursor, mIndices);
	}

	@Override
	public int getGroupCount() {
		if (mCursor == null || mCursor.isClosed()) return 0;
		return mCursor.getCount();
	}

	@Override
	public long getGroupId(final int groupPosition) {
		return getGroup(groupPosition).account_id;
	}

	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded, final View convertView,
			final ViewGroup parent) {
		final View view = convertView != null ? convertView : mInflater.inflate(GROUP_LAYOUT, null);
		final int expander_res = isExpanded ? R.drawable.expander_open_holo : R.drawable.expander_close_holo;
		final Object tag = view.getTag();
		final AccountDrawerGroupViewHolder holder;
		if (tag instanceof AccountDrawerGroupViewHolder) {
			holder = (AccountDrawerGroupViewHolder) tag;
		} else {
			holder = new AccountDrawerGroupViewHolder(view);
			view.setTag(holder);
		}
		final Account account = getGroup(groupPosition);
		view.setAlpha(account.is_activated ? ITEM_ACTIVATED_ALPHA : ITEM_INACTIVATED_ALPHA);
		holder.name.setText(account.name);
		holder.screen_name.setText("@" + account.screen_name);
		holder.name_container.drawEnd(account.user_color);
		holder.expand_indicator.setImageResource(expander_res);
		holder.default_indicator.setVisibility(mDefaultAccountId == account.account_id ? View.VISIBLE : View.GONE);
		final int width = mBannerWidth > 0 ? mBannerWidth : mDefaultBannerWidth;
		mImageLoader.displayProfileBanner(holder.profile_banner, account.profile_banner_url, width);
		mImageLoader.displayProfileImage(holder.profile_image, account.profile_image_url);
		return view;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(final int groupPosition, final int childPosition) {
		return true;
	}

	public void setAccountsCursor(final Cursor cursor) {
		mCursor = cursor;
		mIndices = cursor != null ? new Account.Indices(cursor) : null;
		notifyDataSetChanged();
	}

	public void setBannerWidth(final int width) {
		if (mBannerWidth == width) return;
		mBannerWidth = width;
		notifyDataSetChanged();
	}

	public void setDefaultAccountId(final long account_id) {
		if (mDefaultAccountId == account_id) return;
		mDefaultAccountId = account_id;
		notifyDataSetChanged();
	}

	public static class AccountAction {

		public int name, icon, id;

		AccountAction(final int name, final int icon, final int id) {
			this.name = name;
			this.icon = icon;
			this.id = id;
		}
	}

}
