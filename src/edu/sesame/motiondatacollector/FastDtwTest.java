package edu.sesame.motiondatacollector;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;

import android.content.Context;
import android.util.Log;

import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;
import com.dtw.TimeWarpInfo;

public class FastDtwTest
{
	public static int count = 0;
	public static final int RAW = 0;
	public static final int NORM = 1;
	public static final int THRESHOLD = 120;
	File dir;
	File[] dataFiles;
	String motion;
	String now;
	ArrayList<String> patternList;
	Context context;
	int type;
	
	public static final String[] DISTANCE_LIST = {"EuclideanDistance", "ManhattanDistance", "BinaryDistance"};
	public FastDtwTest(File dir, Context context, int type){
		this.dir = dir;
		this.context = context;
		this.type = type;

		patternList = new ArrayList<String>();
		FileFilter rawFilter = new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				String[] ss = pathname.getName().split("#");
				return pathname.getAbsolutePath().matches(".*\\.csv")
						&& ss.length == 3
						&& ss[1].equals("RAW");
			}
			
		};
		FileFilter normFilter = new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				String[] ss = pathname.getName().split("#");
				return pathname.getAbsolutePath().matches(".*\\.csv")
						&& ss.length == 3
						&& ss[1].equals("NORM");
			}
			
		};		
		if(type == 0){
			dataFiles = dir.listFiles(rawFilter);
		} else {
			dataFiles = dir.listFiles(normFilter);
		}
		now = "";
		if(dataFiles != null && dataFiles.length>0){
			loadPatterns();
		}
	}
	
	public void loadPatterns(){
		for(File file : dataFiles){
			try {
				patternList.add(new Scanner(file).useDelimiter("\\A").next());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String toMotion(Double[][] dd){
		try{
			Writer writer = new StringWriter();
			for(int i = 0; i < dd.length - 1; i++){
				for(int j = 0; j < dd[i].length - 1; j++){
					writer.write(String.valueOf(dd[i][j])+",");
				}
				writer.write(String.valueOf(dd[i][dd[i].length - 1])+"\n");
			}
			for(int j = 0; j < dd[dd.length-1].length - 1; j++){
				writer.write(String.valueOf(dd[dd.length-1][j])+",");
			}
			writer.write(String.valueOf(dd[dd.length-1][dd[dd.length-1].length - 1]));
			return writer.toString();
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public Double[][] transform(Double[][] dd){
    	Double[] sum = new Double[3];
    	Double[] mean = new Double[3];
    	Double[] var = new Double[3];
    	int length = dd.length;
    	Double[][] normdd= new Double[length][3];
    	for(int i = 0; i < 3;i++){
    		sum[i] = 0.;
    		for(Double[] d : dd){
    			sum[i]+=d[i];
    		}
    		mean[i] = sum[i] / length;
    	}
    	for(int i = 0; i < 3;i++){
    		var[i] = 0.;
    		for(Double[] d : dd){
    			var[i]+= (d[i] - mean[i])*(d[i] - mean[i]);
    		}
    		var[i] = Math.sqrt(var[i]/length);
    	}
    	for(int i = 0; i < 3;i++){
    		for(int j=0;j<dd.length;j++){
    			normdd[j][i] = Double.valueOf((dd[j][i]-mean[i])/var[i]);
    		}
    	}    	
    	return normdd;
	}
	
	public String[] match(Double[][] dd){
		if(type == RAW){
			return match(toMotion(dd));
		} else if(type == NORM){
			return match(toMotion(transform(dd)));
		} else {
			return null;
		}
	}
	
	public String[] match(String motion){
		if(patternList!=null && patternList.size()>0){
			this.motion = motion;
			double[] dis = null;
			dis = new double[patternList.size()];
			double min = Double.MAX_VALUE;
			int index =0;
			String[] args = null;
			String path = null;
			int radius = 10;
			int i = 0;
			for(String s : patternList){
				args=new String[3];
				args[0] = s;
				args[1] = motion;
				args[2] = String.valueOf(radius);
				dis[i] = execute(args);
				if(dis[i]!=-1 && dis[i]<min && dis[i]!=-1){
					min = dis[i];
					index = i;
					path = now;
				}
				i++;
			}
//			if(min < THRESHOLD){
				String[] ss = {dataFiles[index].getName(), path, String.valueOf(min)};
				return ss;
//			}else{
//				return null;
//			}
		} else {
			return null;
		}
		
	}
	
	public double execute(String[] args)
	{
		if (args.length!=3 && args.length!=4)
		{
			Log.d("DTW", "USAGE:  java FastDtwTest timeSeries1 timeSeries2 radius [EuclideanDistance|ManhattanDistance|BinaryDistance]");
			return -1;
		}
		else
		{	count++;
			final TimeSeries tsI = new TimeSeries(args[0], false, false, ',');
			final TimeSeries tsJ = new TimeSeries(args[1], false, false, ',');

			final DistanceFunction distFn;
			if (args.length < 4)
			{
				Integer position = Prefs.getInteger("DISTANCE", context);
				position = position==null?0:position;
				distFn = DistanceFunctionFactory.getDistFnByName(DISTANCE_LIST[(int)position]); 
			}
			else
			{
				distFn = DistanceFunctionFactory.getDistFnByName(args[3]);
			}   // end if

			final TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(tsI, tsJ, Integer.parseInt(args[2]), distFn);

			Log.d("DTW", "Distance: "+String.valueOf(info.getDistance()));
			//Log.d("DTW", "Path: "+String.valueOf(info.getPath()));
			now = String.valueOf(info.getPath());
			return info.getDistance();
		}  // end if

	}  // end main()


}  // end class FastDtwTest
