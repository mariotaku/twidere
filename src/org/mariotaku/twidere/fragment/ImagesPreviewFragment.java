package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.parseURL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ImageSpec;
import org.mariotaku.twidere.util.LazyImageLoader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class ImagesPreviewFragment extends BaseFragment implements OnItemClickListener, OnClickListener, OnTouchListener {

	private static final long TICKER_DURATION = 5000L;
	
	private Gallery mGallery;
	private ImagesAdapter mAdapter;
	private List<ImageSpec> mData = new ArrayList<ImageSpec>();
	private View mLoadImagesIndicator;
	private SharedPreferences mPreferences;
	private Handler mHandler;
	private Runnable mTicker;

	public boolean add(ImageSpec spec) {
		return spec != null ? mData.add(spec) : false;
	}

	public boolean add(String thumbnail_url, String image_url) {
		return mData.add(new ImageSpec(thumbnail_url, image_url));
	}

	public void clear() {
		mData.clear();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		super.onActivityCreated(savedInstanceState);
		mAdapter = new ImagesAdapter(getApplication());
		mGallery.setAdapter(mAdapter);
		mGallery.setOnItemClickListener(this);
		mLoadImagesIndicator.setOnClickListener(this);
		if (mData.size() > 0 && mPreferences.getBoolean(PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY, false)) {
			show();
		}
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

	public boolean remove(String url_string) {
		if (mAdapter == null) return false;
		return mAdapter.remove(url_string);
	}

	public void show() {
		if (mAdapter == null || !isAdded()) return;
		mAdapter.clear();
		mAdapter.addAll(mData);
		mLoadImagesIndicator.setVisibility(View.GONE);
	}

	private class ImagesAdapter extends BaseAdapter {

		private final List<ImageSpec> mUrls = new ArrayList<ImageSpec>();
		private final LazyImageLoader mImageLoader;
		private final LayoutInflater mInflater;

		public ImagesAdapter(TwidereApplication context) {
			mImageLoader = context.getPreviewImageLoader();
			mInflater = LayoutInflater.from(context);
		}

		public boolean addAll(Collection<? extends ImageSpec> url_strings) {
			final boolean ret = mUrls.addAll(url_strings);
			notifyDataSetChanged();
			return ret;
		}

		public void clear() {
			mUrls.clear();
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mUrls.size();
		}

		@Override
		public ImageSpec getItem(int position) {
			return mUrls.get(position);
		}

		@Override
		public long getItemId(int position) {
			final ImageSpec spec = getItem(position);
			return spec != null ? spec.hashCode() : 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ImageView view = (ImageView) (convertView instanceof ImageView ? convertView : mInflater.inflate(
					R.layout.images_preview_item, null));
			final ImageSpec spec = getItem(position);
			mImageLoader.displayImage(spec != null ? parseURL(spec.thumbnail_link) : null, view);
			return view;
		}

		public boolean remove(String url_string) {
			final boolean ret = mUrls.remove(url_string);
			notifyDataSetChanged();
			return ret;
		}

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
	
	private volatile boolean mBusy, mTickerStopped;

}
