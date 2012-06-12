package com.mageventory.resprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.mageventory.MageventoryConstants;
import com.mageventory.MyApplication;
import com.mageventory.client.MagentoClient2;
import com.mageventory.job.JobCacheManager;
import com.mageventory.res.ResourceProcessorManager.IProcessor;

public class ProductAttributeFullInfoProcessor implements IProcessor, MageventoryConstants {

	/*
	 * IMPORTANT: There is a duplicated sorting functionality that is supposed
	 * to do the same that this code does but on different data types. Please
	 * make sure these two pieces of code are synchronised (do the sorting the
	 * same way). It's possible we may need to create a common function for this
	 * to avoid confusion.
	 * 
	 * The duplicated functionality is in CustomAttribute class and has a
	 * similar comment to this one.
	 */
	public static void sortOptionsList(List<Object> optionsList) {
		Collections.sort(optionsList, new Comparator<Object>() {

			@Override
			public int compare(Object lhs, Object rhs) {
				String left = (String) (((Map<String, Object>) lhs).get(MAGEKEY_ATTRIBUTE_OPTIONS_LABEL));
				String right = (String) (((Map<String, Object>) rhs).get(MAGEKEY_ATTRIBUTE_OPTIONS_LABEL));

				/* Putting "Other" always at the end of the list. */
				if (left.equalsIgnoreCase("Other") && !right.equalsIgnoreCase("Other"))
					return 1;

				if (right.equalsIgnoreCase("Other") && !left.equalsIgnoreCase("Other"))
					return -1;

				return left.compareToIgnoreCase(right);
			}
		});
	}

	@Override
	public Bundle process(Context context, String[] params, Bundle extras) {

		final MyApplication application = (MyApplication) context.getApplicationContext();
		final MagentoClient2 client = application.getClient2();

		final Object[] atrs = client.productAttributeFullInfo();
		List<Map<String, Object>> atrsList = new ArrayList<Map<String, Object>>();

		if (atrs != null) {
			for (Object elem : atrs) {
				Map<String, Object> attrSetMap = (Map<String, Object>) elem;
				atrsList.add(attrSetMap);

				Object[] customAttrs = (Object[]) attrSetMap.get("attributes");
				String setName = (String) attrSetMap.get(MAGEKEY_ATTRIBUTE_SET_NAME);

				final List<Map<String, Object>> customAttrsList = new ArrayList<Map<String, Object>>(customAttrs.length);
				for (final Object obj : customAttrs) {
					Map<String, Object> attributeMap = (Map<String, Object>) obj;

					final String atrCode = attributeMap.get(MAGEKEY_ATTRIBUTE_CODE_ATTRIBUTE_LIST_REQUEST).toString();

					if (atrCode.endsWith("_") == false) {
						String label = (String) ((Map<String, Object>) (((Object[]) attributeMap.get("frontend_label"))[0]))
								.get("label");

						if (TextUtils.equals(setName, label)) {
							/*
							 * Special attribute that is used for compound names
							 * formatting.
							 */
							attributeMap.put(MAGEKEY_ATTRIBUTE_IS_FORMATTING_ATTRIBUTE, new Boolean(true));
							customAttrsList.add(attributeMap);
						}

						continue;
					}

					customAttrsList.add(attributeMap);

					String type = (String) attributeMap.get(MAGEKEY_ATTRIBUTE_TYPE);

					if (type.equals("multiselect") || type.equals("dropdown") || type.equals("boolean")
							|| type.equals("select")) {
						Object[] options = (Object[]) attributeMap.get(MAGEKEY_ATTRIBUTE_OPTIONS);
						List<Object> optionsList = new ArrayList<Object>();

						for (Object option : options) {
							optionsList.add(option);
						}

						sortOptionsList(optionsList);

						optionsList.toArray(options);
					}
				}
				attrSetMap.put("attributes", customAttrsList);
			}

			JobCacheManager.storeAttributes(atrsList);
		}

		return null;
	}
}
