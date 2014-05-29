package edu.sesame.motiondatacollector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ManageAction extends Activity {
	ListView listView;
	public static final String KEY = "LIST_VALUES";
	public static final String[] defaultList = {"Reading", "Walking", "Eating"};
	ArrayList<String> list;
	Button addButton;
	EditText editText;
	ArrayAdapter<String> aa;
	String temp;
	ActionBar actionBar;
	Runnable run;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_action_layout);
		if(Prefs.getDefaults(KEY, getBaseContext()) == null){
			list = new ArrayList<String>(Arrays.asList(defaultList));
		} else {
			list = new ArrayList<String>(Prefs.getDefaults(KEY, getBaseContext()));
		}
		listView = (ListView) findViewById(R.id.action_list);
		aa = new ArrayAdapter<String>(this, R.layout.list, list);
		listView.setAdapter(aa);
		addButton = (Button) findViewById(R.id.manage_action_add_button);
		editText = (EditText) findViewById(R.id.manage_action_input);
		actionBar = getActionBar();
		actionBar.show();
		actionBar.setDisplayHomeAsUpEnabled(true);
		run = new Runnable(){

			@Override
			public void run() {
				@SuppressWarnings("unchecked")
				ArrayList<String> temp = (ArrayList<String>)list.clone();
				aa.clear();
				aa.addAll(temp);
				aa.notifyDataSetChanged();
				listView.invalidateViews();
				listView.refreshDrawableState();
			}
			
		};
		addButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				temp = editText.getText().toString();
				if(!temp.isEmpty()){
					list.add(temp);
					commitListChange();
					ManageAction.this.runOnUiThread(run);
					editText.setText("");
					new AlertDialog.Builder(ManageAction.this)
						.setTitle("New Action Added")
						.setMessage("'"+temp+"' has been added to the list!")
						.setPositiveButton("Ok", null)
						.show();
				}
			}
		});
		listView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final EditText input = new EditText(ManageAction.this);
				final String s = ((TextView)view).getText().toString();
				if(!s.isEmpty()){
					new AlertDialog.Builder(ManageAction.this)
						.setTitle(s)
						.setMessage("What do you want to do with '"+s+"'?")
						.setNegativeButton("Delete", new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								list.remove(s);
								commitListChange();
								ManageAction.this.runOnUiThread(run);
								editText.setText("");
								new AlertDialog.Builder(ManageAction.this)
									.setTitle("Action deleted")
									.setMessage("'"+s+"' has been deleted!")
									.setPositiveButton("Ok", null)
									.show();
							}
						})
						.setNeutralButton("Rename", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								input.setText(s);
								new AlertDialog.Builder(ManageAction.this)
									.setTitle("Rename '"+s+"'")
									.setMessage("Enter a new name for '"+s+"'")
									.setView(input)
									.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											if(input.getText()!=null && input.getText().toString()!=null){
												list.remove(s);
												list.add(input.getText().toString());
												commitListChange();
												ManageAction.this.runOnUiThread(run);
											}
										}
									})
									.setNegativeButton("Cancel", null)
									.show();
								editText.setText("");
								
							}
							
						})
						.setPositiveButton("Cancel", null)
						.show();
				}
				return true;
			}
			
		});
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
		
		listView.requestFocus();
	}
	
	public void commitListChange(){
		Prefs.setDefaults(KEY, new HashSet<String>(list), getBaseContext());
	}
	
	@Override
	public void onBackPressed() {
		commitListChange();
		Intent i = new Intent(this, PhoneUI.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		finish();
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.manage_action_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.manage_action_clear_all:
			new AlertDialog.Builder(this)
				.setTitle("Confirm")
				.setMessage("Are you sure you want to clear all action labels?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						list = new ArrayList<String>();
						commitListChange();
						ManageAction.this.runOnUiThread(run);
					}
					
				})
				.setNegativeButton("Cancel", null)
				.show();
			break;
		case android.R.id.home:
			onBackPressed();
			break;
		default:
			break;
		}
		return true;
	}

}