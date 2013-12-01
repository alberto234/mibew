package com.scalior.mibewmob;

import android.app.Application;

public class MibewMobApplication extends Application {

	private Thread.UncaughtExceptionHandler m_defaultUEHandler;
	
	private Thread.UncaughtExceptionHandler m_UEHandler = 
			new Thread.UncaughtExceptionHandler() {
				
				@Override
				public void uncaughtException(Thread thread, Throwable ex) {
		
					// Log the exception and proceed with the default handler
					MibewMobLogger.Log("Uncaught Exception", ex);
					m_defaultUEHandler.uncaughtException(thread, ex);
				}
	};
			

	@Override
	public void onCreate() {
		super.onCreate();
		
		// Capture the default unhandled exception handler, and swap it with ours
		m_defaultUEHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(m_UEHandler);
	}
}
