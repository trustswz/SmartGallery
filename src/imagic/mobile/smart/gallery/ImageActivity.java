package imagic.mobile.smart.gallery;

import imagic.mobile.tagit.fragment.AutomaticImagePagerFragment;
import imagic.mobile.tagit.fragment.ImageGridFragment;
import imagic.mobile.tagit.fragment.ImagePagerFragment;
import imagic.mobile.utils.Constants;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;

public class ImageActivity extends FragmentActivity {
	private String tagName;
	private Fragment fr;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			tagName = extras.getString(Constants.Extra.IMAGE_TAG_NAME);
		}else{
			tagName = this.getString(R.string.images);
		}
		
		if(tagName.contains("/") || tagName.contains(".")){
			setTitle(this.getString(R.string.similar_photos));
		}else{
			setTitle(tagName);
		}

		int frIndex = getIntent().getIntExtra(Constants.Extra.FRAGMENT_INDEX, 0);
		String tag;
		switch (frIndex) {
		default:
		case ImageGridFragment.INDEX:
			tag = ImageGridFragment.class.getSimpleName();
			fr = getSupportFragmentManager().findFragmentByTag(tag);
			if (fr == null) {
				fr = new ImageGridFragment();
				fr.setArguments(getIntent().getExtras());
			}
			break;
		case ImagePagerFragment.INDEX:
			tag = ImagePagerFragment.class.getSimpleName();
			fr = getSupportFragmentManager().findFragmentByTag(tag);
			if (fr == null) {
				fr = new ImagePagerFragment();
				fr.setArguments(getIntent().getExtras());
			}
			break;
		case AutomaticImagePagerFragment.INDEX:
			tag = AutomaticImagePagerFragment.class.getSimpleName();
			fr = getSupportFragmentManager().findFragmentByTag(tag);
			if (fr == null) {
				fr = new AutomaticImagePagerFragment();
				fr.setArguments(getIntent().getExtras());
			}
			break;
		}

		getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fr, tag).commit();

		AppRater.app_launched(this);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		savedInstanceState.putString(Constants.Extra.IMAGE_TAG_NAME, tagName);
		// etc.
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		if(savedInstanceState != null){
			String tags = (String) savedInstanceState.getString(Constants.Extra.IMAGE_TAG_NAME);
			if(tags != null){
				tagName = tags;
			}
		}
	}
	
	public void onTagClicked(View v){
		((ImagePagerFragment) fr).onTagClicked(v);
	}

}