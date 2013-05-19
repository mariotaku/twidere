package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import java.util.ArrayList;
import java.util.Collection;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ImageSpec;
import org.mariotaku.twidere.util.ImageLoaderWrapper;

public class StatusImagePreviewLayout extends LinearLayout implements Constants, View.OnClickListener,
		ExtendedFrameLayout.OnSizeChangedListener, ImageLoadingListener {

	private final ArrayList<ImageSpec> mData = new ArrayList<ImageSpec>();

	private final ImageLoaderWrapper mImageLoader;

	private OnImageClickListener mListener;

	private boolean mIsPossiblySensitive;

	public StatusImagePreviewLayout(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mImageLoader = app != null ? app.getImageLoaderWrapper() : null;
	}

	public void add(final ImageSpec spec) {
		if (spec == null) return;
		mData.add(spec);
	}

	public boolean addAll(final Collection<? extends ImageSpec> collection) {
		if (collection == null) return false;
		return mData.addAll(collection);
	}

	public void clear() {
		mData.clear();
		removeAllViewsInLayout();
	}

	@Override
	public void invalidate() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).invalidate();
		}
	}

	@Override
	public void onClick(final View view) {
		if (mListener == null) return;
		mListener.onImageClick((ImageSpec) view.getTag());
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

	@Override
	public void onLoadingFailed(final String url, final View view, final FailReason reason) {
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
	public void onLoadingCancelled(final String url, final View view) {
	}

	@Override
	public void onLoadingProgressChanged(String imageUri, View view, int current, int total) {
		if (total == 0) return;
		final View parent = (View) view.getParent();
		final ProgressBar progress = (ProgressBar) parent.findViewById(R.id.image_preview_progress);
		if (progress != null) {
			progress.setIndeterminate(false);
			progress.setProgress(100 * current / total);
		}
	}
	
	@Override
	public void onSizeChanged(final View view, final int w, final int h, final int oldw, final int oldh) {
		// final ImageView v = (ImageView)
		// view.findViewById(R.id.image_preview_item);
		// final ViewGroup.LayoutParams lp = v.getLayoutParams();
		// lp.height = v.getWidth();
		// v.setLayoutParams(lp);
		// v.requestLayout();
	}

	public void setOnImageClickListener(final OnImageClickListener listener) {
		mListener = listener;
	}

	public void show(final boolean is_possibly_sensitive) {
		mIsPossiblySensitive = is_possibly_sensitive;
		if (mImageLoader == null) return;
		removeAllViewsInLayout();
		final Context context = getContext();
		final LayoutInflater inflater = LayoutInflater.from(context);
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		for (final ImageSpec spec : mData) {
			final ExtendedFrameLayout view = (ExtendedFrameLayout) inflater.inflate(R.layout.image_preview_item, this,
					false);
			view.setOnSizeChangedListener(this);
			addView(view);
			final ImageView image_view = (ImageView) view.findViewById(R.id.image_preview_item);
			image_view.setTag(spec);
			image_view.setOnClickListener(this);
			if (mIsPossiblySensitive && !prefs.getBoolean(PREFERENCE_KEY_DISPLAY_SENSITIVE_CONTENTS, false)) {
				view.findViewById(R.id.image_preview_progress).setVisibility(View.GONE);
				image_view.setImageResource(R.drawable.image_preview_nsfw);
			} else {
				mImageLoader.displayPreviewImage(image_view, spec.image_preview_url, this);
			}
		}
	}

	public static interface OnImageClickListener {

		public void onImageClick(ImageSpec spec);

	}
}
