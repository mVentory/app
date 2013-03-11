package com.mageventory.model;

import java.io.Serializable;

public class ProductDuplicationOptions implements Serializable {

	private static final long serialVersionUID = 178208895425917839L;

	private String mPhotosCopyMode;
	private float mDecreaseOriginalQtyBy;
	private boolean mEditBeforeSaving;

	public void setPhotosCopyMode(String photosCopyMode)
	{
		mPhotosCopyMode = photosCopyMode;
	}
	
	public String getPhotosCopyMode()
	{
		return mPhotosCopyMode;
	}
	
	public void setDecreaseOriginalQtyBy(float decreaseOriginalQtyBy)
	{
		mDecreaseOriginalQtyBy = decreaseOriginalQtyBy;
	}
	
	public float getDecreaseOriginalQtyBy()
	{
		return mDecreaseOriginalQtyBy;
	}
	
	public void setEditBeforeSaving(boolean editBeforeSaving)
	{
		mEditBeforeSaving = editBeforeSaving;
	}
	
	public boolean getEditBeforeSaving()
	{
		return mEditBeforeSaving;
	}
	
}
