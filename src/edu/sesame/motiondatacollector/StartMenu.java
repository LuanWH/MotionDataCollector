package edu.sesame.motiondatacollector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StartMenu extends Activity {
	Button startButton, settingsButton, aboutButton,viewButton;
	
	class Listener implements OnClickListener{
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.enter_recording_button:
				startService(new Intent(StartMenu.this, HelloSensorsExtensionService.class));
				startActivity(new Intent(StartMenu.this, PhoneUI.class));
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
			case R.id.view_data_button:
				startActivity(new Intent(StartMenu.this, ViewData.class));
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
		viewButton = (Button) findViewById(R.id.view_data_button);
		OnClickListener listener = new Listener();
		startButton.setOnClickListener(listener);
		viewButton.setOnClickListener(listener);
		settingsButton.setOnClickListener(listener);
		aboutButton.setOnClickListener(listener);
	}


}
