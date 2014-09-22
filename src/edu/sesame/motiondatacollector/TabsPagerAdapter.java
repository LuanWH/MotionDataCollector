package edu.sesame.motiondatacollector;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
 
public class TabsPagerAdapter extends FragmentPagerAdapter {
	CollectDataFragment cdf;
	DetectMotionFragment dmf;
	RecordPatternFragment rpf;
	
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int index) {
    	Log.d("TabsPagerAdapter", "getItem "+index);
    	switch (index) {
    	case 0:
    		cdf =new CollectDataFragment();
    		return cdf;
    	case 1:
    		dmf =new DetectMotionFragment();
    		return dmf;
    	case 2:
    		rpf =new RecordPatternFragment();
    		return rpf;
    	default:
    		break;
    	}

        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }
    
    public Fragment[] getFragments(){
    	Fragment[] h = {cdf, dmf, rpf};
    	return h;
    }

    
    public static CollectDataFragment transformCDF(Fragment f){
    	return (CollectDataFragment)f;
    }
    
    public static RecordPatternFragment transformRDF(Fragment f){
    	return (RecordPatternFragment)f;
    }
    public static DetectMotionFragment transformDMF(Fragment f){
    	return (DetectMotionFragment)f;
    }
}