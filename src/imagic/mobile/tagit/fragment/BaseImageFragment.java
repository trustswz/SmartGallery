package imagic.mobile.tagit.fragment;

import imagic.mobile.utils.Constants;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class BaseImageFragment extends Fragment {

	private String tagName;

	String[] imageUrls;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		Bundle extras = this.getArguments();
		if (extras != null) {
			setTagName(extras.getString(Constants.Extra.IMAGE_TAG_NAME));
			imageUrls = new String[1];
		}else{
			imageUrls = new String[1];
		}
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	
}
