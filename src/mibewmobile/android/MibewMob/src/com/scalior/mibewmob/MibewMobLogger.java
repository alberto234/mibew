package com.scalior.mibewmob;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;


public class MibewMobLogger {
	private static final String LOGFILENAME = "mibewmoblog.txt";

	private static MibewMobLogger m_sInstance = null;
	private static Context m_sContext = null;
	
	private BufferedWriter m_logWriter;
	private MibewMobLogger() {
		try {
			m_logWriter = new BufferedWriter(new FileWriter(
					new File(m_sContext.getExternalFilesDir(null), LOGFILENAME), true));
		} catch (Exception e) {
			// There shall be no logging for this session
			m_logWriter = null;
		}
	}


	private static MibewMobLogger getInstance() {
		if (m_sInstance == null) {
			m_sInstance = new MibewMobLogger();
		}

		return m_sInstance;
	}
	
	public static synchronized void Log(String p_text) {
		try {
			String timestamp = new SimpleDateFormat("yyyy-MM-dd H:m:s.S", Locale.US).format(Calendar.getInstance().getTime());
			String logLine = timestamp + 
					"\tThread: " + Thread.currentThread().getId() + 
					" -- " + p_text;
			
			BufferedWriter logWriter = getInstance().getWriter();
			logWriter.write(logLine);
			logWriter.newLine();
			logWriter.flush();
		} catch (Exception e) {
			// Do nothing
			e.printStackTrace();
		}
	}


	private BufferedWriter getWriter() {
		return m_logWriter;
	}


	public static void setContext(Context context) {
		m_sContext = context;
	}


	public static void Log(String p_message, Throwable p_exception) {
		StringWriter stringWriter = new StringWriter();
		stringWriter.write(p_message + "\n");
		p_exception.printStackTrace(new PrintWriter(stringWriter));
		
		Log(stringWriter.toString());
	}
}
