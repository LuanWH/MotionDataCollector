package edu.sesame.motiondatacollector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androidplot.xy.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class XYZTGraph extends Activity {
	File file;
	int count = 0;
	private MultitouchPlot plot;
    private ActionBar actionBar;
    final static PointLabeler labeler = new PointLabeler(){

		@Override
		public String getLabel(XYSeries series, int index) {
			if(toggleCount == 0){
				return  series.getY(index).toString();
			} else if(toggleCount ==1){
				if(index%2 == 1){
					return series.getY(index).toString();
				} else {
					return "";
				}
			} else if(toggleCount ==2){
				if(index%4 == 1){
					return series.getY(index).toString();
				} else {
					return "";
				}
			} else {
				return "";
			}
		}
		
	};
    static int toggleCount = 0;
    Double d = 8.0;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.xyzt_graph_layout);
		
		actionBar = getActionBar();
		actionBar.show();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Intent intent = this.getIntent();
		if(intent.getStringExtra("FILE")!=null){
			
			file = new File(intent.getStringExtra("FILE"));
			if(!file.exists()){
				finish();
			}
			plot = (MultitouchPlot) findViewById(R.id.mySimpleXYPlot);
			ProgressDialog progress = new ProgressDialog(this);
			String tempString = file.getName().substring(0,file.getName().indexOf("."));
			plot.setTitle(tempString);
			progress.setMessage("Opening file "+tempString);
			progress.setCancelable(false);
			new AsyncTransFile(
					this, 
					progress,
					plot
					).execute(file);
			
	        plot.setRangeBoundaries((-1)*d, d, BoundaryMode.FIXED);
			plot.setDomainBoundaries(0, 2000, BoundaryMode.FIXED);
			plot.redraw();
			count = toggleCount = 0; 
		} else {
			finish();
		}
	}
	
	protected static final class AsyncTransFile extends AsyncTask<File, Void, XYSeries[]>{
		private final Context context;
		private final XYZTGraph parent;
		private final ProgressDialog load;
		private final XYPlot plot;
		JSONArray array = null;
		
		public AsyncTransFile(XYZTGraph parent, ProgressDialog load, XYPlot plot){
			this.context = parent.getBaseContext();
			this.load = load;
			this.parent = parent;
			this.plot = plot;
		}
		@SuppressWarnings("resource")
		@Override
		protected XYSeries[] doInBackground(File... files) {
			ArrayList<Number> tSAL = new ArrayList<Number>();
			ArrayList<Number> xSAL = new ArrayList<Number>();
			ArrayList<Number> ySAL = new ArrayList<Number>();
			ArrayList<Number> zSAL = new ArrayList<Number>();
			
			File file = null;
			if(1!=files.length||files[0] == null){
				return null;
			} else {				
				//TODO: Multiple files
				file = files[0];
				String s = null;
				String[] ss = null;
				try {
					FileInputStream stream = new FileInputStream(file);
					if(file.getAbsolutePath().matches(".*\\.json")){
						s = new Scanner(stream, "UTF-8").useDelimiter("\\A").next();
						array = new JSONArray(s);		
					} else {
						ss = (new Scanner(stream, "UTF-8").useDelimiter("\\A").next()).split("\n");	
					}
				} catch (FileNotFoundException | JSONException e) {
					e.printStackTrace();
					parent.finish();
				}
				boolean mode = array == null;
				if(array==null && (ss == null || (ss!=null&&ss.length==0))){
					new AlertDialog.Builder(context)
						.setTitle("Error")
						.setMessage("Sorry. An unexpected error occurred.")
						.setPositiveButton("Ok", null)
						.setCancelable(true)
						.show();
				} else {
					try {
						DecimalFormat df = new DecimalFormat("#.00"); 
						String[] temp = null;
						int length = mode?ss.length:array.length();
						for(int i = 0; i < length;i++){
							if(mode){
								temp =ss[i].split(",");
								tSAL.add(i*100);
								xSAL.add(Double.valueOf(String.format("%.2f",Double.valueOf(temp[0]))));
								ySAL.add(Double.valueOf(String.format("%.2f",Double.valueOf(temp[1]))));
								zSAL.add(Double.valueOf(String.format("%.2f",Double.valueOf(temp[2]))));									
							} else {
								JSONObject obj = array.getJSONObject(i);
								tSAL.add(obj.getLong("TIME"));
								xSAL.add(Double.valueOf(df.format(obj.getDouble("X"))));
								ySAL.add(Double.valueOf(df.format(obj.getDouble("Y"))));
								zSAL.add(Double.valueOf(df.format(obj.getDouble("Z"))));								
							}
						}
						XYSeries[] xyzt = new XYSeries[3];
						xyzt[0] = new SimpleXYSeries(tSAL,xSAL,"X");
						xyzt[1] = new SimpleXYSeries(tSAL,ySAL,"Y");
						xyzt[2] = new SimpleXYSeries(tSAL,zSAL,"Z");
						return xyzt;
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
		protected void onPostExecute(XYSeries[] update){
			
			if(update == null){
				new AlertDialog.Builder(context)
					.setTitle("Error")
					.setMessage("An unexpected error occurred!")
					.setPositiveButton("Back", null)
					.show();
			} else{
		        // Create a formatter to use for drawing a series using LineAndPointRenderer
		        // and configure it from xml:
		        LineAndPointFormatter series1Format = new LineAndPointFormatter();
		        series1Format.setPointLabelFormatter(new PointLabelFormatter());
		        series1Format.configure(context,
		                R.xml.line_point_formatter_1);
		        series1Format.setPointLabeler(labeler);
		        // add a new series' to the xyplot:
		        plot.addSeries(update[0], series1Format);
		 
		        // same as above:
		        LineAndPointFormatter series2Format = new LineAndPointFormatter();
		        series2Format.setPointLabelFormatter(new PointLabelFormatter());
		        series2Format.configure(context,
		                R.xml.line_point_formatter_2);
		        series2Format.setPointLabeler(labeler);
		        plot.addSeries(update[1], series2Format);
    
		        LineAndPointFormatter series3Format = new LineAndPointFormatter();
		        series3Format.setPointLabelFormatter(new PointLabelFormatter());
		        series3Format.configure(context,
		                R.xml.line_point_formatter_3);
		        series3Format.setPointLabeler(labeler);
		        plot.addSeries(update[2], series3Format);

		        // reduce the number of range labels
		        plot.setTicksPerRangeLabel(3);
		        plot.getGraphWidget().setDomainLabelOrientation(-20);
			}
			load.dismiss();
		}	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.graph_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.adjust_offset_button:
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
											.setTitle("Adjust Offest")
											.setMessage("Enter an absolute bounder value");
			final EditText input = new EditText(this);
			input.setText(Double.toString(d));
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
			        							LinearLayout.LayoutParams.MATCH_PARENT,
			        							LinearLayout.LayoutParams.MATCH_PARENT);
			lp.setMargins(20, 0, 20, 10);
			input.setLayoutParams(lp);
			builder.setView(input);
			builder.setIcon(android.R.drawable.ic_menu_zoom);
			builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(input.getText()!=null){
						try{
							d = Double.valueOf(input.getText().toString());
							plot.setRangeBoundaries((-1)*Math.abs(d), Math.abs(d), BoundaryMode.FIXED);
							plot.redraw();
						} catch(NumberFormatException e){
							new AlertDialog.Builder(XYZTGraph.this)
								.setTitle("Warning")
								.setMessage("Illegal input! Value will not be updated.")
								.setIcon(android.R.drawable.ic_dialog_alert)
								.setPositiveButton("Ok", null)
								.setCancelable(true)
								.show();
						}
					}
				}
			});
			builder.setNegativeButton("Cancel", null);
			builder.show();
			break;
		case R.id.graph_capture_button:
			saveCanvasImage();
			break;
		case R.id.disable_label_button:
			toggleCount = toggleCount == 3? 0: toggleCount+1;
			plot.redraw();
			break;
		case android.R.id.home:
			onBackPressed();
			break;
		default:
			break;
		}
		return true;
	}
	
	public void saveCanvasImage() {
	    Log.d("bitmap","strm");
	    plot.setDrawingCacheEnabled(true);
	    Bitmap bm = plot.getDrawingCache();
	    File directory = getExternalFilesDir(null);
	    File f = null;
	    f = new File(directory, file.getName()+"_"+count+".png");
	    count++;
	    try {
		    FileOutputStream strm = new FileOutputStream(f);
		    bm.compress(CompressFormat.PNG, 80, strm);
		    strm.close();
		    Toast.makeText(getApplicationContext(), "Current view has been saved to "+f.getAbsolutePath(), 
		    		   Toast.LENGTH_LONG).show();
	    }
	    catch (IOException e){
	        e.printStackTrace();
	    }

	}
}

