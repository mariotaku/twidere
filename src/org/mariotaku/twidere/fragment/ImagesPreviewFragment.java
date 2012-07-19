package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.parseURL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.LazyImageLoader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class ImagesPreviewFragment extends BaseFragment implements OnItemClickListener, OnClickListener {

	private Gallery mGallery;
	private ImagesAdapter mAdapter;
	private List<ImageSpec> mData = new ArrayList<ImageSpec>();
	private View mLoadImagesIndicator;
	private SharedPreferences mPreferences;

	public boolean add(ImageSpec spec) {
		return mData.add(spec);
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

	public static class ImageSpec {
		public final String thumbnail_link, image_link;

		public ImageSpec(String thumbnail_link, String image_link) {
			this.thumbnail_link = thumbnail_link;
			this.image_link = image_link;
		}

		@Override
		public String toString() {
			return "ImageSpec(" + thumbnail_link + ", " + image_link + ")";
		}
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
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ImageView view = (ImageView) (convertView instanceof ImageView ? convertView : mInflater.inflate(
					R.layout.images_preview_item, null));
			mImageLoader.displayImage(parseURL(getItem(position).thumbnail_link), view);
			return view;
		}

		public boolean remove(String url_string) {
			final boolean ret = mUrls.remove(url_string);
			notifyDataSetChanged();
			return ret;
		}

	}

}
