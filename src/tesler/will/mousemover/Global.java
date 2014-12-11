package tesler.will.mousemover;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Global {

	static Activity cont;

	private final static boolean DEBUG = true;

	// Top Action Bar Views
	public static ActionBar ab;
	public static ImageView icon;
	public static TextView title;
	public static TextView version;

	// Preferences for the App
	private static SharedPreferences pref;

	Global(Activity cont) {

		Global.cont = cont;

		pref = cont.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);

		// ab = cont.getActionBar();
		// ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		// ab.setCustomView(R.layout.banner_layout);
		// ab.setDisplayHomeAsUpEnabled(true);

		// icon = (ImageView) cont.findViewById(R.id.iv_icon);
		//
		// title = (TextView) cont.findViewById(R.id.tv_title);
		// title.setText("Home");
		//
		// version = (TextView) cont.findViewById(R.id.tv_version);
		// version.setText("0.1");

	}

	public static String load(String key) {
		return pref.getString(key, "");
	}

	public static void save(String key, String val) {
		SharedPreferences.Editor e = pref.edit();

		e.putString(key, val);

		e.commit();

		// Log.i("Global", "Saved " + key + ", " + val);
	}

	public static void clearPreferences() {

		SharedPreferences.Editor e = pref.edit();

		// e.putString(Keys.ID, null);

		e.commit();

	}

	public static void beginMajorServices() {

		Log.i("Global", "Starting All Major Services");

	}

	public static void stopMajorServices() {

		Log.i("Global", "Stopping All Major Services");

	}

	public static void hideSoftKeyboard() {

		try {
			InputMethodManager inputMethodManager = (InputMethodManager) Global.cont
					.getSystemService(Activity.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(Global.cont
					.getCurrentFocus().getWindowToken(), 0);
		} catch (NullPointerException npe) {
			I("Could not hide the soft keyboard");
		}
	}

	public static void I(String text) {

		String tag = "Default";

		try {
			StackTraceElement[] stackTraceElements = Thread.currentThread()
					.getStackTrace();

			tag = stackTraceElements[stackTraceElements.length - 1]
					.getClassName();

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (DEBUG) {
			Log.i(tag, text);
		}
	}

	public static void W(String text) {

		String tag = "Default";

		try {
			StackTraceElement[] stackTraceElements = Thread.currentThread()
					.getStackTrace();

			tag = stackTraceElements[stackTraceElements.length - 1]
					.getClassName();

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (DEBUG) {
			Log.w(tag, text);
		}
	}

	// Make every view hide the soft keyboard when pressed
	public static void sensitizeUI(View view) {

		// Set up touch listener for non-text box views to hide keyboard.
		if (!(view instanceof EditText)) {

			view.setOnTouchListener(new OnTouchListener() {

				public boolean onTouch(View v, MotionEvent event) {
					hideSoftKeyboard();
					return false;
				}

			});
		}

		// If a layout container, iterate over children and seed recursion.
		if (view instanceof ViewGroup) {

			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

				View innerView = ((ViewGroup) view).getChildAt(i);

				sensitizeUI(innerView);
			}
		}
	}

	public static String time() {
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM d yy - hh:mm",
				Locale.getDefault());

		return sdf.format(cal.getTime());
	}

	public static void toast(String message, int length) {
		Toast.makeText(cont, message, length).show();
		;
	}
}