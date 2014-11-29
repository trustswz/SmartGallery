package imagic.mobile.tagit.fragment;

import imagic.mobile.smart.gallery.ImageActivity;
import imagic.mobile.smart.gallery.R;
import imagic.mobile.utils.ConstantInfo;
import imagic.mobile.utils.Constants;
import imagic.mobile.utils.SmallUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

public abstract class AbsTagListViewBaseFragment extends BaseTagListWithImageIconFragment {

	protected static final String STATE_PAUSE_ON_SCROLL = "STATE_PAUSE_ON_SCROLL";
	protected static final String STATE_PAUSE_ON_FLING = "STATE_PAUSE_ON_FLING";

	protected AbsListView gridView;

	protected boolean pauseOnScroll = false;
	protected boolean pauseOnFling = true;
	private Uri outputFileUri = null;
	private Uri capturedUri = null;

	@Override
	public void onResume() {
		super.onResume();
		applyScrollListener();
	}

	protected void startImageActivity(int position) {
		if(this.tagNames != null){
			final String  tagName = this.tagNames[position];
			if(tagName.equals(getActivity().getString(R.string.camera))){
				// create Intent to take a picture and return control to the calling application
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				outputFileUri = getOutputMediaFileUri(ConstantInfo.MEDIA_TYPE_IMAGE); // create a file to save the image
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri); // set the image file name

				// start the image capture Intent
				startActivityForResult(intent, ConstantInfo.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			}else if(tagName.equals(getActivity().getString(R.string.unorganized))){
				final AlertDialog.Builder helpBuilder = new AlertDialog.Builder(getActivity());
				helpBuilder.setMessage(getActivity().getString(R.string.organize_all));
				helpBuilder.setCancelable(false);
				helpBuilder.setPositiveButton(R.string.rate_yes,
						new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(getActivity(), ImageActivity.class);
						intent.putExtra(Constants.Extra.FRAGMENT_INDEX, AutomaticImagePagerFragment.INDEX);
						intent.putExtra(Constants.Extra.IMAGE_POSITION, 0);
						intent.putExtra(Constants.Extra.IMAGE_TAG_NAME, tagName);
						intent.putExtra(Constants.Extra.IMAGE_URLS, imageUrls);
						startActivity(intent);
						dialog.dismiss();
					}
				});
				helpBuilder.setNeutralButton(R.string.rate_later, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(getActivity(), ImageActivity.class);
						intent.putExtra(Constants.Extra.IMAGE_TAG_NAME, tagName);
						startActivity(intent);
						dialog.dismiss();
					}
				});

				AlertDialog helpDialog = helpBuilder.create();
				helpDialog.show();    
			}else{
				Intent intent = new Intent(getActivity(), ImageActivity.class);
				intent.putExtra(Constants.Extra.IMAGE_TAG_NAME, tagName);
				startActivity(intent);
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		int ok = 0;
		if(requestCode == ConstantInfo.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
			getActivity().getContentResolver().notifyChange(outputFileUri, null);
			ok = checkSource(outputFileUri);
			if(ok == 1){
				// rescan to get the correct uri with orientation tags
				reScan();
				Toast.makeText(getActivity(), "Loading image with orientation!", Toast.LENGTH_LONG).show();
			}
		}

		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode == Activity.RESULT_OK && ok != 1){
			Toast.makeText(getActivity(), "Load image failed, Uri not found!", Toast.LENGTH_LONG).show();
		}

	}

	private void applyScrollListener() {
		gridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), pauseOnScroll, pauseOnFling));
	}

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "SmartGallery");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == ConstantInfo.MEDIA_TYPE_IMAGE){
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_"+ timeStamp + ".jpg");
		} else if(type == ConstantInfo.MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_"+ timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}


	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		savedInstanceState.putParcelable("CapturedUri", capturedUri);
		savedInstanceState.putParcelable("OutputUri", outputFileUri);
		// etc.
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		if(savedInstanceState != null){
			capturedUri = (Uri) savedInstanceState.getParcelable("CapturedUri");
		}else{
			capturedUri = null;
		}
		if(savedInstanceState != null){
			outputFileUri = (Uri) savedInstanceState.getParcelable("OutputUri");
		}else{
			outputFileUri = null;
		}
	}

	public void reScan(){
		MediaScannerConnection.scanFile(getActivity(), new String[]{outputFileUri.getPath()}, 
				null, new OnScanCompletedListener() {
			@Override
			public void onScanCompleted(String path, Uri uri) {

				Intent intent = new Intent(getActivity(), ImageActivity.class);
				
				if(capturedUri == null || checkSource(capturedUri) != 1)
					capturedUri = uri;

				if(checkSource(capturedUri) == 1){
					intent.putExtra(Constants.Extra.FRAGMENT_INDEX, ImagePagerFragment.INDEX);
					intent.putExtra(Constants.Extra.IMAGE_POSITION, 0);
					intent.putExtra(Constants.Extra.IMAGE_TAG_NAME, getActivity().getString(R.string.camera));
					String[] oneImageUrls = new String[1];
					oneImageUrls[0] = SmallUtils.getImageLoaderFormatFromPath(path);
					intent.putExtra(Constants.Extra.IMAGE_URLS, oneImageUrls);
					startActivity(intent);	
				}else{
					Toast.makeText(getActivity(), "Load image failed, Uri not found!", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	public int checkSource(Uri targetUri) {
		if(targetUri.toString().length() > 0){
			return 1;
		}else{
			return 0;
		}
	}
}
