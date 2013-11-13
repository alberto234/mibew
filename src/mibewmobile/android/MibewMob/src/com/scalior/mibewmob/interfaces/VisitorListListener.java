package com.scalior.mibewmob.interfaces;

import java.util.List;

import com.scalior.mibewmob.model.ChatThread;

/**
 * Description:
 * 		This interface is to be implemented by anyone who wants to 
 * 		listen for when there are new active visitors
 * 
 * @author Eyong Nsoesie - 10/26/2013
 * 
 */
public interface VisitorListListener {
	public void onUpdateList(final List<ChatThread> p_activeVisitors);
}
