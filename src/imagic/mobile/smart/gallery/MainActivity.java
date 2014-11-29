package imagic.mobile.smart.gallery;

import imagic.mobile.ui.MagicTextView;
import imagic.mobile.utils.Typefaces;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint.Join;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

	private MagicTextView text_app_name;

	// Splash screen timer
	private static int SPLASH_TIME_OUT = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_main);

		Typeface mFont = Typefaces.get(this, "fonts/pipe.ttf");

		if(mFont != null){
			text_app_name = (MagicTextView) this.findViewById(R.id.text_app_name);
			text_app_name.setTypeface(Typefaces.get(this, "fonts/pipe.ttf"));
		}
		text_app_name.setStroke(4, this.getResources().getColor(R.color.title_border_color), Join.ROUND, 5);

		new Handler().postDelayed(new Runnable() {

			/*
			 * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */

			@Override
			public void run() {
				// This method will be executed once the timer is over
				// Start your app main activity
				Intent intent = new Intent(MainActivity.this, TagListActivity.class);
				startActivity(intent);

				// close this activity
				finish();
			}
		}, SPLASH_TIME_OUT);

	}
}
