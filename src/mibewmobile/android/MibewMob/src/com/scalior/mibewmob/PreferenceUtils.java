package com.scalior.mibewmob;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtils {
	// For singleton pattern
	private static PreferenceUtils m_sInstance = null;
	private PreferenceUtils() { };
	public static PreferenceUtils getInstance() {
		if (m_sInstance == null) {
			m_sInstance = new PreferenceUtils();
		}
		return m_sInstance;
	}
	
	// Enumeration of preferences used in the app
	public enum PreferenceDefs {
		MIBEWMOB_SERVER_URL ("mibewmobServerURL"),
		MIBEWMOB_SERVER_VERSION ("mibewmobServerVersion"),
		MIBEW_SERVER_VERSION ("mibewServerVersion");
		
		private PreferenceDefs(final String text) {
			this.text = text;
		}
		
		public String toString() {
			return text;
		}
		
		private final String text;
	}

	/**
	 * 
	 * @param p_preferenceName: the name of the preference to set
	 * @param value: the value to set.
	 * @return true or false if the preference was successfully set
	 * 
	 * @author ENsoesie	9/23/2013
	 * 
	 * Comments: The preference name determines the Object type
	 */
	public Boolean setPreference(Context p_context, PreferenceDefs p_preferenceName, Object p_value) {
		Boolean bRetVal = true;
		SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(p_context).edit();

		switch(p_preferenceName) {
		case MIBEWMOB_SERVER_URL:
			prefsEditor.putString(PreferenceDefs.MIBEWMOB_SERVER_URL.toString(), (String)p_value);
			break;
		default:
			bRetVal = false;
		}
		
		if (bRetVal) {
			prefsEditor.commit();
		}
		
		return bRetVal;
	}
	
	public Object getPreference(Context p_context, PreferenceDefs p_preferenceName) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(p_context);
		
		switch(p_preferenceName) {
		case MIBEWMOB_SERVER_URL:
			return prefs.getString(p_preferenceName.toString(), "");
		default:
			return null;
		}
	}
}
