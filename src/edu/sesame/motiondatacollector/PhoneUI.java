package edu.sesame.motiondatacollector;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
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
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class PhoneUI extends Activity {
	Receiver receiver;
	Receiver2 receiver2;
	Spinner spinner;
	Button startButton,stopButton,manageButton;
	String[] receivedData = new String[HelloSensorsControl.NUM_OF_ITEMS_PER_INTENT];
	protected boolean displayOn = false;
	public static final String TAG = "PhoneUI";
	Calendar c = null;
	@SuppressLint("SimpleDateFormat")
	SimpleDateFormat sdf = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
	@Override
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate!");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phone_ui_layout);
		startButton = (Button) findViewById(R.id.start_button);
		stopButton = (Button) findViewById(R.id.stop_button);
		spinner = (Spinner) findViewById(R.id.select_action);
		manageButton = (Button) findViewById(R.id.manage_action_button);
		receiver = new Receiver();
		receiver2 = new Receiver2();
		
		Button.OnTouchListener listener = new Button.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(v.getId()){
				case R.id.start_button:
					startRecording(String.valueOf(spinner.getSelectedItem()));
					spinner.setClickable(false);
					manageButton.setClickable(false);
					break;
				case R.id.stop_button:
					stopRecording();
					spinner.setClickable(true);
					manageButton.setClickable(true);
					break;
				case R.id.manage_action_button:
					startActivity(new Intent(PhoneUI.this, ManageAction.class));
					break;
				default:
					break;
				}
				return true;

			}
			
		};
		stopButton.setOnTouchListener(listener);
		startButton.setOnTouchListener(listener);
		manageButton.setOnTouchListener(listener);
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
		displayOn = Prefs.getDisplay(this);
		registerReceiver(receiver, new IntentFilter("DATA"));
		registerReceiver(receiver2, new IntentFilter("DESTROY"));
	}
	
	@Override
	public void onPause(){
		super.onPause();
		stopRecording();
		unregisterReceiver(receiver);
		unregisterReceiver(receiver2);
	}
	
	public void setAcc(String data){
		TextView acc = (TextView)findViewById(R.id.accelerometer_view);
		acc.append("\n" + data);
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
				for(int i = 0; i<HelloSensorsControl.NUM_OF_ITEMS_PER_INTENT;i++){
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
			if(displayOn){
				setAcc(receivedData);
			} else {
				c = Calendar.getInstance();
				setAcc("Received! "+sdf.format(c.getTime()));
			}
		}
	}
	private class Receiver2 extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			stopRecording();
			finish();
		}
		
	}
	
	@Override
	public void onBackPressed(){
		Intent i = new Intent(this, StartMenu.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		finish();
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
