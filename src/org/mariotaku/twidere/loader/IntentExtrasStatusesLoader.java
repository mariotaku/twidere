package org.mariotaku.twidere.loader;
import android.content.Context;
import android.os.Bundle;
import java.util.List;
import org.mariotaku.twidere.model.ParcelableUser;
import java.util.Arrays;
import java.util.Collections;
import org.mariotaku.twidere.model.ParcelableStatus;

public class IntentExtrasStatusesLoader extends ParcelableStatusesLoader {

	private final Bundle mExtras;

	public IntentExtrasStatusesLoader(final Context context, final Bundle extras, final List<ParcelableStatus> data) {
		super(context, data, -1);
		mExtras = extras;
	}

	@Override
	public List<ParcelableStatus> loadInBackground() {
		final List<ParcelableStatus> data = getData();
		if (mExtras != null) {
			final List<ParcelableStatus> users = mExtras.getParcelableArrayList(INTENT_KEY_STATUSES);
			if (users != null) {
				data.addAll(users);
				Collections.sort(data);
			}
		}
		return data;
	}

}
