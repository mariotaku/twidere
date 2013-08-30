package org.mariotaku.twidere.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.PreviewImage;
import org.mariotaku.twidere.util.ImageLoaderWrapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class PreviewPagerAdapter extends PagerAdapter implements Constants, View.OnClickListener, ImageLoadingListener {

	private final List<PreviewImage> mImages;
	private final ImageLoaderWrapper mImageLoader;
	private final SharedPreferences mPreferences;
	private final LayoutInflater mInflater;

	private OnImageClickListener mListener;
	private boolean mIsPossiblySensitive;

	public PreviewPagerAdapter(final Context context) {
		mInflater = LayoutInflater.from(context);
		mImageLoader = ((TwidereApplication) context.getApplicationContext()).getImageLoaderWrapper();
		mImages = new ArrayList<PreviewImage>();
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}

	public boolean addAll(final Collection<PreviewImage> data, final boolean is_possibly_sensitive) {
		mIsPossiblySensitive = is_possibly_sensitive;
		final boolean ret = mImages.addAll(data);
		notifyDataSetChanged();
		return ret;
	}

	public void clear() {
		mImages.clear();
		notifyDataSetChanged();
	}

	@Override
	public void destroyItem(final ViewGroup container, final int position, final Object object) {
		container.removeView((View) object);
	}

	@Override
	public int getCount() {
		return mImages.size();
	}

	@Override
	public Object instantiateItem(final ViewGroup container, final int position) {
		final PreviewImage spec = mImages.get(position);
		final View view = mInflater.inflate(R.layout.image_preview_item, container, false);
		((ViewPager) container).addView(view, 0);
		final ImageView image_view = (ImageView) view.findViewById(R.id.image_preview_item);
		image_view.setTag(spec);
		image_view.setOnClickListener(this);
		if (mIsPossiblySensitive && !mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_SENSITIVE_CONTENTS, false)) {
			view.findViewById(R.id.image_preview_progress).setVisibility(View.GONE);
			image_view.setImageResource(R.drawable.image_preview_nsfw);
		} else {
			mImageLoader.displayPreviewImage(image_view, spec.image_preview_url, this);
		}
		return view;
	}

	@Override
	public boolean isViewFromObject(final View view, final Object object) {
		return view == object;
	}

	@Override
	public void onClick(final View view) {
		if (mListener == null) return;
		mListener.onImageClick((PreviewImage) view.getTag());
	}

	@Override
	public void onLoadingCancelled(final String url, final View view) {
	}

	@Override
	public void onLoadingComplete(final String url, final View view, final Bitmap bitmap) {
		final View parent = (View) view.getParent();
		final View progress = parent.findViewById(R.id.image_preview_progress);
		if (progress != null) {
			progress.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoadingFailed(final String url, final View view, final FailReason reason) {
	}

	@Override
	public void onLoadingProgressChanged(final String imageUri, final View view, final int current, final int total) {
		if (total == 0) return;
		final View parent = (View) view.getParent();
		final ProgressBar progress = (ProgressBar) parent.findViewById(R.id.image_preview_progress);
		if (progress != null) {
			progress.setIndeterminate(false);
			progress.setProgress(100 * current / total);
		}
	}

	@Override
	public void onLoadingStarted(final String url, final View view) {
		final View parent = (View) view.getParent();
		final ProgressBar progress = (ProgressBar) parent.findViewById(R.id.image_preview_progress);
		if (progress != null) {
			progress.setVisibility(View.VISIBLE);
			progress.setIndeterminate(true);
			progress.setMax(100);
		}
	}

	public void setOnImageClickListener(final OnImageClickListener listener) {
		mListener = listener;
	}

	public static interface OnImageClickListener {

		public void onImageClick(PreviewImage spec);

	}
}