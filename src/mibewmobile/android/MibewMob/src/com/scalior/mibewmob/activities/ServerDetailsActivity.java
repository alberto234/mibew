package com.scalior.mibewmob.activities;

import org.json.JSONObject;

import com.scalior.mibewmob.R;
import com.scalior.mibewmob.fragments.ServerDetailsFragment;
import com.scalior.mibewmob.fragments.ServerDetailsFragment.OnConfirmListener;
import com.scalior.mibewmob.fragments.ValidateServerFragment;
import com.scalior.mibewmob.fragments.ValidateServerFragment.OnValidateListener;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

public class ServerDetailsActivity extends FragmentActivity
									implements OnValidateListener,
											   OnConfirmListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_details);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Activate the fragment to collect the new server details
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(R.id.server_activity_fragment, new ValidateServerFragment());
		ft.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.server_details, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onValidate(JSONObject[] p_serverDetails) {
		// If valid server data was collected, proceed to the 
		// server details fragment to confirm
		if (p_serverDetails != null &&
			p_serverDetails[0] != null &&
			p_serverDetails[1] != null) {
			
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ServerDetailsFragment serverDetailsFragment = new ServerDetailsFragment();
			Bundle bundle = new Bundle();
			bundle.putString("serverDetails", p_serverDetails[0].toString());
			bundle.putString("operatorDetails", p_serverDetails[1].toString());
			serverDetailsFragment.setArguments(bundle);
			
			ft.replace(R.id.server_activity_fragment, serverDetailsFragment);
			ft.addToBackStack(null);
			ft.commit();
		}
	}

	@Override
	public void onConfirm() {
		NavUtils.navigateUpFromSameTask(this);
	}
}
