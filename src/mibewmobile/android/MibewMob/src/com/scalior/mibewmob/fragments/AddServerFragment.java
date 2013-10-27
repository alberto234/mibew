package com.scalior.mibewmob.fragments;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;

import com.scalior.mibewmob.ChatUtils;
import com.scalior.mibewmob.MibewMobException;
import com.scalior.mibewmob.R;
import com.scalior.mibewmob.model.MonitoredSite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class AddServerFragment extends Fragment
	implements OnClickListener {

	private static final int COMMAND_LOGIN = 1;
	
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
	private OnAddServerListener m_listener;
	private ChatUtils m_chatUtils;
	private LoginHandler m_handler;
	private MonitoredSite m_addedSite;

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

		m_chatUtils = ChatUtils.getInstance(getActivity().getApplicationContext());
		m_handler = new LoginHandler(this);
		m_addedSite = null;
		
		// Initial data
		m_chatURLView.setText("http://nsoesie.dyndns-home.com:5242/transmawfoods/webim");

		return m_rootView;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			m_listener = (OnAddServerListener) activity;
		}
		catch(ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnAddServerListener");
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
			
			Thread loginThread = new Thread (new Runnable () {

				@Override
				public void run() {
					Message msg;
					try {
						m_addedSite = m_chatUtils.addSiteToMonitor(m_chatURL, m_username, m_password);
						msg = m_handler.obtainMessage(COMMAND_LOGIN, ChatUtils.SERVER_ERROR_SUCCESS, 0);
					} catch (MibewMobException e) {
						// TODO: We can add a log statement here.
						msg = m_handler.obtainMessage(COMMAND_LOGIN, e.getErrorCode(), 0);
					}
					m_handler.sendMessage(msg);
				}
			});
			
			loginThread.start();
		}
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
	public interface OnAddServerListener {
		public void onAddServer(MonitoredSite p_addedSite);
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

	private static class LoginHandler extends Handler {
		private WeakReference<AddServerFragment> m_fragment;

		public LoginHandler(AddServerFragment fragment) {
			super();
			m_fragment = new WeakReference<AddServerFragment>(fragment);
		}
		
		// For messages sent to this handler,
		// what is the command
		// arg1 is the error code
		// arg2 is determined by the command
		@Override
		public void handleMessage(Message msg) {
			m_fragment.get().showProgress(false);
			
			if (msg.what == COMMAND_LOGIN) {
				if (msg.arg1 == ChatUtils.SERVER_ERROR_SUCCESS) {
					// Transition to the details
					m_fragment.get().m_listener.onAddServer(m_fragment.get().m_addedSite);
				}
				else if (msg.arg1 == ChatUtils.SERVER_ERROR_LOGIN_FAILED) {
					// Invoke the error
					Toast.makeText(m_fragment.get().getActivity(), 
							"Login error. Check the username / password and try again", 
							Toast.LENGTH_LONG).show();
					
					m_fragment.get().m_usernameView.requestFocus();
				}
				else {
					// Invoke the error
					Toast.makeText(m_fragment.get().getActivity(), 
							"Error reaching the server. Check the spelling and try again", 
							Toast.LENGTH_LONG).show();
					
					m_fragment.get().m_chatURLView.requestFocus();
				}
			}
		}
	}
}
