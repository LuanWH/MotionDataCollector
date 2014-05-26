package com.example.sonymobile.smartextension.hellosensors;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StartMenu extends Activity {
	Button startButton, settingsButton, aboutButton;
	
	class Listener implements OnClickListener{
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.enter_recording_button:
				if(Prefs.getDisplay(StartMenu.this)){
					new AlertDialog.Builder(StartMenu.this)
						.setMessage("Turning on 'Display' may cause application hanging!")
						.setTitle("Warning")
						.setPositiveButton("Start", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								startActivity(new Intent(StartMenu.this, PhoneUI.class));
							}
						})
						.setNegativeButton("Settings", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								startActivity(new Intent(StartMenu.this, Prefs.class));
							}
						})
						.setCancelable(true)
						.show();
				} else {
					startActivity(new Intent(StartMenu.this, PhoneUI.class));
				}
				break;
			case R.id.settings_button:
				startActivity(new Intent(StartMenu.this, Prefs.class));
				break;
			case R.id.about_button:
				new AlertDialog.Builder(StartMenu.this)
					.setTitle("About")
					.setMessage("This is an application designed for SeSaMe Centre to collect accelerometer data from Sony Smartwatch 2")
					.setPositiveButton("Ok", null)
					.show();
				break;
			default:
				break;
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_menu_layout);
		initializeButtons();
	}
	
	private void initializeButtons(){
		startButton = (Button)findViewById(R.id.enter_recording_button);
		settingsButton = (Button)findViewById(R.id.settings_button);
		aboutButton = (Button)findViewById(R.id.about_button);
		OnClickListener listener = new Listener();
		startButton.setOnClickListener(listener);
		settingsButton.setOnClickListener(listener);
		aboutButton.setOnClickListener(listener);
	}


}
