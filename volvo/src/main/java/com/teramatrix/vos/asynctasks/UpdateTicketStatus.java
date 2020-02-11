package com.teramatrix.vos.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.MapViewTimerActivitySeekBar;
import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.R;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.interfaces.INewTicket;
import com.teramatrix.vos.logs.CompleteLogging;
import com.teramatrix.vos.model.Ticket;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.ticketmanager.MyTicketManager;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.UtilityFunction;

/**
 * This class will be used for update running ticket status on server from
 * device
 * 
 */
public class UpdateTicketStatus extends AsyncTask<Void, Void, Void> {

	// define progress dialog
	private ProgressDialog mProgressDialog;

	// define Context instance of the class
	private Context mContext;

	// define RestIntraction instance of the class
	private RestIntraction restIntraction;

	// define string variable of the class
	String ticket_id = "";
	String description = "";
	String estimation_cost_id = "";
	String estimation_time = "";
	String lastmodified_time = "";
	String lastmodified_by = "";
	String ticket_status = "";
	String sla_missed_reason_id = "";
	String defaultSlaTime = "";
	String acheviedEstimationTimeForJobComplition;
	String editEstimationTimeReasons;
	String updatedEstimatedTime = "";
	boolean isVanReachedConfirmed;
	// define instance of the INewTicket class
	INewTicket inewticket;

	String ticket_priority;

	/**
	 * This class constructor passing the values and initialize them instance
	 * variable
	 * 
	 * @param inewticket
	 * @param mContext
	 * @param ticket_id
	 * @param description
	 * @param estimation_cost_id
	 * @param estimation_time
	 * @param lastmodified_time
	 * @param lastmodified_by
	 * @param ticket_status
	 * @param sla_missed_reason_id
	 */
	public UpdateTicketStatus(INewTicket inewticket, Context mContext,
			String ticket_id, String description, String estimation_cost_id,
			String estimation_time, String lastmodified_time,
			String lastmodified_by, String ticket_status,
			String sla_missed_reason_id, String ticket_priority) {
		// TODO Auto-generated constructor stub

		// get value and initialize them
		this.mContext = mContext;
		this.inewticket = inewticket;
		this.ticket_id = ticket_id;
		this.description = description;
		this.estimation_time = estimation_time;
		this.estimation_cost_id = estimation_cost_id;
		this.lastmodified_time = lastmodified_time;
		this.ticket_status = ticket_status;
		this.sla_missed_reason_id = sla_missed_reason_id;
		this.lastmodified_by = lastmodified_by;
		this.ticket_priority = ticket_priority;
	}

	public UpdateTicketStatus(INewTicket inewticket, Context mContext,
			String ticket_id, String description, String estimation_cost_id,
			String estimation_time, String lastmodified_time,
			String lastmodified_by, String ticket_status,
			String sla_missed_reason_id, boolean isVanReachedConfirmed,
			String ticket_priority) {
		// TODO Auto-generated constructor stub

		// get value and initialize them
		this.mContext = mContext;
		this.inewticket = inewticket;
		this.ticket_id = ticket_id;
		this.description = description;
		this.estimation_time = estimation_time;
		this.estimation_cost_id = estimation_cost_id;
		this.lastmodified_time = lastmodified_time;
		this.ticket_status = ticket_status;
		this.sla_missed_reason_id = sla_missed_reason_id;
		this.lastmodified_by = lastmodified_by;
		this.isVanReachedConfirmed = isVanReachedConfirmed;
		this.ticket_priority = ticket_priority;
	}

	/**
	 * This class constructor passing the values and initialize them instance
	 * variable
	 * 
	 * @param inewticket
	 * @param mContext
	 * @param ticket
	 */

	public UpdateTicketStatus(INewTicket inewticket, Context mContext,
			Ticket ticket) {
		// TODO Auto-generated constructor stub

		// get value and initialize them
		this.mContext = mContext;
		this.inewticket = inewticket;
		this.ticket_id = ticket.Id;
		this.description = ticket.Description;
		this.estimation_time = ticket.EstimatedTimeForJobComplition;
		this.estimation_cost_id = ticket.EstimatedCostForJobComplition;
		this.lastmodified_time = ticket.LastModifiedTime;
		this.ticket_status = ticket.TicketStatus;
		this.sla_missed_reason_id = ticket.slaMissedReasonId;
		this.lastmodified_by = ticket.LastModifiedBy;
		this.acheviedEstimationTimeForJobComplition = ticket.AcheviedEstimatedTimeForJobComplition;
		this.editEstimationTimeReasons = ticket.editEstimationTimeReasonsValue;
		this.updatedEstimatedTime = ticket.EstimatedTimeForJobComplition;
		this.ticket_priority = ticket.Priority;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute() Method is used to perform UI
	 * operation before starting background Service
	 */
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		mProgressDialog = ProgressDialog.show(mContext, "", mContext
				.getResources().getString(R.string.please_wait), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[]) Method is used to
	 * perform Background Task.
	 */
	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub

		// check is log shown ot not
		if (ApplicationConstant.IS_LOG_SHOWN) {

			// define string variable

			String log_message = "Call Api To Update TicketStatus:"
					+ ticket_status;

			// save complete log and update ticket status
			CompleteLogging.logUpdateTicketStatus(mContext, log_message);
		}

		// initialize vecv Preferences

		VECVPreferences vecvPreferences = new VECVPreferences(mContext);
		try {

			// calling url
			restIntraction = new RestIntraction(ApiUrls.UPDATE_TICKET_STATUS);

			if (ticket_priority
					.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC)) {
				restIntraction = new RestIntraction(new VECVPreferences(
						mContext).getAPIEndPoint_Telimatic()
						+ ""
						+ ApiUrls.UPDATE_TICKET_STATUS);
			} else {
				restIntraction = new RestIntraction(new VECVPreferences(
						mContext).getAPIEndPoint_EOS()
						+ ""
						+ ApiUrls.UPDATE_TICKET_STATUS);
			}

			// add parameters
			restIntraction
					.AddParam("Token", vecvPreferences.getSecurityToken());

			// Split ticket_id (NJ0915000228/TICKETID-228) , retrive second part
			// of it.
			String[] ticketid = ticket_id.split("/");
			restIntraction.AddParam("TicketId", ticketid[1]);
			restIntraction.AddParam("LastModifiedTime", lastmodified_time);
			restIntraction.AddParam("LastModifiedBy", lastmodified_by);
			restIntraction.AddParam("TicketStatus", ticket_status);

			// for changing ticket status from in-progress(3) to pre-closer(4)

			// checking ticket status
			if (ticket_status != null && ticket_status.equalsIgnoreCase("4")) {

				// parameter for achevied estuimation time for job complition

				// add parameters
				restIntraction.AddParam("JobCompleteResponseTime",
						acheviedEstimationTimeForJobComplition);
				restIntraction.AddParam("SuggestionComment",
						editEstimationTimeReasons);
				restIntraction.AddParam("EstimatedTimeForJobCompletion",
						updatedEstimatedTime);

			} else if (ticket_status != null
					&& ticket_status.equalsIgnoreCase("8")) {

				// add parameters for 'Flag'
				// 'Flag' -> 'OPEN' if ticket is in pre-closed state(not closed
				// yet from cce) and user has requested trip end.
				restIntraction.AddParam("Flag", "OPEN");
				// 'Flag' -> 'CLOSED' if ticket is in pre-closed + closed
				// state(closed by cce before user has requested trip end) and
				// user has requested trip end.
				//restIntraction.AddParam("Flag", "CLOSED");

			} else {
				// for changing ticket status from acceted(2) to in-progress(3)
				// restIntraction.AddParam("Description", description);
				// not updating value on server

				// add parameters
				if (isVanReachedConfirmed) {

					restIntraction.AddParam("EstimatedTimeForJobCompletion",
							estimation_time);
					restIntraction.AddParam("RepairCost", estimation_cost_id);
				}
				restIntraction.AddParam("SlaMissedReasonsIds",
						sla_missed_reason_id);
				restIntraction.AddParam("SuggestionComment", description);

				restIntraction.AddParam(
						"EstimatedTimeForJobCompletionSubmitTime",
						lastmodified_time);

				// Van reached Form submission
				if (ticket_status != null
						&& ticket_status.equalsIgnoreCase("3"))
					restIntraction.AddParam("IsMobile", "true");
			}
			restIntraction.toString();

			// execue api
			restIntraction.Execute(1);

			// check and get response
			String response = restIntraction.getResponse();

			// check application log shown
			if (ApplicationConstant.IS_LOG_SHOWN) {

				// define string variable
				String log_message = "Call Api To Update TicketStatus Response:"
						+ response;

				// save complete log and update ticket status

				CompleteLogging.logUpdateTicketStatus(mContext, log_message);
			}

			// sync db after updates and set boolean value
			// ifNewUpdatesAvailableInDb true
			MyTicketManager myTicketManager = MyTicketManager
					.getInstance(mContext);
			myTicketManager.api_SyncDbOnUpdates();
			MyTicketActivity.ifNewUpdatesAvailableInDb = true;

		} catch (Exception e) {

			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);
			// save error in log file
			UtilityFunction.saveErrorLog(mContext, e);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object) Method is used
	 * to perform UI operation after background task will be finishing.
	 */
	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);

		// check isShowing dialog null or not
		if (mProgressDialog != null && mProgressDialog.isShowing())
			// dismiss dialog
			mProgressDialog.dismiss();

		if (estimation_cost_id == null
				|| (estimation_cost_id != null && estimation_cost_id
						.equalsIgnoreCase("null"))) {
			// this case is for van reached confiramtion , stay on same screen
			if (mContext != null
					&& mContext instanceof MapViewTimerActivitySeekBar) {


				if(ticket_status!=null && !ticket_status.equalsIgnoreCase("9")) {
					MapViewTimerActivitySeekBar mapViewTimerActivitySeekBar = (MapViewTimerActivitySeekBar) mContext;
					mapViewTimerActivitySeekBar.showTimeEstimationDialog();
				}
			}

		} else {
			// calling api updates ticket response
			inewticket.api_TicketUpdateResponse(ticket_id);
		}

	}
}
