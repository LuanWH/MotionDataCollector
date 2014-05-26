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

package com.example.sonymobile.smartextension.hellosensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

/**
 * The Hello Sensors Extension Service handles registration and keeps track of
 * all sensors on all accessories.
 */
public class HelloSensorsExtensionService extends ExtensionService {

    public static final int NOTIFY_STOP_ALERT = 1;

    public static final String LOG_TAG = "HelloSensors";

    public final String CLASS = getClass().getSimpleName();
    
    
    public HelloSensorsControl control = null;

    Receiver receiver;
   // Receiver2 receiver2;
    public HelloSensorsExtensionService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, CLASS + ": onCreate");
        receiver = new Receiver();
     //   receiver2 = new Receiver2();
        registerReceiver(receiver, new IntentFilter("RECORDING"));
      //  registerReceiver(receiver2, new IntentFilter("NEXT"));
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
    	return control;
    }
    
    public class Receiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getStringExtra("START_OR_STOP").equals("START")){
				control.register();
			} else if(intent.getStringExtra("START_OR_STOP").equals("STOP")){
				control.unregister(false);
			}
		}
    	
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	unregisterReceiver(receiver);
    //	unregisterReceiver(receiver2);
    }
/*    public class Receiver2 extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			control.sendNext();
		}
    	
    }*/
    
    
//    //Start binding the service with PhoneUI
//    private final IBinder mBinder = new LocalBinder();
//    @Override
//    public IBinder onBind(Intent intent){
//    	return mBinder;
//    }
//    public class LocalBinder extends Binder {
//    	public HelloSensorsExtensionService getService(){
//    		return HelloSensorsExtensionService.this;
//    	}
//    }
}
