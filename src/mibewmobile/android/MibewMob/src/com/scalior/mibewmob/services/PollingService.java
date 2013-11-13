package com.scalior.mibewmob.services;

import java.util.HashMap;
import java.util.List;


import com.scalior.mibewmob.ChatUtils;
import com.scalior.mibewmob.MibewMobException;
import com.scalior.mibewmob.interfaces.ChatThreadListener;
import com.scalior.mibewmob.interfaces.VisitorListListener;
import com.scalior.mibewmob.model.ChatMessage;
import com.scalior.mibewmob.model.ChatThread;
import com.scalior.mibewmob.model.MonitoredSite;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.SparseArray;

public class PollingService extends Service {

	// This is the object that receives interactions from clients.  
    private final IBinder m_binder = new PollingServiceBinder();
    
    private Thread m_ThrActiveVisitors;
    private Thread m_ThrNewMessages;
	private ChatUtils m_serverUtils;
	private boolean m_pollForActiveVisitors;
	private boolean m_pollForNewMessages;
	private HashMap<VisitorListListener, String> m_visitorListListenerMap;
	private SparseArray<ChatThreadListener> m_chatThreadListenerMap;
	
	@Override
	public void onCreate() {
		m_visitorListListenerMap = new HashMap<VisitorListListener, String>();
		m_chatThreadListenerMap = new SparseArray<ChatThreadListener>();
	}
	
	
	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
	public class PollingServiceBinder extends Binder {
		public PollingService getService() {
			return PollingService.this;
		}
		
		public void subscribeToVisitorList(VisitorListListener p_listener) {
			m_visitorListListenerMap.put(p_listener, "");
		}

		public void unsubscribeToVisitorList(VisitorListListener p_listener) {
			m_visitorListListenerMap.remove(p_listener);
		}

		public void subscribeToNewMessages(ChatThread p_thread, ChatThreadListener p_listener) {
			m_chatThreadListenerMap.put(p_thread.getThreadID(), p_listener);
		}

		public void unsubscribeFromNewMessages(ChatThread p_thread, ChatThreadListener p_listener) {
			if (m_chatThreadListenerMap.get(p_thread.getThreadID()) == p_listener) {
				m_chatThreadListenerMap.remove(p_thread.getThreadID());
			}
		}
	
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (m_serverUtils == null) {
			m_serverUtils = ChatUtils.getInstance(getApplicationContext());
		}
		m_pollForActiveVisitors = true;
		m_pollForNewMessages = true;
		

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
		m_serverUtils.reloadArchivedVisitorList();
		
		if (monitoredSites == null || monitoredSites.size() == 0) {
			// There is no site to monitor, don't bother launching the thread
			m_pollForActiveVisitors = false;
			return;
		}
		
		if (m_ThrActiveVisitors == null) {
			m_ThrActiveVisitors = new Thread(new Runnable() {

				public void run() {
					try {
						while (m_pollForActiveVisitors) {
							try {
								List<ChatThread> newVisitors = m_serverUtils.checkForNewVisitors(true);

								// Call a listener that does something with the list.
								VisitorListListener[] listeners = new VisitorListListener[m_visitorListListenerMap.keySet().size()]; 
								m_visitorListListenerMap.keySet().toArray(listeners);
								for (int j = 0; j < listeners.length; j++) {
									listeners[j].onUpdateList(newVisitors);
								}
							} catch (MibewMobException e) {
								// TODO Log this to log file
								e.printStackTrace();
							}
							
							Thread.sleep(5000); // This will be configurable
						}
					} catch (InterruptedException e) {
						// TODO After exception, re-run the method again
						e.printStackTrace();
					}	
				}
			});
			
			m_ThrActiveVisitors.start();
		}
	}

	private void monitorForNewMessages() {
		if (m_serverUtils.getVisitorList().size() == 0) {
			m_pollForNewMessages = false;
		}
		
		m_pollForNewMessages = true;
		
		if(m_ThrNewMessages == null) {
			m_ThrNewMessages = new Thread(new Runnable() {

				public void run() {
					try {
						while (m_pollForNewMessages) {
							List<ChatThread> visitors = m_serverUtils.getVisitorList(); 
							try {
								for(ChatThread visitor:  visitors) {
									if (visitor.isViewed() && 
										(visitor.getState() != ChatThread.STATE_CLOSED)) {
										List<ChatMessage> messages = m_serverUtils.getNewMessagesFromServer(visitor);

										if (messages != null) {
											// Notify the listener for this thread if there is one.
											ChatThreadListener listener = m_chatThreadListenerMap.get(visitor.getThreadID());
											if (listener != null) {
												listener.onNewMessages(messages);
											}
										}
									}
								}
							} catch (MibewMobException e) {
								// TODO We have to deal with invalid opr token or invalid threadid
								e.printStackTrace();
							}

							Thread.sleep(5000); // This will be configurable
						}
					} catch (InterruptedException e) {
						// TODO After exception, re-run the method again
						e.printStackTrace();
					}	
				}
			});
			
			m_ThrNewMessages.start();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return m_binder;
	}
}
