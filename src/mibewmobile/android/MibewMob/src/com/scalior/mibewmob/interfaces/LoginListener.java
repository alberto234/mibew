package com.scalior.mibewmob.interfaces;

import com.scalior.mibewmob.model.MonitoredSite;


/**
 * Description:
 * 		Login Interface
 * 
 * @author Eyong Nsoesie - 10/11/2013
 * 
 */
public interface LoginListener {
	// Login status constants
	public final static int STATUS_SUCCESS 				= 0;
	public final static int STATUS_INVALID_SERVER	 	= 1;
	public final static int STATUS_INVALID_CREDENTIALS 	= 2;
	public final static int STATUS_INVALID_PARAMETERS 	= 3;
	public final static int STATUS_SERVER_NOT_READY 	= 4;
	public final static int STATUS_MALFORMED_RESPONSE 	= 5;
	public final static int STATUS_DATABASE_ERROR	 	= 6;
	
	// Status 
	public void OnLoginComplete(int p_status, int p_monitoredListIndex);

	public void OnLoginComplete(int p_status, MonitoredSite p_site);
}
