package com.scalior.mibewmob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.http.AndroidHttpClient;

/**
 * Description:
 * 	This is a bridge between the chat web service and the
 * 	the application.
 *  The web service is currently a JSON web service, so all
 *  of the methods here return JSONObjects. Since this class
 *  doesn't have state, all the methods are static
 *  
 *  @author ENsoesie	10/25/2013
 *
 */
public class WebServiceBridge {
	
	private static final String MIBEWMOB_RELATIVE_PATH = "mobile/index.php";
	
	/**
	 * Description:
	 * 	This is where the HTTP request is sent to the server
	 * @param serverURL: The URL where the Mibew server is hosted, e.g
	 * 				 http://www.example.com/webim
	 * @param queryList: 
	 * @return JSONObject: A JSON object that holds server information 
	 * 
	 * @throws MibewMobException 
	 * 
	 * @author ENsoesie  9/23/2013
	 */
	private static JSONObject runQuery(String serverURL, List<NameValuePair> queryList) 
			throws MibewMobException {
		
		// Check inputs
		if (serverURL == null || serverURL.isEmpty()) {
			throw new MMInvalidParamException("Invalid server URL");
		}
		
		JSONObject jRetVal = null;
		
		// Create a full URI from all the pieces.
		String requestURI = serverURL;

		// Skip all of these if there is no query list
		int queryListSize = queryList.size();
		if (queryList != null && queryListSize > 0) {
			requestURI +=  "?";

			for(int i = 0; i < queryListSize; i++) {
				requestURI += URLEncoder.encode(queryList.get(i).getName());
				if (!queryList.get(i).getValue().isEmpty()) {
					requestURI += "=" + URLEncoder.encode(queryList.get(i).getValue());
				}
				if (i < queryListSize - 1) {
					requestURI += "&";
				}
			}
		}
		
		AndroidHttpClient httpClient = AndroidHttpClient.newInstance("MibewMob");
		try {
			HttpGet request = new HttpGet(requestURI);
			request.setHeader("Content-type", "application/json");
			
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();

			InputStream inputStream = null;
			String result = null;
	
			inputStream = entity.getContent();
		    
			// JSON is UTF-8 by default
		    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
		    StringBuilder sb = new StringBuilder();

		    String line = null;
		    while ((line = reader.readLine()) != null)
		    {
		        sb.append(line + "\n");
		    }
		    result = sb.toString();
	
		    jRetVal = new JSONObject(result);
		    
		} catch (IOException e) {
			MibewMobLogger.Log("IO Error reaching server " + serverURL + "\n" + e.getMessage());
			throw new MMIOException("IO Error reaching server " + serverURL, e);
		} catch (JSONException e) {
			e.printStackTrace();
			jRetVal = null;
		} finally {
			httpClient.close();
		}

		return jRetVal;
	}

	/**
	 * @param p_url: The URL where the Mibew server is hosted, e.g
	 * 				 http://www.example.com/webim
	 * @return JSONObject: A JSON object that holds server information 
	 * 
	 * @throws MibewMobException 
	 * 
	 * @author ENsoesie  9/23/2013
	 */
	public static JSONObject validateServer(String p_url) throws MibewMobException {
		String serverURL = p_url;
		if (!serverURL.endsWith("/")) {
			serverURL += "/";
		}
		serverURL += MIBEWMOB_RELATIVE_PATH;
		
		List<NameValuePair> queryList = new ArrayList<NameValuePair>();
		queryList.add(new BasicNameValuePair("cmd", "isalive"));

		JSONObject retVal = runQuery(serverURL, queryList);

		if (retVal != null) {
			
			try {
				retVal.put("chatURL", p_url);
				retVal.put("webServiceURL", serverURL);
			} catch (JSONException e) {
				throw new RuntimeException("Failed to parse the JSON server details: " +
								e.getMessage(), e);
			}
		}
		
		return retVal;
	}
	
	/**
	 * Descriptions:
	 * 	Log in the operator to the chat site.
	 *
	 * @return JSONObject: A JSON object that holds the list of active visitors 
	 * 
	 * @throws MibewMobException 
	 * 
	 * @author ENsoesie  10/19/2013
	 */
	public static JSONObject loginOperator(String p_serverURL, String p_username, String p_password) 
			throws MibewMobException {
		List<NameValuePair> queryList = new ArrayList<NameValuePair>();
		queryList.add(new BasicNameValuePair("cmd", "login"));
		queryList.add(new BasicNameValuePair("username", p_username));
		queryList.add(new BasicNameValuePair("password", p_password));

		return runQuery(p_serverURL, queryList);
	}
	
	/**
	 * Description:
	 * 	Get the list of active visitors for a particular site.
	 * @return JSONObject: A JSON object that holds the list of active visitors 
	 * 
	 * @throws MibewMobException 
	 * 
	 * @author ENsoesie  10/19/2013
	 */
	public static JSONObject getActiveVisitors(String p_serverURL, String p_oprtoken, String activeVisitors)
			throws MibewMobException {
		List<NameValuePair> queryList = new ArrayList<NameValuePair>();
		queryList.add(new BasicNameValuePair("cmd", "visitorlist"));
		queryList.add(new BasicNameValuePair("oprtoken", p_oprtoken));
		
		if (!activeVisitors.isEmpty()) {
			queryList.add(new BasicNameValuePair("activevisitors", activeVisitors));
		}

		return runQuery(p_serverURL, queryList);
	}

	/**
	 * @param p_thread: The ChatThread instance
	 * @return JSONObject: A JSON object that holds the returned results 
	 * 
	 * @throws MibewMobException 
	 * 
	 * @author ENsoesie  10/22/2013
	 */
	public static JSONObject startChatWithGuest(String p_serverURL, String p_oprtoken, int p_threadid)
			throws MibewMobException {
		List<NameValuePair> queryList = new ArrayList<NameValuePair>();
		queryList.add(new BasicNameValuePair("cmd", "startchat"));
		queryList.add(new BasicNameValuePair("oprtoken", p_oprtoken));
		queryList.add(new BasicNameValuePair("threadid", Integer.toString(p_threadid)));

		return runQuery(p_serverURL, queryList);
	}

	
	/**
	 * @param p_thread: The ChatThread instance
	 * @return JSONObject: A JSON object that holds the returned results 
	 * 
	 * @throws MibewMobException 
	 * 
	 * @author ENsoesie  10/22/2013
	 */
	public static JSONObject viewThread(String p_serverURL, String p_oprtoken, int p_threadid)
			throws MibewMobException {
		List<NameValuePair> queryList = new ArrayList<NameValuePair>();
		queryList.add(new BasicNameValuePair("cmd", "startchat"));
		queryList.add(new BasicNameValuePair("oprtoken", p_oprtoken));
		queryList.add(new BasicNameValuePair("threadid", Integer.toString(p_threadid)));
		queryList.add(new BasicNameValuePair("viewonly", "true"));

		return runQuery(p_serverURL, queryList);
	}
	

	/**
	 * @param p_thread: The ChatThread instance
	 * @return JSONObject: A JSON object that holds the returned results 
	 * 
	 * @throws MibewMobException 
	 * 
	 * @author ENsoesie  10/22/2013
	 */
	public static JSONObject getNewMessagesFromServer(String p_serverURL, String p_oprtoken, 
			int p_threadid, int p_chattoken) throws MibewMobException {
		List<NameValuePair> queryList = new ArrayList<NameValuePair>();
		queryList.add(new BasicNameValuePair("cmd", "newmessages"));
		queryList.add(new BasicNameValuePair("oprtoken", p_oprtoken));
		queryList.add(new BasicNameValuePair("threadid", Integer.toString(p_threadid)));
		queryList.add(new BasicNameValuePair("token", Integer.toString(p_chattoken)));

		return runQuery(p_serverURL, queryList);
	}

	/**
	 * @param p_thread: The ChatThread instance
	 * @param p_message: The message to be sent
	 * @return JSONObject: A JSON object that holds the returned results 
	 * 
	 * @throws MibewMobException 
	 * 
	 * @author ENsoesie  10/24/2013
	 */
	public static JSONObject sendMessage(String p_serverURL, String p_oprtoken, int p_threadid,
			int p_chattoken, int p_messageid, String p_message) throws MibewMobException {
		List<NameValuePair> queryList = new ArrayList<NameValuePair>();
		queryList.add(new BasicNameValuePair("cmd", "postmessage"));
		queryList.add(new BasicNameValuePair("oprtoken", p_oprtoken));
		queryList.add(new BasicNameValuePair("threadid", Integer.toString(p_threadid)));
		queryList.add(new BasicNameValuePair("token", Integer.toString(p_chattoken)));
		queryList.add(new BasicNameValuePair("messageidl", Integer.toString(p_messageid)));
		queryList.add(new BasicNameValuePair("message", p_message));

		return runQuery(p_serverURL, queryList);
	}


	/**
	 * @param p_serverURL: The server URL
	 * @param p_oprtoken: The operator token
	 * @param p_messageIDs: The comma-separated list of message ids
	 * @return JSONObject: A JSON object that holds the returned results 
	 * 
	 * @throws MibewMobException 
	 * 
	 * @author ENsoesie  11/6/2013
	 */
	public static JSONObject acknowledgeMessages(String p_serverURL, String p_oprtoken,
			String p_messageIDs) throws MibewMobException {
		List<NameValuePair> queryList = new ArrayList<NameValuePair>();
		queryList.add(new BasicNameValuePair("cmd", "ack-messages"));
		queryList.add(new BasicNameValuePair("oprtoken", p_oprtoken));
		queryList.add(new BasicNameValuePair("messageids", p_messageIDs));

		return runQuery(p_serverURL, queryList);
	}


	/**
	 * @param p_serverURL: The server URL
	 * @param p_oprtoken: The operator token
	 * @param p_threadID: The id of the thread to close
	 * @return JSONObject: A JSON object that holds the returned results 
	 * 
	 * @throws MibewMobException 
	 * 
	 * @author ENsoesie  11/13/2013
	 */
	public static JSONObject closeThread(String p_serverURL, String p_oprtoken,
			int p_threadID) throws MibewMobException {
		List<NameValuePair> queryList = new ArrayList<NameValuePair>();
		queryList.add(new BasicNameValuePair("cmd", "closethread"));
		queryList.add(new BasicNameValuePair("oprtoken", p_oprtoken));
		queryList.add(new BasicNameValuePair("threadid", Integer.toString(p_threadID)));

		return runQuery(p_serverURL, queryList);
	}
}
