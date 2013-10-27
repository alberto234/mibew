package com.scalior.mibewmob.services;

import java.util.List;


import com.scalior.mibewmob.ChatUtils;
import com.scalior.mibewmob.MibewMobException;
import com.scalior.mibewmob.interfaces.LoginListener;
//import com.scalior.mibewmob.model.ChatThread;
import com.scalior.mibewmob.model.MonitoredSite;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

public class ChatService extends Service {
	
	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
	public class ChatServiceBinder extends Binder {
		LoginListener m_listener; 
		public ChatService getService() {
			return ChatService.this;
		}
		
		/**
		 * Login to the chat server. Results are returned through the listener if provided
		 * @param p_url
		 * @param p_username
		 * @param p_password
		 * @param p_listener
		 */
		public void LoginToChatServer(String p_url, String p_username,
									  String p_password, LoginListener p_listener) {
			m_listener = p_listener;
			
			if (p_url == null ||
				p_username == null ||
				p_password == null) {
				if (p_listener != null) {
					p_listener.OnLoginComplete(LoginListener.STATUS_INVALID_PARAMETERS, -1);
					// TODO: We can check for other invalid conditions like empty strings
				}
			}

			// Create the AsyncTask to login.
			LogonTask loginTask = new LogonTask(ChatService.this);
			loginTask.execute(p_url, p_username, p_password);
			
			return;
		}
		
		
		/**
		 * @param p_status		- login status code
		 * @param p_jDetails 	- this is an array of JSON objects. 
		 * 						  The first element holds the server details
		 * 						  The second element holds the operator details
		 */
		private void success(int p_status, MonitoredSite p_site) {
			m_listener.OnLoginComplete(p_status, p_site);
		}
		
		private void failure(int p_status) {
			if (m_listener != null) {
				m_listener.OnLoginComplete(p_status, -1);
			}
		}
		
		// Task to perform the login in the background
		class LogonTask extends AsyncTask<String, String, Boolean> {
			private MonitoredSite m_addedSite;
			private Context m_context = null;
			private int m_loginResultStatus = LoginListener.STATUS_SUCCESS;
			
			public LogonTask(Context p_context) {
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
				
				try {
					ChatUtils serverUtils = ChatUtils.getInstance(m_context);
					m_addedSite = serverUtils.addSiteToMonitor(chatURL, username, password);
					return true;
				} catch (MibewMobException e) {
					// TODO: We can add a log statement here.
					// Determine if it is an invalid site error or invalid username/password
					if (e.getErrorCode() == ChatUtils.SERVER_ERROR_LOGIN_FAILED) {
						m_loginResultStatus = LoginListener.STATUS_INVALID_CREDENTIALS;
					}
					else {
						m_loginResultStatus = LoginListener.STATUS_INVALID_SERVER;
					}
					return false;
				}
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					success(m_loginResultStatus, m_addedSite);
				}
				else {
					failure(m_loginResultStatus);
				}
			}
			
		}		
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (m_serverUtils == null) {
			m_serverUtils = ChatUtils.getInstance(getApplicationContext());
		}
		m_pollForActiveVisitors = true;
		//m_pollForNewMessages = true;
		

		// Using two separate threads to have two separate timers for each action.
		
		// TODO: When I learn more, this will actually happen on the accounts and sync 
		//		section
		// Launch thread to check for active visitors
		monitorForActiveVisitors();
		
		// Launch thread to check for new messages
		monitorForNewMessages();
		

		// We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
	}
	
	private void monitorForActiveVisitors() {
		final List<MonitoredSite> monitoredSites = m_serverUtils.reloadMonitoredSitesList();
		m_serverUtils.loadArchivedVisitorList();
		
		if (monitoredSites == null || monitoredSites.size() == 0) {
			// There is no site to monitor, don't bother launching the thread
			m_pollForActiveVisitors = false;
			return;
		}
		
		if (m_ThrActiveVisitors == null) {
			m_ThrActiveVisitors = new Thread(new Runnable() {

				public void run() {
					boolean bNewVisitor = false;
					try {
						while (m_pollForActiveVisitors) {
							for (int i = 0; i < monitoredSites.size(); i++) {
								//List<ChatThread> visitors = 
										m_serverUtils.getActiveVisitors(monitoredSites.get(i).getServer(),
												monitoredSites.get(i).getOperator().getToken());
								
								// Call a listener that does something with the list.
							}
							Thread.sleep(5000); // This will be configurable
						}
					}
					catch (MibewMobException e) {
						// TODO We have to deal with invalid opr token or invalid threadid
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	// This shall be configurable
					
					if (bNewVisitor) {
						// Notify user via notification
					}
				}
			});
			
			m_ThrActiveVisitors.start();
		}
	}

	private void monitorForNewMessages() {
		m_ThrNewMessages = null;
		if(m_ThrNewMessages == null) {}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return m_binder;
	}


    // This is the object that receives interactions from clients.  
    private final IBinder m_binder = new ChatServiceBinder();
    
    private Thread m_ThrActiveVisitors;
    private Thread m_ThrNewMessages;
	private ChatUtils m_serverUtils;
	private boolean m_pollForActiveVisitors;
//	private boolean m_pollForNewMessages;
}
