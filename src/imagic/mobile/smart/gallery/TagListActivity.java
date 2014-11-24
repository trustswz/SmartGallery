package imagic.mobile.smart.gallery;

import imagic.mobile.object.TagInfo;
import imagic.mobile.tagit.fragment.TagGridWithImageIconFragment;
import imagic.mobile.utils.Constants;
import imagic.mobile.utils.TagManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

public class TagListActivity extends FragmentActivity {

	private List<TagInfo> sortedTags;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Fragment fr;
		String fragmentTag = TagGridWithImageIconFragment.class.getSimpleName();
		fr = getSupportFragmentManager().findFragmentByTag(fragmentTag);
		if (fr == null) {

			//unorganized tag's name
			String unorganized = this.getString(R.string.unorganized);

			//prepare the data
			Set<String> tags = TagManager.getTagList(this);

			sortedTags = new ArrayList<TagInfo>();

			int tagCount = 0;
			for(String tag:tags){
				if(!tag.equals(unorganized)){
					tagCount = TagManager.getFilesCount(this, tag);
					sortedTags.add(new TagInfo(tag,tagCount));
				}
			}
			
			Collections.sort(sortedTags);
			
			//now add the number of unsortedImages
			sortedTags.add(0,new TagInfo(unorganized,
					TagManager.getUnOrganizedImagesCount(this)));

			String[] tagNames = new String[tags.size()];
			int[] tagNumberImages = new int[tags.size()];

			int i = 0;
			for(TagInfo tag:sortedTags){
				tagNames[i] = tag.getName();
				tagNumberImages[i] = tag.getNumberImages();
				i++;
			}

			List<String> images = TagManager.getTagImageIcons(this,sortedTags);

			String[] urls = new String[images.size()];

			i = 0;
			for(String image:images){
				urls[i] = image;
				i++;
			}

			fr = new TagGridWithImageIconFragment();
			Bundle extras = new Bundle();
			extras.putStringArray(Constants.Extra.TAG_NAME_LIST, tagNames);
			extras.putIntArray(Constants.Extra.TAG_NAME_LIST_NUMBER_OF_IMAGES, tagNumberImages);
			extras.putStringArray(Constants.Extra.TAG_NAME_LIST_IMAGE_URLS, urls);
			fr.setArguments(extras);
		}

		getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fr, fragmentTag).commit();

	}


}
