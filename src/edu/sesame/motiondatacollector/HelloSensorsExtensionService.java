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

 * Neither the name of the Sony Ericsson Mobile Communications AB / Sony Mobile
 Communications AB nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written permission.

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

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

/**
 * The Hello Sensors Extension Service handles registration and keeps track of
 * all sensors on all accessories.
 */
public class HelloSensorsExtensionService extends ExtensionService {

    public static final int NOTIFY_STOP_ALERT = 1;

    public static final String LOG_TAG = "HelloSensors";

    public final String CLASS = getClass().getSimpleName();
    
    public String test = "Bind successful!";
    
    public CollectorHolder holder = null;
    
    public HelloSensorsControl control = null;

    Receiver receiver;
    Receiver2 receiver2;
    Timer timer;
    public HelloSensorsExtensionService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, CLASS + ": onCreate");
        receiver = new Receiver();
        registerReceiver(receiver, new IntentFilter("RECORDING"));
        registerReceiver(receiver2, new IntentFilter("isControl"));
    }
    

    @Override
    protected RegistrationInformation getRegistrationInformation() {
        return new HelloSensorsRegistrationInformation(this);
    }

    @Override
    protected boolean keepRunningWhenConnected() {
        return true;
    }

    @Override
    public ControlExtension createControlExtension(String hostAppPackageName) {
    	control = new HelloSensorsControl(hostAppPackageName, this);
		Intent i = new Intent();
		i.setAction("CONTROL");
		i.putExtra("CONTROL", "ON");
		sendBroadcast(i);
    	return control;
    }
    
    
    private class Receiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(control!=null){
				if(intent.getStringExtra("START_OR_STOP").equals("START")){
					if(intent.getBooleanExtra("PATTERN", false) && 
							intent.getStringExtra("LENGTH")!=null &&
							intent.getStringExtra("DELAY") != null &&
							intent.getStringExtra("NAME") != null){
						control.isPattern = true;
						control.patternLength = Integer.valueOf(intent.getStringExtra("LENGTH"));
						control.patternDelay = Integer.valueOf(intent.getStringExtra("DELAY"));
						control.patternName = intent.getStringExtra("NAME");
						control.action = null;
						control.register();
					}else{
						control.register();
						control.action = intent.getStringExtra("ACTION");	
						control.isPattern = false;
						if(control.action!=null && control.action.equals("MOTION")){
							control.type = intent.getIntExtra("TYPE", FastDtwTest.NORM);
						}
					}
					if(intent.getBooleanExtra("GINTENT", false)){
						 control.isGIntent = true;
					} else {
						control.isGIntent = false;
					}
				} else if(intent.getStringExtra("START_OR_STOP").equals("STOP")){
					control.unregister(false);
				} else if(intent.getStringExtra("START_OR_STOP").equals("FINISH")){
					if(timer!=null){
						timer.cancel();
						timer = null;
					}
					
				} else if(intent.getStringExtra("START_OR_STOP").equals("CREATE")){
					launchTaskTimer();
				} else if(intent.getStringExtra("START_OR_STOP").equals("RESUME")){
					control.register();
				} else if(intent.getStringExtra("START_OR_STOP").equals("PAUSE")){
					control.unregister(true);
				}
			}
		}
    	
    }
    private class Receiver2 extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Intent i = new Intent();
			i.setAction("CONTROL");
			if(control==null){
				i.putExtra("CONTROL","OFF");
			} else {
				i.putExtra("CONTROL", "ON");
			}
			sendBroadcast(i);
		}
    	
    }
    
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
		Intent i = new Intent();
		i.setAction("CONTROL");
		i.putExtra("CONTROL","OFF");
		sendBroadcast(i);
    	unregisterReceiver(receiver);
    	unregisterReceiver(receiver2);
    	control = null;
    	timer.cancel();
    	timer = null;
    }

    public void launchTaskTimer() {
        final Handler handler = new Handler();

        TimerTask timertask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {				
                        	Intent i = new Intent();
	        				i.setAction("CONTROL");
	        				if(control!=null){
	        					i.putExtra("CONTROL", "ON");
	        				} else {
	        					i.putExtra("CONTROL", "OFF");
	        				}
	        				sendBroadcast(i);
                        } catch (Exception e) {
                        }
                    }
                });
            }
        };
        timer = new Timer(); //This is new
        timer.schedule(timertask, 0, 1000); // execute in every 15sec
    }
}
