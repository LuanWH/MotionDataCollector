package edu.sesame.motiondatacollector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class RecordPatternFragment extends Fragment {

	Receiver receiver;
	Receiver2 receiver2;
	Receiver3 receiver3;
	Spinner lengthSpinner, delaySpinner;
	TextView infoView, countDownView;
	EditText nameInput;
	Button startButton,stopButton;
	public static final String TAG = "RecordPatternFragment";
	Calendar c = null;
	SimpleDateFormat sdf = new SimpleDateFormat("dd/MMMM/yyyy HH:mm:ss a");
	Boolean isControl = false;
	Boolean isRecording = false;
	View view;
	CollectorHolder activity;	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.record_pattern_fragment_layout, container, false);
        view = rootView;
        return rootView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
    	super.onActivityCreated(savedInstanceState);
    	activity = (CollectorHolder)getActivity();
    	activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    	initialize();
    }
    
    public void initialize(){
    	startButton = (Button) view.findViewById(R.id.rp_start_button);
		stopButton = (Button) view.findViewById(R.id.rp_stop_button);
		stopButton.setEnabled(false);
		lengthSpinner = (Spinner) view.findViewById(R.id.record_pattern_fragment_length_spinner);
		delaySpinner = (Spinner) view.findViewById(R.id.record_pattern_fragment_delay_spinner);
		lengthSpinner.setSelection(2);
		delaySpinner.setSelection(2);
		nameInput = (EditText) view.findViewById(R.id.record_pattern_frament_filename_input);
		infoView = (TextView) view.findViewById(R.id.record_pattern_fragment_infoview);
		countDownView = (TextView) view.findViewById(R.id.rp_count_down_view);
		receiver = new Receiver();
		receiver2 = new Receiver2();
		receiver3 = new Receiver3();
		Button.OnTouchListener listener = new Button.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(v.getId()){
				case R.id.rp_start_button:
					Intent i = new Intent();
					i.setAction("isControl");
					activity.sendBroadcast(i);
					if(activity.mBound&&activity.mService.control!=null){
						if(nameInput.getText()!=null & !nameInput.getText().toString().isEmpty()){
							startRecording();
							disableItems();
							isRecording = true;
						} else {
							Toast.makeText(activity, 
									"Record aborted: Please enter a motion pattern name", 
									Toast.LENGTH_SHORT).show();
							isRecording = false;							
						}
					} else {
						Toast.makeText(activity, 
								"Record aborted: Please open Data Collector on your connected device.", 
								Toast.LENGTH_SHORT).show();
						isRecording = false;
					}
					break;
				case R.id.rp_stop_button:
					if(isRecording = true){
						stopRecording();
						enableItems();
					}
					isRecording = false;
					break;
				default:
					break;
				}
				return true;

			}
		};
		stopButton.setOnTouchListener(listener);
		startButton.setOnTouchListener(listener);
    }
    
    public void disableItems(){
    	startButton.setEnabled(false);
    	stopButton.setEnabled(true);
    	lengthSpinner.setEnabled(false);
    	delaySpinner.setEnabled(false);
    	nameInput.setEnabled(false);
    }
    public void enableItems(){
    	startButton.setEnabled(true);
    	stopButton.setEnabled(false);
    	lengthSpinner.setEnabled(true);
    	delaySpinner.setEnabled(true);
    	nameInput.setEnabled(true);
    }   
    @Override
    public void onResume(){
    	super.onResume();
		activity.registerReceiver(receiver, new IntentFilter("DATA"));
		activity.registerReceiver(receiver2, new IntentFilter("DESTROY"));
		activity.registerReceiver(receiver3, new IntentFilter("CONTROL"));
		Intent i = new Intent();
		Intent intent = new Intent();
		intent.setAction("RECORDING");
		intent.putExtra("START_OR_STOP", "CREATE");
		activity.sendBroadcast(intent);
		i.setAction("isControl");
		activity.sendBroadcast(i);
    }
    
    @Override
	public void onPause(){
		super.onPause();
		if(isRecording){
			stopRecording();
		}
		isRecording = false;
		enableItems();
		activity.unregisterReceiver(receiver);
		activity.unregisterReceiver(receiver2);
		activity.unregisterReceiver(receiver3);
		Intent intent = new Intent();
		intent.setAction("RECORDING");
		intent.putExtra("START_OR_STOP", "FINISH");
		activity.sendBroadcast(intent);
	}   
	private void stopRecording(){
		if(isRecording){
			Intent i = new Intent();
			i.putExtra("START_OR_STOP","STOP");
			i.setAction("RECORDING");
			activity.sendBroadcast(i);
			setInfo("Stop Recording");
			activity.enableViewPager();
		}
	}
	
	private void startRecording(){
		Intent i = new Intent();
		i.putExtra("START_OR_STOP", "START");
		i.putExtra("PATTERN", true);
		i.putExtra("LENGTH", String.valueOf(lengthSpinner.getSelectedItem()).substring(0,2));
		Log.d("RPF", String.valueOf(lengthSpinner.getSelectedItem()));
		i.putExtra("DELAY", delaySpinner.getSelectedItem().toString().substring(0,1));
		Log.d("RPF", delaySpinner.getSelectedItem().toString());
		i.putExtra("NAME", nameInput.getText().toString());
		i.setAction("RECORDING");
		activity.sendBroadcast(i);
		infoView.setText("");
		setInfo("Start Recording");
		Timer timer = new Timer();
		final int count = Integer.valueOf(delaySpinner.getSelectedItem().toString().substring(0,1));
		countDownView.setVisibility(View.VISIBLE);
		for(int j = 0; j < count; j++){
			final int temp = j;
			timer.schedule(new TimerTask(){

				@Override
				public void run() {			
					activity.runOnUiThread(new Runnable(){
						@Override
						public void run() {
							countDownView.setText(String.valueOf(count - temp));
						}	
					});
				}
			}, j*950);
		}
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						countDownView.setVisibility(View.GONE);
						
					}
				});				
				
			}
			
		}, count*950 - 400);		
		activity.disableViewPager();
	}    
	
	protected void setInfo(String s){
		c = Calendar.getInstance();
		infoView.append(sdf.format(c.getTime())+" "+s+"\n");
	}
	
	private class Receiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Received!");
			if(isRecording){
//				if(intent.getBooleanExtra("PATTERN", false)){
//					setInfo("Record the #"+intent.getIntExtra("PATTERN_COUNT", 0)+" value");	
//				} else
				if(intent.getBooleanExtra("PATTERN_STOP", false)){
					if(isRecording = true){
						stopRecording();
						enableItems();
					}
					isRecording = false;
				}
			}
		}
	}
	
	public void updatePatternCount(int count){
		setInfo("Record the #"+count+" value");
	}
	
	private class Receiver2 extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(isRecording){
				new AlertDialog.Builder(activity)
				.setTitle("Warning")
				.setMessage("Data Collector on smart watch has stopped!")
				.setPositiveButton("Ok", null)
				.show();				
			}
			stopRecording();
			isControl = false;
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
}
