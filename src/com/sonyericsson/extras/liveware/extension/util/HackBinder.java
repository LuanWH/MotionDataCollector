package com.sonyericsson.extras.liveware.extension.util;

import com.sonyericsson.extras.liveware.extension.util.ExtensionService.LocalBinder;

public class HackBinder {
	LocalBinder binder;

	public HackBinder(LocalBinder binder){
		this.binder = binder;
	}
	
	public ExtensionService getService(){
		return binder.getService();
	}
}
