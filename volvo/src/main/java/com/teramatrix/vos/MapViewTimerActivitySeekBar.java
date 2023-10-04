package com.teramatrix.vos;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.teramatrix.vos.adapter.DeclineReasonAdapter;
import com.teramatrix.vos.adapter.EstimationCostAdapter;
import com.teramatrix.vos.checkinternet.CheckInternetConnection;
import com.teramatrix.vos.database.DBInteraction;
import com.teramatrix.vos.firebase.service.MyFirebaseMessagingService;
import com.teramatrix.vos.interfaces.INetworkAvailablity;
import com.teramatrix.vos.map.path.GMapV2Direction;
import com.teramatrix.vos.map.path.GetDirectionsAsyncTask;
import com.teramatrix.vos.model.DeclineReasonModel;
import com.teramatrix.vos.model.EstimationCostModel;
import com.teramatrix.vos.model.Ticket;
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
 * This class MapViewTimerActivity shown Google map with draw route path from
 * source location to destination location . van reached clicking on that will
 * show a popup box having following input fields -Problem Description,Estimated
 * Time to Job Complete TimePicker slider,Estimated Cost for repairing list,
 * User have to input all three details to proceed to next status of
 * Ticket(VanReached). A Time Counter is shown on Top of screen which keep
 * decreasing. It shows remaining SLA Time.
 * 
 * @author Gaurav.Mangal
 */
public class MapViewTimerActivitySeekBar extends FragmentActivity implements OnMapReadyCallback,
		OnClickListener, INetworkAvailablity {

	String TAG = this.getClass().getSimpleName();
	// define TextView component of a class
	TextView tv_time_counter, btn_vanReached, tv_title_estimation_desc, btn_ok,
			btn_cancel, tv_title_estimation_cost, tv_title_estimation_time,
			tv_contactnumber;

	// define dialog for the class
	Dialog timeEstimationDialog, estimationCostDialog;

	// define Button component of a class
	Button btn_viewmapNavigation;

	// Defining TypeFace font component that will be used in this activity
	Typeface centuryGothic;
	Typeface verdana, typeFace_Rupee;

	// define EditText component of a class
	EditText et_problemDescription, et_estimationTime, et_estimationCost,
			ed_sla_missed_reason, ed_pd_reason;

	// Defining Googlemap component that will be used in this activity
	private GoogleMap googleMap;

	// Defining double value that will be used in this activity
	double source_lat, source_long;
	// Defining Polyline drawing component that will be used in this activity
	private Polyline newPolyline;

	// define list view
	ListView list_estimationcost;
	// define EstimationCostAdapter class instance in this activity
	EstimationCostAdapter estimationcost_adapter;
	// define list model
	List<EstimationCostModel> list_estimation_cost_data;
	List<DeclineReasonModel> list_slaMissedReason;

	// define imageview component
	ImageView img_network;
	ImageView img_call;

	// define double value in this activity
	double destination_lat, destination_lng;

	// define ticket instance of an activity
	Ticket ticket;

	// define String variable values in this activity
	String estimation_cost_selected_range = "0";
	String estimation_cost_selected_id = "0";
	String sla_missed_reasons_string, sla_missed_reasons_ids,
			sla_missed_reasons_selected_positions;

	String pd_reasons_string, pd_reasons_ids, pd_reasons_selected_positions;

	// define textview in this activity
	TextView seek_hoursValue;
	// define long value current_sla_countdown
	long current_sla_countdown;

	// define string value EstimatedTimeForJobComplition
	String EstimatedTimeForJobComplition = "60";

	String other_reason;
	// define boolean value
	boolean isSlaMissed;

	// define timer instance
	private Timer slaCountDownTimer;

	// show moving marker
	Marker movingMarker;

	View relativelayout_contact;

	private final BroadcastReceiver updateBroadCastReciever =new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			final ProgressDialog pd = new ProgressDialog(
					MapViewTimerActivitySeekBar.this);
			try {

				pd.setMessage(getResources().getString(
						R.string.updating_tickets));
				pd.setCancelable(false);
				pd.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// listUpdateStatusBar.setVisibility(View.VISIBLE);

			List<Ticket>  list_openTicket = MyTicketManager.getInstance(MapViewTimerActivitySeekBar.this)
					.getAllOpenedTicketsFromLocalDB();
			if (list_openTicket == null)
				list_openTicket = new ArrayList<Ticket>();

			List<Ticket>   list_closed_tickets = MyTicketManager.getInstance(MapViewTimerActivitySeekBar.this)
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

			for (Ticket ticket:list_openTicket
					) {
				if (ticket.getId().equalsIgnoreCase(MapViewTimerActivitySeekBar.this.ticket.getId())){
					MapViewTimerActivitySeekBar.this.ticket=ticket;
					setDataUI();
				}
			}
			pd.dismiss();

		}
	};

	// receiving push using pushBroadCastReceiver
	private final BroadcastReceiver pushBroadCastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String type = intent.getStringExtra("type");
			if (type != null && type.equalsIgnoreCase("pendingTicket")) {
				NewTicketManager newTicketManager = NewTicketManager
						.getInstance(MapViewTimerActivitySeekBar.this);
				newTicketManager
						.receviedNewTicketPush(ApplicationConstant.IS_APP_IN_FORGROUND);
				/*
				 * new VECVPreferences(MyTicketActivity.this)
				 * .decresePendingTicketsCount();
				 */
			} else {

				if (!MyTicketActivity.isNewTicketPopUpShowing) {
					NewTicketManager newTicketManager = NewTicketManager
							.getInstance(MapViewTimerActivitySeekBar.this);
					newTicketManager
							.receviedNewTicketPush(ApplicationConstant.IS_APP_IN_FORGROUND);
				} else {

					new VECVPreferences(MapViewTimerActivitySeekBar.this)
							.addPendingTicketsCount();
				}
			}

		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 * 
	 * This function call when activity start and call itself
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// set no title and full screen view of an activity
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		/*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

		//change Status bar color
		Window window = getWindow();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.setStatusBarColor(ContextCompat.getColor(this,R.color.volvo_blue));
		}

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		setContentView(R.layout.screen_map);

		try {
			// get serializable value from ticket object
			ticket = (Ticket) getIntent().getSerializableExtra("ticket");

			// setup data ui component
			setDataUI();

			// call MapLoad and draw path
			new MapLoadAsy().execute();
		} catch (Exception e) {

			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);

			// save error log in file
			UtilityFunction.saveErrorLog(MapViewTimerActivitySeekBar.this, e);
		}

	}

	/**
	 * This method setup the ui componenet of an activity
	 */
	private void setDataUI() {

		// initialize component
		btn_vanReached = (TextView) findViewById(R.id.btn_van_reached);
		tv_time_counter = (TextView) findViewById(R.id.tv_time_counter);
		// initialize TypeFace Font view component that will be used in this
		// activity
		centuryGothic = Typeface.createFromAsset(getAssets(), "gothic_0.TTF");
		verdana = Typeface.createFromAsset(getAssets(), "Verdana.ttf");
		typeFace_Rupee = Typeface.createFromAsset(getAssets(),
				"Rupee_Foradian.ttf");
		tv_contactnumber = (TextView) findViewById(R.id.tv_contactnumber);
		tv_contactnumber.setText("Contacts");
		relativelayout_contact = findViewById(R.id.rlTitle_bar_contact);

		// asssign string variable
		final String Customer_PhoneNum = ticket.CustomeContact_no + "";

		// make a click contact then call on shown number
		relativelayout_contact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openContactSelector();
			}
		});

		// define ui componenet value
		btn_viewmapNavigation = (Button) findViewById(R.id.btn_navigation);

		img_network = (ImageView) findViewById(R.id.img_network);
		img_call = (ImageView) findViewById(R.id.img_call);

		img_call.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent callIntent = new Intent(Intent.ACTION_DIAL);
				callIntent.setData(Uri.parse("tel:18005724860"));
				startActivity(callIntent);
			}
		});

		if(ticket.TicketStatus.equalsIgnoreCase("2")||ticket.TicketStatus.equalsIgnoreCase("12"))
		{
			btn_vanReached.setText("Trip Start");
		}else
		{
			btn_vanReached.setText("Van Reached");
		}

		// click button van reached
		btn_vanReached.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// open estimation time dialog when press van reached
				Log.e(TAG, "onClick: ticket status "+ticket.TicketStatus );

				if (ticket.TicketStatus.equalsIgnoreCase("3")) {
					//Curretn Ticket status ->3 i.e In Progress(After Van reached confirmed).
					//In this case a button will be shown (Van Reached).
					//When user click (Van Reached) button a Problem description dialog will be shown to confirm Problem,Estimated ime to repaire and Estimated cost  of repaire.
					//On confirming Problem description,Notify to server with new Ticket Status '3'(In Progress).

					showTimeEstimationDialog();
				}else if(ticket.TicketStatus.equalsIgnoreCase("2")||ticket.TicketStatus.equalsIgnoreCase("12"))
				{
					//Curretn Ticket status ->2 i.e Assigned
						/*
						* In this case a button will be shown (Trip Start).
						* When user click (Trip Start) button a confirmation dialog will be shown to confirm trip Start from user.
						  * On confirming Trip Start, Trip will be started (Notify to server with new Ticket Status '9'(Trip Start)).
						  * Trip Start button text will be changed to 'Van Reached'
						* */
					showTripStartConfirmDialog();

				}
				else if(ticket.TicketStatus.equalsIgnoreCase("9")||ticket.TicketStatus.equalsIgnoreCase("13")) {
					//Curretn Ticket status ->9 i.e Trip Start.
					// In this case a button will be shown (Van Reached).
					//When user click (Van Reached) button a confirmation dialog will be shown to confirm Van reached at breakdown location.
					//On confirming Van Reached ,Notify to server with new Ticket Status '3'(In Progress).

					// Show Van reached Confirmation
					showVanReachedConfirmDialog();
				}
			}
		});

		// click button navigation
		btn_viewmapNavigation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// check lat long value from source

				if (source_lat == 0.0 || source_long == 0.0) {

					// show centered toast
					UtilityFunction.showCenteredToast(
							MapViewTimerActivitySeekBar.this, getResources()
									.getString(R.string.location_not_found));
				} else {
					// showing navigation with map on mobile phone with showing
					// direction map views those we are going to use in this
					// activity
					Intent navigation = new Intent(Intent.ACTION_VIEW, Uri
							.parse("http://maps.google.com/maps?saddr="
									+ source_lat + "," + source_long
									+ "&daddr=" + destination_lat + ","
									+ destination_lng));
					// start map navigation view we are going to use in this
					// activity
					startActivity(navigation);
				}

			}
		});
	}

	// define instacne component
	EditText ed_problem_description;
	TextView txt_timer1_time;
	TextView txt_timer2_time;

	// show time estimation dialog
	/**
	 * A pop-up is shown , user have to input Problem description(Text limit
	 * 255) , select estimated time (min 1 to max24) ,select estimated repairing
	 * cost .
	 */
	public void showTimeEstimationDialog() {

		// set string value in empty
		estimation_cost_selected_range = "";
		estimation_cost_selected_id = "";
		sla_missed_reasons_selected_positions = "";

		timeEstimationDialog = new Dialog(MapViewTimerActivitySeekBar.this,
				android.R.style.Theme_Translucent);
		// dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		timeEstimationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		timeEstimationDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		timeEstimationDialog.setCancelable(false);

		// Inflate pop-up view view,set layout parameter
		View screen_van_reached_popup_seekbar = LayoutInflater.from(
				MapViewTimerActivitySeekBar.this).inflate(
				R.layout.screen_van_reached_popup_seekbar, null);

		
		TextView textView  = (TextView)screen_van_reached_popup_seekbar.findViewById(R.id.txt_problem_description);
		
		
		View center_pop_up = screen_van_reached_popup_seekbar
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

		center_pop_up_layout_parm.leftMargin = paop_up_margin_lefy_right/2;
		center_pop_up_layout_parm.rightMargin = paop_up_margin_lefy_right/2;
		center_pop_up_layout_parm.topMargin = paop_up_margin_top_bottom/2;
		center_pop_up_layout_parm.bottomMargin = paop_up_margin_top_bottom/2;

		center_pop_up.setLayoutParams(center_pop_up_layout_parm);
		timeEstimationDialog.setContentView(screen_van_reached_popup_seekbar);

		// set vies values
		TextView txt_signcost = (TextView) timeEstimationDialog
				.findViewById(R.id.txt_signcost);
		txt_signcost.setTypeface(typeFace_Rupee);
		txt_signcost.setText("(" + "`" + ")");
		timeEstimationDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		ed_problem_description = (EditText) timeEstimationDialog
				.findViewById(R.id.ed_problem_description);
		
		if(ticket.Priority.equalsIgnoreCase(Ticket.TICKET_PRIORITY_TELIMATIC))
		{
			//change Description -> DTC Information in case of Telimatic Ticket
			textView.setText("DTC Information");
			if(ticket.Description!=null)
			{
			ed_problem_description.setText(ticket.Description);
			ed_problem_description.setSelection(ticket.Description.length());
			}
		}
		

		TextView txt_estimation_distance_value = (TextView) timeEstimationDialog
				.findViewById(R.id.txt_estimation_distance_value);
		float distance_total;
		try {
			distance_total = NumberFormat
					.getNumberInstance(java.util.Locale.US)
					.parse(ticket.EstimatedDistance).floatValue() * 2;
			txt_estimation_distance_value.setText(ticket.EstimatedDistance
					+ "+" + ticket.EstimatedDistance + "=" + distance_total);
		} catch (Exception e) {
			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);

			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ed_problem_description.setImeOptions(EditorInfo.IME_ACTION_DONE);

		// disable parent scroll if touched on this view
		ed_problem_description.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.getParent().requestDisallowInterceptTouchEvent(true);
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
					v.getParent().requestDisallowInterceptTouchEvent(false);
					break;
				}
				return false;
			}
		});

		// create value instance of a seek bar
		SeekBar seekBar = (SeekBar) timeEstimationDialog
				.findViewById(R.id.seek_bar_timer);

		// initilize component value textview
		seek_hoursValue = (TextView) timeEstimationDialog
				.findViewById(R.id.txt_estimation_time_value);

		// set textbox value
		seek_hoursValue.setText("0");

		// set max value in seekbar
		seekBar.setMax(24 * 2);

		/**
		 * onProgressChanged() refresh a TextView value that should show the
		 * current progress of the SeekBar. But the TextView is just updated on
		 * first touch and last touch
		 */

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub

				// calculate hour value
				int hours = progress / 2; // it will return hours.

				// calculate minute value
				int minutes = (progress % 2) * 30; // here will be minutes.

				// check minute value
				if (minutes == 0)

					// set value in textview
					seek_hoursValue.setText(hours + ":" + minutes + "0");
				else
					// set value in textview
					seek_hoursValue.setText(hours + ":" + minutes);

				// set value initilize string variable
				// EstimatedTimeForJobComplition
				int time_in_minutes = (hours * 60 + minutes);

				if ((time_in_minutes == 0))
					EstimatedTimeForJobComplition = 60 + "";
				else
					EstimatedTimeForJobComplition = time_in_minutes + "";
			}

			/**
			 * A callback that notifies clients when the progress level has been
			 * changed. This includes changes that were initiated by the user
			 * through a touch gesture or arrow key/trackball as well as changes
			 * that were initiated programmatically.
			 */
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * android.widget.SeekBar.OnSeekBarChangeListener#onStopTrackingTouch
			 * (android.widget.SeekBar)
			 * 
			 * The onStopTrackTouch method defined in OnSeekBarChangeListener is
			 * called when the user stops sliding the SeekBar (i.e., has
			 * finished the touch gesture) and provides the "final value".
			 */
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
		});

		// initialize textview component
		txt_timer1_time = (TextView) timeEstimationDialog
				.findViewById(R.id.txt_timer1_time);

		// set value textview
		txt_timer1_time.setText("00");
		// initialize textview component
		txt_timer2_time = (TextView) timeEstimationDialog
				.findViewById(R.id.txt_timer2_time);

		// set value textview
		txt_timer2_time.setText("00");

		// initialize textview component
		btn_ok = (TextView) timeEstimationDialog.findViewById(R.id.btnok);
		btn_cancel = (TextView) timeEstimationDialog
				.findViewById(R.id.btncancel);

		// set listener value
		btn_ok.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);

		// shown estimation dialog
		timeEstimationDialog.show();

		// initialize textview component
		et_estimationCost = (EditText) timeEstimationDialog
				.findViewById(R.id.ed_estimation_cost);

		TextView txt_sla_missed_reason = (TextView) timeEstimationDialog
				.findViewById(R.id.txt_sla_missed_reason);
		ed_sla_missed_reason = (EditText) timeEstimationDialog
				.findViewById(R.id.ed_sla_missed_reason);

		ed_pd_reason = (EditText) timeEstimationDialog
				.findViewById(R.id.ed_pd_reason);

		// get sla status of this ticket from shared preference
		VECVPreferences preferences = new VECVPreferences(
				MapViewTimerActivitySeekBar.this);
		isSlaMissed = preferences.getSlaStatusForTicketId(ticket.Id);
		// check isSlaMissed value
		if (isSlaMissed) {

			// set visibility txt_sla_missed_reason
			txt_sla_missed_reason.setVisibility(View.VISIBLE);
			ed_sla_missed_reason.setVisibility(View.VISIBLE);
		} else {
			// set visibility txt_sla_missed_reason
			txt_sla_missed_reason.setVisibility(View.GONE);
			ed_sla_missed_reason.setVisibility(View.GONE);

		}

		// edit estimation touch listener
		et_estimationCost.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				// get action down value
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					DBInteraction dbInteraction = DBInteraction
							.getInstance(MapViewTimerActivitySeekBar.this);
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
										MapViewTimerActivitySeekBar.this);
								pd.setMessage("Updating Cost from server...");
								pd.setCancelable(false);
								pd.show();
							}

							@Override
							protected Void doInBackground(Void... params) {
								// TODO Auto-generated method stub

								// Get Estiamtion cost list from server
								MyTicketManager.getInstance(
										MapViewTimerActivitySeekBar.this)
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

		// edit estimation ed_sla_missed_reason
		ed_sla_missed_reason.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				// get action down value
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					// open SlaMissedReasonList in listview
					openSlaMissedReasonList();

					// return boolean value
					return true;
				}

				// return boolean value
				return false;
			}
		});

		// edit estimation ed_sla_missed_reason
		ed_pd_reason.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				// get action down value
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					// problem description in listview

					problem_DescriptionReasonList();
					// return boolean value
					return true;
				}

				// return boolean value
				return false;
			}
		});

		// Hour Counter View
		View addHourView = timeEstimationDialog
				.findViewById(R.id.txt_timer1_add);
		View minusHourView = timeEstimationDialog
				.findViewById(R.id.txt_timer1_minus);
		final TextView hourValueView = (TextView) timeEstimationDialog
				.findViewById(R.id.txt_timer1_time);

		// Min. Counter View
		View addMinView = timeEstimationDialog
				.findViewById(R.id.txt_timer2_add);
		View minusMinView = timeEstimationDialog
				.findViewById(R.id.txt_timer2_minus);
		final TextView minValueView = (TextView) timeEstimationDialog
				.findViewById(R.id.txt_timer2_time);

		/**
		 * This function works with hours value and set value in textview
		 */

		addHourView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				// get hours value
				String value_hour_string = hourValueView.getText().toString();

				// get hours int value
				int value_hour_int = Integer.parseInt(value_hour_string);
				// check hour value
				if (value_hour_int <= 23) {

					// increment hour value
					value_hour_int++;
					// check hours int value
					if (value_hour_int < 10)

						// set value in textview
						hourValueView.setText("0" + value_hour_int);
					else
						// set value in textview
						hourValueView.setText("" + value_hour_int);
				}

				// check hour value
				if (value_hour_int == 24) {
					// set value in textview
					minValueView.setText("00");
				}
			}
		});

		/**
		 * This function works with minusHourView value and set value in
		 * textview
		 */
		minusHourView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				// get value from hourValueView
				String value_hour_string = hourValueView.getText().toString();

				int value_hour_int = Integer.parseInt(value_hour_string);
				// check value_hour_int value compare
				if (value_hour_int >= 1) {
					value_hour_int--;

					// check value_hour_int value compare
					if (value_hour_int < 10)

						// set textview value
						hourValueView.setText("0" + value_hour_int);
					else
						// set textview value
						hourValueView.setText("" + value_hour_int);

				}
			}
		});

		/**
		 * This function works with addMinView value and set value in textview
		 */
		addMinView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				// get hourValueView value
				String value_hour_string = hourValueView.getText().toString();
				int value_hour_int = Integer.parseInt(value_hour_string);

				// check condition
				if (value_hour_int == 24)
					return;

				String value_min_string = minValueView.getText().toString();
				int value_min_int = Integer.parseInt(value_min_string);
				// check minutes value compare
				if (value_min_int <= 58) {
					value_min_int++;
					// check condition
					if (value_min_int < 10)
						minValueView.setText("0" + value_min_int);
					else
						minValueView.setText("" + value_min_int);
				} else {
					// check condition
					if (value_hour_int <= 23) {
						// set value textview
						minValueView.setText("00");
						value_hour_int++;
						if (value_hour_int < 10)
							// set value textview
							hourValueView.setText("0" + value_hour_int);
						else
							// set value textview
							hourValueView.setText("" + value_hour_int);
					}
				}
			}
		});

		/**
		 * This function works with minusMinView value and set value in textview
		 */
		minusMinView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String value_min_string = minValueView.getText().toString();
				int value_min_int = Integer.parseInt(value_min_string);

				// check condition
				if (value_min_int >= 1) {

					// decrement value_min_int
					value_min_int--;
					// check decrement value_min_int
					if (value_min_int < 10)
						// set value textview
						minValueView.setText("0" + value_min_int);
					else
						// set value textview
						minValueView.setText("" + value_min_int);

				}
			}
		});

	}

	/**
	 * check validation send value to server
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnok: {

			// validation check
			String pd = "";

			/*
			 * if (false && (ed_pd_reason.getText().toString()
			 * .equalsIgnoreCase("") || ed_pd_reason.getText().toString()
			 * .length() == 0)) { UtilityFunction.showCenteredToast(
			 * MapViewTimerActivitySeekBar.this, getResources()
			 * .getString(R.string.one_select_reason)); return; }
			 */

			if (estimation_cost_selected_range == null
					|| estimation_cost_selected_range.length() == 0) {
				// show center toast
				UtilityFunction.showCenteredToast(
						MapViewTimerActivitySeekBar.this, getResources()
								.getString(R.string.estimation_cost));
				return;
			} else if (isSlaMissed
					&& (sla_missed_reasons_ids == null || sla_missed_reasons_ids
							.length() == 0)) {
				// show center toast
				UtilityFunction.showCenteredToast(
						MapViewTimerActivitySeekBar.this, getResources()
								.getString(R.string.missing_sla_time));
				return;
			} else {
				if (ed_problem_description.getText().toString()
						.equalsIgnoreCase("")
						|| ed_problem_description.getText().toString().length() == 0) {
					// pd = ed_pd_reason.getText().toString();
					UtilityFunction.showCenteredToast(
							MapViewTimerActivitySeekBar.this,
							getResources().getString(
									R.string.fill_problem_description));
					return;

				} else
					pd = ed_problem_description.getText().toString().trim();

			}

			// check ticket null or not
			if (ticket != null) {

				try {

					// In progress -3
					ticket.TicketStatus = "3";

					if (sla_missed_reasons_string != null)
						ticket.SuggestionComment = pd + ","
								+ sla_missed_reasons_string;
					else
						ticket.SuggestionComment = pd;

					// get device alias value
					ticket.LastModifiedBy = new VECVPreferences(
							MapViewTimerActivitySeekBar.this).getDevice_alias();

					// get lastmodify time
					long lastmdify_time_in_milli = TimeFormater
							.getModifiedTimeInMillisecond(ticket.LastModifiedTime);

					// get sla_achevied_time time
					long sla_achevied_time = (System.currentTimeMillis() - TimeZone
							.getDefault().getRawOffset())
							- lastmdify_time_in_milli;

					ticket.SlaTimeAchevied = (sla_achevied_time / (1000 * 60))
							+ "";
					ticket.LastModifiedTime = UtilityFunction
							.currentUTCTimeWithoutDash();

					// passing value and intilize
					ticket.EstimatedTimeForJobComplition = EstimatedTimeForJobComplition;
					ticket.EstimatedCostForJobComplition = estimation_cost_selected_range;
					ticket.slaMissedReasonId = sla_missed_reasons_ids;
					ticket.isVanReachedConfirmed = true;

				} catch (Exception e) {
					// Google Analytic -Tracking Exception
					EosApplication.getInstance().trackException(e);

					// save error log in file
					UtilityFunction.saveErrorLog(
							MapViewTimerActivitySeekBar.this, e);
				}
			}

			// call api van reached
			MyTicketManager myTicketManager = MyTicketManager
					.getInstance(MapViewTimerActivitySeekBar.this);

			// call api
			myTicketManager.api_VanReached(ticket);

			// Googel Analytic - Event
			EosApplication.getInstance().trackEvent(
					"Assigned Ticket",
					"Van Reached",
					"Van Reached Button Pressed for Ticket Id "
							+ ticket.getId());

		}

			break;
		case R.id.btncancel:

			pd_reasons_selected_positions = "";
			// dismiss time estimation dialog
			timeEstimationDialog.dismiss();
			break;
		default:
			break;
		}

	}

	/**
	 * This method used for setting google map and located on SupportMapFragment
	 * class
	 * 
	 * @param lat
	 * @param lng
	 */
	@SuppressLint("NewApi")
	private void myLocationOnGoogleMap(double lat, double lng) {
		// Getting Google Play availability status
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getBaseContext());

		// Showing status
		if (status != ConnectionResult.SUCCESS) { // Google Play Services are
													// not available

			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
					requestCode);
			dialog.show();
		} else {
			// Google Play Services are available
			SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.maplocation);
			//googleMap = mapFragment.getMap();
			mapFragment.getMapAsync(this);
		}

	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		this.googleMap = googleMap;

		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		googleMap.setMyLocationEnabled(true);
		googleMap.getUiSettings().setZoomControlsEnabled(true);
		googleMap.getUiSettings().setZoomGesturesEnabled(true);
		googleMap.getUiSettings().setCompassEnabled(true);
		googleMap.getUiSettings().setMyLocationButtonEnabled(true);
		googleMap.getUiSettings().setRotateGesturesEnabled(true);
		// Enabling MyLocation Layer of Google Map
		googleMap.getUiSettings().setMyLocationButtonEnabled(true);

		// googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000,
		// null);

		drawMarker(source_lat, source_long, destination_lat, destination_lng);

		if (new CheckInternetConnection(MapViewTimerActivitySeekBar.this)
				.isConnectedToInternet())
			findDirections(source_lat, source_long, destination_lat, destination_lng,
					GMapV2Direction.MODE_DRIVING);

	}



	/**
	 * This method used for drawing a marker positions
	 * 
	 * @param source_lat
	 * @param source_long
	 * @param dest_lat
	 * @param dest_long
	 */

	public void drawMarker(double source_lat, double source_long,
			double dest_lat, double dest_long) {
		LatLng currentPosition = new LatLng(source_lat, source_long);
		LatLng destPosition = new LatLng(dest_lat, dest_long);

		// Creating an instance of MarkerOptions
		MarkerOptions markerOptions1 = new MarkerOptions();
		markerOptions1.title(getResources().getString(R.string.source));
		markerOptions1.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.red_s));
		markerOptions1.position(currentPosition);
		MarkerOptions markerOptions2 = new MarkerOptions();
		markerOptions2.title(getResources().getString(R.string.destination));
		markerOptions2.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.red_d));
		markerOptions2.position(destPosition);
		// Adding marker on the Google Map
		googleMap.addMarker(markerOptions1).showInfoWindow();
		googleMap.addMarker(markerOptions2).showInfoWindow();

		// change the camera location in new lating bound in android
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		builder.include(currentPosition);
		builder.include(destPosition);
		LatLngBounds bounds = builder.build();
		CameraUpdate yourLocation = CameraUpdateFactory.newLatLngBounds(bounds,
				0);

		googleMap.animateCamera(yourLocation);

	}

	/**
	 * This method for handle a direction path on google map
	 * 
	 * @param directionPoints
	 */
	public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints) {
		PolylineOptions rectLine = new PolylineOptions().width(5).color(
				Color.rgb(0, 179, 253));
		for (int i = 0; i < directionPoints.size(); i++) {
			rectLine.add(directionPoints.get(i));
		}
		if (newPolyline != null) {
			newPolyline.remove();
		}
		newPolyline = googleMap.addPolyline(rectLine);
	}

	/**
	 * 
	 * @param fromPositionDoubleLat
	 * @param fromPositionDoubleLong
	 * @param toPositionDoubleLat
	 * @param toPositionDoubleLong
	 * @param mode
	 */
	public void findDirections(double fromPositionDoubleLat,
			double fromPositionDoubleLong, double toPositionDoubleLat,
			double toPositionDoubleLong, String mode) {

		// add a value in the map
		Map<String, String> map = new HashMap<String, String>();
		map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT,
				String.valueOf(fromPositionDoubleLat));
		map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG,
				String.valueOf(fromPositionDoubleLong));
		map.put(GetDirectionsAsyncTask.DESTINATION_LAT,
				String.valueOf(toPositionDoubleLat));
		map.put(GetDirectionsAsyncTask.DESTINATION_LONG,
				String.valueOf(toPositionDoubleLong));
		map.put(GetDirectionsAsyncTask.DIRECTIONS_MODE, mode);

		GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask(this);
		asyncTask.execute(map);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onStart()
	 */
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		try {
			super.onStart();
			GoogleAnalytics.getInstance(MapViewTimerActivitySeekBar.this)
					.reportActivityStart(this);
			if (ticket.TicketStatus != null
					&& ticket.TicketStatus.equalsIgnoreCase("3")) {
				//tv_time_counter.setText("Work In Progress");
				tv_time_counter.setText("WIP");
			} else
				startSLACounDownTimer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	InternetAvailabilityRecever internetAvailabilityRecever=new InternetAvailabilityRecever();
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		try {
			registerPushRecevier();
			IntentFilter filter = new IntentFilter();
			filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
			registerReceiver(internetAvailabilityRecever, filter);
			registerReceiver(updateBroadCastReciever, new IntentFilter(
					SyncTicketsService.UPDATEBROADCAST));
			// Google Analytic
			EosApplication.getInstance().trackScreenView("Van Reached Screen");

			System.out.println("MapViewTimerActivity.onResume()");
			ApplicationConstant.IS_APP_IN_FORGROUND = true;
			ApplicationConstant.currentActivityContext = this;

			isNetworkAvailable(new CheckInternetConnection(
					MapViewTimerActivitySeekBar.this).isConnectedToInternet());

			if (!MyTicketActivity.isNewTicketPopUpShowing)
				NewTicketManager.getInstance(this).checkForNewTicket();
		} catch (Exception e) {

			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);

			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onPause()
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
	 * @see android.support.v4.app.FragmentActivity#onStop()
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		stopSLACounDownTimer();
	}

	// open cost estimation selector
	private void openCostSelector() {

		// add estimation cost in the list

		addEstimationCostDataList();

		estimationCostDialog = new Dialog(MapViewTimerActivitySeekBar.this,
				R.style.AppTheme);
		// dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		TextView btn_ok, btn_cancel;

		// initialize component
		estimationCostDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		estimationCostDialog.setCancelable(false);
		estimationCostDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		estimationCostDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		estimationCostDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.R.color.transparent));

		estimationCostDialog.setCancelable(false);

		// inflate pop-up view ,set layout params
		// Inflate pop-up view view,set layout parameter
		View screen_estimation_cost_selector_popup = LayoutInflater.from(
				MapViewTimerActivitySeekBar.this).inflate(
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

		center_pop_up_layout_parm.leftMargin = paop_up_margin_lefy_right/2;
		center_pop_up_layout_parm.rightMargin = paop_up_margin_lefy_right/2;
		center_pop_up_layout_parm.topMargin = paop_up_margin_top_bottom/2;
		center_pop_up_layout_parm.bottomMargin = paop_up_margin_top_bottom/2;

		center_pop_up.setLayoutParams(center_pop_up_layout_parm);

		estimationCostDialog
				.setContentView(screen_estimation_cost_selector_popup);

		// set views values
		btn_ok = (TextView) estimationCostDialog
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
										MapViewTimerActivitySeekBar.this,
										"Plesae enter some value.");
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
						/*
						 * String cost_select_value = list_estimation_cost_data
						 * .get(estimationcost_adapter
						 * .getSelectedPosition()).getCost_range();
						 * 
						 * String cost_selected_id = list_estimation_cost_data
						 * .get(estimationcost_adapter
						 * .getSelectedPosition()).getId();
						 */
						// temp
						estimation_cost_selected_id = cost_selected_id;
						estimation_cost_selected_range = cost_select_value;

						et_estimationCost.setText(cost_select_value);
						estimationCostDialog.dismiss();
					} else {
						UtilityFunction.showCenteredToast(
								MapViewTimerActivitySeekBar.this,
								getResources().getString(
										R.string.select_one_options));
					}
				}

			}
		});

		btn_cancel = (TextView) estimationCostDialog
				.findViewById(R.id.btncancel);
		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				estimationCostDialog.dismiss();
			}
		});

		list_estimationcost = (ListView) estimationCostDialog
				.findViewById(R.id.list_estimation_cost);

		estimationcost_adapter = new EstimationCostAdapter(
				MapViewTimerActivitySeekBar.this, R.layout.row_estimation_cost,
				list_estimation_cost_data, estimation_cost_selected_id,
				list_estimationcost);
		list_estimationcost.setAdapter(estimationcost_adapter);
		estimationCostDialog.show();
	}

	// show list when sla missed reason in list
	private void openSlaMissedReasonList() {

		TextView btn_ok_decline, btn_cancel_decline;
		final Dialog declineTicketDialog = new Dialog(
				MapViewTimerActivitySeekBar.this, R.style.AppTheme);
		// dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		declineTicketDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		declineTicketDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		declineTicketDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		declineTicketDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.R.color.transparent));

		declineTicketDialog.setCancelable(false);
		declineTicketDialog
				.setContentView(R.layout.screen_decline_ticket_popup);
		btn_ok_decline = (TextView) declineTicketDialog
				.findViewById(R.id.btn_decline_ok);
		btn_cancel_decline = (TextView) declineTicketDialog
				.findViewById(R.id.btn_decline_cancel);

		ListView list_reason_type = (ListView) declineTicketDialog
				.findViewById(R.id.list_reason_type);

		DBInteraction dbInteraction = DBInteraction
				.getInstance(MapViewTimerActivitySeekBar.this);
		// reason type id for decline
		String typeid = getResources().getString(
				R.string.ticket_action_reason_type_sla_missed);
		dbInteraction.getConnection();
		ArrayList<DeclineReasonModel> declineReasons = dbInteraction
				.getReasonList(typeid);
		dbInteraction.closeConnection();

		final DeclineReasonAdapter decline_list_adapter = new DeclineReasonAdapter(
				MapViewTimerActivitySeekBar.this,
				R.layout.row_decline_ticket_list, declineReasons,
				sla_missed_reasons_selected_positions, list_reason_type,
				other_reason);

		list_reason_type.setAdapter(decline_list_adapter);

		btn_cancel_decline.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				declineTicketDialog.dismiss();

			}
		});
		btn_ok_decline.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (!decline_list_adapter.isOtherReasonOptionValid()) {
					UtilityFunction.showCenteredToast(
							MapViewTimerActivitySeekBar.this, getResources()
									.getString(R.string.fill_reason_others));
					return;
				}

				String reason_value = decline_list_adapter
						.getSelectedReasonsTitle();
				other_reason = decline_list_adapter.getOtherReasonText();

				if (reason_value != null) {
					sla_missed_reasons_string = decline_list_adapter
							.getSelectedReasonsTitle();
					if (sla_missed_reasons_string != null)
						ed_sla_missed_reason.setText(sla_missed_reasons_string);

					sla_missed_reasons_ids = decline_list_adapter
							.getSelectedReasonsIds();
					sla_missed_reasons_selected_positions = decline_list_adapter
							.getSelectedReasonsPositions();
					declineTicketDialog.dismiss();
				} else
					UtilityFunction.showCenteredToast(
							MapViewTimerActivitySeekBar.this, getResources()
									.getString(R.string.one_select_reason));
			}
		});

		declineTicketDialog.show();
	}

	// show list when sla missed reason in list
	private void problem_DescriptionReasonList() {

		TextView btn_ok_decline, btn_cancel_decline;
		final Dialog declineTicketDialog = new Dialog(
				MapViewTimerActivitySeekBar.this, R.style.AppTheme);
		// dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		declineTicketDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		declineTicketDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		declineTicketDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		declineTicketDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.R.color.transparent));

		declineTicketDialog.setCancelable(false);
		declineTicketDialog
				.setContentView(R.layout.screen_decline_ticket_popup);
		btn_ok_decline = (TextView) declineTicketDialog
				.findViewById(R.id.btn_decline_ok);
		btn_cancel_decline = (TextView) declineTicketDialog
				.findViewById(R.id.btn_decline_cancel);

		ListView list_reason_type = (ListView) declineTicketDialog
				.findViewById(R.id.list_reason_type);

		DBInteraction dbInteraction = DBInteraction
				.getInstance(MapViewTimerActivitySeekBar.this);
		// reason type id for decline
		String typeid = getResources().getString(
				R.string.ticket_action_reason_type_sla_missed);
		dbInteraction.getConnection();
		ArrayList<DeclineReasonModel> declineReasons = dbInteraction
				.getReasonList(typeid);
		dbInteraction.closeConnection();

		final DeclineReasonAdapter decline_list_adapter = new DeclineReasonAdapter(
				MapViewTimerActivitySeekBar.this,
				R.layout.row_decline_ticket_list, declineReasons,
				pd_reasons_selected_positions, list_reason_type, other_reason);

		list_reason_type.setAdapter(decline_list_adapter);

		btn_cancel_decline.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				declineTicketDialog.dismiss();

			}
		});
		btn_ok_decline.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (!decline_list_adapter.isOtherReasonOptionValid()) {
					UtilityFunction.showCenteredToast(
							MapViewTimerActivitySeekBar.this, getResources()
									.getString(R.string.fill_reason_others));
					return;
				}

				String reason_value = decline_list_adapter
						.getSelectedReasonsTitle();
				other_reason = decline_list_adapter.getOtherReasonText();

				if (reason_value != null) {
					pd_reasons_string = decline_list_adapter
							.getSelectedReasonsTitle();
					if (pd_reasons_string != null)
						ed_pd_reason.setText(pd_reasons_string);

					pd_reasons_ids = decline_list_adapter
							.getSelectedReasonsIds();
					pd_reasons_selected_positions = decline_list_adapter
							.getSelectedReasonsPositions();
					declineTicketDialog.dismiss();
				} else
					UtilityFunction.showCenteredToast(
							MapViewTimerActivitySeekBar.this, getResources()
									.getString(R.string.one_select_reason));
			}
		});

		declineTicketDialog.show();
	}

	// add estimation cost in the array list
	private void addEstimationCostDataList() {

		DBInteraction dbInteraction = DBInteraction
				.getInstance(MapViewTimerActivitySeekBar.this);
		dbInteraction.getConnection();
		ArrayList<EstimationCostModel> estimationCostList = dbInteraction
				.getEstiamtionCostList();
		dbInteraction.closeConnection();

		list_estimation_cost_data = new ArrayList<EstimationCostModel>();
		for (int i = 0; i < estimationCostList.size(); i++) {

			list_estimation_cost_data.add(estimationCostList.get(i));
		}
	}

	// open cost estimation selector
	private void openContactSelector() {

		// add estimation cost in the list

		// addEstimationCostDataList();

		final Dialog contactSelectorDialog = new Dialog(
				MapViewTimerActivitySeekBar.this, R.style.AppTheme);
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

		// contactSelectorDialog.setCancelable(false);

		// inflate pop-up view ,set layout params
		// Inflate pop-up view view,set layout parameter
		View screen_estimation_cost_selector_popup = LayoutInflater.from(
				MapViewTimerActivitySeekBar.this).inflate(
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.teramatrix.vecv.interfaces.INetworkAvailablity#isNetworkAvailable
	 * (boolean)
	 */

	@Override
	public void isNetworkAvailable(boolean isNetworkAvailable) {

		try {
			if (isNetworkAvailable) {
				img_network.setImageResource(R.drawable.wifi_enalbed);
			} else {
				img_network.setImageResource(R.drawable.wifi_disalbed);
			}
		} catch (Exception e) {
			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);

			e.printStackTrace();
		}
	}

	private void registerPushRecevier() {
		registerReceiver(pushBroadCastReceiver, new IntentFilter(
				MyFirebaseMessagingService.PUSH_RECEIVE_MESSAGE_ACTION));
	}

	// call and start sla counter timer
	private void startSLACounDownTimer() {

		if (ticket == null)
			return;

		String lastModifiedTimeInMilliSec = ticket.LastModifiedTimeInMilliSec;

		long ticket_accepted_time_in_milli = 0;
		ticket_accepted_time_in_milli = Long
				.parseLong(lastModifiedTimeInMilliSec)
				+ TimeZone.getDefault().getRawOffset();

		long current_time_in_milli = System.currentTimeMillis();

		// 2 hours
		int minutes = 120;
		if (ticket.TotalTicketLifeCycleTimeSlab != null
				&& ticket.TotalTicketLifeCycleTimeSlab.length() > 0
				&& !ticket.TotalTicketLifeCycleTimeSlab
						.equalsIgnoreCase("null")) {
			minutes = Integer.parseInt(ticket.TotalTicketLifeCycleTimeSlab);
		}

		long sla_time_millis = minutes * 60 * 1000;

		long difference_time = current_time_in_milli
				- ticket_accepted_time_in_milli;
		if (sla_time_millis - difference_time >= 0) {
			current_sla_countdown = sla_time_millis - difference_time;
			isSlaMissed = false;
		} else {
			current_sla_countdown = difference_time - sla_time_millis;
			isSlaMissed = true;
		}

		slaCountDownTimer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				if (!isSlaMissed) {
					current_sla_countdown = current_sla_countdown - 1000;
					if (current_sla_countdown <= 0)
						isSlaMissed = true;
				} else
					current_sla_countdown = current_sla_countdown + 1000;

				String formatted = TimeFormater
						.convertSecondsToHMmSs(current_sla_countdown / 1000);

				if (isSlaMissed) {
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

	// call and stop SLA counter down timer
	private void stopSLACounDownTimer() {

		if (slaCountDownTimer != null) {
			slaCountDownTimer.cancel();
			slaCountDownTimer = null;
		}
	}



	// Load google map and draw path api

	class MapLoadAsy extends AsyncTask<Void, Void, Void> {

		// Defining ProgressDialog for this class
		private ProgressDialog mProgressDialog;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute(),Method is used to perform UI
		 * operation before starting background Service
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = ProgressDialog.show(
					MapViewTimerActivitySeekBar.this, "", getResources()
							.getString(R.string.please_wait), false);
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
				SharedPreferences preference = getApplicationContext()
						.getSharedPreferences(
								ApplicationConstant.PREFERENCE_NAME,
								getApplicationContext().MODE_PRIVATE);

				// get source location from updated share preference
				source_lat = preference.getFloat(ApplicationConstant.KEY_LATT,
						0);

				source_long = preference.getFloat(ApplicationConstant.KEY_LONG,
						0);

				// get breakdown location from ticket
				String breakdown_lat = ticket.getBreackDownLatitude();
				String breakdown_lng = ticket.getBreackDownLongitude();
				destination_lat = source_lat;
				destination_lng = source_long;
				if (breakdown_lat != null && breakdown_lng != null) {
					destination_lat = Double.parseDouble(breakdown_lat);
					destination_lng = Double.parseDouble(breakdown_lng);
				}
			} catch (Exception e) {

				// Google Analytic -Tracking Exception
				EosApplication.getInstance().trackException(e);

				// save error log from file
				UtilityFunction.saveErrorLog(MapViewTimerActivitySeekBar.this,
						e);
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
			try {

				if (mProgressDialog != null && mProgressDialog.isShowing())
					mProgressDialog.dismiss();

				myLocationOnGoogleMap(destination_lat, destination_lng);
			} catch (Exception ex) {

				// Google Analytic -Tracking Exception
				EosApplication.getInstance().trackException(ex);

				UtilityFunction.saveErrorLog(MapViewTimerActivitySeekBar.this,
						ex);
			}
		}
	}

	Dialog confirmjobDialog;

	/*
	 * Confirmation Dialog shown when Van Driver reaches at break down location
	 * and presses "Van reached Button" on Map Screen at first time,
	 */
	/* User have two option on confirmation dialog (OK and Cancel). */
	public void showVanReachedConfirmDialog() {

		try {

			confirmjobDialog = new Dialog(MapViewTimerActivitySeekBar.this,
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
					MapViewTimerActivitySeekBar.this).inflate(
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

			TextView confirmation_messsage_text = (TextView) confirmjobDialog
					.findViewById(R.id.confirmation_messsage_text);
			confirmation_messsage_text
					.setText("Are you sure, VAS VAN reached at breakdown location ?");
			// initialize textview component
			TextView btn_ok = (TextView) confirmjobDialog
					.findViewById(R.id.tv_ok);
			TextView btn_cancel = (TextView) confirmjobDialog
					.findViewById(R.id.tv_cancel);

			btn_ok.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub

					confirmjobDialog.hide();

					String currentTime = UtilityFunction
							.currentUTCTimeWithoutDash();
					ticket.LastModifiedTime = currentTime;
					ticket.TicketStatus = "3";
					ticket.SuggestionComment = "Van reached at breakdown location.";
					ticket.LastModifiedBy = new VECVPreferences(
							MapViewTimerActivitySeekBar.this).getDevice_alias();
					ticket.EstimatedTimeForJobComplition = "0";

					// Van reached confirmation status before calling Van
					// Reached API
					ticket.isVanReachedConfirmed = false;

					// Not required at time of confirmation of van reached.
					// ticket.EstimatedCostForJobComplition = "";
					// Not required at time of confirmation of van reached.
					// ticket.EstimatedTimeForJobComplition = "";

					MyTicketManager myTicketManager = MyTicketManager
							.getInstance(MapViewTimerActivitySeekBar.this);
					myTicketManager.api_VanReached(ticket);

					// Stop SLA Timer as Van driver has reached at breakdown
					// location and SLA time counter is no more required.
					stopSLACounDownTimer();

					// update sla status in shared preference for this ticket id
					VECVPreferences preferences = new VECVPreferences(
							MapViewTimerActivitySeekBar.this);
					preferences.setSlaStatusForTicketId(ticket.Id, isSlaMissed);

					// tv_time_counter.setText("Work In Progress");
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
			// shown estimation dialog

			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);

			// save error log when error is created
			UtilityFunction.saveErrorLog(MapViewTimerActivitySeekBar.this, e);

		}

	}

	/*
	 * Confirmation Dialog shown when User(Van Driver) Start Trip to Breakdown location
	 */
	/* User have two option on confirmation dialog (OK and Cancel). */
	public void showTripStartConfirmDialog() {

		try {

			confirmjobDialog = new Dialog(MapViewTimerActivitySeekBar.this,
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
					MapViewTimerActivitySeekBar.this).inflate(
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

			TextView confirmation_messsage_text = (TextView) confirmjobDialog
					.findViewById(R.id.confirmation_messsage_text);
			confirmation_messsage_text
					.setText("Are you sure, Start trip to breakdown location ?");
			// initialize textview component
			TextView btn_ok = (TextView) confirmjobDialog
					.findViewById(R.id.tv_ok);
			TextView btn_cancel = (TextView) confirmjobDialog
					.findViewById(R.id.tv_cancel);

			btn_ok.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub

					confirmjobDialog.hide();

					String currentTime = UtilityFunction
							.currentUTCTimeWithoutDash();
					ticket.LastModifiedTime = currentTime;
					//Status for Trip Start ->9
					ticket.TicketStatus = "9";
					ticket.SuggestionComment = "Van started trip to breakdown location.";
					ticket.LastModifiedBy = new VECVPreferences(
							MapViewTimerActivitySeekBar.this).getDevice_alias();
					ticket.EstimatedTimeForJobComplition = "0";

					// Van reached confirmation status before calling Van
					// Reached API
					ticket.isVanReachedConfirmed = false;

					// Not required at time of confirmation of van reached.
					// ticket.EstimatedCostForJobComplition = "";
					// Not required at time of confirmation of van reached.
					// ticket.EstimatedTimeForJobComplition = "";

					MyTicketManager myTicketManager = MyTicketManager
							.getInstance(MapViewTimerActivitySeekBar.this);
					myTicketManager.api_VanReached(ticket);

					//Update Button Text from 'Trip Start' to 'Van Reached'
					btn_vanReached.setText("Van Reached");

					// Stop SLA Timer as Van driver has reached at breakdown
					// location and SLA time counter is no more required.
					//stopSLACounDownTimer();

					// update sla status in shared preference for this ticket id
					/*VECVPreferences preferences = new VECVPreferences(
							MapViewTimerActivitySeekBar.this);
					preferences.setSlaStatusForTicketId(ticket.Id, isSlaMissed);*/

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
			// shown estimation dialog

			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);

			// save error log when error is created
			UtilityFunction.saveErrorLog(MapViewTimerActivitySeekBar.this, e);

		}

	}

}
