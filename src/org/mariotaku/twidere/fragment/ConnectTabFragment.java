package org.mariotaku.twidere.fragment;

import java.util.ArrayList;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.widget.RefreshableListView;
import org.mariotaku.twidere.widget.RefreshableListView.OnRefreshListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class ConnectTabFragment extends SherlockListFragment implements Constants,
		OnRefreshListener {

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
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getSherlockActivity(),
				android.R.layout.simple_list_item_1, mItems);
		mListView = (RefreshableListView) getListView();
		mListView.setAdapter(adapter);
		mListView.setOnRefreshListener(this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.connect, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.timeline, null, false);
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
