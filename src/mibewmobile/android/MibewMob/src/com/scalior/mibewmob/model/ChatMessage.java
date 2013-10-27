package com.scalior.mibewmob.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage {
	private long m_threadID;
	private String m_message;
	private int m_type;
	private int m_operatorGuid;
	private String m_operatorName;
	private Date m_timeCreated;
	
	// Constructors
	public ChatMessage(long p_threadID, String p_message, int p_type, 
				int p_operatorGuid, String p_operatorName, Date p_timeCreated) {
		m_threadID = p_threadID;
		m_message = p_message;
		m_type = p_type;
		m_operatorGuid = p_operatorGuid;
		m_operatorName = p_operatorName;
		m_timeCreated = p_timeCreated;
	}

	public ChatMessage(JSONObject p_jMessage) {
		try {
			m_threadID = p_jMessage.getInt("threadid"); 
			m_message = p_jMessage.getString("tmessage");
			m_type = p_jMessage.getInt("ikind");
			m_operatorGuid = p_jMessage.getInt("agentId");
			m_operatorName = p_jMessage.getString("tname");
			// m_timeCreated = p_jMessage.getString("dtmcreated");
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
	public Date getTimeCreated() {
		return m_timeCreated;
	}
	public void setTimeCreated(Date timeCreated) {
		m_timeCreated = timeCreated;
	}
	
	
	
}
