package com.teramatrix.vos;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.teramatrix.vos.adapter.DeclineReasonAdapter;
import com.teramatrix.vos.adapter.EstimationCostAdapter;
import com.teramatrix.vos.checkinternet.CheckInternetConnection;
import com.teramatrix.vos.database.DBInteraction;
import com.teramatrix.vos.firebase.service.MyFirebaseMessagingService;
import com.teramatrix.vos.interfaces.INetworkAvailablity;
import com.teramatrix.vos.model.DeclineReasonModel;
import com.teramatrix.vos.model.EstimationCostModel;
import com.teramatrix.vos.model.Ticket;
import com.teramatrix.vos.model.UpdateCounter;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.reciver.InternetAvailabilityRecever;
import com.teramatrix.vos.service.SyncTicketsService;
import com.teramatrix.vos.ticketmanager.MyTicketManager;
import com.teramatrix.vos.ticketmanager.NewTicketManager;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.TimeFormater;
import com.teramatrix.vos.utils.UtilityFunction;

/**
 * 
 * This class JobStatusDetailActivity shown opening a Ticket in IN-Progress
 * status (Green or Red bar) from Opened tickets list. This activity will shown
 * as problem description,sla time,sla status,edit estimation time , estimation
 * cost.
 * 
 * @author gaurav.mangal
 * 
 */
public class JobStatusDetailActivity extends Activity implements
		INetworkAvailablity {

	// define TextView component of an activity
	TextView tv_problemdescription;
	TextView tv_time_counter;
	TextView btn_job_completed;
	TextView tv_estimation_time;
	TextView heading_tv_problem_description;
	TextView heading_tv_sla_time;
	TextView heading_tv_slastatus;
	TextView heading_tv_estimationtime;
	TextView heading_tv_estimationcost;
	TextView heading_tv_partreplaced;
	TextView tv_estimation_cost;
	View tv_time_heading;
	TextView txt_estimation_distance_value;
	TextView screen_title;
	TextView tv_sla_timer;
	TextView tv_sla_status;
	TextView tv_symbol;
	TextView seek_hoursValue;
	TextView tv_edittime;
	TextView tv_editcost;
	TextView tv_contactnumber;

	// define EditText component of the class
	EditText relativeLayout_edit_estimation_time_reason;
	EditText relativeLayout_edit_estimation_cost_reason;
	EditText et_estimationCost;

	// Defining TypeFace font component that will be used in this activity
	Typeface centuryGothic;
	Typeface verdana;
	Typeface typeFace_Rupee;

	// define RelativeLayout component of an activity
	RelativeLayout rl_tv_estimation_time;
	RelativeLayout rl_tv_estimation_cost;
	LinearLayout rlTitle_bar_contact;

	// define String variable of an activity
	String estimated_mins = "";
	String estimated_sec = "00";
	String select_editTime = "";
	String estimated_cost = "";
	String isjobcompleted;
	String estimation_cost_selected_id = "0";
	String estimation_cost_selected_range = "0";
	String edit_stimation_time_reasons_positions;
	String edit_estimation_cost_positions;
	String reason_value = "";
	String other_reason_value;
	String editEstimatedCostValue = "";

	// define int variable of an activity
	int estimated_hours;
	long current_estimated_time_countdown;
	private int editEstimatedTimeValue;

	// define boolean variable of an activity
	boolean isEstimatedTimeToRepaireCrossed;

	// define ImageView component of an activity
	ImageView img_network;

	// define Dialog component of an activity
	Dialog edit_estimation_time_reason_dialog;
	Dialog confirmjobDialog;
	Dialog edit_estimationCostDialog;

	// define instance of Ticket class of an activity
	Ticket ticket;

	// define EstimationCostAdapter class instance in this activity
	EstimationCostAdapter estimationcost_adapter;

	// define Timer class of an activity
	Timer slaCountDownTimer;

	// define list view
	ListView list_estimationcost;
	List<EstimationCostModel> list_estimation_cost_data;

	UpdateCounter updateCounter = new UpdateCounter();

	private final BroadcastReceiver updateBroadCastReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			final ProgressDialog pd = new ProgressDialog(
					JobStatusDetailActivity.this);
			try {

				pd.setMessage(getResources().getString(
						R.string.updating_tickets));
				pd.setCancelable(false);
				pd.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// listUpdateStatusBar.setVisibility(View.VISIBLE);

			List<Ticket> list_openTicket = MyTicketManager.getInstance(JobStatusDetailActivity.this)
					.getAllOpenedTicketsFromLocalDB();
			if (list_openTicket == null)
				list_openTicket = new ArrayList<Ticket>();

			List<Ticket> list_closed_tickets = MyTicketManager.getInstance(JobStatusDetailActivity.this)
					.getAllClosedTicketsFromLocalDB();
			if (list_closed_tickets == null)
				list_closed_tickets = new ArrayList<Ticket>();


			// Shifting closed ticket for which is_trip_end -> false to Open Ticket
			// List
			// Add all closed ticket for which is_trip_end -> false to Open Ticket
			// List
			for (Ticket ticket : list_closed_tickets) {
				String is_trip_end = ticket.IsTripEnd;
				if (is_trip_end != null && is_trip_end.equalsIgnoreCase("false") && !ticket.getTicketStatus().equalsIgnoreCase("7")) {
					list_openTicket.add(ticket);
				}
			}
			// Remove all closed ticket for which is_trip_end -> false from closed
			// ticket List
			for (int i = 0; i < list_closed_tickets.size(); i++) {
				Ticket ticket = list_closed_tickets.get(i);
				String is_trip_end = ticket.IsTripEnd;
				if (is_trip_end != null && is_trip_end.equalsIgnoreCase("false") && !ticket.getTicketStatus().equalsIgnoreCase("7")) {
					list_closed_tickets.remove(i);
					i--;
				}
			}

			for (Ticket ticket : list_openTicket
					) {
				if (ticket.getId().equalsIgnoreCase(JobStatusDetailActivity.this.ticket.getId())) {
					JobStatusDetailActivity.this.ticket = ticket;


					String ticketstatus = JobStatusDetailActivity.this.ticket.getTicketStatus();
					if (ticketstatus.equalsIgnoreCase("3")) {
						// This case will occur when user have reached on place
						// (Van reached) and Form(Problem description) is
						// submitted.
						// Redirect User to JobStatusDetailActivity
						isjobcompleted = null;
						setdataUI();
					} else if (ticketstatus.equalsIgnoreCase("4")) {
						isjobcompleted = "true";
						setdataUI();
					} else if (ticketstatus.equalsIgnoreCase("5") && ticket.IsTripEnd.equalsIgnoreCase("true")) {
						isjobcompleted = "closed";
						setdataUI();
					} else if (ticketstatus.equalsIgnoreCase("7")) {
						isjobcompleted = "closed";
						setdataUI();
					} else if (ticketstatus.equalsIgnoreCase("8")) {
						isjobcompleted = "true";
						setdataUI();
					} else if (ticketstatus.equalsIgnoreCase("5") && ticket.IsTripEnd.equalsIgnoreCase("false")) {
						isjobcompleted = "true";
						setdataUI();
					} else {
						pd.dismiss();
						finish();
					}

				}
			}
			pd.dismiss();

		}
	};
	// Receive push from server in device
	private final BroadcastReceiver pushBroadCastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			// get type match for receive push
			String type = intent.getStringExtra("type");

			// check and match type pendingTicket or null
			if (type != null && type.equalsIgnoreCase("pendingTicket")) {
				NewTicketManager newTicketManager = NewTicketManager
						.getInstance(JobStatusDetailActivity.this);
				newTicketManager
						.receviedNewTicketPush(ApplicationConstant.IS_APP_IN_FORGROUND);
				/*
				 * new VECVPreferences(MyTicketActivity.this)
				 * .decresePendingTicketsCount();
				 */
			} else {

				if (!MyTicketActivity.isNewTicketPopUpShowing) {
					NewTicketManager newTicketManager = NewTicketManager
							.getInstance(JobStatusDetailActivity.this);
					newTicketManager
							.receviedNewTicketPush(ApplicationConstant.IS_APP_IN_FORGROUND);
				} else {

					new VECVPreferences(JobStatusDetailActivity.this)
							.addPendingTicketsCount();
				}
			}

		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		try {
			super.onCreate(savedInstanceState);

			// set the screen layout with no title and full screen
			requestWindowFeature(Window.FEATURE_NO_TITLE);

/*			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

			//change Status bar color
			Window window = getWindow();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				window.setStatusBarColor(ContextCompat.getColor(this,R.color.volvo_blue));
			}

			// setting a layout
			setContentView(R.layout.screen_job_status_details);

			// getting parameter with the tiket and jobcompleted key
			ticket = (Ticket) getIntent().getSerializableExtra("ticket");
			isjobcompleted = getIntent().getStringExtra("jobcompleted");

			// setup data ui component
			setdataUI();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// defination of set up component
	private void setdataUI() {

		// initialize component of the activity class

		// init fonts
		centuryGothic = Typeface.createFromAsset(getAssets(), "gothic_0.TTF");
		verdana = Typeface.createFromAsset(getAssets(), "Verdana.ttf");
		typeFace_Rupee = Typeface.createFromAsset(getAssets(),
				"Rupee_Foradian.ttf");

		// init views from layout file
		heading_tv_problem_description = (TextView) findViewById(R.id.tv_problem_description);
		heading_tv_sla_time = (TextView) findViewById(R.id.tv_sla_time);
		heading_tv_slastatus = (TextView) findViewById(R.id.tv_slastatus);
		heading_tv_estimationtime = (TextView) findViewById(R.id.tv_estimationtime);
		heading_tv_estimationcost = (TextView) findViewById(R.id.tv_estimationcost);
		heading_tv_partreplaced = (TextView) findViewById(R.id.tv_partreplaced);
		btn_job_completed = (TextView) findViewById(R.id.btn_job_completed);
		tv_time_heading = findViewById(R.id.tv_time_heading);
		txt_estimation_distance_value = (TextView) findViewById(R.id.tv_distance_value);
		rlTitle_bar_contact = (LinearLayout) findViewById(R.id.rlTitle_bar_contact);
		tv_contactnumber = (TextView) findViewById(R.id.tv_contactnumber);

		tv_symbol = (TextView) findViewById(R.id.tv_symbol);
		screen_title = (TextView) findViewById(R.id.tv_header_status);
		tv_problemdescription = (TextView) findViewById(R.id.tv_problemdescription);
		tv_sla_timer = (TextView) findViewById(R.id.tv_sla_timer);
		tv_sla_status = (TextView) findViewById(R.id.tv_sla_status);
		tv_estimation_time = (TextView) findViewById(R.id.tv_estimation_time);
		tv_estimation_cost = (TextView) findViewById(R.id.tv_estimation_cost);
		tv_time_counter = (TextView) findViewById(R.id.tv_time_counter);

		// Set fonts
		btn_job_completed.setTypeface(centuryGothic);
		//tv_time_heading.setTypeface(centuryGothic);
		txt_estimation_distance_value.setTypeface(centuryGothic);
		heading_tv_problem_description.setTypeface(verdana);
		heading_tv_sla_time.setTypeface(verdana);
		heading_tv_slastatus.setTypeface(verdana);
		heading_tv_estimationtime.setTypeface(verdana);
		heading_tv_estimationcost.setTypeface(verdana);
		heading_tv_partreplaced.setTypeface(verdana);
		//screen_title.setTypeface(centuryGothic);
		tv_problemdescription.setTypeface(centuryGothic);
		tv_sla_timer.setTypeface(centuryGothic);
		tv_sla_status.setTypeface(centuryGothic);
		tv_estimation_time.setTypeface(centuryGothic);
		tv_estimation_cost.setTypeface(centuryGothic);
		//tv_time_counter.setTypeface(centuryGothic);

		// Set Estimation Distance
		try {
			float distance_total = NumberFormat
					.getNumberInstance(java.util.Locale.US)
					.parse(ticket.EstimatedDistance).floatValue() * 2;
			txt_estimation_distance_value.setText(ticket.EstimatedDistance
					+ "+" + ticket.EstimatedDistance + "=" + distance_total);
		} catch (Exception e1) {
			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e1);
			// save error log when error is created
			UtilityFunction.saveErrorLog(JobStatusDetailActivity.this, e1);
		}

		// Set contact number text
		tv_contactnumber.setText("Contacts");
		// Set Contact Selector
		rlTitle_bar_contact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openContactSelector();
			}
		});

		// Set button status to Job Comleted | End Trip | closed
		View rlbtn = findViewById(R.id.rlbtn);
		if (isjobcompleted != null && isjobcompleted.equalsIgnoreCase("true")
				&& ticket.TicketStatus.equalsIgnoreCase("8")) {
			// Job Completed + Trip End + Not Closed yet
			// Do not Show 'Trip End' Button
			rlbtn.setVisibility(View.GONE);
			// btn_job_completed.setText("End Trip");
			screen_title.setText(getResources().getString(R.string.resolved));
			tv_time_heading.setVisibility(View.VISIBLE);
			// rlTitle_bar_contact.setClickable(false);

		} else if (isjobcompleted != null
				&& isjobcompleted.equalsIgnoreCase("true")
				&& ticket.TicketStatus.equalsIgnoreCase("5")) {
			// Job Completed + Ticket closed + but Trip not End
			// Show 'Trip End' Button
			rlbtn.setVisibility(View.VISIBLE);
			btn_job_completed.setText("End Trip");
			screen_title.setText(getResources().getString(R.string.closed));
			tv_time_heading.setVisibility(View.VISIBLE);
			// rlTitle_bar_contact.setClickable(false);

		} else if (isjobcompleted != null
				&& isjobcompleted.equalsIgnoreCase("true")) {
			// Job Completed but Trip not End
			// Show 'Trip End' Button
			rlbtn.setVisibility(View.VISIBLE);
			btn_job_completed.setText("End Trip");
			screen_title.setText(getResources().getString(R.string.resolved));
			tv_time_heading.setVisibility(View.VISIBLE);
			// rlTitle_bar_contact.setClickable(false);

		} else if (isjobcompleted != null
				&& isjobcompleted.equalsIgnoreCase("closed")) {
			// Job Comleted + Trip End + Ticket Closed
			// Do not Show any Button
			rlbtn.setVisibility(View.GONE);
			screen_title.setText(getResources().getString(R.string.closed));
			rlTitle_bar_contact.setVisibility(View.VISIBLE);
			tv_time_heading.setVisibility(View.VISIBLE);
		} else {
			// Default is 'Work in Progress' screen and show 'Job Completed'
			// button
		}

		// Setting Ticket Description
		if (ticket.TicketStatus != null
				&& !ticket.TicketStatus.equalsIgnoreCase("null")) {
			if (ticket.TicketStatus.equalsIgnoreCase("3")) {
				if (ticket.SuggestionComment != null
						&& !ticket.SuggestionComment.equalsIgnoreCase("null")) {

					tv_problemdescription.setText(""
							+ ticket.Description.trim());
				} else {
					tv_problemdescription.setText(getResources().getString(
							R.string.not_available));
				}

			} else {
				if (ticket.Description != null
						&& !ticket.Description.equalsIgnoreCase("null"))
					tv_problemdescription.setText(""
							+ ticket.Description.trim());
				else
					tv_problemdescription.setText(getResources().getString(
							R.string.not_available));
			}
		}

		if (ticket.Priority.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC)) {
			heading_tv_problem_description.setText("DTC Information");
		}

		// settting SLA time
		int sla_time = 0;

		if (ticket.TotalTicketLifeCycleTimeSlab != null
				&& !ticket.TotalTicketLifeCycleTimeSlab
						.equalsIgnoreCase("null")) {
			sla_time = Integer.parseInt(ticket.TotalTicketLifeCycleTimeSlab);
			tv_sla_timer.setText(""
					+ TimeFormater.convertMinutesToHMmSs(sla_time));
		} else
			tv_sla_timer.setText(getResources().getString(
					R.string.not_available));

		// Setting sla status
		if (ticket.SlaTimeAchevied != null
				&& !ticket.SlaTimeAchevied.equalsIgnoreCase("null")) {
			int sla_achevied_time = Integer.parseInt(ticket.SlaTimeAchevied);
			if (sla_achevied_time <= sla_time)
				tv_sla_status.setText(getResources().getString(R.string.met));
			else
				tv_sla_status.setText(getResources()
						.getString(R.string.not_met));
		} else
			tv_sla_status.setText(getResources().getString(
					R.string.not_available));

		// Setting estiamtion time
		// Setting estiamtion cost
		if (ticket.EstimatedTimeForJobComplition != null
				&& !ticket.EstimatedTimeForJobComplition
						.equalsIgnoreCase("null")) {

			if (ticket.EstimatedTimeForJobComplition != null) {
				int estimated_minutes = Integer
						.parseInt(ticket.EstimatedTimeForJobComplition);
				int estimation_in_hour = estimated_minutes / 60;
				int estimation_in_minutes = estimated_minutes % 60;
				editEstimatedTimeValue = estimation_in_hour * 60
						+ estimation_in_minutes;
				estimated_hours = editEstimatedTimeValue;
				estimated_mins = String.valueOf(estimation_in_minutes);

				String formatedEstimatedTime = TimeFormater
						.getFormatedEstimatedTime(editEstimatedTimeValue);
				tv_estimation_time.setText(formatedEstimatedTime);

				editEstimatedCostValue = ticket.EstimatedCostForJobComplition;
				estimated_cost = editEstimatedCostValue;

			}

		} else
			tv_estimation_time.setText(getResources().getString(
					R.string.not_available));

		tv_symbol.setTypeface(typeFace_Rupee);
		tv_symbol.setText("`");
		if (ticket.EstimatedCostForJobComplition != null
				&& !ticket.EstimatedCostForJobComplition
						.equalsIgnoreCase("null"))
			tv_estimation_cost.setText(ticket.EstimatedCostForJobComplition);
		else
			tv_estimation_cost.setText(getResources().getString(
					R.string.not_available));

		rl_tv_estimation_time = (RelativeLayout) findViewById(R.id.rl_tv_estimation_time);
		rl_tv_estimation_cost = (RelativeLayout) findViewById(R.id.rl_tv_estimation_cost);

		tv_edittime = (TextView) findViewById(R.id.tv_edittime);
		tv_editcost = (TextView) findViewById(R.id.tv_editcost);

		tv_estimation_time = (TextView) findViewById(R.id.tv_estimation_time);

		rl_tv_estimation_time.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {

					estimated_hours = editEstimatedTimeValue;

					if (isjobcompleted != null
							&& (isjobcompleted.equalsIgnoreCase("true") || isjobcompleted
									.equalsIgnoreCase("closed"))) {
					} else
						editEstimatedTime();

				} catch (Exception e) {
					// Google Analytic -Tracking Exception
					EosApplication.getInstance().trackException(e);

					// save error log when error is created
					UtilityFunction.saveErrorLog(JobStatusDetailActivity.this,
							e);
				}

			}
		});

		rl_tv_estimation_cost.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {

					estimated_cost = editEstimatedCostValue;

					if (isjobcompleted != null
							&& (isjobcompleted.equalsIgnoreCase("true") || isjobcompleted
									.equalsIgnoreCase("closed"))) {
					} else {
						editEstimatedCost();
					}

				} catch (Exception e) {
					// Google Analytic -Tracking Exception
					EosApplication.getInstance().trackException(e);

					// save error log when error is created
					UtilityFunction.saveErrorLog(JobStatusDetailActivity.this,
							e);
				}

			}
		});

		if (isjobcompleted != null
				&& (isjobcompleted.equalsIgnoreCase("true") || isjobcompleted
						.equalsIgnoreCase("closed"))) {

			tv_edittime.setVisibility(View.GONE);
			tv_editcost.setVisibility(View.GONE);

			if (ticket.AcheviedEstimatedTimeForJobComplition != null
					&& !ticket.AcheviedEstimatedTimeForJobComplition
							.equalsIgnoreCase("null")) {

				tv_time_counter
						.setText(ticket.AcheviedEstimatedTimeForJobComplition);
			} else {
				tv_time_counter.setText(getResources().getString(
						R.string.not_available));
			}
		}

		img_network = (ImageView) findViewById(R.id.img_network);

		/*
		 * If user click button ,Ticket status changes from In-Progress to
		 * Pre-closer i.e Service engineer has fixed the problem .
		 */
		btn_job_completed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				try {
					showconfirmDialog();
				} catch (Exception e) {

					// Google Analytic -Tracking Exception
					EosApplication.getInstance().trackException(e);

					// TODO: handle exception
					// save error log when error is created
					UtilityFunction.saveErrorLog(JobStatusDetailActivity.this,
							e);
				}

			}
		});

	}

	public void showconfirmDialog() {

		try {

			confirmjobDialog = new Dialog(JobStatusDetailActivity.this,
					android.R.style.Theme_Translucent);
			// dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

			confirmjobDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			confirmjobDialog.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

			confirmjobDialog.setCancelable(false);

			// Inflate pop-up view view,set layout parameter
			View screen_van_reached_popup_seekbar = LayoutInflater.from(
					JobStatusDetailActivity.this).inflate(
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

			// if current Ticket Status -> 3 , call is for 'Job Complete' so set
			// message accordingly
			// if current Ticket Status -> 4 , call is for 'Trip End' so set
			// message accordingly
			if (ticket.TicketStatus != null
					&& ticket.TicketStatus.equalsIgnoreCase("3")) {
				// Call for 'Job Complete'
				confirmation_messsage_text
						.setText("Are you sure, you want to complete this job?");
			} else if (ticket.TicketStatus != null
					&& (ticket.TicketStatus.equalsIgnoreCase("4") || ticket.TicketStatus.equalsIgnoreCase("5"))) {
				// Call for 'Trip End'
				confirmation_messsage_text
						.setText("Are you sure, you want to end this trip?");
			}

			btn_ok.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub

					confirmjobDialog.hide();

					// if current Ticket Status -> 3 , call is for 'Job
					// Complete'
					// if current Ticket Status -> 4 , call is for 'Trip End'

					if (ticket.TicketStatus != null
							&& ticket.TicketStatus.equalsIgnoreCase("3")) {
						// Call for 'Job Complete'

						String currentTime = UtilityFunction
								.currentUTCTimeWithoutDash();
						ticket.LastModifiedTime = currentTime;
						// precloser status
						ticket.TicketStatus = "4";
						ticket.LastModifiedBy = new VECVPreferences(
								JobStatusDetailActivity.this).getDevice_alias();
						ticket.editEstimationTimeReasonsValue = reason_value;
						ticket.AcheviedEstimatedTimeForJobComplition = getAcheviedjobCompletionEstimationDuration();

						MyTicketManager myTicketManager = MyTicketManager
								.getInstance(JobStatusDetailActivity.this);
						myTicketManager.api_JobCompleted(ticket);

						// Googel Analytic - Event
						EosApplication.getInstance().trackEvent(
								"Work In Progress",
								"Work In Progress",
								"Job Completed Button pressed for Ticket Id "
										+ ticket.getId());

					} else if (ticket.TicketStatus != null
							&& (ticket.TicketStatus.equalsIgnoreCase("4") || ticket.TicketStatus.equalsIgnoreCase("5"))) {
						// Call for 'Trip End'

						String currentTime = UtilityFunction
								.currentUTCTimeWithoutDash();
						ticket.LastModifiedTime = currentTime;
						// precloser status
						ticket.Ticket_Previous_Status = ticket.TicketStatus; 
						ticket.TicketStatus = "8";
						ticket.LastModifiedBy = new VECVPreferences(
								JobStatusDetailActivity.this).getDevice_alias();
						ticket.editEstimationTimeReasonsValue = reason_value;
						// ticket.AcheviedEstimatedTimeForJobComplition =
						// getAcheviedjobCompletionEstimationDuration();

						MyTicketManager myTicketManager = MyTicketManager
								.getInstance(JobStatusDetailActivity.this);
						myTicketManager.api_JobCompleted(ticket);

						// Googel Analytic - Event
						EosApplication.getInstance().trackEvent(
								"Trip End",
								"Trip End",
								"Trip End Button pressed for Ticket Id "
										+ ticket.getId());
					}

				}
			});

			btn_cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					confirmjobDialog.hide();
				}
			});

			confirmjobDialog.show();

		} catch (Exception e) {
			// TODO: handle exception
			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);

			// save error log when error is created
			UtilityFunction.saveErrorLog(JobStatusDetailActivity.this, e);

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		// Google Anayltic
		GoogleAnalytics.getInstance(JobStatusDetailActivity.this)
				.reportActivityStart(this);

		if (isjobcompleted != null
				&& (isjobcompleted.equalsIgnoreCase("true") || isjobcompleted
						.equalsIgnoreCase("closed"))) {
			// DO not start count down timer on pre closer screen
		} else {

			startSLACounDownTimer();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	InternetAvailabilityRecever internetAvailabilityRecever=new InternetAvailabilityRecever();
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		registerPushRecevier();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(internetAvailabilityRecever, filter);
		registerReceiver(updateBroadCastReciever, new IntentFilter(
				SyncTicketsService.UPDATEBROADCAST));
		// Google Analytic
		EosApplication.getInstance().trackScreenView("Job Status Screen");

		ApplicationConstant.IS_APP_IN_FORGROUND = true;
		ApplicationConstant.currentActivityContext = this;

		isNetworkAvailable(new CheckInternetConnection(this)
				.isConnectedToInternet());

		if (!MyTicketActivity.isNewTicketPopUpShowing)
			NewTicketManager.getInstance(this).checkForNewTicket();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(pushBroadCastReceiver);
		unregisterReceiver(internetAvailabilityRecever);
		unregisterReceiver(updateBroadCastReciever);

		ApplicationConstant.IS_APP_IN_FORGROUND = false;
		ApplicationConstant.currentActivityContext = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (isjobcompleted != null && isjobcompleted.equalsIgnoreCase("true")) {
			// Do nothing if this is pre closer screen
		} else {
			stopSLACounDownTimer();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.teramatrix.vecv.interfaces.INetworkAvailablity#isNetworkAvailable
	 * (boolean)
	 */
	@Override
	public void isNetworkAvailable(boolean isNetworkAvailable) {
		if (isNetworkAvailable) {
			img_network.setImageResource(R.drawable.wifi_enalbed);
		} else {
			img_network.setImageResource(R.drawable.wifi_disalbed);
		}
	}

	private void registerPushRecevier() {
		registerReceiver(pushBroadCastReceiver, new IntentFilter(
				MyFirebaseMessagingService.PUSH_RECEIVE_MESSAGE_ACTION));
	}

	/**
	 * For Editing EstiamtedTime ,user have to click on this view , a pop-up box
	 * will open , two input options are there one for timepicker sliding bar
	 * and second is Edit Time reason list. Edit reason is mandatory to select.
	 */
	public void editEstimatedTime() {

		/**
		 * filling New Estimated Time, Reason to edit Time
		 */

		MyTicketManager manager = MyTicketManager
				.getInstance(JobStatusDetailActivity.this);
		UpdateCounter counter = manager.getUpdateCounters(ticket.Id);
		manager = null;
		if (counter != null) {
			String s = counter.estiamtedTime_Counter;
			if (s != null && !s.equalsIgnoreCase("null")) {
				int c = Integer.parseInt(s);
				ticket.counter_estiamted_time_update = c;
				if (c >= 3) {
					Toast.makeText(JobStatusDetailActivity.this,
							"Estimated Time Value is updated 3 times. ",
							Toast.LENGTH_SHORT).show();
					return;
				}
			}
		}
		// initialize string variable
		edit_stimation_time_reasons_positions = "";
		reason_value = "";

		// define textview component
		TextView btn_ok_editTime, btn_cancel_editTime;
		final Dialog editEstimatedTimeDialog = new Dialog(
				JobStatusDetailActivity.this, R.style.AppTheme);
		// dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		editEstimatedTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		editEstimatedTimeDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		editEstimatedTimeDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		editEstimatedTimeDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.R.color.transparent));

		editEstimatedTimeDialog.setCancelable(false);

		// Inflate pop-up view view,set layout parameter
		View screen_edit_estimation_time_seek_time = LayoutInflater.from(
				JobStatusDetailActivity.this).inflate(
				R.layout.screen_edit_estimation_time_seek_time, null);

		View center_pop_up = screen_edit_estimation_time_seek_time
				.findViewById(R.id.center_pop_up);
		LayoutParams center_pop_up_layout_parm = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		int popup_h = 0;

		// ScreenDpi 150 - 200 for mdpi
		// ScreenDpi 200 -300 for hdpi
		if (MyTicketActivity.ScreenDpi > 150
				&& MyTicketActivity.ScreenDpi < 200) {
			popup_h = (int) (MyTicketActivity.ScreenHeight * 0.35);
		} else if (MyTicketActivity.ScreenDpi > 200
				&& MyTicketActivity.ScreenDpi < 300) {
			popup_h = (int) (MyTicketActivity.ScreenHeight * 0.4);
		} else if (MyTicketActivity.ScreenDpi > 300) {
			popup_h = (int) (MyTicketActivity.ScreenHeight * 0.43);
		}

		int popup_w = (int) (MyTicketActivity.ScreenWidth * 0.8);

		int paop_up_margin_top_bottom = (MyTicketActivity.ScreenHeight - popup_h) / 2;
		int paop_up_margin_lefy_right = (MyTicketActivity.ScreenWidth - popup_w) / 2;

		center_pop_up_layout_parm.leftMargin = paop_up_margin_lefy_right/2;
		center_pop_up_layout_parm.rightMargin = paop_up_margin_lefy_right/2;
		center_pop_up_layout_parm.topMargin = paop_up_margin_top_bottom-10;
		center_pop_up_layout_parm.bottomMargin = paop_up_margin_top_bottom-10;

		center_pop_up.setLayoutParams(center_pop_up_layout_parm);

		editEstimatedTimeDialog
				.setContentView(screen_edit_estimation_time_seek_time);

		relativeLayout_edit_estimation_time_reason = (EditText) editEstimatedTimeDialog
				.findViewById(R.id.ed_estimated_time_reason);

		relativeLayout_edit_estimation_time_reason
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub

						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							edit_estimation_time_reason();
							return true;
						}

						return false;
					}
				});
		btn_ok_editTime = (TextView) editEstimatedTimeDialog
				.findViewById(R.id.btnok);
		btn_cancel_editTime = (TextView) editEstimatedTimeDialog
				.findViewById(R.id.btncancel);

		// Hour Counter View
		View addHourView = editEstimatedTimeDialog
				.findViewById(R.id.txt_timer1_add);
		View minusHourView = editEstimatedTimeDialog
				.findViewById(R.id.txt_timer1_minus);
		final TextView hourValueView = (TextView) editEstimatedTimeDialog
				.findViewById(R.id.txt_timer1_time);

		SeekBar seekBar = (SeekBar) editEstimatedTimeDialog
				.findViewById(R.id.seek_bar_timer);

		seekBar.setMax(24 * 2);
		seek_hoursValue = (TextView) editEstimatedTimeDialog
				.findViewById(R.id.txt_estimation_time_value);
		seek_hoursValue.setText(TimeFormater
				.getFormatedEstimatedTime(estimated_hours));

		if (estimated_hours > 0) {
			// int estimated_hours_val = Integer.parseInt(estimated_hours);
			seekBar.setProgress(estimated_hours / 30);
		}
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub

				int hours = progress / 2; // it will return hours.
				int minutes = (progress % 2) * 30; // here will be minutes.

				String formatedEstimatedTime = TimeFormater
						.getFormatedEstimatedTime(hours * 60 + minutes);
				seek_hoursValue.setText(formatedEstimatedTime);

				editEstimatedTimeValue = hours * 60 + minutes;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
		});

		btn_cancel_editTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				editEstimatedTimeValue = estimated_hours;
				editEstimatedTimeDialog.dismiss();

			}
		});
		btn_ok_editTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*
				 * if (false && seek_hoursValue.getText().toString()
				 * .equalsIgnoreCase("00:00 hr")) { UtilityFunction
				 * .showCenteredToast( JobStatusDetailActivity.this,
				 * getResources().getString( R.string.select_estimation_time));
				 * return; }
				 */

				if (reason_value != null && !reason_value.equalsIgnoreCase("")) {
					// TODO Auto-generated method stub
					editEstimatedTimeDialog.dismiss();

					if (editEstimatedTimeValue == 0) {
						editEstimatedTimeValue = 60;
					}
					String formatedEstimatedTime = TimeFormater
							.getFormatedEstimatedTime(editEstimatedTimeValue);

					tv_estimation_time.setText(formatedEstimatedTime);

					long previous_estimated_time = 0;
					previous_estimated_time = estimated_hours * 60 * 1000;

					int new_estimated_time = editEstimatedTimeValue;

					editEstimatedTimeForJobComplition(previous_estimated_time,
							new_estimated_time);

					// update Ticket for update in JobComplete Estimated Time
					ticket.EstimatedTimeForJobComplition = new_estimated_time
							+ "";
					ticket.LastModifiedBy = new VECVPreferences(
							JobStatusDetailActivity.this).getDevice_alias();
					ticket.LastModifiedTime = UtilityFunction
							.currentUTCTimeWithoutDash();
					ticket.SuggestionComment = reason_value;

					// call api to update value of JobCompletionEstimated Time
					MyTicketManager myTicketManager = MyTicketManager
							.getInstance(JobStatusDetailActivity.this);

					ticket.counter_estiamted_time_update = ticket.counter_estiamted_time_update + 1;
					myTicketManager.api_UpdateJobCompleteEstimatedTime(ticket);
					myTicketManager.updateCounter_EditEstiamtedTime(ticket);

				} else {
					UtilityFunction.showCenteredToast(
							JobStatusDetailActivity.this, getResources()
									.getString(R.string.one_select_reason));
				}

			}
		});

		editEstimatedTimeDialog.show();
	}

	// Opens Dialog for Editing Estiamted Cost of Repairing
	public void editEstimatedCost() {

		MyTicketManager manager = MyTicketManager
				.getInstance(JobStatusDetailActivity.this);
		UpdateCounter counter = manager.getUpdateCounters(ticket.Id);
		manager = null;
		if (counter != null) {
			String s = counter.estiamtedCost_Counter;
			if (s != null && !s.equalsIgnoreCase("null")) {
				int c = Integer.parseInt(s);
				ticket.counter_estiamted_cost_update = c;
				if (c >= 3) {
					Toast.makeText(JobStatusDetailActivity.this,
							"Estimated Cost Value is updated 3 times. ",
							Toast.LENGTH_SHORT).show();
					return;
				}
			}
		}
		// int counter = ticket.counter_estiamted_cost_update;

		// initialize string variable
		edit_estimation_cost_positions = "";
		reason_value = "";

		// define textview component
		TextView btn_ok_editCost, btn_cancel_editCost;
		final Dialog editEstimatedCostDialog = new Dialog(
				JobStatusDetailActivity.this, R.style.AppTheme);
		// dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		editEstimatedCostDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		editEstimatedCostDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		editEstimatedCostDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		editEstimatedCostDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.R.color.transparent));

		editEstimatedCostDialog.setCancelable(false);

		// Inflate pop-up view view,set layout parameter
		View screen_edit_estimation_cost = LayoutInflater.from(
				JobStatusDetailActivity.this).inflate(
				R.layout.screen_edit_estimation_cost, null);

		View center_pop_up = screen_edit_estimation_cost
				.findViewById(R.id.center_pop_up);
		LayoutParams center_pop_up_layout_parm = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		int popup_h = 0;

		// ScreenDpi 150 - 200 for mdpi
		// ScreenDpi 200 -300 for hdpi
		if (MyTicketActivity.ScreenDpi > 150
				&& MyTicketActivity.ScreenDpi < 200) {
			popup_h = (int) (MyTicketActivity.ScreenHeight * 0.35);
		} else if (MyTicketActivity.ScreenDpi > 200
				&& MyTicketActivity.ScreenDpi < 300) {
			popup_h = (int) (MyTicketActivity.ScreenHeight * 0.4);
		} else if (MyTicketActivity.ScreenDpi > 300) {
			popup_h = (int) (MyTicketActivity.ScreenHeight * 0.43);
		}

		int popup_w = (int) (MyTicketActivity.ScreenWidth * 0.8);

		int paop_up_margin_top_bottom = (MyTicketActivity.ScreenHeight - popup_h) / 2;
		int paop_up_margin_lefy_right = (MyTicketActivity.ScreenWidth - popup_w) / 2;

		center_pop_up_layout_parm.leftMargin = paop_up_margin_lefy_right/2;
		center_pop_up_layout_parm.rightMargin = paop_up_margin_lefy_right/2;
		center_pop_up_layout_parm.topMargin = paop_up_margin_top_bottom-10;
		center_pop_up_layout_parm.bottomMargin = paop_up_margin_top_bottom-10;

		center_pop_up.setLayoutParams(center_pop_up_layout_parm);

		editEstimatedCostDialog.setContentView(screen_edit_estimation_cost);

		// initialize textview component
		et_estimationCost = (EditText) editEstimatedCostDialog
				.findViewById(R.id.ed_estimation_cost);

		et_estimationCost.setText(ticket.EstimatedCostForJobComplition);

		// edit estimation touch listener
		et_estimationCost.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				// get action down value
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					// open cost selector listview
					// openCostSelector();

					DBInteraction dbInteraction = DBInteraction
							.getInstance(JobStatusDetailActivity.this);
					dbInteraction.getConnection();
					ArrayList<EstimationCostModel> estimationCostList = dbInteraction
							.getEstiamtionCostList();
					dbInteraction.closeConnection();

					// If Cost values are saved in DB open Cost selector Dialog
					// Box ,else get cost values from server, save into DB and
					// show .
					if (estimationCostList != null
							&& estimationCostList.size() > 0) {
						openCostSelector();
					} else {

						new AsyncTask<Void, Void, Void>() {

							ProgressDialog pd;

							@Override
							protected void onPreExecute() {
								// TODO Auto-generated method stub
								super.onPreExecute();
								pd = new ProgressDialog(
										JobStatusDetailActivity.this);
								pd.setMessage("Updating Cost from server...");
								pd.setCancelable(false);
								pd.show();
							}

							@Override
							protected Void doInBackground(Void... params) {
								// TODO Auto-generated method stub

								// Get Estiamtion cost list from server
								MyTicketManager.getInstance(
										JobStatusDetailActivity.this)
										.downloadEstimatedCostListFromServer();
								return null;
							}

							@Override
							protected void onPostExecute(Void result) {
								// TODO Auto-generated method stub
								super.onPostExecute(result);
								if (pd.isShowing())
									pd.dismiss();
								openCostSelector();

							}
						}.execute();
					}
					// return boolean value
					return true;
				}
				// return boolean value
				return false;
			}
		});

		relativeLayout_edit_estimation_cost_reason = (EditText) editEstimatedCostDialog
				.findViewById(R.id.ed_estimated_time_reason);

		relativeLayout_edit_estimation_cost_reason
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub

						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							edit_estimation_cost_reason();
							return true;
						}

						return false;
					}
				});
		/**
		 * button ok and cancel for edit cost
		 */
		btn_ok_editCost = (TextView) editEstimatedCostDialog
				.findViewById(R.id.btnok);
		btn_cancel_editCost = (TextView) editEstimatedCostDialog
				.findViewById(R.id.btncancel);

		btn_cancel_editCost.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				editEstimatedCostValue = estimated_cost;
				editEstimatedCostDialog.dismiss();

			}
		});
		btn_ok_editCost.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (et_estimationCost.getText().toString().equalsIgnoreCase("")) {

					UtilityFunction
							.showCenteredToast(
									JobStatusDetailActivity.this,
									getResources().getString(
											R.string.select_estimation_cost));
					return;
				}

				else if (reason_value != null
						&& !reason_value.equalsIgnoreCase("")) {
					// TODO Auto-generated method stub
					tv_estimation_cost.setText(et_estimationCost.getText()
							.toString());
					editEstimatedCostDialog.dismiss();

					ticket.LastModifiedTime = UtilityFunction
							.currentUTCTimeWithoutDash();

					ticket.EstimatedCostForJobComplition = et_estimationCost
							.getText().toString();
					ticket.SuggestionComment = reason_value;

					// call api to update value of JobCompletionEstimated Time
					MyTicketManager myTicketManager = MyTicketManager
							.getInstance(JobStatusDetailActivity.this);

					myTicketManager.api_UpdateJobCompleteEstimatedCost(ticket);

					ticket.counter_estiamted_cost_update = ticket.counter_estiamted_cost_update + 1;
					myTicketManager.updateCounter_EditEstiamtedCost(ticket);

				} else {
					UtilityFunction.showCenteredToast(
							JobStatusDetailActivity.this, getResources()
									.getString(R.string.one_select_reason));
				}

			}
		});

		editEstimatedCostDialog.show();
	}

	// open cost estimation selector
	private void openCostSelector() {

		// add estimation cost in the list

		addEstimationCostDataList();

		edit_estimationCostDialog = new Dialog(JobStatusDetailActivity.this,
				R.style.AppTheme);
		// dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		TextView btn_ok, btn_cancel;

		// initialize component
		edit_estimationCostDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		edit_estimationCostDialog.setCancelable(false);
		edit_estimationCostDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		edit_estimationCostDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		edit_estimationCostDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.R.color.transparent));

		edit_estimationCostDialog.setCancelable(false);

		// inflate pop-up view ,set layout params
		// Inflate pop-up view view,set layout parameter
		View screen_estimation_cost_selector_popup = LayoutInflater.from(
				JobStatusDetailActivity.this).inflate(
				R.layout.screen_estimation_cost_selector_popup, null);

		View center_pop_up = screen_estimation_cost_selector_popup
				.findViewById(R.id.center_pop_up);
		LayoutParams center_pop_up_layout_parm = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		int popup_h = 0;

		// ScreenDpi 150 - 200 for mdpi
		// ScreenDpi 200 -300 for mdpi
		if (MyTicketActivity.ScreenDpi > 150
				&& MyTicketActivity.ScreenDpi < 200) {
			popup_h = (int) (MyTicketActivity.ScreenHeight * 0.55);
		} else if (MyTicketActivity.ScreenDpi > 200) {
			popup_h = (int) (MyTicketActivity.ScreenHeight * 0.6);
		}
		int popup_w = (int) (MyTicketActivity.ScreenWidth * 0.8);

		int paop_up_margin_top_bottom = (MyTicketActivity.ScreenHeight - popup_h) / 2;
		int paop_up_margin_lefy_right = (MyTicketActivity.ScreenWidth - popup_w) / 2;

		center_pop_up_layout_parm.leftMargin = paop_up_margin_lefy_right;
		center_pop_up_layout_parm.rightMargin = paop_up_margin_lefy_right;
		center_pop_up_layout_parm.topMargin = paop_up_margin_top_bottom;
		center_pop_up_layout_parm.bottomMargin = paop_up_margin_top_bottom;

		center_pop_up.setLayoutParams(center_pop_up_layout_parm);

		edit_estimationCostDialog
				.setContentView(screen_estimation_cost_selector_popup);

		// set views values
		btn_ok = (TextView) edit_estimationCostDialog
				.findViewById(R.id.estimationok);

		// click ok button estimation dialog

		btn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (estimationcost_adapter != null) {
					if (estimationcost_adapter.getSelectedPosition() > -1) {

						int selected_position = estimationcost_adapter
								.getSelectedPosition();

						String cost_select_value = null;
						String cost_selected_id = null;
						if (selected_position == list_estimation_cost_data
								.size()) {
							// Other option selected
							String cost_select_value_local = estimationcost_adapter
									.getOthersValue();
							if (cost_select_value_local == null
									|| (cost_select_value_local != null && cost_select_value_local
											.equalsIgnoreCase(""))) {
								UtilityFunction.showCenteredToast(
										JobStatusDetailActivity.this,
										getResources().getString(
												R.string.select_one_options));
								return;
							}
							cost_select_value = estimationcost_adapter
									.getOthersValue();
							cost_selected_id = "Others";
						} else {
							// Given Range Selected
							cost_select_value = list_estimation_cost_data.get(
									estimationcost_adapter
											.getSelectedPosition())
									.getCost_range();

							cost_selected_id = list_estimation_cost_data.get(
									estimationcost_adapter
											.getSelectedPosition()).getId();
						}
						// temp
						estimation_cost_selected_id = cost_selected_id;
						estimation_cost_selected_range = cost_select_value;

						et_estimationCost.setText(cost_select_value);
						edit_estimationCostDialog.dismiss();
					} else {
						UtilityFunction
								.showCenteredToast(
										JobStatusDetailActivity.this,
										getResources().getString(
												R.string.select_one_options));
					}
				}

			}
		});

		btn_cancel = (TextView) edit_estimationCostDialog
				.findViewById(R.id.btncancel);
		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				edit_estimationCostDialog.dismiss();
			}
		});

		list_estimationcost = (ListView) edit_estimationCostDialog
				.findViewById(R.id.list_estimation_cost);

		for (int i = 0; i < list_estimation_cost_data.size(); i++) {
			String cost_range = list_estimation_cost_data.get(i).cost_range;
			if (cost_range.equalsIgnoreCase(et_estimationCost.getText()
					.toString().trim())) {
				i = i + 1;
				estimation_cost_selected_id = String.valueOf(i);
			}
		}

		estimationcost_adapter = new EstimationCostAdapter(
				JobStatusDetailActivity.this, R.layout.row_estimation_cost,
				list_estimation_cost_data, estimation_cost_selected_id,
				list_estimationcost);
		list_estimationcost.setAdapter(estimationcost_adapter);
		edit_estimationCostDialog.show();
	}

	// add estimation cost in the array list
	private void addEstimationCostDataList() {

		DBInteraction dbInteraction = DBInteraction
				.getInstance(JobStatusDetailActivity.this);
		dbInteraction.getConnection();
		ArrayList<EstimationCostModel> estimationCostList = dbInteraction
				.getEstiamtionCostList();
		dbInteraction.closeConnection();
		System.out.println("Size:" + estimationCostList.size());

		list_estimation_cost_data = new ArrayList<EstimationCostModel>();
		for (int i = 0; i < estimationCostList.size(); i++) {

			list_estimation_cost_data.add(estimationCostList.get(i));
		}

		System.out.println("MapViewTimerActivity.addEstimationCostDataList()");

	}

	/**
	 * This method edit estimation time for reasons
	 */
	private void edit_estimation_time_reason() {

		TextView btn_ok_decline, btn_cancel_decline;
		edit_estimation_time_reason_dialog = new Dialog(
				JobStatusDetailActivity.this, R.style.AppTheme);
		// dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		edit_estimation_time_reason_dialog
				.requestWindowFeature(Window.FEATURE_NO_TITLE);
		edit_estimation_time_reason_dialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		edit_estimation_time_reason_dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		edit_estimation_time_reason_dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.R.color.transparent));

		edit_estimation_time_reason_dialog.setCancelable(false);
		edit_estimation_time_reason_dialog
				.setContentView(R.layout.screen_decline_ticket_popup);
		btn_ok_decline = (TextView) edit_estimation_time_reason_dialog
				.findViewById(R.id.btn_decline_ok);
		btn_cancel_decline = (TextView) edit_estimation_time_reason_dialog
				.findViewById(R.id.btn_decline_cancel);

		ListView list_reason_type = (ListView) edit_estimation_time_reason_dialog
				.findViewById(R.id.list_reason_type);

		DBInteraction dbInteraction = DBInteraction
				.getInstance(JobStatusDetailActivity.this);
		// reason type id for decline
		String typeid = JobStatusDetailActivity.this.getResources().getString(
				R.string.ticket_action_reason_type_estimation_time_increment);
		dbInteraction.getConnection();
		ArrayList<DeclineReasonModel> declineReasons = dbInteraction
				.getReasonList(typeid);
		dbInteraction.closeConnection();

		final DeclineReasonAdapter decline_list_adapter = new DeclineReasonAdapter(
				JobStatusDetailActivity.this, R.layout.row_decline_ticket_list,
				declineReasons, edit_stimation_time_reasons_positions,
				list_reason_type, other_reason_value);
		list_reason_type.setAdapter(decline_list_adapter);

		btn_cancel_decline.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				edit_estimation_time_reason_dialog.dismiss();

			}
		});
		btn_ok_decline.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (!decline_list_adapter.isOtherReasonOptionValid()) {
					UtilityFunction.showCenteredToast(
							JobStatusDetailActivity.this, getResources()
									.getString(R.string.fill_reason_others));
					return;
				}

				reason_value = decline_list_adapter.getSelectedReasonsTitle();
				other_reason_value = decline_list_adapter.getOtherReasonText();

				edit_stimation_time_reasons_positions = decline_list_adapter
						.getSelectedReasonsPositions();

				if (reason_value != null) {

					relativeLayout_edit_estimation_time_reason
							.setText(reason_value);
					edit_estimation_time_reason_dialog.dismiss();

				} else
					UtilityFunction.showCenteredToast(
							JobStatusDetailActivity.this, getResources()
									.getString(R.string.one_select_reason));
			}
		});

		edit_estimation_time_reason_dialog.show();
	}

	/**
	 * This method edit estimation time for reasons
	 */
	private void edit_estimation_cost_reason() {

		TextView btn_ok_decline, btn_cancel_decline;
		edit_estimation_time_reason_dialog = new Dialog(
				JobStatusDetailActivity.this, R.style.AppTheme);
		// dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		edit_estimation_time_reason_dialog
				.requestWindowFeature(Window.FEATURE_NO_TITLE);
		edit_estimation_time_reason_dialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		edit_estimation_time_reason_dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		edit_estimation_time_reason_dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.R.color.transparent));

		edit_estimation_time_reason_dialog.setCancelable(false);
		edit_estimation_time_reason_dialog
				.setContentView(R.layout.screen_decline_ticket_popup);
		btn_ok_decline = (TextView) edit_estimation_time_reason_dialog
				.findViewById(R.id.btn_decline_ok);
		btn_cancel_decline = (TextView) edit_estimation_time_reason_dialog
				.findViewById(R.id.btn_decline_cancel);

		ListView list_reason_type = (ListView) edit_estimation_time_reason_dialog
				.findViewById(R.id.list_reason_type);

		DBInteraction dbInteraction = DBInteraction
				.getInstance(JobStatusDetailActivity.this);
		// reason type id for decline
		String typeid = JobStatusDetailActivity.this.getResources().getString(
				R.string.ticket_action_reason_type_estimation_cost_increment);
		dbInteraction.getConnection();
		ArrayList<DeclineReasonModel> declineReasons = dbInteraction
				.getReasonList(typeid);
		dbInteraction.closeConnection();

		final DeclineReasonAdapter decline_list_adapter = new DeclineReasonAdapter(
				JobStatusDetailActivity.this, R.layout.row_decline_ticket_list,
				declineReasons, edit_estimation_cost_positions,
				list_reason_type, other_reason_value);
		list_reason_type.setAdapter(decline_list_adapter);

		btn_cancel_decline.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				edit_estimation_time_reason_dialog.dismiss();

			}
		});
		btn_ok_decline.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (!decline_list_adapter.isOtherReasonOptionValid()) {
					UtilityFunction.showCenteredToast(
							JobStatusDetailActivity.this, getResources()
									.getString(R.string.fill_reason_others));
					return;
				}

				reason_value = decline_list_adapter.getSelectedReasonsTitle();
				other_reason_value = decline_list_adapter.getOtherReasonText();

				edit_estimation_cost_positions = decline_list_adapter
						.getSelectedReasonsPositions();

				if (reason_value != null) {

					relativeLayout_edit_estimation_cost_reason
							.setText(reason_value);
					edit_estimation_time_reason_dialog.dismiss();

				} else
					UtilityFunction.showCenteredToast(
							JobStatusDetailActivity.this, getResources()
									.getString(R.string.one_select_reason));
			}
		});

		edit_estimation_time_reason_dialog.show();
	}

	/**
	 * This is Time Counter for showing remaining time for repairing vechical.
	 * It is calculated from EstimatedTime filled by service engineer while
	 * accessing VanReached option in Map Screen. Its formate is HH:mm:ss .It
	 * keeps decresing per seconds . when reached at 00:00:00 it increases per
	 * second with minus (-) sign before.
	 */

	private void startSLACounDownTimer() {

		int minutes = 120;
		if (ticket.EstimatedTimeForJobComplition != null
				&& ticket.EstimatedTimeForJobComplition.length() > 0
				&& !ticket.EstimatedTimeForJobComplition
						.equalsIgnoreCase("null")) {
			minutes = Integer.parseInt(ticket.EstimatedTimeForJobComplition);
		}
		getCurrentCountDownForEstimationTime(minutes);
		slaCountDownTimer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				if (!isEstimatedTimeToRepaireCrossed) {
					current_estimated_time_countdown = current_estimated_time_countdown - 1000;
					if (current_estimated_time_countdown < 0)
						isEstimatedTimeToRepaireCrossed = true;
				} else
					current_estimated_time_countdown = current_estimated_time_countdown + 1000;

				String formatted = TimeFormater
						.convertSecondsToHMmSs(current_estimated_time_countdown / 1000);
				if (isEstimatedTimeToRepaireCrossed) {
					if (!formatted.equalsIgnoreCase("00:00:00"))
						formatted = "- " + formatted;
				}

				final String shownTime = formatted;

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						tv_time_counter.setText(shownTime);
					}
				});
			}
		};
		slaCountDownTimer.schedule(task, 0, 1000);
	}

	// check slaCountDownTimer null or not. if slaCountDownTimer not null then
	// cancel timer and assign value null
	private void stopSLACounDownTimer() {

		// check slaCountDownTimer
		if (slaCountDownTimer != null) {
			// cancel timer
			slaCountDownTimer.cancel();

			// assign timer value null
			slaCountDownTimer = null;
		}
	}

	/**
	 * This method enable to edit estimation time for job completions and
	 * passing two parameter
	 * 
	 * @param previous_estimated_time
	 * @param new_estimated_time
	 */
	private void editEstimatedTimeForJobComplition(
			long previous_estimated_time, int new_estimated_time) {

		// get value current counter down time passing new estimation time
		getCurrentCountDownForEstimationTime(new_estimated_time);

	}

	/**
	 * This method get current counter down time with passing estimation time
	 * 
	 * @param estimatedTime
	 */

	private void getCurrentCountDownForEstimationTime(int estimatedTime) {

		// get value from ticket last modified time in milli sec
		String lastModifiedTimeInMilliSec = ticket.LastModifiedTimeInMilliSec;

		// get time with default time zone
		long work_in_progress_begin_time = Long
				.parseLong(lastModifiedTimeInMilliSec)
				+ TimeZone.getDefault().getRawOffset();

		// get current time in milli
		long current_time_in_milli = System.currentTimeMillis();

		// assign value from estimation time to minutes
		// 2 hours
		int minutes = estimatedTime;

		// convert time
		long estimated_time_to_repaire = minutes * 60 * 1000;

		// difference time calculation
		long difference_time = current_time_in_milli
				- work_in_progress_begin_time;

		// check time difference
		if (estimated_time_to_repaire - difference_time >= 0) {

			// assign value difference time
			current_estimated_time_countdown = estimated_time_to_repaire
					- difference_time;
			// define false and assign value
			isEstimatedTimeToRepaireCrossed = false;
		} else {
			// difference time calculation
			current_estimated_time_countdown = difference_time
					- estimated_time_to_repaire;

			// define true and assign value
			isEstimatedTimeToRepaireCrossed = true;
		}
	}

	/**
	 * This method Achevied for jobs completion estimation time duration in
	 * minutes.
	 * 
	 * @return
	 */

	private String getAcheviedjobCompletionEstimationDuration() {
		// get value from ticket last modified time in milli sec
		String lastModifiedTimeInMilliSec = ticket.LastModifiedTimeInMilliSec;

		// get time with default time zone
		long work_in_progress_begin_time = Long
				.parseLong(lastModifiedTimeInMilliSec)
				+ TimeZone.getDefault().getRawOffset();
		;
		// get current time in milli
		long current_time_in_milli = System.currentTimeMillis();
		// difference time calculation
		long difference_time = current_time_in_milli
				- work_in_progress_begin_time;

		// return time converted format hours + ":" + minutes + ":" + second;
		return TimeFormater.convertSecondsToHMmSs(difference_time / 1000);

	}

	// open cost estimation selector
	private void openContactSelector() {

		// add estimation cost in the list

		// addEstimationCostDataList();

		final Dialog contactSelectorDialog = new Dialog(
				JobStatusDetailActivity.this, R.style.AppTheme);
		// dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		TextView btn_ok, btn_cancel;

		// initialize component
		contactSelectorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		contactSelectorDialog.setCancelable(false);
		contactSelectorDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		contactSelectorDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		contactSelectorDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.R.color.transparent));

		// inflate pop-up view ,set layout params
		// Inflate pop-up view view,set layout parameter
		View screen_estimation_cost_selector_popup = LayoutInflater.from(
				JobStatusDetailActivity.this).inflate(
				R.layout.screen_contact_selector_popup, null);

		View center_pop_up = screen_estimation_cost_selector_popup
				.findViewById(R.id.center_pop_up);
		LayoutParams center_pop_up_layout_parm = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		int popup_h = 0;

		// ScreenDpi 150 - 200 for mdpi
		// ScreenDpi 200 -300 for mdpi
		if (MyTicketActivity.ScreenDpi > 150
				&& MyTicketActivity.ScreenDpi < 200) {
			popup_h = (int) (MyTicketActivity.ScreenHeight * 0.3);
		} else if (MyTicketActivity.ScreenDpi > 200) {
			popup_h = (int) (MyTicketActivity.ScreenHeight * 0.4);
		}
		int popup_w = (int) (MyTicketActivity.ScreenWidth * 0.8);

		int paop_up_margin_top_bottom = (MyTicketActivity.ScreenHeight - popup_h) / 2;
		int paop_up_margin_lefy_right = (MyTicketActivity.ScreenWidth - popup_w) / 2;

		center_pop_up_layout_parm.leftMargin = paop_up_margin_lefy_right/2;
		center_pop_up_layout_parm.rightMargin = paop_up_margin_lefy_right/2;
		center_pop_up_layout_parm.topMargin = paop_up_margin_top_bottom;
		center_pop_up_layout_parm.bottomMargin = paop_up_margin_top_bottom;

		center_pop_up.setLayoutParams(center_pop_up_layout_parm);

		contactSelectorDialog
				.setContentView(screen_estimation_cost_selector_popup);

		// Hide Cancel Divider
		screen_estimation_cost_selector_popup.findViewById(R.id.didider)
				.setVisibility(View.GONE);
		screen_estimation_cost_selector_popup.findViewById(R.id.btncancel)
				.setVisibility(View.GONE);

		final String Customer_PhoneNum = ticket.CustomeContact_no + "";
		final String Owner_PhoneNum = ticket.OwnerContact_no + "";
		// Customer Contact Number
		TextView txt_contacct_value = (TextView) screen_estimation_cost_selector_popup
				.findViewById(R.id.txt_contacct_value);

		if (ticket.CustomeContact_no == null
				|| (ticket.CustomeContact_no != null && ticket.CustomeContact_no
						.equalsIgnoreCase("null"))) {
			txt_contacct_value.setText("Not Available");
		} else {
			txt_contacct_value.setText("+91-" + ticket.CustomeContact_no);
		}

		View rel_customer_contact = screen_estimation_cost_selector_popup
				.findViewById(R.id.rel_customer_contact);

		rel_customer_contact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (ticket.CustomeContact_no == null
						|| (ticket.CustomeContact_no != null && ticket.CustomeContact_no
								.equalsIgnoreCase("null"))) {
				} else {
					Intent callIntent = new Intent(Intent.ACTION_DIAL);
					callIntent.setData(Uri.parse("tel:"
							+ Uri.encode("+91-" + Customer_PhoneNum.trim())));
					callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// start activity dialog
					startActivity(callIntent);
				}

			}
		});

		// Owner Contact Number
		TextView txt_contacct_value_owner = (TextView) screen_estimation_cost_selector_popup
				.findViewById(R.id.txt_contacct_value_owner);

		if (ticket.OwnerContact_no == null
				|| (ticket.OwnerContact_no != null && ticket.OwnerContact_no
						.equalsIgnoreCase("null"))) {
			txt_contacct_value_owner.setText("Not Available");
		} else {
			txt_contacct_value_owner.setText("+91-" + ticket.OwnerContact_no);
		}

		View rel_owner_contact = screen_estimation_cost_selector_popup
				.findViewById(R.id.rel_owner_contact);

		rel_owner_contact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (ticket.OwnerContact_no == null
						|| (ticket.OwnerContact_no != null && ticket.OwnerContact_no
								.equalsIgnoreCase("null"))) {
				} else {
					Intent callIntent = new Intent(Intent.ACTION_DIAL);
					callIntent.setData(Uri.parse("tel:"
							+ Uri.encode("+91-" + Owner_PhoneNum.trim())));
					callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// start activity dialog
					startActivity(callIntent);
				}

			}
		});

		// set views values
		btn_ok = (TextView) screen_estimation_cost_selector_popup
				.findViewById(R.id.estimationok);

		// click ok button estimation dialog

		btn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				contactSelectorDialog.dismiss();
			}
		});

		btn_cancel = (TextView) contactSelectorDialog
				.findViewById(R.id.btncancel);
		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				contactSelectorDialog.dismiss();
			}
		});

		contactSelectorDialog.show();
	}
}
