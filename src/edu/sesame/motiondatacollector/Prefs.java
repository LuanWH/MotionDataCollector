package edu.sesame.motiondatacollector;

import java.util.Locale;
import java.util.Set;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

public class Prefs extends PreferenceActivity {
	public static final int FILE_FORMAT_JSON_INDEX = 0;
	public static final int FILE_FORMAT_CSV_INDEX = 1;
	public static final int FILE_FORMAT_DAT_INDEX = 2;
	
	private static final String OPT_STORAGE = "store_in_file";
	private static final boolean OPT_STORAGE_DEF = true;	
	private static final String OPT_NUMBERS = "number_of_data_per_file";
	private static final String OPT_NUMBERS_DEF = "1000";		
	private static final String OPT_COUNT_INTERVAL = "count_interval";
	private static final String OPT_COUNT_INTERVAL_DEF = "2";
	private static final String OPT_FILTER = "gravity_filter";
	private static final boolean OPT_FILTER_DEF = true;
	private static final String OPT_PARAMETER = "parameter";
	private static final String OPT_PARAMETER_DEF = "0.8";
	private static final String OPT_MATCHING_FREQUENCY = "match_pattern_frequency";
	private static final String OPT_MATCHING_FREQUENCY_DEF = "10";
	public static final String FILE_NAME = "curent_file";
	private static final String OPT_LANGUAGE = "language";
	private static final String OPT_LANGUAGE_DEF = "0";	
	private static final String OPT_QUEUE_LENGTH = "queue_length";
	private static final String OPT_QUEUE_LENGTH_DEF = "30";	
	private static final String OPT_DATA_FILE_FORMAT = "file_format";
	private static final String OPT_DATA_FILE_FORMAT_DEF = String.valueOf(FILE_FORMAT_JSON_INDEX);		
	
	
	ActionBar actionBar;
	CheckBoxPreference storage;
	EditTextPreference parameter;
	ListPreference language;
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		actionBar = getActionBar();
		actionBar.show();
		actionBar.setDisplayHomeAsUpEnabled(true);	
		storage = (CheckBoxPreference) getPreferenceScreen().findPreference(OPT_STORAGE);
		parameter = (EditTextPreference) getPreferenceScreen().findPreference(OPT_PARAMETER);
		language = (ListPreference)getPreferenceScreen().findPreference(OPT_LANGUAGE);
		
	}
	@Override
	public void onResume(){
		super.onResume();
		storage.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if((Boolean) newValue){
					new AlertDialog.Builder(Prefs.this)
						.setTitle("Information")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setMessage("Data file path is SD/Android/data/edu.sesame.motiondatacollector/files")
						.setPositiveButton("Ok", null)
						.show();
				}
				return true;
			}});
		parameter.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				String value = (String) newValue;
				try{
					Float.valueOf(value);
				} catch(NumberFormatException e){
					new AlertDialog.Builder(Prefs.this)
						.setTitle("Warning")
						.setMessage("Illegal input! Value will not be updated.")
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton("Ok", null)
						.setCancelable(true)
						.show();
					return false;
				}
				return true;
			}
			
		});
		language.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				String s = (String) newValue;
				if(s.equals("1")){
					Resources res = Prefs.this.getResources();
					DisplayMetrics dm = res.getDisplayMetrics();
					android.content.res.Configuration conf = res.getConfiguration();
					conf.locale = new Locale("zh");
					res.updateConfiguration(conf, dm);
				}else{
					Resources res = Prefs.this.getResources();
					DisplayMetrics dm = res.getDisplayMetrics();
					android.content.res.Configuration conf = res.getConfiguration();
					conf.locale = new Locale("en");
					res.updateConfiguration(conf, dm);					
				}
				restart();
				return true;
			}
			
		});
	}
	public static int getFileFormat(Context context){
		return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
				.getString(OPT_DATA_FILE_FORMAT, OPT_DATA_FILE_FORMAT_DEF));
	}
	public static int getQueueLength(Context context){
		return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
				.getString(OPT_QUEUE_LENGTH, OPT_QUEUE_LENGTH_DEF));
	}		
	public static int getMatchingFrequency(Context context){
		return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
				.getString(OPT_MATCHING_FREQUENCY, OPT_MATCHING_FREQUENCY_DEF));
	}	
	public static float getParameter(Context context){
		return Float.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
				.getString(OPT_PARAMETER, OPT_PARAMETER_DEF));
	}
	
	public static boolean getFilter(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(OPT_FILTER, OPT_FILTER_DEF);
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
	public static String getString(String key, Context context){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getString(key, null);		
	}
	public static void setString(String key, String value, Context context){
		SharedPreferences  prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.commit();
	}	
	public static void setInteger(String key, int value, Context context){
		SharedPreferences  prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	public static int getInteger(String key, Context context){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getInt(key, 0);
	}
	private void restart(){
		finish();
		startActivity(getIntent());
	}
	
	@Override
	public void onBackPressed(){
		finish();
		Intent i = new Intent(this, StartMenu.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}
}
