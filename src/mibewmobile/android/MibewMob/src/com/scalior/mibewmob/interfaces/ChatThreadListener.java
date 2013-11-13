package com.scalior.mibewmob.interfaces;

import java.util.List;

import com.scalior.mibewmob.model.ChatMessage;

/**
 * Description:
 * 		This interface is to be implemented by anyone who wants to 
 * 		listen for when there are new active visitors
 * 
 * @author Eyong Nsoesie - 10/26/2013
 * 
 */
public interface ChatThreadListener {
	public void onNewMessages(final List<ChatMessage> p_newMessages);
}
