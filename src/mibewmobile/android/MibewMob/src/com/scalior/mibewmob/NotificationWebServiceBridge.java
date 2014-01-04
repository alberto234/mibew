package com.scalior.mibewmob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
//import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Description:
 * 	This is a bridge between the notification web service and the
 * 	the application.
 *  The web service is currently a JSON web service, so all
 *  of the methods here return JSONObjects. Since this class
 *  doesn't have state, all the methods are static
 *  
 *  @author ENsoesie	12/23/2013
 *
 */
public class NotificationWebServiceBridge {
	
	private static final String NOTIFICATION_SERVER_URL = "http://nsoesie.dyndns-home.com:5229/mibewmob-server-web";
	
	private static final int HTTP_METHOD_GET 	= 1;
	private static final int HTTP_METHOD_POST 	= 2;

	/**
	 * Description:
	 * 	This converts the name-value pairs to a string.
	 * @param queryList: 
	 * @return JSONObject: A JSON object that holds server return 
	 * 
	 * @throws MibewMobException
	 * 
	 * @author ENsoesie  12/23/2013
	 */
	private static String getQuery(List<NameValuePair> queryList) 
			throws MibewMobException {
		
		// Check inputs
		if (queryList == null || queryList.size() == 0) {
			return null;
		}
		
		StringBuilder sbQuery =  new StringBuilder();

		try {
			for(int i = 0; i < queryList.size(); i++) {
				sbQuery.append(URLEncoder.encode(queryList.get(i).getName(), "UTF-8"));
				if (!queryList.get(i).getValue().isEmpty()) {
					sbQuery.append("=").append(URLEncoder.encode(queryList.get(i).getValue(), "UTF-8"));
				}
				if (i < queryList.size() - 1) {
					sbQuery.append("&");
				}
			}
			return sbQuery.toString();
		} catch (UnsupportedEncodingException e) {
			throw new MibewMobException("Error encoding a parameter", e);
		}
	}

	
	/**
	 * Description:
	 * 	This is where the HTTP GET request is sent to the server
	 * @param queryList: 
	 * @return JSONObject: A JSON object that holds server return 
	 * 
	 * @throws MibewMobException
	 * 
	 * @author ENsoesie  12/23/2013
	 */
	private static JSONObject runGetQuery(String p_resource, List<NameValuePair> queryList) 
			throws MibewMobException {
		
		// Check inputs
		if (p_resource == null || p_resource.isEmpty()) {
			throw new MMInvalidParamException("Invalid resource");
		}
		
		if (queryList == null || queryList.size() == 0) {
			throw new MMInvalidParamException("Invalid query");
		}

		JSONObject jRetVal = null;
		
		StringBuilder sbRequestURL =  new StringBuilder(NOTIFICATION_SERVER_URL);
		sbRequestURL.append(p_resource).append("?").append(getQuery(queryList));

		HttpURLConnection connection = null;
		String result = null;
		try {
			URL url = new URL(sbRequestURL.toString());
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty("Content-type", "application/json");
			connection.setRequestProperty("charset", "UTF-8");
			
			// JSON is UTF-8 by default
		    BufferedReader reader = new BufferedReader(
		    		new InputStreamReader(connection.getInputStream(), "UTF-8"), 8);
		    StringBuilder sb = new StringBuilder();

		    String line = null;
		    while ((line = reader.readLine()) != null)
		    {
		        sb.append(line + "\n");
		    }
		    result = sb.toString();
		    jRetVal = new JSONObject(result);
		    
		} catch (IOException e) {
			throw new MMIOException("IO Error reaching server " + NOTIFICATION_SERVER_URL, e);
		} catch (JSONException e) {
			throw new RuntimeException("Failed to convert response to JSON: \nResponse: " + result +
									   "\nDetails: " + e.getMessage(), e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return jRetVal;
	}

	/**
	 * Description:
	 * 	This is where the HTTP GET request is sent to the server
	 * @param queryList: 
	 * @return JSONObject: A JSON object that holds server return 
	 * 
	 * @throws MibewMobException
	 * 
	 * @author ENsoesie  12/23/2013
	 */
	private static JSONObject runQuery(String p_resource, List<NameValuePair> queryList, 
			int p_httpMethod, String p_data) throws MibewMobException {
		
		// Check inputs
		if (p_resource == null || p_resource.isEmpty()) {
			throw new MMInvalidParamException("Invalid resource");
		}
		
		JSONObject jRetVal = null;
		
		StringBuilder sbRequestURL =  new StringBuilder(NOTIFICATION_SERVER_URL);
		sbRequestURL.append(p_resource);
		
		if (queryList != null && queryList.size() != 0) {
			sbRequestURL.append("?").append(getQuery(queryList));
		}

		HttpURLConnection connection = null;
		String result = null;
		try {
			URL url = new URL(sbRequestURL.toString());
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty("Content-type", "application/json");
			connection.setRequestProperty("charset", "UTF-8");
			
			if (p_httpMethod == HTTP_METHOD_POST) {
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);

				if (p_data != null && !p_data.isEmpty()) {
					OutputStream os = connection.getOutputStream();
					BufferedWriter writer = new BufferedWriter(
					        new OutputStreamWriter(os, "UTF-8"));
					writer.write(p_data);
					writer.flush();
					writer.close();
					os.close();
				}
				
				connection.connect();

			}
		
			// JSON is UTF-8 by default
		    BufferedReader reader = new BufferedReader(
		    		new InputStreamReader(connection.getInputStream(), "UTF-8"), 8);
		    StringBuilder sb = new StringBuilder();

		    String line = null;
		    while ((line = reader.readLine()) != null)
		    {
		        sb.append(line + "\n");
		    }
		    result = sb.toString();
		    jRetVal = new JSONObject(result);
		    
		} catch (IOException e) {
			throw new MMIOException("IO Error reaching server " + NOTIFICATION_SERVER_URL, e);
		} catch (JSONException e) {
			throw new RuntimeException("Failed to convert response to JSON: \nResponse: " + result +
									   "\nDetails: " + e.getMessage(), e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return jRetVal;
	}

	/**
	 * Description:
	 * 	This is where the HTTP POST request is sent to the server
	 * @param queryList: 
	 * @return JSONObject: A JSON object that holds server return 
	 * 
	 * @throws MibewMobException
	 * 
	 * @author ENsoesie  12/23/2013
	 */
	private static JSONObject runPostQuery(String p_resource, List<NameValuePair> p_queryList,
			String p_data) throws MibewMobException {
		
		// Check inputs
		if (p_resource == null || p_resource.isEmpty()) {
			throw new MMInvalidParamException("Invalid resource");
		}
		if (p_queryList == null || p_queryList.size() == 0) {
			throw new MMInvalidParamException("Invalid query");
		}
		
		JSONObject jRetVal = null;
		
		StringBuilder sbRequestURL =  new StringBuilder(NOTIFICATION_SERVER_URL);
		sbRequestURL.append(p_resource);

		HttpURLConnection connection = null;
		String result = null;
		try {
			URL url = new URL(sbRequestURL.toString());
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty("Content-type", "application/json");
			connection.setRequestProperty("charset", "UTF-8");
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);

			OutputStream os = connection.getOutputStream();
			BufferedWriter writer = new BufferedWriter(
			        new OutputStreamWriter(os, "UTF-8"));
			writer.write(p_data);
			writer.flush();
			writer.close();
			os.close();

			connection.connect();
			
			// JSON is UTF-8 by default
		    BufferedReader reader = new BufferedReader(
		    		new InputStreamReader(connection.getInputStream(), "UTF-8"), 8);
		    StringBuilder sb = new StringBuilder();

		    String line = null;
		    while ((line = reader.readLine()) != null)
		    {
		        sb.append(line + "\n");
		    }
		    result = sb.toString();
		    jRetVal = new JSONObject(result);
		    
		} catch (IOException e) {
			throw new MMIOException("IO Error reaching server " + NOTIFICATION_SERVER_URL, e);
		} catch (JSONException e) {
			throw new RuntimeException("Failed to convert response to JSON: \nResponse: " + result +
									   "\nDetails: " + e.getMessage(), e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return jRetVal;
	}

	/**
	 * Description:
	 * 	This posts the list of active visitors to the notification server
	 * @param jOprActiveVisitors
	 * @return
	 * @throws MibewMobException
	 */
	public static JSONObject setActiveVisitors(JSONArray jOprActiveVisitors) throws MibewMobException {
		// Check inputs
		if (jOprActiveVisitors == null) {
			throw new MMInvalidParamException("Invalid parameter");
		}
		
		return runQuery("/setactivevisitors", null, HTTP_METHOD_POST, jOprActiveVisitors.toString());
	}

	/**
	 * Description:
	 * 	This posts the operator status to the notification server
	 * @param jOprActiveVisitors
	 * @return
	 * @throws MibewMobException
	 */
	public static JSONObject setOperatorStatus(String p_notificationId, int p_operatorStatus) throws MibewMobException {
		List<NameValuePair> queryList = new ArrayList<NameValuePair>();
		queryList.add(new BasicNameValuePair("oprnotificationid", p_notificationId));
		queryList.add(new BasicNameValuePair("oprstatus", Integer.toString(p_operatorStatus)));

		return runQuery("/setoperatorstate", queryList, HTTP_METHOD_POST, null);
	}


	/**
	 * Description:
	 * 	This registers this operator to the notification server
	 * @return
	 * @throws MibewMobException
	 */
	public static JSONObject register(String p_gcmRegId, String p_oprToken,
			String p_mibewmoburl) throws MibewMobException {
		List<NameValuePair> queryList = new ArrayList<NameValuePair>();
		queryList.add(new BasicNameValuePair("regid", p_gcmRegId));
		queryList.add(new BasicNameValuePair("oprtoken", p_oprToken));
		queryList.add(new BasicNameValuePair("mibewmoburl", p_mibewmoburl));
		queryList.add(new BasicNameValuePair("platform", "android"));
		
		return runQuery("/register", queryList, HTTP_METHOD_POST, null);
	}
}
