package edu.sesame.motiondatacollector;

import java.io.File;
import java.io.FileFilter;

import android.util.Log;

import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;
import com.dtw.TimeWarpInfo;

public class FastDtwTest
{
	public static final int THRESHOLD = 120;
	File dir;
	File[] dataFiles;
	public FastDtwTest(File dir){
		this.dir = dir;
		FileFilter filter = new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().matches(".*\\.csv") && !pathname.getName().equals("temp.csv");
			}
			
		};
		dataFiles = dir.listFiles(filter);
	}
	
	public String match(File data){
		String dataPath = data.getAbsolutePath();
		double[] dis = null;
		dis = new double[dataFiles.length];
		double min = Double.MAX_VALUE;
		int index =0;
		String[] args = null;
		int radius = 50;
		for(int i = 0; i < dataFiles.length; i++){
			args=new String[3];
			args[0] = dataFiles[i].getAbsolutePath();
			args[1] = dataPath;
			args[2] = String.valueOf(radius);
			dis[i] = execute(args);
			if(dis[i]<min && dis[i]!=-1){
				min = dis[i];
				index = i;
			}
		}
		if(min < THRESHOLD){
			return dataFiles[index].getName();
		}else{
			return "";
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
		{
			final TimeSeries tsI = new TimeSeries(args[0], false, false, ',');
			final TimeSeries tsJ = new TimeSeries(args[1], false, false, ',');

			final DistanceFunction distFn;
			if (args.length < 4)
			{
				distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance"); 
			}
			else
			{
				distFn = DistanceFunctionFactory.getDistFnByName(args[3]);
			}   // end if

			final TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(tsI, tsJ, Integer.parseInt(args[2]), distFn);

			Log.d("DTW", "Distance: "+String.valueOf(info.getDistance()));
			Log.d("DTW", "Path: "+String.valueOf(info.getPath()));
			return info.getDistance();
		}  // end if

	}  // end main()


}  // end class FastDtwTest
