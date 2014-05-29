package edu.sesame.motiondatacollector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class PhoneUI extends Activity {
	TextView statusView;
	Receiver receiver;
	Receiver2 receiver2;
	Receiver3 receiver3;
	Spinner spinner;
	Button startButton,stopButton,manageButton;
	ActionBar actionBar;
	String[] receivedData = new String[HelloSensorsControl.NUM_OF_ITEMS_PER_INTENT];
	protected boolean displayOn = false;
	public static final String TAG = "PhoneUI";
	Calendar c = null;
	@SuppressLint("SimpleDateFormat")
	SimpleDateFormat sdf = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
	ArrayAdapter<String> aa;
	ArrayList<String> list;
	Boolean isControl = false;
	Boolean isRecording = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate!");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phone_ui_layout);
		startButton = (Button) findViewById(R.id.start_button);
		stopButton = (Button) findViewById(R.id.stop_button);
		stopButton.setFocusableInTouchMode(false);
		manageButton = (Button) findViewById(R.id.manage_action_button);
		statusView = (TextView) findViewById(R.id.status_view);
		receiver = new Receiver();
		receiver2 = new Receiver2();
		receiver3 = new Receiver3();
		Button.OnTouchListener listener = new Button.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(v.getId()){
				case R.id.start_button:
					Intent i = new Intent();
					i.setAction("isControl");
					sendBroadcast(i);
					if(isControl){
						startRecording(String.valueOf(spinner.getSelectedItem()));
						spinner.setFocusableInTouchMode(false);
						manageButton.setFocusableInTouchMode(false);
						startButton.setFocusableInTouchMode(false);
						stopButton.setFocusableInTouchMode(true);
						isRecording = true;
					} else {
						new AlertDialog.Builder(PhoneUI.this)
							.setTitle("Warning")
							.setMessage("Record aborted: Please open Data Collector on your connected device.")
							.setPositiveButton("Back", new OnClickListener(){

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent i = new Intent(PhoneUI.this, PhoneUI.class);
									i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(i);
									dialog.dismiss();
								}
								
							})
							.show();
						isRecording = false;
					}
					break;
				case R.id.stop_button:
					if(isRecording = true){
						stopRecording();
						spinner.setFocusableInTouchMode(true);
						manageButton.setFocusableInTouchMode(true);
						startButton.setFocusableInTouchMode(true);
					}
					isRecording = false;
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
		actionBar = getActionBar();
		actionBar.show();
		actionBar.setDisplayHomeAsUpEnabled(true);
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
		registerReceiver(receiver3, new IntentFilter("CONTROL"));
		spinner = (Spinner) findViewById(R.id.select_action);
		if(Prefs.getDefaults(ManageAction.KEY, getBaseContext()) == null){
			list = new ArrayList<String>(Arrays.asList(ManageAction.defaultList));
		} else {
			list = new ArrayList<String>(Prefs.getDefaults(ManageAction.KEY, getBaseContext()));
		}
		aa = new ArrayAdapter<String>(this, R.layout.list, list);
		spinner.setAdapter(aa);
		spinner.refreshDrawableState();
		Intent i = new Intent();
		i.setAction("isControl");
		sendBroadcast(i);
		String temp = Boolean.toString(Prefs.getFilter(this));
		String temp2 = Boolean.toString(Prefs.getStorage(this));
		statusView.setText("Display: "+Boolean.toString(displayOn)
				+", Storage: "+temp2+", Gravity Filter: "+temp);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if(isRecording){
			stopRecording();
		}
		isRecording = false;
		startButton.setFocusableInTouchMode(true);
		unregisterReceiver(receiver);
		unregisterReceiver(receiver2);
		unregisterReceiver(receiver3);
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
			isControl = false;
			new AlertDialog.Builder(PhoneUI.this)
				.setTitle("Warning")
				.setMessage("Data Collector on smart watch has stopped!")
				.setPositiveButton("Ok", null)
				.show();
		}
		
	}
	private class Receiver3 extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getStringExtra("CONTROL")!=null){
				if(intent.getStringExtra("CONTROL").equals("ON")){
					isControl = true;
				} else {
					isControl = false;
				}
			}
		}
		
	}
	
	@Override
	public void onBackPressed(){
		Intent i = new Intent(this, StartMenu.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.phone_ui_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.phone_ui_open_settings:
			startActivity(new Intent(PhoneUI.this, Prefs.class));
			break;
		case android.R.id.home:
			onBackPressed();
			break;
		default:
			break;
		}
		return true;
	}

}
