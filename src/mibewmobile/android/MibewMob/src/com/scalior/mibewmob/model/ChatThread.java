package com.scalior.mibewmob.model;

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
	private String m_userAgent;
	private String m_initialMessage;
	private long m_ID;
	private int m_token;
	private boolean m_viewed;
	
	// These are helper data members, not necessarily saved in the database
	private String m_serverLogoURL;
	
	// Constructors
	public ChatThread(int p_serverID, int p_threadID, int p_state,
						boolean p_canOpen, boolean p_canView, boolean p_canBan,
						boolean p_typing, String p_guestName, String p_agentName,
						String p_userAgent, String p_initialMessage) {
		m_serverID = p_serverID;
		m_threadID = p_threadID;
		m_state = p_state;
		m_canOpen = p_canOpen;
		m_canView = p_canView;
		m_canBan = p_canBan;
		m_typing = p_typing;
		m_guestName = p_guestName;
		m_agentName = p_agentName;
		m_userAgent = p_userAgent;
		m_initialMessage = p_initialMessage;
		m_ID = 0;
		m_token = 0;
		m_viewed = false;
	}
	
	public ChatThread(JSONObject p_jThread, long p_serverID) {
		try {
			m_serverID = p_serverID;
			m_threadID = p_jThread.getInt("threadid");
			m_state = p_jThread.getInt("state");
			m_typing = p_jThread.getInt("typing") != 0;
			m_guestName = p_jThread.getString("name");
			m_agentName = p_jThread.getString("agent");
			m_userAgent = p_jThread.getString("useragent");
			m_initialMessage = p_jThread.optString("message");
			m_canOpen = p_jThread.optString("canopen") == "true";
			m_canView = p_jThread.optString("canview") == "true";
			m_canBan = p_jThread.optString("canban") == "true";
			m_ID = 0;
			m_token = 0;
			m_viewed = false;
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

	public void setID(int iD) {
		m_ID = iD;
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
	
}
