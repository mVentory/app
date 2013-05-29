package com.mageventory.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;

public class ZXingCodeScanner {

	private MultiFormatReader mMultiFormatReader;
	private String mLastReadCodeType;
	
	public ZXingCodeScanner() {
		mMultiFormatReader = new MultiFormatReader();

		Collection<BarcodeFormat> barcodeCollection = new HashSet<BarcodeFormat>();
		barcodeCollection.add(BarcodeFormat.QR_CODE);
		barcodeCollection.add(BarcodeFormat.UPC_E);
		barcodeCollection.add(BarcodeFormat.UPC_A);
		barcodeCollection.add(BarcodeFormat.EAN_8);
		barcodeCollection.add(BarcodeFormat.EAN_13);
		barcodeCollection.add(BarcodeFormat.CODE_128);
		barcodeCollection.add(BarcodeFormat.CODE_39);
		barcodeCollection.add(BarcodeFormat.ITF);

		Map<DecodeHintType, Collection<BarcodeFormat>> barcodeFormats = new HashMap<DecodeHintType, Collection<BarcodeFormat>>();
		barcodeFormats.put(DecodeHintType.POSSIBLE_FORMATS, barcodeCollection);

		mMultiFormatReader.setHints(barcodeFormats);
	}
	
	public String getLastReadCodeType()
	{
		return mLastReadCodeType;
	}
	
	public String decode(Bitmap imageBitmap) {
		if (imageBitmap == null) {
			return null;
		}
		
		String out = null;
		
		float width = imageBitmap.getWidth();
		float height = imageBitmap.getHeight();

		out = decodeInternal(imageBitmap);
		
		/* Try smaller images if the QRcode can't be recognized in the originally-sized one. */
		while(out == null)
		{
			width *= 0.7;
			height *= 0.7;
			
			/* No point in resizing down to much. */
			if (width * height > 80 * 80)
			{
				Bitmap smallerBitmap = Bitmap.createScaledBitmap(imageBitmap, (int)width, (int)height, false);
				out = decodeInternal(smallerBitmap);
			}
			else
			{
				break;
			}
		}
		
		return out;
	}

	private String decodeInternal(Bitmap imageBitmap) {
		if (imageBitmap == null) {
			return null;
		}

		int width = imageBitmap.getWidth();
		int height = imageBitmap.getHeight();
		int[] pixels = new int[width * height];
		imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);

		boolean success;
		Result result = null;

		try {
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			result = mMultiFormatReader.decodeWithState(bitmap);
			success = true;
		} catch (ReaderException e) {
			success = false;
		}

		if (success && result != null) {
			mLastReadCodeType = result.getBarcodeFormat().toString();
			return result.getText();
		} else {
			return null;
		}
	}
	
	public String decode(String path) {
		Bitmap imageBitmap = BitmapFactory.decodeFile(path);
		return decode(imageBitmap);
	}
		
}
