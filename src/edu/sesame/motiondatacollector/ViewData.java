package edu.sesame.motiondatacollector;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
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
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.graphics.Color;

@SuppressLint("DefaultLocale")
public class ViewData extends Activity {
	
	ActionBar actionBar;
	Button openButton, graphButton, deleteButton, smoothButton;
	TableLayout table;
	File[] dataFiles,jsonFiles,csvFiles;
	File chosenFile;
	ListView listView;
	Dialog dialog;
	File directory;
	TextView fileNameView;
	ScrollView tableOuterView;
	TableLayout header;
	AlertDialog noFileFoundDialog,chooseAction,listJSON,listCSV;
	OnClickListener listener;
	
	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		directory = getExternalFilesDir(null);
		setContentView(R.layout.view_data_layout);
		initialize();
		Intent i = getIntent();
		if(i.getStringExtra(Prefs.FILE_NAME)!=null && i.getStringExtra(Prefs.FILE_NAME).equals("START")){
			listener.onClick((View)openButton);
		}
	}
	
	private void initialize(){		
		actionBar = getActionBar();
		actionBar.show();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		tableOuterView = (ScrollView) findViewById(R.id.view_data_outer_view);
		fileNameView = (TextView) findViewById(R.id.file_name_view);
		openButton = (Button) findViewById(R.id.open_button);
		graphButton = (Button) findViewById(R.id.plot_graph_button);
		deleteButton= (Button) findViewById(R.id.delete_button);
		smoothButton = (Button) findViewById(R.id.smooth_button);
		table = new TableLayout(this);
		table.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));		
		noFileFoundDialog = new AlertDialog.Builder(this)
								.setTitle("Error")
								.setMessage("No Such File Found!")
								.setPositiveButton("Ok", null)
								.setCancelable(true)
								.create();
		if(chosenFile==null){
			graphButton.setEnabled(false);
			smoothButton.setEnabled(false);
		} else {
			graphButton.setEnabled(true);
			smoothButton.setEnabled(true);
		}
		updateDataFiles();
		if(dataFiles != null && dataFiles.length!=0){
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
			listView.invalidate();
			v.invalidate();
			dialog.setContentView(v);
			dialog.setCancelable(true);
			dialog.setTitle("Select a data file");
		}
		
		listener = new OnClickListener(){

			@Override
			public void onClick(View v) {
				switch (v.getId()){
				case R.id.open_button:
					if(dataFiles == null){
						noFileFoundDialog.show();
					} else{
						if(dataFiles!=null && dataFiles.length!=0){
							dialog.show();
						} else {
							noFileFoundDialog.show();
						}
					}
					break;
				case R.id.plot_graph_button:
					if(chosenFile!=null){
						Intent i = new Intent(ViewData.this, XYZTGraph.class);
						i.putExtra("FILE", chosenFile.getAbsolutePath());
						startActivity(i);
					}
					break;
				case R.id.delete_button:
					chooseAction.show();
					break;
				case R.id.smooth_button:
					if(chosenFile!=null){
						smoothData();
					}					
					break;
				default:
					break;
				}
			}
			
		};
		openButton.setOnClickListener(listener);
		graphButton.setOnClickListener(listener);
		deleteButton.setOnClickListener(listener);
		smoothButton.setOnClickListener(listener);
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
	    
	    AlertDialog.Builder chooseActionBuilder = new AlertDialog.Builder(this);
	    chooseActionBuilder.setTitle("Choose File Type");
	    CharSequence[] items = {"Data Files", "Pattern Files", "Cancel"};
	    chooseActionBuilder.setItems(items, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which){
				case 0:
					if(jsonFiles!=null && jsonFiles.length>0){
						listJSON.show();
					} else {
						noFileFoundDialog.show();
					}
					break;
				case 1:
					if(csvFiles!=null && csvFiles.length>0){
						listCSV.show();
					} else {
						noFileFoundDialog.show();
					}
					break;
				case 2:
					break;
				default:
					break;	
				}
			}
	    	}
	    );
	    chooseActionBuilder.setCancelable(true);
	    chooseAction = chooseActionBuilder.create();

	}
	
	public void updateDataFiles(){
		FileFilter filter1 = new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().matches(".*\\.json");
			}
			
		};
		FileFilter filter2 = new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().matches(".*\\.csv");
			}
			
		};
		jsonFiles = directory.listFiles(filter1);
		csvFiles = directory.listFiles(filter2);
		ArrayList<File> temp =new ArrayList<File>(Arrays.asList(jsonFiles));
		temp.addAll(Arrays.asList(csvFiles));
		dataFiles = temp.toArray(new File[0]);
		
		CharSequence[] jsonFilesNames = new CharSequence[jsonFiles.length];
		for(int i = 0; i < jsonFiles.length;i++){
			jsonFilesNames[i] = jsonFiles[i].getName().replace(".json", "");
		}
		CharSequence[] csvFilesNames = new CharSequence[csvFiles.length];
		for(int i = 0; i < csvFiles.length;i++){
			csvFilesNames[i] = csvFiles[i].getName().replace(".csv", "");
		}		
		
		final ArrayList<Integer> jsonSelected = new ArrayList<Integer>();
		final ArrayList<Integer> csvSelected = new ArrayList<Integer>();
	    AlertDialog.Builder listJSONBuilder = new AlertDialog.Builder(this);
	    listJSONBuilder.setTitle("Select files to be deleted");
	    listJSONBuilder.setMultiChoiceItems(jsonFilesNames, null, new OnMultiChoiceClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which,
					boolean isChecked) {
				if(isChecked){
					jsonSelected.add(which);
				} else {
					jsonSelected.remove(Integer.valueOf(which));
				}
			}
	    	
	    });
	    listJSONBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(jsonSelected!=null && !jsonSelected.isEmpty()){
					String s = "";
					for(int i : jsonSelected){
						s+=jsonFiles[i].getName()+" ";
						jsonFiles[i].delete();
					}
					showConfirmationDialog(s);
					initialize();
				}
				jsonSelected.clear();
			}
	    	
	    });
	    listJSONBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

	    	@Override
	    	public void onClick(DialogInterface dialog, int which) {
	    		jsonSelected.clear();
	    	}
	    });
	    listJSONBuilder.setCancelable(true);
	    listJSON = listJSONBuilder.create();
	    
	    
	    AlertDialog.Builder listCSVBuilder = new AlertDialog.Builder(this);		
	    listCSVBuilder.setTitle("Select files to be deleted");
	    listCSVBuilder.setMultiChoiceItems(csvFilesNames, null, new OnMultiChoiceClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which,
					boolean isChecked) {
				if(isChecked){
					csvSelected.add(which);
				} else {
					csvSelected.remove(Integer.valueOf(which));
				}
			}
	    	
	    }); 
	    listCSVBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(csvSelected!=null && !csvSelected.isEmpty()){
					String s = "";
					for(int i : csvSelected){
						s+=csvFiles[i].getName()+" ";
						csvFiles[i].delete();
					}
					showConfirmationDialog(s);
					initialize();
					
				}
				csvSelected.clear();
			}
	    	
	    });	 
	    listCSVBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				csvSelected.clear();
			}
		});
	    listCSVBuilder.setCancelable(true);
	    listCSV = listCSVBuilder.create();
	    if(fileNameView.getText()!=null && fileNameView.getText().toString().length()>0){
	    	Boolean flag = false;
	    	for(int i = 0; jsonFilesNames!=null && i < jsonFilesNames.length && !flag;i++){
	    		flag = jsonFilesNames[i].equals(fileNameView.getText().toString().replace(".json",""));
	    	}
	    	for(int i = 0; csvFilesNames!=null && i < csvFilesNames.length && !flag;i++){
	    		flag = csvFilesNames[i].equals(fileNameView.getText().toString().replace(".csv",""));
	    	}
	    	if(!flag){
	    		Log.d("ViewData","File has been deleted! Clearing view!");
	    		tableOuterView.removeAllViewsInLayout();
	    		fileNameView.setText("");
	    		tableOuterView.invalidate();
	    		chosenFile = null;
	    		graphButton.setEnabled(false);
	    		smoothButton.setEnabled(false);
	    		initialize();
	    	}
	    }
	}
	
	public void showConfirmationDialog(String s){
		new AlertDialog.Builder(this)
			.setTitle("Success")
			.setMessage(s+"deleted!")
			.setPositiveButton("Ok", null)
			.show();
	}
	
	public void openFile(String name){
		chosenFile = new File(directory, name);
		graphButton.setEnabled(true);
		smoothButton.setEnabled(true);
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
				).execute(chosenFile);
	}
	
	public JSONArray loadJSON(File file){
		if(file.exists()){
			String json = null;
			try {
				FileInputStream stream = new FileInputStream(file);
				json = new Scanner(stream, "UTF-8").useDelimiter("\\A").next();
				return new JSONArray(json);				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		} else {
			return null;
		}
	}
	
	@SuppressLint("DefaultLocale")
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
		@SuppressLint("DefaultLocale")
		@Override
		protected TableLayout doInBackground(File... files) {
			File file = null;
			if(1!=files.length||files[0] == null){
				return null;
			} else {				
				file = files[0];
				JSONArray array = null;
				String[] ss = null;
				
				if(file.getAbsolutePath().matches(".*\\.json")){
					array = parent.loadJSON(file);
				} else {
					FileInputStream stream = null;
					try {
						stream = new FileInputStream(file);
						ss = (new Scanner(stream, "UTF-8").useDelimiter("\\A").next()).split("\n");	
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}					
				}
				v.removeAllViewsInLayout();
				table.removeAllViewsInLayout();	
				int width = parent.header.getWidth() / 4;
				if(array==null && (ss == null || (ss!=null&&ss.length==0))){
					new AlertDialog.Builder(context)
						.setTitle("Error")
						.setMessage("Sorry. An unexpected error occurred.")
						.setPositiveButton("Ok", null)
						.setCancelable(true)
						.show();
				} else {
					boolean mode = array == null;
					String[] s = null;
					int length = mode?ss.length:array.length();
					String x,y,z,time;
					JSONObject obj = null;
					try {
						//boolean flag = true;
						for(int i = 0; i < length;i++){
							TableRow tr = new TableRow(context);
							tr.setLayoutParams(new LayoutParams(
													LayoutParams.MATCH_PARENT,
													LayoutParams.WRAP_CONTENT));
							obj = mode?null:array.getJSONObject(i);
							s = mode?ss[i].split(","):null;
							x = mode?String.format("%.4f",Double.valueOf(s[0])):String.format("%.4f",obj.getDouble("X"));
							y = mode?String.format("%.4f",Double.valueOf(s[1])):String.format("%.4f",obj.getDouble("Y"));
							z = mode?String.format("%.4f",Double.valueOf(s[2])):String.format("%.4f",obj.getDouble("Z"));
							time = mode?String.valueOf(i*100):String.valueOf(obj.getLong("TIME"));;
							
							TextView b0 = new TextView(context);
							b0.setText(time);
							b0.setGravity(Gravity.CENTER);
							b0.setTextColor(Color.BLACK);
							b0.setTextSize(18);
							b0.setWidth(width);
							tr.addView(b0);
							
							TextView b1 = new TextView(context);
							b1.setText(x);
							b1.setGravity(Gravity.CENTER);
							b1.setTextColor(Color.BLACK);
							b1.setTextSize(18);
							b1.setWidth(width);
							tr.addView(b1);	
							
							TextView b2 = new TextView(context);
							b2.setText(y);
							b2.setTextColor(Color.BLACK);
							b2.setGravity(Gravity.CENTER);
							b2.setTextSize(18);
							b2.setWidth(width);
							tr.addView(b2);
							
							TextView b3 = new TextView(context);
							b3.setText(z);
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
						}
						return table;
					
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(context, "Sorry! An error occurs and the file can't be opened!", Toast.LENGTH_SHORT).show();
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
				v.refreshDrawableState();
				v.invalidate();
			}
		}	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.view_data_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.view_data_share:
			if(dataFiles != null){
				String[] fileNames = new String[dataFiles.length];
				for (int i = 0; i < dataFiles.length;i++){
					fileNames[i] = dataFiles[i].getName();
				}
				final ArrayList<Integer> chosenFiles = new ArrayList<Integer>();
				final ArrayList<Uri> filesToBeSent = new ArrayList<Uri>();
				if(fileNames!=null && fileNames.length > 0){
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Select Files To Be Shared");
					builder.setIcon(android.R.drawable.ic_menu_share);
					builder.setMultiChoiceItems(fileNames, null, new DialogInterface.OnMultiChoiceClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
							if(isChecked){
								chosenFiles.add(which);
							} else {
								chosenFiles.remove(Integer.valueOf(which));
							}
						}
					});
					builder.setPositiveButton("Share", new DialogInterface.OnClickListener(){
	
						@Override
						public void onClick(DialogInterface dialog, int which) {
							for(int j = 0; j < chosenFiles.size();j++){
								filesToBeSent.add(Uri.fromFile(dataFiles[chosenFiles.get(Integer.valueOf(j))]));
							}
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_SEND_MULTIPLE);
							intent.putExtra(Intent.EXTRA_SUBJECT, "Files shared from MotionDataCollector");
							intent.setType("text/*");
							intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesToBeSent);
							startActivity(intent);
						}
						
					});
					builder.setNegativeButton("Cancel", null);
					builder.show();
				} else {
					noFileFoundDialog.show();
				}
				break;
			} else {
				noFileFoundDialog.show();
			}
			break;
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.action_delete_all_files:
			if(dataFiles!=null && dataFiles.length!=0){
				CharSequence[] items = {"JSON Data Files", "CSV Pattern Files", "Cancel"};
				new AlertDialog.Builder(this)
					.setTitle("Choose a format of files to be all deleted")
					.setItems(items, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch(which){
							case 0:
								if(jsonFiles!=null & jsonFiles.length>0){
									for(File file : jsonFiles){
										file.delete();
									}
									updateDataFiles();
								} else {
									noFileFoundDialog.show();
								}
								break;
							case 1:
								if(csvFiles!=null & csvFiles.length>0){
									for(File file : csvFiles){
										file.delete();
									}
									updateDataFiles();
								} else {
									noFileFoundDialog.show();
								}
								break;
							case 2:
								break;
							default:
								break;
							}
						}
						
					})
					.show();
			} else {
				noFileFoundDialog.show();
			}
			updateDataFiles();
			break;
		default:
			break;
		}
		return true;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		updateDataFiles();
	}
	
	public void smoothData(){
		
	}
}
