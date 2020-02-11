package com.teramatrix.vos.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.R;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.model.Ticket;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.ticketmanager.MyTicketManager;
import com.teramatrix.vos.utils.UtilityFunction;

/*
 * This class will be used for sending updated job estimation time send to server
 */
public class UpdateJobEstimatedCostService extends AsyncTask<Void, Void, Void> {

	// define instance of the Context class
	private Context mContext;

	// define string variable of the class
	private String newEstimatedCost;

	// define instance of the RestIntraction class
	private RestIntraction restIntraction;

	// define string variable of the class
	private String ticket_id, lastmodified_time, lastmodified_by,
			editEstimationCostReasons;

	private String priority;
	// define instance of the ProgressDialog class
	private ProgressDialog pd;

	/**
	 * 
	 * @param mContext
	 * @param ticket_id
	 * @param newEstimatedHoursToCompleteJob
	 * @param lastmodified_time
	 * @param lastmodified_by
	 * @param editEstimationTimeReasons
	 */
	public UpdateJobEstimatedCostService(Context mContext, String ticket_id,
			String newEstimatedCost, String lastmodified_time,
			String lastmodified_by, String editEstimationCostReasons,String priority) {
		// TODO Auto-generated constructor stub
		this.mContext = mContext;
		this.ticket_id = ticket_id;
		this.newEstimatedCost = newEstimatedCost;
		this.lastmodified_time = lastmodified_time;
		this.lastmodified_by = lastmodified_by;
		this.editEstimationCostReasons = editEstimationCostReasons;
		this.priority = priority;
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
		// define instace of the dialog
		pd = new ProgressDialog(mContext);

		// set messgae dialog shown
		pd.setMessage(mContext.getResources().getString(
				R.string.update_estimation_cost));

		// dialog show
		pd.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */

	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		VECVPreferences vecvPreferences = new VECVPreferences(mContext);
		try {
			
			
			restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS()+""+ApiUrls.UPDATE_TICKET_STATUS);
			
			if(priority.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC))
			{
				restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_Telimatic()+""+ApiUrls.UPDATE_TICKET_STATUS);
			}
			
			restIntraction
					.AddParam("Token", vecvPreferences.getSecurityToken());


			String [] ticketid = ticket_id.split("/");
			restIntraction.AddParam("TicketId", ticketid[1]);
			
			restIntraction.AddParam("LastModifiedTime", lastmodified_time);
			restIntraction.AddParam("LastModifiedBy", lastmodified_by);

			restIntraction.AddParam("RepairCost", newEstimatedCost);
			restIntraction.AddParam("SuggestionComment",
					editEstimationCostReasons);

			restIntraction.AddParam("EstimatedTimeForJobCompletionSubmitTime",
					lastmodified_time);
			restIntraction.AddParam("IsMobile", "true");
			restIntraction.toString();
			restIntraction.Execute(1);
//			String response = restIntraction.getResponse();

			// sync db after updates
			MyTicketManager myTicketManager = MyTicketManager
					.getInstance(mContext);
			myTicketManager.api_SyncDbOnUpdates();
			MyTicketActivity.ifNewUpdatesAvailableInDb = true;

		} catch (Exception e) {

			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			// save error log in the file
			UtilityFunction.saveErrorLog(mContext, e);
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
		if (pd != null && pd.isShowing())
			pd.dismiss();

		super.onPostExecute(result);
	}

}
