package imagic.mobile.tagit.fragment;

import imagic.mobile.smart.gallery.R;
import imagic.mobile.utils.Constants;
import imagic.mobile.utils.ImageDecoder;
import imagic.mobile.utils.SmallUtils;
import imagic.mobile.utils.TagManager;

import java.util.List;
import java.util.Set;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;

public class AutomaticImagePagerFragment extends ImagePagerFragment {

	public static final int INDEX = 3;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = CreateView(inflater,container);

		startLoading();

		return rootView;
	}

	@Override
	public void onSucess(List<String> tags) {
		if(!this.isRemoving() && getActivity() != null && !getActivity().isFinishing()){
			populateTagViews(tags);
			TagManager.addTag(getActivity(), SmallUtils.getPathFromImageLoaderFormat(getTargetPath()), tags);

			increamentPosition();
			getPager().setCurrentItem(getPosition() - 1);
			setTargetPath(imageUrls[getPosition()]);
			setSource(ImageDecoder.resizeBitmapToRightSize(getActivity(), 
					ImageLoader.getInstance().loadImageSync(imageUrls[getPosition()])));
			sendImageForTag();

			getActivity().setTitle(getActivity().getString(R.string.organize_all_title)
					+" " + String.valueOf(imageUrls.length - this.getPosition()));
		}
	}

	private void increamentPosition() {
		if(getPosition() < imageUrls.length - 1){
			setPosition(getPosition() + 1);
		}else{
			getActivity().finish();
		}
	}

	//Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		//no menu
		return;
	}

	@Override
	public void onFail(String debug) {
		if(!this.isRemoving() && getActivity() != null && !getActivity().isFinishing()){
			
			hideAllTagViews();
			
			increamentPosition();
			getPager().setCurrentItem(getPosition());
			setTargetPath(imageUrls[getPosition()]);
			setSource(ImageDecoder.resizeBitmapToRightSize(getActivity(), 
					ImageLoader.getInstance().loadImageSync(imageUrls[getPosition()])));
			sendImageForTag();

			getActivity().setTitle(getActivity().getString(R.string.organize_all_title)
					+ " " + String.valueOf(imageUrls.length - this.getPosition()));
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		Set<String> imageNames;
		if(this.getTagName().contains("/") || this.getTagName().contains(".")){
			imageNames = TagManager.getSimilarFiles(this.getActivity(), getTagName());
		}else{
			imageNames = TagManager.getFiles(this.getActivity(), getTagName());
		}
		if(!imageNames.isEmpty()){
			imageUrls = new String[imageNames.size()];
			int i = 0;
			for(String imageName:imageNames){
				imageUrls[i] = SmallUtils.getImageLoaderFormatFromPath(imageName);
				i++;
			}
		}else{
			//no image in the grid
			getActivity().finish();
		}

		setAdapter(new ImageAdapter());
		getPager().setAdapter(getAdapter());
		setPosition(getArguments().getInt(Constants.Extra.IMAGE_POSITION, 0));
		getPager().setCurrentItem(getPosition());
		setTargetPath(imageUrls[getPosition()]);
		setSource(ImageDecoder.resizeBitmapToRightSize(getActivity(), 
				ImageLoader.getInstance().loadImageSync(imageUrls[getPosition()])));
		sendImageForTag();

		getActivity().setTitle(getActivity().getString(R.string.organize_all_title)
				+" " + String.valueOf(imageUrls.length - this.getPosition()));

	}

	@Override
	public void onPause() {
		super.onPause();
		//stop the process to avoid problem
		getActivity().finish();
	}
	
	
}
