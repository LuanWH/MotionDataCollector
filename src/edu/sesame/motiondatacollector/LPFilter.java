package edu.sesame.motiondatacollector;

import android.content.Context;

public class LPFilter {
	public float[] gravity;
	private float alpha;
	
	public LPFilter(Context context){
		this.alpha = Prefs.getParameter(context);
		gravity = new float[3];
		for (int i = 0; i < 3; i ++){
			gravity[i]= 0;
		}
	}
	
	public float[] filter(float[] reading){
		float[] temp = new float[3];
		for(int i = 0; i < 3;i ++){
			gravity[i] = alpha * gravity[i] + (1 - alpha) * reading[i];
			temp[i] = reading[i] - gravity[i];
		}
		return temp;
	}
}
