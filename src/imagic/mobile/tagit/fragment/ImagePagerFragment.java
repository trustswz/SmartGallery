package imagic.mobile.tagit.fragment;

import imagic.mobile.network.RetrieveTags;
import imagic.mobile.smart.gallery.ImageActivity;
import imagic.mobile.smart.gallery.R;
import imagic.mobile.ui.DisableableViewPager;
import imagic.mobile.utils.Constants;
import imagic.mobile.utils.ImageDecoder;
import imagic.mobile.utils.RetrieveTagsListener;
import imagic.mobile.utils.SingleMediaScanner;
import imagic.mobile.utils.SmallUtils;
import imagic.mobile.utils.TagManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ImagePagerFragment extends BaseImageFragment implements RetrieveTagsListener, OnPageChangeListener {

	public static final int INDEX = 2;

	DisplayImageOptions options;

	private MenuItem menuItemDelete;

	private MenuItem menuItemSimilar;

	private MenuItem menuItemShare;

	private String targetPath;

	private RelativeLayout mRootView;

	private TextView tags_view;

	private ProgressBar loadingView;

	private DisableableViewPager pager;

	private Bitmap source;

	private RetrieveTags retriever;

	private ImageAdapter adapter;

	private int position;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		options = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(R.drawable.ic_empty)
		.showImageOnFail(R.drawable.ic_error)
		.resetViewBeforeLoading(true)
		.cacheOnDisk(true)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.considerExifParams(true)
		.displayer(new FadeInBitmapDisplayer(300))
		.build();

		Bundle extras = this.getArguments();
		if (extras != null) {
			setTagName(extras.getString(Constants.Extra.IMAGE_TAG_NAME));
			imageUrls = extras.getStringArray(Constants.Extra.IMAGE_URLS);
		}else{
			imageUrls = new String[1];
		}
	}

	@Override
	public void onPause() {
		this.dismissLoading();
		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fr_image_pager, container, false);

		mRootView = (RelativeLayout) rootView.findViewById(R.id.rootView);

		tags_view = (TextView) rootView.findViewById(R.id.textView);

		loadingView = (ProgressBar) rootView.findViewById(R.id.loadingimage);

		pager = (DisableableViewPager) rootView.findViewById(R.id.pager);
		adapter = new ImageAdapter();
		pager.setAdapter(adapter);
		position = getArguments().getInt(Constants.Extra.IMAGE_POSITION, 0);
		pager.setCurrentItem(position);
		targetPath = imageUrls[position];
		source = ImageDecoder.resizeBitmapToRightSize(getActivity(), 
				ImageLoader.getInstance().loadImageSync(imageUrls[position]));
		sendImageForTag();

		pager.setOnPageChangeListener(this);

		return rootView;
	}

	private class ImageAdapter extends PagerAdapter {

		private LayoutInflater inflater;

		ImageAdapter() {
			inflater = LayoutInflater.from(getActivity());
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return imageUrls.length;
		}

		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
			assert imageLayout != null;
			ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
			ImageLoader.getInstance().displayImage(imageUrls[position], imageView, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					spinner.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					String message = null;
					switch (failReason.getType()) {
					case IO_ERROR:
						message = "Input/Output error";
						break;
					case DECODING_ERROR:
						message = "Image can't be decoded";
						break;
					case NETWORK_DENIED:
						message = "Downloads are denied";
						break;
					case OUT_OF_MEMORY:
						message = "Out Of Memory error";
						break;
					case UNKNOWN:
						message = "Unknown error";
						break;
					}
					Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

					spinner.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					spinner.setVisibility(View.GONE);
				}
			});

			view.addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}

	//Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.image_activity_menu, menu);

		menuItemDelete = menu.findItem(R.id.menu_item_delete);
		menuItemSimilar = menu.findItem(R.id.menu_item_similar);
		menuItemShare = menu.findItem(R.id.menu_item_share);

		return;
	}

	//Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_item_delete:
			this.deleteImage();
			return true;
		case R.id.menu_item_similar:
			this.searchSimilar();
			return true;
		case R.id.menu_item_share:
			this.ShareResult();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void deleteImage() {
		if(targetPath != null){
			
			final AlertDialog.Builder confirmDeleteDialogBuilder = new AlertDialog.Builder(getActivity());
			confirmDeleteDialogBuilder.setMessage(getActivity().getString(R.string.delete_image));
			confirmDeleteDialogBuilder.setCancelable(false);
			confirmDeleteDialogBuilder.setPositiveButton(R.string.confirm,
					new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					File fdelete = new File(SmallUtils.getPathFromImageLoader(targetPath));
					if (fdelete.exists()) {
						if (fdelete.delete()) {

							//remove the file from the imageUrls
							final List<String> newImagelist =  new ArrayList<String>();
							Collections.addAll(newImagelist, imageUrls); 
							newImagelist.remove(targetPath);
							imageUrls = newImagelist.toArray(new String[newImagelist.size()]);

							//remove from the system
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
								Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
								File f = new File(targetPath);
								Uri contentUri = Uri.fromFile(f);
								mediaScanIntent.setData(contentUri);
								getActivity().sendBroadcast(mediaScanIntent);
							} else {
								getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/" + "FOLDER_TO_REFRESH")));
							}
							
							//remove from tag manager
							TagManager.deleteFile(getActivity(), targetPath);

							pager.setAdapter(adapter);
							
							if(position > 0)
								position--;
							pager.setCurrentItem(position);
						}
					}
					dialog.dismiss();
				}
			});
			confirmDeleteDialogBuilder.setNegativeButton(R.string.cancel, null);

			AlertDialog confirmDeleteDialog = confirmDeleteDialogBuilder.create();
			confirmDeleteDialog.show();
		}
	}

	private void searchSimilar() {
		if(targetPath != null){
			Intent intent = new Intent(getActivity(), ImageActivity.class);
			intent.putExtra(Constants.Extra.IMAGE_TAG_NAME, SmallUtils.getPathFromImageLoader(targetPath));
			startActivity(intent);
		}
	}

	public void ShareResult(){
		mRootView.setDrawingCacheEnabled(true);
		mRootView.buildDrawingCache(true);
		Bitmap bitmap = this.mRootView.getDrawingCache();

		File tempDir= Environment.getExternalStorageDirectory();
		tempDir=new File(tempDir.getAbsolutePath()+"/ClarifaiIM/");
		if(!tempDir.exists())
		{
			tempDir.mkdir();
		}
		File file = new File(Environment.getExternalStorageDirectory()+"/ClarifaiIM/","share.jpg");

		FileOutputStream out;
		try {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);

			new SingleMediaScanner(getActivity(), file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		bitmap.recycle();

		mRootView.setDrawingCacheEnabled(false);

		// now share
		String[] blacklist = new String[]{this.getString(R.string.app_package_name)};

		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("image/jpeg");
		sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
		sendIntent.putExtra(Intent.EXTRA_TEXT, tags_view.getText() + "\n" + 
				getString(R.string.share_string) + getString(R.string.app_package_name));
		startActivity(SmallUtils.generateCustomChooserIntent(getActivity(),sendIntent, blacklist));
	}

	private void startLoading() {
		if(menuItemDelete != null)
			menuItemDelete.setEnabled(false);
		if(menuItemSimilar != null)
			menuItemSimilar.setEnabled(false);
		if(menuItemShare != null)
			menuItemShare.setEnabled(false);

		if(loadingView != null){
			loadingView.setVisibility(View.VISIBLE);
			loadingView.invalidate();
		}

		if(pager != null)
			pager.setPagingEnabled(false);

	}

	private void dismissLoading() {

		if(menuItemDelete!=null)
			menuItemDelete.setEnabled(true);
		if(menuItemSimilar!=null)
			menuItemSimilar.setEnabled(true);
		if(menuItemShare!=null)
			menuItemShare.setEnabled(true);

		if(loadingView != null){
			loadingView.setVisibility(View.GONE);
			loadingView.invalidate();
		}

		if(pager != null)
			pager.setPagingEnabled(true);
	}

	private void sendImageForTag() {

		if(source != null){

			this.startLoading();

			Set<String> tags = TagManager.getFileTagList(getActivity(),
					SmallUtils.getPathFromImageLoader(targetPath));

			if(tags != null){
				List<String> tagList = new ArrayList<String>();
				for(String tag:tags){
					tagList.add(tag);
				}
				onSucess(tagList);
			}else if(SmallUtils.isNetworkConnected(getActivity())){

				String imageDataOriginal = "";

				ByteArrayOutputStream baos = new ByteArrayOutputStream();  
				byte[] byteArrayImage = baos.toByteArray(); 
				source.compress(Bitmap.CompressFormat.JPEG, 75, baos);  
				byteArrayImage = baos.toByteArray(); 
				imageDataOriginal = Base64.encodeToString(byteArrayImage,Base64.NO_WRAP);

				retriever = new RetrieveTags(this.getActivity(),this);
				retriever.execute(imageDataOriginal);
			}else{
				Toast.makeText(getActivity(),R.string.nonetwork,Toast.LENGTH_SHORT).show();
				this.dismissLoading();
			}
		}

	}

	@Override
	public void onSucess(List<String> tags) {
		if(!this.isRemoving()){
			StringBuilder builder = new StringBuilder();
			for(String tag:tags)
				builder.append(tag+"; ");
			tags_view.setText(builder.toString());
			dismissLoading();
			TagManager.addTag(getActivity(), SmallUtils.getPathFromImageLoader(targetPath), tags);
		}
	}

	@Override
	public void onFail(String debug) {
		if(!this.isRemoving()){
			if(debug != null){
				AlertDialog.Builder helpBuilder = new AlertDialog.Builder(getActivity());
				helpBuilder.setTitle(this.getString(R.string.retrieve_failed));
				helpBuilder.setMessage(debug);
				helpBuilder.setPositiveButton(R.string.ok,null);

				AlertDialog helpDialog = helpBuilder.create();
				helpDialog.show();
			}else{
				AlertDialog.Builder helpBuilder = new AlertDialog.Builder(getActivity());

				helpBuilder.setTitle(this.getString(R.string.retrieve_failed));
				helpBuilder.setMessage(this.getString(R.string.unknown_erorr));
				//helpBuilder.setTitle(this.getString(R.string.connectfailed));
				helpBuilder.setPositiveButton(R.string.ok,null);

				AlertDialog helpDialog = helpBuilder.create();
				helpDialog.show();
			}
			dismissLoading();
		}
	}

	@Override
	public void onPageScrollStateChanged(int position) {

	}

	@Override
	public void onPageScrolled(int position, float x, int y) {

	}

	@Override
	public void onPageSelected(int newPosition) {
		targetPath = imageUrls[newPosition];
		position = newPosition;
		source = ImageDecoder.resizeBitmapToRightSize(getActivity(), 
				ImageLoader.getInstance().loadImageSync(imageUrls[position]));
		sendImageForTag();
	}
}