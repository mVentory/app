package com.mageventory.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.mageventory.MyApplication;
import com.mageventory.R;
import com.mageventory.ZXingCodeScanner;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ImagesLoader
{
	public static class CachedImage
	{
		public int mWidth, mHeight;
		public Bitmap mBitmap;
		public File mFile;
		
		public CachedImage(File file)
		{
			mFile = file;
		}
	}
	
	private AsyncTask<Void, Void, Void> mLoaderTask;
	private ArrayList<CachedImage> mCachedImages;
	private int mCurrentImageIndex = -1;
	
	private FrameLayout mLeftImage;
	private FrameLayout mCenterImage;
	private FrameLayout mRightImage;
	
	private Activity mActivity;
	
	public ImagesLoader(Activity activity)
	{
		mCachedImages = new ArrayList<CachedImage>();
		mActivity = activity;
	}
	
	public boolean canSwitchLeft()
	{
		if (mCurrentImageIndex>0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean canSwitchRight()
	{
		if (mCurrentImageIndex<mCachedImages.size())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public RectF getCurrentImageRectF()
	{
		if (mCachedImages.get(mCurrentImageIndex).mBitmap == null)
		{
			return null;
		}
		
		int bitmapWidth = mCachedImages.get(mCurrentImageIndex).mWidth;
		int bitmapHeight = mCachedImages.get(mCurrentImageIndex).mHeight;
		
		float imageMatrixArray[] = new float[9];
		imageView(mCenterImage).getImageMatrix().getValues(imageMatrixArray);
		
		float transX = imageMatrixArray[Matrix.MTRANS_X];
		float transY = imageMatrixArray[Matrix.MTRANS_Y];
		float scaleX = imageMatrixArray[Matrix.MSCALE_X];
		float scaleY = imageMatrixArray[Matrix.MSCALE_Y];
		
		return new RectF(transX, transY, transX + bitmapWidth*scaleX, transY + bitmapHeight*scaleY);
	}
	
	public String decodeQRCode(RectF cropRect)
	{
		Bitmap bitmapToDecode = null;
		
		if (cropRect !=null)
		{
			if (mCachedImages.get(mCurrentImageIndex).mBitmap == null)
			{
				return null;
			}
			
			int bitmapWidth = mCachedImages.get(mCurrentImageIndex).mWidth;
			int bitmapHeight = mCachedImages.get(mCurrentImageIndex).mHeight;
			
			float preScale = ((float)mCachedImages.get(mCurrentImageIndex).mBitmap.getWidth())/bitmapWidth;
			
			float imageMatrixArray[] = new float[9];
			imageView(mCenterImage).getImageMatrix().getValues(imageMatrixArray);
			
			float transX = imageMatrixArray[Matrix.MTRANS_X];
			float transY = imageMatrixArray[Matrix.MTRANS_Y];
			float scaleX = imageMatrixArray[Matrix.MSCALE_X] * preScale;
			float scaleY = imageMatrixArray[Matrix.MSCALE_Y] * preScale;
			
			cropRect.offset(-transX, -transY);
			
			cropRect.left /= scaleX;
			cropRect.right /= scaleX;
			cropRect.top /= scaleY;
			cropRect.bottom /= scaleY;
			
			Rect bitmapRect = new Rect((int)cropRect.left, (int)cropRect.top, (int)cropRect.right, (int)cropRect.bottom);
			
			bitmapRect.left = bitmapRect.left<0?0:bitmapRect.left;
			bitmapRect.top = bitmapRect.top<0?0:bitmapRect.top;
			
			bitmapRect.right = bitmapRect.right>bitmapWidth?bitmapWidth:bitmapRect.right;
			bitmapRect.bottom = bitmapRect.bottom>bitmapHeight?bitmapHeight:bitmapRect.bottom;
			
			try {
				FileInputStream fis = new FileInputStream(mCachedImages.get(mCurrentImageIndex).mFile);
				
				int screenSmallerDimension;
				
				DisplayMetrics metrics = new DisplayMetrics();
				mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
				screenSmallerDimension = metrics.widthPixels;
				if (screenSmallerDimension > metrics.heightPixels)
				{
					screenSmallerDimension = metrics.heightPixels;
				}
				
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inInputShareable = true;
				opts.inPurgeable = true;
				opts.inJustDecodeBounds = true;
				int coef = Integer.highestOneBit((bitmapRect.right-bitmapRect.left) / screenSmallerDimension);
				
				opts.inJustDecodeBounds = false;
				if (coef > 1) {
					opts.inSampleSize = coef;
				}
				
				BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(fis, false);  
				bitmapToDecode = decoder.decodeRegion(bitmapRect, opts);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			bitmapToDecode = mCachedImages.get(mCurrentImageIndex).mBitmap;
		}
		
		if (bitmapToDecode != null)
		{
			ZXingCodeScanner multiDetector = new ZXingCodeScanner();
			String code = multiDetector.decode(bitmapToDecode);
			return code;
		}
		else
		{
			return null;
		}
	}
	
	private Bitmap loadBitmap(CachedImage cachedImage) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inInputShareable = true;
		opts.inPurgeable = true;

		int screenSmallerDimension;
		
		DisplayMetrics metrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		screenSmallerDimension = metrics.widthPixels;
		
		if (screenSmallerDimension > metrics.heightPixels)
		{
			screenSmallerDimension = metrics.heightPixels;
		}
		
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(cachedImage.mFile.getAbsolutePath(), opts);

		cachedImage.mWidth = opts.outWidth;
		cachedImage.mHeight = opts.outHeight;
		
		int coef = Integer.highestOneBit(opts.outWidth / screenSmallerDimension);
		
		opts.inJustDecodeBounds = false;
		if (coef > 1) {
			opts.inSampleSize = coef;
		}
		
		Bitmap bitmap = BitmapFactory.decodeFile(cachedImage.mFile.getAbsolutePath(), opts);

		return bitmap;
	}
	
	private ImageView imageView(FrameLayout layout)
	{
		return (ImageView)layout.findViewById(R.id.image);
	}
	
	private ProgressBar progressBar(FrameLayout layout)
	{
		return (ProgressBar)layout.findViewById(R.id.progressBar);
	}
	public void setState(int currentImageIndex, FrameLayout leftImage, FrameLayout centerImage, FrameLayout rightImage)
	{
		if (mCurrentImageIndex == -1)
		{
			updateImageLayout(leftImage, currentImageIndex-1);
			updateImageLayout(centerImage, currentImageIndex);
			updateImageLayout(rightImage, currentImageIndex+1);
		}
		else
		{
			if (currentImageIndex<mCurrentImageIndex)
			{
				updateImageLayout(leftImage, currentImageIndex-1);
			}
			else
			if (currentImageIndex>mCurrentImageIndex)
			{
				updateImageLayout(rightImage, currentImageIndex+1);
			}
			else
			if (currentImageIndex==mCurrentImageIndex)
			{
				updateImageLayout(leftImage, currentImageIndex-1);
				updateImageLayout(centerImage, currentImageIndex);
				updateImageLayout(rightImage, currentImageIndex+1);
			}
		}
		
		mCurrentImageIndex = currentImageIndex;
		mLeftImage = leftImage;
		mCenterImage = centerImage;
		mRightImage = rightImage;
		
		if (currentImageIndex-2>=0 && currentImageIndex-2<mCachedImages.size())
		{
			mCachedImages.get(currentImageIndex-2).mBitmap = null;
		}
			
		if (currentImageIndex+2>=0 && currentImageIndex+2<mCachedImages.size())
		{
			mCachedImages.get(currentImageIndex+2).mBitmap = null;
		}
		
		loadImages();
	}
	
	private void updateImageLayout(FrameLayout layout, int associatedIndex)
	{
		ImageView imageView = imageView(layout);
		ProgressBar progressBar = progressBar(layout);

		if (associatedIndex<0 || associatedIndex>=mCachedImages.size())
		{
			imageView.setImageBitmap(null);
			imageView.setVisibility(View.GONE);
			progressBar.setVisibility(View.GONE);
		}
		else if (mCachedImages.get(associatedIndex).mBitmap == null)
		{
			imageView.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
		}
		else
		{
			imageView.setImageBitmap(mCachedImages.get(associatedIndex).mBitmap);
			progressBar.setVisibility(View.GONE);
			imageView.setVisibility(View.VISIBLE);
		}
	}
	
	public void addCachedImage(CachedImage cachedImage)
	{
		mCachedImages.add(cachedImage);
	}
	
	private AsyncTask<Void, Void, Void> getAsyncTask()
	{
		return new AsyncTask<Void, Void, Void>()
		{
			private int mIndexToLoad;
			
			private void calculateIndexToLoad()
			{
				if (mCurrentImageIndex>=0 && mCurrentImageIndex<mCachedImages.size() && (mCachedImages.get(mCurrentImageIndex).mBitmap == null))
				{
					mIndexToLoad = mCurrentImageIndex;
					return;
				}
				
				if (mCurrentImageIndex+1<mCachedImages.size() && mCachedImages.get(mCurrentImageIndex+1).mBitmap == null)
				{
					mIndexToLoad = mCurrentImageIndex+1;
					return;
				}
				
				if (mCurrentImageIndex-1>=0 && mCachedImages.get(mCurrentImageIndex-1).mBitmap == null)
				{
					mIndexToLoad = mCurrentImageIndex-1;
					return;
				}
				
				mIndexToLoad = -1;
			}
			
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				calculateIndexToLoad();
				if (mIndexToLoad == -1)
				{
					this.cancel(false);
					mLoaderTask = null;
				}
			}
			
			@Override
			protected Void doInBackground(Void... params) {
				mCachedImages.get(mIndexToLoad).mBitmap = loadBitmap(mCachedImages.get(mIndexToLoad));
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				
				if (mCurrentImageIndex-1 == mIndexToLoad)
				{
					updateImageLayout(mLeftImage, mIndexToLoad);
				}
				else
				if (mCurrentImageIndex == mIndexToLoad)
				{
					updateImageLayout(mCenterImage, mIndexToLoad);
				}
				else				
				if (mCurrentImageIndex+1 == mIndexToLoad)
				{
					updateImageLayout(mRightImage, mIndexToLoad);
				}
				else
				{
					mCachedImages.get(mIndexToLoad).mBitmap = null;
				}
				
				mLoaderTask = null;
				loadImages();
			}
		};
	}
	
	public void queueImage(int idx, String sku)
	{
		final MyApplication application = (MyApplication)mActivity.getApplication();
		
		File originalFile = mCachedImages.get(idx).mFile;
		final File fileToUpload = new File(originalFile.getParentFile(), sku + "__" + originalFile.getName());

		new Thread(new Runnable() {
			
			@Override
			public void run() {
				application.mExternalImageUploader.scheduleImageUpload(fileToUpload.getAbsolutePath());
			}
		}).start();
		
		mCachedImages.remove(idx);
	}
	
	private void loadImages()
	{
		if (mLoaderTask == null)
		{
			mLoaderTask = getAsyncTask();
			mLoaderTask.execute();
		}
	}
}