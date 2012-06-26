package org.mariotaku.twidere.adapter;

import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.BaseAdapterInterface;
import org.mariotaku.twidere.util.ParcelableUser;
import org.mariotaku.twidere.util.ProfileImageLoader;
import org.mariotaku.twidere.util.UserViewHolder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class UsersAdapter extends ArrayAdapter<ParcelableUser> implements BaseAdapterInterface {

	private final ProfileImageLoader mImageLoader;
	private boolean mDisplayProfileImage, mShowLastItemAsGap, mDisplayName;
	private float mTextSize;

	public UsersAdapter(Context context) {
		super(context, R.layout.user_list_item, R.id.description);
		final TwidereApplication application = (TwidereApplication) context.getApplicationContext();
		mImageLoader = application.getProfileImageLoader();
		application.getServiceInterface();
	}

	public ParcelableUser findItem(long id) {
		for (int i = 0; i < getCount(); i++) {
			if (getItemId(i) == id) return getItem(i);
		}
		return null;
	}
	
	public ParcelableUser findItemByUserId(long user_id) {
		for (int i = 0; i < getCount(); i++) {
			ParcelableUser item = getItem(i);
			if (item.user_id == user_id) return item;
		}
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final Object tag = view.getTag();
		UserViewHolder holder = null;
		if (tag instanceof UserViewHolder) {
			holder = (UserViewHolder) tag;
		} else {
			holder = new UserViewHolder(view);
			view.setTag(holder);
		}
		final boolean show_gap = mShowLastItemAsGap && position == getCount() - 1;
		holder.setShowAsGap(show_gap);
		if (!show_gap) {
			final ParcelableUser user = getItem(position);
			holder.setTextSize(mTextSize);
			holder.name.setText(mDisplayName ? user.name : user.screen_name);
			holder.name.setCompoundDrawablesWithIntrinsicBounds(user.is_protected ? R.drawable.ic_tweet_stat_is_protected : 0, 0, 0, 0);
			holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				mImageLoader.displayImage(user.profile_image_url, holder.profile_image);
			}
		}
		return view;
	}

	public boolean isGap(int position) {
		return mShowLastItemAsGap && position == getCount() - 1;
	}

	@Override
	public void setDisplayName(boolean display) {
		if (display != mDisplayName) {
			mDisplayName = display;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setDisplayProfileImage(boolean display) {
		if (display != mDisplayProfileImage) {
			mDisplayProfileImage = display;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setShowLastItemAsGap(boolean gap) {
		if (gap != mShowLastItemAsGap) {
			mShowLastItemAsGap = gap;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setTextSize(float text_size) {
		if (text_size != mTextSize) {
			mTextSize = text_size;
			notifyDataSetChanged();
		}
	}
	
	public void setData(List<ParcelableUser> data) {
		setData(data, false);
	}
	
	public void setData(List<ParcelableUser> data, boolean clear_old) {
		if (clear_old) {
			clear();
		}
		if (data == null) return;
		for (ParcelableUser user : data) {
			if (clear_old || findItemByUserId(user.user_id) == null) {
				add(user);
			}
		}
	}

}