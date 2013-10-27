package com.scalior.mibewmob;

import com.scalior.mibewmob.services.ChatService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MibewMobServiceBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent startServiceIntent = new Intent(context, ChatService.class);
		context.startService(startServiceIntent);
	}

}
