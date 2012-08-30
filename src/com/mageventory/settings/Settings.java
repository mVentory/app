package com.mageventory.settings;

import com.mageventory.job.JobCacheManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Settings {
	
	/* Store specific keys. */
	private static final String PROFILE_ID = "profile_id";
	private static final String USER_KEY = "user";
	private static final String PASS_KEY = "pass";
	private static final String URL_KEY = "url";
	private static final String CUSTOMER_VALID_KEY = "customer_valid";
	private static final String PROFILE_DATA_VALID = "profile_data_valid";
	private static final String GOOGLE_BOOK_API_KEY = "api_key";
	private static final String MAX_IMAGE_WIDTH_KEY = "image_width";
	private static final String MAX_IMAGE_HEIGHT_KEY = "image_height";

	/* Keys that are common for all stores and are stored in a common file. */
	private static final String SOUND_CHECKBOX_KEY = "sound_checkbox";
	private static final String SERVICE_CHECKBOX_KEY = "service_checkbox";
	private static final String EXTERNAL_PHOTOS_CHECKBOX_KEY = "external_photos_checkbox";
	private static final String CAMERA_TIME_DIFFERENCE_SECONDS_KEY = "camera_time_difference_seconds";
	private static final String LIST_OF_STORES_KEY = "list_of_stores";
	private static final String CURRENT_STORE_KEY = "current_store_key";
	private static final String NEXT_PROFILE_ID_KEY = "next_profile_id";
	private static final String GALLERY_PHOTOS_DIRECTORY_KEY = "gallery_photos_directory";

	private static String listOfStoresFileName = "list_of_stores.dat";
	private static final String NEW_MODE_STRING = "New profile";
	private static final String NO_STORE_IS_CURRENT = "no store is current";
	
	private SharedPreferences settings;

	private Context context;
	
	public void switchToStoreURL(String url)
	{
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);
		
		boolean assingProfileID = false;
		long nextProfileID = storesPreferences.getLong(NEXT_PROFILE_ID_KEY, 0);

		if (url != null)
		{
			settings = context.getSharedPreferences(JobCacheManager.encodeURL(url), Context.MODE_PRIVATE);
		}
		
		/* Check if a profile id is already assigned to this store url. */
		if (settings.getLong(PROFILE_ID, -1) == -1)
		{
			assingProfileID = true;
		}
		
		if (assingProfileID == true)
		{
			SharedPreferences.Editor edit = settings.edit();
			edit.putLong(PROFILE_ID, nextProfileID);
			edit.commit();
		}
		
		Editor e = storesPreferences.edit();
		e.putString(CURRENT_STORE_KEY, url);
		if (assingProfileID == true)
		{
			e.putLong(NEXT_PROFILE_ID_KEY, nextProfileID + 1);
		}
		e.commit();		
	}

	public String [] getListOfStores(boolean newMode)
	{
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);
		
		String storesString = storesPreferences.getString(LIST_OF_STORES_KEY, null);
		
		if (storesString == null)
		{
			if (newMode)
			{
				return new String [] {NEW_MODE_STRING};
			}
			else
			{
				return new String [0];
			}
		}
		
		if (newMode)
		{
			return (storesString + "\n" + NEW_MODE_STRING).split("\n");	
		}
		else
		{
			return storesString.split("\n");
		}
	}
	
	public int getStoresCount()
	{
		return getListOfStores(false).length;
	}
	
	public void addStore(String url)
	{
		if (storeExists(url))
		{
			return;
		}
		
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);
		
		String storesString = storesPreferences.getString(LIST_OF_STORES_KEY, null);
		
		if (storesString == null)
		{
			storesString = url;
		}
		else
		{
			storesString = storesString + "\n" + url;	
		}
		
		Editor e = storesPreferences.edit();
		e.putString(LIST_OF_STORES_KEY, storesString);
		e.commit();
	}
	
	public void removeStore(String url)
	{
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);
		
		String storesString = null;
		String [] storesList = getListOfStores(false);
		
		for(int i=0; i<storesList.length; i++)
		{
			if (!storesList[i].equals(url))
			{
				if (storesString == null)
				{
					storesString = storesList[i];
				}
				else
				{
					storesString = storesString + "\n" + storesList[i]; 
				}
			}
			else
			{
				SharedPreferences settingsToRemove = context.getSharedPreferences(JobCacheManager.encodeURL(url), Context.MODE_PRIVATE);
				Editor edit = settingsToRemove.edit();
				edit.clear();
				edit.commit();
			}
		}
		
		Editor e = storesPreferences.edit();
		e.putString(LIST_OF_STORES_KEY, storesString);
		e.commit();
	}
	
	public boolean storeExists(String url)
	{
		String [] storesList = getListOfStores(false);
		
		for(int i=0; i<storesList.length; i++)
		{
			if (storesList[i].equals(url))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public String getCurrentStoreUrl()
	{
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);
		
		return storesPreferences.getString(CURRENT_STORE_KEY, NO_STORE_IS_CURRENT);
	}
	
	public int getCurrentStoreIndex()
	{
		String [] stores = getListOfStores(false);
		String currentStore = getCurrentStoreUrl();
		
		for(int i=0; i<stores.length; i++)
		{
			if (stores[i].equals(currentStore))
				return i;
		}
		
		return -1;
	}
	
	public static class ProfileIDNotFoundException extends Exception
	{
		private static final long serialVersionUID = -9041230111429421043L;
	}
	
	/**
	 * @param act
	 *            The context from which to pick SharedPreferences
	 */
	public Settings(Context act) {
		context = act;
		
		settings = act.getSharedPreferences(
				JobCacheManager.encodeURL(getCurrentStoreUrl()), Context.MODE_PRIVATE);
	}
	
	public Settings(Context act, String url) {
		context = act;
		
		settings = act.getSharedPreferences(
				JobCacheManager.encodeURL(url), Context.MODE_PRIVATE);		
	}
	
	public Settings(Context act, long profileID) throws ProfileIDNotFoundException {
		context = act;
		
		String [] storeURLs = getListOfStores(false);
		
		for(String url : storeURLs)
		{
			SharedPreferences sp = act.getSharedPreferences(
				JobCacheManager.encodeURL(url), Context.MODE_PRIVATE);
			
			long pid = sp.getLong(PROFILE_ID, -1);
			
			if (profileID == pid)
			{
				settings = sp;
			}
		}
		
		if (settings == null)
		{
			throw new ProfileIDNotFoundException();
		}
	}

	public void setCameraTimeDifference(int timeDiff) {
		/* Save the time difference in the file that is common for all stores. */
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);

		Editor editor = storesPreferences.edit();
		editor.putInt(CAMERA_TIME_DIFFERENCE_SECONDS_KEY, timeDiff);
		editor.commit();
	}
	
	public int getCameraTimeDifference() {
		/* Get the time difference from the file that is common for all stores. */
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);
		return storesPreferences.getInt(CAMERA_TIME_DIFFERENCE_SECONDS_KEY, 0);
	}
	
	public void setUser(String user) {
		Editor editor = settings.edit();
		editor.putString(USER_KEY, user);
		editor.commit();
	}

	public String getUser() {
		return settings.getString(USER_KEY, "");
	}

	public String getPass() {
		return settings.getString(PASS_KEY, "");
	}

	public void setPass(String pass) {
		Editor editor = settings.edit();
		editor.putString(PASS_KEY, pass);
		editor.commit();
	}

	public String getUrl() {
		return settings.getString(URL_KEY, "");
	}

	public void setUrl(String url) {
		Editor editor = settings.edit();
		editor.putString(URL_KEY, url);
		editor.commit();
	}
	
	public long getProfileID()
	{
		return settings.getLong(PROFILE_ID, -1);
	}

	public String getAPIkey() {
		return settings.getString(GOOGLE_BOOK_API_KEY, "");
	}

	public void setAPIkey(String url) {
		Editor editor = settings.edit();
		editor.putString(GOOGLE_BOOK_API_KEY, url);
		editor.commit();
	}
	
	public String getMaxImageWidth() {
		return settings.getString(MAX_IMAGE_WIDTH_KEY, "");
	}

	public void setMaxImageWidth(String width) {
		Editor editor = settings.edit();
		editor.putString(MAX_IMAGE_WIDTH_KEY, width);
		editor.commit();
	}
	
	public String getMaxImageHeight() {
		return settings.getString(MAX_IMAGE_HEIGHT_KEY, "");
	}

	public void setMaxImageHeight(String height) {
		Editor editor = settings.edit();
		editor.putString(MAX_IMAGE_HEIGHT_KEY, height);
		editor.commit();
	}
	
	public boolean getExternalPhotosCheckBox()
	{
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);
		return storesPreferences.getBoolean(EXTERNAL_PHOTOS_CHECKBOX_KEY, true);
	}
	
	public void setExternalPhotosCheckBox(boolean checked)
	{
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);

		Editor editor = storesPreferences.edit();
		editor.putBoolean(EXTERNAL_PHOTOS_CHECKBOX_KEY, checked);
		editor.commit();
	}
	
	public boolean getServiceCheckBox()
	{
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);
		return storesPreferences.getBoolean(SERVICE_CHECKBOX_KEY, true);
	}
	
	public void setServiceCheckBox(boolean checked)
	{
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);

		Editor editor = storesPreferences.edit();
		editor.putBoolean(SERVICE_CHECKBOX_KEY, checked);
		editor.commit();
	}
	
	public boolean getSoundCheckBox()
	{
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);
		return storesPreferences.getBoolean(SOUND_CHECKBOX_KEY, true);
	}
	
	public void setSoundCheckBox(boolean checked)
	{
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);

		Editor editor = storesPreferences.edit();
		editor.putBoolean(SOUND_CHECKBOX_KEY, checked);
		editor.commit();
	}
	
	public String getGalleryPhotosDirectory() {
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);
		return storesPreferences.getString(GALLERY_PHOTOS_DIRECTORY_KEY, "");
	}

	public void setGalleryPhotosDirectory(String path) {
		SharedPreferences storesPreferences = context.getSharedPreferences(listOfStoresFileName, Context.MODE_PRIVATE);

		Editor editor = storesPreferences.edit();
		editor.putString(GALLERY_PHOTOS_DIRECTORY_KEY, path);
		editor.commit();
	}
	
	public void setProfileDataValid(boolean valid) {
		Editor editor = settings.edit();
		editor.putBoolean(PROFILE_DATA_VALID, valid);
		editor.commit();
	}

	public boolean getProfileDataValid() {
		return settings.getBoolean(PROFILE_DATA_VALID, false);
	}
	
	/* Setter and Getter for CustomerValid */
	public void setCustomerValid(boolean valid) {
		Editor editor = settings.edit();
		editor.putBoolean(CUSTOMER_VALID_KEY, valid);
		editor.commit();
	}

	public boolean getCustomerValid() {
		return settings.getBoolean(CUSTOMER_VALID_KEY, false);
	}

	public boolean hasSettings() {
		return (!settings.getString(USER_KEY, "").equals(""));
	}

}
