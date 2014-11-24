package imagic.mobile.utils;

import java.io.IOException;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;

import com.ipaulpro.afilechooser.utils.FileUtils;

public class ImageDecoder {

	public static Bitmap decodeFile(Activity activity, Uri targetUri){
		Bitmap b = null;
		try {
			//Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			o.inScaled = false;

			String path = FileUtils.getPath(activity, targetUri);

			BitmapFactory.decodeFile(path,o);

			//BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri),null,o);

			DisplayMetrics displaymetrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			int height = displaymetrics.heightPixels;
			int width = displaymetrics.widthPixels;

			int imagemaxheight;
			int imagemaxwidth;

			if(width > height){
				imagemaxheight = width;
				imagemaxwidth = height;
			}
			else{
				imagemaxheight = height;
				imagemaxwidth = width;
			}

			//long maxsizeallowedbymemory = Runtime.getRuntime().totalMemory();
			ActivityManager am = ((ActivityManager)activity.getSystemService(Context.ACTIVITY_SERVICE));
			long maxsizeallowedbymemory = am.getMemoryClass()*1024*1024;
			//			if(maxsizeallowedbymemory > maxmemory){
			//				maxsizeallowedbymemory = maxmemory;
			//			}
			//int takes 4
			maxsizeallowedbymemory = maxsizeallowedbymemory/4;
			//need 6 copy of images (565 and short count as 0.5) to be saved in memory, 
			//20% of the app's memory is safe.
			//leave 80% memory for gui
			maxsizeallowedbymemory = maxsizeallowedbymemory/28;
			//now maxsize
			maxsizeallowedbymemory = (long) Math.sqrt(maxsizeallowedbymemory);

			if(maxsizeallowedbymemory > 600)
				maxsizeallowedbymemory = 600;

			if(imagemaxheight > maxsizeallowedbymemory ){
				imagemaxwidth = (int) (imagemaxwidth * maxsizeallowedbymemory / imagemaxheight);
				imagemaxheight = (int) maxsizeallowedbymemory;
			}

			int scale = 1;
			int scale1,scale2;
			if (o.outHeight > imagemaxwidth || o.outWidth > imagemaxwidth) {
				scale1 = (int)Math.pow(2, (int) Math.round(Math.log(imagemaxheight / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
				scale2 = (int)Math.pow(2, (int) Math.round(Math.log(imagemaxwidth / (double) Math.min(o.outHeight, o.outWidth)) / Math.log(0.5)));
				scale = Math.max(scale1, scale2);
			}

			o.inSampleSize = scale;
			o.inJustDecodeBounds = false;
			o.inDither = false;
			o.inPreferredConfig = Bitmap.Config.RGB_565;
			b = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(targetUri),null,o);

			if(b.getWidth() < b.getHeight()){
				if(b.getWidth() < 224){
					b = Bitmap.createScaledBitmap(b, 224, b.getHeight()*224/b.getWidth(), true);
				}
			}else{
				if(b.getHeight() < 224){
					b = Bitmap.createScaledBitmap(b, b.getWidth()*224/b.getHeight(), 224, true);
				}
			}
		} catch (OutOfMemoryError e){
			Log.e("MobilePainterActivity", "error not enough memory", e);
			b = null;
		}catch (IOException e) {
			return null;
		} catch (Exception e) {
			return null;
		}

		return b;
	}
	
	public static Bitmap resizeBitmapToRightSize(Activity activity, Bitmap input){
		Bitmap b = null;
		try {
			//Decode image size
			DisplayMetrics displaymetrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			int height = displaymetrics.heightPixels;
			int width = displaymetrics.widthPixels;

			int imagemaxheight;
			int imagemaxwidth;

			if(width > height){
				imagemaxheight = width;
				imagemaxwidth = height;
			}
			else{
				imagemaxheight = height;
				imagemaxwidth = width;
			}

			//long maxsizeallowedbymemory = Runtime.getRuntime().totalMemory();
			ActivityManager am = ((ActivityManager)activity.getSystemService(Context.ACTIVITY_SERVICE));
			long maxsizeallowedbymemory = am.getMemoryClass()*1024*1024;
			//			if(maxsizeallowedbymemory > maxmemory){
			//				maxsizeallowedbymemory = maxmemory;
			//			}
			//int takes 4
			maxsizeallowedbymemory = maxsizeallowedbymemory/4;
			//need 6 copy of images (565 and short count as 0.5) to be saved in memory, 
			//20% of the app's memory is safe.
			//leave 80% memory for gui
			maxsizeallowedbymemory = maxsizeallowedbymemory/28;
			//now maxsize
			maxsizeallowedbymemory = (long) Math.sqrt(maxsizeallowedbymemory);

			if(maxsizeallowedbymemory > 600)
				maxsizeallowedbymemory = 600;

			if(imagemaxheight > maxsizeallowedbymemory ){
				imagemaxwidth = (int) (imagemaxwidth * maxsizeallowedbymemory / imagemaxheight);
				imagemaxheight = (int) maxsizeallowedbymemory;
			}

			int scale = 1;
			int scale1,scale2;
			if (input.getHeight() > imagemaxwidth || input.getWidth() > imagemaxwidth) {
				scale1 = (int)Math.pow(2, (int) Math.round(Math.log(imagemaxheight / (double) Math.max(input.getHeight(), input.getWidth())) / Math.log(0.5)));
				scale2 = (int)Math.pow(2, (int) Math.round(Math.log(imagemaxwidth / (double) Math.min(input.getHeight(), input.getWidth())) / Math.log(0.5)));
				scale = Math.max(scale1, scale2);
			}
			
			b = Bitmap.createScaledBitmap(input, input.getWidth()/scale, input.getHeight()/scale, true);

			if(b.getWidth() < b.getHeight()){
				if(b.getWidth() < 224){
					b = Bitmap.createScaledBitmap(b, 224, b.getHeight()*224/b.getWidth(), true);
				}
			}else{
				if(b.getHeight() < 224){
					b = Bitmap.createScaledBitmap(b, b.getWidth()*224/b.getHeight(), 224, true);
				}
			}
		} catch (OutOfMemoryError e){
			Log.e("MobilePainterActivity", "error not enough memory", e);
			b = null;
		}catch (Exception e) {
			return null;
		}

		return b;
	}

}
