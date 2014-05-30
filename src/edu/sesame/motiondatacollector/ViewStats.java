package edu.sesame.motiondatacollector;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.TextView;

public class ViewStats extends Activity {
	public static final String TOTAL_TIME = "TOTAL_TIME";
	public static final String TOTAL_COUNTS = "TOTAL_COUNTS";
	public static final String TOTAL_ITEMS = "TOTAL_ITEMS";
	public static final String LOGS = "RECORD_LOGS";
	private int time, counts, items;
	private final CharSequence[] clearItems = {"Total Time", "Total Counts", "Total Items", "Logs"};
	private ArrayList<String> logs;
	

	ActionBar actionBar;
	TextView totalTime, totalCounts, totalItems, logView;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_stats_layout);

		
	    try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception ex) {
	        // Ignore
	    }
		actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);		
		actionBar.show();		
		totalTime = (TextView) findViewById(R.id.total_time);
		totalCounts = (TextView) findViewById(R.id.total_counts);
		totalItems = (TextView) findViewById(R.id.total_items);
		logView = (TextView) findViewById(R.id.log_view);
		logView.setText("");
		display();
		
	}
	
	private void display(){
		time = Prefs.getInteger(TOTAL_TIME, getBaseContext());
		totalTime.setText(String.format("%.2f",time/1000.0)+"s");
		counts = Prefs.getInteger(TOTAL_COUNTS, getBaseContext());
		totalCounts.setText(String.valueOf(counts));
		items = Prefs.getInteger(TOTAL_ITEMS, getBaseContext());
		totalItems.setText(String.valueOf(items));
		if(Prefs.getDefaults(LOGS, getBaseContext())!= null){
			logs = new ArrayList<String>(Prefs.getDefaults(LOGS, getBaseContext()));
			for(String i : logs){
				logView.append(i+"\n");
			}
		} else {
			logView.setText("");
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.view_stats_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		super.onOptionsItemSelected(item);
		final ArrayList<Integer> selectedItems = new ArrayList<Integer>();
		switch(item.getItemId()){
		case R.id.action_clear_log:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Select Stats To Be Cleared");
			builder.setMultiChoiceItems(clearItems, null, new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					if(isChecked){
						selectedItems.add(which);
					} else {
						selectedItems.remove(Integer.valueOf(which));
					}
				}
			});
			builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(selectedItems.contains(Integer.valueOf(0))){
						Prefs.setInteger(TOTAL_TIME, 0, ViewStats.this.getBaseContext());
					}
					if(selectedItems.contains(Integer.valueOf(1))){
						Prefs.setInteger(TOTAL_COUNTS, 0, ViewStats.this.getBaseContext());
					}
					if(selectedItems.contains(Integer.valueOf(2))){
						Prefs.setInteger(TOTAL_ITEMS, 0, ViewStats.this.getBaseContext());
					}
					if(selectedItems.contains(Integer.valueOf(3))){
						Prefs.setDefaults(LOGS, null, ViewStats.this.getBaseContext());
					}
					String temp = "";
					if(!selectedItems.isEmpty()){
						int i = 0;
						for(; i < selectedItems.size() - 1;i++){
							temp+= clearItems[selectedItems.get(Integer.valueOf(i))]+", ";
						}
						temp+=clearItems[selectedItems.get(Integer.valueOf(i))];
						new AlertDialog.Builder(ViewStats.this)
						.setTitle("Clear Stats")
						.setMessage(temp+" record cleared!")
						.setPositiveButton("Ok", null)
						.show();
						display();
					}
					

					
				}
				
			});
			builder.setNegativeButton("Cancel", null);
			builder.show();
			break;
		case android.R.id.home:
			ViewStats.this.onBackPressed(); 
			break;
		default:
			break;
		}
		return true;
	}
}
