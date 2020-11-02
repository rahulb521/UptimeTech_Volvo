package com.teramatrix.vos.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.teramatrix.vos.service.AlertRingerService;
/**
 * NewTicketAlertRinger
 * A broadcast receiver is an Android component which allows you to register for system or application events.
 *  All registered receivers for an event are notified by the Android runtime once this event happens.
 * *
 */
public class NewTicketAlertRinger extends BroadcastReceiver {

	public static final int REQUEST_CODE = 1703;

	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
        //AlertRingerService class to start the service with instance object and calling startService method.
		Intent mServiceIntent = new Intent(context, AlertRingerService.class);
		
		//start the service 
		context.startService(mServiceIntent);

	}
	

}
