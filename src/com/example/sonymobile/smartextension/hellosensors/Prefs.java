package com.example.sonymobile.smartextension.hellosensors;

import android.content.Context;
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
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
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
}
