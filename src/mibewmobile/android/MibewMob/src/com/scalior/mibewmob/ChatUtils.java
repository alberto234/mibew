package com.scalior.mibewmob;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.scalior.mibewmob.database.MibewMobSQLiteHelper;
import com.scalior.mibewmob.model.ChatMessage;
import com.scalior.mibewmob.model.ChatOperator;
import com.scalior.mibewmob.model.ChatServer;
import com.scalior.mibewmob.model.ChatThread;
import com.scalior.mibewmob.model.MonitoredSite;

import android.content.Context;
import android.util.SparseArray;

/* Some constants from the server.
 *
$state_queue = 0;
$state_waiting = 1;
$state_chatting = 2;
$state_closed = 3;
$state_loading = 4;
$state_left = 5;

$kind_user = 1;
$kind_agent = 2;
$kind_for_agent = 3;
$kind_info = 4;
$kind_conn = 5;
$kind_events = 6;
$kind_avatar = 7;

 */

/* This is a general purpose utility class. It serves as a bridge between
 * the different components of the application
 * 
 * Author: ENsoesie		9/23/2013
 */
public class ChatUtils {
	// Chat Util error codes
	public static final int ERROR_SUCCESS 					= 100;
	public static final int ERROR_UNKNOWN					= 101;
	public static final int ERROR_CANT_REACH_SERVER			= 102;

	
	// Chat server error codes. These mirror what the web service will return
	// in the JSON responses.
	public static final int SERVER_ERROR_SUCCESS 			= 0;
	public static final int SERVER_ERROR_LOGIN_FAILED 		= 1;
	public static final int SERVER_ERROR_INVALID_OP_TOKEN 	= 2;
	public static final int SERVER_ERROR_INVALID_THREAD		= 3;
	public static final int SERVER_ERROR_CANNOT_TAKEOVER	= 4;
	public static final int SERVER_ERROR_CONFIRM_TAKEOVER 	= 5;
	public static final int SERVER_ERROR_CANNOT_VIEW_THREAD	= 6;
	public static final int SERVER_ERROR_WRONG_THREAD		= 7;
	public static final int SERVER_ERROR_INVALID_CHAT_TOKEN	= 8;
	public static final int SERVER_ERROR_INVALID_COMMAND	= 9;
	public static final int SERVER_ERROR_UNKNOWN 			= 10;
	public static final int SERVER_ERROR_THREAD_CLOSED		= 11;

	// String constants used around
	public static final String CHAT_KEY 		= "thread_key";
	
	
	private final String m_dbCreationUUID;
	
	private boolean m_bRefreshServerList;
	private List<MonitoredSite> m_monitoredSitesList;
	private SparseArray<MonitoredSite> m_monitoredSitesMap;
	private List<ChatThread> m_visitorList;
//	private SparseArray<ChatThread> m_visitorMap;
	private SparseArray<ChatThread> m_threadsToExpand;
	
	private List<ChatServer> m_serversWithNewVisitors;

	// Note. The chat utility should be created with the application context.
	// That said, no UI updates should be done using this context.
	private Context m_context;

	// For singleton pattern
	private static ChatUtils m_sInstance = null;
	private ChatUtils(Context p_context) {
		m_context = p_context;
		m_bRefreshServerList = false;
		m_threadsToExpand = null;
		m_monitoredSitesList = reloadMonitoredSitesList();
		m_visitorList = reloadArchivedVisitorList();
		m_dbCreationUUID = getDBUUID();
		
		// Set the WebServiceBridge's context
		MibewMobLogger.setContext(m_context);
	};

	private String getDBUUID() {
		MibewMobSQLiteHelper database = new MibewMobSQLiteHelper(m_context);
		return database.getDBUUID();
	}

	public static ChatUtils getInstance(Context p_context) {
		if (m_sInstance == null) {
			m_sInstance = new ChatUtils(p_context);
		}
		return m_sInstance;
	}
	
	
	/**
	 * Description:
	 * 	Validate if the server provided exists, and returns the details
	 *
	 * @param p_url: The URL where the Mibew server is hosted, e.g
	 * 				 http://www.example.com/webim
	 * @return ChatServer:  
	 * 
	 * @throws	MibewMobException: If it can't validate the server
	 * 
	 * @author ENsoesie  9/23/2013
	 */
	private ChatServer validateServer(String p_url) throws MibewMobException {
		return new ChatServer(WebServiceBridge.validateServer(p_url));
	}
	

	/**
	 * Description:
	 * 	Log the operator into the chat server
	 * 
	 * @param p_serverURL: The URL where the Mibew web service is hosted
	 * @param p_username
	 * @param p_password
	 * 
	 * @return ChatOperator:  
	 * 
	 * @throws	MibewMobException: If it can't validate the server
	 * 
	 * @author ENsoesie  9/23/2013
	 */
	public ChatOperator loginOperator(String p_serverURL, String p_username, 
							String p_password) throws MibewMobException {

		JSONObject jResult = WebServiceBridge.loginOperator(p_serverURL, p_username, p_password, m_dbCreationUUID);
		try {
			int errorCode = jResult.getInt("errorCode");
			if ( errorCode == SERVER_ERROR_SUCCESS) {
				return new ChatOperator(jResult);
			}
			else {
				throw new MibewMobException("Server error during login", errorCode);
			}
		} catch (JSONException e) {
			throw new RuntimeException("Failed to parse JSON during login for server + " + 
							": " + e.getMessage(), e);
		}
	}
	
	public boolean getRefreshServerList() {
		return m_bRefreshServerList;
	}
	
	public void setRefreshServerList(boolean p_bRefreshServerList) {
		m_bRefreshServerList = p_bRefreshServerList;
	}


	
	/**
	 * Description:
	 * 		Method to add a new monitored site.
	 * 		This method is called after a successful login to a new site.
	 * @param p_server
	 * @param p_operator
	 * @return index of the new monitored site in the list, or -1 if it failed
	 * 
	 */
	private MonitoredSite addNewMonitoredSite(ChatServer p_server, ChatOperator p_operator) {
		if (p_server == null || p_operator == null) {
			return null;
		}
		
		MonitoredSite newSite = null;
		MibewMobSQLiteHelper dbHelper = new MibewMobSQLiteHelper(m_context);
		long serverID = dbHelper.addNewChatServer(p_server);
		if (serverID != -1) {
			p_operator.setServerID(serverID);
			long operatorID = dbHelper.addNewOperator(p_operator);
			if (operatorID != -1) {
				newSite = new MonitoredSite(p_server, p_operator);
				if (m_monitoredSitesList == null) {
					m_monitoredSitesList = new ArrayList<MonitoredSite>();
				}
				m_monitoredSitesList.add(newSite);
				
				// Maintain the map. This is used for faster querying, and will eventually
				// replace the list in the server util class. To obtain a list from the map,
				// new ArrayList<MonitoredSites>(m_monitoredSitesMap.values());
				if (m_monitoredSitesMap == null) {
					m_monitoredSitesMap = new SparseArray<MonitoredSite>();
				}
				m_monitoredSitesMap.put((int) newSite.getServer().getID(), newSite);

				return newSite;
			}
		}
		
		return null;
	}
	
		
	
	public List<MonitoredSite> reloadMonitoredSitesList() {
		// Clear the server list before the starting the query
		MibewMobSQLiteHelper dbHelper = new MibewMobSQLiteHelper(m_context);
		if (m_monitoredSitesList == null) {
			m_monitoredSitesList = new ArrayList<MonitoredSite>();
		}
		
		dbHelper.refreshMonitoredSites(m_monitoredSitesList);
		
		if (m_monitoredSitesMap == null) {
			m_monitoredSitesMap = new SparseArray<MonitoredSite>();
		}
		for (int i = 0; i < m_monitoredSitesList.size(); i++) {
			m_monitoredSitesMap.put((int) m_monitoredSitesList.get(i).getServer().getID(), m_monitoredSitesList.get(i));
		}
		return m_monitoredSitesList;
	}
	
	public MonitoredSite getSiteAt(int index) {
		if (m_monitoredSitesList == null ||
			index < 0 || index >= m_monitoredSitesList.size()) {
			return null;
		}
		
		return m_monitoredSitesList.get(index);
	}
	
	public List<ChatThread> reloadArchivedVisitorList() {
		if (m_visitorList == null) {
			m_visitorList = new ArrayList<ChatThread>();
		}
		
		m_visitorList.clear();

		MibewMobSQLiteHelper dbHelper = new MibewMobSQLiteHelper(m_context);
		dbHelper.loadArchivedVisitors(m_visitorList);
		
		// Refresh the visitor map.
//		if (m_visitorMap == null) {
//			m_visitorMap = new SparseArray<ChatThread>();
//		}
//		m_visitorMap.clear();
		
//		for (int i = 0; i < m_visitorList.size(); i++) {
	//		m_visitorMap.put(m_visitorList.get(i).getThreadID(),
		//					m_visitorList.get(i));
//		}

		Collections.sort(m_visitorList, new ChatThread.ChatThreadComparatorDesc());
		
		return m_visitorList;
	}

	public List<ChatThread> getVisitorList() {
		return m_visitorList;
	}
	
//	public SparseArray<ChatThread> getVisitorMap() {
//		return m_visitorMap;
//	}

	/**
	 * Description:
	 * 	Get the list of NEW active conversations for the given server.
	 * 	Updated conversations are going to be returned in p_updatedVisitors
	 * 
	 * @param p_monitoredSite
	 * @param p_oprtoken
	 * @param p_updatedVisitors: any visitors that have changed status
	 * 
	 * @return List<ChatThread>: list of active visitors
	 * 
	 * @throws	MibewMobException:
	 * 
	 * @author ENsoesie  10/19/2013
	 */
	private List<ChatThread> getActiveVisitors(MonitoredSite p_monitoredSite, String p_oprtoken,
					List<ChatThread> p_updatedVisitors) throws MibewMobException {
		List<ChatThread> retVisitorList = new ArrayList<ChatThread>();
		List<ChatThread> activeVisitorList = new ArrayList<ChatThread>();
		
		// Get a comma-separated list of current active visitors
		StringBuilder sbActiveVisitorString = new StringBuilder();
		boolean bFirst = true;
		for(ChatThread visitor: m_visitorList) {
			if (visitor.getServerID() == p_monitoredSite.getServer().getID() &&
				visitor.getState() != ChatThread.STATE_CLOSED) {
				if (!bFirst) {
					sbActiveVisitorString.append(",");
				} else {
					bFirst = false;
				}
				sbActiveVisitorString.append(visitor.getThreadID());
				activeVisitorList.add(visitor);
			}
		}

		try {
			JSONObject jActiveVisitors = 
					WebServiceBridge.getActiveVisitors(p_monitoredSite.getServer().getWebServiceURL(), 
							p_oprtoken, sbActiveVisitorString.toString());

			int errorCode = jActiveVisitors.getInt("errorCode");

			if (errorCode == SERVER_ERROR_SUCCESS) {
				if (jActiveVisitors.optInt("threadCount") > 0) {
					
					// Indicate that this server has a new visitor. This will be used for notifications
					/*if (m_serversWithNewVisitors == null) {
						m_serversWithNewVisitors = new ArrayList<ChatServer>();
					}
					
					m_serversWithNewVisitors.add(p_monitoredSite.getServer()); */

					JSONArray jThreadList = jActiveVisitors.getJSONArray("threadList");
					for (int j = 0; j < jThreadList.length(); j++) {
						
						// If this is an existing thread, update the fields in the internal list
						// This change will only be persisted in the db if the save to db flag
						// was set when calling checkForNewVisitors
						boolean bExistingVisitor = false;
						JSONObject jVisitor = jThreadList.getJSONObject(j);
						for(ChatThread visitor: activeVisitorList) {
							if (visitor.getThreadID() == jVisitor.getInt("threadid")) {
								visitor.setState(jVisitor.getInt("state"));
								if (jVisitor.optInt("shownmessageid") != 0) {
									visitor.setInitialMessage(jVisitor.getString("message"));
								}
								bExistingVisitor = true;
								p_updatedVisitors.add(visitor);
								break;
							}
						}
						
						if (bExistingVisitor) {
							continue;
						}
						
						ChatThread visitor = new ChatThread(jVisitor, p_monitoredSite.getServer().getID());
						
						// Determine if this is a thread that the user is actively chatting with
						if (visitor.getAgentID() == p_monitoredSite.getOperator().getOperatorID_R() &&
							visitor.getState() == ChatThread.STATE_CHATTING) {
							visitor.setChattingWithGuest(true);
						}
						retVisitorList.add(visitor);
					}
				}				
				return retVisitorList;
			}
			else {
				throw new MibewMobException("Server error (" + errorCode + 
							") getting active visitors", errorCode);
			}
		} catch (JSONException e) {
			throw new RuntimeException("Failed to parse JSON when getting active visitors for server " + 
							p_monitoredSite.getServer().getURL() + ": " + e.getMessage(), e);
		}
	}


	/**
	 * Description:
	 * 	Checks the monitored servers if there are any new visitors
	 * 
	 * @param p_bSaveToDb: Determine if after the check we should save the new visitors to the database
	 *
	 * @return boolean: true if the there were new visitors, false otherwise
	 * 
	 * @throws	MibewMobException:
	 * 
	 * @author ENsoesie  11/11/2013
	 */
	public synchronized boolean checkForNewVisitors(boolean p_bSaveToDb) throws MibewMobException {
		List<ChatThread> visitors = new ArrayList<ChatThread>();
		List<ChatThread> updatedVisitors = new ArrayList<ChatThread>();
		
		if (m_monitoredSitesList == null) {
			return false;
		}
		
		if (m_serversWithNewVisitors != null) {
			m_serversWithNewVisitors.clear();
		}
		
		for (int i = 0; i < m_monitoredSitesList.size(); i++) {
			visitors.addAll(getActiveVisitors(m_monitoredSitesList.get(i),
					m_monitoredSitesList.get(i).getOperator().getToken(),
					updatedVisitors));
		}
		
		if (visitors.size() > 0 || updatedVisitors.size() > 0) {
			if (p_bSaveToDb) {
				
				// Add to the database
				MibewMobSQLiteHelper database = new MibewMobSQLiteHelper(m_context);

				for(ChatThread visitor: visitors) {
					database.addOrUpdateThread(visitor);
				}
				
				for(ChatThread visitor: updatedVisitors) {
					database.addOrUpdateThread(visitor);
				}
			}
			
			m_visitorList.addAll(visitors);
			Collections.sort(m_visitorList, new ChatThread.ChatThreadComparatorDesc());

			// Return true if there was a change
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Description:
	 * 	Saves the list of new visitors to the database.
	 * 
	 * @param p_updatedList: The list of new visitors
	 *
	 * @return List<ChatThread>: the merged list, containing the new visitors
	 * 
	 * @author ENsoesie  10/19/2013
	 */
	/*public List<ChatThread> saveNewisitors(List<ChatThread> p_updatedList) {
		// This method updates the internal m_visitorList as well as returns it.

		List<ChatThread> tempVisitorList = new ArrayList<ChatThread>();

		tempVisitorList.addAll(p_updatedList);
		tempVisitorList.addAll(m_visitorList);
		
		Collections.sort(tempVisitorList, new ChatThread.ChatThreadComparatorDesc());

		// Replace 
		m_visitorList.clear();
		m_visitorList.addAll(tempVisitorList);
		
		// Add to the database
		MibewMobSQLiteHelper database = new MibewMobSQLiteHelper(m_context);
		//for
		
		for(ChatThread visitor: m_visitorList) {
			
		}
		return m_visitorList;
	}*/

	public List<ChatMessage> getMessages(long p_threadID) {
		MibewMobSQLiteHelper dbHelper = new MibewMobSQLiteHelper(m_context);
		
		return dbHelper.getMessages(p_threadID);
	}
	

	/**
	 * Description:
	 * 	Initiate a conversation. This indicates to the server that the operator is
	 *  ready to have an active conversation with the guest
	 * 
	 * @param p_thread: The URL where the Mibew web service is hosted
	 * 
	 * @return boolean
	 * 
	 * @throws	MibewMobException:
	 * 
	 * @author ENsoesie  10/22/2013
	 */
	public boolean startChatWithGuest(ChatThread p_thread) throws MibewMobException {
		MonitoredSite monitoredSite = m_monitoredSitesMap.get((int) p_thread.getServerID());
		String serverURL = monitoredSite.getServer().getWebServiceURL();
		String oprtoken = monitoredSite.getOperator().getToken();

		JSONObject jResult = WebServiceBridge.startChatWithGuest(serverURL, oprtoken,
									p_thread.getThreadID());

		try {
			int errorCode = jResult.getInt("errorCode");
			if (errorCode == SERVER_ERROR_SUCCESS) {
				p_thread.setToken(jResult.getInt("chattoken"));
				p_thread.setChattingWithGuest(true);
				
				// Update the thread in the database
				MibewMobSQLiteHelper dbHelper = new MibewMobSQLiteHelper(m_context);
				dbHelper.addOrUpdateThread(p_thread);
				reloadArchivedVisitorList();
				return true;
			} else {
				if (errorCode == SERVER_ERROR_THREAD_CLOSED) {
					p_thread.setState(ChatThread.STATE_CLOSED);

					// Update the thread in the database
					MibewMobSQLiteHelper dbHelper = new MibewMobSQLiteHelper(m_context);
					dbHelper.addOrUpdateThread(p_thread);
					reloadArchivedVisitorList();
				}

				throw new MibewMobException("Server error (" + errorCode + 
							") initiating chat with guest", errorCode);
			}
		} catch (JSONException e) {
			throw new RuntimeException("Failed to parse JSON when initiating chat with server " + 
							monitoredSite.getServer().getURL() + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Description:
	 * 	Initiate a conversation. This indicates to the server that the operator wants
	 *  only to view the conversation 
	 * 
	 * @param p_thread: The URL where the Mibew web service is hosted
	 * 
	 * @return boolean
	 * 
	 * @throws	MibewMobException:
	 * 
	 * @author ENsoesie  10/22/2013
	 */
	public boolean viewThread(ChatThread p_thread) throws MibewMobException {
		MonitoredSite monitoredSite = m_monitoredSitesMap.get((int) p_thread.getServerID());
		String serverURL = monitoredSite.getServer().getWebServiceURL();
		String oprtoken = monitoredSite.getOperator().getToken();

		JSONObject jResult = WebServiceBridge.viewThread(serverURL, oprtoken,
									p_thread.getThreadID());

		try {
			int errorCode = jResult.getInt("errorCode");
			if (errorCode == SERVER_ERROR_SUCCESS) {
				p_thread.setToken(jResult.getInt("chattoken"));
				
				// Determine if the operator is already actively chatting with this user
				if (p_thread.getState() == ChatThread.STATE_CHATTING &&
					p_thread.getAgentID() == monitoredSite.getOperator().getOperatorID_R()) {
					p_thread.setChattingWithGuest(true);
				}
				
				// Save this thread in the database if this is the first time we are viewing it
				if (!p_thread.isViewed()) {
					p_thread.setViewed(true);
					MibewMobSQLiteHelper dbHelper = new MibewMobSQLiteHelper(m_context);
					dbHelper.addOrUpdateThread(p_thread);
					
					// Notify any listeners
					reloadArchivedVisitorList();
				}

				return true;
			}
			else {
				throw new MibewMobException("Server error (" + errorCode + 
							") viewing chat thread", errorCode);
			}
		} catch (JSONException e) {
			throw new RuntimeException("Failed to parse JSON when initiating chat with server " + 
							monitoredSite.getServer().getURL() + ": " + e.getMessage(), e);
		}
	}
	


	/**
	 * Description:
	 * 	Gets a list of new messages for a given thread from the server 
	 * 
	 * @param p_thread: 
	 * 
	 * @return List<ChatMessage>
	 * 
	 * @throws	MibewMobException:
	 * 
	 * @author ENsoesie  10/22/2013
	 */
	public List<ChatMessage> getNewMessagesFromServer(ChatThread p_thread) 
			throws MibewMobException {
		MonitoredSite monitoredSite = m_monitoredSitesMap.get((int) p_thread.getServerID());
		final String serverURL = monitoredSite.getServer().getWebServiceURL();
		final String oprtoken = monitoredSite.getOperator().getToken();
		
		List<ChatMessage> messageList = new ArrayList<ChatMessage>();
		
		JSONObject jNewMessages = 
				WebServiceBridge.getNewMessagesFromServer(serverURL, oprtoken,
						p_thread.getThreadID(), p_thread.getToken());
		try {
			int errorCode = jNewMessages.getInt("errorCode");

			if (errorCode == SERVER_ERROR_SUCCESS) {
				if (jNewMessages.getInt("messageCount") > 0) {
					final StringBuilder sbMsgIdList = new StringBuilder();
					
					JSONArray jMessageList = jNewMessages.getJSONArray("messageList");
					for (int j = 0; j < jMessageList.length(); j++) {
						messageList.add(new ChatMessage(jMessageList.getJSONObject(j), p_thread.getID()));
						if (j != 0) {
							sbMsgIdList.append(",");
						}
						sbMsgIdList.append(messageList.get(j).getMessageID_R());
					}
					
					// Save these messages to the database.
					MibewMobSQLiteHelper dbHelper = new MibewMobSQLiteHelper(m_context);
					List<ChatMessage> addedMessages = dbHelper.addNewChatMessages(messageList);
					
					// Launch a new thread to send the acknowledgment.
					Thread ackThread = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								WebServiceBridge.acknowledgeMessages(serverURL, oprtoken, sbMsgIdList.toString());
							} catch (MibewMobException e) {
								// TODO There was a communication error. We need to save the list to be validated later
							}
						}
					});
					
					ackThread.start();
					
					return addedMessages;
				} else {
					return null;
				}
			}
			else {
				throw new MibewMobException("Server error (" + errorCode + 
							") getting new messages", errorCode);
			}
		} catch (JSONException e) {
			throw new RuntimeException("Failed to parse JSON when getting new messages for server " + 
							monitoredSite.getServer().getURL() + ": " + e.getMessage(), e);
		}
	}

	
	/**
	 * Description:
	 * 	Send a message to the guest
	 * 
	 * @param p_thread: 
	 * @param p_message
	 * 
	 * @return boolean
	 * 
	 * @throws	MibewMobException:
	 * 
	 * @author ENsoesie  10/24/2013
	 */
	public boolean sendMessage(ChatThread p_thread, ChatMessage p_message) 
			throws MibewMobException {
		MonitoredSite monitoredSite = m_monitoredSitesMap.get((int) p_thread.getServerID());
		String serverURL = monitoredSite.getServer().getWebServiceURL();
		String oprtoken = monitoredSite.getOperator().getToken();

		p_message.setOperatorGuid(monitoredSite.getOperator().getOperatorID_R());
		p_message.setOperatorName(monitoredSite.getOperator().getCommonName());
		
		// Add the message to the database first
		MibewMobSQLiteHelper dbHelper = new MibewMobSQLiteHelper(m_context);
		long messageID = dbHelper.addNewChatMessage(p_message);
		p_message.setMessageID(messageID);

		// Then post the message to the server
		JSONObject jResponse = 
				WebServiceBridge.sendMessage(serverURL, oprtoken, p_thread.getThreadID(),
						p_thread.getToken(), (int)messageID, p_message.getMessage());
		try {
			int errorCode = jResponse.getInt("errorCode");
			if (errorCode == SERVER_ERROR_SUCCESS) {
				p_message.setMessageID_R(jResponse.getInt("messageidr"));
				p_message.setTimeCreated(new Timestamp(jResponse.getLong("timestamp") * 1000));
				dbHelper.updateMessage(p_message);
				return true;
			}
			else {
				throw new MibewMobException("Server error (" + errorCode + 
							") sending a message", errorCode);
			}
		} catch (JSONException e) {
			throw new RuntimeException("Failed to parse JSON when sending a new message to server " + 
							monitoredSite.getServer().getURL() + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Description:
	 * 	Close the conversation 
	 * 
	 * @param p_thread: 
	 * 
	 * @return boolean
	 * 
	 * @throws	MibewMobException:
	 * 
	 * @author ENsoesie  10/22/2013
	 */
	public boolean closeThread(ChatThread p_thread) throws MibewMobException {
		MonitoredSite monitoredSite = m_monitoredSitesMap.get((int) p_thread.getServerID());
		String serverURL = monitoredSite.getServer().getWebServiceURL();
		String oprtoken = monitoredSite.getOperator().getToken();

		JSONObject jResult = WebServiceBridge.closeThread(serverURL, oprtoken,
									p_thread.getThreadID());

		try {
			int errorCode = jResult.getInt("errorCode");
			if (errorCode == SERVER_ERROR_SUCCESS) {
				p_thread.setState(ChatThread.STATE_CLOSED);

				MibewMobSQLiteHelper dbHelper = new MibewMobSQLiteHelper(m_context);
				dbHelper.addOrUpdateThread(p_thread);
				
				reloadArchivedVisitorList();
				return true;
			}
			else {
				throw new MibewMobException("Server error (" + errorCode + 
							") viewing chat thread", errorCode);
			}
		} catch (JSONException e) {
			throw new RuntimeException("Failed to parse JSON when initiating chat with server " + 
							monitoredSite.getServer().getURL() + ": " + e.getMessage(), e);
		}
	}
	


	
	
	
	
	
	public MonitoredSite addSiteToMonitor(String p_serverURL, String p_username, String p_password) 
			throws MibewMobException {
		ChatServer newServer = validateServer(p_serverURL);
		ChatOperator newOperator = 
				loginOperator(newServer.getWebServiceURL(), p_username, p_password);
		return addNewMonitoredSite(newServer, newOperator);
	}

	public MonitoredSite getSiteWithID(long p_serverID) {
		return m_monitoredSitesMap.get((int)p_serverID);
	}

	public ChatThread getThreadToExpand(int p_key) {
		ChatThread thread = m_threadsToExpand.get(p_key);
		m_threadsToExpand.remove(p_key);
		return thread;
	}

	public int setThreadToExpand(ChatThread threadToExpand) {
		int key = new Random().nextInt(1000) + 1;
		if (m_threadsToExpand == null) {
			m_threadsToExpand = new SparseArray<ChatThread>();
		}
		m_threadsToExpand.put(key, threadToExpand);
		
		return key;
	}
	
	public List<ChatServer> getServersWithNewVisitors() {
		return m_serversWithNewVisitors;
	}

	public String getDbCreationUUID() {
		return m_dbCreationUUID;
	}
}
