package com.neteasemusiccontrol;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.view.Window;

public class Splash extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_splash);
		
		new Handler().postDelayed(new Runnable(){
			public void run(){
				Intent mainIntent = new Intent(Splash.this, MainActivity.class);
				startActivity(mainIntent);
				finish();
			}
		}, 2900);
	}

	
}
