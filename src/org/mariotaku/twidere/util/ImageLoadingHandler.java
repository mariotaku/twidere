package org.mariotaku.twidere.util;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;

import org.mariotaku.twidere.R;

import java.util.HashMap;
import java.util.Map;

public class ImageLoadingHandler implements ImageLoadingListener, ImageLoadingProgressListener {

	private final Map<View, String> mLoadingUris = new HashMap<View, String>();

	public String getLoadingUri(final View view) {
		return mLoadingUris.get(view);
	}

	@Override
	public void onLoadingCancelled(final String imageUri, final View view) {
		if (view == null || imageUri == null || imageUri.equals(mLoadingUris.get(view))) return;
		mLoadingUris.remove(view);
		final View parent = (View) view.getParent();
		final View progress = parent.findViewById(R.id.image_preview_progress);
		if (progress != null) {
			progress.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoadingComplete(final String imageUri, final View view, final Bitmap bitmap) {
		if (view == null) return;
		mLoadingUris.remove(view);
		final View parent = (View) view.getParent();
		final View progress = parent.findViewById(R.id.image_preview_progress);
		if (progress != null) {
			progress.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoadingFailed(final String imageUri, final View view, final FailReason reason) {
		if (view == null) return;
		if (view instanceof ImageView) {
			((ImageView) view).setImageDrawable(null);
			view.setBackgroundResource(R.drawable.image_preview_refresh);
		}
		mLoadingUris.remove(view);
		final View parent = (View) view.getParent();
		final View progress = parent.findViewById(R.id.image_preview_progress);
		if (progress != null) {
			progress.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoadingStarted(final String imageUri, final View view) {
		if (view == null || imageUri == null || imageUri.equals(mLoadingUris.get(view))) return;
		mLoadingUris.put(view, imageUri);
		final View parent = (View) view.getParent();
		final ProgressBar progress = (ProgressBar) parent.findViewById(R.id.image_preview_progress);
		if (progress != null) {
			progress.setVisibility(View.VISIBLE);
			progress.setIndeterminate(true);
			progress.setMax(100);
		}
	}

	@Override
	public void onProgressUpdate(final String imageUri, final View view, final int current, final int total) {
		if (total == 0 || view == null) return;
		final View parent = (View) view.getParent();
		final ProgressBar progress = (ProgressBar) parent.findViewById(R.id.image_preview_progress);
		if (progress != null) {
			progress.setIndeterminate(false);
			progress.setProgress(100 * current / total);
		}
	}
}
