package com.scalior.mibewmob.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ChatService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_REDELIVER_INTENT;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
