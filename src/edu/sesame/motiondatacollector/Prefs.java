package edu.sesame.motiondatacollector;

import java.util.Set;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Prefs extends PreferenceActivity {
	private static final String OPT_DISPLAY = "display_data";
	private static final boolean OPT_DISPLAY_DEF = false;
	private static final String OPT_STORAGE = "store_in_file";
	private static final boolean OPT_STORAGE_DEF = true;	
	private static final String OPT_NUMBERS = "number_of_data_per_file";
	private static final String OPT_NUMBERS_DEF = "1000";		
	private static final String OPT_COUNT_INTERVAL = "count_interval";
	private static final String OPT_COUNT_INTERVAL_DEF = "2";
	ActionBar actionBar;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		actionBar = getActionBar();
		actionBar.show();
		actionBar.setDisplayHomeAsUpEnabled(true);		
	}
	public static boolean getDisplay(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(OPT_DISPLAY, OPT_DISPLAY_DEF);
	}
	public static boolean getStorage(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(OPT_STORAGE, OPT_STORAGE_DEF);
	}
	public static String getNumbers(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(OPT_NUMBERS, OPT_NUMBERS_DEF);
	}
	public static int getCountInterval(Context context){
		return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
				.getString(OPT_COUNT_INTERVAL, OPT_COUNT_INTERVAL_DEF));
	}
	public static void setDefaults(String key, Set<String> value, Context context){
		SharedPreferences  prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putStringSet(key, value);
		editor.commit();
	}
	public static Set<String> getDefaults(String key, Context context){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getStringSet(key, null);
	}
}
