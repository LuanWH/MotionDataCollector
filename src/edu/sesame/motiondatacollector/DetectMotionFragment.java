package edu.sesame.motiondatacollector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DetectMotionFragment extends Fragment {
	TextView curMotionView,curMotionDetailView,hisMotionView;
	ScrollView hisMotionScroll;
	Button startButton,stopButton;
	CollectorHolder activity;
//	Receiver receiver;
	Receiver2 receiver2;
	Receiver3 receiver3;
	int matchingFrequency = 10;
	public static final String TAG = "DetectMotionFragment";
	Calendar c = null;
	SimpleDateFormat sdf = new SimpleDateFormat("dd/MMMM/yyyy HH:mm:ss a");
	StringBuilder stringBuilder;
	ArrayList<String> list;
	Boolean isControl = false;
	Boolean isRecording = false;
	View view;
	Queue<Double[]> queue;
	int testType=1;
	public static final int QUEUE_LENGTH_SHORT = 20;
	public static final int QUEUE_LENGTH_MEDIUM = 30;
	public static final int QUEUE_LENGTH_LONG = 40;
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) { 
        View rootView = inflater.inflate(R.layout.detect_motion_fragment_layout, container, false);
        view = rootView;
        return rootView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
    	super.onActivityCreated(savedInstanceState);
    	activity = (CollectorHolder)getActivity();
    	initialize();
    }
    public void initialize(){

    	startButton = (Button) view.findViewById(R.id.dm_start_button);
		stopButton = (Button) view.findViewById(R.id.dm_stop_button);
		stopButton.setEnabled(false);
		curMotionView = (TextView) view.findViewById(R.id.dm_current_motion_view);
		curMotionDetailView = (TextView) view.findViewById(R.id.dm_current_motion_detail_view);
		hisMotionView = (TextView)view.findViewById(R.id.dm_history_motion_view);
		hisMotionScroll = (ScrollView) view.findViewById(R.id.dm_history_motion_scroll);
//		receiver = new Receiver();
		receiver2 = new Receiver2();
		receiver3 = new Receiver3();
		queue = new LinkedList<Double[]>();
		Button.OnTouchListener listener = new Button.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(v.getId()){
				case R.id.dm_start_button:
					Intent i = new Intent();
					i.setAction("isControl");
					activity.sendBroadcast(i);
					startButton.setEnabled(false);
					if(activity.mBound&&activity.mService.control!=null){
						AlertDialog dialog = 
						new AlertDialog.Builder(activity)
							.setTitle("Choose an action")
							.setMessage("Which type of matching?")
							.setNegativeButton("Normalized", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									testType = FastDtwTest.NORM;
									startRecording();
									startButton.setEnabled(false);
									stopButton.setEnabled(true);
									isRecording = true;
									dialog.cancel();
								}
							})
							.setNeutralButton("Raw", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									
									testType = FastDtwTest.RAW;
									startRecording();
									startButton.setEnabled(false);
									stopButton.setEnabled(true);
									isRecording = true;
									dialog.cancel();
								}
							})
							.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							})
							.setCancelable(true)
							.create();
						dialog.show();

					} else {
						Toast.makeText(activity, 
								"Record aborted: Please open Data Collector on your connected device.", 
								Toast.LENGTH_SHORT).show();
						isRecording = false;
						startButton.setEnabled(true);
					}
					break;
				case R.id.dm_stop_button:
					if(isRecording = true){
						stopRecording();
						startButton.setEnabled(true);
						stopButton.setEnabled(false);
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

	@Override
	public void onResume(){
		super.onResume();
//		activity.registerReceiver(receiver, new IntentFilter("DATA"));
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
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
//		activity.unregisterReceiver(receiver);
		activity.unregisterReceiver(receiver2);
		activity.unregisterReceiver(receiver3);
		Intent intent = new Intent();
		intent.setAction("RECORDING");
		intent.putExtra("START_OR_STOP", "FINISH");
		activity.sendBroadcast(intent);
	}
    
	private void stopRecording(){
		Intent i = new Intent();
		i.putExtra("START_OR_STOP","STOP");
		i.setAction("RECORDING");
		activity.sendBroadcast(i);
		activity.enableViewPager();
		Toast.makeText(activity, "Total DTW calculation: "+FastDtwTest.count, Toast.LENGTH_SHORT).show();
		Log.d("DetectMotionFragment","Total DTW calculation: "+FastDtwTest.count);
	}
	
	private void startRecording(){
		stringBuilder = new StringBuilder(256);
		Intent i = new Intent();
		i.putExtra("ACTION", "MOTION");
		i.putExtra("TYPE", testType);
		i.putExtra("START_OR_STOP", "START");
		i.setAction("RECORDING");
		activity.sendBroadcast(i);
		activity.disableViewPager();
	}
    
//	private class Receiver extends BroadcastReceiver{
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if(isRecording){
//				Log.d(TAG, "Received!");
//				if(intent.getBooleanExtra("isMatch", false)){
//					curMotionView.setText(intent.getStringExtra("MATCH_FILE"));
//					curMotionDetailView.setText(intent.getStringExtra("MATCH_DETAIL"));
//					hisMotionView.append(intent.getStringExtra("MATCH_FILE")+"\n");
//					sendScroll();
//				}
//			}
//		}
//	}
//	
	public void update(String s1, String s2){
		curMotionView.setText(s1);
		curMotionDetailView.setText(s2);
		stringBuilder.append(s1+"\n");
		hisMotionView.setText(stringBuilder.toString());
		sendScroll();		
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
	
    private void sendScroll(){
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {Thread.sleep(100);} catch (InterruptedException e) {}
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        hisMotionScroll.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        }).start();
    }
    
}
