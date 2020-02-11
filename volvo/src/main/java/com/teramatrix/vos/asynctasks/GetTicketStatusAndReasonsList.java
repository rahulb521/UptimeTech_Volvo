package com.teramatrix.vos.asynctasks;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.R;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.database.DBInteraction;
import com.teramatrix.vos.jsonparser.TicketJsonParser;
import com.teramatrix.vos.model.TicketStatus;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.ticketmanager.MyTicketManager;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.UtilityFunction;
import com.teramatrix.vos.volvouptime.UpTimeVehicleListActivity;

/**
 * This GetTicketStatusAndReasonsList class used for getting all the reasons
 * with using different-2 ids.
 * 
 * Save all response data to local db in respective tables
 * (TicketStatus,TicketActionReasons,RepairingEstimationCost)
 * 
 * @author Gaurav.Mangal
 * 
 */
public class GetTicketStatusAndReasonsList extends AsyncTask<Void, Void, Void> {

	// Defining Context for this class
	private Context mContext;

	// Define ProgressDialog for this class
	private ProgressDialog mProgressDialog;

	// Define RestIntraction for this class
	private RestIntraction restIntraction;
	private boolean isNewGcmIdSetup;

	/**
	 * 
	 * @param mContext
	 */
	public GetTicketStatusAndReasonsList(Context mContext,
			boolean isNewGcmIdSetup) {
		// TODO Auto-generated constructor stub
		this.mContext = mContext;
		this.isNewGcmIdSetup = isNewGcmIdSetup;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		mProgressDialog = ProgressDialog.show(mContext, "", mContext
				.getResources().getString(R.string.sync_data), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		try {

			// 1. get Ticket statuses list from server
			restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS()+""+ApiUrls.TICKET_STATUS_LIST);
			restIntraction.AddParam("Token",
					new VECVPreferences(mContext).getSecurityToken());

			restIntraction.toString();
			restIntraction.Execute(0);
			String response = restIntraction.getResponse();
			ArrayList<TicketStatus> arrayList = TicketJsonParser
					.getTicketStatus(response);

			if (arrayList != null && arrayList.size() > 0) {
				DBInteraction dbInteraction = DBInteraction
						.getInstance(mContext);
				dbInteraction.getConnection();
				for (int i = 0; i < arrayList.size(); i++) {
					TicketStatus status = arrayList.get(i);

					dbInteraction.fillStatusTable(
							status.id, status.statusName,
							status.alias);

				}
				dbInteraction.closeConnection();
			}

			// 2. get Ticket action reasons list from server
			MyTicketManager.getInstance(mContext)
					.downloadDeclineTicketReasonListFromServer();

			// 3. Get Estiamtion cost list from server
			MyTicketManager.getInstance(mContext)
					.downloadEstimatedCostListFromServer();

			// 4. update timestamp value from server
			DBInteraction dbInteraction = DBInteraction.getInstance(mContext);
			dbInteraction.getConnection();
			long i = dbInteraction.updateTimeStamp(true,
					ApplicationConstant.FIRST_SYNC_TIMESTAMP);
			String lastTimestamp = dbInteraction.getLastTimestap();
			dbInteraction.closeConnection();

			// 5. Synchronizing exsisting opened tickets and closed tickets from
			// server
			MyTicketManager myTicketManager = MyTicketManager
					.getInstance(mContext);

			/**
			 * All opened and history tickets will be fetched from server and
			 * saved in local db. timestap for this thime will be 1 JAN 1970
			 * 00:00:00.
			 */
			myTicketManager.api_SyncDbOnInstallation();

		} catch (Exception ex) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(ex);
			// save error location in file
			UtilityFunction.saveErrorLog(mContext, ex);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();

		new VECVPreferences(mContext).setTicketStatusFill(true);

		//Launch Home Screen
		Intent myTicket_Intent = new Intent(mContext, MyTicketActivity.class);
		//Intent myTicket_Intent = new Intent(mContext, UpTimeVehicleListActivity.class);
		myTicket_Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mContext.startActivity(myTicket_Intent);
		((Activity) mContext).finish();

	}
}
