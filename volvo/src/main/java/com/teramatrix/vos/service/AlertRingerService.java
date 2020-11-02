package com.teramatrix.vos.service;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

import com.teramatrix.vos.utils.UtilityFunction;

/**
 * This AlertRingerService class is used for ringer when ticket show on device
 * and call every time with in 30 sec. when ticket accept or decline.
 * 
 */
public class AlertRingerService extends Service {

	// defining instance of ringtone class
	private Ringtone r;

	/*
	 * defining constructor of the class without paramater
	 */
	public AlertRingerService() {
		// TODO Auto-generated constructor stub
		// super(AlertRingerService.class.getName());
	}

	/**
	 * onStartCommand() is called every time a starts the service using
	 * startService(Intent intent). This means that onStartCommand() can get
	 * called multiple times. You should do the things in this method that are
	 * needed each time a ticket receive from your service. This depends a lot
	 * on what your service does and how it communicates with the device
	 * tickets.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_ALARM);
		// alert is null, using backup
		r = RingtoneManager.getRingtone(getApplicationContext(), notification);

		if (r != null) {
			Timer timer = new Timer();
			TimerTask timerTask = new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					// start the ringer
					r.play();

					try {

						// wait ringer for 30 sec.
						Thread.sleep(1000 * 30);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block

						
						// save log when exception is created
						UtilityFunction
								.saveErrorLog(getApplicationContext(), e);
					}

					// check ringer isPlaying or not

					if (r.isPlaying())
						r.stop();
					stopSelf();
				}
			};
			timer.schedule(timerTask, 0);
		}

		return START_STICKY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (r != null) {
			r.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
