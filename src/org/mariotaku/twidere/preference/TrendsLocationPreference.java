package org.mariotaku.twidere.preference;

import static org.mariotaku.twidere.util.Utils.getDefaultTwitterInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import twitter4j.Location;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TrendsLocationPreference extends Preference implements Constants, OnPreferenceClickListener,
		OnClickListener {

	private SharedPreferences mPreferences;

	private int mCheckedWoeId = 1;

	private GetAvailableTrendsTask mGetAvailableTrendsTask;

	private final AvailableTrendsAdapter mAdapter;

	private static final Comparator<Location> LOCATION_COMPATATOR = new Comparator<Location>() {

		@Override
		public int compare(final Location object1, final Location object2) {
			return object1.getWoeid() - object2.getWoeid();
		}

	};

	private AlertDialog mDialog;

	public TrendsLocationPreference(final Context context) {
		this(context, null);
	}

	public TrendsLocationPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public TrendsLocationPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mAdapter = new AvailableTrendsAdapter(context);
		setOnPreferenceClickListener(this);
	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		final SharedPreferences.Editor editor = getEditor();
		if (editor == null) return;
		final Location item = mAdapter.getItem(which);
		if (item != null) {
			editor.putInt(PREFERENCE_KEY_LOCAL_TRENDS_WOEID, item.getWoeid());
			editor.commit();
		}
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	@Override
	public boolean onPreferenceClick(final Preference preference) {
		mPreferences = getSharedPreferences();
		if (mPreferences == null) return false;
		mCheckedWoeId = mPreferences.getInt(PREFERENCE_KEY_LOCAL_TRENDS_WOEID, 1);
		if (mGetAvailableTrendsTask != null) {
			mGetAvailableTrendsTask.cancel(false);
		}
		mGetAvailableTrendsTask = new GetAvailableTrendsTask(getContext());
		mGetAvailableTrendsTask.execute();
		return true;
	}

	class AvailableTrendsAdapter extends BaseAdapter {

		private final ArrayList<Location> mData = new ArrayList<Location>();
		private final LayoutInflater mInflater;

		public AvailableTrendsAdapter(final Context context) {
			mInflater = LayoutInflater.from(context);
		}

		public int findItemPosition(final int woeid) {
			final int count = getCount();
			for (int i = 0; i < count; i++) {
				final Location item = getItem(i);
				if (item.getWoeid() == woeid) return i;
			}
			return -1;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Location getItem(final int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(final int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = convertView != null ? convertView : mInflater.inflate(
					android.R.layout.simple_list_item_single_choice, parent, false);
			final TextView text = (TextView) (view instanceof TextView ? view : view.findViewById(android.R.id.text1));
			final Location item = getItem(position);
			if (item != null && text != null) {
				text.setSingleLine();
				text.setText(item.getName());
			}
			return view;
		}

		public void setData(final List<Location> data) {
			mData.clear();
			if (data != null) {
				mData.addAll(data);
			}
			Collections.sort(mData, LOCATION_COMPATATOR);
			notifyDataSetChanged();
		}

	}

	class GetAvailableTrendsTask extends AsyncTask<Void, Void, ResponseList<Location>> implements OnCancelListener {

		private final ProgressDialog mProgress;

		public GetAvailableTrendsTask(final Context context) {
			mProgress = new ProgressDialog(context);
		}

		@Override
		public void onCancel(final DialogInterface dialog) {
			cancel(true);
		}

		@Override
		protected ResponseList<Location> doInBackground(final Void... args) {
			final Twitter twitter = getDefaultTwitterInstance(getContext(), false);
			if (twitter == null) return null;
			try {
				return twitter.getAvailableTrends();
			} catch (final TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(final ResponseList<Location> result) {
			if (mProgress != null && mProgress.isShowing()) {
				mProgress.dismiss();
			}
			mAdapter.setData(result);
			if (result == null) return;
			final AlertDialog.Builder selector_builder = new AlertDialog.Builder(getContext());
			selector_builder.setTitle(getTitle());
			selector_builder.setSingleChoiceItems(mAdapter, mAdapter.findItemPosition(mCheckedWoeId),
					TrendsLocationPreference.this);
			selector_builder.setNegativeButton(android.R.string.cancel, null);
			mDialog = selector_builder.show();
		}

		@Override
		protected void onPreExecute() {
			if (mProgress != null && mProgress.isShowing()) {
				mProgress.dismiss();
			}
			mProgress.setMessage(getContext().getString(R.string.please_wait));
			mProgress.setOnCancelListener(this);
			mProgress.show();
		}

	}
}
