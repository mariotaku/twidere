package org.mariotaku.twidere.activity.support;

import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.mariotaku.menucomponent.internal.menu.MenuAdapter;
import org.mariotaku.menucomponent.internal.menu.MenuUtils;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;

public class StatusMenuDialogFragment extends BaseSupportDialogFragment implements OnItemClickListener {

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final FragmentActivity activity = getActivity();
		final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final Bundle args = getArguments();
		final ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
		final int themeRes, accentColor;
		if (activity instanceof IThemedActivity) {
			themeRes = ((IThemedActivity) activity).getThemeResourceId();
			accentColor = ((IThemedActivity) activity).getThemeColor();
		} else {
			themeRes = ThemeUtils.getSettingsThemeResource(activity);
			accentColor = ThemeUtils.getUserThemeColor(activity);
		}
		final Context context = ThemeUtils.getThemedContextForActionIcons(activity, themeRes, accentColor);
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final MenuAdapter adapter = new MenuAdapter(context);
		final ListView listView = new ListView(context);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		builder.setView(listView);
		final int activatedColor = ThemeUtils.getUserThemeColor(getActivity());
		final boolean separateRetweetAction = prefs.getBoolean(KEY_SEPARATE_RETWEET_ACTION,
				DEFAULT_SEPARATE_RETWEET_ACTION);
		final boolean longclickToOpenMenu = prefs.getBoolean(KEY_LONG_CLICK_TO_OPEN_MENU, false);
		final Menu menu = MenuUtils.createMenu(context);
		new MenuInflater(context).inflate(R.menu.action_status, menu);
		setMenuForStatus(getActivity(), menu, status);
		Utils.setMenuItemAvailability(menu, R.id.retweet_submenu, !separateRetweetAction);
		Utils.setMenuItemAvailability(menu, R.id.direct_quote, separateRetweetAction);
		Utils.setMenuItemAvailability(menu, MENU_MULTI_SELECT, longclickToOpenMenu);
		final MenuItem directRetweet = menu.findItem(R.id.direct_retweet);
		if (directRetweet != null) {
			final Drawable icon = directRetweet.getIcon().mutate();
			directRetweet.setVisible(separateRetweetAction && (!status.user_is_protected || isMyRetweet(status)));
			if (isMyRetweet(status)) {
				icon.setColorFilter(activatedColor, PorterDuff.Mode.SRC_ATOP);
				directRetweet.setTitle(R.string.cancel_retweet);
			} else {
				icon.clearColorFilter();
				directRetweet.setTitle(R.string.retweet);
			}
		}
		adapter.setMenu(menu);
		return builder.create();
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		final Fragment parentFragment = getParentFragment();
		final MenuItem item = (MenuItem) parent.getItemAtPosition(position);
		if (item.hasSubMenu()) {

		} else if (parentFragment instanceof OnMenuItemClickListener) {
			((OnMenuItemClickListener) parentFragment).onMenuItemClick(item);
			dismiss();
		}
	}

}
