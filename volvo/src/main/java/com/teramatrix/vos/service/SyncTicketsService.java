package com.teramatrix.vos.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.ticketmanager.MyTicketManager;
import com.teramatrix.vos.utils.ApplicationConstant;


/**
 * 
 *This SyncTicketsService service used in background and calling when new updates on devices.
 */
public class SyncTicketsService extends Service {

	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub

		//calling service in backgorund SyncInBackground
		new SyncInBackground(getApplicationContext(), this).execute();

		return super.onStartCommand(intent, flags, startId);
	}

	
	//This class is used to call service when updates on server. This is run in background.
	class SyncInBackground extends AsyncTask<Void, Void, Void> {
		
		//define context instance of a class
		private Context mContext;
		//define sevice instace of a class
		private Service mService;
		
		//calling and define constructor in class SyncInBackground

		public SyncInBackground(Context mContext, Service mService) {
			// TODO Auto-generated constructor stub
			this.mContext = mContext;
			this.mService = mService;
		}

		/*
		 * (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			MyTicketManager myTicketManager = MyTicketManager
					.getInstance(mContext);
			myTicketManager.api_SyncDbOnUpdates();

//			if (ApplicationConstant.IS_APP_IN_FORGROUND)
				MyTicketActivity.ifNewUpdatesAvailableInDb = true;

			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Void result) 
		{
			// TODO Auto-generated method stub
			
			super.onPostExecute(result);
			//check if application IS_APP_IN_FORGROUND or not and check isMyTicketActivityOpened or not ,check instance of the activity MyTicketActivity
			if (ApplicationConstant.IS_APP_IN_FORGROUND
					&& MyTicketActivity.isMyTicketActivityOpened
					&& (ApplicationConstant.currentActivityContext instanceof MyTicketActivity)) {
				MyTicketActivity myTicketActivity = (MyTicketActivity) ApplicationConstant.currentActivityContext;
				
				//loading updated tickets check
				myTicketActivity.loadUpdatedTickets();
			}else {
				if (ApplicationConstant.IS_APP_IN_FORGROUND){
					Intent intent=new Intent(UPDATEBROADCAST);
					mContext.sendBroadcast(intent);
				}
			}

			if (mService != null)
				mService.stopSelf();
		}
	}
	public static final String UPDATEBROADCAST="com.vas.van.UPDATETICKET";

}
