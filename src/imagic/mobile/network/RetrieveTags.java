package imagic.mobile.network;

import imagic.mobile.smart.gallery.R;
import imagic.mobile.utils.RetrieveTagsListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

//public class RegisterNet extends Activity {
public class RetrieveTags extends AsyncTask<String, Integer, JSONObject> {
	private Context mContext;
	private JSONObject mJson;
	private String retCode;
	private List<String> tags;
	private String debug;
	private RetrieveTagsListener mListener;
	/**
	 * constructor
	 */
	public RetrieveTags(Context applicationContext,RetrieveTagsListener listener) {
		mContext = applicationContext;
		mListener = listener;
		mJson = new JSONObject();
	}

	/**
	 * background
	 */
	@Override
	protected JSONObject doInBackground(String... params) {

		String uploadURL = "http://"+mContext.getString(R.string.clarifaidomain);
		uploadURL = uploadURL + "/clarifai.php";

		List <NameValuePair> postURL=new ArrayList<NameValuePair>();
		postURL.add(new BasicNameValuePair("image",params[0]));

		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		int timeoutConnection = 5000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 20000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		HttpClient httpclient = new DefaultHttpClient(httpParameters);
		HttpPost httppost = new HttpPost(uploadURL);

		HttpResponse response;
		String jsonResponse = null;

		try{  
			//if sending json object this need
			//httppost.setHeader("Content-Type",
            //        "application/json");
			
			httppost.setEntity(new UrlEncodedFormEntity(postURL,HTTP.UTF_8));

			response = httpclient.execute(httppost);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

			StringBuilder stringBuilder = new StringBuilder();

			String bufferedStrChunk = null;

			while((bufferedStrChunk = reader.readLine()) != null){
				stringBuilder.append(bufferedStrChunk);
			}

			jsonResponse = stringBuilder.toString();

		}
		catch(Exception e){
			e.getMessage();
		}

		mJson = null;
		// try parse the string to a JSON object
		try {
			if(jsonResponse != null)
				mJson = new JSONObject(jsonResponse);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		return mJson;
	}


	/**
	 * on getting result
	 */
	@Override
	protected void onPostExecute(JSONObject result) {

		if(mJson != null){
			try {
				retCode = mJson.getString("status_code");
				debug = mJson.getString("status_msg");
			} catch (JSONException e) {
				mListener.onFail(null);
			}

			if(retCode.equals("OK"))
			{
				try {
					JSONArray results = mJson.getJSONArray("results");
					JSONObject imageresult = results.getJSONObject(0);
					JSONObject contents = imageresult.getJSONObject("result");
					JSONObject tagresult = contents.getJSONObject("tag");
					JSONArray classes = tagresult.getJSONArray("classes");
					JSONArray probs = tagresult.getJSONArray("probs"); 
					tags = new ArrayList<String>();
					for(int i = 0; i < classes.length(); i++){
						if(i < 4 || probs.getDouble(i) > 0.1)
							tags.add(classes.getString(i));
					}
				} catch (JSONException e) {
					mListener.onFail(null);
				}
				//save results
				if(mContext != null){
					mListener.onSucess(tags);
				}
			}else{
				if(mContext != null){
					mListener.onFail(debug);
				}
			}
		}else{
			if(mContext != null){
				mListener.onFail(null);
			}
		}
	}

}