package edu.sesame.motiondatacollector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.sonyericsson.extras.liveware.extension.util.ExtensionService.LocalBinder;
import com.sonyericsson.extras.liveware.extension.util.HackBinder;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
 
public class CollectorHolder extends FragmentActivity implements
        ActionBar.TabListener {
 
    public CollectorHolderPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    private int position = 0;
    Menu menu;
    public MenuItem spinner;
    OnItemSelectedListener spinnerListener;
    Boolean enabled = true;
    public HelloSensorsExtensionService mService;
    boolean mBound = false;
    Fragment fragment = null;

    // Tab titles
    private String[] tabs = { "Data", "Motion", "Pattern" };
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collector_holder_layout);
 
        // Initialization
        viewPager = (CollectorHolderPager) findViewById(R.id.collector_holder_pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
 
        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);     
        actionBar.setDisplayHomeAsUpEnabled(true);
 
        // Adding Tabs
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }
		/**
		 * force the menu to overflow
		 */
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
	    invalidateOptionsMenu();
    }
    
    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
 
    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
    	if(enabled){
    		viewPager.setCurrentItem(tab.getPosition());
    	}
    }
 
    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
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
		getMenuInflater().inflate(R.menu.collector_holder_menu, menu);
		this.menu = menu;
		spinner = menu.findItem(R.id.collector_holder_spinner);
		Spinner mSpinner = (Spinner) spinner.getActionView();
		final List<String> list = new ArrayList<String>();
        list.add("Euclidean Distance");
        list.add("Manhattan Distance");
        list.add("Binary Distance");
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, list); 
    	mSpinner.setAdapter(dataAdapter);
    	mSpinner.setSelection(CollectorHolder.this.position);
    	spinnerListener = new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent,
					View view, int position, long id) {
				CollectorHolder.this.position = position;
				Prefs.setInteger("DISTANCE", position, CollectorHolder.this);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Prefs.setInteger("DISTANCE", 0, CollectorHolder.this);
			}
    		
    	};      	
		mSpinner.setOnItemSelectedListener(spinnerListener);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.collector_holder_open_settings:
			startActivity(new Intent(CollectorHolder.this, Prefs.class));
			break;
		case R.id.collector_holder_view_button:
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
	
	@Override
	public void onResume(){
		super.onResume();
        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
 
            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
                if(position == 1){
                	menu.findItem(R.id.collector_holder_view_button).setVisible(false);
                	menu.findItem(R.id.collector_holder_spinner).setVisible(true);
                } else {
                	menu.findItem(R.id.collector_holder_view_button).setVisible(true);
                	menu.findItem(R.id.collector_holder_spinner).setVisible(false);
                }
            }
 
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
 
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });	
	}
	public Fragment getVisibleFragment(){
	    FragmentManager fragmentManager = this.getSupportFragmentManager();
	    List<Fragment> fragments = fragmentManager.getFragments();
	    for(Fragment fragment : fragments){
	        if(fragment != null && fragment.isVisible())
	            return fragment;
	    }
	    return null;
	}
	public void disableViewPager(){
		viewPager.setPagingEnabled(false);
		enabled=false;
	}
	public void enableViewPager(){
		viewPager.setPagingEnabled(true);
		enabled=true;
	}
    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, HelloSensorsExtensionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
       
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    
    /** Defines callbacks for service binding, passed to bindService() */
    public ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = (HelloSensorsExtensionService) new HackBinder(binder).getService();
            Log.d("ColletorHolder", mService==null?"Failed to bind service!":mService.test);
            mBound = true;
            mService.holder = CollectorHolder.this;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	if(mService!=null) mService.holder = null;
            mBound = false;
            Log.d("CollectorHolder", "disconnect service");
        }
    };
    
    public void setGravity(String s){
    	Log.d("CollectorHolder","Force casting to CDF");
    	CollectDataFragment f = TabsPagerAdapter.transformCDF(mAdapter.getFragments()[0]);
    	if(f.isRecording)f.setGravity(s);
    }
    
    public void updateWritingCount(){
    	Log.d("CollectorHolder","Force casting to CDF");
    	CollectDataFragment f = TabsPagerAdapter.transformCDF(mAdapter.getFragments()[0]);
    	if(f.isRecording)f.setAcc();

    }

    public void updatePatternCount(int count){

    	Log.d("CollectorHolder","Force casting to RDF");
    	RecordPatternFragment f = TabsPagerAdapter.transformRDF(mAdapter.getFragments()[2]);
    	if(f.isRecording)f.updatePatternCount(count);

    }
    public void updateMotion(String s1, String s2){

    	Log.d("CollectorHolder","Force casting to DMF");
    	DetectMotionFragment f = TabsPagerAdapter.transformDMF(mAdapter.getFragments()[1]);
    	if(f.isRecording)f.update(s1, s2);    
    }

	
}