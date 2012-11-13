package org.mariotaku.twidere.loader;

import java.util.List;

import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.SynchronizedStateSavedList;

import android.content.Context;

public final class DummyParcelableStatusesLoader extends ParcelableStatusesLoader {

	public DummyParcelableStatusesLoader(final Context context, final long account_id, final List<ParcelableStatus> data) {
		super(context, account_id, data, null, false);
	}

	@Override
	public SynchronizedStateSavedList<ParcelableStatus, Long> loadInBackground() {
		return new SynchronizedStateSavedList<ParcelableStatus, Long>();
	}

}
