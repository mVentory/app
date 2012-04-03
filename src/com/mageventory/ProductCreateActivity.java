package com.mageventory;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.mageventory.client.MagentoClient2;
import com.mageventory.model.Category;
import com.mageventory.res.LoadOperation;
import com.mageventory.res.ResourceServiceHelper;
import com.mageventory.res.ResourceServiceHelper.OperationObserver;

public class ProductCreateActivity extends BaseActivity implements MageventoryConstants, OperationObserver {
    
    private static final String TAG = "ProductCreateActivity";
    
	MagentoClient2 magentoClient;
	ArrayList<Category> categories;
	ProgressDialog pDialog;
	ArrayAdapter<Category> categories_adapter;
	ArrayAdapter aa;
	MyApplication app;
	
	private int requestId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_product);
		this.setTitle("Mventory: Create Product");
		app = (MyApplication) getApplication();
		this.setTitle("Mventory: Create Product");
		String[] status_options = { "Enable", "Disable" };

		aa = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, status_options);
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categories = app.getCategories();

		categories_adapter = new ArrayAdapter<Category>(getApplicationContext(), android.R.layout.simple_spinner_item,
				categories);
		categories_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		Button create = (Button) findViewById(R.id.createbutton);
		Button refresh = (Button) findViewById(R.id.refreshbutton);
		Spinner spin = (Spinner) findViewById(R.id.status);
		Spinner catSpin = (Spinner) findViewById(R.id.categoriesSpin);

		spin.setAdapter(aa);
		catSpin.setAdapter(categories_adapter);
		create.setOnClickListener(buttonlistener);
		refresh.setOnClickListener(buttonlistener);
		/* if no categories auto refresh*/
		if(categories.size()==0){
			CategoryRetrieve cr = new CategoryRetrieve();
			cr.execute(new Integer[] { 1 });
		}

	}

	private OnClickListener buttonlistener = new OnClickListener() {
		public void onClick(View v) {
			if (v.getId() == R.id.createbutton) {
				if (!verify_form()) {
					Toast.makeText(getApplicationContext(), "All Fields Required.", Toast.LENGTH_SHORT).show();
				} else {
				    createProduct();
					// ProductCreate pr = new ProductCreate();
					// pr.execute(new Integer[] { 1 });
				}

			}
			if (v.getId() == R.id.refreshbutton) {
				CategoryRetrieve cr = new CategoryRetrieve();
				cr.execute(new Integer[] { 1 });

			}
		}
	};

	public boolean verify_form() {

		String name = ((EditText) findViewById(R.id.product_name_input)).getText().toString();
		String price = ((EditText) findViewById(R.id.product_price_input)).getText().toString();
		String description = ((EditText) findViewById(R.id.description_input)).getText().toString();
		String weight = ((EditText) findViewById(R.id.weight_input)).getText().toString();
		Spinner catspin = (Spinner) findViewById(R.id.categoriesSpin);

		if (name.equalsIgnoreCase("") || description.equalsIgnoreCase("") || price.equalsIgnoreCase("")
				|| weight.equalsIgnoreCase("") || catspin.getSelectedItem() == null) {
			return false;
		}

		return true;

	}

	private static ArrayList<Category> getCategorylist(HashMap map) {

		Object[] children = (Object[]) map.get("children");
		if (children.length == 0)
			return new ArrayList<Category>();
		ArrayList<Category> catList = new ArrayList<Category>();
		try {

			for (Object m : children) {
				HashMap cHashmap = (HashMap) m;
				catList.add(new Category(cHashmap.get("name").toString(), cHashmap.get("category_id").toString()));
				catList.addAll(getCategorylist(cHashmap));
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.i("APP_INFO", "dead");
		}
		Log.i("APP_INFO", "" + catList.size());
		return catList;
	}


	private class CategoryRetrieve extends AsyncTask<Integer, Integer, ArrayList<Category>> {
		@Override
		protected void onPreExecute() {
		    showProgressDialog("Loading Categories");
		}

		@Override
		protected ArrayList<Category> doInBackground(Integer... ints) {
			try {
				magentoClient = app.getClient2();
				Object categories;
				categories = magentoClient.catalogCategoryTree();
				HashMap map = (HashMap) categories;
				return getCategorylist(map);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}

		@Override
		protected void onPostExecute(ArrayList<Category> result) {
			dismissProgressDialog();
			if (result != null) {
				app.setCategories(result);
				categories.clear();
				categories.addAll(result);
				categories_adapter.notifyDataSetChanged();
			} else {
				Toast.makeText(getApplicationContext(), "Request Error", Toast.LENGTH_SHORT).show();
			}

			// end execute
		}

	}

	private class CreateProduct extends AsyncTask<Integer, Integer, String> {

	    @Override
		protected String doInBackground(Integer... ints) {
			String name = ((EditText) findViewById(R.id.product_name_input)).getText().toString();
			String description = ((EditText) findViewById(R.id.description_input)).getText().toString();
			String weight = ((EditText) findViewById(R.id.weight_input)).getText().toString();
			String price = ((EditText) findViewById(R.id.product_price_input)).getText().toString();
			Category cat=(Category)((Spinner) findViewById(R.id.categoriesSpin)).getSelectedItem();
			int categorie_id=cat.getId();
			long status_id = ((Spinner) findViewById(R.id.status)).getSelectedItemId();
			String status = (String) aa.getItem((int) status_id);
			Log.d("status", status + "");
			if ("Enable".equalsIgnoreCase(status)) {
				status_id = 1;
			} else {
				status_id = 0;
			}
			Log.d("status s", status_id + "");

			try {
			    // FIXME y: apply attribute set, issue #18
                // Object[] map = (Object[]) magentoClient.execute("product_attribute_set.list");
                // String set_id = (String) ((HashMap) map[0]).get("set_id");
			    
			    final Bundle bundle = new Bundle();
			    bundle.putString(MAGEKEY_PRODUCT_NAME, name);
			    bundle.putString(MAGEKEY_PRODUCT_PRICE, price);
			    bundle.putString(MAGEKEY_PRODUCT_WEBSITE, "1");
			    bundle.putString(MAGEKEY_PRODUCT_DESCRIPTION, description);
			    bundle.putString(MAGEKEY_PRODUCT_SHORT_DESCRIPTION, description);
			    bundle.putString(MAGEKEY_PRODUCT_STATUS, "" + status_id);
			    bundle.putString(MAGEKEY_PRODUCT_WEIGHT, weight);
			    bundle.putSerializable(MAGEKEY_PRODUCT_CATEGORIES, new Object[] { String.valueOf(categorie_id) });
                requestId = ResourceServiceHelper.getInstance().loadResource(ProductCreateActivity.this,
                        RES_CATALOG_PRODUCT_CREATE, null, bundle);
			    return null;
			} catch (Exception e) {
				Log.w(TAG, "" + e);
				return null;
			}
		}
	}
	
	private void showProgressDialog(final String message) {
	    if (pDialog != null) {
	        return;
	    }
	    pDialog = new ProgressDialog(ProductCreateActivity.this);
        pDialog.setMessage(message);
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(true);
        pDialog.show();
	}
	
	private void dismissProgressDialog() {
	    if (pDialog == null) {
	        return;
	    }
	    pDialog.dismiss();
	    pDialog = null;
	}
	
	private void createProduct() {
	    showProgressDialog("Creating product");
	    new CreateProduct().execute();
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        ResourceServiceHelper.getInstance().registerLoadOperationObserver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ResourceServiceHelper.getInstance().unregisterLoadOperationObserver(this);
    }

    @Override
    public void onLoadOperationCompleted(LoadOperation op) {
        if (requestId != op.getOperationRequestId()) {
            // that's not our operation
            return;
        }
        if (op.getException() != null) {
            Toast.makeText(getApplicationContext(), "Action Failed\n" + op.getException().getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        dismissProgressDialog();
        final String ekeyProductId = getString(R.string.ekey_product_id);
        final int productId = op.getExtras().getInt(ekeyProductId, INVALID_PRODUCT_ID);
        final Intent intent = new Intent(getApplicationContext(), ProductDetailsActivity.class);
        intent.putExtra(ekeyProductId, productId);
        startActivity(intent);
    }

}
/*
 * $newProductData = array( 'name' => 'name of product', // websites - Array of
 * website ids to which you want to assign a new product 'websites' => array(1),
 * // array(1,2,3,...) 'short_description' => 'short description', 'description'
 * => 'description', 'status' => 1, 'weight' => 0, 'tax_class_id' => 1,
 * 'categories' => array(3), //3 is the category id 'price' => 12.05 );
 */