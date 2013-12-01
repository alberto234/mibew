package com.scalior.mibewmob;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;

public class ExceptionHandler implements UncaughtExceptionHandler {

	private Context m_context;
	
	
	public ExceptionHandler(Context p_context) {
		m_context = p_context;
	}

	
	@Override
	public void uncaughtException(Thread p_thread, Throwable p_exception) {
		
		// Log the exception
		MibewMobLogger.Log("Uncaught Exception", p_exception);
		
		//Intent crashIntent = new Intent(m_context, CrashActivity.class);
		//m_context.startActivity(crashIntent);
		
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(10);

	}
}
