package com.scalior.mibewmob.fragments;

import com.scalior.mibewmob.services.PollingService;
import com.scalior.mibewmob.services.PollingService.PollingServiceBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.widget.Toast;


/**
 * Description:
 * 			This provides a Fragment that is already bound to the Chat service.
 * 			All fragments that interact with the chat service should extend this fragment
 * @author Eyong Nsoesie
 *
 */
public class FragmentWithChatService extends Fragment {
	protected PollingServiceBinder m_chatServiceBinder; 
	private boolean m_isBound;
	private ServiceConnection m_connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			m_chatServiceBinder = (PollingServiceBinder)service;
			Toast.makeText(getActivity(), "Service connected", Toast.LENGTH_SHORT).show();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			Toast.makeText(getActivity(), "Service disconnected", Toast.LENGTH_SHORT).show();
			m_chatServiceBinder = null;
		}
	};
	
	private void doBindService() {
		getActivity().getApplicationContext().bindService(
				new Intent(getActivity().getApplicationContext(), PollingService.class),
				m_connection, Context.BIND_AUTO_CREATE);
		Toast.makeText(getActivity(), "Service bound", Toast.LENGTH_SHORT).show();

		m_isBound = true;
	}
	
	private void doUnbindService() {
		if (m_isBound) {
			getActivity().getApplicationContext().unbindService(m_connection);
			Toast.makeText(getActivity(), "Service unbound", Toast.LENGTH_SHORT).show();
			m_isBound = false;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		doBindService();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		doUnbindService();
	}

}
