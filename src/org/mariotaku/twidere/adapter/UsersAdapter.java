package org.mariotaku.twidere.adapter;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.ProfileImageLoader;
import org.mariotaku.twidere.util.UserViewHolder;

import twitter4j.User;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class UsersAdapter extends ArrayAdapter<User> {

	private final ProfileImageLoader mImageLoader;
	private boolean mDisplayProfileImage;
	private float mTextSize;
	private boolean mShowLastAsGap;

	public UsersAdapter(Context context) {
		super(context, R.layout.user_list_item, R.id.bio);
		TwidereApplication application = (TwidereApplication) context.getApplicationContext();
		mImageLoader = application.getProfileImageLoader();
		application.getServiceInterface();
	}

	public User findItem(long id) {
		for (int i = 0; i < getCount(); i++) {
			if (getItemId(i) == id) return getItem(i);
		}
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		Object tag = view.getTag();
		UserViewHolder holder = null;
		if (tag instanceof UserViewHolder) {
			holder = (UserViewHolder) tag;
		} else {
			holder = new UserViewHolder(view);
			view.setTag(holder);
		}
		boolean show_gap = mShowLastAsGap && position == getCount() - 1;
		if (!show_gap) {
			final User user = getItem(position);
			holder.setTextSize(mTextSize);

			holder.screen_name.setText(user.getScreenName());
			holder.user_name.setText(user.getName());
			holder.bio.setText(user.getDescription());
			holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				mImageLoader.displayImage(user.getProfileImageURL(), holder.profile_image);
			}
		}
		return view;
	}

	public void setDisplayProfileImage(boolean display) {
		mDisplayProfileImage = display;
	}

	public void setShowLastAsGap(boolean show_gap) {
		mShowLastAsGap = show_gap;
	}

	public void setTextSize(float text_size) {
		mTextSize = text_size;
	}

}