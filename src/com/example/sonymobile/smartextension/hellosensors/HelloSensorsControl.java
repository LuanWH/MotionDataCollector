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

package com.example.sonymobile.smartextension.hellosensors;

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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This demonstrates how to collect and display data from two different sensors,
 * accelerometer and light.
 */
class HelloSensorsControl extends ControlExtension {
	
	public static final String aTAG = "HelloSensorsControl";

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.RGB_565;

    private int mWidth = 220;

    private int mHeight = 176;
    
    protected JSONObject obj;

    private int mCurrentSensor = 0;
    public Context serviceContext = null;
    private boolean isRecording = false;
    
    protected Intent pendingIntent;
    
    protected int pendingCount = 0;
    
    protected int writingCount = 0;
    
    //protected Queue<Intent> queue;
    
    //private boolean waitingFlag = true;
    protected Writer writer;
    protected File file;

    private List<AccessorySensor> mSensors = new ArrayList<AccessorySensor>();

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
        
        //queue = (Queue<Intent>) new LinkedList<Intent>();

        AccessorySensorManager manager = new AccessorySensorManager(context, hostAppPackageName);

        // Add accelerometer, if supported by the host application.
        if (DeviceInfoHelper.isSensorSupported(context, hostAppPackageName,
                SensorTypeValue.ACCELEROMETER)) {
            mSensors.add(manager.getSensor(SensorTypeValue.ACCELEROMETER));
        }

//        // Add magnetic field sensor, if supported by the host application.
//        if (DeviceInfoHelper.isSensorSupported(context, hostAppPackageName,
//                SensorTypeValue.MAGNETIC_FIELD)) {
//            mSensors.add(manager.getSensor(SensorTypeValue.MAGNETIC_FIELD));
//        }
//
//        // Add light sensor, if supported by the host application.
//        if (DeviceInfoHelper.isSensorSupported(context, hostAppPackageName, SensorTypeValue.LIGHT)) {
//            mSensors.add(manager.getSensor(SensorTypeValue.LIGHT));
//        }

        // Determine host application screen size.
        determineSize(context, hostAppPackageName);
        serviceContext = context;
        Intent intent = new Intent(serviceContext, PhoneUI.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final Intent intent2 = intent;
        Log.d("ServiceLALALALLA", "Creating Phone UI!");
        serviceContext.startActivity(intent2);

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
        

        updateCurrentDisplay(null);
    }

    @Override
    public void onPause() {
        // Stop sensor.
        unregister(isRecording);
    }

    @Override
    public void onDestroy() {
        // Stop sensor.
        unregisterAndDestroy();
    }

    /**
     * Checks if the control extension supports the given width.
     *
     * @param context The context.
     * @param int width The width.
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

//    @Override
//    public void onTouch(ControlTouchEvent event) {
//        super.onTouch(event);
//        if (event.getAction() == Control.Intents.TOUCH_ACTION_RELEASE) {
//            toggleSensor();
//        }
//    }

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
        return mSensors.get(mCurrentSensor);
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
//                if (sensor.isInterruptModeSupported()) {
//                    sensor.registerInterruptListener(mListener);
//                } else {
//                    sensor.registerFixedRateListener(mListener, Sensor.SensorRates.SENSOR_DELAY_UI);
//                }
            	sensor.registerFixedRateListener(mListener, Sensor.SensorRates.SENSOR_DELAY_UI);
            } catch (AccessorySensorException e) {
                Log.d(HelloSensorsExtensionService.LOG_TAG, "Failed to register listener", e);
            }
        }
        isRecording = true;
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
        isRecording = r;
    }

    /**
     * Unregisters any sensor event listeners and unsets the sensor currently
     * being used.
     */
    private void unregisterAndDestroy() {
        unregister(false);
        mSensors.clear();
        mSensors = null;
    }

    /**
     * Cycles between currently available sensors and updates the display with
     * new data.
     */
//    private void toggleSensor() {
//        // Unregister the current sensor.
//        unregister();
//
//        // Toggle sensor type.
//        //nextSensor();
//
//        // Register the new sensor.
//        register();
//
//        // Update the screen.
//        updateCurrentDisplay(null);
//    }

//    /**
//     * Cycles between sensors to be used.
//     */
//    private void nextSensor() {
//        if (mCurrentSensor == (mSensors.size() - 1)) {
//            mCurrentSensor = 0;
//        } else {
//            mCurrentSensor++;
//        }
//    }

    /**
     * Determines what sensor is currently being used and updates the display
     * with new data.
     *
     * @param sensorEvent
     */
    private void updateCurrentDisplay(AccessorySensorEvent sensorEvent) {
        AccessorySensor sensor = getCurrentSensor();
        if (sensor.getType().getName().equals(Registration.SensorTypeValue.ACCELEROMETER)
               // || sensor.getType().getName().equals(Registration.SensorTypeValue.MAGNETIC_FIELD)
                																					) {
            updateGenericSensorDisplay(sensorEvent, sensor.getType().getName());
        }
//        else {
//            updateLightSensorDisplay(sensorEvent);
//        }
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
                TextView xView = (TextView)sensorLayout.findViewById(R.id.sensor_value_x);
                TextView yView = (TextView)sensorLayout.findViewById(R.id.sensor_value_y);
                TextView zView = (TextView)sensorLayout.findViewById(R.id.sensor_value_z);

                // Show values with one decimal.
                xView.setText(String.format("%.1f", values[0]));
                yView.setText(String.format("%.1f", values[1]));
                zView.setText(String.format("%.1f", values[2]));
            }

            // Show time stamp in milliseconds. (Reading is in nanoseconds.)
            TextView timeStampView = (TextView)sensorLayout
                    .findViewById(R.id.sensor_value_timestamp);
            timeStampView.setText(String.format("%d", (long)(sensorEvent.getTimestamp() / 1e9)));

            // Show sensor accuracy.
            TextView accuracyView = (TextView)sensorLayout.findViewById(R.id.sensor_value_accuracy);
            accuracyView.setText(getAccuracyText(sensorEvent.getAccuracy()));
			
            
            
            if(pendingIntent == null||pendingCount==0){
            	pendingIntent = new Intent();
            	pendingIntent.setAction("DATA");
            }
        	if(pendingCount >= 20){
        		Log.d(aTAG, "Accelerometer sent!");
        		pendingIntent.putExtra("SENSOR_TYPE", sensorType);
        		//queue.offer(pendingIntent);				
				
				//if(waitingFlag){
					sendNext();
				//}
				pendingCount=0;	
        	}

            try{
	            obj = new JSONObject();
	            obj.put("TIME", (long)sensorEvent.getTimestamp()/1e6);
				obj.put("X", values[0]);
				obj.put("Y", values[1]);
				obj.put("Z", values[2]);  
	            pendingIntent.putExtra(Integer.toString(pendingCount), obj.toString());
	        	if(writingCount>=100||writer == null){        		
	        		try{	
	        			if(writer!=null){
	        				writer.write("]");
	        				writer.close();
	        			}
		        		file = new File(serviceContext.getExternalFilesDir(null), String.valueOf((long)sensorEvent.getTimestamp() / 1e6));
	        			writer = new BufferedWriter(new FileWriter(file));
	        			writer.write("[");
	        		}catch(IOException e){
	        			Log.d("HelloSensorsControl", "IOException");
	        		}
	        		writingCount=0;
	        	}
        		try{
        			writer.write(obj.toString());
        			if(writingCount<99){
        				writer.write(",");
        			}
        		} catch(IOException e){
        			Log.d("HelloSensorsControl", "IOException");
        		}
	            pendingCount++;
	            writingCount++;
            } catch(JSONException e){
            	Log.d("HelloSensorsControl", "Can't create JSON String!");
            }          	
            
//            if (values != null && values.length == 3) {
//	            Intent intent = new Intent();
//				intent.setAction("DATA");
//				intent.putExtra("TIME", String.format("%d", (long)(sensorEvent.getTimestamp() / 1e9)));
//				intent.putExtra("SENSOR_TYPE", sensorType);
//				intent.putExtra("X", String.format("%.1f", values[0]));
//				intent.putExtra("Y", String.format("%.1f", values[1]));
//				intent.putExtra("Z", String.format("%.1f", values[2]));
//				intent.putExtra("ALL", "x-axis: "+String.format("%.1f", values[0])+"\n"
//										+ "y-axis: "+String.format("%.1f", values[1]) +"\n"
//										+ "z-axis: "+String.format("%.1f", values[2]));
//				Log.d(aTAG, "Accelerometer sent!");
//				serviceContext.sendBroadcast(intent);
//			}
        }

        root.measure(mWidth, mHeight);
        root.layout(0, 0, mWidth, mHeight);

        Canvas canvas = new Canvas(bitmap);
        sensorLayout.draw(canvas);

        showBitmap(bitmap);
    }

    /**
     * Updates the display with new light sensor data.
     *
     * @param sensorEvent The sensor event.
     */
/*    private void updateLightSensorDisplay(AccessorySensorEvent sensorEvent) {
        // Create bitmap to draw in.
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, BITMAP_CONFIG);

        // Set default density to avoid scaling.
        bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);

        LinearLayout root = new LinearLayout(mContext);
        root.setLayoutParams(new LayoutParams(mWidth, mHeight));

        LinearLayout sensorLayout = (LinearLayout)LinearLayout.inflate(mContext,
                R.layout.lightsensor_values, root);

        // Update the values.
        if (sensorEvent != null) {
            float[] values = sensorEvent.getSensorValues();

            if (values != null && values.length == 1) {
                TextView xView = (TextView)sensorLayout.findViewById(R.id.light_value);

                // Show values with one decimal.
                xView.setText(String.format("%.1f", values[0]));
            }

            // Show time stamp in milliseconds. (Reading is in nanoseconds.)
            TextView timeStampView = (TextView)sensorLayout
                    .findViewById(R.id.light_value_timestamp);
            timeStampView.setText(String.format("%d", (long)(sensorEvent.getTimestamp() / 1e9)));

            // Show sensor accuracy.
            TextView accuracyView = (TextView)sensorLayout.findViewById(R.id.light_value_accuracy);
            accuracyView.setText(getAccuracyText(sensorEvent.getAccuracy()));
            if (values != null && values.length == 1) {
	            Intent intent = new Intent();
				intent.setAction("DATA");
				intent.putExtra("TIME", String.format("%d", (long)(sensorEvent.getTimestamp() / 1e9)));
				intent.putExtra("SENSOR_TYPE", "Light Sensor");
				intent.putExtra("ALL", "Light: "+String.format("%.1f", values[0]));
				Log.d(aTAG, "Light Sensor sent!");
				serviceContext.sendBroadcast(intent);
			}
        }

        sensorLayout.measure(mWidth, mHeight);
        sensorLayout
                .layout(0, 0, sensorLayout.getMeasuredWidth(), sensorLayout.getMeasuredHeight());

        Canvas canvas = new Canvas(bitmap);
        sensorLayout.draw(canvas);

        showBitmap(bitmap);
    }*/

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
    
    public void sendNext(){
    	//if(!queue.isEmpty()){
    		//serviceContext.sendBroadcast(queue.poll());
    		serviceContext.sendBroadcast(pendingIntent);
    	//	waitingFlag = false;
    	//} else{
    	//	waitingFlag = true;
    	//}
    }
}
