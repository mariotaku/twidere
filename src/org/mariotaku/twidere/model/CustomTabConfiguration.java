package org.mariotaku.twidere.model;

import android.support.v4.app.Fragment;

import org.mariotaku.twidere.Constants;

import java.util.Comparator;
import java.util.Map.Entry;

public final class CustomTabConfiguration implements Constants {

	public static final int FIELD_TYPE_NONE = 0;
	public static final int FIELD_TYPE_USER = 1;
	public static final int FIELD_TYPE_USER_LIST = 2;
	public static final int FIELD_TYPE_TEXT = 3;

	private final int title, icon, secondaryFieldType, secondaryFieldTitle, sortPosition;
	private final boolean accountIdRequired;
	private final Class<? extends Fragment> cls;
	private final String secondaryFieldTextKey;

	public CustomTabConfiguration(final Class<? extends Fragment> cls, final int title, final int icon,
			final boolean account_id_required, final int secondary_field_type, final int sort_position) {
		this(cls, title, icon, account_id_required, secondary_field_type, 0, EXTRA_TEXT, sort_position);
	}

	public CustomTabConfiguration(final Class<? extends Fragment> cls, final int title, final int icon,
			final boolean accountIdRequired, final int secondaryFieldType, final int secondaryFieldTitle,
			final String secondaryFieldTextKey, final int sortPosition) {
		this.cls = cls;
		this.title = title;
		this.icon = icon;
		this.sortPosition = sortPosition;
		this.accountIdRequired = accountIdRequired;
		this.secondaryFieldType = secondaryFieldType;
		this.secondaryFieldTitle = secondaryFieldTitle;
		this.secondaryFieldTextKey = secondaryFieldTextKey;
	}

	public int getDefaultIcon() {
		return icon;
	}

	public int getDefaultTitle() {
		return title;
	}

	public Class<? extends Fragment> getFragmentClass() {
		return cls;
	}

	public String getSecondaryFieldTextKey() {
		return secondaryFieldTextKey;
	}

	public int getSecondaryFieldTitle() {
		return secondaryFieldTitle;
	}

	public int getSecondaryFieldType() {
		return secondaryFieldType;
	}

	public int getSortPosition() {
		return sortPosition;
	}

	public boolean isAccountIdRequired() {
		return accountIdRequired;
	}

	public static class CustomTabConfigurationComparator implements Comparator<Entry<String, CustomTabConfiguration>> {

		public static final CustomTabConfigurationComparator SINGLETON = new CustomTabConfigurationComparator();

		@Override
		public int compare(final Entry<String, CustomTabConfiguration> lhs,
				final Entry<String, CustomTabConfiguration> rhs) {
			return lhs.getValue().getSortPosition() - rhs.getValue().getSortPosition();
		}

	}

}
