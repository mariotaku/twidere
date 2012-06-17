package org.mariotaku.twidere.util;

import android.widget.ListAdapter;

public interface BaseAdapterInterface extends ListAdapter {

	public void notifyDataSetChanged();

	public void setDisplayName(boolean display);

	public void setDisplayProfileImage(boolean display);

	public void setShowLastItemAsGap(boolean gap);

	public void setTextSize(float text_size);

}
