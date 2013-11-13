package com.scalior.mibewmob.activities;

import com.scalior.mibewmob.services.PollingService;
import com.scalior.mibewmob.services.PollingService.PollingServiceBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;


/**
 * Description:
 * 			This provides a FragmentActivity that is already bound to the Chat service.
 * 			All activities that interact with the chat service should extend this fragment
 * @author Eyong Nsoesie
 *
 */
public class FragmentActivityWithChatService extends FragmentActivity {
	protected PollingServiceBinder m_chatServiceBinder; 
	private boolean m_isBound;
	private ServiceConnection m_connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			m_chatServiceBinder = (PollingServiceBinder)service;
			Toast.makeText(FragmentActivityWithChatService.this, "Service connected", Toast.LENGTH_SHORT).show();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			Toast.makeText(FragmentActivityWithChatService.this, "Service disconnected", Toast.LENGTH_SHORT).show();
			m_chatServiceBinder = null;
		}
	};
	
	private void doBindService() {
		getApplicationContext().bindService(
				new Intent(getApplicationContext(), PollingService.class),
				m_connection, Context.BIND_AUTO_CREATE);
		Toast.makeText(FragmentActivityWithChatService.this, "Service bound", Toast.LENGTH_SHORT).show();

		m_isBound = true;
	}
	
	private void doUnbindService() {
		if (m_isBound) {
			getApplicationContext().unbindService(m_connection);
			Toast.makeText(FragmentActivityWithChatService.this, "Service unbound", Toast.LENGTH_SHORT).show();
			m_isBound = false;
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		doBindService();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		doUnbindService();
	}

}
