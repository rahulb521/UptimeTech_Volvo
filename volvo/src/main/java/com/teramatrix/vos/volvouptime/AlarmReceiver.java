package com.teramatrix.vos.volvouptime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.teramatrix.vos.firebase.config.Config;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //call the method here
        Intent i = context.getPackageManager().getLaunchIntentForPackage("com.teramatrix.vos");
        Config.is24Hrs = true;
        context.startActivity(i);

    }

}
