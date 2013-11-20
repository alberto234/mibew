package com.scalior.mibewmob;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ChatAvailability {
	public static final int STATE_AVAILABLE = 1;
	public static final int STATE_UNAVAILABLE = 2;
	
	// For testing
	//private static int m_sState = STATE_AVAILABLE;

	private Context m_context;
	private int m_state;
	private long m_nextTriggerMillis;
	private boolean m_bInit;
	
	public ChatAvailability(Context p_context) {
		m_context = p_context;
		m_bInit = false;
		
		m_state = 0;
		m_nextTriggerMillis = 0;
	}
	
	public void computeStateAndNextTrigger() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(m_context);
		Calendar fromTime = Calendar.getInstance();
		Calendar toTime = Calendar.getInstance();
		Calendar currTime = Calendar.getInstance();
		Calendar tempTime = Calendar.getInstance();

		String timestamp = new SimpleDateFormat("yyyy-MM-dd H:m:s", Locale.US).format(tempTime.getTime());

		tempTime.setTimeInMillis(prefs.getLong("available_from", 50400000));
		timestamp = new SimpleDateFormat("yyyy-MM-dd H:m:s", Locale.US).format(tempTime.getTime());
		fromTime.set(Calendar.HOUR_OF_DAY, tempTime.get(Calendar.HOUR_OF_DAY));
		fromTime.set(Calendar.MINUTE, tempTime.get(Calendar.MINUTE));
		
		timestamp = new SimpleDateFormat("yyyy-MM-dd H:m:s", Locale.US).format(fromTime.getTime());

		tempTime.setTimeInMillis(prefs.getLong("available_to", 79200000));
		timestamp = new SimpleDateFormat("yyyy-MM-dd H:m:s", Locale.US).format(tempTime.getTime());
		toTime.set(Calendar.HOUR_OF_DAY, tempTime.get(Calendar.HOUR_OF_DAY));
		toTime.set(Calendar.MINUTE, tempTime.get(Calendar.MINUTE));

		timestamp = new SimpleDateFormat("yyyy-MM-dd H:m:s", Locale.US).format(toTime.getTime());

		timestamp = new SimpleDateFormat("yyyy-MM-dd H:m:s", Locale.US).format(currTime.getTime());

		if (currTime.getTimeInMillis() < fromTime.getTimeInMillis()) {
			m_state = STATE_UNAVAILABLE;
			m_nextTriggerMillis = fromTime.getTimeInMillis();
		} else if (currTime.getTimeInMillis() >= toTime.getTimeInMillis()) {
			m_state = STATE_UNAVAILABLE;
			fromTime.add(Calendar.DATE, 1);
			m_nextTriggerMillis = fromTime.getTimeInMillis();
		} else if (currTime.getTimeInMillis() >= fromTime.getTimeInMillis()) {
			m_state = STATE_AVAILABLE;
			m_nextTriggerMillis = toTime.getTimeInMillis();
		}
		
		m_bInit = true;
	}
	
	
	public int getCurrentState() {
		/* Toggle
		if (m_sState == STATE_AVAILABLE) {
			m_sState = STATE_UNAVAILABLE;
		} else {
			m_sState = STATE_AVAILABLE;
		}
		return m_sState;*/
		
		if (!m_bInit) {
			// Throw illegal sate exception
		}
		
		return m_state;
	}
	
	public long getNextTriggerMillis() {
		if (!m_bInit) {
			// Throw illegal state exception
		}
		return m_nextTriggerMillis;
	}
}
