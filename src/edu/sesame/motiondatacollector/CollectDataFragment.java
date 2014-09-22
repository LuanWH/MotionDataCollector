package edu.sesame.motiondatacollector;

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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CollectDataFragment extends Fragment {
	TextView statusView, gravityView,accView;
//	Receiver receiver;
	Receiver2 receiver2;
	Receiver3 receiver3;
	Spinner spinner;
	int matchingFrequency;
	Button startButton,stopButton,manageButton,pauseButton;
	String[] receivedData;
	public static final String TAG = "CollectDataFragment";
	Calendar c = null;
	SimpleDateFormat sdf = new SimpleDateFormat("dd/MMMM/yyyy HH:mm:ss a");
	ArrayAdapter<String> aa;
	ArrayList<String> list;
	Boolean isControl = false;
	Boolean isRecording = false;
	View view;
	CollectorHolder activity;

	/**
	 * initialize the view when onCreateView. 
	 * all the other initialization manipulations can only be done 
	 * after creating the view.
	 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.collect_data_fragment_layout, container, false);
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
    	startButton = (Button) view.findViewById(R.id.start_button);
		stopButton = (Button) view.findViewById(R.id.stop_button);
		stopButton.setEnabled(false);
		manageButton = (Button) view.findViewById(R.id.manage_action_button);
		statusView = (TextView) view.findViewById(R.id.status_view);
		gravityView = (TextView) view.findViewById(R.id.gravity_view);
		accView = (TextView)view.findViewById(R.id.accelerometer_view);
		pauseButton = (Button) view.findViewById(R.id.pause_button);
		pauseButton.setEnabled(false);
//		receiver = new Receiver();
		receiver2 = new Receiver2();
		receiver3 = new Receiver3();
		Button.OnTouchListener listener = new Button.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(v.getId()){
				case R.id.start_button:
					Intent i = new Intent();
					i.setAction("isControl");
					activity.sendBroadcast(i);

					if(activity.mBound && activity.mService.control!=null){
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
						Toast.makeText(activity, 
								"Record aborted: Please open Data Collector on your connected device.", 
								Toast.LENGTH_SHORT).show();
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
					startActivity(new Intent(activity, ManageAction.class));
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
    }
    
    @Override
    public void onResume(){
    	super.onResume();
		startButton.setText("Start");
//		activity.registerReceiver(receiver, new IntentFilter("DATA"));
		activity.registerReceiver(receiver2, new IntentFilter("DESTROY"));
		activity.registerReceiver(receiver3, new IntentFilter("CONTROL"));
		spinner = (Spinner) view.findViewById(R.id.select_action);
		if(Prefs.getDefaults(ManageAction.KEY, activity) == null){
			list = new ArrayList<String>(Arrays.asList(ManageAction.defaultList));
		} else {
			list = new ArrayList<String>(Prefs.getDefaults(ManageAction.KEY, activity));
		}
		aa = new ArrayAdapter<String>(activity, R.layout.list, list);
		spinner.setAdapter(aa);
		spinner.refreshDrawableState();
		Intent i = new Intent();
		Intent intent = new Intent();
		intent.setAction("RECORDING");
		intent.putExtra("START_OR_STOP", "CREATE");
		activity.sendBroadcast(intent);
		i.setAction("isControl");
		activity.sendBroadcast(i);
		String temp = Boolean.toString(Prefs.getFilter(activity));
		String temp2 = Boolean.toString(Prefs.getStorage(activity));
		statusView.setText("Storage: "+temp2+", Gravity Filter: "+temp);
		gravityView.setText("Estimated Gravity Value: Waiting");
		spinner.requestFocus();
		matchingFrequency=Prefs.getMatchingFrequency(activity);
		receivedData= new String[matchingFrequency];
		gravityView.setText("Estimated Gravity Value: Waiting");
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
	}
	
	private void startRecording(String item){
		Intent i = new Intent();
		i.putExtra("ACTION", item);
		i.putExtra("START_OR_STOP", "START");
		i.putExtra("GINTENT", true);
		i.setAction("RECORDING");
		activity.sendBroadcast(i);
		activity.disableViewPager();
	}
	
	private void pauseRecording(){
		Intent i = new Intent();
		i.putExtra("START_OR_STOP","PAUSE");
		i.setAction("RECORDING");
		activity.sendBroadcast(i);
		activity.disableViewPager();
	}
	private void resumeRecording(){
		Intent i = new Intent();
		i.putExtra("START_OR_STOP","RESUME");
		i.setAction("RECORDING");
		activity.sendBroadcast(i);
		activity.disableViewPager();
	}
	public void setAcc(){
		c = Calendar.getInstance();
		accView.append("\n" + "Received! "+sdf.format(c.getTime()));
	}
	
	public void setGravity(String data){
		if(data!=null && !data.isEmpty()){
			gravityView.setText("Estimated Gravity Value: "+data);
		}
	}
    /*
	private class Receiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Received!");
			if(isRecording){
				if(intent.getStringExtra("G")!=null){
					setGravity(intent.getStringExtra("G"));
				} else if(intent.getBooleanExtra("COUNT", false)){
					c = Calendar.getInstance();
					setAcc("Received! "+sdf.format(c.getTime()));
				}
			}
		}
	}*/
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
