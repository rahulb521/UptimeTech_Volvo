package com.teramatrix.vos.ticketmanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.widget.Toast;

import com.teramatrix.vos.EosApplication;

import com.teramatrix.vos.JobStatusDetailActivity;
import com.teramatrix.vos.MapViewTimerActivitySeekBar;
import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.R;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.asynctasks.AcceptTicket;
import com.teramatrix.vos.asynctasks.UpdateJobEstimatedCostService;
import com.teramatrix.vos.asynctasks.UpdateJobEstimatedTimeService;
import com.teramatrix.vos.asynctasks.UpdateTicketStatus;
import com.teramatrix.vos.checkinternet.CheckInternetConnection;
import com.teramatrix.vos.database.DBInteraction;
import com.teramatrix.vos.firebase.service.MyFirebaseMessagingService;
import com.teramatrix.vos.interfaces.INewTicket;
import com.teramatrix.vos.jsonparser.TicketJsonParser;
import com.teramatrix.vos.logs.CompleteLogging;
import com.teramatrix.vos.model.DeclineReasonModel;
import com.teramatrix.vos.model.EstimationCostModel;
import com.teramatrix.vos.model.Ticket;
import com.teramatrix.vos.model.UpdateCounter;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.TimeFormater;
import com.teramatrix.vos.utils.UtilityFunction;

/**
 * 
 * This class MyTicketManager class manages for ticket operations as
 * (Accept,Decline,Van Reached ,Jobs Completed ,Synchronization). and calling
 * alternatively api for the response from server.
 * 
 */
public class MyTicketManager implements INewTicket {

	// define instance of MyTicketManager class
	private static MyTicketManager instance;

	// define instance of Context class
	private static Context mContext;

	// define boolean variable isOperationSuccessfull
	public static boolean isOperationSuccessfull;

	/**
	 * getting instance of the context for the class
	 * 
	 * @param context
	 * @return
	 */
	public static MyTicketManager getInstance(Context context) {
		if (instance == null)
			instance = new MyTicketManager();

		mContext = context;
		return instance;
	}

	/**
	 * create no argument constructor
	 */

	private MyTicketManager() {

	}

	// Api Get All Open Tickets
	/**
	 * 
	 * @return
	 */
	public ArrayList<Ticket> api_GetAllOpenTickets() {
		return null;
	}

	// Api Get All Closed/History Tickets

	/**
	 * 
	 * @return
	 */
	public ArrayList<Ticket> api_GetAllClosedTickets() {
		return null;
	}

	/**
	 * save all response data to local db in respective
	 * tables(OpenTickets,ClosedTickets) , update timestamp value to current
	 * date and time in Timestamp table locally.
	 */

	public void api_SyncDbOnInstallation() {

		try {
			DBInteraction dbInteraction = DBInteraction
					.getInstance(ApplicationConstant.currentActivityContext);
			dbInteraction.getConnection();
			dbInteraction.deleteMyTicketData();
			dbInteraction.closeConnection();

			api_SyncDbOnInstallation(Ticket.TICKET_PRIORITY_EOS);
			api_SyncDbOnInstallation(Ticket.TICKET_PRIORITY_TELIMATIC);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void api_SyncDbOnInstallation(String sync_priority) {

		try {
			// updates last time sync time stamp 1970-01-01 00:00:00
			updateLastSyncTimeStamp(true,
					ApplicationConstant.FIRST_SYNC_TIMESTAMP);

			// create Rest interaction with passing Sync DB initially loaded
			RestIntraction restIntraction = new RestIntraction(ApiUrls.SYNC_DB);
			if (sync_priority
					.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC)) {
				// sync from Telimatic DB
				restIntraction = new RestIntraction(new VECVPreferences(
						mContext).getAPIEndPoint_Telimatic()
						+ ""
						+ ApiUrls.SYNC_DB);
			} else {
				// sync from Eos DB
				restIntraction = new RestIntraction(new VECVPreferences(
						mContext).getAPIEndPoint_EOS() + "" + ApiUrls.SYNC_DB);

			}

			// added and passing parameter SYNC_DB url call
			restIntraction.AddParam("Token",
					new VECVPreferences(mContext).getSecurityToken());
			restIntraction.AddParam("DeviceAlias",
					new VECVPreferences(mContext).getDevice_alias());
			restIntraction.AddParam("DbSynLastTime",
					ApplicationConstant.FIRST_SYNC_TIMESTAMP);
			restIntraction.AddParam("Ieminumber",
					new VECVPreferences(mContext).getImeiNumber());

			restIntraction.toString();
			// Execute post data with pass 1
			restIntraction.Execute(1);
			// check and get response from server
			String response4 = restIntraction.getResponse();

			String Status = "";
			String DbSynLastTime = "";
			if (response4 != null && response4.length() > 0) {
				JSONArray array = new JSONArray(response4);
				if (array != null && array.length() > 0) {
					JSONObject object = (JSONObject) array.get(0);
					if (object.has("Status")) {
						Status = object.getString("Status");
					}
					if (object.has("DbSynLastTime")) {
						DbSynLastTime = object.getString("DbSynLastTime");
					}
				}
			}

			// check status
			if (Status != null && Status.equalsIgnoreCase("1")) {
				ArrayList<ArrayList<Ticket>> allTickets = TicketJsonParser
						.getExistingOpenAndClosedTicket(response4);

				// check allTickets
				if (allTickets != null && allTickets.size() > 0) {
					// DBInteraction dbInt = DBInteraction.getInstance();

					// get position 0 for fetch all open tickets
					ArrayList<Ticket> opendTickets = allTickets.get(0);
					// get position 1 for fetch all closed tickets
					ArrayList<Ticket> closedTickets = allTickets.get(1);

					if (opendTickets != null && opendTickets.size() > 0) {
						if (sync_priority
								.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC)) {
							for (Ticket ticket : opendTickets) {
								ticket.Priority = Ticket.TICKET_PRIORITY_TELIMATIC;
							}
						}
						saveOpenTicketsInLocalDB(opendTickets);
					}
					if (closedTickets != null && closedTickets.size() > 0) {
						if (sync_priority
								.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC)) {
							for (Ticket ticket : closedTickets) {
								ticket.Priority = Ticket.TICKET_PRIORITY_TELIMATIC;
							}
						}
						saveClosedTicketsInLocalDB(closedTickets);
					}

					// update last sync time stamp
					updateLastSyncTimeStamp(false, DbSynLastTime);
				}
			}
		} catch (Exception e) {
			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);
			// save error in the log file
			UtilityFunction.saveErrorLog(mContext, e);
		}
	}

	public void api_SyncDbOnUpdates() {

		try {
			DBInteraction dbInteraction = DBInteraction
					.getInstance(ApplicationConstant.currentActivityContext);
			dbInteraction.getConnection();
			dbInteraction.deleteMyTicketData();
			dbInteraction.closeConnection();
			String DbSynLastTime = api_SyncDbOnUpdates(Ticket.TICKET_PRIORITY_EOS);
			DbSynLastTime = api_SyncDbOnUpdates(Ticket.TICKET_PRIORITY_TELIMATIC);
			updateLastSyncTimeStamp(false, DbSynLastTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method for calling api sync db when updates on server.
	 */
	public String api_SyncDbOnUpdates(String sync_priority) {

		String lastSyncTimestamp = getLastSyncTimeStamp();
		try {
			// get last sync time stamp

			// call api sync
			RestIntraction restIntraction = new RestIntraction(ApiUrls.SYNC_DB);

			if (sync_priority
					.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC)) {
				// sync from Telimatic DB
				restIntraction = new RestIntraction(new VECVPreferences(
						mContext).getAPIEndPoint_Telimatic()
						+ ""
						+ ApiUrls.SYNC_DB);
			} else {
				// sync from Eos DB
				restIntraction = new RestIntraction(new VECVPreferences(
						mContext).getAPIEndPoint_EOS() + "" + ApiUrls.SYNC_DB);

			}

			// Add parameter
			restIntraction.AddParam("Token",
					new VECVPreferences(mContext).getSecurityToken());

			// add parameter
			restIntraction.AddParam("DeviceAlias",
					new VECVPreferences(mContext).getDevice_alias());

			restIntraction.AddParam("DbSynLastTime", lastSyncTimestamp);
			restIntraction.AddParam("Ieminumber",
					new VECVPreferences(mContext).getImeiNumber());

			restIntraction.toString();
			// execue apise
			restIntraction.Execute(1);

			// get response from api
			String response4 = restIntraction.getResponse();

			// define string variable
			String Status = "";
			String DbSynLastTime = "";

			// check response value
			if (response4 != null && response4.length() > 0) {
				JSONArray array = new JSONArray(response4);
				if (array != null && array.length() > 0) {
					JSONObject object = (JSONObject) array.get(0);
					if (object.has("Status")) {
						Status = object.getString("Status");
					}
					if (object.has("DbSynLastTime")) {
						DbSynLastTime = object.getString("DbSynLastTime");
					}
				}
			}

			// check status value
			if (Status != null && Status.equalsIgnoreCase("1")) {
				ArrayList<ArrayList<Ticket>> allTickets = TicketJsonParser
						.getExistingOpenAndClosedTicket(response4);

				// check all ticket
				if (allTickets != null && allTickets.size() > 0) {

					ArrayList<Ticket> opendTickets = allTickets.get(0);
					ArrayList<Ticket> closedTickets = allTickets.get(1);

					if (opendTickets != null && opendTickets.size() > 0) {
						if (sync_priority
								.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC)) {
							for (Ticket ticket : opendTickets) {
								ticket.Priority = Ticket.TICKET_PRIORITY_TELIMATIC;
							}
						}
						saveOpenTicketsInLocalDB(opendTickets);
					}
					if (closedTickets != null && closedTickets.size() > 0) {
						if (sync_priority
								.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC)) {
							for (Ticket ticket : closedTickets) {
								ticket.Priority = Ticket.TICKET_PRIORITY_TELIMATIC;
							}
						}
						saveClosedTicketsInLocalDB(closedTickets);
					}

					// updateLastSyncTimeStamp(false, DbSynLastTime);
					isOperationSuccessfull = true;
				}
				return DbSynLastTime;
			} else {
				isOperationSuccessfull = false;
				if (ApplicationConstant.IS_APP_IN_FORGROUND)
					Toast.makeText(
							mContext,
							mContext.getResources().getString(
									R.string.error_sync_update),
							Toast.LENGTH_LONG).show();

			}
		} catch (Exception e) {
			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);
			// save error log
			UtilityFunction.saveErrorLog(mContext, e);

		}
		return lastSyncTimestamp;
	}

	public void api_SyncDbOnPullToRefresh() {
		try {
			DBInteraction dbInteraction = DBInteraction
					.getInstance(ApplicationConstant.currentActivityContext);
			dbInteraction.getConnection();
			dbInteraction.deleteMyTicketData();
			dbInteraction.closeConnection();

			api_SyncDbOnPullToRefresh(Ticket.TICKET_PRIORITY_EOS);
			api_SyncDbOnPullToRefresh(Ticket.TICKET_PRIORITY_TELIMATIC);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void api_SyncDbOnPullToRefresh(String sync_priority) {

		try {
			// get last sync time stamp

			// String lastSyncTimestamp = getLastSyncTimeStamp();
			String lastSyncTimestamp = ApplicationConstant.FIRST_SYNC_TIMESTAMP;

			// check application log shown or not
			if (ApplicationConstant.IS_LOG_SHOWN) {

				String log_message = "Call Api To Sync DB "
						+ " lastSyncTimestamp:" + lastSyncTimestamp;
				CompleteLogging.logSyncDB(mContext, log_message);
			}

			// call api sync
			RestIntraction restIntraction = new RestIntraction(ApiUrls.SYNC_DB);
			if (sync_priority
					.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC)) {
				// sync from Telimatic DB
				restIntraction = new RestIntraction(new VECVPreferences(
						mContext).getAPIEndPoint_Telimatic()
						+ ""
						+ ApiUrls.SYNC_DB);
			} else {
				// sync from Eos DB
				restIntraction = new RestIntraction(new VECVPreferences(
						mContext).getAPIEndPoint_EOS() + "" + ApiUrls.SYNC_DB);

			}

			// Add parameter
			restIntraction.AddParam("Token",
					new VECVPreferences(mContext).getSecurityToken());

			// add parameter
			restIntraction.AddParam("DeviceAlias",
					new VECVPreferences(mContext).getDevice_alias());

			restIntraction.AddParam("DbSynLastTime", lastSyncTimestamp);
			restIntraction.AddParam("Ieminumber",
					new VECVPreferences(mContext).getImeiNumber());

			restIntraction.toString();
			// execue api
			restIntraction.Execute(1);

			// get response from api
			String response4 = restIntraction.getResponse();

			// check if application log show or not

			if (ApplicationConstant.IS_LOG_SHOWN) {

				// define string value initialization
				String log_message = "Api Sync DB Response:" + response4;

				// define complete log
				CompleteLogging.logSyncDB(mContext, log_message);
			}

			// define string variable
			String Status = "";
			String DbSynLastTime = "";

			// check response value
			if (response4 != null && response4.length() > 0) {
				JSONArray array = new JSONArray(response4);
				if (array != null && array.length() > 0) {
					JSONObject object = (JSONObject) array.get(0);
					if (object.has("Status")) {
						Status = object.getString("Status");
					}
					if (object.has("DbSynLastTime")) {
						DbSynLastTime = object.getString("DbSynLastTime");
					}
				}
			}

			// check status value
			if (Status != null && Status.equalsIgnoreCase("1")) {
				ArrayList<ArrayList<Ticket>> allTickets = TicketJsonParser
						.getExistingOpenAndClosedTicket(response4);

				// check all ticket
				if (allTickets != null && allTickets.size() > 0) {
					// DBInteraction dbInt = DBInteraction.getInstance();
					ArrayList<Ticket> opendTickets = allTickets.get(0);
					ArrayList<Ticket> closedTickets = allTickets.get(1);

					if (opendTickets != null && opendTickets.size() > 0) {
						if (sync_priority
								.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC)) {
							for (Ticket ticket : opendTickets) {
								ticket.Priority = Ticket.TICKET_PRIORITY_TELIMATIC;
							}
						}
						saveOpenTicketsInLocalDB(opendTickets);
					}
					if (closedTickets != null && closedTickets.size() > 0) {
						if (sync_priority
								.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC)) {
							for (Ticket ticket : closedTickets) {
								ticket.Priority = Ticket.TICKET_PRIORITY_TELIMATIC;
							}
						}
						saveClosedTicketsInLocalDB(closedTickets);
					}

					// updateLastSyncTimeStamp(false, DbSynLastTime);
					isOperationSuccessfull = true;
				}

			} else {
				isOperationSuccessfull = false;
				if (ApplicationConstant.IS_APP_IN_FORGROUND)
					Toast.makeText(
							mContext,
							mContext.getResources().getString(
									R.string.error_sync_update),
							Toast.LENGTH_LONG).show();

			}
		} catch (Exception e) {
			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);
			// save error log
			UtilityFunction.saveErrorLog(mContext, e);
		}
	}

	/**
	 * 
	 * @param isFirstTime
	 * @param timestamp
	 * 
	 *            This method updates last sync time stamp value in local db
	 */
	private void updateLastSyncTimeStamp(boolean isFirstTime, String timestamp) {

		// create instance of the DBInteraction class
		DBInteraction dbInteraction = DBInteraction
				.getInstance(ApplicationConstant.currentActivityContext);
		// getting connection to the database
		dbInteraction.getConnection();

		// update time stamp in local db
		dbInteraction.updateTimeStamp(isFirstTime, timestamp);
		// close the databse connection
		dbInteraction.closeConnection();
	}

	/**
	 * This method get the last timestamp value from local db
	 * 
	 * @return
	 */
	private String getLastSyncTimeStamp() {
		// create instance of the DBInteraction class
		DBInteraction dbInteraction = DBInteraction
				.getInstance(ApplicationConstant.currentActivityContext);
		// getting connection to the database
		dbInteraction.getConnection();
		// update time stamp in local db
		String timestamp = dbInteraction.getLastTimestap();
		// close the databse connection
		dbInteraction.closeConnection();

		// return string timestamp value
		return timestamp;
	}

	// Api Call
	/**
	 * This function defines for accept ticket and set initialize value of them
	 * and passing parameter of the ticket object
	 * 
	 * @param ticket
	 */
	public void api_AcceptTicket(final Ticket ticket) {

		// create instance of the class VECVPreferences
		final VECVPreferences vecvPreferences = new VECVPreferences(mContext);

		// check internet connection
		if (new CheckInternetConnection(mContext).isConnectedToInternet()) {
			// Network available, Online ticket processing

			/**
			 * call accept ticket intialization value and constructor.
			 * token,device alias,ticketid current time fetch from
			 * vecvPreferences instance mContext is an apllication context
			 */
			new AcceptTicket(this, mContext,
					vecvPreferences.getSecurityToken(),
					vecvPreferences.getDevice_alias(), "false", ticket.getId(),
					vecvPreferences.getDevice_alias(),
					UtilityFunction.currentUTCTime(), null, null, null, null,
					ticket.Priority).execute();

			// Google Analytic - Event Notify
			EosApplication.getInstance()
					.trackEvent("New Ticket", "Ticket Accepted API Called",
							"Ticket Id:" + ticket.getId());
		} else {
			// No Network, offline Ticket processing initilize value
			ticket.LastModifiedBy = vecvPreferences.getDevice_alias();

			// get current utc time without dash
			ticket.LastModifiedTime = UtilityFunction
					.currentUTCTimeWithoutDash();

			// set and instance value
			ticket.IsDeclined = "false";
			ticket.TicketStatus = "2";
			ticket.LastModifiedTimeInMilliSec = getModifiedTimeInMillisecond(ticket.LastModifiedTime)
					+ "";

			// calling async task loading
			new AsyncTask<Void, Void, Void>() {

				// define boolean varibale
				boolean isOperationSuccessfull;
				// define progress dialog
				ProgressDialog pd;

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#onPreExecute() Method is used to
				 * perform UI operation before starting background Service
				 */
				@Override
				protected void onPreExecute() {
					// TODO Auto-generated method stub
					super.onPreExecute();
					// set dialog context and set message value
					pd = new ProgressDialog(mContext);
					pd.setMessage(mContext.getResources().getString(
							R.string.please_wait));
					pd.show();
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#doInBackground(Params[]) Method is
				 * used to perform Background Task.
				 */
				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub

					isOperationSuccessfull = saveTicketOffline(ticket);
					return null;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
				 * Method is used to perform UI operation after background task
				 * will be finishing.
				 */

				@Override
				protected void onPostExecute(Void result) {
					// TODO Auto-generated method stub
					// check dialog showing or not
					if (pd.isShowing())
						// check dialog
						pd.dismiss();

					// check isOperationSuccessfull value
					if (isOperationSuccessfull) {

						// get value from getPendingTicketCount
						int count = vecvPreferences.getPendingTicketCount();

						// check count value
						if (false && count > 0) {
							// previous ticket consumeded
							vecvPreferences.decresePendingTicketsCount();
							// get new ticket from pending ticket queue

							// check and passing values in the intent of the key
							// "type"
							Intent in = new Intent(
									MyFirebaseMessagingService.PUSH_RECEIVE_MESSAGE_ACTION);
							in.putExtra("type", "pendingTicket");
							// send broadcast intent
							mContext.sendBroadcast(in);
						} else {

							// Move to Map screen is user is on any other
							// screen, otherwise
							// keep user on map screen

							ticket.isTicketOffLine = true;

							// updates seek bar timer activity
							// Intent in = new
							// Intent(mContext,MapViewTimerActivity.class);
							Intent in = new Intent(mContext,
									MapViewTimerActivitySeekBar.class);

							in.putExtra("ticket", ticket);
							mContext.startActivity(in);

							new VECVPreferences(mContext)
									.setNewTicketAssigned(false);

							MyTicketActivity.ifNewUpdatesAvailableInDb = true;
						}

						// Google Analytic - Event Notify
						EosApplication.getInstance().trackEvent("New Ticket",
								"Ticket Accepted in Offline Mode",
								"Ticket Id:" + ticket.getId());
					}

				}
			}.execute();

		}

	}

	// Api Call
	/**
	 * This method for passing decline tickets value to server.
	 * 
	 * @param ticket
	 */
	public void api_DeclineTicket(final Ticket ticket) {

		// create instance of the class VECVPreferences
		VECVPreferences vecvPreferences = new VECVPreferences(mContext);

		// get value from vecvPreferences and initialize them
		ticket.LastModifiedBy = vecvPreferences.getDevice_alias();
		ticket.LastModifiedTime = UtilityFunction.currentUTCTime();
		ticket.IsDeclined = "true";
		// check if network available
		if (new CheckInternetConnection(mContext).isConnectedToInternet()) {
			// if network available , call API
			new AcceptTicket(this, mContext,
					vecvPreferences.getSecurityToken(),
					vecvPreferences.getDevice_alias(), ticket.IsDeclined,
					ticket.getId(), ticket.LastModifiedBy,
					ticket.LastModifiedTime, null, null, null,
					ticket.SuggestionComment, ticket.Priority).execute();

			// Google Analytic - Event Notify
			EosApplication.getInstance().trackEvent("New Ticket",
					"Ticket Decline API Called", "Ticket Id:" + ticket.getId());
		} else {
			// if offline,save ticket state in OpenTicket and OpenTicketActivity
			// table locally,

			ticket.TicketStatus = "6";
			new AsyncTask<Void, Void, Void>() {

				// define boolean variable
				boolean isOperationSuccessfull;
				// define progress dialog
				ProgressDialog pd;

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#onPreExecute() Method is used to
				 * perform UI operation before starting background Service
				 */
				@Override
				protected void onPreExecute() {
					// TODO Auto-generated method stub
					super.onPreExecute();
					pd = new ProgressDialog(mContext);
					pd.setMessage(mContext.getResources().getString(
							R.string.please_wait));
					pd.show();
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#doInBackground(Params[]) Method is
				 * used to perform Background Task.
				 */
				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub

					isOperationSuccessfull = saveTicketOffline(ticket);
					return null;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
				 * Method is used to perform UI operation after background task
				 * will be finishing.
				 */
				@Override
				protected void onPostExecute(Void result) {
					// TODO Auto-generated method stub
					if (pd.isShowing())
						pd.dismiss();
					if (isOperationSuccessfull) {

					}

				}
			}.execute();

		}

	}

	// Api Call
	/**
	 * This method used for calling when van reached button press and call api
	 * sent value to server
	 * 
	 * @param ticket
	 */
	public void api_VanReached(final Ticket ticket) {

		// check internet
		if (new CheckInternetConnection(mContext).isConnectedToInternet()) {

			/**
			 * updates ticket status api calling and passing values from ticket
			 * data
			 * 
			 * mContext define current activity
			 */
			new UpdateTicketStatus(this, mContext, ticket.Id,
					ticket.SuggestionComment,
					ticket.EstimatedCostForJobComplition,
					ticket.EstimatedTimeForJobComplition,
					ticket.LastModifiedTime, ticket.LastModifiedBy,
					ticket.TicketStatus, ticket.slaMissedReasonId,
					ticket.isVanReachedConfirmed, ticket.Priority).execute();

			// Googel Analytic - Event
			EosApplication.getInstance().trackEvent("Assigned Ticket",
					"Van Reached", "Van Reached API called");
		} else {
			// defines VECVPreferences instance class
			final VECVPreferences vecvPreferences = new VECVPreferences(
					mContext);
			try {
				// get modified time in milli sec from TimeFormater class
				ticket.LastModifiedTimeInMilliSec = TimeFormater
						.getModifiedTimeInMillisecond(ticket.LastModifiedTime)
						+ "";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// Google Analytic -Tracking Exception
				EosApplication.getInstance().trackException(e);
				// save error in log file
				UtilityFunction.saveErrorLog(mContext, e);
			}
			// define IsDeclined value
			ticket.IsDeclined = "false";

			/**
			 * calling AsyncTask in background api
			 */
			new AsyncTask<Void, Void, Void>() {

				// define boolean variable
				boolean isOperationSuccessfull;

				// define progress dialog
				ProgressDialog pd;

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#onPreExecute() Method is used to
				 * perform UI operation before starting background Service
				 */
				@Override
				protected void onPreExecute() {
					// TODO Auto-generated method stub
					super.onPreExecute();
					pd = new ProgressDialog(mContext);
					pd.setMessage(mContext.getResources().getString(
							R.string.please_wait));
					pd.show();
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#doInBackground(Params[]) Method is
				 * used to perform Background Task.
				 */
				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub

					// save offline ticket
					isOperationSuccessfull = saveTicketOffline(ticket);
					return null;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
				 * 
				 * Method is used to perform UI operation after background task
				 * will be finishing.
				 */
				@Override
				protected void onPostExecute(Void result) {
					// TODO Auto-generated method stub
					// progress dialog check
					if (pd.isShowing())
						pd.dismiss();
					if (isOperationSuccessfull) {
						int count = vecvPreferences.getPendingTicketCount();

						if (false && count > 0) {
							// no more useable
							// previous ticket consumeded
							vecvPreferences.decresePendingTicketsCount();
							// get new ticket from pending ticket queue
							Intent in = new Intent(
									MyFirebaseMessagingService.PUSH_RECEIVE_MESSAGE_ACTION);
							in.putExtra("type", "pendingTicket");
							mContext.sendBroadcast(in);
						} else {

							if (ticket.EstimatedCostForJobComplition == null
									|| (ticket.EstimatedCostForJobComplition != null && ticket.EstimatedCostForJobComplition
											.equalsIgnoreCase("null"))) {
								// If User confirms Van reached in offline mode
								if (mContext != null
										&& mContext instanceof MapViewTimerActivitySeekBar) {
									MapViewTimerActivitySeekBar mapViewTimerActivitySeekBar = (MapViewTimerActivitySeekBar) mContext;
									mapViewTimerActivitySeekBar
											.showTimeEstimationDialog();
								}

							} else {
								// If User Fills forms after Van reached in
								// offline mode
								// Move to JobStatus activity
								Intent mainIntent = new Intent(mContext,
										JobStatusDetailActivity.class);

								MyTicketActivity.ifNewUpdatesAvailableInDb = true;
								if (ticket != null) {
									mainIntent.putExtra("ticket", ticket);
									mainIntent
											.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									mContext.startActivity(mainIntent);
								}
								if (!(mContext instanceof Application))
									((Activity) mContext).finish();
							}

						}
					}

				}
			}.execute();

			// Googel Analytic - Event
			EosApplication.getInstance().trackEvent("Assigned Ticket",
					"Van Reached", "Van Reached data saved Offline");

		}

	}

	// Api Call
	/**
	 * This method used for updated jobs completed time in local db
	 * 
	 * @param ticket
	 */
	public void api_UpdateJobCompleteEstimatedTime(final Ticket ticket) {

		// check internet
		if (new CheckInternetConnection(mContext).isConnectedToInternet()) {

			// call api for jobs estimation time
			new UpdateJobEstimatedTimeService(mContext, ticket.Id,
					ticket.EstimatedTimeForJobComplition,
					ticket.LastModifiedTime, ticket.LastModifiedBy,
					ticket.SuggestionComment, ticket.Priority).execute();
		} else {

			// create instance VECVPreferences class
			final VECVPreferences vecvPreferences = new VECVPreferences(
					mContext);
			ticket.LastModifiedBy = vecvPreferences.getDevice_alias();
			try {
				ticket.LastModifiedTimeInMilliSec = TimeFormater
						.getModifiedTimeInMillisecond(ticket.LastModifiedTime)
						+ "";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// Google Analytic -Tracking Exception
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(mContext, e);
			}
			new AsyncTask<Void, Void, Void>() {
				// define boolean value
				boolean isOperationSuccessfull;
				// define progress dialog
				ProgressDialog pd;

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#onPreExecute() Method is used to
				 * perform UI operation before starting background Service
				 */
				@Override
				protected void onPreExecute() {
					// TODO Auto-generated method stub
					super.onPreExecute();
					pd = new ProgressDialog(mContext);
					pd.setMessage(mContext.getResources().getString(
							R.string.update_estimation_time));
					pd.show();
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#doInBackground(Params[]) Method is
				 * used to perform Background Task.
				 */
				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub

					// save offline ticket data
					isOperationSuccessfull = saveTicketOffline(ticket);
					return null;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
				 * Method is used to perform UI operation after background task
				 * will be finishing.
				 */
				@Override
				protected void onPostExecute(Void result) {
					// TODO Auto-generated method stub

					// progress dialog check
					if (pd.isShowing())
						// dismiss dialog
						pd.dismiss();

					// check isOperationSuccessfull or not
					if (isOperationSuccessfull) {

						// check app value IS_APP_IN_FORGROUND
						if (ApplicationConstant.IS_APP_IN_FORGROUND) {
							// check instance currentActivityContext
							if (ApplicationConstant.currentActivityContext != null
									&& ApplicationConstant.currentActivityContext instanceof MyTicketActivity) {
								MyTicketActivity myTicketActivity = (MyTicketActivity) ApplicationConstant.currentActivityContext;

								// call and load updated tikcet
								myTicketActivity.loadUpdatedTickets();
							} else {
								/*
								 * Assign value ifNewUpdatesAvailableInDb
								 */
								MyTicketActivity.ifNewUpdatesAvailableInDb = true;
							}
						}

					}

				}
			}.execute();

		}

	}

	public void api_UpdateJobCompleteEstimatedCost(final Ticket ticket) {

		// check internet
		if (new CheckInternetConnection(mContext).isConnectedToInternet()) {

			// call api for jobs estimation time
			new UpdateJobEstimatedCostService(mContext, ticket.Id,
					ticket.EstimatedCostForJobComplition,
					ticket.LastModifiedTime, ticket.LastModifiedBy,
					ticket.SuggestionComment, ticket.Priority).execute();
		} else {

			// create instance VECVPreferences class
			final VECVPreferences vecvPreferences = new VECVPreferences(
					mContext);
			ticket.LastModifiedBy = vecvPreferences.getDevice_alias();
			try {
				ticket.LastModifiedTimeInMilliSec = TimeFormater
						.getModifiedTimeInMillisecond(ticket.LastModifiedTime)
						+ "";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// Google Analytic -Tracking Exception
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(mContext, e);
			}
			new AsyncTask<Void, Void, Void>() {
				// define boolean value
				boolean isOperationSuccessfull;
				// define progress dialog
				ProgressDialog pd;

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#onPreExecute() Method is used to
				 * perform UI operation before starting background Service
				 */
				@Override
				protected void onPreExecute() {
					// TODO Auto-generated method stub
					super.onPreExecute();
					pd = new ProgressDialog(mContext);
					pd.setMessage(mContext.getResources().getString(
							R.string.update_estimation_time));
					pd.show();
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#doInBackground(Params[]) Method is
				 * used to perform Background Task.
				 */
				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub

					// save offline ticket data
					isOperationSuccessfull = saveTicketOffline(ticket);
					return null;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
				 * Method is used to perform UI operation after background task
				 * will be finishing.
				 */
				@Override
				protected void onPostExecute(Void result) {
					// TODO Auto-generated method stub

					// progress dialog check
					if (pd.isShowing())
						// dismiss dialog
						pd.dismiss();

					// check isOperationSuccessfull or not
					if (isOperationSuccessfull) {

						// check app value IS_APP_IN_FORGROUND
						if (ApplicationConstant.IS_APP_IN_FORGROUND) {
							// check instance currentActivityContext
							if (ApplicationConstant.currentActivityContext != null
									&& ApplicationConstant.currentActivityContext instanceof MyTicketActivity) {
								MyTicketActivity myTicketActivity = (MyTicketActivity) ApplicationConstant.currentActivityContext;

								// call and load updated tikcet
								myTicketActivity.loadUpdatedTickets();
							} else {
								/*
								 * Assign value ifNewUpdatesAvailableInDb
								 */
								MyTicketActivity.ifNewUpdatesAvailableInDb = true;
							}
						}

					}

				}
			}.execute();

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.teramatrix.vecv.interfaces.INewTicket#api_VanReachedResponse()
	 */
	@Override
	public void api_VanReachedResponse() {

	}

	// Api Call
	/**
	 * This method used for calling api when press button job completed and save
	 * response in local db
	 * 
	 * @param ticket
	 */
	public void api_JobCompleted(final Ticket ticket) {

		// check internet
		if (new CheckInternetConnection(mContext).isConnectedToInternet()) {
			// update ticket status
			new UpdateTicketStatus(this, mContext, ticket).execute();

		} else {
			final VECVPreferences vecvPreferences = new VECVPreferences(
					mContext);
			try {
				ticket.LastModifiedTimeInMilliSec = TimeFormater
						.getModifiedTimeInMillisecond(ticket.LastModifiedTime)
						+ "";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// Google Analytic -Tracking Exception
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(mContext, e);
			}
			ticket.LastModifiedBy = vecvPreferences.getDevice_alias();
			ticket.IsDeclined = "false";
			// ticket.TicketStatus = "4";
			new AsyncTask<Void, Void, Void>() {
				// define boolean value
				boolean isOperationSuccessfull;
				// define progress dialog
				ProgressDialog pd;

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#onPreExecute() Method is used to
				 * perform UI operation before starting background Service
				 */
				@Override
				protected void onPreExecute() {
					// TODO Auto-generated method stub
					super.onPreExecute();
					pd = new ProgressDialog(mContext);
					pd.setMessage(mContext.getResources().getString(
							R.string.please_wait));
					pd.show();
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#doInBackground(Params[]) Method is
				 * used to perform Background Task.
				 */
				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub

					isOperationSuccessfull = saveTicketOffline(ticket);
					return null;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
				 * Method is used to perform UI operation after background task
				 * will be finishing.
				 */
				@Override
				protected void onPostExecute(Void result) {
					// TODO Auto-generated method stub
					if (pd.isShowing())
						pd.dismiss();
					if (isOperationSuccessfull) {
						int count = vecvPreferences.getPendingTicketCount();

						if (count > 0) {
							// previous ticket consumeded
							vecvPreferences.decresePendingTicketsCount();
							// get new ticket from pending ticket queue
							Intent in = new Intent(
									MyFirebaseMessagingService.PUSH_RECEIVE_MESSAGE_ACTION);
							in.putExtra("type", "pendingTicket");
							mContext.sendBroadcast(in);
						} else {

							// finish jobstatus activity
							MyTicketActivity.ifNewUpdatesAvailableInDb = true;
							if (!(mContext instanceof Application))
								((Activity) mContext).finish();
						}
					}

				}
			}.execute();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.teramatrix.vecv.interfaces.INewTicket#api_JobCompletedREsponse()
	 */
	@Override
	public void api_JobCompletedREsponse() {
		// TODO Auto-generated method stub

	}

	/*
	 * this method return a update ticket api response and save in local db
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.teramatrix.vecv.interfaces.INewTicket#api_TicketUpdateResponse(java
	 * .lang.String)
	 */
	@Override
	public void api_TicketUpdateResponse(String ticket_id) {
		// TODO Auto-generated method stub
		if (mContext instanceof MapViewTimerActivitySeekBar) {

			if (isOperationSuccessfull) {
				Intent mainIntent = new Intent(mContext,
						JobStatusDetailActivity.class);
				Ticket ticket = getTicketFromLocalDB(ticket_id);

				if (ticket != null) {
					mainIntent.putExtra("ticket", ticket);
					mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					mContext.startActivity(mainIntent);
				}
			}

		}

		if (!(mContext instanceof Application))
			((Activity) mContext).finish();

	}

	// Update Tickets in Local Db, returned from server
	/**
	 * 
	 * @param ticket
	 */
	public void updateTicketInLocalDB(Ticket ticket) {

	}

	/*
	 * update ticket in local db
	 */
	public void updateTicketsInLocalDB(ArrayList<Ticket> tickets) {

	}

	/**
	 * This method used to save open ticket in local db
	 * 
	 * @param opendTickets
	 */
	public void saveOpenTicketsInLocalDB(ArrayList<Ticket> opendTickets) {

		// check open ticket value
		if (opendTickets != null) {
			// create instace of the class DBInteraction
			DBInteraction dbInteraction = DBInteraction
					.getInstance(ApplicationConstant.currentActivityContext);
			// getting a connection
			dbInteraction.getConnection();

			// Deleing old opened tickets
			// dbInteraction.deleteMyTicketData();

			for (int p = 0; p < opendTickets.size(); p++) {

				Ticket ticket = opendTickets.get(p);
				long lastmodifiedtimeInmilli = 0;
				if (ticket.LastModifiedTime != null)
					lastmodifiedtimeInmilli = getModifiedTimeInMillisecond(ticket.LastModifiedTime);

				String lmt = String.valueOf(lastmodifiedtimeInmilli);

				// passing value and initialize them
				long inserted_row_index = dbInteraction.addOpenTicket(
						ticket.Id, ticket.TicketStatus,
						ticket.BreakDownLocation, ticket.BreackDownLatitude,
						ticket.BreackDownLongitude, ticket.vehicleType,
						ticket.Description,
						ticket.TotalTicketLifeCycleTimeSlab,
						ticket.SlaTimeAchevied, ticket.CreationTime,
						ticket.LastModifiedTime, lmt, ticket.Priority,
						ticket.VehicleRegistrationNo, ticket.vehicleType,
						ticket.EstimatedTimeForJobComplition,
						ticket.EstimatedCostForJobComplition,
						ticket.AcheviedEstimatedTimeForJobComplition,
						ticket.SuggestionComment, ticket.CustomeContact_no,
						ticket.EstimatedDistance, ticket.OwnerContact_no,
						ticket.IsTripEnd);

			}

			// close a database connection
			dbInteraction.closeConnection();
		}
	}

	/**
	 * This method used to save closed ticket in local db
	 * 
	 * @param closedTickets
	 */
	public void saveClosedTicketsInLocalDB(ArrayList<Ticket> closedTickets) {

		// check close ticket
		if (closedTickets != null && closedTickets.size() > 0) {
			// create instace of the class DBInteraction
			DBInteraction dbInteraction = DBInteraction
					.getInstance(ApplicationConstant.currentActivityContext);
			// getting a connection
			dbInteraction.getConnection();
			for (int p = 0; p < closedTickets.size(); p++) {
				// insert open ticket in open ticket table

				Ticket ticket = closedTickets.get(p);

				long lastmodifiedtimeInmilli = 0;
				if (ticket.LastModifiedTime != null)
					lastmodifiedtimeInmilli = getModifiedTimeInMillisecond(ticket.LastModifiedTime);

				String lmt = String.valueOf(lastmodifiedtimeInmilli);

				dbInteraction.addClosedTicket(ticket.Id, ticket.TicketStatus,
						ticket.BreakDownLocation, ticket.BreackDownLatitude,
						ticket.BreackDownLongitude, ticket.vehicleType,
						ticket.Description,
						ticket.TotalTicketLifeCycleTimeSlab,
						ticket.SlaTimeAchevied, ticket.LastModifiedTime,
						ticket.LastModifiedTime, lmt, ticket.Priority,
						ticket.VehicleRegistrationNo, ticket.vehicleType,
						ticket.EstimatedTimeForJobComplition,
						ticket.EstimatedCostForJobComplition,
						ticket.AcheviedEstimatedTimeForJobComplition,
						ticket.SuggestionComment, ticket.CustomeContact_no,
						ticket.EstimatedDistance, ticket.OwnerContact_no,
						ticket.IsTripEnd);
			}

			removeClosedTicketsFromOpenTicketTable(closedTickets, dbInteraction);

			// close a connnection
			dbInteraction.closeConnection();
		}
	}

	/**
	 * This method used for removing closed ticket from open ticket table
	 * 
	 * @param closedTickets
	 * @param dbInteraction
	 */
	public void removeClosedTicketsFromOpenTicketTable(
			ArrayList<Ticket> closedTickets, DBInteraction dbInteraction) {
		// if (closedTickets != null && closedTickets.size() > 0) {
		// DBInteraction dbInteraction = DBInteraction.getInstance();
		// dbInteraction.getConnection();
		for (int p = 0; p < closedTickets.size(); p++) {
			// insert open ticket in open ticket table

			Ticket ticket = closedTickets.get(p);
			dbInteraction.deleteClosedTicketFromOpenTicketsTable(ticket.Id);
		}
		// dbInteraction.closeConnection();
		// }
	}

	// Get Tickets saved in Local DB
	/**
	 * 
	 * @param ticketId
	 * @return
	 */
	public Ticket getTicketFromLocalDB(String ticketId) {
		// create instace of the class DBInteraction
		DBInteraction dbInteraction = DBInteraction
				.getInstance(ApplicationConstant.currentActivityContext);
		// getting a connection
		dbInteraction.getConnection();
		Ticket tickets = dbInteraction.getOpenTickets(ticketId);
		// close a connection
		dbInteraction.closeConnection();
		return tickets;
	}

	/**
	 * This method perform to get all the open ticket from local db
	 * 
	 * @return
	 */
	public ArrayList<Ticket> getAllOpenedTicketsFromLocalDB() {

		// create instace of the class DBInteraction
		DBInteraction dbInteraction = DBInteraction
				.getInstance(ApplicationConstant.currentActivityContext);
		// getting a connection
		dbInteraction.getConnection();
		// get the ticket from arraylist
		ArrayList<Ticket> tickets = dbInteraction.getAllOpenTickets();

		// read ticket value using loop
		for (Ticket ticket : tickets) {
			if (!ticket.TicketStatus.equalsIgnoreCase("5")) {
				// set status text value
				String statusText = dbInteraction
						.getStatusName(ticket.TicketStatus);
				// initialize status text value
				ticket.TicketStatusText = statusText;
			}
		}
		// close connection
		dbInteraction.closeConnection();
		return tickets;
	}

	/**
	 * This method perform to get all the ticket from local db
	 * 
	 * @return
	 */
	public ArrayList<Ticket> getAllClosedTicketsFromLocalDB() {

		// create instace of the class DBInteraction
		DBInteraction dbInteraction = DBInteraction
				.getInstance(ApplicationConstant.currentActivityContext);
		// getting a connection
		dbInteraction.getConnection();
		// get the ticket from arraylist
		ArrayList<Ticket> tickets = dbInteraction.getAllClosedTickets();

		// close the connection
		dbInteraction.closeConnection();
		return tickets;
	}

	// Override from INewTicket Interface
	@Override
	public void setNewTicket(Ticket ticket) {
		// TODO Auto-generated method stub
	}

	/*
	 * This method set value for accepting a ticket (non-Javadoc)
	 * 
	 * @see com.teramatrix.vecv.interfaces.INewTicket#setAcceptedTicket(boolean,
	 * java.lang.String)
	 */
	@Override
	public void setAcceptedTicket(boolean isAccepted, String ticketId) {
		// TODO Auto-generated method stub
		// saveAcceptedTicketInLocalDB(ticket);
		VECVPreferences vecvPreferences = new VECVPreferences(mContext);
		int count = vecvPreferences.getPendingTicketCount();

		/*
		 * Check value isAccepted
		 */
		if (isAccepted) {

			if (false && count > 0) {
				// Disable this Functinality

				// previous ticket consumeded
				vecvPreferences.decresePendingTicketsCount();
				// get new ticket from pending ticket queue
				Intent in = new Intent(
						MyFirebaseMessagingService.PUSH_RECEIVE_MESSAGE_ACTION);
				in.putExtra("type", "pendingTicket");
				mContext.sendBroadcast(in);
			} else {

				// Move to Map screen is user is on any other screen, otherwise
				// keep user on map screen
				if (ApplicationConstant.IS_APP_IN_FORGROUND
						&& !(ApplicationConstant.currentActivityContext instanceof MapViewTimerActivitySeekBar)) {
					Ticket newTicket = getTicketFromLocalDB(ticketId);

					Intent in = new Intent(mContext,
							MapViewTimerActivitySeekBar.class);

					in.putExtra("ticket", newTicket);
					mContext.startActivity(in);

					new VECVPreferences(mContext).setNewTicketAssigned(false);
				}
			}

			if (AcceptTicket.mProgressDialog != null
					&& AcceptTicket.mProgressDialog.isShowing()) {
				AcceptTicket.mProgressDialog.dismiss();
			}
		} else {
			if (count > 0) {
				vecvPreferences.decresePendingTicketsCount();
				Intent in = new Intent(
						MyFirebaseMessagingService.PUSH_RECEIVE_MESSAGE_ACTION);
				in.putExtra("type", "pendingTicket");
				mContext.sendBroadcast(in);
			}
		}
	}

	/**
	 * This method get value Modified TimeInMillisecond.
	 * 
	 * @param lastModifiedTime
	 * @return
	 */
	private long getModifiedTimeInMillisecond(String lastModifiedTime) {

		// 02-JAN-2015 02:15:25
		String definedFormate = "dd MMM yyyy HH:mm:ss";
		// Create an instance of the SimpleDateFormat
		SimpleDateFormat sdf = new SimpleDateFormat(definedFormate);
		try {
			// parse time
			Date formatedDate = sdf.parse(lastModifiedTime);
			// return date in the specified format
			return formatedDate.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);
			// save error log when exception is created
			UtilityFunction.saveErrorLog(mContext, e);
		}
		return 0;
	}

	// save offline ticket in local db
	/**
	 * 
	 * @param ticket
	 * @return
	 */
	public boolean saveTicketOffline(Ticket ticket) {
		if (ticket == null)
			return false;

		// Offline Ticket save Logging
		if (ApplicationConstant.IS_LOG_SHOWN) {
			String log_message = "Save Ticket in OfflineMode:" + " "
					+ "TicketID:" + ticket.Id + " TicketState:"
					+ ticket.TicketStatus + " isDecline:" + ticket.IsDeclined;
			CompleteLogging.logOffLineTicketing(mContext, log_message);
		}

		//Get local db connection
		DBInteraction dbInteraction = DBInteraction
				.getInstance(ApplicationConstant.currentActivityContext);
		dbInteraction.getConnection();
		
		if (ticket.TicketStatus == null
				|| ticket.TicketStatus.equalsIgnoreCase("null"))
			return false;

		if (ticket.TicketStatus.equalsIgnoreCase("2")
				|| ticket.TicketStatus.equalsIgnoreCase("3")
				|| ticket.TicketStatus.equalsIgnoreCase("4")
				|| ticket.TicketStatus.equalsIgnoreCase("9")) {
			// Ticket state becomes Accepted(2) or VanReached(3) or
			// JobCompleted(4)
			// Add Ticket to OpenTicketTable when ticket state is 2 ,or update
			// existing ticket in OpenTicketTable when ticket state is 3 or 4
			long l = dbInteraction.addOpenTicket(ticket.Id,
					ticket.TicketStatus, ticket.BreakDownLocation,
					ticket.BreackDownLatitude, ticket.BreackDownLongitude,
					ticket.vehicleType, ticket.Description,
					ticket.TotalTicketLifeCycleTimeSlab,
					ticket.SlaTimeAchevied, ticket.CreationTime,
					ticket.LastModifiedTime, ticket.LastModifiedTimeInMilliSec,
					ticket.Priority, ticket.VehicleRegistrationNo,
					ticket.vehicleType, ticket.EstimatedTimeForJobComplition,
					ticket.EstimatedCostForJobComplition,
					ticket.AcheviedEstimatedTimeForJobComplition,
					ticket.SuggestionComment, ticket.CustomeContact_no,
					ticket.EstimatedDistance, ticket.OwnerContact_no,
					ticket.IsTripEnd);

			
			// Add each state of Ticket to OpenTicketActivity
			long insertValue = dbInteraction.saveTicketOffline(ticket);
			
			//close db connection	
			dbInteraction.closeConnection();
			//return this operation status 
			if (insertValue > 0)
				return true;
		} else if (ticket.TicketStatus.equalsIgnoreCase("6")) {
			// Ticket state becomes Declined(6)
			// Add Ticket to OpenTicketActivity Table for ticket state 6, avoid
			// adding it to OpenTicketTable since it is now declined.
			long insertValue = dbInteraction.saveTicketOffline(ticket);
			dbInteraction.closeConnection();
			if (insertValue > 0)
				return true;
		} else if (ticket.TicketStatus.equalsIgnoreCase("8")) {
			// Ticket States is 8 (End Trip Action)
			// There can be two cases for End Trip
			// 1. Pre-Closed Ticket -> End Trip
			// 2. Closed Ticket(trip not ended) -> End Trip

			if (ticket.Ticket_Previous_Status.equalsIgnoreCase("4")) {
				// case 1.Pre-Closed Ticket -> End Trip
				
				//Trip is Ended so ...
				ticket.IsTripEnd = "true";
				
				//Update this ticket in open ticket table
				long l = dbInteraction.addOpenTicket(ticket.Id,
						ticket.TicketStatus, ticket.BreakDownLocation,
						ticket.BreackDownLatitude, ticket.BreackDownLongitude,
						ticket.vehicleType, ticket.Description,
						ticket.TotalTicketLifeCycleTimeSlab,
						ticket.SlaTimeAchevied, ticket.CreationTime,
						ticket.LastModifiedTime, ticket.LastModifiedTimeInMilliSec,
						ticket.Priority, ticket.VehicleRegistrationNo,
						ticket.vehicleType, ticket.EstimatedTimeForJobComplition,
						ticket.EstimatedCostForJobComplition,
						ticket.AcheviedEstimatedTimeForJobComplition,
						ticket.SuggestionComment, ticket.CustomeContact_no,
						ticket.EstimatedDistance, ticket.OwnerContact_no,
						ticket.IsTripEnd);

				
				// Add this activity in OpenTicketActivity table so it can be synched to server when network/server is up.
				long insertValue = dbInteraction.saveTicketOffline(ticket);

				//close db connection
				dbInteraction.closeConnection();
				
				//return this operation status 
				if (insertValue > 0)
					return true;

			} else if (ticket.Ticket_Previous_Status.equalsIgnoreCase("5")) {
				// 2. Closed Ticket(trip not ended) -> End Trip
				
				//Trip is Ended so ...
				ticket.IsTripEnd = "true";
				
				//Update this already closed Ticket in Closed Ticket Table  
				dbInteraction.addClosedTicket(ticket.Id, ticket.TicketStatus,
						ticket.BreakDownLocation, ticket.BreackDownLatitude,
						ticket.BreackDownLongitude, ticket.vehicleType,
						ticket.Description,
						ticket.TotalTicketLifeCycleTimeSlab,
						ticket.SlaTimeAchevied, ticket.LastModifiedTime,
						ticket.LastModifiedTime, ticket.LastModifiedTimeInMilliSec, ticket.Priority,
						ticket.VehicleRegistrationNo, ticket.vehicleType,
						ticket.EstimatedTimeForJobComplition,
						ticket.EstimatedCostForJobComplition,
						ticket.AcheviedEstimatedTimeForJobComplition,
						ticket.SuggestionComment, ticket.CustomeContact_no,
						ticket.EstimatedDistance, ticket.OwnerContact_no,
						ticket.IsTripEnd);
				
				// Add this offline activity in OpenTicketActivity so it can be synched to server when network/server is up.
				long insertValue = dbInteraction.saveTicketOffline(ticket);

				//Close db connection
				dbInteraction.closeConnection();
				
				//return this operation status 
				if (insertValue > 0)
					return true;
				
			}

		}

		dbInteraction.closeConnection();
		return false;

	}

	public int updateCounter_EditEstiamtedCost(Ticket ticket) {
		DBInteraction dbInteraction = DBInteraction
				.getInstance(ApplicationConstant.currentActivityContext);
		// getting a connection
		dbInteraction.getConnection();
		// get the ticket from arraylist
		long count = dbInteraction.updateCounter_EstimatedCostCounter(ticket);

		// close the connection
		dbInteraction.closeConnection();
		return 1;
	}

	public int updateCounter_EditEstiamtedTime(Ticket ticket) {
		DBInteraction dbInteraction = DBInteraction
				.getInstance(ApplicationConstant.currentActivityContext);
		// getting a connection
		dbInteraction.getConnection();
		// get the ticket from arraylist
		long count = dbInteraction.updateCounter_EstimatedTimeCounter(ticket);

		// System.out.println("estimated_time_update_counter:"+count);
		// close the connection
		dbInteraction.closeConnection();
		return 1;
	}

	public UpdateCounter getUpdateCounters(String ticket_id) {
		DBInteraction dbInteraction = DBInteraction
				.getInstance(ApplicationConstant.currentActivityContext);
		// getting a connection
		dbInteraction.getConnection();
		// get the ticket from arraylist
		UpdateCounter counter = dbInteraction
				.getEstiamteTimeUpgradeCount(ticket_id);

		// System.out.println("estimated_time_update_counter:"+count);
		// close the connection
		dbInteraction.closeConnection();
		return counter;
	}

	public void downloadEstimatedCostListFromServer() {
		try {
			RestIntraction restIntraction = new RestIntraction(
					new VECVPreferences(mContext).getAPIEndPoint_EOS() + ""
							+ ApiUrls.ESTIMATION_COST_LIST);
			restIntraction.AddParam("Token",
					new VECVPreferences(mContext).getSecurityToken());

			restIntraction.toString();
			restIntraction.Execute(0);
			String response3 = restIntraction.getResponse();

			ArrayList<EstimationCostModel> arrayList3 = TicketJsonParser
					.getEstimationCostList(response3);

			if (arrayList3 != null && arrayList3.size() > 0) {
				DBInteraction dbInteraction = DBInteraction
						.getInstance(mContext);
				dbInteraction.getConnection();
				for (int i = 0; i < arrayList3.size(); i++) {
					EstimationCostModel cost = arrayList3.get(i);
					dbInteraction.fillEstimationCostTable(cost.id,
							cost.cost_range);
					}
				dbInteraction.closeConnection();
			}
		} catch (Exception e) {
			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);
			e.printStackTrace();
		}
	}

	public void downloadDeclineTicketReasonListFromServer() {
		try {
			RestIntraction restIntraction = new RestIntraction(
					new VECVPreferences(mContext).getAPIEndPoint_EOS() + ""
							+ ApiUrls.TICKET_ACTION_REASONS_LIST);
			restIntraction.AddParam("Token",
					new VECVPreferences(mContext).getSecurityToken());

			restIntraction.toString();
			restIntraction.Execute(0);
			String response2 = restIntraction.getResponse();

			ArrayList<DeclineReasonModel> arrayList2 = TicketJsonParser
					.getTicketActionReasonsList(response2);

			if (arrayList2 != null && arrayList2.size() > 0) {
				DBInteraction dbInteraction = DBInteraction
						.getInstance(mContext);
				dbInteraction.getConnection();
				for (int i = 0; i < arrayList2.size(); i++) {
					DeclineReasonModel reason = arrayList2.get(i);
					dbInteraction.fillTicketActionReasonsTable(reason.id,
							reason.reason_type, reason.reason_name);

				}
				dbInteraction.closeConnection();
			}

		} catch (Exception e) {
			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);
			e.printStackTrace();
		}
	}

}
