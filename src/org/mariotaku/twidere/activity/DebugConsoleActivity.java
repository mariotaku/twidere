package org.mariotaku.twidere.activity;


public class DebugConsoleActivity extends BaseActivity/*
													 * implements
													 * ActionBar.TabListener
													 */{

	// private ActionBar mActionBar;
	//
	// private TabInfo mLastTab;
	//
	// @Override
	// public void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	// mActionBar = getActionBar();
	// mActionBar.setDisplayHomeAsUpEnabled(true);
	// mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	// mActionBar.addTab(makeTab("Running tasks", RunningTasksFragment.class));
	// mActionBar.addTab(makeTab("test", Fragment.class));
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case MENU_HOME:
	// finish();
	// break;
	// }
	// return super.onOptionsItemSelected(item);
	// }
	//
	// @Override
	// public void onTabReselected(Tab tab, FragmentTransaction ft) {
	//
	// }
	//
	// @Override
	// public void onTabSelected(Tab tab, FragmentTransaction ft) {
	// Object tag = tab.getTag();
	//
	// if (tag instanceof TabInfo) {
	// TabInfo newTab = (TabInfo) tag;
	//
	// if (mLastTab != newTab) {
	// if (mLastTab != null) {
	// if (mLastTab.fragment != null) {
	// ft.detach(mLastTab.fragment);
	// }
	// }
	// if (newTab != null) {
	// if (newTab.fragment == null) {
	// newTab.fragment = Fragment.instantiate(this, newTab.cls.getName());
	// ft.add(android.R.id.content, newTab.fragment);
	// } else {
	// ft.attach(newTab.fragment);
	// }
	// }
	//
	// mLastTab = newTab;
	// getSupportFragmentManager().executePendingTransactions();
	// }
	// }
	//
	// }
	//
	// @Override
	// public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	//
	// }
	//
	// private Tab makeTab(String text, Class<? extends Fragment> cls) {
	// return mActionBar.newTab().setText(text).setTag(new
	// TabInfo(cls)).setTabListener(this);
	// }
	//
	// public static class RunningTasksFragment extends BaseListFragment {
	//
	// private AsyncTaskManager mAsyncTaskManager;
	//
	// private TaskListAdapter mAdapter;
	//
	// @Override
	// public void onActivityCreated(Bundle savedInstanceState) {
	// mAsyncTaskManager = AsyncTaskManager.getInstance();
	// super.onActivityCreated(savedInstanceState);
	// mAdapter = new TaskListAdapter(getActivity(),
	// mAsyncTaskManager.getTaskList());
	// setListAdapter(mAdapter);
	// }
	//
	// private static class TaskListAdapter extends
	// ArrayAdapter<ManagedAsyncTask<?>> {
	//
	// public TaskListAdapter(Context context, List<ManagedAsyncTask<?>>
	// objects) {
	// super(context, android.R.layout.simple_list_item_1, objects);
	// }
	//
	// }
	//
	// }
	//
	// private static final class TabInfo {
	// private final Class<?> cls;
	// private Fragment fragment;
	//
	// public TabInfo(Class<? extends Fragment> cls) {
	// this.cls = cls;
	// }
	// }
}
