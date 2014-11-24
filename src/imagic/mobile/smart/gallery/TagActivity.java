package imagic.mobile.smart.gallery;

import imagic.mobile.network.RetrieveTags;
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
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

public class TagActivity extends Activity implements RetrieveTagsListener {

	private static final int LOAD_REQUEST = 1;

	private TextView tags_view;
	private Uri targetUri;
	public Bitmap source;
	private RetrieveTags retriever;
	private ProgressBar loadingView;
	private Bitmap orientedSource;
	private String restoredtags;
	private RelativeLayout mRootView;

	private ImageView imageview;

	private MenuItem menuItemSimilar;

	private MenuItem menuItemShare;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get intent, action and MIME type
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				handleSendImage(intent); // Handle single image being sent
			}
		}

		getDatafromUri();

		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_tag);

		mRootView = (RelativeLayout) this.findViewById(R.id.rootView);

		tags_view = (TextView) this.findViewById(R.id.textView);

		imageview = (ImageView) this.findViewById(R.id.imageView);
		imageview.setImageBitmap(orientedSource);

		loadingView = (ProgressBar)findViewById(R.id.loadingimage);

		this.onLoad(null);

		AppRater.app_launched(this);

	}

	@Override
	protected void onResume() {
		super.onResume();

		if(restoredtags != null && !restoredtags.equals("")
				&& !restoredtags.equals(getString(R.string.predicted_tags))){
			tags_view.setText(restoredtags);
		}

	}

	private void startLoading() {
		if(menuItemSimilar != null)
			menuItemSimilar.setEnabled(false);
		if(menuItemShare != null)
			menuItemShare.setEnabled(false);

		loadingView.setVisibility(View.VISIBLE);
		loadingView.invalidate();
	}

	@Override
	protected void onPause() {
		this.dismissLoading();
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		savedInstanceState.putString("tags", tags_view.getText().toString());
		// etc.
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		if(savedInstanceState != null){
			String tags = (String) savedInstanceState.getString("tags");
			if(tags != null){
				restoredtags = tags;
			}
		}
	}

	private void sendImageForTag() {

		Set<String> tags = TagManager.getFileTagList(this,SmallUtils.uri2String(targetUri));

		if(tags != null){
			List<String> tagList = new ArrayList<String>();
			for(String tag:tags){
				tagList.add(tag);
			}
			onSucess(tagList);
		}else{

			String imageDataOriginal = "";

			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			byte[] byteArrayImage = baos.toByteArray(); 
			source.compress(Bitmap.CompressFormat.JPEG, 75, baos);  
			byteArrayImage = baos.toByteArray(); 
			imageDataOriginal = Base64.encodeToString(byteArrayImage,Base64.NO_WRAP);

			retriever = new RetrieveTags(TagActivity.this,this);
			retriever.execute(imageDataOriginal);
		}

	}

	public void handleSendImage(Intent intent) {
		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (imageUri != null) {
			targetUri = imageUri;
		}
	}

	public void getDatafromUri(){
		if(this.source != null){
			this.source = null;
		}
		if(this.orientedSource != null){
			this.orientedSource = null;
		}

		if(source != null){
			source.recycle();
			source = null;
		}
		source = ImageDecoder.decodeFile(this,targetUri);

		int orientation = getOrientation(this, targetUri);

		// rotate the source if width > height
		if(source.getWidth() > source.getHeight()){
			Bitmap targetBitmap = Bitmap.createBitmap(source.getHeight(), source.getWidth(), Config.RGB_565);
			Canvas canvas = new Canvas(targetBitmap);
			if(orientation == 270 ){
				canvas.rotate(270);
				canvas.drawBitmap(source, -source.getWidth(), 0, null);
			}else{
				canvas.rotate(90);
				canvas.drawBitmap(source, 0, -source.getHeight(), null);
			}

			orientedSource = Bitmap.createScaledBitmap(targetBitmap,targetBitmap.getWidth(), targetBitmap.getHeight(), false);
			targetBitmap.recycle();
		}else{
			orientedSource = Bitmap.createBitmap(source);
		}
	}

	public static int getOrientation(Context context, Uri photoUri) {
		/* it's on the external media. */
		Cursor cursor = context.getContentResolver().query(photoUri,
				new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

		if (cursor == null || cursor.getCount() != 1) {
			return -1;
		}

		cursor.moveToFirst();
		int orientation = cursor.getInt(0);
		cursor.close();
		return orientation;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  

		int ok = 0;
		if (requestCode == LOAD_REQUEST && resultCode == Activity.RESULT_OK){	
			Uri outputFileUri = data.getData();
			ok = SmallUtils.checkSource(outputFileUri);
			if(ok == 1){
				targetUri = outputFileUri;
				getDatafromUri();
				imageview.setImageBitmap(orientedSource);
				this.onLoad(null);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode == Activity.RESULT_OK && ok != 1){
			Toast.makeText(this, "Load image failed, Uri not found!", Toast.LENGTH_LONG).show();
		}

	}

	public void onShare(View v){
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

			new SingleMediaScanner(this, file);
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
		startActivity(SmallUtils.generateCustomChooserIntent(this,sendIntent, blacklist));
	}

	public void onLoad(View v){
		boolean serveravailable = SmallUtils.isNetworkConnected(this);
		if(serveravailable){
			this.startLoading();
			sendImageForTag();
		}else{
			Toast.makeText(this,R.string.nonetwork,Toast.LENGTH_SHORT).show();
			this.dismissLoading();
		}
	}

	@Override
	public void onFail(String debug) {
		if(!isFinishing()){
			if(debug != null){
				AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
				helpBuilder.setTitle(this.getString(R.string.retrieve_failed));
				helpBuilder.setMessage(debug);
				helpBuilder.setPositiveButton(R.string.ok,null);

				AlertDialog helpDialog = helpBuilder.create();
				helpDialog.show();
			}else{
				AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);

				helpBuilder.setTitle(this.getString(R.string.connectfailed));
				helpBuilder.setPositiveButton(R.string.ok,null);

				AlertDialog helpDialog = helpBuilder.create();
				helpDialog.show();
			}
			dismissLoading();
		}
	}

	private void dismissLoading() {

		if(menuItemSimilar!=null)
			menuItemSimilar.setEnabled(true);
		if(menuItemShare!=null)
			menuItemShare.setEnabled(true);

		loadingView.setVisibility(View.GONE);
		loadingView.invalidate();
	}

	@Override
	public void onSucess(List<String> tags) {
		if(!isFinishing()){
			StringBuilder builder = new StringBuilder();
			for(String tag:tags)
				builder.append(tag+"; ");
			tags_view.setText(builder.toString());
			dismissLoading();
			TagManager.addTag(this, FileUtils.getPath(this, targetUri), tags);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tag_activity_menu, menu);

		menuItemSimilar = menu.findItem(R.id.menu_item_similar);
		menuItemShare = menu.findItem(R.id.menu_item_share);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_item_similar:
			this.searchSimilar();
			return true;
		case R.id.menu_item_share:
			this.onShare(null);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(TagActivity.this, TagListActivity.class);
		startActivity(intent);
		// close this activity
		finish();
	}

	private void searchSimilar() {
		Intent intent = new Intent(this, ImageActivity.class);
		intent.putExtra(Constants.Extra.IMAGE_TAG_NAME, FileUtils.getPath(this, targetUri));
		startActivity(intent);
	}

}
