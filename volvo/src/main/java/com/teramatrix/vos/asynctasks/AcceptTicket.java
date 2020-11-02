package com.teramatrix.vos.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.R;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.interfaces.INewTicket;
import com.teramatrix.vos.jsonparser.TicketJsonParser;
import com.teramatrix.vos.logs.CompleteLogging;
import com.teramatrix.vos.model.Ticket;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.ticketmanager.MyTicketManager;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.UtilityFunction;

/**
 * AcceptTicket Class is used to perform background operation, in order to
 * accept ticket when network is available. and store them to local database.
 * 
 * @author Gaurav.Mangal
 * 
 */

public class AcceptTicket extends AsyncTask<Void, Void, Void> {

	// Define Context for this class
	private Context mContext;

	// Define String variables for this class
	private String securityToken, assignedTo, isDecline, ticket_id,
			LastModifiedBy, LastModifiedtime, ticket_description,
			estimation_time_job_completion,
			estimation_time_job_completion_submit, response, status, message;

	// Define ProgressDialog for this class
	public static ProgressDialog mProgressDialog;

	// Define RestIntraction for this class
	private RestIntraction restIntraction;

	// Define Ticket instance of a class
	private Ticket updatedticket;

	// Define INewTicket instance of an interface
	private INewTicket iNewTicket;

	// Define String variables for this class
	private String decline_reasons;

	private String ticket_priority;
	/**
	 * 
	 * @param iNewTicket
	 * @param context
	 * @param token
	 * @param assigned_To
	 * @param is_Decline
	 * @param ticket_Id
	 * @param last_modifiedBy
	 * @param last_ModifiedTime
	 * @param ticket_description
	 * @param estimation_time_job_completion
	 * @param estimation_time_job_completion_submit
	 * @param decline_reasons
	 * 
	 *            Pass all the @params in constructor, as it it required for
	 *            Service API.
	 */
	public AcceptTicket(INewTicket iNewTicket, Context context, String token,
			String assigned_To, String is_Decline, String ticket_Id,
			String last_modifiedBy, String last_ModifiedTime,
			String ticket_description, String estimation_time_job_completion,
			String estimation_time_job_completion_submit, String decline_reasons,String ticket_priority) {
		mContext = context;
		securityToken = token;
		assignedTo = assigned_To;
		isDecline = is_Decline;
		this.ticket_id = ticket_Id;
		LastModifiedBy = last_modifiedBy;
		LastModifiedtime = last_ModifiedTime;
		this.iNewTicket = iNewTicket;
		this.decline_reasons = decline_reasons;
		this.ticket_priority =ticket_priority;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute() Method is used to perform UI
	 * operation before starting background Service
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.please_wait),
				false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[]) Method is used to
	 * perform Background Task.
	 */
	@Override
	protected Void doInBackground(Void... params) {
		try {

			// Log Acccept Process
			if (ApplicationConstant.IS_LOG_SHOWN) {
				String log_message = "Accepting/DeclineTicket Api calling "
						+ "IsDecline:" + isDecline;
				CompleteLogging.logAcceptTicket(mContext, log_message);
			}

			if(ticket_priority!=null && ticket_priority.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC))
			{
				// Create an instance of the RestClient class
				restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_Telimatic()+""+ApiUrls.TICKET_ACCEPT);
			}else
			{
				// Create an instance of the RestClient class
				restIntraction = new RestIntraction( new VECVPreferences(mContext).getAPIEndPoint_EOS()+""+ApiUrls.TICKET_ACCEPT);
			}
			// Pass required parameter
			restIntraction.AddParam("Token", securityToken);
			String [] ticketid = ticket_id.split("/");
			restIntraction.AddParam("Id", ticketid[1]);
			restIntraction.AddParam("LastModifiedBy", LastModifiedBy);
			restIntraction.AddParam("LastModifiedTime", LastModifiedtime);
			restIntraction.AddParam("isDeclined", isDecline);

			if (isDecline.equalsIgnoreCase("true")) {
				restIntraction.AddParam("Description", decline_reasons);
			}
			restIntraction.toString();
			restIntraction.Execute(1);

			// get response from the service
			response = restIntraction.getResponse();
			if (ApplicationConstant.IS_LOG_SHOWN) {
				String log_message = "Accepting Ticket Api Response:"
						+ response;
				CompleteLogging.logAcceptTicket(mContext, log_message);
			}

			updatedticket = TicketJsonParser.getTicket(response);
			// sync local db and remote db for latest update ticket state
			MyTicketManager myTicketManager = MyTicketManager
					.getInstance(mContext);
			myTicketManager.api_SyncDbOnUpdates();
			MyTicketActivity.ifNewUpdatesAvailableInDb = true;
		} catch (Exception ex) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(ex);
			//save error location in file
			UtilityFunction.saveErrorLog(mContext, ex);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 *  Method is used to perform
	 * UI operation after background task will be finishing.
	 */
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

		// if isDecline is "false" it means ticket is accepted
		if (isDecline.equalsIgnoreCase("false")) {
			iNewTicket.setAcceptedTicket(true, ticket_id);
		} else {
			// check mProgressDialog dismiss or not
			if (mProgressDialog != null && mProgressDialog.isShowing())
				mProgressDialog.dismiss();
			iNewTicket.setAcceptedTicket(false, null);
		}

	}
}
