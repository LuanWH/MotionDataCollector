/*
 Copyright (c) 2011, Sony Ericsson Mobile Communications AB
 Copyright (c) 2011-2013, Sony Mobile Communications AB

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB nor the names
 of its contributors may be used to endorse or promote products derived from
 this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.sesame.motiondatacollector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.aef.registration.Registration.SensorTypeValue;
import com.sonyericsson.extras.liveware.aef.sensor.Sensor;
import com.sonyericsson.extras.liveware.aef.sensor.Sensor.SensorAccuracy;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfoHelper;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensor;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorEvent;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorEventListener;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorException;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This demonstrates how to collect and display data from two different sensors,
 * accelerometer and light.
 */
class HelloSensorsControl extends ControlExtension {
	/**
	 * Constants
	 */
	public static final String aTAG = "HelloSensorsControl";
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.RGB_565;
    /**
     * Attributes: defined by preferences or the service
     */
	public int countInterval = 2;	    
    public int patternLength = 30;
    public int patternDelay = 2;
    public String patternName = "DEFAULT";
    private int mWidth = 220;
    private int numbersPerFile = 1000;
    private int mHeight = 176;    
    protected int matchingFrequency;      
    public HelloSensorsExtensionService service;
    
    /**
     * Tools: to be used across methods
     */
    protected JSONObject obj;
    private int mCurrentSensor = 0,queueLength;
    public Context serviceContext = null;
    protected long recordStartTime,startTime;
    protected ArrayList<String> al;
    public String lastTime = null, estGravity = "0", action;
    protected LPFilter lpFilter;
    protected Writer writer, patternWriter;
    protected File file, csv = null;
    private List<AccessorySensor> mSensors = new ArrayList<AccessorySensor>();    
	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-HH-mm-ss");
	Calendar c = null;    
	protected Queue<Double[]> queue;
	FastDtwTest testRaw, testNorm;
	protected String[] fileFormatArray = {".json",".csv", ".dat"};
    
    /**
     * Flags:control logic flow
     */
    private boolean storageOn = true;
    private boolean filterOn = true;   
    private boolean isRecording = false;      
    public boolean isGIntent = false;
    public boolean isPattern = false;
    private boolean waitingFlag = false;
    public int fileFormat = Prefs.FILE_FORMAT_JSON_INDEX;
    public int type= FastDtwTest.NORM;
    /**
     * Counts: variables for counting
     */  
    protected int writingCount = 0;    
    protected int gravityCount = 0;
    protected int patternCount = 0;
    protected int filterCount = 2;
    protected int matchingCount = 0;
    protected int itemCount=0;
    
    private final AccessorySensorEventListener mListener = new AccessorySensorEventListener() {

        @Override
        public void onSensorEvent(AccessorySensorEvent sensorEvent) {
            Log.d(HelloSensorsExtensionService.LOG_TAG, "Listener: OnSensorEvent");
            updateCurrentDisplay(sensorEvent);
        }
    };

    /**
     * Creates a control extension.
     *
     * @param hostAppPackageName Package name of host application.
     * @param context The context.
     */
    HelloSensorsControl(final String hostAppPackageName, final Context context) {
        super(context, hostAppPackageName);
        AccessorySensorManager manager = new AccessorySensorManager(context, hostAppPackageName);
        // Add accelerometer, if supported by the host application.
        if (DeviceInfoHelper.isSensorSupported(context, hostAppPackageName,
                SensorTypeValue.ACCELEROMETER)) {
            mSensors.add(manager.getSensor(SensorTypeValue.ACCELEROMETER));
        }
        // Determine host application screen size.
        determineSize(context, hostAppPackageName);
        serviceContext = context;
        service = (HelloSensorsExtensionService) serviceContext;
        updateSettings();
    }

    @Override
    public void onResume() {
        Log.d(HelloSensorsExtensionService.LOG_TAG, "Starting control");
        // Note: Setting the screen to be always on will drain the accessory
        // battery. It is done here solely for demonstration purposes.
        setScreenState(Control.Intents.SCREEN_STATE_ON);

        // Start listening for sensor updates.
        if(isRecording){
        	register();
        }
        updateSettings();
        updateCurrentDisplay(null);
    }
    
    /**
     * Refresh settings and reload queue & test
     */
    public void updateSettings(){
        storageOn = Prefs.getStorage(serviceContext);
        filterOn = Prefs.getFilter(serviceContext);
        countInterval = Prefs.getCountInterval(serviceContext);
        filterCount = countInterval;     
        numbersPerFile = Integer.valueOf(Prefs.getNumbers(serviceContext));
        matchingFrequency = Prefs.getMatchingFrequency(serviceContext);
        waitingFlag = false;
        queue = new LinkedList<Double[]>();
        queueLength = Prefs.getQueueLength(serviceContext);
		testRaw = new FastDtwTest(serviceContext.getExternalFilesDir(null), serviceContext, 0);
		testNorm = new FastDtwTest(serviceContext.getExternalFilesDir(null), serviceContext, 1);  
		testRaw.loadPatterns();
		testNorm.loadPatterns();
		fileFormat = Prefs.getFileFormat(serviceContext);
    }

    @Override
    public void onPause() {
        // Stop sensor.
        unregister(isRecording);
        waitingFlag = false;
    }

    @Override
    public void onDestroy() {
        // Stop sensor.
        unregisterAndDestroy();
        Intent i = new Intent();
        i.setAction("DESTROY");
        serviceContext.sendBroadcast(i);
    }

    /**
     * Checks if the control extension supports the given width.
     *
     * @param context The context.
     * @param width The width.
     * @return True if the control extension supports the given width.
     */
    public static boolean isWidthSupported(Context context, int width) {
        return width == context.getResources().getDimensionPixelSize(
                R.dimen.smart_watch_2_control_width)
                || width == context.getResources().getDimensionPixelSize(
                        R.dimen.smart_watch_control_width);
    }

    /**
     * Checks if the control extension supports the given height.
     *
     * @param context The context.
     * @param int height The height.
     * @return True if the control extension supports the given height.
     */
    public static boolean isHeightSupported(Context context, int height) {
        return height == context.getResources().getDimensionPixelSize(
                R.dimen.smart_watch_2_control_height)
                || height == context.getResources().getDimensionPixelSize(
                        R.dimen.smart_watch_control_height);
    }

    /**
     * Determines the width and height in pixels of a given host application.
     *
     * @param context The context.
     * @param hostAppPackageName The host application.
     */
    private void determineSize(Context context, String hostAppPackageName) {
        Log.d(HelloSensorsExtensionService.LOG_TAG, "Now determine screen size.");

        boolean smartWatch2Supported = DeviceInfoHelper.isSmartWatch2ApiAndScreenDetected(context,
                hostAppPackageName);
        if (smartWatch2Supported) {
            mWidth = context.getResources().getDimensionPixelSize(
                    R.dimen.smart_watch_2_control_width);
            mHeight = context.getResources().getDimensionPixelSize(
                    R.dimen.smart_watch_2_control_height);
        } else {
            mWidth = context.getResources()
                    .getDimensionPixelSize(R.dimen.smart_watch_control_width);
            mHeight = context.getResources().getDimensionPixelSize(
                    R.dimen.smart_watch_control_height);
        }
    }

    /**
     * Returns the sensor currently being used.
     *
     * @return The sensor.
     */
    private AccessorySensor getCurrentSensor() {
    	if(mSensors!=null){
    		return mSensors.get(mCurrentSensor);
    	} else {
    		return null;
    	}
    }

    /**
     * Checks if the sensor currently being used supports interrupt mode and
     * registers an interrupt listener if it does. If not, a fixed rate listener
     * will be registered instead.
     */
    void register() {
        Log.d(HelloSensorsExtensionService.LOG_TAG, "Register listener");

        AccessorySensor sensor = getCurrentSensor();
        if (sensor != null) {
            try {
            	sensor.registerFixedRateListener(mListener, Sensor.SensorRates.SENSOR_DELAY_UI);
            } catch (AccessorySensorException e) {
                Log.d(HelloSensorsExtensionService.LOG_TAG, "Failed to register listener", e);
            }
        }
        if(isRecording){
        	startTime = System.currentTimeMillis() - startTime;
        	recordStartTime = System.currentTimeMillis()-recordStartTime;
        } else {
            isRecording = true;
            startTime = System.currentTimeMillis();
            lpFilter = new LPFilter(serviceContext);
            updateSettings();       	
        }
        
        Timer timer= new Timer();
        TimerTask timerTask = new TimerTask(){

			@Override
			public void run() {
				waitingFlag = true;
			}
        	
        };
        timer.schedule(timerTask, patternDelay*1000);
    }

    /**
     * Unregisters any sensor event listeners connected to the sensor currently
     * being used.
     */

    void unregister(boolean r) {
        AccessorySensor sensor = getCurrentSensor();
        if (sensor != null) {
            sensor.unregisterListener();
            
        }
        if (writer != null && storageOn && !r){
        	try{
        		if(fileFormat == Prefs.FILE_FORMAT_JSON_INDEX){
        			writer.write("]");
        		}
        		
        		writer.close();
        		writer = null;
                int duration = (int)(System.currentTimeMillis() - startTime);
        		Prefs.setInteger(
        				ViewStats.TOTAL_ITEMS, 
        				Prefs.getInteger(ViewStats.TOTAL_ITEMS, serviceContext) + itemCount,
        				serviceContext);	
        		Prefs.setInteger(
        				ViewStats.TOTAL_TIME, 
        				Prefs.getInteger(ViewStats.TOTAL_TIME, serviceContext) + duration,
        				serviceContext);
        		if(Prefs.getDefaults(ViewStats.LOGS, serviceContext)!= null){
        			al = new ArrayList<String>(Prefs.getDefaults(ViewStats.LOGS, serviceContext));
        		} else {
        			al = new ArrayList<String>();
        		}
        		al.add( "On "+sdf.format(c.getTime())+" record "+itemCount+" items for "+String.format("%.2f",duration/1000.0)+" seconds "
        				+"with Gravity Filter "+ Boolean.toString(filterOn));
        		Prefs.setDefaults(ViewStats.LOGS, new HashSet<String>(al), serviceContext);
        		itemCount = 0;
        	} catch(IOException e){
        		Log.d("HelloSensorsControl", "IOException");
        	}
        } else if(r){
        	recordStartTime = System.currentTimeMillis() - recordStartTime;
        	startTime = System.currentTimeMillis() - startTime;
        }
        
        if(patternWriter!=null && isPattern){
        	try {
				patternWriter.close();
				patternWriter = null;
				isPattern = false;
				waitingFlag = false;
				csv = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
        	
        }
        isRecording = r;

    }

    /**
     * Unregisters any sensor event listeners and unsets the sensor currently
     * being used.
     */
    public void unregisterAndDestroy() {
        unregister(false);
        mSensors.clear();
        mSensors = null;
    }
    

    /**
     * Determines what sensor is currently being used and updates the display
     * with new data.
     *
     * @param sensorEvent
     */
    private void updateCurrentDisplay(AccessorySensorEvent sensorEvent) {
    	if(filterCount >= countInterval){
	        AccessorySensor sensor = getCurrentSensor();
	        if(sensor!=null){
		        if (sensor.getType().getName().equals(Registration.SensorTypeValue.ACCELEROMETER)) {
		            updateGenericSensorDisplay(sensorEvent, sensor.getType().getName());
		        }
	        }
	        filterCount = 0;
    	}else{
    		filterCount++;
    	}
    	
    }

    /**
     * Updates the display with new accelerometer data.
     *
     * @param sensorEvent The sensor event.
     */
    private void updateGenericSensorDisplay(AccessorySensorEvent sensorEvent, String sensorType) {
        // Create bitmap to draw in.
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, BITMAP_CONFIG);

        // Set default density to avoid scaling.
        bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);

        LinearLayout root = new LinearLayout(mContext);
        root.setLayoutParams(new ViewGroup.LayoutParams(mWidth, mHeight));
        root.setGravity(Gravity.CENTER);

        LayoutInflater inflater = (LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout sensorLayout = (LinearLayout)inflater.inflate(R.layout.generic_sensor_values,
                root, true);

        TextView title = (TextView)sensorLayout.findViewById(R.id.sensor_title);
        title.setText(sensorType);
        
        
        // Update the values.
        if (sensorEvent != null) {
            float[] values = sensorEvent.getSensorValues();

            if (values != null && values.length == 3) {
            	if(filterOn){
            		values = lpFilter.filter(values);
            	}
            	if(isPattern && waitingFlag){
	            	writeToCSV(values[0],values[1],values[2]);
            	}
            	
            	if(action!=null&&action.equals("MOTION")){
            		match(values);
            	}
            	
                TextView xView = (TextView)sensorLayout.findViewById(R.id.sensor_value_x);
                TextView yView = (TextView)sensorLayout.findViewById(R.id.sensor_value_y);
                TextView zView = (TextView)sensorLayout.findViewById(R.id.sensor_value_z);

                // Show values with one decimal.
                xView.setText(String.format("%.1f", values[0]));
                yView.setText(String.format("%.1f", values[1]));
                zView.setText(String.format("%.1f", values[2]));
            

	            // Show time stamp in milliseconds. (Reading is in nanoseconds.)
	            TextView timeStampView = (TextView)sensorLayout
	                    .findViewById(R.id.sensor_value_timestamp);
				
	            timeStampView.setText(String.format("%d", (long)(sensorEvent.getTimestamp() / 1e9)));
	
	            // Show sensor accuracy.
	            TextView accuracyView = (TextView)sensorLayout.findViewById(R.id.sensor_value_accuracy);
	            if(lpFilter!=null && lpFilter.gravity!=null){
	            	estGravity = String.format("%.2f",Math.sqrt(
	            			Math.pow(lpFilter.gravity[0],2)+
	            			Math.pow(lpFilter.gravity[1],2)+
	            			Math.pow(lpFilter.gravity[2],2)));
	            	accuracyView.setText(estGravity);
	            } else {
	            	accuracyView.setText(getAccuracyText(sensorEvent.getAccuracy()));
	            }
	            
	            
	            if(isGIntent){
		            if(gravityCount >= 2){
		        		if(estGravity!=null){
//		        			Intent gIntent = new Intent();
//		        			gIntent.setAction("DATA");	        			
//		        			gIntent.putExtra("G", String.format("%.2f", Float.valueOf(estGravity)));
//		        			serviceContext.sendBroadcast(gIntent);
		        			service.holder.setGravity(String.format("%.2f", Float.valueOf(estGravity)));
		        			Log.d("HelloSensorsControl","send gravity!");
		        		}
		        		gravityCount =0;
		            } else {
		            	gravityCount++;
		            }
	            }
					
	        	if(storageOn){
		            try{
		            	if(writer == null){
		            		recordStartTime = System.currentTimeMillis();
		            	}
			            obj = new JSONObject();
			            obj.put("TIME", (long)System.currentTimeMillis() - recordStartTime);
						obj.put("X", values[0]);
						obj.put("Y", values[1]);
						obj.put("Z", values[2]);  
			            if(storageOn){
				        	if(writingCount>=numbersPerFile||writer == null){        		
				        		try{	
				        			if(writer!=null){
				        				if(fileFormat == Prefs.FILE_FORMAT_JSON_INDEX){
				        					writer.write("]");
				        				}
				        				writer.close();
				        				writer = null;
				        			}
				        			c = Calendar.getInstance();
				        			if(action != null){
				        				
						        		file = new File(serviceContext.getExternalFilesDir(null), 
						        				action + "#"+sdf.format(c.getTime()) + fileFormatArray[fileFormat]);
						        		lastTime = sdf.format(c.getTime());
				        			} else {
						        		file = new File(serviceContext.getExternalFilesDir(null), 
						        				sdf.format(c.getTime()) + fileFormatArray[fileFormat]);	        				
				        			}
				        			Prefs.setString(Prefs.FILE_NAME, file.getName(), serviceContext);
				        			writer = new BufferedWriter(new FileWriter(file));
				        			if(fileFormat == Prefs.FILE_FORMAT_JSON_INDEX){
				        				writer.write("[");
				        			}
				        			Prefs.setInteger(
				        					ViewStats.TOTAL_COUNTS, 
				        					Prefs.getInteger(ViewStats.TOTAL_COUNTS, serviceContext) + 1,
				        					serviceContext);
				        		}catch(IOException e){
				        			Log.d("HelloSensorsControl", "IOException");
				        		}
				        		writingCount=0;
				        	}
			            
			        		try{
			        			if(fileFormat == Prefs.FILE_FORMAT_JSON_INDEX){
				        			if(writingCount != 0){
				        				writer.write(",");
				        			}
				        			writer.write(obj.toString());			        				
			        			} else {
			        				String splitter = " ";
			        				if(fileFormat == Prefs.FILE_FORMAT_CSV_INDEX){
			        					splitter = ",";
			        				} else if(fileFormat == Prefs.FILE_FORMAT_DAT_INDEX){
			        					splitter = " ";
			        				}
			        				String builder = "";
			        				builder += String.valueOf(obj.get("TIME")) +splitter;
			        				builder += String.valueOf(obj.get("X")) + splitter;
			        				builder += String.valueOf(obj.get("Y")) + splitter;
			        				builder += String.valueOf(obj.get("Z")) + "\n";
			        				writer.write(builder);
			        			}

			        			itemCount++;
			        		} catch(IOException e){
			        			Log.d("HelloSensorsControl", "IOException");
			        		}
			        		if(writingCount%20==0){
//			        			Intent intent = new Intent();
//			        			intent.setAction("DATA");
//			        			intent.putExtra("COUNT", true);
//			        			serviceContext.sendBroadcast(intent);
			        			service.holder.updateWritingCount();
			        		}
			        		
			        		writingCount++;
			        		
			            }
		            } catch(JSONException e){
		            	Log.d("HelloSensorsControl", "Can't create JSON String!");
		            }          	
		        }
	        }
        }
        root.measure(mWidth, mHeight);
        root.layout(0, 0, mWidth, mHeight);

        Canvas canvas = new Canvas(bitmap);
        sensorLayout.draw(canvas);

        showBitmap(bitmap);
    }

    /**
     * Converts an accuracy value to a string.
     *
     * @param accuracy The accuracy value.
     * @return The text.
     */
    @SuppressLint("DefaultLocale")
    private String getAccuracyText(int accuracy) {

        switch (accuracy) {
            case SensorAccuracy.SENSOR_STATUS_UNRELIABLE:
                return mContext.getString(R.string.accuracy_unreliable);
            case SensorAccuracy.SENSOR_STATUS_ACCURACY_LOW:
                return mContext.getString(R.string.accuracy_low);
            case SensorAccuracy.SENSOR_STATUS_ACCURACY_MEDIUM:
                return mContext.getString(R.string.accuracy_medium);
            case SensorAccuracy.SENSOR_STATUS_ACCURACY_HIGH:
                return mContext.getString(R.string.accuracy_high);
            default:
                return String.format("%d", accuracy);
        }
    }
    
    /**
     * Write the accelerometer data to CSV files.
     * @param x Acceleration along x-axis
     * @param y Acceleration along y-axis
     * @param z Acceleration along z-axis
     */
    private void writeToCSV(double x, double y, double z){
    	try{
	    	if(csv == null){
	    		c = Calendar.getInstance();
	    		csv = new File(serviceContext.getExternalFilesDir(null), patternName+"#RAW#"+sdf.format(c.getTime())+".csv");
	    		patternCount = 0;
	    		patternWriter = new BufferedWriter(new FileWriter(csv));
	    	}
	    	if(patternCount < patternLength){
	    		patternWriter.write(x+","+y+","+z);
	    		if(patternCount!=patternLength-1){
	    			patternWriter.write("\n");
	    		}
	    		patternCount++;
//	    		Intent intent = new Intent();
//	    		intent.setAction("DATA");
//	    		intent.putExtra("PATTERN", true);
//	    		intent.putExtra("PATTERN_COUNT", patternCount);
//	    		serviceContext.sendBroadcast(intent);	
	    		if(patternCount%10 == 0)
	    		service.holder.updatePatternCount(patternCount);
	    		
	    	} else {
	    		patternWriter.close();
	    		patternWriter = null;
	    		csv = null;
	    		isPattern = false;
	    		waitingFlag= false;
	    		Intent intent = new Intent();
	    		intent.setAction("DATA");
	    		intent.putExtra("PATTERN_STOP", true);
	    		serviceContext.sendBroadcast(intent);
	    	}
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    /**
     * Update new values for matching.
     * It will update the queue but only invoke the matching process after a 
     * defined interval "MatchingFrequency".
     * Two types: NORM for normalize the data and match, RAW for direct matching. 
     * Change the type by changing "type".
     * @param values 3-dimensional acceleration data 
     * in the following order: x-axis, y-axis, z-axis
     */
    private void match(float[] values){
    	if(values.length!=3){
    		return;
    	}
    	Double[] d = new Double[3];
    	for(int i = 0; i < 3;i++){
    		d[i] = Double.valueOf(values[i]);
    	}
    	if(queue.size()<queueLength){
    		queue.offer(d);
    	} else {
    		queue.poll();
    		queue.offer(d);
    	}
    	if(matchingCount>=matchingFrequency && queue.size()==queueLength){
    		final int finaltype = type;
    		Thread cal = new Thread(new Runnable(){

				@Override
				public void run() {
					String[] ss = null;
		    		if(finaltype == FastDtwTest.NORM){
		    			ss = testNorm.match(queue.toArray(new Double[0][0]));
		    		} else if(finaltype == FastDtwTest.RAW){
		    			ss = testRaw.match(queue.toArray(new Double[0][0]));
		    		}
		    		if(ss!=null){
//		        		Intent intent = new Intent();
//		        		intent.setAction("DATA");
//		        		intent.putExtra("isMatch", true);
//		        		intent.putExtra("MATCH_FILE", ss[0].split("#")[0]);
//		        		intent.putExtra("MATCH_DETAIL", "Distance: "+ss[2]+"\nPath: "+ss[1]);
//		        		serviceContext.sendBroadcast(intent);
		    			final String[] sss =ss;
		    			service.holder.runOnUiThread(new Runnable(){

							@Override
							public void run() {
								service.holder.updateMotion(sss[0].split("#")[0], "Distance: "+sss[2]+"\nPath: "+sss[1]);
							}
		    			
		    			});
		    		}
				}
    			
    		});
    		cal.start();
    		matchingCount=0;
    	}
    	matchingCount++;
    }
}