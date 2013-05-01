package org.mariotaku.twidere.loader;
import android.content.Context;
import android.os.Bundle;
import java.util.List;
import org.mariotaku.twidere.model.ParcelableUser;
import java.util.Arrays;
import java.util.Collections;

public class IntentExtrasUsersLoader extends ParcelableUsersLoader {

	private final Bundle mExtras;

	public IntentExtrasUsersLoader(final Context context, final Bundle extras, final List<ParcelableUser> data) {
		super(context, data);
		mExtras = extras;
	}

	@Override
	public List<ParcelableUser> loadInBackground() {
		final List<ParcelableUser> data = getData();
		if (mExtras != null) {
			final List<ParcelableUser> users = mExtras.getParcelableArrayList(INTENT_KEY_USERS);
			if (users != null) {
				data.addAll(users);
				Collections.sort(data);
			}
		}
		return data;
	}

}
