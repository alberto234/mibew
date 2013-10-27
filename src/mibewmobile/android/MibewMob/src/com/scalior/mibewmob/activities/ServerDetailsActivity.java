package com.scalior.mibewmob.activities;

import com.scalior.mibewmob.R;
import com.scalior.mibewmob.fragments.ServerDetailsFragment;
import com.scalior.mibewmob.fragments.ServerDetailsFragment.OnConfirmListener;
import com.scalior.mibewmob.fragments.AddServerFragment;
import com.scalior.mibewmob.fragments.AddServerFragment.OnAddServerListener;
import com.scalior.mibewmob.model.MonitoredSite;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

public class ServerDetailsActivity extends FragmentActivity
									implements OnAddServerListener,
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
		ft.add(R.id.server_activity_fragment, new AddServerFragment());
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
	public void onAddServer(MonitoredSite p_addedSite) {
		// If valid server data was collected, proceed to the 
		// server details fragment to confirm
		if (p_addedSite != null) {
			
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ServerDetailsFragment serverDetailsFragment = new ServerDetailsFragment();
			Bundle bundle = new Bundle();
			bundle.putLong("serverID", p_addedSite.getServer().getID());
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
