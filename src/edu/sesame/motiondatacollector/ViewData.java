package edu.sesame.motiondatacollector;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

public class ViewData extends Activity {
	
	ActionBar actionBar;
	Button openButton, statButton;
	TableLayout table;
	File[] dataFiles;
	ListView listView;
	Dialog dialog;
	File directory;
	TextView fileNameView;
	ScrollView tableOuterView;
	TableLayout header;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		directory = getExternalFilesDir(null);
		setContentView(R.layout.view_data_layout);
		initialize();
		
		
	}
	
	private void initialize(){		
		actionBar = getActionBar();
		actionBar.show();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		tableOuterView = (ScrollView) findViewById(R.id.view_data_outer_view);
		tableOuterView.removeAllViewsInLayout();
		fileNameView = (TextView) findViewById(R.id.file_name_view);
		openButton = (Button) findViewById(R.id.open_button);
		statButton = (Button) findViewById(R.id.stat_button);
		table = new TableLayout(this);
		table.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));		
		table.removeAllViewsInLayout();	
		OnClickListener listener = new OnClickListener(){

			@Override
			public void onClick(View v) {
				switch (v.getId()){
				case R.id.open_button:
					if(dataFiles == null){
						new AlertDialog.Builder(ViewData.this)
							.setTitle("Warning")
							.setMessage("No data file found!")
							.setPositiveButton("Ok", null)
							.show();
					} else{
						dialog.show();
					}
					break;
				case R.id.stat_button:
					startActivity(new Intent(ViewData.this, ViewStats.class));
					break;
				default:
					break;
				}
			}
			
		};
		openButton.setOnClickListener(listener);
		statButton.setOnClickListener(listener);
		
		
		FileFilter filter = new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().matches(".*\\.json");
			}
			
		};
		dataFiles = directory.listFiles(filter);
		if(dataFiles != null){
			String[] fileNames = new String[dataFiles.length];
			for (int i = 0; i < dataFiles.length;i++){
				fileNames[i] = dataFiles[i].getName();
			}
			ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileNames);	
			Log.d("HHA", fileNames[0]);
			dialog = new Dialog(ViewData.this);
			LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
			View v = li.inflate(R.layout.dialog_layout, null, false);
			listView = (ListView) v.findViewById(R.id.dialog_listview);
			listView.setAdapter(aa);
			listView.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					String s = ((TextView)view).getText().toString();
					fileNameView.setText(s);
					openFile(s);
					dialog.dismiss();
				}
				
			});
			dialog.setContentView(v);
			dialog.setCancelable(true);
			dialog.setTitle("Select a data file");
		}

	}
	
	public void openFile(String name){
		header = (TableLayout) findViewById(R.id.header_table);
		ProgressDialog progress = new ProgressDialog(this);
		String tempString = name.substring(0,name.indexOf("."));
		progress.setMessage("Opening file "+tempString);
		progress.setCancelable(false);
		new AsyncOpenFile(
				tableOuterView, 
				table, 
				this, 
				ViewData.this, 
				progress
				).execute(new File(directory, name));
	}
	
	@SuppressWarnings("resource")
	public JSONArray loadJSON(File file){
		if(file.exists()){
			String json = null;
			try {
				FileInputStream stream = new FileInputStream(file);
				json = new Scanner(stream, "UTF-8").useDelimiter("\\A").next();
				return new JSONArray(json);				
			} catch (FileNotFoundException | JSONException e) {
				e.printStackTrace();
			}
			return null;
		} else {
			return null;
		}
	}
	
	protected static final class AsyncOpenFile extends AsyncTask<File, Void, TableLayout>{
		private final ScrollView v;
		private final Context context;
		private final ViewData parent;
		private final ProgressDialog load;
		private final TableLayout table;
		
		public AsyncOpenFile(ScrollView v, TableLayout table, Context context, ViewData parent, ProgressDialog load){
			this.v = v;
			this.context = context;
			this.load = load;
			this.table = table;
			this.parent = parent;
		}
		@Override
		protected TableLayout doInBackground(File... files) {
			File file = null;
			if(1!=files.length||files[0] == null){
				return null;
			} else {				
				//TODO: Multiple files
				file = files[0];
				JSONArray array = parent.loadJSON(file);
				v.removeAllViewsInLayout();
				table.removeAllViewsInLayout();	
				int width = parent.header.getWidth() / 4;
				if(array==null){
					new AlertDialog.Builder(context)
						.setTitle("Error")
						.setMessage("Sorry. An unexpected error occurred.")
						.setPositiveButton("Ok", null)
						.setCancelable(true)
						.show();
				} else {
					try {
						//boolean flag = true;
						for(int i = 0; i < array.length();i++){
							TableRow tr = new TableRow(context);
							tr.setLayoutParams(new LayoutParams(
													LayoutParams.MATCH_PARENT,
													LayoutParams.WRAP_CONTENT));
							JSONObject obj = array.getJSONObject(i);
							
							TextView b0 = new TextView(context);
							String str = String.valueOf(obj.getLong("TIME"));
							b0.setText(str);
							b0.setGravity(Gravity.CENTER);
							b0.setTextColor(Color.BLACK);
							b0.setTextSize(18);
							b0.setWidth(width);
							tr.addView(b0);
							
							TextView b1 = new TextView(context);
							str = String.format("%.4f",obj.getDouble("X"));
							b1.setText(str);
							//b1.setPadding(10, 0, 0, 0);
							b1.setGravity(Gravity.CENTER);
							b1.setTextColor(Color.BLACK);
							b1.setTextSize(18);
							b1.setWidth(width);
							tr.addView(b1);	
							
							TextView b2 = new TextView(context);
							str = String.format("%.4f",obj.getDouble("Y"));
							b2.setText(str);
							//b2.setPadding(10, 0, 0, 0);
							b2.setTextColor(Color.BLACK);
							b2.setGravity(Gravity.CENTER);
							b2.setTextSize(18);
							b2.setWidth(width);
							tr.addView(b2);
							
							TextView b3 = new TextView(context);
							str = String.format("%.4f",obj.getDouble("Z"));
							b3.setText(str);
							//b3.setPadding(10, 0, 0, 0);
							b3.setGravity(Gravity.CENTER);
							b3.setTextColor(Color.BLACK);
							b3.setTextSize(18);
							b3.setWidth(width);
							tr.addView(b3);	
							
							table.addView(tr);
							
							final View vline = new View(context);
							vline.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,1));
							vline.setBackgroundColor(Color.WHITE);
							table.addView(vline);
						//}
						}
						return table;
					
					} catch (JSONException e) {
						e.printStackTrace();
						return null;
					}
				}
				return null;
			}
		}
		
		@Override
		protected void onPreExecute(){
			load.show();
		}
		
		@Override
		protected void onPostExecute(TableLayout update){
			load.dismiss();
			if(update == null){
				new AlertDialog.Builder(context)
					.setTitle("Error")
					.setMessage("An unexpected error occurred!")
					.setPositiveButton("Back", null)
					.show();
			} else{
				v.addView(update);
			}
		}	
	}	
}
