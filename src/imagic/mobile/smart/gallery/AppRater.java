package imagic.mobile.smart.gallery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class AppRater {

	private final static int LAUNCHES_UNTIL_PROMPT = 2;

	public static void app_launched(Context mContext) {

		SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);

		SharedPreferences.Editor editor = prefs.edit();

		// Increment launch counter
		long launch_count = prefs.getLong("launch_count", 0) + 1;
		if(launch_count <= LAUNCHES_UNTIL_PROMPT*2.0)
			editor.putLong("launch_count", launch_count);

		if (launch_count > LAUNCHES_UNTIL_PROMPT && !prefs.getBoolean("dontshowagain", false)) {
			showRateDialog(mContext, editor);
		}

		editor.commit();
	}   

	public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
		final AlertDialog.Builder helpBuilder = new AlertDialog.Builder(mContext);
		helpBuilder.setMessage(mContext.getString(R.string.rate_us));
		helpBuilder.setCancelable(false);
		helpBuilder.setPositiveButton(R.string.rate_yes,
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" 
						+ mContext.getString(R.string.app_package_name))));
				if (editor != null) {
					editor.putBoolean("dontshowagain", true);
					editor.commit();
				}
				dialog.dismiss();
			}
		});
		helpBuilder.setNeutralButton(R.string.rate_later, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				if (editor != null) {
					editor.putLong("launch_count", 0);
					editor.commit();
				}
				dialog.dismiss();
			}
		});

		AlertDialog helpDialog = helpBuilder.create();
		helpDialog.show();          
	}
}