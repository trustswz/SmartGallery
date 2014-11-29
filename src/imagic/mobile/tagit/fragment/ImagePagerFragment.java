package imagic.mobile.tagit.fragment;

import imagic.mobile.network.RetrieveTags;
import imagic.mobile.smart.gallery.ImageActivity;
import imagic.mobile.smart.gallery.R;
import imagic.mobile.ui.DisableableViewPager;
import imagic.mobile.ui.MagicButton;
import imagic.mobile.utils.Constants;
import imagic.mobile.utils.ImageDecoder;
import imagic.mobile.utils.RetrieveTagsListener;
import imagic.mobile.utils.SingleMediaScanner;
import imagic.mobile.utils.SmallUtils;
import imagic.mobile.utils.TagManager;
import imagic.mobile.utils.Typefaces;

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
import android.graphics.Typeface;
import android.graphics.Paint.Join;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

	private MagicButton tagButton1;

	private MagicButton tagButton2;

	private MagicButton tagButton3;

	private MagicButton tagButton4;

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

		View rootView = CreateView(inflater,container);

		setAdapter(new ImageAdapter());
		getPager().setAdapter(getAdapter());
		setPosition(getArguments().getInt(Constants.Extra.IMAGE_POSITION, 0));
		getPager().setCurrentItem(getPosition());
		setTargetPath(imageUrls[getPosition()]);
		setSource(ImageDecoder.resizeBitmapToRightSize(getActivity(), 
				ImageLoader.getInstance().loadImageSync(imageUrls[getPosition()])));
		sendImageForTag();

		getPager().setOnPageChangeListener(this);

		return rootView;
	}

	protected View CreateView(LayoutInflater inflater, ViewGroup container) {
		View rootView = inflater.inflate(R.layout.fr_image_pager, container, false);

		setmRootView((RelativeLayout) rootView.findViewById(R.id.rootView));

		tagButton1 = (MagicButton) rootView.findViewById(R.id.TagButton1);
		tagButton2 = (MagicButton) rootView.findViewById(R.id.TagButton2);
		tagButton3 = (MagicButton) rootView.findViewById(R.id.TagButton3);
		tagButton4 = (MagicButton) rootView.findViewById(R.id.TagButton4);

		Typeface mFont = Typefaces.get(getActivity(), "fonts/pipe.ttf");

		if(mFont != null){
			tagButton1.setTypeface(mFont);
			tagButton2.setTypeface(mFont);
			tagButton3.setTypeface(mFont);
			tagButton4.setTypeface(mFont);
		}
		tagButton1.setStroke(2, this.getResources().getColor(R.color.text_border_color), Join.ROUND, 5);
		tagButton2.setStroke(2, this.getResources().getColor(R.color.text_border_color), Join.ROUND, 5);
		tagButton3.setStroke(2, this.getResources().getColor(R.color.text_border_color), Join.ROUND, 5);
		tagButton4.setStroke(2, this.getResources().getColor(R.color.text_border_color), Join.ROUND, 5);


		setLoadingView((ProgressBar) rootView.findViewById(R.id.loadingimage));

		setPager((DisableableViewPager) rootView.findViewById(R.id.pager));

		return rootView;
	}

	class ImageAdapter extends PagerAdapter {

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

		setMenuItemDelete(menu.findItem(R.id.menu_item_delete));
		setMenuItemSimilar(menu.findItem(R.id.menu_item_similar));
		setMenuItemShare(menu.findItem(R.id.menu_item_share));

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
		if(getTargetPath() != null){

			final AlertDialog.Builder confirmDeleteDialogBuilder = new AlertDialog.Builder(getActivity());
			confirmDeleteDialogBuilder.setMessage(getActivity().getString(R.string.delete_image));
			confirmDeleteDialogBuilder.setCancelable(false);
			confirmDeleteDialogBuilder.setPositiveButton(R.string.confirm,
					new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					File fdelete = new File(SmallUtils.getPathFromImageLoaderFormat(getTargetPath()));
					if (fdelete.exists()) {
						if (fdelete.delete()) {

							//remove the file from the imageUrls
							final List<String> newImagelist =  new ArrayList<String>();
							Collections.addAll(newImagelist, imageUrls); 
							newImagelist.remove(getTargetPath());
							imageUrls = newImagelist.toArray(new String[newImagelist.size()]);

							//remove from the system
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
								Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
								File f = new File(getTargetPath());
								Uri contentUri = Uri.fromFile(f);
								mediaScanIntent.setData(contentUri);
								getActivity().sendBroadcast(mediaScanIntent);
							} else {
								getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/" + "FOLDER_TO_REFRESH")));
							}

							//remove from tag manager
							TagManager.deleteFile(getActivity(), getTargetPath());

							getPager().setAdapter(getAdapter());

							if(getPosition() > 0)
								setPosition(getPosition() - 1);
							getPager().setCurrentItem(getPosition());
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
		if(getTargetPath() != null){
			Intent intent = new Intent(getActivity(), ImageActivity.class);
			intent.putExtra(Constants.Extra.IMAGE_TAG_NAME, SmallUtils.getPathFromImageLoaderFormat(getTargetPath()));
			startActivity(intent);
		}
	}

	public void ShareResult(){
		getmRootView().setDrawingCacheEnabled(true);
		getmRootView().buildDrawingCache(true);
		Bitmap bitmap = this.getmRootView().getDrawingCache();

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

		getmRootView().setDrawingCacheEnabled(false);

		// now share
		String[] blacklist = new String[]{this.getString(R.string.app_package_name)};

		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("image/jpeg");
		sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
		sendIntent.putExtra(Intent.EXTRA_TEXT, tagButton1.getText() + " " + tagButton2.getText()
				+ " " + tagButton3.getText() + " " + tagButton4.getText() + "\n" + 
				getString(R.string.share_string) + getString(R.string.app_package_name));
		startActivity(SmallUtils.generateCustomChooserIntent(getActivity(),sendIntent, blacklist));
	}

	protected void startLoading() {
		if(getMenuItemDelete() != null)
			getMenuItemDelete().setEnabled(false);
		if(getMenuItemSimilar() != null)
			getMenuItemSimilar().setEnabled(false);
		if(getMenuItemShare() != null)
			getMenuItemShare().setEnabled(false);

		if(getLoadingView() != null){
			getLoadingView().setVisibility(View.VISIBLE);
			getLoadingView().invalidate();
		}

		if(getPager() != null)
			getPager().setPagingEnabled(false);

		if(this.tagButton1 != null){
			this.tagButton1.setClickable(false);
		}

		if(this.tagButton2 != null){
			this.tagButton2.setClickable(false);
		}

		if(this.tagButton3 != null){
			this.tagButton3.setClickable(false);
		}

		if(this.tagButton4 != null){
			this.tagButton4.setClickable(false);
		}

	}

	private void dismissLoading() {

		if(getMenuItemDelete()!=null)
			getMenuItemDelete().setEnabled(true);
		if(getMenuItemSimilar()!=null)
			getMenuItemSimilar().setEnabled(true);
		if(getMenuItemShare()!=null)
			getMenuItemShare().setEnabled(true);

		if(getLoadingView() != null){
			getLoadingView().setVisibility(View.GONE);
			getLoadingView().invalidate();
		}

		if(getPager() != null)
			getPager().setPagingEnabled(true);

		if(this.tagButton1 != null){
			this.tagButton1.setClickable(true);
		}

		if(this.tagButton2 != null){
			this.tagButton2.setClickable(true);
		}

		if(this.tagButton3 != null){
			this.tagButton3.setClickable(true);
		}

		if(this.tagButton4 != null){
			this.tagButton4.setClickable(true);
		}
	}

	protected void sendImageForTag() {

		if(getSource() != null){

			this.startLoading();

			Set<String> tags = TagManager.getFileTagList(getActivity(),
					SmallUtils.getPathFromImageLoaderFormat(getTargetPath()));

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
				getSource().compress(Bitmap.CompressFormat.JPEG, 75, baos);  
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
		if(!this.isRemoving() && getActivity() != null && !getActivity().isFinishing()){
			populateTagViews(tags);
			dismissLoading();
			TagManager.addTag(getActivity(), SmallUtils.getPathFromImageLoaderFormat(getTargetPath()), tags);
		}
	}

	protected void populateTagViews(List<String> tags) {
		int count = 0;
		for(String tag:tags){
			if(count < 4){
				switch(count){
				case 0:
					tagButton1.setText(tag);
					tagButton1.setVisibility(View.VISIBLE);
					tagButton1.invalidate();
					break;
				case 1:
					tagButton2.setText(tag);
					tagButton2.setVisibility(View.VISIBLE);
					tagButton2.invalidate();
					break;
				case 2:
					tagButton3.setText(tag);
					tagButton3.setVisibility(View.VISIBLE);
					tagButton3.invalidate();
					break;
				case 3:
					tagButton4.setText(tag);
					tagButton4.setVisibility(View.VISIBLE);
					tagButton4.invalidate();
					break;
				}
				count++;
			}else{
				break;
			}
		}
	}

	protected void hideAllTagViews() {
		tagButton2.setVisibility(View.GONE);
		tagButton2.invalidate();
		tagButton3.setVisibility(View.GONE);
		tagButton3.invalidate();
		tagButton4.setVisibility(View.GONE);
		tagButton4.invalidate();

		tagButton1.setText(getActivity().getString(R.string.predicted_tags));
		tagButton1.setVisibility(View.VISIBLE);
		tagButton1.invalidate();
	}

	@Override
	public void onFail(String debug) {
		if(!this.isRemoving() && getActivity() != null && !getActivity().isFinishing()){
			hideAllTagViews();
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
		setTargetPath(imageUrls[newPosition]);
		setPosition(newPosition);
		setSource(ImageDecoder.resizeBitmapToRightSize(getActivity(), 
				ImageLoader.getInstance().loadImageSync(imageUrls[getPosition()])));
		sendImageForTag();
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	public RelativeLayout getmRootView() {
		return mRootView;
	}

	public void setmRootView(RelativeLayout mRootView) {
		this.mRootView = mRootView;
	}

	public ProgressBar getLoadingView() {
		return loadingView;
	}

	public void setLoadingView(ProgressBar loadingView) {
		this.loadingView = loadingView;
	}

	public DisableableViewPager getPager() {
		return pager;
	}

	public void setPager(DisableableViewPager pager) {
		this.pager = pager;
	}

	public ImageAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(ImageAdapter adapter) {
		this.adapter = adapter;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Bitmap getSource() {
		return source;
	}

	public void setSource(Bitmap source) {
		if(this.source != null && this.source.isRecycled() != true){
			this.source.recycle();
			this.source = null;
		}
		this.source = source;
	}

	public MenuItem getMenuItemDelete() {
		return menuItemDelete;
	}

	public void setMenuItemDelete(MenuItem menuItemDelete) {
		this.menuItemDelete = menuItemDelete;
	}

	public MenuItem getMenuItemSimilar() {
		return menuItemSimilar;
	}

	public void setMenuItemSimilar(MenuItem menuItemSimilar) {
		this.menuItemSimilar = menuItemSimilar;
	}

	public MenuItem getMenuItemShare() {
		return menuItemShare;
	}

	public void setMenuItemShare(MenuItem menuItemShare) {
		this.menuItemShare = menuItemShare;
	}

	public void onTagClicked(View v) {
		AlertDialog.Builder helpBuilder = new AlertDialog.Builder(getActivity());
		helpBuilder.setTitle(getActivity().getString(R.string.under_development));
		helpBuilder.setMessage(getActivity().getString(R.string.edit_tag_in_future));
		helpBuilder.setPositiveButton(R.string.ok,null);

		AlertDialog helpDialog = helpBuilder.create();
		helpDialog.show();
	}
}