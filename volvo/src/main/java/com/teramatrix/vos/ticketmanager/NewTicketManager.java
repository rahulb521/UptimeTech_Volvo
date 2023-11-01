package com.teramatrix.vos.ticketmanager;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.R;
import com.teramatrix.vos.adapter.DeclineReasonAdapter;
import com.teramatrix.vos.asynctasks.GetOpenTicket;
import com.teramatrix.vos.checkinternet.CheckInternetConnection;
import com.teramatrix.vos.database.DBInteraction;
import com.teramatrix.vos.interfaces.INewTicket;
import com.teramatrix.vos.logs.CompleteLogging;
import com.teramatrix.vos.model.DeclineReasonModel;
import com.teramatrix.vos.model.Ticket;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.reciver.NewTicketAlertRinger;
import com.teramatrix.vos.service.AlertRingerService;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.TimeFormater;
import com.teramatrix.vos.utils.UtilityFunction;

/**
 * This class perform operation for receive push on device,pull to
 * refresh,ticket popup
 * getNewTicketFromServerForPullRefreshRequest,saveNewTicketDetailInDB
 * ,getSavedNewTicketFromLocalDB
 * ,removeNewTicketFromLocalDB,clearPrefrenceForNewTicket,
 * setNewTicket,showTicketAlert
 * ,showDeclineTicketDialog,clearNewTicketAlert,addReasonListData
 * ,generateNotification,startNewTicketRinger,stopNewTicketRinger
 * cancelNotification for foreground application
 * 
 */
/* Singleton */
public class NewTicketManager implements INewTicket {

	// define instace for new tickey manager class
	private static NewTicketManager newTicketManager = null;
	// define context for the class instace
	private static Context mContext;
	// define boolean variable isNewRinger is playing or not
	public static boolean isNewTicketRingerPlaying;
	// define progress dialog box instance
	static ProgressDialog progressDialog;
	// define notification id
	public static int NOTIFICATION_ID = 1234;
	// define showticket dialog instance
	private Dialog showTicketDialog;
	// define decline ticket dialog instance
	private Dialog declineTicketDialog;

	Dialog confirmjobDialog;
	/**
	 * define constructor no argument
	 */
	private NewTicketManager() {
	}

	/**
	 * 
	 * @param context
	 * @return
	 * 
	 *         Getting instacne of the current activity
	 */
	public static NewTicketManager getInstance(Context context) {
		if (ApplicationConstant.IS_APP_IN_FORGROUND)
			mContext = ApplicationConstant.currentActivityContext;
		else
			mContext = context;

		if (newTicketManager == null)
			// get new ticket manager instacne
			newTicketManager = new NewTicketManager();

		return newTicketManager;
	}

	// called from BroadcastReceviers(available in
	// MyTicketActivity,MapViewTimerActivity,JobStatusDetailActivity) when new
	// GCM push for newTicket is recevied from server
	public void receviedNewTicketPush(boolean is_app_in_forground) {

		// save newticket arrival status
		VECVPreferences vecvPreferences = new VECVPreferences(mContext);
		// set boolean value for new ticket assigned
		vecvPreferences.setNewTicketAssigned(true);
		// if App is in forground call newTicket api
		if (is_app_in_forground) {
			// get ticket from server
			getNewTicketFromServer();
		} else {
			// app is in background, put this gcm push into pendingTicket
			// queue(update Pending Ticket counter)
			new VECVPreferences(mContext).addPendingTicketsCount();
			// Generate status bar noticication.
			//generateNotification(mContext, "New Ticket Push");
			generateNotification_New(mContext, "New Ticket Push");
			// check if ringer for new ticket is already ringing,if not , start
			// ringer.
			if (!isNewTicketRingerPlaying)

				// start ringer when new ticket on device
				startNewTicketRinger();
		}
	}

	/**
	 * This method used for pull to refresh option for new ticket and open
	 * ticket
	 * 
	 * @param is_app_in_forground
	 */
	public void pullToRefreshForNewTicketOrOpenedTicketUpdate(
			boolean is_app_in_forground) {

		// save newticket arrival status
		VECVPreferences vecvPreferences = new VECVPreferences(mContext);
		vecvPreferences.setNewTicketAssigned(true);
		// if App is in forground call newTicket api
		if (is_app_in_forground) {

			// get new ticket from server pull to refresh options
			getNewTicketFromServerForPullRefreshRequest();
		}

		// close new Ticket Ringer if on
		if (isNewTicketRingerPlaying)
			// stop ringer
			stopNewTicketRinger();
	}

	/*
	 * check for new ticket
	 */
	public void checkForNewTicket() {
		// create instance of VECVPreferences class
		VECVPreferences vecvPreferences = new VECVPreferences(mContext);
		// get the value count pending tickets
		int count = vecvPreferences.getPendingTicketCount();
		// check count value and is new ticket assigned value
		if (count > 0 || vecvPreferences.isNewTicketAssigned()) {
			// check new ticket exist in local db
			if (vecvPreferences.isNewTicketExistsInDB()) {
				// show ticket popup from db
				Ticket ticket = getSavedNewTicketFromLocalDB();
				// show ticket alerts
				showTicketAlert(ticket);
			} else {
				// show ticket after geting ticket from server
				getNewTicketFromServer();
			}
		}
	}

	// get new ticket from server
	private void getNewTicketFromServer() {
		// showLoadingDialog();

		// check internet connection
		if (new CheckInternetConnection(mContext).isConnectedToInternet()) {
			// create instance VECVPreferences class
			VECVPreferences vecvPreferences = new VECVPreferences(mContext);
			// get token using perferences.
			String token = vecvPreferences.getSecurityToken();
			// get device alias
			String assigned_to = vecvPreferences.getDevice_alias();

			// Recode log
			if (ApplicationConstant.IS_LOG_SHOWN) {
				CompleteLogging.logNewTicketApiCall(mContext,
						"Asyn Task execute for New Ticket Api");
			}

			new GetOpenTicket(mContext, this, token, assigned_to,
					ApplicationConstant.FLAG_NEW_ASSIGNED_TICKET).execute();
		} else {
			/*
			 * UtilityFunction.showCenteredToast(mContext,
			 * "Please connect to working internet connection");
			 */
		}
	}

	// get new ticket from server when pull to refresh request
	private void getNewTicketFromServerForPullRefreshRequest() {
		// showLoadingDialog();

		// check internet connection
		if (new CheckInternetConnection(mContext).isConnectedToInternet()) {
			VECVPreferences vecvPreferences = new VECVPreferences(mContext);
			String token = vecvPreferences.getSecurityToken();
			String assigned_to = vecvPreferences.getDevice_alias();
			// call api for new ticket assigned or not
			new GetOpenTicket(mContext, this, token, assigned_to,
					ApplicationConstant.FLAG_NEW_ASSIGNED_TICKET, true)
					.execute();
		} else {
			// show toast message
			/*
			 * UtilityFunction.showCenteredToast(mContext,
			 * "Please connect to working internet connection");
			 */
		}
	}

	/**
	 * save new ticket details in local db
	 * 
	 * @param ticket
	 */
	private void saveNewTicketDetailInDB(Ticket ticket) {

		try {
			// create instance of the DBInteraction class
			DBInteraction dbInteraction = DBInteraction
					.getInstance(ApplicationConstant.currentActivityContext);

			// get the databsae connection
			dbInteraction.getConnection();

			// get and assign value
			String ticket_id = ticket.getId();
			String breakdown_location = ticket.getBreakDownLocation();
			String breakdown_latitude = ticket.getBreackDownLatitude();
			String breakdown_longitude = ticket.getBreackDownLongitude();
			String breakdown_vehical_type = ticket.vehicleType;
			String problem_description = ticket.getDescription();
			String time_sla = ticket.getTotalTicketLifeCycleTimeSlab();
			String time_sla_acheived = ticket.SlaTimeAchevied;
			String time_ticket_assigned_time = ticket.getCreationTime();
			String vehical_type = ticket.vehicleType;
			String customer_contact_number = ticket.CustomeContact_no;
			String OwnerContact_no = ticket.OwnerContact_no;
			String priority = ticket.Priority+"";

			// String creationTimeInMillisecond =
			// ticket.LastModifiedTimeInMilliSec ;
			ticket.LastModifiedTimeInMilliSec = TimeFormater
					.getModifiedTimeInMillisecond(time_ticket_assigned_time)
					+ "";

			// passing value in constructor addNewTicket
			dbInteraction.addNewTicket(ticket_id,
					breakdown_location, breakdown_latitude,
					breakdown_longitude, breakdown_vehical_type,
					problem_description, time_sla, time_sla_acheived,
					time_ticket_assigned_time, vehical_type,
					ticket.LastModifiedTimeInMilliSec, customer_contact_number,
					OwnerContact_no,priority);

			// close db connection
			dbInteraction.closeConnection();

			// set preference for newticket saved in local db
			new VECVPreferences(mContext).setNewTicketSavedInDB(true);

		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			// save error log when exception is created
			UtilityFunction.saveErrorLog(mContext, e);
		}
	}

	/**
	 * This method used for get save new ticket from local db
	 * 
	 * @return
	 */
	private Ticket getSavedNewTicketFromLocalDB() {

		try {
			// define a instance of the class DBInteraction
			DBInteraction dbInteraction = DBInteraction
					.getInstance(ApplicationConstant.currentActivityContext);
			// getting a databse connection
			dbInteraction.getConnection();
			// save new ticket in local db
			Ticket ticket = dbInteraction.getSavedNewTicket();
			// close database connection
			dbInteraction.closeConnection();
			return ticket;
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			// save error log when exception is created
			UtilityFunction.saveErrorLog(mContext, e);
		}
		return null;
	}

	/**
	 * This method used for remove new ticket from local database
	 */

	private void removeNewTicketFromLocalDB() {

		try {
			// define a instance of the clas DBInteraction
			DBInteraction dbInteraction = DBInteraction
					.getInstance(ApplicationConstant.currentActivityContext);
			// getting a database connection
			dbInteraction.getConnection();
			// delete new ticket in local db
			dbInteraction.deleteNewTicketData();
			// close a database connectino
			dbInteraction.closeConnection();
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			// save error log when exception is created
			UtilityFunction.saveErrorLog(mContext, e);
		}
	}

	/**
	 * This method used for clear a preference for new ticket
	 */
	private void clearPrefrenceForNewTicket() {
		// define a preference of the VECVPreferences class
		VECVPreferences vecvPreferences = new VECVPreferences(mContext);
		// asign boolean value setting
		vecvPreferences.setNewTicketAssigned(false);
		vecvPreferences.setNewTicketSavedInDB(false);
		vecvPreferences.setNewAssignedTicketId("");
	}

	/*
	 * This method used for set new ticket in local db (non-Javadoc)
	 * 
	 * @see
	 * com.teramatrix.vecv.interfaces.INewTicket#setNewTicket(com.teramatrix
	 * .vecv.model.Ticket)
	 */
	@Override
	public void setNewTicket(Ticket ticket) {
		// TODO Auto-generated method stub

		// hideLoadingDialog();
		if (ticket != null) {
			// Log Recode
			if (ApplicationConstant.IS_LOG_SHOWN) {
				// assign string value
				String log_message = "Response" + " " + "TicketId:" + ticket.Id
						+ " CreationTime:" + ticket.LastModifiedTime;
				// set Complete Logging in file
				CompleteLogging.logNewTicketApiCall(mContext, log_message);
			}

			// save new ticket details data in local db
			saveNewTicketDetailInDB(ticket);
			// check and start ringer
			if (!isNewTicketRingerPlaying)
				startNewTicketRinger();
			// show new ticket alert
			showTicketAlert(ticket);

		} else {

			// Log Recode
			if (ApplicationConstant.IS_LOG_SHOWN) {
				// define string value
				String log_message = "Response" + " " + "Ticket Null";
				// set Complete Logging in file
				CompleteLogging.logNewTicketApiCall(mContext, log_message);
			}

			// No more Ticket available on server , so there is no pending
			// tickets ,set pending ticket count to 0
			new VECVPreferences(mContext).addPendingTicketsCount(0);
			// check instance of an activity
			if (ApplicationConstant.IS_APP_IN_FORGROUND
					&& (ApplicationConstant.currentActivityContext instanceof MyTicketActivity)) {

				// Load updated tickets on MYTicketActivity from local db
				((MyTicketActivity) ApplicationConstant.currentActivityContext)
						.loadUpdatedTickets();

				// set new ticket assigned flag false ,so when opening app again
				// there will be no api call for geting newTicket from server.
				new VECVPreferences(mContext).setNewTicketAssigned(false);

			}

			// No ticket available on server , so if there is ringer playing in
			// background stop it.
			if (isNewTicketRingerPlaying)
				// stop ringer
				stopNewTicketRinger();
		}

	}

	/**
	 * This method define for show ticket details data in device pop up assign
	 * from server
	 * 
	 * @param ticket
	 */
	private void showTicketAlert(final Ticket ticket) {
		MyTicketActivity.isNewTicketPopUpShowing = true;
		try {
			// show dialog
			showTicketDialog = new Dialog(mContext,
					android.R.style.Theme_Translucent);
			// dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

			showTicketDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			showTicketDialog.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			showTicketDialog.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			// set Cancelable value false
			showTicketDialog.setCancelable(false);

			View screen_new_ticket_popup = LayoutInflater.from(mContext)
					.inflate(R.layout.screen_new_ticket_popup, null);

			View center_pop_up = screen_new_ticket_popup
					.findViewById(R.id.center_pop_up);
			LayoutParams center_pop_up_layout_parm = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			int popup_h = 0;
			if (MyTicketActivity.ScreenDpi > 150
					&& MyTicketActivity.ScreenDpi < 200) {
				popup_h = (int) (MyTicketActivity.ScreenHeight * 0.6);
			} else if (MyTicketActivity.ScreenDpi > 200
					&& MyTicketActivity.ScreenDpi < 300) {
				popup_h = (int) (MyTicketActivity.ScreenHeight * 0.7);
			} else if (MyTicketActivity.ScreenDpi > 300) {
				popup_h = (int) (MyTicketActivity.ScreenHeight * 0.74);
			}
			int popup_w = (int) (MyTicketActivity.ScreenWidth * 0.8);

			int paop_up_margin_top_bottom = (MyTicketActivity.ScreenHeight - popup_h) / 2;
			int paop_up_margin_lefy_right = (MyTicketActivity.ScreenWidth - popup_w) / 2;

			center_pop_up_layout_parm.leftMargin = paop_up_margin_lefy_right;
			center_pop_up_layout_parm.rightMargin = paop_up_margin_lefy_right;
			center_pop_up_layout_parm.topMargin = paop_up_margin_top_bottom;
			center_pop_up_layout_parm.bottomMargin = paop_up_margin_top_bottom;

			center_pop_up.setLayoutParams(center_pop_up_layout_parm);

			// set layout
			showTicketDialog.setContentView(screen_new_ticket_popup);

			// initialization component
			TextView txt_location = (TextView) showTicketDialog
					.findViewById(R.id.value);
			TextView txt_vehical_type = (TextView) showTicketDialog
					.findViewById(R.id.value1);
			TextView txt_problem_des = (TextView) showTicketDialog
					.findViewById(R.id.value2);
			TextView txt_sla_time = (TextView) showTicketDialog
					.findViewById(R.id.value3);
			TextView txt_creation_time = (TextView) showTicketDialog
					.findViewById(R.id.value4);
			TextView txt_contacct_value = (TextView) showTicketDialog
					.findViewById(R.id.txt_contacct_value);
			TextView txt_contacct_value_owner = (TextView) showTicketDialog
					.findViewById(R.id.txt_contacct_value_owner);

			TextView distance_value = (TextView) showTicketDialog
					.findViewById(R.id.distance_value);

			TextView txt_nature_of_problem = (TextView)showTicketDialog.findViewById(R.id.text2);
			
			
			View relative_contact_number = showTicketDialog
					.findViewById(R.id.relative_contact_number);
			View relative_contact_number_owner = showTicketDialog
					.findViewById(R.id.relative_contact_number_owner);

			
			if(ticket.Priority!=null && ticket.Priority.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC)){
				txt_nature_of_problem.setText("DTC Information");
			}
			
			// asssign string variable
			final String Customer_PhoneNum = ticket.CustomeContact_no + "";
			final String Owner_PhoneNum = ticket.OwnerContact_no + "";

			// make a click contact then call on shown number
			relative_contact_number.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					// passing and intialize numnber
					// TODO Auto-generated method stub
					Intent callIntent = new Intent(Intent.ACTION_DIAL);
					callIntent.setData(Uri.parse("tel:"
							+ Uri.encode("+91-" + Customer_PhoneNum.trim())));
					callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// start activity dialog
					mContext.startActivity(callIntent);
				}
			});

			relative_contact_number_owner
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							// passing and intialize numnber
							// TODO Auto-generated method stub
							Intent callIntent = new Intent(Intent.ACTION_DIAL);
							callIntent.setData(Uri.parse("tel:"
									+ Uri.encode("+91-" + Owner_PhoneNum.trim())));
							callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							// start activity dialog
							mContext.startActivity(callIntent);
						}
					});

			// set text value from ticket
			txt_location.setText(ticket.getBreakDownLocation());

			txt_vehical_type.setText(ticket.vehicleType);
			txt_problem_des.setText(ticket.getDescription());

			if (ticket.getTotalTicketLifeCycleTimeSlab() != null
					&& !ticket.getTotalTicketLifeCycleTimeSlab()
							.equalsIgnoreCase("null"))
				txt_sla_time.setText(TimeFormater.convertMinutesToHMmSs(Long
						.parseLong(ticket.TotalTicketLifeCycleTimeSlab)));
			else
				txt_sla_time.setText("120");

			txt_creation_time
					.setText(TimeFormater
							.getTimeInCurrentTimeZone(ticket.LastModifiedTimeInMilliSec));

			txt_contacct_value.setText("+91-" + ticket.CustomeContact_no);
			txt_contacct_value_owner.setText("+91-" + ticket.OwnerContact_no);

			float distance_value_total;
			try {
				if (ticket.EstimatedDistance != null
						&& !ticket.EstimatedDistance.equalsIgnoreCase("null")) {

					distance_value_total = NumberFormat
							.getNumberInstance(java.util.Locale.US)
							.parse(ticket.EstimatedDistance).floatValue() * 2;
					distance_value.setText(ticket.EstimatedDistance + "+"
							+ ticket.EstimatedDistance + "="
							+ distance_value_total);

				}
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// define textview
			TextView tv_decline, tv_accept;
			// define component in textview
			tv_decline = (TextView) showTicketDialog
					.findViewById(R.id.tv_decline);
			tv_accept = (TextView) showTicketDialog
					.findViewById(R.id.tv_accept);

			// click decline ticket option
			tv_decline.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					DBInteraction dbInteraction = DBInteraction
							.getInstance(ApplicationConstant.currentActivityContext);
					// reason type id for decline
					String typeid = mContext.getResources().getString(
							R.string.ticket_action_reason_type_decline_ticket);

					// get the connection of the class
					dbInteraction.getConnection();

					// define arraylist data
					ArrayList<DeclineReasonModel> declineReasons = dbInteraction
							.getReasonList(typeid);
					// close a connection
					dbInteraction.closeConnection();

					if (declineReasons == null
							|| (declineReasons != null && declineReasons.size() == 0)) {
						new AsyncTask<Void, Void, Void>() {

							ProgressDialog pd;

							@Override
							protected void onPreExecute() {
								// TODO Auto-generated method stub
								super.onPreExecute();
								pd = new ProgressDialog(mContext);
								pd.setMessage("Updating decline reason from server...");
								pd.setCancelable(false);
								pd.show();
							}

							@Override
							protected Void doInBackground(Void... params) {
								// TODO Auto-generated method stub

								// Get Estiamtion cost list from server
								MyTicketManager
										.getInstance(mContext)
										.downloadDeclineTicketReasonListFromServer();
								return null;
							}

							@Override
							protected void onPostExecute(Void result) {
								// TODO Auto-generated method stub
								super.onPostExecute(result);
								if (pd.isShowing())
									pd.dismiss();
								showDeclineTicketDialog(ticket,
										showTicketDialog);
							}
						}.execute();
					} else {
						showDeclineTicketDialog(ticket, showTicketDialog);
					}

				}
			});

			// click tv_accept ticket option
			tv_accept.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					// clear new ticket alert
					clearNewTicketAlert();
					// call api for accept a ticket
					MyTicketManager.getInstance(mContext).api_AcceptTicket(
							ticket);
					
					//Google Analytic - Event Notify
					EosApplication.getInstance().trackEvent("New Ticket", "User Pressed Accepted Option", "Ticket Id:"+ticket.getId());
					

				}
			});

			//Show Ticket
			showTicketDialog.show();
			
			//Google Analytic - Event Notify
			EosApplication.getInstance().trackEvent("New Ticket", "New Ticket Dialog Show", "Ticket Id:"+ticket.getId());
			 
			

		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			// save error log when exception is created
			UtilityFunction.saveErrorLog(mContext, e);
		}
	}

	/**
	 * This method used for decline a ticket on device
	 * 
	 * @param ticket
	 * @param newTicketDialog
	 */

	public void showDeclineTicketDialog(final Ticket ticket,
			final Dialog newTicketDialog) {

		// define a textview component
		TextView btn_ok_decline, btn_cancel_decline;

		// define a dialog
		declineTicketDialog = new Dialog(mContext, R.style.AppTheme);
		// dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// set dialog with no title and full screen
		declineTicketDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		declineTicketDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		declineTicketDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		// set background drawable
		declineTicketDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.R.color.transparent));
		declineTicketDialog.setCancelable(false);

		// Inflate Lyout to set in dialog, set its layoutparameters

		View screen_decline_ticket_popup = LayoutInflater.from(mContext)
				.inflate(R.layout.screen_decline_ticket_popup, null);

		View center_pop_up = screen_decline_ticket_popup
				.findViewById(R.id.center_pop_up);
		LayoutParams center_pop_up_layout_parm = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		int popup_h = 0;

		// ScreenDpi 150 - 200 for mdpi
		// ScreenDpi 200 -300 for mdpi
		if (MyTicketActivity.ScreenDpi > 150
				&& MyTicketActivity.ScreenDpi < 200) {
			popup_h = (int) (MyTicketActivity.ScreenHeight * 0.6);
		} else if (MyTicketActivity.ScreenDpi > 200) {
			popup_h = (int) (MyTicketActivity.ScreenHeight * 0.7);
		}
		int popup_w = (int) (MyTicketActivity.ScreenWidth * 0.8);

		int paop_up_margin_top_bottom = (MyTicketActivity.ScreenHeight - popup_h) / 2;
		int paop_up_margin_lefy_right = (MyTicketActivity.ScreenWidth - popup_w) / 2;

		center_pop_up_layout_parm.leftMargin = paop_up_margin_lefy_right;
		center_pop_up_layout_parm.rightMargin = paop_up_margin_lefy_right;
		center_pop_up_layout_parm.topMargin = paop_up_margin_top_bottom;
		center_pop_up_layout_parm.bottomMargin = paop_up_margin_top_bottom;

		center_pop_up.setLayoutParams(center_pop_up_layout_parm);

		// set layout
		declineTicketDialog.setContentView(screen_decline_ticket_popup);

		// initilize component
		btn_ok_decline = (TextView) declineTicketDialog
				.findViewById(R.id.btn_decline_ok);
		btn_cancel_decline = (TextView) declineTicketDialog
				.findViewById(R.id.btn_decline_cancel);

		// define listview component
		ListView list_reason_type = (ListView) declineTicketDialog
				.findViewById(R.id.list_reason_type);

		// define instace of the class DBInteraction
		DBInteraction dbInteraction = DBInteraction
				.getInstance(ApplicationConstant.currentActivityContext);
		// reason type id for decline
		String typeid = mContext.getResources().getString(
				R.string.ticket_action_reason_type_decline_ticket);

		// get the connection of the class
		dbInteraction.getConnection();

		// define arraylist data
		ArrayList<DeclineReasonModel> declineReasons = dbInteraction
				.getReasonList(typeid);
		// close a connection
		dbInteraction.closeConnection();

		// passing data to DeclineReasonAdapter class
		final DeclineReasonAdapter decline_list_adapter = new DeclineReasonAdapter(
				mContext, R.layout.row_decline_ticket_list, declineReasons,
				null, list_reason_type);

		// set the adapter
		list_reason_type.setAdapter(decline_list_adapter);

		// click button decline reason cancel
		btn_cancel_decline.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				// decline reason dialog dismiss
				declineTicketDialog.dismiss();

			}
		});

		// click button Accept reason
		btn_ok_decline.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (!decline_list_adapter.isOtherReasonOptionValid()) {
					UtilityFunction.showCenteredToast(
							mContext,
							mContext.getResources().getString(
									R.string.fill_reason_others));
					return;
				}

				// get the title for selected reason
				String reason_value = decline_list_adapter
						.getSelectedReasonsTitle();

				// check reason value
				if (reason_value != null) {
					/*// stopNewTicketRinger();
					ticket.SuggestionComment = reason_value;
					// clear new ticket alert
					clearNewTicketAlert();
					// call api for decline ticket
					MyTicketManager.getInstance(mContext).api_DeclineTicket(
							ticket);
					//Google Analytic - Event Capture
					EosApplication.getInstance().trackEvent("New Ticket", "User Pressed Decline Option", "Ticket Id:"+ticket.getId());
*/

					showconfirmDialog(ticket,reason_value, declineTicketDialog);


				} else
					// show center toast
					UtilityFunction.showCenteredToast(
							mContext,
							mContext.getResources().getString(
									R.string.one_select_reason));
			}
		});

		// show decline ticket dialog
		declineTicketDialog.show();
		
		//Google Analytic - Event Capture
		EosApplication.getInstance().trackEvent("New Ticket", "Decline Reason Dialog Box Shown", "Ticket Id:"+ticket.getId());
		
	}

	// this method will dismiss Opened dialog of NewTicket, Stop Ringer Alert,
	// clear preference flags for newTickets,clear notification on status bar.
	public void clearNewTicketAlert() {

		// check showTicketDialog null or not
		if (showTicketDialog != null && showTicketDialog.isShowing())
			// dismiss showTicketDialog
			showTicketDialog.dismiss();

		// check declineTicketDialog null or not
		if (declineTicketDialog != null && declineTicketDialog.isShowing())
			// dismiss declineTicketDialog
			declineTicketDialog.dismiss();

		// stop new ticket ringer
		stopNewTicketRinger();

		// clear preference for new ticket on local db
		clearPrefrenceForNewTicket();
		// define instance isNewTicketPopUpShowing
		MyTicketActivity.isNewTicketPopUpShowing = false;

		// cancel notification on notification bar
		cancelNotification();
		// remove new ticket from local db
		removeNewTicketFromLocalDB();
	}

	

	/**
	 * This function is used to generate a notification on top bar with time and
	 * message content
	 * 
	 * @param context
	 * @param message
	 */

	private static void generateNotification(Context context, String message) {

		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		String title = context.getString(R.string.app_name);

		// Open Activity when notification is clicked
		// set intent so it does not start a new activity
		Intent notificationIntent = new Intent(context, MyTicketActivity.class);
		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
						| PendingIntent.FLAG_ONE_SHOT);

		//Initialize Notification
		Notification notification = new Notification(icon, message, when);
		//notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;


		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, notification);

	}

	private static void generateNotification_New(Context context, String message) {

		String title = context.getString(R.string.app_name);

		// Open Activity when notification is clicked
		// set intent so it does not start a new activity
		Intent notificationIntent = new Intent(context, MyTicketActivity.class);
		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
						| PendingIntent.FLAG_ONE_SHOT);

		//Initialize Notification
		//initialize notification
		Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		String CHANNEL_ID = "my_channel_01789";// The id of the channel.
		CharSequence name = context.getString(R.string.app_name);// The user-visible name of the channel.
		int importance = NotificationManager.IMPORTANCE_HIGH;
		NotificationChannel mChannel = null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
		}
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.volvo_logo)
				.setContentTitle(title)
				.setContentText(message)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setContentIntent(pendingIntent)
				.setSound(uri)
				.setVibrate(new long[] { 0, 1000, 1000, 1000, 1000 })
				.setAutoCancel(true);

		//Notifiy
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); ;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notificationManager.createNotificationChannel(mChannel);
		}
		notificationManager.notify(NOTIFICATION_ID, mBuilder.build());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.teramatrix.vecv.interfaces.INewTicket#setAcceptedTicket(boolean,
	 * java.lang.String)
	 */
	@Override
	public void setAcceptedTicket(boolean isAccepted, String ticketId) {
		// TODO Auto-generated method stub

	}

	/*
	 * Alarm Ringer for new Ticket . When new Ticket arrives, ringer is started
	 * , it keep running for 1 minute and stops,it restart after 1 minute and
	 * repeats like before untli any action(Accept/Decline) is taken on newTicke
	 */
	public void startNewTicketRinger() {

		// define a intent on new ticket ringer
		Intent intent = new Intent(mContext, NewTicketAlertRinger.class);

		// define PendingIntent and receive broadcast receiver
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
				NewTicketAlertRinger.REQUEST_CODE, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// define AlarmManager class instance
		AlarmManager alarmManager = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);

		// get current time
		long firstMillis = System.currentTimeMillis();
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
				ApplicationConstant.NEW_TICKET_RINGER_REPEAT_TIME,
				pendingIntent);
		// define and assign value isNewTicketRingerPlaying "true"
		isNewTicketRingerPlaying = true;

	}

	/*
	 * This function is used for stop new ticket ringer and stop alarm manager
	 * class instance
	 */
	public void stopNewTicketRinger() {

		// define alarm manager class instace
		AlarmManager alarmManager = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);

		// define instance of the class
		Intent intent = new Intent(mContext, NewTicketAlertRinger.class);
		// define pending intent of the class
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
				NewTicketAlertRinger.REQUEST_CODE, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		// cancel alarm
		alarmManager.cancel(pendingIntent);
		// call and stop service
		Intent mServiceIntent = new Intent(mContext, AlertRingerService.class);
		mContext.stopService(mServiceIntent);

		// define value of instance isNewTicketRingerPlaying
		isNewTicketRingerPlaying = false;

	}

	/*
	 * This function is used to show loading dialog
	 */
	private void showLoadingDialog() {
		// define progress dialog of an activity
		progressDialog = new ProgressDialog(mContext);
		// set message
		progressDialog.setMessage(mContext.getResources().getString(
				R.string.getting_new_ticket));
		progressDialog.setCancelable(false);
		// show dialog
		progressDialog.show();
	}

	/*
	 * This function is used to hide loading dialog
	 */
	private void hideLoadingDialog() {
		// check dialog
		if (progressDialog != null && progressDialog.isShowing())
			progressDialog.dismiss();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.teramatrix.vecv.interfaces.INewTicket#api_VanReachedResponse()
	 */
	@Override
	public void api_VanReachedResponse() {
		// TODO Auto-generated method stub

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
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.teramatrix.vecv.interfaces.INewTicket#api_TicketUpdateResponse(java
	 * .lang.String)
	 */
	@Override
	public void api_TicketUpdateResponse(String ticket_id) {
		// TODO Auto-generated method stub

	}

	/*
	 * This function is used to cancel a notification on notification bar
	 */
	public static void cancelNotification() {
		try {
			// define instace of the class
			String ns = mContext.NOTIFICATION_SERVICE;
			// define notification manager of the clas instace
			NotificationManager nMgr = (NotificationManager) mContext
					.getSystemService(ns);
			// cancel notification id
			nMgr.cancel(NOTIFICATION_ID);
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			// save error log when exception is created
			UtilityFunction.saveErrorLog(mContext, e);
		}
	}





	public void showconfirmDialog(final Ticket ticket, final String reason_value, final Dialog declineTicketDialog) {

		try {

			confirmjobDialog = new Dialog(mContext,
					android.R.style.Theme_Translucent);
			// dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

			confirmjobDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			confirmjobDialog.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			confirmjobDialog.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

			confirmjobDialog.setCancelable(false);

			// Inflate pop-up view view,set layout parameter
			View screen_van_reached_popup_seekbar = LayoutInflater.from(
					mContext).inflate(
					R.layout.screen_confirmbox_popup, null);

			View center_pop_up = screen_van_reached_popup_seekbar
					.findViewById(R.id.center_pop_up);
			LayoutParams center_pop_up_layout_parm = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			int popup_h = 0;

			// ScreenDpi 150 - 200 for mdpi
			// ScreenDpi 200 -300 for mdpi
			if (MyTicketActivity.ScreenDpi > 150
					&& MyTicketActivity.ScreenDpi < 200) {
				popup_h = (int) (MyTicketActivity.ScreenHeight * 0.20);
			} else if (MyTicketActivity.ScreenDpi > 200) {
				popup_h = (int) (MyTicketActivity.ScreenHeight * 0.20);
			}
			int popup_w = (int) (MyTicketActivity.ScreenWidth * 0.75);

			int paop_up_margin_top_bottom = (MyTicketActivity.ScreenHeight - popup_h) / 2;
			int paop_up_margin_lefy_right = (MyTicketActivity.ScreenWidth - popup_w) / 2;

			center_pop_up_layout_parm.leftMargin = paop_up_margin_lefy_right;
			center_pop_up_layout_parm.rightMargin = paop_up_margin_lefy_right;
			center_pop_up_layout_parm.topMargin = paop_up_margin_top_bottom;
			center_pop_up_layout_parm.bottomMargin = paop_up_margin_top_bottom;

			center_pop_up.setLayoutParams(center_pop_up_layout_parm);
			confirmjobDialog.setContentView(screen_van_reached_popup_seekbar);

			confirmjobDialog.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

			// initialize textview component
			TextView btn_ok = (TextView) confirmjobDialog
					.findViewById(R.id.tv_ok);
			TextView btn_cancel = (TextView) confirmjobDialog
					.findViewById(R.id.tv_cancel);

			TextView confirmation_messsage_text = (TextView) confirmjobDialog
					.findViewById(R.id.confirmation_messsage_text);

			confirmation_messsage_text.setText("Are you sure, you want to decline ticket?");

			btn_ok.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub

					confirmjobDialog.hide();
					// stopNewTicketRinger();
					ticket.SuggestionComment = reason_value;
					// clear new ticket alert
					clearNewTicketAlert();
					// call api for decline ticket
					MyTicketManager.getInstance(mContext).api_DeclineTicket(
							ticket);
					//Google Analytic - Event Capture
					EosApplication.getInstance().trackEvent("New Ticket", "User Pressed Decline Option", "Ticket Id:"+ticket.getId());

				}
			});

			btn_cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					confirmjobDialog.hide();

					if (declineTicketDialog!=null&& declineTicketDialog.isShowing()){
						declineTicketDialog.dismiss();
					}
				}
			});

			confirmjobDialog.show();

		} catch (Exception e) {
			// TODO: handle exception
			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);
		}

	}



}
