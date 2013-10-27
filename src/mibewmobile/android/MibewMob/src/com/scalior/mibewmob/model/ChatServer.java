package com.scalior.mibewmob.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatServer {
	private String m_name;
	private String m_URL;
	private String m_version;
	private String m_logoURL;
	private String m_mibewMobVersion;
	private String m_webServiceURL;
	private long m_ID;
	
	public ChatServer (String p_name, String p_URL, String p_version,
					   String p_logoURL, String p_mibewMobVersion,
					   String p_webServiceURL) {
		m_name = p_name;
		m_URL = p_URL;
		m_version = p_version;
		m_logoURL = p_logoURL;
		m_mibewMobVersion = p_mibewMobVersion;
		m_webServiceURL = p_webServiceURL;
	}
	
	public ChatServer (JSONObject p_serverDetails) {
		try {
			m_name = p_serverDetails.optString("name");
			m_URL = p_serverDetails.getString("chatURL");
			m_version = p_serverDetails.getString("version");
			m_logoURL = p_serverDetails.optString("logoURL");
			m_mibewMobVersion = p_serverDetails.getString("mibewMobVersion");
			m_webServiceURL = p_serverDetails.getString("webServiceURL");
		} catch (JSONException e) {
			throw new RuntimeException("Failed to parse the JSON server details: " +
							e.getMessage(), e);
		}
	}
	// Getters and Setters
	public String getName() {
		return m_name;
	}
	public void setName(String p_name) {
		m_name = p_name;
	}

	public String getURL() {
		return m_URL;
	}
	public void setURL(String p_URL) {
		m_URL = p_URL;
	}

	public String getVersion() {
		return m_version;
	}
	public void setVersion(String p_version) {
		m_version = p_version;
	}

	public String getLogoURL() {
		return m_logoURL;
	}
	public void setLogoURL(String p_logoURL) {
		m_logoURL = p_logoURL;
	}

	public String getMibewMobVersion() {
		return m_mibewMobVersion;
	}
	public void setMibewMobVersion(String p_mibewMobVersion) {
		m_mibewMobVersion = p_mibewMobVersion;
	}

	public String getWebServiceURL() {
		return m_webServiceURL;
	}

	public void setWebServiceURL(String webServiceURL) {
		m_webServiceURL = webServiceURL;
	}

	public long getID() {
		// TODO Auto-generated method stub
		return m_ID;
	}

	public void setID(long iD) {
		m_ID = iD;
	}

}
