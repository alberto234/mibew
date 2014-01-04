package com.scalior.mibewmob.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.SparseArray;

public class ChatOperator {
	
	// Operator attributes
	private String m_token;
	private String m_username;
	private String m_localeName;
	private String m_commonName;
	private String m_email;
	private int m_permissions;
	private long m_serverID;
	private int m_operatorID_R;
	private String m_oprNotificationId;
	private SparseArray<Object> m_activeVisitors;
	private boolean m_updatedActiveVisitors;
	
	// Constructors
	public ChatOperator(String p_token, int p_operatorID_R, String p_username, String p_localeName, String p_commonName,
						String p_email, int p_permissions) {

		m_token = p_token;
		m_operatorID_R = p_operatorID_R;
		m_username = p_username;
		m_localeName = p_localeName;
		m_commonName = p_commonName;
		m_email = p_email;
		m_permissions = p_permissions;
		m_serverID = 0;
		m_activeVisitors = new SparseArray<Object>();
		m_updatedActiveVisitors = false;
	}

	
	public ChatOperator(JSONObject p_jOperator) {
		try {
			m_token = p_jOperator.getString("oprtoken");
			m_operatorID_R = p_jOperator.getInt("operatorid");
			m_username = p_jOperator.getString("username");
			m_localeName = p_jOperator.getString("localename");
			m_commonName = p_jOperator.getString("commonname");
			m_email = p_jOperator.getString("email");
			m_permissions = p_jOperator.getInt("permissions");
			m_activeVisitors = new SparseArray<Object>();
			m_updatedActiveVisitors = false;
		} catch (JSONException e) {
			throw new RuntimeException("Failed to parse the JSON operator details: " +
							e.getMessage(), e);
		}
	}
	

	// Getters and setters
	public String getToken() {
		return m_token;
	}
	public void setToken(String m_token) {
		this.m_token = m_token;
	}
	public String getLocaleName() {
		return m_localeName;
	}
	public String getUsername() {
		return m_username;
	}


	public void setUsername(String username) {
		m_username = username;
	}


	public void setLocaleName(String localeName) {
		m_localeName = localeName;
	}
	public String getCommonName() {
		return m_commonName;
	}
	public void setCommonName(String commonName) {
		m_commonName = commonName;
	}
	public String getEmail() {
		return m_email;
	}
	public void setEmail(String email) {
		m_email = email;
	}
	public int getPermissions() {
		return m_permissions;
	}
	public void setPermissions(int permissions) {
		m_permissions = permissions;
	}


	public long getServerID() {
		return m_serverID;
	}


	public void setServerID(long serverID) {
		m_serverID = serverID;
	}


	public int getOperatorID_R() {
		return m_operatorID_R;
	}


	public void setOperatorID_R(int operatorID_R) {
		m_operatorID_R = operatorID_R;
	}


	public String getOprNotificationId() {
		return m_oprNotificationId;
	}


	public void setOprNotificationId(String oprNotificationId) {
		m_oprNotificationId = oprNotificationId;
	}


	public void addActiveVisitor(int p_visitorId) {
		m_activeVisitors.put(p_visitorId, null);
		m_updatedActiveVisitors = true;
	}

	public void removeActiveVisitor(int p_visitorId) {
		m_activeVisitors.remove(p_visitorId);
		m_updatedActiveVisitors = true;
	}

	public JSONArray getActiveVisitorsAsJSON() {
		JSONArray jActiveVisitors = new JSONArray();
		for (int i = 0; i < m_activeVisitors.size(); i++) {
			jActiveVisitors.put(m_activeVisitors.keyAt(i));
		}
		return jActiveVisitors;
	}
	
	public void resetUpdatedActiveVisitorsFlag() {
		m_updatedActiveVisitors = false;
	}
	
	public boolean isUpdatedActiveVisitors() {
		return m_updatedActiveVisitors;
	}
}
