/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.adapter.iface;

import org.mariotaku.twidere.model.ParcelableStatus;

public interface IStatusesAdapter<Data> extends IBaseAdapter {

	public long findItemIdByPosition(final int position);
	
	public int findItemPositionByStatusId(final long status_id);
	
	public ParcelableStatus getStatus(int position);

	public void setData(Data data);

	public void setDisplaySensitiveContents(boolean display);

	public void setFastTimelineProcessingEnabled(boolean enabled);

	public void setGapDisallowed(boolean disallowed);

	public void setIndicateMyStatusDisabled(boolean disable);

	public void setInlineImagePreviewDisplayOption(String option);

	public void setLinkHightlightingEnabled(boolean enable);

	public void setLinkUnderlineOnly(boolean underline_only);
	
	public void setMentionsHightlightDisabled(boolean disable);

	public void setMultiSelectEnabled(boolean multi);

	public void setShowAbsoluteTime(boolean show);

	public void setShowAccountColor(boolean show);

}
