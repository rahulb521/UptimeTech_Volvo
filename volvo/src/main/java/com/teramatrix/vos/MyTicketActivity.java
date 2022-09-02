package com.teramatrix.vos;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.teramatrix.vos.accept_reject_ticket.APIClient;
import com.teramatrix.vos.accept_reject_ticket.APIInterface;
import com.teramatrix.vos.accept_reject_ticket.responsebodyticketdetail.TicketOpen;
import com.teramatrix.vos.accept_reject_ticket.responsebodyticketdetail.TicketResponseBody;
import com.teramatrix.vos.adapter.DeclineReasonAdapter;
import com.teramatrix.vos.adapter.EstimationCostAdapter;
import com.teramatrix.vos.adapter.MyTicketListAdapter;
import com.teramatrix.vos.asynctasks.AppVersionChekerAsyn;
import com.teramatrix.vos.checkinternet.CheckInternetConnection;
import com.teramatrix.vos.firebase.service.MyFirebaseMessagingService;
import com.teramatrix.vos.interfaces.INetworkAvailablity;
import com.teramatrix.vos.model.DeclineReasonModel;
import com.teramatrix.vos.model.EstimationCostModel;
import com.teramatrix.vos.model.Ticket;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.reciver.InternetAvailabilityRecever;
import com.teramatrix.vos.service.Locationservice;
import com.teramatrix.vos.ticketmanager.MyTicketManager;
import com.teramatrix.vos.ticketmanager.NewTicketManager;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.TimeFormater;
import com.teramatrix.vos.utils.UtilityFunction;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Gaurav.Mangal
 * <p>
 * This class MyTicketActivity shown two list are displayed on it ,one
 * for Opened Tickets and one for closed Tickets.and show top right
 * corner with log options. Opened Ticket List is refresh able by
 * pulling down. Opened Ticket list gets refresh and load data from
 * local db after every event of Ticket
 * (Accept,VanReached/JobCompleted/Closed)
 */
public class MyTicketActivity extends Activity implements INetworkAvailablity, AppVersionChekerAsyn.I_AppVersionChekerAsyn {
    // define list view instance
    String TAG = this.getClass().getSimpleName();
    ListView listView_reason_type, listView_estimationcost,
            listview_filename_location;

    // define list view instance
    ListView listView_openTicket, listView_historyTicket;

    // define PullToRefreshListView instance
    PullToRefreshListView listView_openTicketPullToRefresh,
            listView_historyTicketPullToRefresh;
    // MyTicketListAdapter myTicket_list_adapter;

    // define adapter in the class instance DeclineReasonAdapter
    DeclineReasonAdapter declineReasonListAdapter;
    // define adapter in the class instance EstimationCostAdapter
    EstimationCostAdapter estimationCostListAdapter;

    // define list view instance
    List<Ticket> list_openTicket, list_closed_tickets;
    List<DeclineReasonModel> list_reason_data;
    List<EstimationCostModel> list_estimation_cost_data;

    // define adapter in the class instance MyTicketListAdapter
    MyTicketListAdapter adapter_openTicketList, adapter_historyTicketList;

    // define button component in the class
    private TextView btn_openTicket, btn_historyTicket;

    // define LinearLayout component in the class
    LinearLayout linearLayout_openTicket_title_row,
            linearLayout_history_title_row;

    // define RelativeLayout component in the class
    RelativeLayout relativeLayout_openTicket_bar,
            relativeLayout_historyticket_bar;

    // define ImageView component in the class
    ImageView img_network;

    // define TextView component in the class
    private TextView listUpdateStatusBar;

    // Defining TypeFace font component that will be used in this activity
    Typeface centuryGothic;
    Typeface verdana;

    // define dialog in the class
    Dialog declineTicketDialog, estimationCostDialog;

    // define TextView component in the class
    TextView txtView_title_decline, txtView_ticket_data_not_availabe;
    // define EditText component in the class
    EditText editText_reason_decline;

    // define boolean value in the class
    public static boolean isMyTicketActivityOpened;
    public static boolean ifNewUpdatesAvailableInDb;

    public static boolean isNewTicketPopUpShowing = false;

    // define constant click value in the class
    private int TAB_OPENED_TAB = 0;
    private int TAB_OPEN_TICKET = 1;
    private int TAB_CLOSED_TICKET = 2;
    ImageView img_splash;

    // Google client to interact with Google API
    // boolean flag to toggle periodic location updates
    /*
     * Broadcast receiver for . 1. receiving broadcast from GSMIntentservice
     * class when new Ticket is assigned 2. receiving broadcast from
     * MyTicketManager class when there is pending tickets in queue to be
     * showned .
     */

    //==============accept and reject ticket==============
    APIInterface apiInterface;
    List<TicketResponseBody> ticketResponseBody;

    private final BroadcastReceiver pushBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // get type of broadcast from intent
            String type = intent.getStringExtra("type");
            // broadcast is for showing pending tickets
            if (type != null && type.equalsIgnoreCase("pendingTicket")) {
                NewTicketManager newTicketManager = NewTicketManager
                        .getInstance(MyTicketActivity.this);
                newTicketManager
                        .receviedNewTicketPush(ApplicationConstant.IS_APP_IN_FORGROUND);
            } else if (type != null
                    && type.equalsIgnoreCase("pull_to_refresh_for_new_ticket")) {
                NewTicketManager newTicketManager = NewTicketManager
                        .getInstance(MyTicketActivity.this);
                newTicketManager
                        .pullToRefreshForNewTicketOrOpenedTicketUpdate(ApplicationConstant.IS_APP_IN_FORGROUND);
            } else {
                // broadcast is for new Ticket,check if there is any pop-up for
                // previously delivered ticket showing or not
                if (!isNewTicketPopUpShowing) {
                    // no pop-up for previous ticket is showing
                    NewTicketManager newTicketManager = NewTicketManager
                            .getInstance(MyTicketActivity.this);
                    newTicketManager
                            .receviedNewTicketPush(ApplicationConstant.IS_APP_IN_FORGROUND);
                } else {
                    // There is already popup showing for previous ticket, put
                    // this broadcast request for new ticket in pending
                    // queue.update pending tickets counter.
                    new VECVPreferences(MyTicketActivity.this)
                            .addPendingTicketsCount();
                }
            }
        }
    };

    private static MyTicketActivity myTicketActivity;

    public static MyTicketActivity getInstance() {
        return myTicketActivity;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     *
     * onCreate method Load Data from Local Db , Start DeviceTrackingService
     * ,Set AlarmManager for sending broadcastMessage to Device Location Posting
     * service.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		/*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        //change Status bar color
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.volvo_blue));
        }

        setContentView(R.layout.screen_myticket_list);
        myTicketActivity = this;

        //accept and reject ticket======================
        apiInterface = APIClient.getClient().create(APIInterface.class);
        retrofitTicketDetailApiCall();

        //====================================
        Log.e(TAG, "onCreate: " );
        // Google Analytic
        VECVPreferences preferences = new VECVPreferences(this);
        if (!preferences.getGoogleAnalyticSetupStatus()) {

            String licence_nubmer = preferences.getLicenseKey();
            String imei_number = preferences.getImeiNumber();
            String gcm_id = preferences.getGcmID();
            String login_date = TimeFormater
                    .convertMillisecondsToDateFormat(System.currentTimeMillis());
            EosApplication.getInstance().sendUserDetails(licence_nubmer,
                    imei_number, gcm_id, login_date);
            preferences.setGoogleAnalyticSetupStatus(true);
        }

        try {
            TAB_OPENED_TAB = TAB_OPEN_TICKET;
            // initialize UI elements
            setDataUi();
            //loadTicketsFromLocalDB();

        } catch (Exception e) {
            // TODO: handle exception
            // Google Analytic -Tracking Exception
            EosApplication.getInstance().trackException(e);
            // save error log in file
            UtilityFunction.saveErrorLog(MyTicketActivity.this, e);
        }
        new AppVersionChekerAsyn(this, "1001", this).execute();

        //Get Users Last Known Location
        UtilityFunction.setDefaultLocationToDelhi(this);
        // start tracking
		/*Intent mServiceIntent = new Intent(this,
				DeviceTrackingService.class);*/
        Intent mServiceIntent = new Intent(this,
                Locationservice.class);
        startService(mServiceIntent);

    }

    // initialize UI elements
    public void setDataUi() {
        // intialize view of the activity

        // OpenTickets List (Pull-To-Refresh)
        listView_openTicketPullToRefresh = (PullToRefreshListView) findViewById(R.id.ticket_list);
        setOpenTicketListView();

        listView_historyTicketPullToRefresh = (PullToRefreshListView) findViewById(R.id.ticket_list_history);
        // we don't need to load new data from server on History Ticket list
        // since this list can be refreshed from OpenTicket list pull to
        // refresh,so we have disabled this functionality here
        listView_historyTicketPullToRefresh.setMode(Mode.DISABLED);
        setHistoryTicketListView();
        // listView_historyTicket.setVisibility(View.GONE);

        list_openTicket = new ArrayList<Ticket>();
        list_closed_tickets = new ArrayList<Ticket>();
        list_reason_data = new ArrayList<DeclineReasonModel>();
        list_estimation_cost_data = new ArrayList<EstimationCostModel>();
        btn_openTicket = (TextView) findViewById(R.id.btn_myTicket);
        btn_openTicket.setText(getResources().getString(R.string.my_ticket));

        // my_ticket
        btn_historyTicket = (TextView) findViewById(R.id.btn_historyTicket);
        btn_historyTicket.setText(getResources().getString(
                R.string.history_ticket));
        // initialize TypeFace Font view component that will be used in this
        // activity
        centuryGothic = Typeface.createFromAsset(getAssets(), "gothic_0.TTF");
        verdana = Typeface.createFromAsset(getAssets(), "Verdana.ttf");

        relativeLayout_openTicket_bar = (RelativeLayout) findViewById(R.id.rl_active_myticket);
        relativeLayout_historyticket_bar = (RelativeLayout) findViewById(R.id.rl_active_historyticket);
        img_network = (ImageView) findViewById(R.id.img_network);

        listUpdateStatusBar = (TextView) findViewById(R.id.rel_loading_status_bar);
        listUpdateStatusBar.setTypeface(centuryGothic);
        listUpdateStatusBar.setVisibility(View.GONE);

        txtView_ticket_data_not_availabe = (TextView) findViewById(R.id.ticket_data_not_availabe);
        txtView_ticket_data_not_availabe.setTypeface(centuryGothic);

        // Refresh Button to Refresh Ticket data ,works similar to
        // pull-to-refresh
        View img_refresh = findViewById(R.id.img_refresh);
        img_refresh.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                listView_openTicketPullToRefresh.setRefreshing(true);
                // pullRefreshTicketDataFromServer();
            }
        });


        btn_openTicket.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                int red = getResources().getColor(R.color.volvo_blue);
                int white_grey = getResources().getColor(R.color.white);
                int button_not_select = getResources().getColor(
                        R.color.black);
                relativeLayout_openTicket_bar.setBackgroundColor(red);
                relativeLayout_historyticket_bar.setBackgroundColor(white_grey);
                btn_openTicket.setTextColor(red);
                btn_historyTicket.setTextColor(button_not_select);
                listView_openTicketPullToRefresh.setVisibility(View.VISIBLE);
                listView_historyTicketPullToRefresh.setVisibility(View.GONE);

                TAB_OPENED_TAB = TAB_OPEN_TICKET;
                showNoDataText(TAB_OPENED_TAB);
            }
        });

        btn_historyTicket.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                int red = getResources().getColor(R.color.volvo_blue);
                int white_grey = getResources().getColor(R.color.white);
                int button_not_select = getResources().getColor(
                        R.color.black);
                if (relativeLayout_openTicket_bar != null)
                    relativeLayout_openTicket_bar
                            .setBackgroundColor(white_grey);
                if (relativeLayout_historyticket_bar != null)
                    relativeLayout_historyticket_bar.setBackgroundColor(red);
                btn_openTicket.setTextColor(button_not_select);
                btn_historyTicket.setTextColor(red);
                listView_openTicketPullToRefresh.setVisibility(View.GONE);
                listView_historyTicketPullToRefresh.setVisibility(View.VISIBLE);

                TAB_OPENED_TAB = TAB_CLOSED_TICKET;
                showNoDataText(TAB_OPENED_TAB);
            }
        });


    }

    // get Opened and closed tickets from local db
    private void loadTicketsFromLocalDB() {
        // get All saved open tickets from db
        list_openTicket = MyTicketManager.getInstance(this)
                .getAllOpenedTicketsFromLocalDB();

        if (list_openTicket == null)
            list_openTicket = new ArrayList<Ticket>();

        // get All saved closed tickets from db
        list_closed_tickets = MyTicketManager.getInstance(this)
                .getAllClosedTicketsFromLocalDB();
        if (list_closed_tickets == null)
            list_closed_tickets = new ArrayList<Ticket>();

        // Shifting closed ticket for which is_trip_end -> false to Open Ticket
        // List
        // Add all closed ticket for which is_trip_end -> false to Open Ticket
        // List
        for (Ticket ticket : list_closed_tickets) {
            String is_trip_end = ticket.IsTripEnd;
            if (is_trip_end != null && is_trip_end.equalsIgnoreCase("false")) {
                list_openTicket.add(ticket);
            }
        }
        // Remove all closed ticket for which is_trip_end -> false from closed
        // ticket List
        for (int i = 0; i < list_closed_tickets.size(); i++) {
            Ticket ticket = list_closed_tickets.get(i);
            String is_trip_end = ticket.IsTripEnd;
            if (is_trip_end != null && is_trip_end.equalsIgnoreCase("false")) {
                list_closed_tickets.remove(i);
                i--;
            }
        }

        // show no data text if ticket list is empty
        showNoDataText(TAB_OPENED_TAB);
        // .getAllTicketsFromLocalDB();
        adapter_openTicketList = new MyTicketListAdapter(this,
                R.layout.row_myticket_list, list_openTicket);
        listView_openTicket.setAdapter(adapter_openTicketList);
        adapter_historyTicketList = new MyTicketListAdapter(this,
                R.layout.row_myticket_list, list_closed_tickets);
        listView_historyTicket.setAdapter(adapter_historyTicketList);
    }

    public void loadUpdatedTickets() {
        try {
            if (ifNewUpdatesAvailableInDb) {
                final ProgressDialog pd = new ProgressDialog(
                        MyTicketActivity.this);
                try {
                    pd.setMessage(getResources().getString(
                            R.string.updating_tickets));
                    pd.setCancelable(false);
                    pd.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // listUpdateStatusBar.setVisibility(View.VISIBLE);
                list_openTicket = MyTicketManager.getInstance(this)
                        .getAllOpenedTicketsFromLocalDB();
                if (list_openTicket == null)
                    list_openTicket = new ArrayList<Ticket>();
                list_closed_tickets = MyTicketManager.getInstance(this)
                        .getAllClosedTicketsFromLocalDB();
                if (list_closed_tickets == null)
                    list_closed_tickets = new ArrayList<Ticket>();
                // Shifting closed ticket for which is_trip_end -> false to Open Ticket
                // List
                // Add all closed ticket for which is_trip_end -> false to Open Ticket
                // List
                for (Ticket ticket : list_closed_tickets) {
                    String is_trip_end = ticket.IsTripEnd;
                    if (is_trip_end != null && is_trip_end.equalsIgnoreCase("false")) {
                        list_openTicket.add(ticket);
                    }
                }
                // Remove all closed ticket for which is_trip_end -> false from closed
                // ticket List
                for (int i = 0; i < list_closed_tickets.size(); i++) {
                    Ticket ticket = list_closed_tickets.get(i);
                    String is_trip_end = ticket.IsTripEnd;
                    if (is_trip_end != null && is_trip_end.equalsIgnoreCase("false")) {
                        list_closed_tickets.remove(i);
                        i--;
                    }
                }
                showNoDataText(TAB_OPENED_TAB);
                adapter_openTicketList.setNewData(list_openTicket);
                adapter_historyTicketList.setNewData(list_closed_tickets);
                ifNewUpdatesAvailableInDb = false;
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        // listUpdateStatusBar.setVisibility(View.GONE);
                        try {
                            adapter_openTicketList.notifyDataSetChanged();
                            adapter_historyTicketList.notifyDataSetChanged();
                            if (pd != null && pd.isShowing())
                                pd.dismiss();
                        } catch (Exception e) {
                            // Google Analytic -Tracking Exception
                            EosApplication.getInstance().trackException(e);
                            e.printStackTrace();
                        }
                    }
                }, 3 * 1000);
            }
        } catch (Exception e) {
            // Google Analytic -Tracking Exception
            EosApplication.getInstance().trackException(e);
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onStart()
     */

    public static int ScreenHeight;
    public static int ScreenWidth;
    public static int ScreenDpi;

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        /*
         * if (mGoogleApiClient != null) { mGoogleApiClient.connect(); }
         */
        // Google Anayltic
        GoogleAnalytics.getInstance(MyTicketActivity.this).reportActivityStart(
                this);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        // will either be DENSITY_LOW, DENSITY_MEDIUM or DENSITY_HIGH
        ScreenDpi = dm.densityDpi;
        // these will return the actual dpi horizontally and vertically
        ScreenHeight = dm.heightPixels;
        ScreenWidth = dm.widthPixels;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onResume()
     *
     * Register GCM Push broadcast receiver, loadUpdated Ticket from local db if
     * db state has changed, Check for PendingTickets.
     */
    InternetAvailabilityRecever internetAvailabilityRecever = new InternetAvailabilityRecever();

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // Push
        registerPushRecevier();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(internetAvailabilityRecever, filter);
        // Google Analytic
        EosApplication.getInstance().trackScreenView("Home");
        isMyTicketActivityOpened = true;
        ApplicationConstant.IS_APP_IN_FORGROUND = true;
        ApplicationConstant.currentActivityContext = this;
        // check internet connection
        isNetworkAvailable(new CheckInternetConnection(this)
                .isConnectedToInternet());
        // load updated tickets
        loadUpdatedTickets();
        if (!isNewTicketPopUpShowing)
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
        isMyTicketActivityOpened = false;
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
        // google analytic
        GoogleAnalytics.getInstance(MyTicketActivity.this).reportActivityStop(
                this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // MyTicketManager.getInstance(this).startLocationTrackingService(this);
    }

    public void loadTicketsOnScreen(ArrayList<Ticket> tickets) {

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

            // set image when wifi enabed
            img_network.setImageResource(R.drawable.wifi_enalbed);
        } else {

            // set image
            img_network.setImageResource(R.drawable.wifi_disalbed);
        }
    }

    // register and receive push from server
    private void registerPushRecevier() {
        registerReceiver(pushBroadCastReceiver, new IntentFilter(
                MyFirebaseMessagingService.PUSH_RECEIVE_MESSAGE_ACTION));
    }

    /**
     * @param opendTab check and set text value when no open and closed ticket is a
     */
    private void showNoDataText(int opendTab) {

        if (opendTab == TAB_OPEN_TICKET) {
            if (list_openTicket == null
                    || (list_openTicket != null && list_openTicket.size() == 0)) {

                // set visibility in txtView_ticket_data_not_availabe
                txtView_ticket_data_not_availabe.setVisibility(View.VISIBLE);

                // set text in the textview No Open Tickets
                txtView_ticket_data_not_availabe.setText(getResources()
                        .getString(R.string.no_my_tickets));
            } else {
                txtView_ticket_data_not_availabe.setVisibility(View.GONE);
            }
        }

        if (opendTab == TAB_CLOSED_TICKET) {
            if (list_closed_tickets == null
                    || (list_closed_tickets != null && list_closed_tickets
                    .size() == 0)) {

                txtView_ticket_data_not_availabe.setVisibility(View.VISIBLE);
                txtView_ticket_data_not_availabe.setText(getResources()
                        .getString(R.string.no_closed_tickets));
            } else {
                txtView_ticket_data_not_availabe.setVisibility(View.GONE);
            }
        }
    }

    /**
     * set open ticket in the list view
     */
    private void setOpenTicketListView() {

        listView_openTicketPullToRefresh
                .setOnRefreshListener(new OnRefreshListener<ListView>() {
                    @Override
                    public void onRefresh(
                            PullToRefreshBase<ListView> refreshView) {
                        String label = DateUtils.formatDateTime(
                                getApplicationContext(),
                                System.currentTimeMillis(),
                                DateUtils.FORMAT_SHOW_TIME
                                        | DateUtils.FORMAT_SHOW_DATE
                                        | DateUtils.FORMAT_ABBREV_ALL);

                        // Update the LastUpdatedLabel
                        refreshView.getLoadingLayoutProxy()
                                .setLastUpdatedLabel(label);

                        // Do work to refresh the list here.
                        pullRefreshTicketDataFromServer();
                        retrofitTicketDetailApiCall();
                    }
                });
        // Add an end-of-list listener
        listView_openTicketPullToRefresh
                .setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

                    @Override
                    public void onLastItemVisible() {
                        /*
                         * Toast.makeText(MyTicketActivity.this, "End of List!",
                         * Toast.LENGTH_SHORT).show();
                         */
                    }
                });
        listView_openTicket = listView_openTicketPullToRefresh
                .getRefreshableView();

        // Need to use the Actual ListView when registering for Context Menu
        registerForContextMenu(listView_openTicket);

        listView_openTicketPullToRefresh.setVisibility(View.VISIBLE);

    }

    // set History ticket in the listview
    private void setHistoryTicketListView() {
        listView_historyTicketPullToRefresh
                .setOnRefreshListener(new OnRefreshListener<ListView>() {
                    @Override
                    public void onRefresh(
                            PullToRefreshBase<ListView> refreshView) {
                        String label = DateUtils.formatDateTime(
                                getApplicationContext(),
                                System.currentTimeMillis(),
                                DateUtils.FORMAT_SHOW_TIME
                                        | DateUtils.FORMAT_SHOW_DATE
                                        | DateUtils.FORMAT_ABBREV_ALL);

                        // Update the LastUpdatedLabel
                        refreshView.getLoadingLayoutProxy()
                                .setLastUpdatedLabel(label);

                        // Do work to refresh the list here.
                        if (new CheckInternetConnection(MyTicketActivity.this)
                                .isConnectedToInternet()) {
                            new AsyncTask<Void, Void, Void>() {

                                @Override
                                protected Void doInBackground(Void... params) {
                                    // TODO Auto-generated method stub

                                    try {

                                        // wait for 3 sec
                                        Thread.sleep(3 * 1000);
                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block

                                        // Google Analytic -Tracking Exception
                                        EosApplication.getInstance()
                                                .trackException(e);

                                        // save error log in file
                                        UtilityFunction.saveErrorLog(
                                                MyTicketActivity.this, e);
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void result) {
                                    // TODO Auto-generated method stub
                                    super.onPostExecute(result);
                                    // Call onRefreshComplete when the list has
                                    // been
                                    // refreshed.

                                    listView_historyTicketPullToRefresh
                                            .onRefreshComplete();
                                }
                            }.execute();
                        } else {
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    stopHistoryTicketListPullRefreshing();
                                }
                            }, 1 * 1000);
                            UtilityFunction.showCenteredToast(
                                    MyTicketActivity.this,
                                    MyTicketActivity.this.getResources()
                                            .getString(R.string.data_offline));
                            listView_historyTicketPullToRefresh
                                    .onRefreshComplete();
                        }

                    }
                });
        // Add an end-of-list listener
        listView_historyTicketPullToRefresh
                .setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

                    @Override
                    public void onLastItemVisible() {
                        /*
                         * Toast.makeText(MyTicketActivity.this, "End of List!",
                         * Toast.LENGTH_SHORT).show();
                         */
                    }
                });
        listView_historyTicket = listView_historyTicketPullToRefresh
                .getRefreshableView();

        // Need to use the Actual ListView when registering for Context Menu
        registerForContextMenu(listView_historyTicket);

        listView_historyTicketPullToRefresh.setVisibility(View.GONE);

    }

    public void stopOpenTicketListPullRefreshing() {
        listView_openTicketPullToRefresh.onRefreshComplete();
    }

    public void stopHistoryTicketListPullRefreshing() {
        listView_historyTicketPullToRefresh.onRefreshComplete();
    }

    public void settingPhoneAppLanguage(String lang) {

        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    private void pullRefreshTicketDataFromServer() {
        if (new CheckInternetConnection(MyTicketActivity.this)
                .isConnectedToInternet()) {
            Intent in = new Intent(MyFirebaseMessagingService.PUSH_RECEIVE_MESSAGE_ACTION);
            in.putExtra("type", "pull_to_refresh_for_new_ticket");
            sendBroadcast(in);
        } else {
            new Handler().postDelayed(new Runnable() {
                public void run() {

                    // stop open ticket pull to refreshing
                    stopOpenTicketListPullRefreshing();
                }
            }, 1 * 1000);
            UtilityFunction.showCenteredToast(
                    MyTicketActivity.this,
                    MyTicketActivity.this.getResources().getString(
                            R.string.data_offline));
        }
    }

    @Override
    public void I_AppVersionChekerAsyn_onSuccess(String playStoreVersionName) {
        String curretnVersion = UtilityFunction.getAppVersion(MyTicketActivity.this);

        Log.e(TAG, curretnVersion+"  I_AppVersionChekerAsyn_onSuccess: playstore version "+playStoreVersionName );
        if (!curretnVersion.equalsIgnoreCase(playStoreVersionName)) {
            showAppUpgradePopUp(MyTicketActivity.this);
        }
    }

    @Override
    public void I_AppVersionChekerAsyn_onFailure(String message) {

    }
    public void showAppUpgradePopUp(Context context) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.app_version_upgrade);
        dialog.setCancelable(false);
        dialog.findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                        ("market://details?id=" + getPackageName())));
            }
        });
        dialog.show();
    }

    public void showTicketDialog(final List<TicketResponseBody> ticketResponseBodyList) {

        final Dialog dialog = new Dialog(MyTicketActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_ticket_popup);
        dialog.setCancelable(false);

        final Spinner itemsSpinner= (Spinner) dialog.findViewById(R.id.spinner_reject_commect);
        TextView txt_ticket_id_value= (TextView) dialog.findViewById(R.id.txt_ticket_id_value);
        TextView txt_contacct_value= (TextView) dialog.findViewById(R.id.txt_contacct_value);
        TextView txt_contacct_value_owner= (TextView) dialog.findViewById(R.id.txt_contacct_value_owner);
        TextView value= (TextView) dialog.findViewById(R.id.value);//location
        TextView value2= (TextView) dialog.findViewById(R.id.value2);//problem
        TextView value4= (TextView) dialog.findViewById(R.id.value4);//time
        final RelativeLayout rl_decline_reason= (RelativeLayout) dialog.findViewById(R.id.rl_decline_reason);//time
        TextView tv_decline= (TextView) dialog.findViewById(R.id.tv_decline);//time
        TextView tv_accept= (TextView) dialog.findViewById(R.id.tv_accept);//time

        tv_accept.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                acceptRejectTicketApiCall(ticketResponseBodyList.get(0).getTicketOpenList().get(0),"","12");
            }
        });

        tv_decline.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_decline_reason.setVisibility(View.VISIBLE);
                if (itemsSpinner.getSelectedItem().toString().matches("Select")){
                    Toast.makeText(MyTicketActivity.this, "Please select Decline Reason", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                acceptRejectTicketApiCall(ticketResponseBodyList.get(0).getTicketOpenList().get(0),itemsSpinner.getSelectedItem().toString(),"10");


            }
        });

        try {
            txt_ticket_id_value.setText(ticketResponseBodyList.get(0).getTicketOpenList().get(0).getTicketIdAlias());
            txt_contacct_value.setText(ticketResponseBodyList.get(0).getTicketOpenList().get(0).getCustomerContactNo());
            txt_contacct_value_owner.setText(ticketResponseBodyList.get(0).getTicketOpenList().get(0).getOwnerContactNo());
            value.setText(ticketResponseBodyList.get(0).getTicketOpenList().get(0).getBreakdownLocation());
            value2.setText(ticketResponseBodyList.get(0).getTicketOpenList().get(0).getDescription());
            value4.setText(ticketResponseBodyList.get(0).getTicketOpenList().get(0).getCreationTime());

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MyTicketActivity.this,
                    R.layout.spinner_item_layout, getResources().getStringArray(R.array.reject_ticket_reason_array));
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            itemsSpinner.setAdapter(adapter);

            if (ticketResponseBodyList.get(0).getStatus().matches("1"))
                dialog.show();
        }catch (Exception e){
            e.printStackTrace();
            loadTicketsFromLocalDB();
        }
    }

    public void retrofitTicketDetailApiCall(){

        Call<List<TicketResponseBody>> call1 = apiInterface.getDetailTicket("teramatrix",new VECVPreferences(getApplicationContext()).getDevice_alias(),
                "1970-01-01 00:00:00",new VECVPreferences(getApplicationContext()).getImeiNumber());
        call1.enqueue(new Callback<List<TicketResponseBody>>() {
            @Override
            public void onResponse(Call<List<TicketResponseBody>> call, Response<List<TicketResponseBody>> response) {

                if (response.isSuccessful()){
                    ticketResponseBody = response.body();
                    Log.e(TAG, "onResponse:1111 "+ new Gson().toJson(ticketResponseBody));
                    showTicketDialog(ticketResponseBody);
                }
            }

            @Override
            public void onFailure(Call<List<TicketResponseBody>> call, Throwable t) {
                Log.e(TAG, "onResponse onFailure:222222 "+t.getMessage() );
                call.cancel();
            }
        });
    }

    public void acceptRejectTicketApiCall(TicketOpen ticketOpen, String spinneritem, final String ticketstatus){

        final ProgressDialog pd = new ProgressDialog(
                MyTicketActivity.this);
        try {
            pd.setMessage(getResources().getString(
                    R.string.updating_tickets));
            pd.setCancelable(false);
            pd.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Call<ResponseBody> call1 = apiInterface.acceptRejectTicket("teramatrix",
                ticketOpen.getTicketId(),ticketOpen.getLastModifiedTime(),ticketOpen.getLastModifiedBy(),ticketstatus,spinneritem);
        call1.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                pd.dismiss();
                if (response.isSuccessful()){
                    Log.e(TAG, "onResponse:1111 "+ new Gson().toJson(ticketResponseBody));
                    //if (ticketstatus.matches("12")){

                    loadTicketsFromLocalDB();
                    //}
                    //loadUpdatedTickets();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onResponse onFailure:111 "+t.getMessage() );
                call.cancel();
                pd.dismiss();
            }
        });
    }

}
