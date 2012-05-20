package com.mageventory.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;

import com.mageventory.MageventoryConstants;
import com.mageventory.MyApplication;
import com.mageventory.client.Base64Coder_magento;
import com.mageventory.model.Product;

public class JobCacheManager {
	
	public static Object mSynchronizationObject = new Object();
	
	/* Returns true on success. */
	private static boolean serialize(Object o, File file)
	{
		FileOutputStream fos;
		ObjectOutputStream oos;
		try {
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(o);
			oos.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	/* Returns something else than null on success */
	private static Object deserialize(File file)
	{
		Object out;
		FileInputStream fis;
		ObjectInputStream ois;
		
		try {
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			out = ois.readObject();
			ois.close();
		} catch (Exception e) {
			return null;
		}
		return out;
	}
	
	private static String encodeSKU(String SKU)
	{
		return Base64Coder_magento.encodeString(SKU).replace("+", "_").replace("/", "-").replace("=","");
	}
	
	private static String getCachedResourceSubdirName(int resourceType)
	{
		switch (resourceType)
		{
		case MageventoryConstants.RES_UPLOAD_IMAGE:
			return "UPLOAD_IMAGE";

		default:
			return null;
		}
	}
	
	private static String getCachedResourceFileName(JobID jobID)
	{
		switch (jobID.getJobType())
		{
		case MageventoryConstants.RES_UPLOAD_IMAGE:
			return jobID.getTimeStamp() + ".jpg";
		case MageventoryConstants.RES_CATALOG_PRODUCT_CREATE:
			return "new_prod.obj";

		default:
			return null;
		}
	}
	
	private static File getDirectoryAssociatedWithJob(JobID jobID, boolean createDirectories)
	{
		File dir = new File(Environment.getExternalStorageDirectory(), MyApplication.APP_DIR_NAME);
		dir = new File(dir, encodeSKU(jobID.getSKU()));
		
		String subdir = getCachedResourceSubdirName(jobID.getJobType());
		
		if (subdir != null)
		{
			dir = new File(dir, subdir);	
		}
		
		if (createDirectories == true)
		{
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					return null;
				}
			}
		}
		
		return dir;
	}
	
	private static File getFileAssociatedWithJob(JobID jobID, boolean createDirectories)
	{
		File fileToSave = new File(getDirectoryAssociatedWithJob(jobID, createDirectories), getCachedResourceFileName(jobID));
		return fileToSave;
	}
	
	public static String getFilePathAssociatedWithJob(JobID jobID)
	{
	synchronized(mSynchronizationObject)
	{
		return getFileAssociatedWithJob(jobID, false).getAbsolutePath();
	}
	}
	
	public static boolean store(Job job)
	{
	synchronized(mSynchronizationObject)
	{
		File fileToSave = getFileAssociatedWithJob(job.getJobID(), true);
		
		if (fileToSave != null && serialize(job, fileToSave) == true)
			return true;
		else
			return false;
	}
	}
	
	public static Job restore(JobID jobID)
	{
	synchronized(mSynchronizationObject)
	{
		File fileToRead = getFileAssociatedWithJob(jobID, false);
		
		if (fileToRead == null)
			return null;
		else
			return (Job)deserialize(fileToRead);
	}
	}
	
	public static void removeFromCache(JobID jobID)
	{
	synchronized(mSynchronizationObject)
	{
		File fileToRemove = getFileAssociatedWithJob(jobID, false);
		
		if (fileToRemove !=null)
		{
			fileToRemove.delete();
		}
	}
	}
	
	public static File getImageUploadDirectory(String SKU)
	{
	synchronized(mSynchronizationObject)
	{
		return getDirectoryAssociatedWithJob(new JobID(-1, MageventoryConstants.RES_UPLOAD_IMAGE, SKU), true);
	}
	}
	
	public static File getImageDownloadDirectory(String SKU)
	{
	synchronized(mSynchronizationObject)
	{
		File dir = new File(Environment.getExternalStorageDirectory(), MyApplication.APP_DIR_NAME);
		dir = new File(dir, encodeSKU(SKU));
		dir = new File(dir, "DOWNLOAD_IMAGE");	
		
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				return null;
			}
		}
		
		return dir;
	}
	}
	
	public static void clearImageDownloadDirectory(String SKU)
	{
	synchronized(mSynchronizationObject)
	{
		File dir = new File(Environment.getExternalStorageDirectory(), MyApplication.APP_DIR_NAME);
		dir = new File(dir, encodeSKU(SKU));
		dir = new File(dir, "DOWNLOAD_IMAGE");	
		
		if (dir.exists())
		{
			for (File child : dir.listFiles())
        	{
        		child.delete();
        	}
			
			dir.delete();
		}
	}
	}
	
	public static List<Job> restoreImageUploadJobs(String SKU)
	{
	synchronized(mSynchronizationObject)
	{
		File uploadDir = getImageUploadDirectory(SKU);
		List<Job> out = new ArrayList<Job>();
		
		if (uploadDir == null)
			return out;
		
		File[] jobFileList = uploadDir.listFiles();
		
		if (jobFileList != null)
		{
			for (int i=0; i<jobFileList.length; i++)
			{
				Job job = (Job)deserialize(jobFileList[i]);
				if (job != null)
					out.add(job);
			}
		}
		
		return out;
	}
	}

	private static File getProductDetailsFile(String SKU, boolean createDirectories)
	{
		File file = new File(Environment.getExternalStorageDirectory(), MyApplication.APP_DIR_NAME);
		file = new File(file, encodeSKU(SKU));
		
		if (createDirectories == true)
		{
			if (!file.exists()) {
				file.mkdirs();
			}
		}
		
		return new File(file, "prod_dets.obj");
	}
	
	public static void storeProductDetails(Product product)
	{
	synchronized(mSynchronizationObject)
	{
		if (product == null || product.getSku() == null)
		{
			return;
		}
		serialize(product, getProductDetailsFile(product.getSku(), true));
	}
	}
	
	public static Product restoreProductDetails(String SKU)
	{
	synchronized(mSynchronizationObject)
	{
		return (Product)deserialize(getProductDetailsFile(SKU, false));
	}
	}
	
	public static void removeProductDetails(String SKU)
	{
	synchronized(mSynchronizationObject)
	{
		File f = getProductDetailsFile(SKU, false);
		
		if (f.exists())
		{
			f.delete();
		}
	}
	}
	
	public static boolean productDetailsExists(String SKU)
	{
		return getProductDetailsFile(SKU, false).exists();
	}
}
