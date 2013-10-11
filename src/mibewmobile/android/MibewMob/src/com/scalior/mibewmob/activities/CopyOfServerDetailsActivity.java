package com.scalior.mibewmob.activities;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import com.scalior.mibewmob.ChatServerUtils;
import com.scalior.mibewmob.R;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CopyOfServerDetailsActivity extends FragmentActivity {
	EditText m_chatURLView;
	Button m_validateBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_details);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		m_chatURLView = (EditText) findViewById(R.id.chatServerURL);
		m_validateBtn = (Button) findViewById(R.id.serverValidate);
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
	
	public void validateServer(View p_button) {
		// Call the web APIs to validate
		// Show a toast for success or failure

		String mobileChatURL = m_chatURLView.getText().toString();
		
		// Make sure that a URL was entered.
		try {
			// URI is used only for validation
			@SuppressWarnings("unused")
			URI uri = new URI(mobileChatURL);
			
			// Disable the validate button
			p_button.setEnabled(false);
			ValidateServer validateTask = new ValidateServer(this);
			validateTask.execute(mobileChatURL);
		} catch (URISyntaxException e) {
			Toast toast = Toast.makeText(this, 
										"Invalid chat server URL: " + mobileChatURL,
										Toast.LENGTH_LONG);
			toast.show();
			
			// Put the focus on the EditText view
			m_chatURLView.requestFocus();
		}
	}
	
	private void success(JSONObject p_jObject) {
		try {
			String version = p_jObject.getString("version");

			Toast toast = Toast.makeText(this, 
					"The server successfully validated. Version: " + version,
					Toast.LENGTH_LONG);
			toast.show();

		} catch (JSONException e) {
			failure();
		}
	}
	
	private void failure() {
		Toast toast = Toast.makeText(this, "The server could not be validated",
				Toast.LENGTH_LONG);
		toast.show();
		
		// Put the focus on the EditText view
		m_chatURLView.requestFocus();
		m_validateBtn.setEnabled(true);
	}
	
	class ValidateServer extends AsyncTask<String, String, Boolean> {
		private JSONObject m_jResultObj = null;
		private Context m_context = null;
		
		public ValidateServer(Context p_context) {
			m_context = p_context;
		}

		@Override
		protected Boolean doInBackground(String... p_urls) {
			// There should only be one URL
			int count = p_urls.length;
			if (count != 1) {
				return false;
			}
			
			ChatServerUtils serverUtils = ChatServerUtils.getInstance(m_context);
			m_jResultObj = serverUtils.validateServer(p_urls[0]);
			
			if (m_jResultObj != null) {
				return true;
			}
			else {
				return false;
			}
			
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				// Show server details. Make other field visible, then display them
				success(m_jResultObj);
			}
			else {
				failure();
			}
		}
		
	}
}
