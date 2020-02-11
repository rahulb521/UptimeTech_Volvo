package com.teramatrix.vos.asynctasks;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.model.Ticket;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.reciver.InternetAvailabilityRecever;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.ticketmanager.MyTicketManager;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.UtilityFunction;

/**
 * This class will be used for upload offline tickets data send on server when
 * internet is on.
 * 
 * @author Gaurav.Mangal
 * 
 */

public class UploadOfflineTicketingData extends AsyncTask<Void, Void, Void> {

	// define instance of the context class
	private Context context;

	// define arraylist object instance
	private ArrayList<Ticket> offlineData;

	// define RestIntraction instance of the class
	private RestIntraction restIntraction;
	// define JSONObject instance of the class
	private JSONObject jsonObject;
	// define String variable of the class
	private String securityToken, str_deviceAlias, response, status, message;
	// define InternetAvailabilityRecever instance of the class
	private InternetAvailabilityRecever receiver;
	// define boolean variable of the class
	private boolean isOpertionSuccessfull;

	/**
	 * 
	 * @param receiver
	 * @param context
	 * @param offlineData
	 * @param token
	 * @param device_alias
	 */
	public UploadOfflineTicketingData(InternetAvailabilityRecever receiver,
			Context context, ArrayList<Ticket> offlineData, String token,
			String device_alias) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.offlineData = offlineData;
		securityToken = token;
		str_deviceAlias = device_alias;
		this.receiver = receiver;
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

			JSONArray jsonArray_eos_ticket = new JSONArray();
			JSONArray jsonArray_telimatic_ticket = new JSONArray();

			// Prepare a Json string of all Ticket data which need to be
			// uploaded on server,this json string will be send as a value of
			// some parameter in service api
			for (Ticket ticketingdata : offlineData) {

				JSONObject obj = new JSONObject();
				String[] ticketid = ticketingdata.Id.split("/");
				obj.put("TicketId", ticketid[1]);
				obj.put("Description", ticketingdata.Description);
				obj.put("TicketStatus", ticketingdata.TicketStatus);
				obj.put("CreationTime", ticketingdata.CreationTime);
				obj.put("LastModifiedBy", str_deviceAlias);
				obj.put("LastModifiedTime", ticketingdata.LastModifiedTime);
				obj.put("BreakdownLocation", ticketingdata.BreakDownLocation);
				obj.put("BreakdownLongitude", ticketingdata.BreackDownLongitude);
				obj.put("BreakdownLattitude", ticketingdata.BreackDownLatitude);
				obj.put("IsDeclined", ticketingdata.IsDeclined);
				obj.put("EstimatedTimeForJobCompletion",
						ticketingdata.EstimatedTimeForJobComplition);
				obj.put("TotalTicketLifecycleTimeSla",
						ticketingdata.TotalTicketLifeCycleTimeSlab);
					obj.put("EstimatedTimeForJobCompletionSubmitTime",
						ticketingdata.EstimatedTimeForJobComplitionSubmitTime);
				obj.put("VehicleRegistrationNumber",
						ticketingdata.VehicleRegistrationNo);
				obj.put("CustomerContactNo", ticketingdata.CustomeContact_no);
				obj.put("RepairCost",
						ticketingdata.EstimatedCostForJobComplition);
				obj.put("DefaultSlaTime",
						ticketingdata.TotalTicketLifeCycleTimeSlab);
				obj.put("SlaMissedReason", ticketingdata.slaMissedReasonId);
				obj.put("SuggestionComment", ticketingdata.SuggestionComment);
				obj.put("JobCompleteResponseTime",
						ticketingdata.AcheviedEstimatedTimeForJobComplition);

				if (ticketingdata.Priority != null
						&& ticketingdata.Priority
								.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC)) {
					jsonArray_telimatic_ticket.put(obj);
				} else {
					jsonArray_eos_ticket.put(obj);
				}

			}

			// Execute API For Eos
			try {
				restIntraction = new RestIntraction(new VECVPreferences(context).getAPIEndPoint_EOS() + ""
						+ ApiUrls.BULK_UPLOAD_OFFLINE_TICKETS);
				restIntraction.AddParam("Token", securityToken);
				restIntraction.AddParam("BulkTicketStr",
						jsonArray_eos_ticket.toString());
				restIntraction.Execute(1);
				String res= restIntraction.getResponse();
				System.out
				.println("Testing UploadOfflineTicketingData.doInBackground() Eos Ticket "+res);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Execute API For Telimatic
			if(new VECVPreferences(context).getAPIEndPoint_Telimatic().length()>0)
			{
			try {
				restIntraction = new RestIntraction(new VECVPreferences(context).getAPIEndPoint_Telimatic() + ""
						+ ApiUrls.BULK_UPLOAD_OFFLINE_TICKETS);
				restIntraction.AddParam("Token", securityToken);
				restIntraction.AddParam("BulkTicketStr",
						jsonArray_telimatic_ticket.toString());
				restIntraction.Execute(1);
				
				String res= restIntraction.getResponse();
				
				System.out
						.println("Testing UploadOfflineTicketingData.doInBackground() Telimatic Ticket "+res);
			} catch (Exception e) {
				e.printStackTrace();
			}
			}

			// Sync OpenTicketTable and ClosedTicketTable from server db
			MyTicketManager myTicketManager = MyTicketManager
					.getInstance(context);
			myTicketManager.api_SyncDbOnUpdates();
			isOpertionSuccessfull = true;
		} catch (Exception ex) {
			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(ex);
			// save log error on device
			UtilityFunction.saveErrorLog(context, ex);
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

		// delete offline ticket data from local db
		if (isOpertionSuccessfull) {
			try {
				receiver.deletOffLineTicketData();
			} catch (Exception e) {
				// Google Analytic -Tracking Exception
				EosApplication.getInstance().trackException(e);
				// TODO Auto-generated catch block
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		InternetAvailabilityRecever.isOfflineTicketUploadingOnServer = false;

		// update Home screen Tickets when only App is running in forground
		if (isOpertionSuccessfull && ApplicationConstant.IS_APP_IN_FORGROUND) {

			// set flag ifNewUpdatesAvailableInDb to true so when user move to
			// MyTicketActivity(home screen) , it knows that local db has
			// updated and get updated data from local db on Resume .
			MyTicketActivity.ifNewUpdatesAvailableInDb = true;
			// if MyTicketActivity(Homescreen) is opened while this
			// operation(uploading offline ticket data to server),home
			// screen updates immediately after operation completes.
			if (ApplicationConstant.currentActivityContext != null
					&& ApplicationConstant.currentActivityContext instanceof MyTicketActivity) {
				MyTicketActivity myTicketActivity = (MyTicketActivity) ApplicationConstant.currentActivityContext;
				myTicketActivity.loadUpdatedTickets();
			}
		}
	}
}
