package com.scalior.mibewmob.model;

import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;


public class ChatThread {
	// Chat state constants (from server. We have to make sure they always match)
	public static final int STATE_QUEUE			= 0;
	public static final int STATE_WAITING		= 1;
	public static final int STATE_CHATTING		= 2;
	public static final int STATE_CLOSED		= 3;
	public static final int STATE_LOADING		= 4;
	public static final int STATE_LEFT			= 5;

	private long m_serverID;
	private int m_threadID;
	private int m_state;
	private boolean m_canOpen;
	private boolean m_canView;
	private boolean m_canBan;
	private boolean m_typing;
	private String m_guestName;
	private String m_agentName;
	private int m_agentID;
	private String m_userAgent;
	private String m_initialMessage;
	private long m_ID;
	private int m_token;
	private boolean m_viewed;
	private boolean m_chattingWithGuest;
	
	// These are helper data members, not necessarily saved in the database
	private String m_serverLogoURL;
	
	// Constructors
	public ChatThread(int p_serverID, int p_threadID, int p_state,
						boolean p_canOpen, boolean p_canView, boolean p_canBan,
						boolean p_typing, String p_guestName, String p_agentName,
						int p_agentID, String p_userAgent, String p_initialMessage) {
		m_serverID = p_serverID;
		m_threadID = p_threadID;
		m_state = p_state;
		m_canOpen = p_canOpen;
		m_canView = p_canView;
		m_canBan = p_canBan;
		m_typing = p_typing;
		m_guestName = p_guestName;
		m_agentName = p_agentName;
		m_agentID = p_agentID;
		m_userAgent = p_userAgent;
		m_initialMessage = p_initialMessage;
		m_ID = 0;
		m_token = 0;
		m_viewed = false;
		m_chattingWithGuest = false;
	}
	
	public ChatThread(JSONObject p_jThread, long p_serverID) {
		try {
			m_serverID = p_serverID;
			m_threadID = p_jThread.getInt("threadid");
			m_state = p_jThread.getInt("state");
			m_typing = p_jThread.getInt("typing") != 0;
			m_guestName = p_jThread.getString("name");
			m_agentName = p_jThread.getString("agent");
			m_agentID = p_jThread.getInt("agentid");
			m_userAgent = p_jThread.getString("useragent");
			m_initialMessage = p_jThread.optString("message");
			m_canOpen = p_jThread.optString("canopen") == "true";
			m_canView = p_jThread.optString("canview") == "true";
			m_canBan = p_jThread.optString("canban") == "true";
			m_ID = 0;
			m_token = 0;
			m_viewed = false;
			m_chattingWithGuest = false;
		} catch (JSONException e) {
			throw new RuntimeException("Failed to parse the JSON thread details: " +
							e.getMessage(), e);
		}
	}

	
	// Getters and Setters
	public long getServerID() {
		return m_serverID;
	}
	public void setServerID(int serverID) {
		m_serverID = serverID;
	}
	public int getThreadID() {
		return m_threadID;
	}
	public void setThreadID(int threadID) {
		m_threadID = threadID;
	}
	public int getState() {
		return m_state;
	}
	public void setState(int state) {
		m_state = state;
	}
	public boolean isCanOpen() {
		return m_canOpen;
	}
	public void setCanOpen(boolean canOpen) {
		m_canOpen = canOpen;
	}
	public boolean isCanView() {
		return m_canView;
	}
	public void setCanView(boolean canView) {
		m_canView = canView;
	}
	public boolean isCanBan() {
		return m_canBan;
	}
	public void setCanBan(boolean canBan) {
		m_canBan = canBan;
	}
	public boolean isTyping() {
		return m_typing;
	}
	public void setTyping(boolean typing) {
		m_typing = typing;
	}
	public String getGuestName() {
		return m_guestName;
	}
	public void setGuestName(String guestName) {
		m_guestName = guestName;
	}
	public String getAgentName() {
		return m_agentName;
	}
	public void setAgentName(String agentName) {
		m_agentName = agentName;
	}
	public String getUserAgent() {
		return m_userAgent;
	}
	public void setUserAgent(String userAgent) {
		m_userAgent = userAgent;
	}
	public String getInitialMessage() {
		return m_initialMessage;
	}
	public void setInitialMessage(String initialMessage) {
		m_initialMessage = initialMessage;
	}

	public String getServerLogoURL() {
		return m_serverLogoURL;
	}

	public void setServerLogoURL(String serverLogoURL) {
		m_serverLogoURL = serverLogoURL;
	}

	public long getID() {
		return m_ID;
	}

	public void setID(long p_ID) {
		m_ID = p_ID;
	}

	public int getToken() {
		return m_token;
	}

	public void setToken(int token) {
		m_token = token;
	}

	public boolean isViewed() {
		return m_viewed;
	}

	public void setViewed(boolean viewed) {
		m_viewed = viewed;
	}

	public boolean isChattingWithGuest() {
		return m_chattingWithGuest;
	}

	public void setChattingWithGuest(boolean chattingWithGuest) {
		m_chattingWithGuest = chattingWithGuest;
	}

	public int getAgentID() {
		return m_agentID;
	}

	public void setAgentID(int agentID) {
		m_agentID = agentID;
	}
	
	// Comparator for ChatThreads.
	// This comparator compares using state, time stamp
	public static class ChatThreadComparatorDesc implements Comparator<ChatThread> {

		@Override
		public int compare(ChatThread lhs, ChatThread rhs) {
			// TODO For now, use state, id.
			if ((lhs.getState() == STATE_CLOSED && rhs.getState() == STATE_CLOSED) ||
					(lhs.getState() != STATE_CLOSED && rhs.getState() != STATE_CLOSED))	{
				// They both have the same state (closed or not closed), compare id
				if (lhs.getThreadID() > rhs.getThreadID()) {
					return -1; 
				} else if (lhs.getThreadID() < rhs.getThreadID()) {
					return 1;
				} else {
					return 0;
				}
			} else if (lhs.getState() == STATE_CLOSED){
				return 1;
			} else {
				return -1;
			}
		}		
	}
}