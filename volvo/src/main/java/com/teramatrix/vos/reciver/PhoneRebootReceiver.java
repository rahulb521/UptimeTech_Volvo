package com.teramatrix.vos.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.teramatrix.vos.service.Locationservice;

public class PhoneRebootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i("PhoneRebootReceiver","onReceive");
		// start tracking
		Intent mServiceIntent = new Intent(context, Locationservice.class);
		context.startService(mServiceIntent);
	}

}
