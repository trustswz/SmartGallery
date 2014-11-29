package imagic.mobile.tagit.fragment;
import imagic.mobile.object.TagInfo;
import imagic.mobile.smart.gallery.R;
import imagic.mobile.utils.Constants;
import imagic.mobile.utils.SmallUtils;
import imagic.mobile.utils.TagManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class BaseTagListWithImageIconFragment extends Fragment {

	String[] tagNames = null;
	
	int[] tagNumberImages = null;

	String[] imageUrls = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		Bundle extras = this.getArguments();
		if (extras != null) {
			tagNames = extras.getStringArray(Constants.Extra.TAG_NAME_LIST);
			tagNumberImages = extras.getIntArray(Constants.Extra.TAG_NAME_LIST_NUMBER_OF_IMAGES);
			imageUrls = extras.getStringArray(Constants.Extra.TAG_NAME_LIST_IMAGE_URLS);
			for(int i = 1; i < imageUrls.length; i++){
				imageUrls[i] = SmallUtils.getImageLoaderFormatFromPath(imageUrls[i]);
			}
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		//unorganized tag's name
		String unorganized = this.getString(R.string.unorganized);
		//camera tag's name
				String camera = this.getString(R.string.camera);

		//prepare the data
		Set<String> tags = TagManager.getTagList(getActivity());

		ArrayList<TagInfo> sortedTags = new ArrayList<TagInfo>();

		int tagCount = 0;
		for(String tag:tags){
			if(!tag.equals(unorganized)){
				tagCount = TagManager.getFilesCount(getActivity(), tag);
				sortedTags.add(new TagInfo(tag,tagCount));
			}
		}

		Collections.sort(sortedTags);

		//now add the number of unsortedImages
		int unorganizedCount = TagManager.getUnOrganizedImagesCount(getActivity());
		if(unorganizedCount > 0){
			sortedTags.add(0,new TagInfo(unorganized,
					TagManager.getUnOrganizedImagesCount(getActivity())));
		}
		//now add the camera
		sortedTags.add(0,new TagInfo(camera,-1));
		
		tagNames = new String[sortedTags.size()];
		tagNumberImages = new int[sortedTags.size()];

		int i = 0;
		for(TagInfo tag:sortedTags){
			tagNames[i] = tag.getName();
			tagNumberImages[i] = tag.getNumberImages();
			i++;
		}

		List<String> images = TagManager.getTagImageIcons(getActivity(),sortedTags);

		String[] urls = new String[images.size()];

		i = 0;
		for(String image:images){
			urls[i] = image;
			i++;
		}
		
		imageUrls = new String[urls.length];
		
		imageUrls[0] = urls[0];
		for(i = 1; i < urls.length; i++){
			imageUrls[i] = SmallUtils.getImageLoaderFormatFromPath(urls[i]);
		}
		
	}
}
