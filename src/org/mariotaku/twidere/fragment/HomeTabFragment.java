package org.mariotaku.twidere.fragment;

import java.util.ArrayList;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.ComposeActivity;
import org.mariotaku.twidere.widget.RefreshableListView;
import org.mariotaku.twidere.widget.RefreshableListView.OnRefreshListener;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class HomeTabFragment extends SherlockListFragment implements Constants, OnRefreshListener {

	private ArrayList<String> mItems;
	private RefreshableListView mListView;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		mItems = new ArrayList<String>();
		mItems.add("Diary of a Wimpy Kid 6: Cabin Fever");
		mItems.add("Steve Jobs");
		mItems.add("Inheritance (The Inheritance Cycle)");
		mItems.add("11/22/63: A Novel");
		mItems.add("The Hunger Games");
		mItems.add("The LEGO Ideas Book");
		mItems.add("Explosive Eighteen: A Stephanie Plum Novel");
		mItems.add("Catching Fire (The Second Book of the Hunger Games)");
		mItems.add("Elder Scrolls V: Skyrim: Prima Official Game Guide");
		mItems.add("Death Comes to Pemberley");
		StatusesAdapter adapter = new StatusesAdapter(mItems);
		mListView = (RefreshableListView) getListView();
		mListView.setAdapter(adapter);
		mListView.setOnRefreshListener(this);
	}

	private class StatusesAdapter extends BaseAdapter {

		private ArrayList<String> mItems;

		public StatusesAdapter(ArrayList<String> objects) {
			mItems = objects;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = getLayoutInflater(new Bundle()).inflate(R.layout.tweet_list_item, null,
					false);
			TextView screen_name = (TextView) view.findViewById(R.id.screen_name);
			screen_name.setText("@user" + position);
			TextView tweet_content = (TextView) view.findViewById(R.id.tweet_content);
			tweet_content.setText(getItem(position));
			return view;
		}

		public String getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.timeline, null, false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.compose:
				startActivity(new Intent(getSherlockActivity(), ComposeActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRefresh() {
		new NewDataTask().execute();
	}

	private class NewDataTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}

			return "A new list item";
		}

		@Override
		protected void onPostExecute(String result) {
			mItems.add(0, result);
			// This should be called after refreshing finished
			mListView.completeRefreshing();

			super.onPostExecute(result);
		}
	}
}
