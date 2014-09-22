package edu.sesame.motiondatacollector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class NormalizeData extends Activity {
	ListView listView;
	Button normalizeButton;
	File[] files;
	ArrayList<String> fileCates,fileNames;
	HashMap<String, Integer> fileCateCounts;
	File directory;
	ArrayList<Double[]> data;
	AlertDialog errorDialog;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.normalize_data);
		
		initialize();
		
	}
	
	public void initialize(){
		errorDialog = new AlertDialog.Builder(this)
		.setTitle("Error")
		.setMessage("Sorry. An error occurred. Please do some changes and try again.")
		.setPositiveButton("Ok", null)
		.setCancelable(true)
		.create();
		
		ActionBar actionBar = getActionBar();
		actionBar.show();
		actionBar.setDisplayHomeAsUpEnabled(true);			
		listView = (ListView) findViewById(R.id.nd_choose_file_view);
		normalizeButton = (Button) findViewById(R.id.nd_normalize_button);
		OnClickListener listener = new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				switch(v.getId()){
				case R.id.nd_normalize_button:
					if(listView.getCheckedItemPosition()!=ListView.INVALID_POSITION){
						startNormalize(fileCates.get(listView.getCheckedItemPosition()));
					}
					
					break;
				default:
					break;
				}
			}
			
		};
		normalizeButton.setOnClickListener(listener);
		directory = getExternalFilesDir(null);
		
		constructList();
	}
	
	public void constructList(){
		try{
			fileCates = new ArrayList<String>();
			fileCateCounts = new HashMap<String, Integer>();
			fileNames = new ArrayList<String>();
			FileFilter filter = new FileFilter(){
	
				@Override
				public boolean accept(File pathname) {
					String[] ss = pathname.getName().split("#");
					String s = null;
					if(ss.length>1){
						s = ss[1];
					}
					return pathname.getAbsolutePath().matches(".*\\.csv") && s!=null &&!s.equals("NORM");
				}
				
			};
			files = directory.listFiles(filter);
			String s = null;
			for(int i = 0; i < files.length;i++){
				s = files[i].getName();
				fileNames.add(i,s);
				s=s.split("#")[0];
				if(!fileCates.contains(s)){
					fileCates.add(s);
					fileCateCounts.put(s, 1);
				} else {
					fileCateCounts.put(s, fileCateCounts.get(s)+1);
				}
			}
			ArrayList<String> catesAndCounts = new ArrayList<String>();
			for(String temp : fileCates){
				catesAndCounts.add(temp+" * "+Integer.toString(fileCateCounts.get(temp)));
			}
			ArrayAdapter<String> aa = new ArrayAdapter<String>(
					this,
					android.R.layout.simple_list_item_single_choice,
					catesAndCounts);
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			listView.setAdapter(aa);
			listView.invalidateViews();
		}catch(Exception e){
			e.printStackTrace();
			errorDialog.show();
		}
		
	}
	
	public void startNormalize(final String cate){
		try{
			ProgressDialog progress = new ProgressDialog(this);
			progress.setMessage("Normalizing all "+cate+" patterns. Please wait.");
			progress.setCancelable(false);
			new Normalize(this, progress, directory).execute(cate);
		}catch(Exception e){
			e.printStackTrace();
			errorDialog.show();
		}
	}
	

	@Override
	public void onResume(){
		super.onResume();
		constructList();
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	protected static final class Normalize extends AsyncTask<String, Void, double[]>{
		private final Context context;
		private final ProgressDialog load;
		private final File directory;
		private final AlertDialog errorDialog;
		public Normalize(Context context, ProgressDialog load, File directory){
			this.context = context;
			this.load = load;
			this.directory = directory;
			this.errorDialog = new AlertDialog.Builder(context)
				.setTitle("Error")
				.setMessage("Sorry. An error occurred. Please do some changes and try again.")
				.setPositiveButton("Ok", null)
				.setCancelable(true)
				.create();		
		}
		@SuppressLint("DefaultLocale")
		@Override
		protected double[] doInBackground(String... cates) {
			try{
				String catess = null;
				if(1!=cates.length||cates[0] == null){
					return null;
				} else {				
					catess = cates[0];
				}
				final String cate = catess;
				FileFilter filter = new FileFilter(){
					
					@Override
					public boolean accept(File pathname) {
						String[] s = pathname.getName().split("#");
						return pathname.getAbsolutePath().matches(".*\\.csv") 
								&& s.length>1
								&& !s[1].equals("NORM")
								&& s[0].equals(cate);
					}
					
				};
				File[] cateFiles = directory.listFiles(filter);	
				ArrayList<Double[]> data = new ArrayList<Double[]>();
				for(File file : cateFiles){
					
					data.addAll(readFile(file));
				}
				double[] d = updateStats(data);
				for(File file : cateFiles){
					writeNewFile(file.getName().replace("#RAW#", "#NORM#"), d, file);
				}
				return d;
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPreExecute(){
			load.show();
		}
		
		@Override
		protected void onPostExecute(double[] d){
			load.dismiss();
			if(d==null || d.length!=6){
				errorDialog.show();
			} else {
				new AlertDialog.Builder(context)
					.setTitle("Finish")
					.setMessage("Selected patterns have been normalized!")
					.setCancelable(true)
					.setPositiveButton("Ok", null)
					.show();
			}
		}	
		public double[] updateStats(ArrayList<Double[]> data){
			try{
				double meanX, meanY, meanZ, vX, vY, vZ;
				double sumX = 0, sumY = 0, sumZ = 0;
				int length = data.size();
				
				//Calculate mean
				for(Double[] d : data){
					sumX+=d[0];
					sumY+=d[1];
					sumZ+=d[2];
				}
				meanX = sumX / length;
				meanY = sumY / length;
				meanZ = sumZ / length;
				
				//Calculate variance
				sumX = sumY = sumZ = 0;
				for(Double[] d : data){
					sumX+=(d[0]-meanX)*(d[0]-meanX);
					sumY+=(d[1]-meanY)*(d[1]-meanY);
					sumZ+=(d[2]-meanZ)*(d[2]-meanZ);
				}	
				vX = Math.sqrt(sumX / length);
				vY = Math.sqrt(sumY / length);
				vZ = Math.sqrt(sumZ / length);	
				
				double[] d = {meanX, meanY, meanZ, vX, vY, vZ};
				Log.d("ND","6 stats: "+Arrays.toString(d));
				return d;
			} catch(Exception e){
				e.printStackTrace();
				errorDialog.show();
			}
			return null;
		}
		
		@SuppressWarnings("resource")
		public ArrayList<Double[]> readFile(File file){
			try{
				FileInputStream stream = new FileInputStream(file);
				String[] sss = (new Scanner(stream, "UTF-8").useDelimiter("\\A").next()).split("\n");
				ArrayList<Double[]> array = new ArrayList<Double[]>();
				for(String ss : sss){
					Double[] d = new Double[3];
					String[] s = ss.split(",");
					d[0] = Double.valueOf(s[0]);
					d[1] = Double.valueOf(s[1]);
					d[2] = Double.valueOf(s[2]);
					array.add(d);
				}
				return array;
			}catch(Exception e){
				e.printStackTrace();
				errorDialog.show();
			}
			return null;
		}
		
		public void writeNewFile(String s, double[] di, File origin){
			try{
				File file =new File(directory, s);
				ArrayList<Double[]> d = readFile(origin);
				Writer writer = new BufferedWriter(new FileWriter(file));
				for(int i = 0; i < d.size(); i++){
					writer.write(transform(d.get(i), di));
					if(i != d.size() - 1){
						writer.write("\n");
					}
				}
				writer.close();
			} catch(Exception e){
				e.printStackTrace();
				errorDialog.show();
			}
		}
		public String transform(Double[] d, double[] di){
			String s = "";
			for(int i = 0; i < 3; i++){
				s+=Double.valueOf((d[i]-di[i])/di[i+3]);
				if(i!=2){
					s+=",";
				}
			}
			return s;
		}
	}	
}
