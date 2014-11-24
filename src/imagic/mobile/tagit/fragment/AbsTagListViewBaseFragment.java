package imagic.mobile.tagit.fragment;

import imagic.mobile.smart.gallery.ImageActivity;
import imagic.mobile.utils.Constants;
import android.content.Intent;
import android.widget.AbsListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

public abstract class AbsTagListViewBaseFragment extends BaseTagListWithImageIconFragment {

	protected static final String STATE_PAUSE_ON_SCROLL = "STATE_PAUSE_ON_SCROLL";
	protected static final String STATE_PAUSE_ON_FLING = "STATE_PAUSE_ON_FLING";

	protected AbsListView gridView;

	protected boolean pauseOnScroll = false;
	protected boolean pauseOnFling = true;

	@Override
	public void onResume() {
		super.onResume();
		applyScrollListener();
	}

	protected void startImageActivity(int position) {
		if(this.tagNames != null){
			String  tagName = this.tagNames[position];
			Intent intent = new Intent(getActivity(), ImageActivity.class);
			intent.putExtra(Constants.Extra.IMAGE_TAG_NAME, tagName);
			startActivity(intent);
		}
	}

	private void applyScrollListener() {
		gridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), pauseOnScroll, pauseOnFling));
	}
}
