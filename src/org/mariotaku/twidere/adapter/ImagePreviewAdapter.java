package org.mariotaku.twidere.adapter;

import java.util.Collection;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.PreviewImage;
import org.mariotaku.twidere.util.ImageLoaderWrapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class ImagePreviewAdapter extends ArrayAdapter<PreviewImage> implements Constants, ImageLoadingListener {

	private final ImageLoaderWrapper mImageLoader;
	private final SharedPreferences mPreferences;

	private boolean mIsPossiblySensitive;

	public ImagePreviewAdapter(final Context context) {
		super(context, R.layout.image_preview_item);
		mImageLoader = ((TwidereApplication) context.getApplicationContext()).getImageLoaderWrapper();
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}

	public void addAll(final Collection<PreviewImage> data, final boolean is_possibly_sensitive) {
		mIsPossiblySensitive = is_possibly_sensitive;
		addAll(data);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final PreviewImage spec = getItem(position);
		final ImageView image_view = (ImageView) view.findViewById(R.id.image_preview_item);
		image_view.setTag(spec);
		if (mIsPossiblySensitive && !mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_SENSITIVE_CONTENTS, false)) {
			view.findViewById(R.id.image_preview_progress).setVisibility(View.GONE);
			image_view.setBackgroundResource(R.drawable.image_preview_nsfw);
		} else {
			image_view.setBackgroundResource(0);
			mImageLoader.displayPreviewImage(image_view, spec.image_preview_url, this);
		}
		return view;
	}

	@Override
	public void onLoadingCancelled(final String url, final View view) {
	}

	@Override
	public void onLoadingComplete(final String url, final View view, final Bitmap bitmap) {
		if (view == null) return;
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
		if (view == null || total == 0) return;
		final View parent = (View) view.getParent();
		final ProgressBar progress = (ProgressBar) parent.findViewById(R.id.image_preview_progress);
		if (progress != null) {
			progress.setIndeterminate(false);
			progress.setProgress(100 * current / total);
		}
	}

	@Override
	public void onLoadingStarted(final String url, final View view) {
		if (view == null) return;
		final View parent = (View) view.getParent();
		final ProgressBar progress = (ProgressBar) parent.findViewById(R.id.image_preview_progress);
		if (progress != null) {
			progress.setVisibility(View.VISIBLE);
			progress.setIndeterminate(true);
			progress.setMax(100);
		}
	}

}