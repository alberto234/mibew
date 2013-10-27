package com.scalior.mibewmob;

import java.util.ArrayList;
import java.util.List;

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

/* This is a utility class that enables communication between
 * the chat server and the application
 * 
 * Author: ENsoesie		9/23/2013
 */
public class ChatUtils {
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

	
	private boolean m_bRefreshServerList;
	private List<MonitoredSite> m_monitoredSitesList;
	private SparseArray<MonitoredSite> m_monitoredSitesMap;
	private List<ChatThread> m_visitorList;
	private SparseArray<ChatThread> m_visitorMap;

	// Note. The chat utility should be created with the application context.
	// That said, no UI updates should be done using this context.
	private Context m_context;

	// For singleton pattern
	private static ChatUtils m_sInstance = null;
	private ChatUtils(Context p_context) {
		m_context = p_context;
		m_bRefreshServerList = false;
		m_monitoredSitesList = null;
		m_visitorList = loadArchivedVisitorList();
	};

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

		JSONObject jResult = WebServiceBridge.loginOperator(p_serverURL, p_username, p_password);
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
	
	public List<ChatThread> loadArchivedVisitorList() {
		if (m_visitorList == null) {
			m_visitorList = new ArrayList<ChatThread>();

			MibewMobSQLiteHelper dbHelper = new MibewMobSQLiteHelper(m_context);
			dbHelper.loadArchivedVisitors(m_visitorList);
			
			// Refresh the visitor map.
			if (m_visitorMap == null) {
				m_visitorMap = new SparseArray<ChatThread>();
			}
			m_visitorMap.clear();
			
			for (int i = 0; i < m_visitorList.size(); i++) {
				m_visitorMap.put(m_visitorList.get(i).getThreadID(),
								m_visitorList.get(i));
			}
		}

		return m_visitorList;
	}

	public List<ChatThread> getVisitorList() {
		return m_visitorList;
	}
	
	public SparseArray<ChatThread> getVisitorMap() {
		return m_visitorMap;
	}

	/**
	 * Description:
	 * 	Get the list of active conversations for the given server
	 * 
	 * @param p_server
	 * @param p_oprtoken
	 * 
	 * @return List<ChatThread>: list of active visitors
	 * 
	 * @throws	MibewMobException:
	 * 
	 * @author ENsoesie  10/19/2013
	 */
	public List<ChatThread> getActiveVisitors(ChatServer p_server, String p_oprtoken) 
			throws MibewMobException {
		List<ChatThread> visitorList = null;
		
		JSONObject jActiveVisitors = 
				WebServiceBridge.getActiveVisitors(p_server.getWebServiceURL(), p_oprtoken);
		try {
			int errorCode = jActiveVisitors.getInt("errorCode");

			if (errorCode == SERVER_ERROR_SUCCESS &&
					jActiveVisitors.getInt("threadCount") > 0) {
				visitorList = new ArrayList<ChatThread>();
				
				JSONArray jThreadList = jActiveVisitors.getJSONArray("threadList");
				for (int j = 0; j < jThreadList.length(); j++) {
					visitorList.add(new ChatThread(jThreadList.getJSONObject(j),
							p_server.getID()));
				}
				
				return visitorList;
			}
			else {
				throw new MibewMobException("Server error (" + errorCode + 
							") getting active visitors", errorCode);
			}
		} catch (JSONException e) {
			throw new RuntimeException("Failed to parse JSON when getting active visitors for server " + 
							p_server.getURL() + ": " + e.getMessage(), e);
		}
	}


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
				return true;
			}
			else {
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
	 * 	View a conversation. This indicates to the server that the operator wants
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
	public List<ChatMessage> getNewMessagesFromServer(ChatThread p_thread) 
			throws MibewMobException {
		MonitoredSite monitoredSite = m_monitoredSitesMap.get((int) p_thread.getServerID());
		String serverURL = monitoredSite.getServer().getWebServiceURL();
		String oprtoken = monitoredSite.getOperator().getToken();
		
		List<ChatMessage> messageList = new ArrayList<ChatMessage>();
		
		JSONObject jNewMessages = 
				WebServiceBridge.getNewMessagesFromServer(serverURL, oprtoken,
						p_thread.getThreadID(), p_thread.getToken());
		try {
			int errorCode = jNewMessages.getInt("errorCode");

			if (errorCode == SERVER_ERROR_SUCCESS &&
					jNewMessages.getInt("messageCount") > 0) {
				
				JSONArray jMessageList = jNewMessages.getJSONArray("threadList");
				for (int j = 0; j < jMessageList.length(); j++) {
					messageList.add(new ChatMessage(jMessageList.getJSONObject(j)));
				}
				
				return messageList;
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
	 * @param p_thread: The URL where the Mibew web service is hosted
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

		JSONObject jResponse = 
				WebServiceBridge.sendMessage(serverURL, oprtoken, p_thread.getThreadID(),
						p_thread.getToken(), p_message.getMessage());
		try {
			int errorCode = jResponse.getInt("errorCode");
			if (errorCode == SERVER_ERROR_SUCCESS) {
				// TODO: Update the message with details from the server
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
}
