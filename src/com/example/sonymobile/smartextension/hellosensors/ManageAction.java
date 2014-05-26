package com.example.sonymobile.smartextension.hellosensors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_action_layout);
		if(getPreferences(MODE_PRIVATE).getStringSet(KEY, null) == null){
			list = new ArrayList<String>(Arrays.asList(defaultList));
		} else {
			list = new ArrayList<String>(getPreferences(MODE_PRIVATE).getStringSet(KEY, null));
		}
		listView = (ListView) findViewById(R.id.action_list);
		aa = new ArrayAdapter<String>(this, R.layout.list, list);
		listView.setAdapter(aa);
		addButton = (Button) findViewById(R.id.manage_action_add_button);
		editText = (EditText) findViewById(R.id.manage_action_input);
		addButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				temp = editText.getText().toString();
				if(!temp.isEmpty()){
					aa.add(temp);
					commitListChange();
					new AlertDialog.Builder(ManageAction.this)
						.setTitle("New Action Added")
						.setMessage("'"+temp+"' has been added to the list!")
						.setPositiveButton("Ok", null)
						.show();
				}
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final String s = ((TextView)view).getText().toString();
				if(!s.isEmpty()){
					new AlertDialog.Builder(ManageAction.this)
						.setTitle("Confirm deletion")
						.setMessage("Are you sure you want to delete '"+s+"'?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								aa.remove(s);
								commitListChange();
								editText.setText("");
							}
						})
						.setNegativeButton("Cancel", null)
						.show();
				}
				
			}
			
		});
	}
	
	public void commitListChange(){
		SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putStringSet(KEY, new HashSet<String>(list));
		editor.commit();		
	}
}
/*<string-array name="action_arrays">
<item>Reading</item>
<item>Walking</item>
<item>Running</item>
<item>Writing</item>
<item>Typing</item>
<item>Eating</item>
</string-array>*/