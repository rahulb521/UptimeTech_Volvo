package com.teramatrix.vos.asynctasks;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.R;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.interfaces.INewTicket;
import com.teramatrix.vos.jsonparser.TicketJsonParser;
import com.teramatrix.vos.model.Ticket;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.ticketmanager.MyTicketManager;
import com.teramatrix.vos.utils.UtilityFunction;

/**
 * This GetOpenTicket class for geting open ticket for calling
 * TICKET_OPEN_DEVICE_ALIAS and also sync on local database when internet is
 * available.
 * 
 * @author Gaurav.Mangal
 * 
 */
public class GetOpenTicket extends AsyncTask<Void, Void, Void> {

	// Define Context for this class
	private Context mContext;

	// Define String variables for this class
	private String securityToken, assignedTo, priority, response, status,
			message;

	// Define ProgressDialog for this class
	private ProgressDialog mProgressDialog;

	// Define RestIntraction for this class
	private RestIntraction restIntraction;

	// Define JsonObject for this class
	private JSONObject jsonObject;

	// Define VECVPreferences for this class
	private VECVPreferences vecvPreferences;

	// Define Ticket instance of a class
	private Ticket ticket;

	// Define boolean value of a new ticket is available or not
	private boolean isNewTicketAvailable;

	// Define Notification id NOTIFY_ME_ID variable
	private static final int NOTIFY_ME_ID = 1337;

	// Define INewTicket interface instace of a class
	private INewTicket inewticket;

	// Define tickets data in ArrayList
	private ArrayList<Ticket> tickets;

	private boolean isCalledForPullRefresh;

	/**
	 * 
	 * @param context
	 * @param inewticket
	 * @param token
	 * @param assigned_to
	 * @param priority_value
	 */

	public GetOpenTicket(Context context, INewTicket inewticket, String token,
			String assigned_to, String priority_value) {
		mContext = context;
		this.inewticket = inewticket;
		securityToken = token;
		assignedTo = assigned_to;
		priority = priority_value;
		ticket = new Ticket();
	}

	/**
	 * 
	 * @param context
	 * @param inewticket
	 * @param token
	 * @param assigned_to
	 * @param priority_value
	 * @param isCalledForPullRefresh
	 * 
	 *            Pass all the @params in constructor, as it it required for
	 *            Service API.
	 */
	public GetOpenTicket(Context context, INewTicket inewticket, String token,
			String assigned_to, String priority_value,
			boolean isCalledForPullRefresh) {
		mContext = context;
		this.inewticket = inewticket;
		securityToken = token;
		assignedTo = assigned_to;
		priority = priority_value;
		ticket = new Ticket();
		this.isCalledForPullRefresh = isCalledForPullRefresh;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute(), Method is used to perform UI
	 * operation before starting background Service
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (!isCalledForPullRefresh)
			mProgressDialog = ProgressDialog.show(mContext, "", mContext
					.getResources().getString(R.string.loading_new_ticket),
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

			vecvPreferences = new VECVPreferences(mContext);

			// Get New Ticket from Eos API
			restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS() + ""
					+ ApiUrls.TICKET_OPEN_DEVICE_ALIAS);
			restIntraction.AddParam("Token", securityToken);
			restIntraction.AddParam("DeviceAlias", assignedTo);
			restIntraction.AddParam("TicketStatus", priority);
			restIntraction.AddParam("Ieminumber",
					new VECVPreferences(mContext).getImeiNumber());

			restIntraction.Execute(1);

			response = restIntraction.getResponse();
			if (response != null) {
				try {

					tickets = TicketJsonParser
							.getTicketFromArrayJsonString(response);
				} catch (Exception e) {
					// Google Analytic -Tracking Exception
					EosApplication.getInstance().trackException(e);
					// save error location in file
					UtilityFunction.saveErrorLog(mContext, e);
				}
			}

			if (new VECVPreferences(mContext).getAPIEndPoint_Telimatic().length() > 0) {
				// Get New Ticket from Telimatic API
				restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_Telimatic() + ""
						+ ApiUrls.TICKET_OPEN_DEVICE_ALIAS);
				restIntraction.AddParam("Token", securityToken);
				restIntraction.AddParam("DeviceAlias", assignedTo);
				restIntraction.AddParam("TicketStatus", priority);
				restIntraction.AddParam("Ieminumber", new VECVPreferences(
						mContext).getImeiNumber());
				restIntraction.Execute(1);
				response = restIntraction.getResponse();

				if (response != null) {
					try {

						ArrayList<Ticket> arrayList_tickets = TicketJsonParser
								.getTicketFromArrayJsonString(response);

						if (arrayList_tickets != null
								&& arrayList_tickets.size() > 0) {

							// Update Ticket priority
							for (Ticket ticket : arrayList_tickets) {
								ticket.Priority = Ticket.TICKET_PRIORITY_TELIMATIC;
							}

							if (tickets == null)
								tickets = new ArrayList<Ticket>();

							tickets.addAll(arrayList_tickets);
						}

					} catch (Exception e) {
						// Google Analytic -Tracking Exception
						EosApplication.getInstance().trackException(e);
						// save error location in file
						UtilityFunction.saveErrorLog(mContext, e);
					}
				}
			}

			if (isCalledForPullRefresh) {
				MyTicketManager myTicketManager = MyTicketManager
						.getInstance(mContext);
				myTicketManager.api_SyncDbOnPullToRefresh();
			}

		} catch (Exception ex) {

			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(ex);
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
		super.onPostExecute(result);
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();

		if (isCalledForPullRefresh) {
			MyTicketActivity myTicketActivity = MyTicketActivity.getInstance();
			myTicketActivity.stopOpenTicketListPullRefreshing();
		}

		if (tickets != null && tickets.size() > 0) {
			inewticket.setNewTicket(tickets.get(0));
		} else {
			if (isCalledForPullRefresh) {
				MyTicketActivity.ifNewUpdatesAvailableInDb = true;
				MyTicketActivity myTicketActivity = MyTicketActivity
						.getInstance();
				myTicketActivity.loadUpdatedTickets();
			}
			inewticket.setNewTicket(null);
		}
	}
}
