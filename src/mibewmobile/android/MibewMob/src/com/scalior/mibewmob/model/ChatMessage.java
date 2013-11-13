package com.scalior.mibewmob.model;

import java.sql.Timestamp;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage {
	// Message types, coordinated with the server
	public static final int TYPE_USER 		= 1;
	public static final int TYPE_AGENT 		= 2;
	public static final int TYPE_FOR_AGENT	= 3;
	public static final int TYPE_INFO 		= 4;
	public static final int TYPE_CONN 		= 5;
	public static final int TYPE_EVENTS		= 6;
	public static final int TYPE_AVATAR		= 7;
	
	// Data members
	private long m_messageID;
	private int m_messageID_R;
	private long m_threadID;
	private String m_message;
	private int m_type;
	private int m_operatorGuid;
	private String m_operatorName;
	private Timestamp m_timeCreated;
	
	// Constructors
	public ChatMessage(long p_threadID, String p_message, int p_type, 
				int p_operatorGuid, String p_operatorName, Timestamp p_timeCreated) {
		m_messageID = 0;
		m_messageID_R = 0;
		m_threadID = p_threadID;
		m_message = p_message;
		m_type = p_type;
		m_operatorGuid = p_operatorGuid;
		m_operatorName = p_operatorName;
		
		if (p_timeCreated != null) {
			m_timeCreated = p_timeCreated;
		} else {
			m_timeCreated = new Timestamp(new Date().getTime());
		}
	}

	public ChatMessage(JSONObject p_jMessage, long p_threadID) {
		try {
			m_messageID = 0;
			m_messageID_R = p_jMessage.getInt("messageid");
			m_threadID = p_threadID; 
			m_message = p_jMessage.getString("tmessage");
			m_type = p_jMessage.getInt("ikind");
			m_operatorGuid = p_jMessage.getInt("agentId");
			if (m_operatorGuid != 0) {
				m_operatorName = p_jMessage.getString("tname");
			}
			
			// Server sends the timestamp with second granularity so we 
			// multiply to get the milliseconds
			m_timeCreated = new Timestamp(p_jMessage.getLong("timestamp") * 1000);
		} catch (JSONException e) {
			throw new RuntimeException("Failed to parse the JSON message details: " +
							e.getMessage(), e);
		}
		
	}
	
	
	// Getters and setters
	public long getThreadID() {
		return m_threadID;
	}
	public void setThreadID(long threadid) {
		m_threadID = threadid;
	}
	public int getType() {
		return m_type;
	}
	public void setType(int type) {
		m_type = type;
	}
	public int getOperatorGuid() {
		return m_operatorGuid;
	}
	public void setOperatorGuid(int operatorGuid) {
		m_operatorGuid = operatorGuid;
	}
	public String getOperatorName() {
		return m_operatorName;
	}
	public void setOperatorName(String operatorname) {
		m_operatorName = operatorname;
	}
	public String getMessage() {
		return m_message;
	}
	public void setMessage(String message) {
		m_message = message;
	}
	public Timestamp getTimeCreated() {
		return m_timeCreated;
	}
	public void setTimeCreated(Timestamp timeCreated) {
		m_timeCreated = timeCreated;
	}

	public long getMessageID() {
		return m_messageID;
	}

	public void setMessageID(long messageID) {
		m_messageID = messageID;
	}

	public int getMessageID_R() {
		return m_messageID_R;
	}

	public void setMessageID_R(int messageID_R) {
		m_messageID_R = messageID_R;
	}
}
