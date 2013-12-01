package com.scalior.mibewmob;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.Menu;

public class CrashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crash);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.crash, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		
		// Show dialog box
		new AlertDialog.Builder(this)
		.setMessage(R.string.unhandled_exception)
		.setCancelable(false)
		.setPositiveButton("OK", new OnClickListener () {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();

				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(10);
			}
		})
		.show();
	}
}
