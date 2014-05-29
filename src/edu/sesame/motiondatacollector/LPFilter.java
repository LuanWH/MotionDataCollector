package edu.sesame.motiondatacollector;

public class LPFilter {
	private float[] gravity;
	public static final float ALPHA = 0.1f;
	
	public LPFilter(){
		gravity = new float[3];
		for (int i = 0; i < 3; i ++){
			gravity[i]= 0;
		}
	}
	
	public float[] filter(float[] reading){
		float[] temp = new float[3];
		for(int i = 0; i < 3;i ++){
			gravity[i] = ALPHA * gravity[i] + (1 - ALPHA) * reading[i];
			temp[i] = reading[i] - gravity[i];
		}
		return temp;
	}
}
