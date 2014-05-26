package com.example.sonymobile.smartextension.hellosensors;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class PhoneUI extends Activity {
	Receiver receiver = new Receiver();
	Spinner spinner;
	Button startButton,stopButton;
	String[] receivedData = new String[20];
	public static final String TAG = "PhoneUI";
	@Override
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate!");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phone_ui_layout);
		startButton = (Button) findViewById(R.id.start_button);
		stopButton = (Button) findViewById(R.id.stop_button);
		spinner = (Spinner) findViewById(R.id.select_action);
		
		Button.OnTouchListener startListener = new Button.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				startRecording(String.valueOf(spinner.getSelectedItem()));
				return true;
			}
			
		};
		Button.OnTouchListener stopListener = new Button.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				stopRecording();
				return true;
			}
			
		};		
		stopButton.setOnTouchListener(stopListener);
		startButton.setOnTouchListener(startListener);
//		Intent intent = new Intent();
//		intent.setAction("NEXT");
//		sendBroadcast(intent);
	}
	
	private void stopRecording(){
		Intent i = new Intent();
		i.putExtra("START_OR_STOP","STOP");
		i.setAction("RECORDING");
		sendBroadcast(i);		
	}
	
	private void startRecording(String item){
		Intent i = new Intent();
		i.putExtra("ACTION", item);
		i.putExtra("START_OR_STOP", "START");
		i.setAction("RECORDING");
		sendBroadcast(i);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		registerReceiver(receiver, new IntentFilter("DATA"));
	}
	
	@Override
	public void onPause(){
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	public void setAcc(String[] data){
		TextView acc = (TextView)findViewById(R.id.accelerometer_view);		
		String temp ="";
		for(int i = 0; i < data.length;i++){
			temp+="\n"+data[i];
		}
		acc.setText(temp);
//		Intent intent = new Intent();
//		intent.setAction("NEXT");
//		sendBroadcast(intent);
	}
	private class Receiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Received!");
			if(intent.getStringExtra("SENSOR_TYPE").equals("Accelerometer")){
				JSONObject obj;
				for(int i = 0; i<20;i++){
					try{
						obj = new JSONObject(intent.getStringExtra(Integer.toString(i)));
						receivedData[i] = "Time: "+obj.getLong("TIME")+"; x-axis: "+ String.format("%.1f", obj.getDouble("X"))
								+ "; y-axis: "+String.format("%.1f", obj.getDouble("Y"))
								+ "; z-axis: "+String.format("%.1f", obj.getDouble("Z"));
					}catch(JSONException e){
						Log.d(TAG, "JSONException!");
					}
				}
			}
			setAcc(receivedData);
		}
		
	}
	
//	//Start binding service
//	private HelloSensorsExtensionService iservice;
//	
//
//
//	@Override
//	protected void onStart(){
//		super.onStart();
//		bindService(new Intent(this, HelloSensorsExtensionService.class), mConnection, Context.BIND_AUTO_CREATE);
//		Log.d("gear","Bound!");
//	}
//	
//	@Override
//	protected void onStop(){
//		super.onStop();
//		if(mBound){
//			unbindService(mConnection);
//			mBound = false;
//		}
//	}
//	public boolean mBound;
//	
//	public ServiceConnection mConnection = new ServiceConnection(){
//		@Override
//		public void onServiceConnected(ComponentName className, IBinder aservice){
//			com.example.sonymobile.smartextension.hellosensors.HelloSensorsExtensionService.LocalBinder binder = (com.example.sonymobile.smartextension.hellosensors.HelloSensorsExtensionService.LocalBinder) aservice;
//			iservice = binder.getService();
//			mBound = true;
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName name) {
//			mBound = false;
//		}
//		
//		
//	};
}
