package imagic.mobile.utils;

import imagic.mobile.smart.gallery.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Parcelable;

public class SmallUtils {
	public static Uri string2Uri(String string){
		return Uri.parse(string);
	}

	public static String uri2String(Uri uri){
		return uri.toString();
	}


	public static int checkSource(Uri targetUri) {
		if(targetUri.toString().length() > 0){
			return 1;
		}else{
			return 0;
		}
	}
	
	public static boolean isFileExist(Context mContext, String fileName){
		File file = new File(fileName);
		if(file.exists()) {
			return true;
		}else{
			return false;
		}
	}

	public static Set<String> extractExistingFiles(Context mContext, Set<String> fileList) {
		Set<String> existingFileList = new HashSet<String>();
		//check the list of files are all existing and remove the non existing ones
		int count = 0;
		for(String fileName:fileList){
			if(fileName != null){
				File file = new File(fileName);
				if(file.exists()) {
					existingFileList.add(fileName);
				}else{
					count ++;
				}
			}
		}
		TagManager.updateDeleteFileCount(mContext, count);
		return existingFileList;
	}

	// Method: share yes, but exclude itself
	public static Intent generateCustomChooserIntent(Context mContext, Intent prototype, String[] forbiddenChoices) {
		List<Intent> targetedShareIntents = new ArrayList<Intent>();
		List<HashMap<String, String>> intentMetaInfo = new ArrayList<HashMap<String, String>>();
		Intent chooserIntent;

		Intent dummy = new Intent(prototype.getAction());
		dummy.setType(prototype.getType());
		List<ResolveInfo> resInfo = mContext.getPackageManager().queryIntentActivities(dummy, 0);

		if (!resInfo.isEmpty()) {
			for (ResolveInfo resolveInfo : resInfo) {
				if (resolveInfo.activityInfo == null || Arrays.asList(forbiddenChoices).contains(resolveInfo.activityInfo.packageName))
					continue;

				HashMap<String, String> info = new HashMap<String, String>();
				info.put("packageName", resolveInfo.activityInfo.packageName);
				info.put("className", resolveInfo.activityInfo.name);
				info.put("simpleName", String.valueOf(resolveInfo.activityInfo.loadLabel(mContext.getPackageManager())));
				intentMetaInfo.add(info);
			}

			if (!intentMetaInfo.isEmpty()) {
				// sorting for nice readability
				Collections.sort(intentMetaInfo, new Comparator<HashMap<String, String>>() {
					@Override
					public int compare(HashMap<String, String> map, HashMap<String, String> map2) {
						return map.get("simpleName").compareTo(map2.get("simpleName"));
					}
				});

				// create the custom intent list
				for (HashMap<String, String> metaInfo : intentMetaInfo) {
					Intent targetedShareIntent = (Intent) prototype.clone();
					targetedShareIntent.setPackage(metaInfo.get("packageName"));
					targetedShareIntent.setClassName(metaInfo.get("packageName"), metaInfo.get("className"));
					targetedShareIntents.add(targetedShareIntent);
				}

				chooserIntent = Intent.createChooser(targetedShareIntents.remove(targetedShareIntents.size() - 1), mContext.getString(R.string.share_title));
				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
				return chooserIntent;
			}
		}

		return Intent.createChooser(prototype, mContext.getString(R.string.share_title));
	}

	public static boolean isNetworkConnected(Context mContext) {
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}

	public static String getPathFromImageLoader(String input) {
		String header = "file:////";
		int index = input.indexOf(header);
		if(index >= 0)
			return input.substring(index + header.length());
		else
			return input;
	}
}
