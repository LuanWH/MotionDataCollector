package edu.sesame.motiondatacollector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

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
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class PhoneUI extends Activity {
	TextView statusView, gravityView, motionHistoryView, motionCurrentView;
	Receiver receiver;
	Receiver2 receiver2;
	Receiver3 receiver3;
	Spinner spinner;
	Button startButton,stopButton,manageButton,pauseButton;
	ActionBar actionBar;
	int matchingFrequency;
	String[] receivedData;
	protected boolean displayOn = false;
	public static final String TAG = "PhoneUI";
	Calendar c = null;
	@SuppressLint("SimpleDateFormat")
	SimpleDateFormat sdf = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
	ArrayAdapter<String> aa;
	ArrayList<String> list;
	Boolean isControl = false;
	Boolean isRecording = false;
	Boolean matchFlag = false;
	FastDtwTest test;
	Queue<Double[]> queue;
	@Override
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate!");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phone_ui_layout);
		startButton = (Button) findViewById(R.id.start_button);
		stopButton = (Button) findViewById(R.id.stop_button);
		stopButton.setEnabled(false);
		manageButton = (Button) findViewById(R.id.manage_action_button);
		statusView = (TextView) findViewById(R.id.status_view);
		gravityView = (TextView) findViewById(R.id.gravity_view);
		motionHistoryView = (TextView) findViewById(R.id.phone_ui_motion_history_view);
		motionCurrentView = (TextView) findViewById(R.id.phone_ui_motion_current_view);
		pauseButton = (Button) findViewById(R.id.pause_button);
		pauseButton.setEnabled(false);
		receiver = new Receiver();
		receiver2 = new Receiver2();
		receiver3 = new Receiver3();
		gravityView.setText("Estimated Gravity Value: Waiting");
		test = new FastDtwTest(getExternalFilesDir(null));
		queue = new LinkedList<Double[]>();
		
		Button.OnTouchListener listener = new Button.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(v.getId()){
				case R.id.start_button:
					Intent i = new Intent();
					i.setAction("isControl");
					sendBroadcast(i);

					if(isControl){
						if(startButton.getText().toString().equals("Resume")){
							resumeRecording();
						} else {
							startRecording(String.valueOf(spinner.getSelectedItem()));
						}
						
						spinner.setEnabled(false);
						manageButton.setEnabled(false);
						startButton.setEnabled(false);
						stopButton.setEnabled(true);
						pauseButton.setEnabled(true);
						
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
						spinner.setEnabled(true);
						manageButton.setEnabled(true);
						startButton.setEnabled(true);
						stopButton.setEnabled(false);
						pauseButton.setEnabled(false);
						startButton.setText("Start");
					}
					isRecording = false;
					break;
				case R.id.pause_button:
					if(isRecording = true){
						pauseRecording();
						startButton.setEnabled(true);
						startButton.setText("Resume");
						pauseButton.setEnabled(false);
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
		pauseButton.setOnTouchListener(listener);
		actionBar = getActionBar();
		actionBar.show();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
	    try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception ex) {
	        // Ignore
	    }
	    matchFlag = Prefs.getMatching(this);
	    
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
	
	private void pauseRecording(){
		Intent i = new Intent();
		i.putExtra("START_OR_STOP","PAUSE");
		i.setAction("RECORDING");
		sendBroadcast(i);
	}
	private void resumeRecording(){
		Intent i = new Intent();
		i.putExtra("START_OR_STOP","RESUME");
		i.setAction("RECORDING");
		sendBroadcast(i);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		startButton.setText("Start");
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
		Intent intent = new Intent();
		intent.setAction("RECORDING");
		intent.putExtra("START_OR_STOP", "CREATE");
		sendBroadcast(intent);
		i.setAction("isControl");
		sendBroadcast(i);
		String temp = Boolean.toString(Prefs.getFilter(this));
		String temp2 = Boolean.toString(Prefs.getStorage(this));
		statusView.setText("Display: "+Boolean.toString(displayOn)
				+", Storage: "+temp2+", Gravity Filter: "+temp);
		gravityView.setText("Estimated Gravity Value: Waiting");
		spinner.requestFocus();
		matchFlag = Prefs.getMatching(this);
		matchingFrequency=Prefs.getMatchingFrequency((Context)this);
		receivedData= new String[matchingFrequency];
	}
	
	@Override
	public void onPause(){
		super.onPause();
		startButton.setText("Start");
		if(isRecording){
			stopRecording();
		}
		isRecording = false;
		spinner.setEnabled(true);
		manageButton.setEnabled(true);
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
		pauseButton.setEnabled(false);
		//startButton.setFocusableInTouchMode(true);
		unregisterReceiver(receiver);
		unregisterReceiver(receiver2);
		unregisterReceiver(receiver3);
		Intent intent = new Intent();
		intent.setAction("RECORDING");
		intent.putExtra("START_OR_STOP", "FINISH");
		sendBroadcast(intent);
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
	
	public void setGravity(String data){
		if(data!=null && !data.isEmpty()){
			gravityView.setText("Estimated Gravity Value: "+data);
		}
	}
	
	private class Receiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Received!");
			if(intent.getStringExtra("G")!=null){
				setGravity(intent.getStringExtra("G"));
				return;
			}
			
			if(intent.getStringExtra("SENSOR_TYPE").equals("Accelerometer")){
				JSONObject obj;
				Double[][] d = new Double[Prefs.getMatchingFrequency((Context)PhoneUI.this)][3];
				for(int i = 0; i<Prefs.getMatchingFrequency((Context)PhoneUI.this);i++){
					try{
						obj = new JSONObject(intent.getStringExtra(Integer.toString(i)));
						receivedData[i] = "Time: "+obj.getLong("TIME")+"; x-axis: "+ String.format("%.1f", obj.getDouble("X"))
								+ "; y-axis: "+String.format("%.1f", obj.getDouble("Y"))
								+ "; z-axis: "+String.format("%.1f", obj.getDouble("Z"));
						d[i][0] = obj.getDouble("X");
						d[i][1] = obj.getDouble("Y");
						d[i][2] = obj.getDouble("Z");
					}catch(JSONException e){
						Log.d(TAG, "JSONException!");
					}
				}
				if(matchFlag){
					matchMotion(d);
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
		case R.id.phone_ui_view_button:
			Intent i = new Intent(this, ViewData.class);
			i.putExtra(Prefs.FILE_NAME, "START");
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
			break;
		case android.R.id.home:
			onBackPressed();
			break;
		default:
			break;
		}
		return true;
	}
	
	public void matchMotion(Double[][] d){
		File data = new File(getExternalFilesDir(null), "temp.csv");
		try {
			for(int i = 0; i < d.length;i++){
				if(queue.size()>30){
					queue.poll();
				}
				queue.offer(d[i]);
			}
			Double[][] dd = queue.toArray(new Double[0][]);
			Writer writer = new BufferedWriter(new FileWriter(data));
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
			writer.close();
			String s =test.match(data);
			s = s.replace(".csv", "");
			motionCurrentView.setText(s);
			motionHistoryView.append(s+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
