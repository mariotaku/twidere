package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.parseURL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ImageSpec;
import org.mariotaku.twidere.util.LazyImageLoader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.content.Context;

public class ImagesPreviewFragment extends BaseFragment implements OnItemClickListener, OnClickListener,
		OnTouchListener {

	private static final long TICKER_DURATION = 5000L;

	private Gallery mGallery;
	private ImagesAdapter mAdapter;
	private View mLoadImagesIndicator;
	private Handler mHandler;
	private Runnable mTicker;

	private volatile boolean mBusy, mTickerStopped;

	private final List<ImageSpec> mData = new ArrayList<ImageSpec>();

	public boolean addAll(Collection<? extends ImageSpec> images) {
		mData.clear();
		return images != null && mData.addAll(images);
	}

	public void clear() {
		mData.clear();
		update();
		if (mLoadImagesIndicator != null) {
			mLoadImagesIndicator.setVisibility(View.VISIBLE);
		}
		if (mGallery != null) {
			mGallery.setVisibility(View.GONE);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new ImagesAdapter(getActivity());
		mGallery.setAdapter(mAdapter);
		mGallery.setOnItemClickListener(this);
		mLoadImagesIndicator.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.load_images: {
				show();
				break;
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.images_preview, null, false);
		mGallery = (Gallery) view.findViewById(R.id.preview_gallery);
		mGallery.setOnTouchListener(this);
		mLoadImagesIndicator = view.findViewById(R.id.load_images);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final ImageSpec spec = mAdapter.getItem(position);
		if (spec == null) return;
		final Intent intent = new Intent(INTENT_ACTION_VIEW_IMAGE, Uri.parse(spec.image_link));
		intent.setPackage(getActivity().getPackageName());
		startActivity(intent);
	}

	@Override
	public void onStart() {
		super.onStart();
		mTickerStopped = false;
		mHandler = new Handler();

		mTicker = new Runnable() {

			@Override
			public void run() {
				if (mTickerStopped) return;
				if (mGallery != null && !mBusy) {
					mAdapter.notifyDataSetChanged();
				}
				final long now = SystemClock.uptimeMillis();
				final long next = now + TICKER_DURATION - now % TICKER_DURATION;
				mHandler.postAtTime(mTicker, next);
			}
		};
		mTicker.run();
	}

	@Override
	public void onStop() {
		mTickerStopped = true;
		super.onStop();
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mBusy = true;
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mBusy = false;
				break;
		}
		return false;
	}

	public void show() {
		if (mAdapter == null) return;
		update();
		mLoadImagesIndicator.setVisibility(View.GONE);
		mGallery.setVisibility(View.VISIBLE);
	}

	public void update() {
		if (mAdapter == null) return;
		mAdapter.clear();
		mAdapter.addAll(mData);
	}

	static class ImagesAdapter extends BaseAdapter {

		private final List<ImageSpec> mImages = new ArrayList<ImageSpec>();
		private final LazyImageLoader mImageLoader;
		private final LayoutInflater mInflater;

		public ImagesAdapter(Context context) {
			mImageLoader = TwidereApplication.getInstance(context).getPreviewImageLoader();
			mInflater = LayoutInflater.from(context);
		}

		public boolean addAll(Collection<? extends ImageSpec> images) {
			final boolean ret = images != null && mImages.addAll(images);
			notifyDataSetChanged();
			return ret;
		}

		public void clear() {
			mImages.clear();
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mImages.size();
		}

		@Override
		public ImageSpec getItem(int position) {
			return mImages.get(position);
		}

		@Override
		public long getItemId(int position) {
			final ImageSpec spec = getItem(position);
			return spec != null ? spec.hashCode() : 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = convertView != null ? convertView : mInflater.inflate(R.layout.images_preview_item, null);
			final ImageView image = (ImageView) view.findViewById(R.id.image);
			final ImageSpec spec = getItem(position);
			mImageLoader.displayImage(spec != null ? parseURL(spec.thumbnail_link) : null, image);
			return view;
		}

	}

}
