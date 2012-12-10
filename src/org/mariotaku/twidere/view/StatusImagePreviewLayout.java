package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.Collection;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ImageSpec;
import org.mariotaku.twidere.util.LazyImageLoader;

import static android.view.ViewGroup.LayoutParams.*;

public class StatusImagePreviewLayout extends LinearLayout implements View.OnClickListener, ExtendedFrameLayout.OnSizeChangedListener {

	public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {
		//final ImageView v = (ImageView) view.findViewById(R.id.image_preview_item);
		//final ViewGroup.LayoutParams lp = v.getLayoutParams();
		//lp.height = v.getWidth();
		//v.setLayoutParams(lp);
//		v.requestLayout();
	}
	

	public void onClick(View view) {
		if (mListener == null) return;
		mListener.onImageClick((ImageSpec) view.getTag());
	}
	

	private final ArrayList<ImageSpec> mData = new ArrayList<ImageSpec>();
	private final LazyImageLoader mImageLoader;

	public StatusImagePreviewLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mImageLoader = app != null ? app.getPreviewImageLoader() : null;
	}
	
	public void add(ImageSpec spec) {
		if (spec == null) return;
		mData.add(spec);
	}
	
	public void invalidate() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++){
			getChildAt(i).invalidate();
		}
	}
	
	public boolean addAll(Collection<? extends ImageSpec> collection) {
		if (collection == null) return false;
		return 	mData.addAll(collection);
	}
	
	public void show() {
		if (mImageLoader == null) return;
		removeAllViewsInLayout();
		final LayoutInflater inflater = LayoutInflater.from(getContext());
		for (final ImageSpec spec : mData) {
			final ExtendedFrameLayout view = (ExtendedFrameLayout) inflater.inflate(R.layout.image_preview_item, this, false);
			view.setTag(spec);
			view.setOnClickListener(this);
			view.setOnSizeChangedListener(this);
			addView(view);
			final ImageView image_view = (ImageView) view.findViewById(R.id.image_preview_item);
			mImageLoader.displayImage(spec.preview_image_link, image_view);
		}
	}
	
	public void clear() {
		mData.clear();
		removeAllViewsInLayout();
	}
	
	public void setOnImageClickListener(OnImageClickListener listener) {
		mListener = listener;
	}
	
	private OnImageClickListener mListener;
	
	public static interface OnImageClickListener {
		
		public void onImageClick(ImageSpec spec);
		
	}
}
