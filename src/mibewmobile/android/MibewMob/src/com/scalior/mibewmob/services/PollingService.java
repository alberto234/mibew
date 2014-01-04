package com.scalior.mibewmob.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
	
	private ChatUtils m_serverUtils;

	private HashMap<VisitorListListener, String> m_visitorListListenerMap;
	private SparseArray<ChatThreadListener> m_chatThreadListenerMap;
	
	// Google cloud messaging
	private GoogleCloudMessaging m_gcm;
	private String m_gcmRegId;
	private boolean m_gcmEnabled;
	
	// The SENDER_ID should be saved like in a file or the application manifest
	private final String GCM_SENDER_ID = "114136055473";
	
    public static final String GCM_EXTRA_MESSAGE = "message";
    public static final String GCM_PROPERTY_REG_ID = "registration_id";
    private static final String GCM_PROPERTY_APP_VERSION = "appVersion";
	// private final static int GCM_PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
	@Override
	public void onCreate() {
		// This must be the first line here, so that our worker class is instantiated before
		// anything else that depends on it.
		m_serverUtils = ChatUtils.getInstance(getApplicationContext());
		
		if (checkPlayServices()) {
			m_gcm = GoogleCloudMessaging.getInstance(this);
			m_gcmRegId = getRegistrationId(getApplicationContext());
			
			if (m_gcmRegId.isEmpty()) {
				registerInBackground();
			} else {
				m_gcmEnabled = true;
				m_serverUtils.saveGCMParameters(m_gcmEnabled, m_gcmRegId);
			}
			
		} else {
			// No play services, so notifications from GCM will not work.
			// These notifications serve as triggers when there is new 
			// activity. In the absence of these notifications, the app will
			// be polling for new activity. This isn't guaranteed to work well,
			// especially if the device is sleeping
			MibewMobLogger.Log("No valid Google Play Services APK found.");
			m_gcmEnabled = false;
		}

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
					pollActiveVisitors(false, false);
				} else {
					// Run again after set delay
					m_handler.postDelayed(m_pollActiveVisitors, 30000);
				}
			}
		};
		
		m_pollNewMessages = new Runnable() {

			@Override
			public void run() {
				if (m_bPollForNewMessages) {
					pollNewMessages(false);
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

	private void pollActiveVisitors(final boolean p_bRunOnce, final boolean p_bFromNotification) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				m_serverUtils.acquireWakeLock();
				try {
					if (m_serverUtils.checkForNewVisitors(true, p_bFromNotification)) {
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
				if (!p_bRunOnce) {
					m_handler.postDelayed(m_pollActiveVisitors, 30000);
				}
				m_serverUtils.releaseWakeLock();
			}
		}).start();
	}

	private void pollNewMessages(final boolean p_bRunOnce) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// m_serverUtils.acquireWakeLock();
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
				if (!p_bRunOnce) {
					m_handler.postDelayed(m_pollNewMessages, 5000);
				}
				
				// m_serverUtils.releaseWakeLock();
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
			m_bPollForActiveVisitors = false;
			m_bPollForNewMessages = true;
			
			// Tell the notification service that the operator is online
			m_serverUtils.setNSOperatorStatus(ChatAvailability.STATE_AVAILABLE);
		} else {
			m_bPollForActiveVisitors = false;
			m_bPollForNewMessages = false;

			// Tell the notification service that the operator is offline.
			m_serverUtils.setNSOperatorStatus(ChatAvailability.STATE_UNAVAILABLE);
		}

		alarmMan.set(AlarmManager.RTC_WAKEUP, chatAvail.getNextTriggerMillis(), availabilityPendingIntent);
	}
	
	
	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	        	
	        	// TODO: Since this is running from a service, the context doesn't have access
	        	// to a UI. In the future, launch an activity that give the user an opportunity
	        	// to download the Google Play Services.
	            // GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	               //      GCM_PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            MibewMobLogger.Log("This device is not supported.");
	        }
	        return false;
	    }
	    return true;
	}


	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    String registrationId = prefs.getString(GCM_PROPERTY_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        MibewMobLogger.Log("Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = prefs.getInt(GCM_PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        MibewMobLogger.Log("App version changed.");
	        return "";
	    }
	    return registrationId;
	}
	
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return getSharedPreferences(ChatActivity.class.getSimpleName(),
	            Context.MODE_PRIVATE);
	}
	
	
	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
	    new AsyncTask<Void, Void, String>() {
	        @Override
	        protected String doInBackground(Void... params) {
	            String msg = "";
	            Context context = PollingService.this.getApplicationContext();
	            
	            try {
	                if (m_gcm == null) {
	                    m_gcm = GoogleCloudMessaging.getInstance(context);
	                }
	                m_gcmRegId = m_gcm.register(GCM_SENDER_ID);
	                if (!m_gcmRegId.isEmpty()) {
		                msg = "Device registered, registration ID=" + m_gcmRegId;

		                // You should send the registration ID to your server over HTTP,
		                // so it can use GCM/HTTP or CCS to send messages to your app.
		                // The request to your server should be authenticated if your app
		                // is using accounts.
		                sendRegistrationIdToBackend();

		                // Persist the regID - no need to register again.
		                storeRegistrationId(context, m_gcmRegId);

			    	    m_gcmEnabled = true;
						m_serverUtils.saveGCMParameters(m_gcmEnabled, m_gcmRegId);
	                }
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	                // If there is an error, don't just keep trying to register.
	                // Require the user to click a button again, or perform
	                // exponential back-off.
	            }
	            return msg;
	        }

	        @Override
	        protected void onPostExecute(String msg) {
	        	MibewMobLogger.Log(msg);
	        }
	    }.execute(null, null, null);
	}
	
	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
	 * or CCS to send messages to your app. Not needed for this demo since the
	 * device sends upstream messages to a server that echoes back the message
	 * using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		
		// Registration shall be sent to the backend on a per-monitored server basis
	}
	
	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    int appVersion = getAppVersion(context);
	    MibewMobLogger.Log("Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(GCM_PROPERTY_REG_ID, regId);
	    editor.putInt(GCM_PROPERTY_APP_VERSION, appVersion);
	    editor.commit();
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
		
		public void doCheckForActiveVisitors(String p_oprToken) {
			// TODO: Check schedule
			
			// Keep device awake
			m_serverUtils.acquireWakeLock();
			pollActiveVisitors(true, true);
			m_serverUtils.releaseWakeLock();
		}
		
		public void doCheckForNewMessages() {
			// TODO: Check messages only from thread that reported that there was a new message
			// 		 Also check schedule

			// Keep device awake
			m_serverUtils.acquireWakeLock();
			pollNewMessages(true);
			m_serverUtils.releaseWakeLock();
		}
		
		// This sends a response of a ping from the notification server
		public void sendPong() {
			
		}
	}
}
