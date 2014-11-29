package imagic.mobile.utils;

import imagic.mobile.object.TagInfo;
import imagic.mobile.smart.gallery.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.ipaulpro.afilechooser.utils.FileUtils;

public class TagManager {

	public static int getUnOrganizedImagesCount(Context mContext){
		String[] proj =
			{ MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
		Cursor imageCursor = mContext.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null, 
				MediaStore.Images.Media._ID);
		int count = imageCursor.getCount() 
				- TagManager.getTaggedFileList(mContext).size()
				- TagManager.deletedFileCount(mContext);
		if(count < 0) count = 0;
		return count;
	}

	public static Set<String> getUnOrganizedImages(Context mContext){
		Set<String> imageNames = new HashSet<String>();
		Set<String> taggedFileList = TagManager.getTaggedFileList(mContext);
		String[] proj =
			{ MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
		Cursor imageCursor = mContext.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null, 
				MediaStore.Images.Media._ID);

		int column_index=imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		//int column_index=imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
		imageCursor.moveToFirst();
		while(imageCursor.isAfterLast() == false){
			//int id = imageCursor.getInt(column_index);
			String imageName = imageCursor.getString(column_index);
			//Uri baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			//String imageName = FileUtils.getPath(mContext, Uri.withAppendedPath(baseUri, "" + id));
			if(!taggedFileList.contains(imageName)){
				imageNames.add(imageName);
			}
			imageCursor.moveToNext();
		}
		return imageNames;
	}

	public static String getUnOrganizedImageIcon(Context mContext){
		Set<String> taggedFileList = TagManager.getTaggedFileList(mContext);
		String imageName;
		String[] proj =
			{ MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
		Cursor imageCursor = mContext.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null, 
				MediaStore.Images.Media._ID);
		int column_index=imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
		imageCursor.moveToFirst();
		while(imageCursor.isAfterLast() == false){
			int id = imageCursor.getInt(column_index);
			Uri baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			imageName = FileUtils.getPath(mContext, Uri.withAppendedPath(baseUri, "" + id));
			if(!taggedFileList.contains(imageName)
					&& SmallUtils.isFileExist(mContext, imageName)){
				return imageName;
			}
			imageCursor.moveToNext();
		}
		return null;
	}

	public static Set<String> getTagList(Context mContext){
		SharedPreferences prefs = mContext.getApplicationContext().getSharedPreferences(ConstantInfo.PREFERENCE_TAGS_INFO, 0);
		Set<String> tagList = prefs.getStringSet(ConstantInfo.PREFERENCE_KEY_TAGLIST, new HashSet<String>());
		tagList.add(mContext.getString(R.string.unorganized));
		return tagList;
	}

	public static Set<String> getFileTagList(Context mContext, String fileName){
		SharedPreferences prefs = mContext.getApplicationContext().getSharedPreferences(ConstantInfo.PREFERENCE_TAGS_INFO, 0);
		Set<String> tagList = prefs.getStringSet(fileName, null);
		return tagList;
	}

	public static Set<String> getTaggedFileList(Context mContext){
		SharedPreferences prefs = mContext.getApplicationContext().getSharedPreferences(ConstantInfo.PREFERENCE_TAGS_INFO, 0);
		Set<String> taggedFileList = prefs.getStringSet(ConstantInfo.PREFERENCE_KEY_TAGGEDFILELIST, new HashSet<String>());
		return taggedFileList;
	}

	public static void addTag(Context mContext, String fileName, List<String> tags){
		if(mContext != null && fileName != null && tags != null){
			SharedPreferences prefs = mContext.getApplicationContext().getSharedPreferences(ConstantInfo.PREFERENCE_TAGS_INFO, 0);
			if(tags != null&&!tags.isEmpty()){
				SharedPreferences.Editor editor = prefs.edit();
				Set<String> tagList = prefs.getStringSet(ConstantInfo.PREFERENCE_KEY_TAGLIST, new HashSet<String>());
				Set<String> taggedFileList = prefs.getStringSet(ConstantInfo.PREFERENCE_KEY_TAGGEDFILELIST, new HashSet<String>());
				Set<String> fileTagList = prefs.getStringSet(fileName, new HashSet<String>());
				for(String tag:tags){
					tagList.add(tag);
					Set<String> fileList = prefs.getStringSet(tag, new HashSet<String>());
					fileList.add(fileName);
					editor.putStringSet(tag, fileList);
					fileTagList.add(tag);
				}
				taggedFileList.add(fileName);
				editor.putStringSet(fileName, fileTagList);
				editor.putStringSet(ConstantInfo.PREFERENCE_KEY_TAGLIST, tagList);
				editor.putStringSet(ConstantInfo.PREFERENCE_KEY_TAGGEDFILELIST, taggedFileList);
				editor.commit();
			}
		}
	}

	public static Set<String> getFiles(Context mContext, String tag){
		Set<String> existingFileList = null;
		SharedPreferences prefs = mContext.getApplicationContext().getSharedPreferences(ConstantInfo.PREFERENCE_TAGS_INFO, 0);
		Set<String> taggedFileList = prefs.getStringSet(ConstantInfo.PREFERENCE_KEY_TAGGEDFILELIST, new HashSet<String>());
		if(tag.equals(mContext.getString(R.string.unorganized))){
			Set<String> fileList = TagManager.getUnOrganizedImages(mContext);

			existingFileList = SmallUtils.extractExistingFiles(mContext, fileList);

		}else{
			existingFileList = new HashSet<String>();
			Set<String> fileList = prefs.getStringSet(tag, new HashSet<String>());
			SharedPreferences.Editor editor = prefs.edit();

			//check the list of files are all existing and remove the non existing ones
			for(String filePath:fileList){
				if(filePath != null){
					File file = new File(filePath);
					if(file.exists()) {
						existingFileList.add(filePath);
					}else{
						//no such file, remove the file from database
						taggedFileList.remove(filePath);
						editor.remove(filePath);
					}
				}
			}

			//if no file under this tag, remove the tag from list and from database
			if(existingFileList.isEmpty()){
				Set<String> tagList = prefs.getStringSet(ConstantInfo.PREFERENCE_KEY_TAGLIST, new HashSet<String>());
				tagList.remove(tag);
				editor.putStringSet(ConstantInfo.PREFERENCE_KEY_TAGLIST, tagList);
				editor.remove(tag);
			}else{
				editor.putStringSet(tag, existingFileList);
			}
			editor.putStringSet(ConstantInfo.PREFERENCE_KEY_TAGGEDFILELIST, taggedFileList);

			editor.commit();
		}

		return existingFileList;
	}

	public static int getFilesCount(Context mContext, String tag){
		SharedPreferences prefs = mContext.getApplicationContext().getSharedPreferences(ConstantInfo.PREFERENCE_TAGS_INFO, 0);
		Set<String> fileList = prefs.getStringSet(tag, new HashSet<String>());
		return fileList.size();
	}

	public static Set<String> getSimilarFiles(Context mContext, String fileName) {
		SharedPreferences prefs = mContext.getApplicationContext().getSharedPreferences(ConstantInfo.PREFERENCE_TAGS_INFO, 0);
		Set<String> tagList = prefs.getStringSet(fileName, new HashSet<String>());
		Map<String,Integer> similarFileMap = new HashMap<String,Integer>();
		for(String tag:tagList){
			Set<String> fileList = prefs.getStringSet(tag, new HashSet<String>());
			for(String file:fileList){
				if(similarFileMap.containsKey(file)){
					similarFileMap.put(file, similarFileMap.get(file) + 1);
				}else{
					similarFileMap.put(file, 1);
				}
			}
		}
		Set<String> similarFileSet = new HashSet<String>();
		for(String file:similarFileMap.keySet()){
			if(similarFileMap.get(file) > 0){
				similarFileSet.add(file);
			}
		}
		return similarFileSet;
	}

	public static List<String> getTagImageIcons(Context mContext, List<TagInfo> sortedTags) {
		List<String> iconURLs = new ArrayList<String>();

		for(TagInfo tag:sortedTags){
			if(tag.getName().equals(mContext.getString(R.string.unorganized))){
				iconURLs.add(TagManager.getUnOrganizedImageIcon(mContext));
			}else if(tag.getName().equals(mContext.getString(R.string.camera))){
				iconURLs.add("drawable://" + R.drawable.ic_camera);
			} else{
				Set<String> tmpFileNames = TagManager.getFiles(mContext, tag.getName());
				if(tmpFileNames != null && !tmpFileNames.isEmpty()){
					boolean inserted = false;
					String backUpFileName = null;
					for(String tmpFileName:tmpFileNames){
						File file = new File(tmpFileName);
						if(file.exists()) {
							if(iconURLs.contains(tmpFileName)){
								if(backUpFileName == null){
									backUpFileName = tmpFileName;
								}else if(Math.random() > 0.5){
									backUpFileName = tmpFileName;
								}
							}else{
								iconURLs.add(tmpFileName);
								inserted = true;
								break;
							}
						}
					}
					if(inserted == false){
						iconURLs.add(backUpFileName);
					}
				}else{
					iconURLs.add("NA");
				}
			}
		}

		return iconURLs;
	}

	public static void deleteFile(Context mContext,
			String fileName) {
		SharedPreferences prefs = mContext.getApplicationContext().getSharedPreferences(ConstantInfo.PREFERENCE_TAGS_INFO, 0);
		Set<String> taggedFileList = prefs.getStringSet(ConstantInfo.PREFERENCE_KEY_TAGGEDFILELIST, new HashSet<String>());
		Set<String> tagList = prefs.getStringSet(fileName, new HashSet<String>());
		int count = prefs.getInt(ConstantInfo.PREFERENCE_KEY_DELETEDFILECOUNT, 0);
		SharedPreferences.Editor editor = prefs.edit();
		if(!taggedFileList.contains(fileName)){
			count ++;
			editor.putInt(ConstantInfo.PREFERENCE_KEY_DELETEDFILECOUNT, count);
		}else{
			//remove from taggedFileList
			taggedFileList.remove(fileName);
			editor.putStringSet(ConstantInfo.PREFERENCE_KEY_TAGGEDFILELIST, taggedFileList);
		}//remove file from different tags
		for(String tag:tagList){
			Set<String> fileList = prefs.getStringSet(tag, new HashSet<String>());
			fileList.remove(fileName);
			editor.putStringSet(tag, fileList);
		}
		//remove file from database
		editor.remove(fileName);
		editor.commit();
	}

	public static int deletedFileCount(Context mContext){
		SharedPreferences prefs = mContext.getApplicationContext().getSharedPreferences(ConstantInfo.PREFERENCE_TAGS_INFO, 0);
		return prefs.getInt(ConstantInfo.PREFERENCE_KEY_DELETEDFILECOUNT, 0);
	}

	public static void updateDeleteFileCount(Context mContext, int count) {
		SharedPreferences prefs = mContext.getApplicationContext().getSharedPreferences(ConstantInfo.PREFERENCE_TAGS_INFO, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(ConstantInfo.PREFERENCE_KEY_DELETEDFILECOUNT, count);
		editor.commit();
	}

}
