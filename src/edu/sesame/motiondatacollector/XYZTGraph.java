package edu.sesame.motiondatacollector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.androidplot.xy.*;

import edu.sesame.motiondatacollector.ViewData.AsyncOpenFile;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class XYZTGraph extends Activity {
	File file;
	private XYPlot plot;
	
	
	
	@SuppressWarnings("resource")
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.xyzt_graph_layout);
		Intent intent = this.getIntent();
		if(intent.getStringExtra("FILE")!=null){
			
			file = new File(intent.getStringExtra("FILE"));
			if(!file.exists()){
				finish();
			}
			plot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
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
	       
	       
	        // initialize our XYPlot reference:
	        
		} else {
			finish();
		}
	}
	
	protected static final class AsyncTransFile extends AsyncTask<File, Void, XYSeries[]>{
		private final Context context;
		private final XYZTGraph parent;
		private final ProgressDialog load;
		private final XYPlot plot;
		JSONArray array;
		
		public AsyncTransFile(XYZTGraph parent, ProgressDialog load, XYPlot plot){
			this.context = parent.getBaseContext();
			this.load = load;
			this.parent = parent;
			this.plot = plot;
		}
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
				String json = null;
				try {
					FileInputStream stream = new FileInputStream(file);
					json = new Scanner(stream, "UTF-8").useDelimiter("\\A").next();
					array = new JSONArray(json);				
				} catch (FileNotFoundException | JSONException e) {
					e.printStackTrace();
					parent.finish();
				}

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
						DecimalFormat df = new DecimalFormat("#.00"); 
						for(int i = 0; i < array.length();i++){
							JSONObject obj = array.getJSONObject(i);
							tSAL.add(obj.getLong("TIME"));
							xSAL.add(Double.valueOf(df.format(obj.getDouble("X"))));
							ySAL.add(Double.valueOf(df.format(obj.getDouble("Y"))));
							zSAL.add(Double.valueOf(df.format(obj.getDouble("Z"))));
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
		 
		        // add a new series' to the xyplot:
		        plot.addSeries(update[0], series1Format);
		 
		        // same as above:
		        LineAndPointFormatter series2Format = new LineAndPointFormatter();
		        series2Format.setPointLabelFormatter(new PointLabelFormatter());
		        series2Format.configure(context,
		                R.xml.line_point_formatter_2);
		        plot.addSeries(update[1], series2Format);
    
		        LineAndPointFormatter series3Format = new LineAndPointFormatter();
		        series3Format.setPointLabelFormatter(new PointLabelFormatter());
		        series3Format.configure(context,
		                R.xml.line_point_formatter_3);
		        plot.addSeries(update[2], series3Format);

		        // reduce the number of range labels
		        plot.setTicksPerRangeLabel(3);
		        plot.getGraphWidget().setDomainLabelOrientation(-20);
			}
			load.dismiss();
		}	
	}
}
