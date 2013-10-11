package com.scalior.mibewmob.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatOperator {
	
	// Operator attributes
	private String m_token;
	private String m_username;
	private String m_localeName;
	private String m_commonName;
	private String m_email;
	private int m_permissions;
	
	// Constructors
	public ChatOperator(String p_token, String p_username, String p_localeName, String p_commonName,
						String p_email, int p_permissions) {

		m_token = p_token;
		m_username = p_username;
		m_localeName = p_localeName;
		m_commonName = p_commonName;
		m_email = p_email;
		m_permissions = p_permissions;
	}

	
	public ChatOperator(JSONObject p_jOperator) throws JSONException {
		m_token = p_jOperator.getString("oprtoken");
		m_username = p_jOperator.getString("username");
		m_localeName = p_jOperator.getString("localename");
		m_commonName = p_jOperator.getString("commonname");
		m_email = p_jOperator.getString("email");
		m_permissions = p_jOperator.getInt("permissions");
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
}
