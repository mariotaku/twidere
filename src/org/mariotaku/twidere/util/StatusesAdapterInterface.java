package org.mariotaku.twidere.util;

public interface StatusesAdapterInterface extends BaseAdapterInterface {

	public ParcelableStatus findItem(long id);

	public void setShowAccountColor(boolean show);

}
