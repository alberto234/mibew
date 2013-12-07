package com.scalior.mibewmob.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.scalior.mibewmob.ChatAvailability;
import com.scalior.mibewmob.ChatUtils;
import com.scalior.mibewmob.MibewMobException;
import com.scalior.mibewmob.MibewMobLogger;
import com.scalior.mibewmob.R;
import com.scalior.mibewmob.activities.ChatActivity;
import com.scalior.mibewmob.activities.ChattingActivity;
import com.scalior.mibewmob.interfaces.ChatThreadListener;
import com.scalior.mibewmob.interfaces.VisitorListListener;
import com.scalior.mibewmob.model.ChatMessage;
import com.scalior.mibewmob.model.ChatThread;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.SparseArray;

public class PollingService extends Service 
				implements Handler.Callback {
	
	private static final int NOTIFICATION_NEWVISITOR = 1000;
	private static final int NOTIFICATION_NEWMESSAGE = 1001;

	// This is the object that receives interactions from clients.  
    private final IBinder m_binder = new PollingServiceBinder();
    
    private Handler m_handler;
    private SharedPreferences.OnSharedPreferenceChangeListener m_prefListener;
    
    private Runnable m_pollActiveVisitors;
    private Runnable m_pollNewMessages;
	private boolean m_bPollForActiveVisitors;
	private boolean m_bPollForNewMessages;
	private boolean m_bPollingLoop;
	private static final Object m_pollingLock = new Object();
	private PowerManager.WakeLock m_wakeLock;
	
	private ChatUtils m_serverUtils;

	private HashMap<VisitorListListener, String> m_visitorListListenerMap;
	private SparseArray<ChatThreadListener> m_chatThreadListenerMap;
	
	@Override
	public void onCreate() {
		m_serverUtils = ChatUtils.getInstance(getApplicationContext());
		m_visitorListListenerMap = new HashMap<VisitorListListener, String>();
		m_chatThreadListenerMap = new SparseArray<ChatThreadListener>();
		
		m_handler = new Handler(this);
		m_prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (key.equals("available_from") || key.equals("available_to")) {
					updatePollingState();
				}
			}
		};
		
		m_pollActiveVisitors = new Runnable() {

			@Override
			public void run() {
				if (m_bPollForActiveVisitors) {
					pollActiveVisitors();
				} else {
					// Run again after set delay
					m_handler.postDelayed(m_pollActiveVisitors, 5000);
				}
			}
		};
		
		m_pollNewMessages = new Runnable() {

			@Override
			public void run() {
				if (m_bPollForNewMessages) {
					pollNewMessages();
				} else {
					// Run again after set delay
					m_handler.postDelayed(m_pollNewMessages, 5000);
				}
			}
		};
		
		m_bPollingLoop = false;
		
		// Set the current chat availability state and 
		// register a broadcast receiver for chat availability triggers
		updatePollingState();
		
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		m_wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PollingService");
		
		MibewMobLogger.Log("Service started");
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// Register for preference change notifications
		PreferenceManager.getDefaultSharedPreferences(this)
						.registerOnSharedPreferenceChangeListener(m_prefListener);

		// TODO: When I learn more, this will actually happen on the accounts and sync 
		//		section

		// Using two separate threads to have two separate timers for each action.
		
		// The polling loop should be started just once, but onStartCommand can be called
		// multiple times by different threads. Synchronization is not an overkill here 
		// because if two threads kick off the polling loop, we will be polling twice as 
		// frequently.
		synchronized (m_pollingLock) {
			if (!m_bPollingLoop) {
				m_handler.post(m_pollActiveVisitors);
				m_handler.post(m_pollNewMessages);
				m_bPollingLoop = true;
				
				MibewMobLogger.Log("Polling started");
			}
		}
		
		// We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return m_binder;
	}

	private void pollActiveVisitors() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if (m_serverUtils.checkForNewVisitors(true)) {
						// Notify all listeners
						VisitorListListener[] listeners = new VisitorListListener[m_visitorListListenerMap.keySet().size()]; 
						m_visitorListListenerMap.keySet().toArray(listeners);
						for (int j = 0; j < listeners.length; j++) {
							listeners[j].onListUpdated();
						}
						
						// Display notifications of new visitors if no listener is registered
						if (listeners.length == 0) {
							NotificationCompat.Builder notBuilder = 
									new NotificationCompat.Builder(PollingService.this)
									.setSmallIcon(R.drawable.ic_launcher)
									.setContentTitle("New Visitor")
									.setContentText("Tap to see visitor list")
									.setDefaults(Notification.DEFAULT_ALL)
									.setAutoCancel(true);
							
							Intent visitorListIntent = new Intent(PollingService.this, ChatActivity.class);
							visitorListIntent.putExtra(ChatActivity.SHOW_VISITOR_LIST, true);
							
							// Create the back stack for the intent.
							TaskStackBuilder stackBuilder = TaskStackBuilder.create(PollingService.this);
							stackBuilder.addParentStack(ChatActivity.class);
							stackBuilder.addNextIntent(visitorListIntent);
							
							PendingIntent visitorListPI = 
									stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
							
							notBuilder.setContentIntent(visitorListPI);
							
							NotificationManager notManager = 
									(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
							notManager.notify(NOTIFICATION_NEWVISITOR, notBuilder.build());
						}
					}
				} catch (MibewMobException e) {
					MibewMobLogger.Log(e.getMessage());
				}

				// Run again after set delay
				m_handler.postDelayed(m_pollActiveVisitors, 5000);
			}
		}).start();
	}

	private void pollNewMessages() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				List<ChatThread> visitors = new ArrayList<ChatThread>();
				visitors.addAll(m_serverUtils.getVisitorList()); 

				int notificationCount = 0;
				String contentTitle = "New message from a guest";
				String contentText = "";
				ChatThread threadToExpand = null;
				
				for(ChatThread visitor:  visitors) {
					if (visitor.isViewed() && 
						(visitor.getState() != ChatThread.STATE_CLOSED)) {
						try {
							List<ChatMessage> messages = m_serverUtils.getNewMessagesFromServer(visitor);

							if (messages != null) {
								// Notify the listener for this thread if there is one.
								ChatThreadListener listener = m_chatThreadListenerMap.get(visitor.getThreadID());
								if (listener != null) {
									listener.onNewMessages(messages);
								} else {
									
									// A listener is registered only when the user is actively viewing a thread.
									// Do not post a notification then, but do so if a listener for the thread
									// is not registered.
									
									notificationCount++;
	
									if (notificationCount == 1) {
										contentText = visitor.getGuestName();
										threadToExpand = visitor;
									} else if (notificationCount == 2) {
										contentTitle = "New messages from visitors";
										contentText += ", " + visitor.getGuestName();
										threadToExpand = null;
									} else if (notificationCount == 3) {
										contentText += ",...";
									}
								}
							}
						} catch (MibewMobException e) {
							MibewMobLogger.Log(e.getMessage());
						}
					}
				}

				NotificationCompat.Builder notBuilder = null;
				Intent notClickIntent = null;
				PendingIntent notClickPI = null;
				
				if (notificationCount == 1) {
					// Show notification for 1 thread and when clicked, go straight to the thread
					notBuilder = new NotificationCompat.Builder(PollingService.this)
									.setContentTitle(contentTitle)
									.setContentText(contentText);

					notClickIntent = new Intent(PollingService.this, ChattingActivity.class);
					notClickIntent.putExtra(ChatUtils.CHAT_KEY, m_serverUtils.setThreadToExpand(threadToExpand));
					
					// Create the back stack for the intent.
					TaskStackBuilder stackBuilder = TaskStackBuilder.create(PollingService.this);
					stackBuilder.addParentStack(ChattingActivity.class);
					stackBuilder.addNextIntent(notClickIntent);
					
					notClickPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
				} else if (notificationCount > 1) {
					// Show notification for multiple threads. When clicked, go to visitor list
					notBuilder = new NotificationCompat.Builder(PollingService.this)
									.setContentTitle(contentTitle)
									.setContentText(contentText)
									.setNumber(notificationCount);

					notClickIntent = new Intent(PollingService.this, ChatActivity.class);
					notClickIntent.putExtra(ChatActivity.SHOW_VISITOR_LIST, true);
					
					// Create the back stack for the intent.
					TaskStackBuilder stackBuilder = TaskStackBuilder.create(PollingService.this);
					stackBuilder.addParentStack(ChatActivity.class);
					stackBuilder.addNextIntent(notClickIntent);
					
					notClickPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
				}
				

				if (notBuilder != null) {
					notBuilder.setSmallIcon(R.drawable.ic_launcher)
								.setDefaults(Notification.DEFAULT_ALL)
								.setAutoCancel(true)
								.setOnlyAlertOnce(true);

					notBuilder.setContentIntent(notClickPI);
					
					NotificationManager notManager = 
							(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
					notManager.notify(NOTIFICATION_NEWMESSAGE, notBuilder.build());
				}
				
				// Run again after set delay
				m_handler.postDelayed(m_pollNewMessages, 5000);
			}
		}).start();
	}
	
	
	@Override
	public boolean handleMessage(Message msg) {
		// Do nothing for now
		return true;
	}
	
	private void updatePollingState() {
		BroadcastReceiver br = new BroadcastReceiver() {

			@Override
			public void onReceive(Context p_context, Intent p_intent) {
				// TODO 
				// Although this may look like recursion, onReceive is only called
				// when the alarm manager fires the broadcast event. Once this happens
				// a new broadcast receiver is created for the next trigger time, and
				// this new broadcast receiver will then call updatePollingState when
				// triggered. We don't have an infinite loop here...
				updatePollingState();
			}
		};
		
		registerReceiver(br, new IntentFilter("com.scalior.mibewmob.services.PollingService"));
		PendingIntent availabilityPendingIntent = 
				PendingIntent.getBroadcast(this, 0, new Intent("com.scalior.mibewmob.services.PollingService"), 0);
		AlarmManager alarmMan = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

		ChatAvailability chatAvail = new ChatAvailability(this);
		chatAvail.computeStateAndNextTrigger();
		
		if (chatAvail.getCurrentState() == ChatAvailability.STATE_AVAILABLE) {
			m_bPollForActiveVisitors = true;
			m_bPollForNewMessages = true;
		} else {
			m_bPollForActiveVisitors = false;
			m_bPollForNewMessages = false;
		}

		alarmMan.set(AlarmManager.RTC_WAKEUP, chatAvail.getNextTriggerMillis(), availabilityPendingIntent);
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
}
