package com.scalior.mibewmob.model;

public class MonitoredSite {
	private ChatServer m_server;
	private ChatOperator m_operator;
	
	// Constructors
	public MonitoredSite(ChatServer p_server, ChatOperator p_operator) {
		m_server = p_server;
		m_operator = p_operator;
	}
	
	// Getters and setters
	public ChatServer getServer() {
		return m_server;
	}
	public void setServer(ChatServer server) {
		m_server = server;
	}
	public ChatOperator getOperator() {
		return m_operator;
	}
	public void setOperator(ChatOperator operator) {
		m_operator = operator;
	}
}
