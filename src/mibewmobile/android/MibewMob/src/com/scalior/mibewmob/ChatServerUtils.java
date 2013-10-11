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

import com.scalior.mibewmob.PreferenceUtils.PreferenceDefs;

import android.content.Context;
import android.net.http.AndroidHttpClient;


/* This is a utility class that enables communication between
 * the chat server and the application
 * 
 * Author: ENsoesie		9/23/2013
 */
public class ChatServerUtils {
	private final String MIBEWMOB_RELATIVE_PATH = "mobile/index.php";
	private String m_chatServerURL;
	private Context m_context;
	private boolean m_bRefreshServerList;

	public static final String CHAT_SERVER_ADDED = "chat_server_added";
	
	// For singleton pattern
	private static ChatServerUtils m_sInstance = null;
	private ChatServerUtils(Context p_context) {
		m_context = p_context;
		m_bRefreshServerList = false;
		
		// Attempt to load relevant chat preferences
		m_chatServerURL = (String) PreferenceUtils.getInstance().
							getPreference(m_context, PreferenceDefs.MIBEWMOB_SERVER_URL);
	};
	public static ChatServerUtils getInstance(Context p_context) {
		if (m_sInstance == null) {
			m_sInstance = new ChatServerUtils(p_context);
		}
		return m_sInstance;
	}
	
	
	/**
	 * @param p_url: The URL where the Mibew server is hosted, e.g
	 * 				 http://www.example.com/webim
	 * @return JSONObject: A JSON object that holds server information 
	 * 
	 * @author ENsoesie  9/23/2013
	 */
	public JSONObject validateServer(String p_url) {
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*
			m_chatServerURL = serverURL;
			// Save the preference
			PreferenceUtils.getInstance().setPreference(m_context,
														PreferenceDefs.MIBEWMOB_SERVER_URL,
														m_chatServerURL);*/
		}
		
		return retVal;
	}
	
	public JSONObject loginOperator(String p_serverURL, String p_username, String p_password) {
		List<NameValuePair> queryList = new ArrayList<NameValuePair>();
		queryList.add(new BasicNameValuePair("cmd", "login"));
		queryList.add(new BasicNameValuePair("username", p_username));
		queryList.add(new BasicNameValuePair("password", p_password));

		return runQuery(p_serverURL, queryList);
	}
	
	private JSONObject runQuery(String serverURL, List<NameValuePair> queryList) {
		
		// Check inputs
		if (serverURL == null || serverURL.isEmpty()) {
			return null;
			// TODO: implement exception-based error handling
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			jRetVal = null;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			jRetVal = null;
		}
		
		return jRetVal;
	}
	
	
	public boolean getRefreshServerList() {
		return m_bRefreshServerList;
	}
	
	public void setRefreshServerList(boolean p_bRefreshServerList) {
		m_bRefreshServerList = p_bRefreshServerList;
	}
}
