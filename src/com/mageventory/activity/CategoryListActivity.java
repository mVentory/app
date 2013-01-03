package com.mageventory.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

import com.mageventory.MageventoryConstants;
import com.mageventory.R;
import com.mageventory.R.id;
import com.mageventory.R.layout;
import com.mageventory.R.string;
import com.mageventory.activity.base.BaseListActivity;
import com.mageventory.adapters.SimpleStandardAdapter;
import com.mageventory.job.JobCacheManager;
import com.mageventory.model.Category;
import com.mageventory.res.LoadOperation;
import com.mageventory.res.ResourceServiceHelper;
import com.mageventory.res.ResourceServiceHelper.OperationObserver;
import com.mageventory.settings.SettingsSnapshot;
import com.mageventory.util.DefaultOptionsMenuHelper;
import com.mageventory.util.Util;

public class CategoryListActivity extends BaseListActivity implements MageventoryConstants, OperationObserver {

	public static final int RECENT_CATEGORY_ID = -1000;
	
	private class LoadTask extends AsyncTask<Object, Void, Boolean> {

		private SettingsSnapshot mSettingsSnapshot;
		private Map<String, Object> mData;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			// empty list
			InMemoryTreeStateManager<Category> manager = new InMemoryTreeStateManager<Category>();
			SimpleStandardAdapter adapter = new SimpleStandardAdapter(CategoryListActivity.this, null, manager, 1);
			setListAdapter(adapter);
			
			mSettingsSnapshot = new SettingsSnapshot(CategoryListActivity.this);
		}

		@Override
		protected Boolean doInBackground(Object... args) {
			boolean forceReload = false;
			if (args != null && args.length >= 1 && args[0] instanceof Boolean) {
				forceReload = (Boolean) args[0];
			}
			if (forceReload == false && JobCacheManager.categoriesExist(mSettingsSnapshot.getUrl())) {
				mData = JobCacheManager.restoreCategories(mSettingsSnapshot.getUrl());
				if (mData == null) {
					return Boolean.FALSE;
				}
				
				Object[] children = (Object[]) mData.get(MAGEKEY_CATEGORY_CHILDREN);
				
				if (children != null)
				{
					Map<String, Object> childData = new HashMap<String, Object>();
					childData.put(MAGEKEY_CATEGORY_NAME, "Recent");
					childData.put(MAGEKEY_CATEGORY_ID, "" + RECENT_CATEGORY_ID);
					
					ArrayList<Object> childrenArrayList = new ArrayList<Object>();
					
					childrenArrayList.add(childData);
					
					for (int i=0; i<children.length; i++)
					{
						childrenArrayList.add(children[i]);
					}
					
					mData.put(MAGEKEY_CATEGORY_CHILDREN, childrenArrayList.toArray());
				}
				
				InMemoryTreeStateManager<Category> manager = new InMemoryTreeStateManager<Category>();
				TreeBuilder<Category> treeBuilder = new TreeBuilder<Category>(manager);
				Util.buildCategoryTree(mData, treeBuilder);
				simpleAdapter = new SimpleStandardAdapter(CategoryListActivity.this, selected, manager, 12);

				return Boolean.TRUE;
			} else {
				requestId = resHelper.loadResource(CategoryListActivity.this, RES_CATALOG_CATEGORY_TREE, mSettingsSnapshot);
				return Boolean.FALSE;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				displayTree();
			}
		}
	}

	@SuppressWarnings("unused")
	private static final String TAG = "CategoryListActivity";

	private ResourceServiceHelper resHelper = ResourceServiceHelper.getInstance();
	private int requestId;
	private SimpleStandardAdapter simpleAdapter;
	private boolean dataDisplayed;
	private final Set<Category> selected = new HashSet<Category>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.category_list);

		// title
		this.setTitle("Mventory: Categories");

		// attach listeners
		getListView().setOnItemLongClickListener(myOnItemClickListener);
	}

	private OnItemLongClickListener myOnItemClickListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long categoryId) {
			String categoryName = "";
			
			final Map<String, Object> rootCategory = task.mData;
			if (rootCategory != null && !rootCategory.isEmpty()) {
				for (Category cat : Util.getCategorylist(rootCategory, null)) {
					if (cat.getId() == categoryId) {
						categoryName = cat.getFullName();
					}
				}
			}
			
			Intent intent = new Intent();
			intent.putExtra(getString(R.string.ekey_category_id), (int) categoryId);
			intent.putExtra(getString(R.string.ekey_category_name), categoryName);
			
			setResult(MageventoryConstants.RESULT_SUCCESS, intent);
			finish();
			
			return true;
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		resHelper.registerLoadOperationObserver(this);
		if (!dataDisplayed) {
			loadData();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		resHelper.unregisterLoadOperationObserver(this);
	}

	private void displayTree() {
		if (simpleAdapter != null) {
			if (simpleAdapter == getListAdapter()) {
				simpleAdapter.notifyDataSetChanged();
			} else {
				setListAdapter(simpleAdapter);
			}
		}
		dataDisplayed = true;
	}

	@Override
	public void onLoadOperationCompleted(LoadOperation op) {
		if (requestId != op.getOperationRequestId()) {
			return;
		}
		if (op.getException() != null) {
			Toast.makeText(this, "" + op.getException().getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
		loadData();
	}

	private void loadData() {
		loadData(false);
	}

	private LoadTask task;

	private void loadData(boolean force) {
		dataDisplayed = false;
		if (task != null) {
			task.cancel(true);
		}
		task = new LoadTask();
		task.execute(force);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_refresh) {
			loadData(true);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}