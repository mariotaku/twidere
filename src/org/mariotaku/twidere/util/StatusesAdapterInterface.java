package org.mariotaku.twidere.util;

import android.widget.ListAdapter;

public interface StatusesAdapterInterface extends ListAdapter {

	public ParcelableStatus findItem(long id);

	public void setDisplayName(boolean display);

	public void setDisplayProfileImage(boolean display);

	public void setShowAccountColor(boolean show);

	public void setShowLastItemAsGap(boolean gap);

	public void setStatusesTextSize(float text_size);
}
