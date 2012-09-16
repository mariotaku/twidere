package org.mariotaku.twidere.loader;

import java.util.Collections;
import java.util.List;

import org.mariotaku.twidere.model.ParcelableStatus;

import android.content.Context;

public final class DummyParcelableStatusesLoader extends ParcelableStatusesLoader {

	public DummyParcelableStatusesLoader(Context context, long account_id, List<ParcelableStatus> data) {
		super(context, account_id, data, null, false);
	}

	@Override
	public List<ParcelableStatus> loadInBackground() {
		return Collections.emptyList();
	}

}
