package com.scalior.mibewmob.fragments;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import com.scalior.mibewmob.ChatServerUtils;
import com.scalior.mibewmob.R;
import com.scalior.mibewmob.activities.OperatorLoginActivity.UserLoginTask;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ValidateServerFragment extends Fragment
	implements OnClickListener {

	// UI references
	private View m_rootView;
	private EditText m_chatURLView;
	private EditText m_usernameView;
	private EditText m_passwordView;
	private View m_loginFormView;
	private View m_loginStatusView;
	private TextView m_loginStatusMessageView;
	private Button m_validateBtn;

	// Values at the time of the login attempt.
	private String m_username;
	private String m_password;
	private String m_chatURL;

	// Other data members
	private OnValidateListener m_listener;
	private ValidateServerTask m_validateTask;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		m_rootView = inflater.inflate(R.layout.fragment_validateserver,
				container, false);
	
		m_chatURLView = (EditText) m_rootView.findViewById(R.id.chatServerURL);
		m_usernameView = (EditText) m_rootView.findViewById(R.id.username);
		m_passwordView = (EditText) m_rootView.findViewById(R.id.password);
		m_loginFormView = m_rootView.findViewById(R.id.login_form);
		m_loginStatusView = m_rootView.findViewById(R.id.login_status);
		m_loginStatusMessageView = (TextView) m_rootView.findViewById(R.id.login_status_message);

		m_validateBtn = (Button) m_rootView.findViewById(R.id.serverValidate);
		m_validateBtn.setOnClickListener(this);

		return m_rootView;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			m_listener = (OnValidateListener) activity;
		}
		catch(ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnValidateListener");
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Put the focus on the EditText view
		m_chatURLView.requestFocus();
		m_validateBtn.setEnabled(true);
	}

	
	public void validateServer() {
		// Reset errors.
		m_usernameView.setError(null);
		m_passwordView.setError(null);
		m_chatURLView.setError(null);
		
		// Store values at the time of the login attempt.
		m_username = m_usernameView.getText().toString();
		m_password = m_passwordView.getText().toString();
		m_chatURL = m_chatURLView.getText().toString();
		
		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(m_password)) {
			m_passwordView.setError(getString(R.string.error_field_required));
			focusView = m_passwordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(m_username)) {
			m_usernameView.setError(getString(R.string.error_field_required));
			focusView = m_usernameView;
			cancel = true;
		}

		// Check for a valid chat server URL.
		if (TextUtils.isEmpty(m_chatURL)) {
			m_chatURLView.setError(getString(R.string.error_field_required));
			focusView = m_chatURLView;
			cancel = true;
		}
		else {
			// Make sure that a valid URL was entered.
			try {
				// URI is used only for validation
				@SuppressWarnings("unused")
				URI uri = new URI(m_chatURL);
			} catch (URISyntaxException e) {
				m_chatURLView.setError(getString(R.string.error_field_required));
				focusView = m_chatURLView;
				cancel = true;
			}
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			m_loginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			m_validateTask = new ValidateServerTask(getActivity());
			m_validateTask.execute(m_chatURL, m_username, m_password);
		}
	}
	
	/**
	 * 
	 * @param p_jDetails - this is an array of JSON objects. 
	 * 						The first element holds the server details
	 * 						The second element holds the operator details
	 */
	private void success(JSONObject[] p_jDetails) {
		try {
			String version = p_jDetails[0].getString("version");

			Toast toast = Toast.makeText(getActivity(), 
					"The server successfully validated. Version: " + version,
					Toast.LENGTH_SHORT);
			toast.show();
			
			// Need to proceed to the confirmation fragment
			m_listener.onValidate(p_jDetails);

		} catch (JSONException e) {
			failure();
		}
	}
	
	private void failure() {
		Toast toast = Toast.makeText(getActivity(),
				"The server could not be validated",
				Toast.LENGTH_LONG);
		toast.show();
		
		showProgress(false);
	}
	
	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			m_loginStatusView.setVisibility(View.VISIBLE);
			m_loginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							m_loginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			m_loginFormView.setVisibility(View.VISIBLE);
			m_loginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							m_loginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			m_loginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			m_loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	// The activity needs to implement this interface to receive the 
	// result of the server validation
	public interface OnValidateListener {
		public void onValidate(JSONObject[] p_serverDetails);
	}
	
	class ValidateServerTask extends AsyncTask<String, String, Boolean> {
		private JSONObject[] m_jResultObj = null;
		private Context m_context = null;
		
		public ValidateServerTask(Context p_context) {
			m_context = p_context;
		}

		@Override
		protected Boolean doInBackground(String... p_params) {
			// There should be three parameters
			int count = p_params.length;
			if (count != 3) {
				return false;
			}
			
			String chatURL = p_params[0];
			String username = p_params[1];
			String password = p_params[2];
			JSONObject jServerDetails = null;
			JSONObject jOperatorDetails = null;
			
			ChatServerUtils serverUtils = ChatServerUtils.getInstance(m_context);
			jServerDetails = serverUtils.validateServer(chatURL);
			
			if (jServerDetails != null) {
				try {
					if (jServerDetails.getInt("errorCode") == 0) { // Magic number!!!
						jOperatorDetails = serverUtils.loginOperator(
								jServerDetails.getString("webServiceURL"), username, password);
					}
					
					// The first element of this array holds the server details
					// while the second element holds the operator details
					m_jResultObj = new JSONObject[2];
					m_jResultObj[0] = jServerDetails;
					m_jResultObj[1] = jOperatorDetails;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				return true;
			}
			else {
				return false;
			}
			
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				success(m_jResultObj);
			}
			else {
				failure();
			}
		}
		
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.serverValidate:
			validateServer();
			break;
		default:
			break;
		}
		
	}
}
