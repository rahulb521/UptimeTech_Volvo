package com.teramatrix.vos.volvouptime;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.teramatrix.vos.R;
import com.teramatrix.vos.checkinternet.CheckInternetConnection;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.utils.TimeFormater;
import com.teramatrix.vos.volvouptime.adapter.VehicleAdapter;
import com.teramatrix.vos.volvouptime.asyntask.UpTimeUpdateTicket;
import com.teramatrix.vos.volvouptime.custom.DAO;
import com.teramatrix.vos.volvouptime.custom.TimePickerUtil;
import com.teramatrix.vos.volvouptime.models.UpTimeReasonsModel;
import com.teramatrix.vos.volvouptime.models.UpTimeTicketDetailModel;
import com.teramatrix.vos.volvouptime.models.VehicleModel;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by arun.singh on 6/1/2018.
 * This activity present a form to register a new ticket, Add new reasaon, Update ticket and reason.
 */
public class UpTimeRegisterActivity extends Activity implements View.OnClickListener,
        UpTimeUpdateTicket.I_UpTimeUpdateTicket,

        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        TimePickerUtil.I_TimePickerUtil
        //, TimePickerUtilEndDate.I_TimePickerUtilEndDate
{



    String TAG = this.getClass().getSimpleName();

    private RecyclerView recyclerView;
    private VehicleAdapter vehicleAdapter;

    private List<VehicleModel> vehicleModelList;
    private String type,
            registration_no,
            job_type_id,
            ticketId,
            ticketIdAlias,
            reasonId,
            reason,
            jobTypeAlias,
            reasonStartDate,
            reasonEndDate,
            door_no,
            inreasonUniqueId,
            delayedReasonComment,
            chasis_number,causalpartintent,enginehourintent,preenginehoursintent;

    private String jobEndDate = "N/A";
    private String jobStartDate = "N/A";
    private String oldestStartTime_reason;
    private String latestEndTime_reason;
    EditText edit_jobComment;
    public static String TYPE_ADD_REASON = "add_reason";
    public static String TYPE_EDIT_REASON = "edit_reason";
    public static String TYPE_JOB = "job_type";
    public static String TYPE_EDIT_JOB = "edit_job_type";
    public static String TYPE_CLOSE_JOB = "close_job";

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;

    EditText et_causalpart;
    TextView txt_causalpart;
    ArrayList<String>  reasonidlist ;

    //======================================engine hours
    EditText et_enginehours;
    TextView txt_enginehours;


    public interface Getenginehour{
        void ongetenginghour(String enginehour);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //define layout with no title with full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

        //change Status bar color
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.volvo_blue));
        }
        setContentView(R.layout.activity_register);



        //============engine hour===========================
        et_enginehours = findViewById(R.id.et_enginehour);
        txt_enginehours = findViewById(R.id.txt_enginehour);


        et_causalpart = findViewById(R.id.et_causalpart);
        txt_causalpart = findViewById(R.id.txt_causalpart);
        //Get Extra
        type = getIntent().getStringExtra("type");
        registration_no = getIntent().getStringExtra("registration_no");
        job_type_id = getIntent().getStringExtra("job_type_id");
        jobTypeAlias = getIntent().getStringExtra("jobTypeAlias");
        ticketId = getIntent().getStringExtra("ticketId");
        ticketIdAlias = getIntent().getStringExtra("ticketIdAlias");
        reason = getIntent().getStringExtra("reason");
        reasonId = getIntent().getStringExtra("reasonId");
        inreasonUniqueId = getIntent().getStringExtra("inreasonUniqueId");
        reasonStartDate = getIntent().getStringExtra("reasonStartDate");
        reasonEndDate = getIntent().getStringExtra("reasonEndDate");
        jobStartDate = getIntent().getStringExtra("jobStartDate");
        jobEndDate = getIntent().getStringExtra("jobEndDate");
        oldestStartTime_reason = getIntent().getStringExtra("oldestStartTime_reason");
        latestEndTime_reason = getIntent().getStringExtra("latestEndTime_reason");
        door_no = getIntent().getStringExtra("door_no");
        delayedReasonComment = getIntent().getStringExtra("delayedReasonComment");
        chasis_number = getIntent().getStringExtra("chasis_number");
        causalpartintent=getIntent().getStringExtra("causalpartintent");
        enginehourintent= getIntent().getStringExtra("enginehourintent");

        preenginehoursintent = getIntent().getStringExtra("preenginehoursintent");

        Log.e(TAG, preenginehoursintent+"   onCreate: getintent causal part "+type );

        //set causal part
        if (causalpartintent==null||causalpartintent.isEmpty()||causalpartintent.matches("null")){
            et_causalpart.setText("");
        }else {
            et_causalpart.setText(causalpartintent);
        }

        if (enginehourintent==null||enginehourintent.isEmpty()||enginehourintent.matches("null")){
            et_enginehours.setText("");
        }else {
            et_enginehours.setText(enginehourintent);
        }

        //txt_chasis_number
        //init screen titles and basic views
        initViews();

        final String blockCharacterSet = "$";

        InputFilter filter = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source != null && blockCharacterSet.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };
        ((EditText) findViewById(R.id.ed_comment)).setFilters(new InputFilter[]{filter});
        //Get All Job Type + Reasons List from Server
        if (!type.equalsIgnoreCase(TYPE_EDIT_REASON))
            loadDelayedReasons();


        Log.e(TAG, reasonId+" reasonid onCreate: jobtypeid  "+job_type_id );
        //new UpTimeGetReasons(this,"teramatrix",this).execute();
    }


    /**
     * initialize basic views
     */
    private void initViews() {
        //Set Title of Screen
        Log.e(TAG, "initViews: type "+type );
        if (type.equalsIgnoreCase(TYPE_JOB)) {

            ((TextView) findViewById(R.id.rl_title_bar_title)).setText("New Job");
            findViewById(R.id.spinner_container).setVisibility(View.VISIBLE);
            findViewById(R.id.reason_container).setVisibility(View.GONE);

            //Vehicle/Ticket identification details
            ((TextView) findViewById(R.id.txt_vehicle_reg_title)).setText("Vehicle Registration Number");
            ((TextView) findViewById(R.id.txt_vehicle_reg_no)).setText("" + registration_no);



            //lable of first Field
            TextView textView = findViewById(R.id.tv_type_title);
            textView.setText("Job Type");
//            edit_jobComment=findViewById(R.id.edit_jobComment);
//            edit_jobComment.setText();
            String startTime_UTC = TimeFormater.convertMillisecondsToDateFormat(System.currentTimeMillis(), "dd MMM yyyy HH:mm");
            String endTime_UTC = TimeFormater.convertMillisecondsToDateFormat(System.currentTimeMillis() + (1 * 60 * 60 * 1000), "dd MMM yyyy HH:mm");

            ((TextView) findViewById(R.id.txt_startDate)).setText(startTime_UTC);
            ((TextView) findViewById(R.id.txt_endDate)).setText("");
            //findViewById(R.id.end_date_container).setVisibility(View.GONE);

            //Make visible -> Comment Box
            findViewById(R.id.comment_box_contatiner).setVisibility(View.VISIBLE);

            //Positive Button text
            TextView update = findViewById(R.id.update);
            update.setText("SUBMIT");

        } else if (type.equalsIgnoreCase(TYPE_ADD_REASON)) {

            et_enginehours.setVisibility(View.GONE);
            txt_enginehours.setVisibility(View.GONE);


            et_causalpart.setVisibility(View.INVISIBLE);
            txt_causalpart.setVisibility(View.INVISIBLE);

            ((TextView) findViewById(R.id.txt_comment_header)).setText("Delayed Reason Comments");
            ((EditText) findViewById(R.id.ed_comment)).setHint("Enter Delayed Reason Comments");

            ((TextView) findViewById(R.id.rl_title_bar_title)).setText("Add Delay / Sub Activity");
            findViewById(R.id.spinner_container).setVisibility(View.VISIBLE);
            findViewById(R.id.reason_container).setVisibility(View.GONE);

            //Vehicle/Ticket identification details
            ((TextView) findViewById(R.id.txt_vehicle_reg_title)).setText("Job Id");
            ((TextView) findViewById(R.id.txt_vehicle_reg_no)).setText("" + ticketIdAlias);

            //lable of first Field
            TextView textView = findViewById(R.id.tv_type_title);
            textView.setText("Type of Delay / Sub Activity");

            //Positive Button text
            TextView update = findViewById(R.id.update);
            update.setText("SUBMIT");

            Log.e(TAG, "initViews: jot start date "+jobStartDate );

            ((TextView) findViewById(R.id.txt_startDate)).setText(jobStartDate);
            ((TextView) findViewById(R.id.txt_endDate)).setText("");

            //Make visible -> Comment Box
            findViewById(R.id.comment_box_contatiner).setVisibility(View.VISIBLE);

        } else if (type.equalsIgnoreCase(TYPE_EDIT_REASON)) {

            et_enginehours.setVisibility(View.GONE);
            txt_enginehours.setVisibility(View.GONE);


            if (reason!=null){

                if (reason.matches("Volvo - Parts not available")
                        ||reason.matches("Customer - Parts")
                        ||reason.matches("Vendor Parts")){

                    et_causalpart.setVisibility(View.VISIBLE);
                    txt_causalpart.setVisibility(View.VISIBLE);
                    et_causalpart.setEnabled(true);

                }else {
                    et_causalpart.setVisibility(View.INVISIBLE);
                    txt_causalpart.setVisibility(View.INVISIBLE);
                    et_causalpart.setEnabled(false);

                }
            }

            ((TextView) findViewById(R.id.txt_comment_header)).setText("Delayed Reason Comments");
            ((EditText) findViewById(R.id.ed_comment)).setHint("Enter Delayed Reason Comments");

            ((TextView) findViewById(R.id.rl_title_bar_title)).setText("Edit Delayed Reason");
            findViewById(R.id.spinner_container).setVisibility(View.GONE);
            findViewById(R.id.reason_container).setVisibility(View.VISIBLE);

            //Vehicle/Ticket identification details
            ((TextView) findViewById(R.id.txt_vehicle_reg_title)).setText("Job Id");
            ((TextView) findViewById(R.id.txt_vehicle_reg_no)).setText("" + ticketIdAlias);

            ((TextView) findViewById(R.id.txt_job_type)).setText(jobTypeAlias);
            ((TextView) findViewById(R.id.txt_reason)).setText(reason);




            ((TextView) findViewById(R.id.txt_startDate)).setText(TimeFormater.convertDateFormate(reasonStartDate, "dd MMM yyyy HH:mm:ss", "dd MMM yyyy HH:mm"));

            //SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
            //String currentDateandTime = sdf.format(new Date());
            //((TextView) findViewById(R.id.txt_endDate)).setText(currentDateandTime);


            if (reasonEndDate == null || reasonEndDate.isEmpty() || reasonEndDate.contains("01 Jan 0001"))
                ((TextView) findViewById(R.id.txt_endDate)).setText("");
            else
                ((TextView) findViewById(R.id.txt_endDate)).setText(TimeFormater.convertDateFormate(reasonEndDate, "dd MMM yyyy HH:mm:ss", "dd MMM yyyy HH:mm"));

            ((EditText) findViewById(R.id.ed_comment)).setText(delayedReasonComment);

            //Positive Button text
            TextView update = findViewById(R.id.update);
            update.setText("UPDATE");

            //Make visible -> Comment Box
            findViewById(R.id.comment_box_contatiner).setVisibility(View.VISIBLE);

        } else if (type.equalsIgnoreCase(TYPE_EDIT_JOB)) {
            et_causalpart.setEnabled(false);

           // et_enginehours.setEnabled(false);

            ((TextView) findViewById(R.id.rl_title_bar_title)).setText("Edit Job");
            findViewById(R.id.spinner_container).setVisibility(View.GONE);
            findViewById(R.id.reason_container).setVisibility(View.VISIBLE);

            //Vehicle/Ticket identification details
            ((TextView) findViewById(R.id.txt_vehicle_reg_title)).setText("Job Id");
            ((TextView) findViewById(R.id.txt_vehicle_reg_no)).setText("" + ticketIdAlias);

            ((TextView) findViewById(R.id.txt_job_type)).setText("Job Type");
            ((TextView) findViewById(R.id.txt_reason)).setText(jobTypeAlias);

            ((TextView) findViewById(R.id.txt_startDate)).setText(TimeFormater.convertDateFormate(reasonStartDate, "dd MMM yyyy HH:mm:ss", "dd MMM yyyy HH:mm"));

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());
            ((TextView) findViewById(R.id.txt_endDate)).setText(currentDateandTime);

            /* if (reasonEndDate == null || reasonEndDate.isEmpty() || reasonEndDate.contains("01 Jan 0001"))
                ((TextView) findViewById(R.id.txt_endDate)).setText("");
            else
                ((TextView) findViewById(R.id.txt_endDate)).setText(TimeFormater.convertDateFormate(reasonEndDate, "dd MMM yyyy HH:mm:ss", "dd MMM yyyy HH:mm"));
*/
            //Positive Button text
            TextView update = findViewById(R.id.update);
            update.setText("UPDATE");
            ((EditText) findViewById(R.id.ed_comment)).setText(delayedReasonComment);

            findViewById(R.id.comment_box_contatiner).setVisibility(View.VISIBLE);
        }

        //Init View Click Listeners
        findViewById(R.id.txt_startDate).setOnClickListener(this);
        findViewById(R.id.txt_endDate).setOnClickListener(this);
        findViewById(R.id.update).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        //Door Number
        ((TextView) findViewById(R.id.txt_door_no)).setText("" + door_no);
        ((TextView) findViewById(R.id.txt_chasis_number)).setText("" + chasis_number);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.update: {

                Log.e(TAG, type+" onClick: update "+et_causalpart.isEnabled() );
//                if (type.equalsIgnoreCase(TYPE_JOB)) {
//
//                    if (et_causalpart.isEnabled() == true) {
//                        if (et_causalpart.getText().toString().isEmpty()) {
//                            Toast.makeText(this, "Please add causal part", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                    }
//                }


                String licenceKey = new VECVPreferences(UpTimeRegisterActivity.this).getLicenseKey();
                String startTime_UTC = ((TextView) findViewById(R.id.txt_startDate)).getText().toString();
                String endTime_UTC = ((TextView) findViewById(R.id.txt_endDate)).getText().toString();

                if (endTime_UTC != null || endTime_UTC.length() > 0) {
                    int result = TimeFormater.compareDateString(startTime_UTC, endTime_UTC, "dd MMM yyyy HH:mm");

                    if (result > 0) {
                        //Invalid Date selection, Start Date is greater than End Date.
                        Toast.makeText(UpTimeRegisterActivity.this, "End Date should not be older than Start Date.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }


                UpTimeUpdateTicket.RequestModel requestModel = new UpTimeUpdateTicket.RequestModel();
                requestModel.TicketId = ticketId;
                requestModel.licenceNo = licenceKey;
                requestModel.startTime = startTime_UTC;
                requestModel.endTime = endTime_UTC;
                requestModel.vehicleRegNo = registration_no;
                requestModel.token = "teramatrix";
                requestModel.requestType = type;
                requestModel.causalpart = et_causalpart.getText().toString();
                requestModel.enginehour = et_enginehours.getText().toString();

                if (type.equalsIgnoreCase(TYPE_JOB)) {

                    //Validate Description before sumbitting
                    EditText editText = (EditText) findViewById(R.id.ed_comment);
                    String comment = editText.getText().toString();
                    if (comment == null || comment.isEmpty()) {
                        Toast.makeText(UpTimeRegisterActivity.this, "Enter a valid description of delayed reason.", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    if (true || new CheckInternetConnection(UpTimeRegisterActivity.this).isConnectedToInternet()) {
                        String jobType = ((Spinner) findViewById(R.id.job_spinner)).getSelectedItem().toString();
                        String jobSequenceNo = (((Spinner) findViewById(R.id.job_spinner)).getSelectedItemPosition()) + "";
                        jobSequenceNo = reasonidlist.get(Integer.parseInt(jobSequenceNo));
                        if (jobType.equalsIgnoreCase("select")) {
                            Toast.makeText(UpTimeRegisterActivity.this, "Please select job type.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (preenginehoursintent==null){
                            preenginehoursintent="0";
                        }else if (preenginehoursintent.isEmpty()){
                            preenginehoursintent="0";
                        }else if (preenginehoursintent.matches("null")){
                            preenginehoursintent = "0";
                        }

                        Log.e(TAG, preenginehoursintent+" onClick: eeeeeeeeee "+et_enginehours.getText().toString() );
                        if (et_enginehours.getText().toString().isEmpty()){
                            Toast.makeText(UpTimeRegisterActivity.this, "Please Add Engine Hours", Toast.LENGTH_SHORT).show();

                            return;
                        }else if (Long.parseLong(et_enginehours.getText().toString())<Long.parseLong(preenginehoursintent)){
                            Log.e(TAG, "onClick: greater "+preenginehoursintent );
                            Toast.makeText(UpTimeRegisterActivity.this, "Please enter value greater than or equal to "+preenginehoursintent, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        requestModel.description = jobType;
                        requestModel.reasonId = jobSequenceNo;
                        requestModel.isTicketClosed = "false";
                        requestModel.delayedReasonComment = comment;
                        requestModel.causalpart = et_causalpart.getText().toString();
                        requestModel.enginehour=et_enginehours.getText().toString();

                        new UpTimeUpdateTicket(
                                UpTimeRegisterActivity.this,
                                requestModel, UpTimeRegisterActivity.this).execute();

                    } else {
                        Toast.makeText(UpTimeRegisterActivity.this, "Can not create new ticket in offline mode.", Toast.LENGTH_SHORT).show();
                    }
                } else if (type.equalsIgnoreCase(TYPE_ADD_REASON)) {
                    String jobTypesp = ((Spinner) findViewById(R.id.job_spinner)).getSelectedItem().toString();

                    if (jobTypesp.matches("Volvo - Parts not available")
                            ||jobTypesp.matches("Customer - Parts")
                            ||jobTypesp.matches("Vendor Parts")) {

                        if (et_causalpart.getText().toString().isEmpty()) {
                            Toast.makeText(this, "Please add causal part", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    //Add New Reason to current Ticket

                    //Validate Description before sumbitting
                    EditText editText = (EditText) findViewById(R.id.ed_comment);
                    String comment = editText.getText().toString();
                    if (comment == null || comment.isEmpty()) {
                        Toast.makeText(UpTimeRegisterActivity.this, "Enter a valid description of delayed reason.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String jobType = ((Spinner) findViewById(R.id.job_spinner)).getSelectedItem().toString();
                    String jobSequenceNo = (((Spinner) findViewById(R.id.job_spinner)).getSelectedItemPosition()) + "";
                    String selectedjobtypeid = reasonidlist.get(Integer.parseInt(jobSequenceNo));

                    if (jobType.equalsIgnoreCase("select")) {
                        Toast.makeText(UpTimeRegisterActivity.this, "Please select delay type.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int delayedReasonId = 0;
//                    if (job_type_id != null) {
//                        delayedReasonId = Integer.parseInt(job_type_id);
//                        delayedReasonId = 100 * delayedReasonId;
//                        delayedReasonId = delayedReasonId + Integer.parseInt(jobSequenceNo);
//                    }


                    //new code for getting reasonid
                    delayedReasonId = Integer.parseInt(reasonidlist.get(Integer.parseInt(jobSequenceNo)));

                    requestModel.DelayedReasonId = delayedReasonId + "";
                    requestModel.description = jobType;
                    requestModel.reasonId = selectedjobtypeid;
                    requestModel.isTicketClosed = "false";
                    requestModel.InreasonUniqueId = licenceKey + "" + System.currentTimeMillis();

                    requestModel.delayedReasonComment = comment;
                    requestModel.causalpart = et_causalpart.getText().toString();
                    requestModel.enginehour= et_enginehours.getText().toString();
                   // requestModel.endTime = "";

                    new UpTimeUpdateTicket(UpTimeRegisterActivity.this, requestModel, UpTimeRegisterActivity.this).execute();

                } else if (type.equalsIgnoreCase(TYPE_EDIT_REASON)) {
                    //Update Current Reason

                    //String jobTypesp1 = ((Spinner) findViewById(R.id.job_spinner)).getSelectedItem().toString();

                    Log.e(TAG, "onClick: gettext edit dela reason"+reason );
                    if (reason.matches("Volvo - Parts not available")
                            ||reason.matches("Customer - Parts")
                            ||reason.matches("Vendor Parts")) {

                        if (et_causalpart.getText().toString().isEmpty()) {
                            Toast.makeText(this, "Please add causal part", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }


                    //Validate Description before sumbitting
                    EditText editText = (EditText) findViewById(R.id.ed_comment);
                    String comment = editText.getText().toString();
                    if (comment == null || comment.isEmpty()) {
                        Toast.makeText(UpTimeRegisterActivity.this, "Enter a valid description of delayed reason.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    requestModel.DelayedReasonId = reasonId;
                    requestModel.isTicketClosed = "false";
                    requestModel.InreasonUniqueId = inreasonUniqueId;
                    requestModel.delayedReasonComment = comment;
                    requestModel.causalpart = et_causalpart.getText().toString();
                    requestModel.enginehour=et_enginehours.getText().toString();
                    //requestModel.endTime = "";

                    new UpTimeUpdateTicket(
                            UpTimeRegisterActivity.this,
                            requestModel,
                            UpTimeRegisterActivity.this).execute();

                } else if (type.equalsIgnoreCase(TYPE_EDIT_JOB)) {
                    //Update Current Job
                    EditText editText = (EditText) findViewById(R.id.ed_comment);
                    String comment = editText.getText().toString();
                    if (comment == null || comment.isEmpty()) {
                        Toast.makeText(UpTimeRegisterActivity.this, "Enter a valid description of delayed reason.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    requestModel.DelayedReasonId = reasonId;
                    requestModel.isTicketClosed = "false";
                    requestModel.delayedReasonComment = comment;
                    requestModel.causalpart = et_causalpart.getText().toString();
                    requestModel.enginehour= et_enginehours.getText().toString();
                    //requestModel.endTime="";


                    if (et_enginehours.getText().toString().isEmpty()){
                        Toast.makeText(UpTimeRegisterActivity.this, "Please Add Engine Hours", Toast.LENGTH_SHORT).show();

                        return;
                    }else if (Long.parseLong(et_enginehours.getText().toString())<Long.parseLong(preenginehoursintent)){
                        Log.e(TAG, "onClick: greater "+preenginehoursintent );
                        Toast.makeText(UpTimeRegisterActivity.this, "Please enter value greater than or equal to "+preenginehoursintent, Toast.LENGTH_SHORT).show();
                        return;
                    }


                    new UpTimeUpdateTicket(
                            UpTimeRegisterActivity.this,
                            requestModel,
                            UpTimeRegisterActivity.this).execute();


                } else {
                    Toast.makeText(UpTimeRegisterActivity.this, "Please select Start Date.", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case R.id.txt_startDate: {

                if (type.equalsIgnoreCase(TYPE_ADD_REASON)) {
                    Log.e(TAG, "onClick: add reason" );
                    String timeRange_Min = jobStartDate;
                    //String timeRange_Min = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", -1, 0);
                    //String timeRange_Max = ((TextView) findViewById(R.id.txt_endDate)).getText().toString();

                    Log.e(TAG, jobStartDate+" onClick: add min  111 "+timeRange_Min );

//                    if (timeRange_Max.isEmpty())
//                        timeRange_Max = jobEndDate;
//
//                    if (timeRange_Max.equalsIgnoreCase("N/A"))
//                        timeRange_Max = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", 0, 1);
//
                    String timeRange_Max = null;

                    if (timeRange_Max == null || timeRange_Max.equalsIgnoreCase("N/A"))
                        timeRange_Max = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", 0, 1);

                    Log.e(TAG, "onClick: maxdate "+timeRange_Max );
                    //If timeRange_Max is greater than current time than assign (timeRange_Max <- current time)
                    String currentTime = TimeFormater.convertMillisecondsToDateFormat(System.currentTimeMillis(), "dd MMM yyyy HH:mm");
                    int result = TimeFormater.compareDateString(currentTime, timeRange_Max, "dd MMM yyyy HH:mm");
                    if (result <= 0)
                        timeRange_Max = currentTime;


                    Log.e(TAG, "onClick: result "+result );
                    // String defaultTime = timeRange_Max;
                    String defaultTime = ((TextView) findViewById(R.id.txt_startDate)).getText().toString();

                    TimePickerUtil timePickerUtil = new TimePickerUtil();
                    timePickerUtil.initTimePicker(UpTimeRegisterActivity.this, timeRange_Min, timeRange_Max, defaultTime, UpTimeRegisterActivity.this, R.id.txt_startDate);

                } else if (type.equalsIgnoreCase(TYPE_EDIT_REASON)) {
                    String timeRange_Min = jobStartDate;
                    String timeRange_Max = ((TextView) findViewById(R.id.txt_endDate)).getText().toString();

                    if (timeRange_Max.isEmpty())
                        timeRange_Max = jobEndDate;

                    if (timeRange_Max.equalsIgnoreCase("N/A"))
                        timeRange_Max = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", 0, 1);

                    //If timeRange_Max is greater than current time than assign (timeRange_Max <- current time)
                    String currentTime = TimeFormater.convertMillisecondsToDateFormat(System.currentTimeMillis(), "dd MMM yyyy HH:mm");
                    int result = TimeFormater.compareDateString(currentTime, timeRange_Max, "dd MMM yyyy HH:mm");
                    if (result <= 0)
                        timeRange_Max = currentTime;


                    String defaultTime = ((TextView) findViewById(R.id.txt_startDate)).getText().toString();
                    //  TimePickerUtil timePickerUtil = new TimePickerUtil();
                    //   timePickerUtil.initTimePicker(UpTimeRegisterActivity.this, timeRange_Min, timeRange_Max, defaultTime, UpTimeRegisterActivity.this, R.id.txt_startDate);

                } else if (type.equalsIgnoreCase(TYPE_JOB)) {
                    String timeRange_Min = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", -2, 0);
                    // String timeRange_Min = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", -7, 0);

                    //String timeRange_Max = ((TextView)findViewById(R.id.txt_endDate)).getText().toString();
                    String timeRange_Max = TimeFormater.convertMillisecondsToDateFormat(System.currentTimeMillis(), "dd MMM yyyy HH:mm");

                    if (timeRange_Max.isEmpty())
                        timeRange_Max = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", 0, 1);

                    String defaultTime = ((TextView) findViewById(R.id.txt_startDate)).getText().toString();

                    TimePickerUtil timePickerUtil = new TimePickerUtil();
                    timePickerUtil.initTimePicker(UpTimeRegisterActivity.this, timeRange_Min, timeRange_Max, defaultTime, UpTimeRegisterActivity.this, R.id.txt_startDate);

                } else if (type.equalsIgnoreCase(TYPE_EDIT_JOB)) {


                    String timeRange_Min = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", -7, 0);

                    String timeRange_Max = oldestStartTime_reason;

                    if (timeRange_Max == null || timeRange_Max.isEmpty() || timeRange_Max.equalsIgnoreCase("N/A"))
                        timeRange_Max = ((TextView) findViewById(R.id.txt_endDate)).getText().toString();

                    if (timeRange_Max.isEmpty())
                        timeRange_Max = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", 0, 1);
                    //timeRange_Max = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", 0, 1);

                    //If timeRange_Max is greater than current time than assign (timeRange_Max <- current time)
                    String currentTime = TimeFormater.convertMillisecondsToDateFormat(System.currentTimeMillis(), "dd MMM yyyy HH:mm");
                    int result = TimeFormater.compareDateString(currentTime, timeRange_Max, "dd MMM yyyy HH:mm");
                    if (result <= 0)
                        timeRange_Max = currentTime;

                    String defaultTime = ((TextView) findViewById(R.id.txt_startDate)).getText().toString();

                    //  TimePickerUtil timePickerUtil = new TimePickerUtil();
                    //timePickerUtil.initTimePicker(UpTimeRegisterActivity.this, timeRange_Min, timeRange_Max, defaultTime, UpTimeRegisterActivity.this, R.id.txt_startDate);
                }

                //initDateTimePicker(dateString,"txt_startDate");

            }
            break;
            case R.id.txt_endDate: {

                Log.e(TAG, "onClick: type "+type );
                if (type.equalsIgnoreCase(TYPE_ADD_REASON)) {
                    String timeRange_Min = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", -1, 0);
                    //String timeRange_Min  = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", -1, 0);
                    //   String timeRange_Min = ((TextView) findViewById(R.id.txt_startDate)).getText().toString();
//                    String timeRange_Max = jobEndDate;
//                    if (timeRange_Max.equalsIgnoreCase("N/A"))
//                        timeRange_Max = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", 0, 1);
                    String timeRange_Max = null;

                    if (timeRange_Max == null || timeRange_Max.equalsIgnoreCase("N/A"))

                       timeRange_Max = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", 0, 1);
                        //timeRange_Max = TimePickerUtil.getTimeOffsetNew(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm",  1);



                    //If timeRange_Max is greater than current time than assign (timeRange_Max <- current time)
                    String currentTime = TimeFormater.convertMillisecondsToDateFormat(System.currentTimeMillis(), "dd MMM yyyy HH:mm");
                    int result = TimeFormater.compareDateString(currentTime, timeRange_Max, "dd MMM yyyy HH:mm");
                    if (result <= 0)
                        timeRange_Max = currentTime;


                    String defaultTime = ((TextView) findViewById(R.id.txt_endDate)).getText().toString();
                    if (defaultTime.isEmpty())
                        defaultTime = timeRange_Min;

                    //TimePickerUtil timePickerUtil = new TimePickerUtil();
                    //timePickerUtil.initTimePicker(UpTimeRegisterActivity.this, timeRange_Min, timeRange_Max, timeRange_Max, UpTimeRegisterActivity.this, R.id.txt_endDate);
                    // timePickerUtil.initTimePicker(UpTimeRegisterActivity.this, timeRange_Min, timeRange_Max, defaultTime, UpTimeRegisterActivity.this, R.id.txt_endDate);

                    TimePickerUtil timePickerUtil = new TimePickerUtil();
                    timePickerUtil.initTimePicker(UpTimeRegisterActivity.this, timeRange_Min, timeRange_Max, timeRange_Max, UpTimeRegisterActivity.this, R.id.txt_endDate);


                } else if (type.equalsIgnoreCase(TYPE_EDIT_JOB)) {
                    // String timeRange_Min = latestEndTime_reason;
                    String timeRange_Min = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", -1, 0);
                    //String timeRange_Min = TimePickerUtil.getTimeOffsetNew(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm",  0);



                    if (timeRange_Min == null || timeRange_Min.isEmpty() || timeRange_Min.equalsIgnoreCase("N/A"))
                        timeRange_Min = ((TextView) findViewById(R.id.txt_startDate)).getText().toString();

                    //String timeRange_Max = jobEndDate;
                    String timeRange_Max = null;

                    if (timeRange_Max == null || timeRange_Max.equalsIgnoreCase("N/A"))
                        // timeRange_Max = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", -2, 0);
                        timeRange_Max = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", 0, 1);

                    //If timeRange_Max is greater than current time than assign (timeRange_Max <- current time)
                    String currentTime = TimeFormater.convertMillisecondsToDateFormat(System.currentTimeMillis(), "dd MMM yyyy HH:mm");
                    int result = TimeFormater.compareDateString(currentTime, timeRange_Max, "dd MMM yyyy HH:mm");
                    if (result <= 0)
                        timeRange_Max = currentTime;

                    String defaultTime = ((TextView) findViewById(R.id.txt_endDate)).getText().toString();
                    if (defaultTime.isEmpty())
                        defaultTime = timeRange_Min;

                    TimePickerUtil timePickerUtil = new TimePickerUtil();
                    timePickerUtil.initTimePicker(UpTimeRegisterActivity.this, timeRange_Min, timeRange_Max, defaultTime, UpTimeRegisterActivity.this, R.id.txt_endDate);
                } else {
                    // String timeRange_Min = ((TextView) findViewById(R.id.txt_startDate)).getText().toString();
                    //  String timeRange_Max = jobEndDate;
                    String timeRange_Min = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", -1, 0);
                    //String timeRange_Min = TimePickerUtil.getTimeOffsetNew(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm",  0);

                    String timeRange_Max = null;

                    if (timeRange_Max == null || timeRange_Max.equalsIgnoreCase("N/A"))
                        // timeRange_Max = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", -2, 0);
                        timeRange_Max = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", 0, 1);

                    if (timeRange_Max == null || timeRange_Max.equalsIgnoreCase("N/A"))
                        timeRange_Max = TimePickerUtil.getTimeOffset(UpTimeRegisterActivity.this, "dd MMM yyyy HH:mm", 0, 1);

                    //If timeRange_Max is greater than current time than assign (timeRange_Max <- current time)
                    String currentTime = TimeFormater.convertMillisecondsToDateFormat(System.currentTimeMillis(), "dd MMM yyyy HH:mm");
                    int result = TimeFormater.compareDateString(currentTime, timeRange_Max, "dd MMM yyyy HH:mm");
                    if (result <= 0)
                        timeRange_Max = currentTime;


                    String defaultTime = ((TextView) findViewById(R.id.txt_endDate)).getText().toString();
                    if (defaultTime.isEmpty())
                        defaultTime = timeRange_Min;

                    TimePickerUtil timePickerUtil = new TimePickerUtil();
                    timePickerUtil.initTimePicker(UpTimeRegisterActivity.this, timeRange_Min, timeRange_Max, defaultTime, UpTimeRegisterActivity.this, R.id.txt_endDate);
                }

                //String dateString = ((TextView)findViewById(R.id.txt_endDate)).getText().toString();
                //initDateTimePicker(dateString,"txt_endDate");

            }
            break;
            case R.id.cancel: {
                //Close screen
                finish();
            }
            break;

        }
    }


    /*
     * Load All delayed Reasons in Spinner
     * */
    private void loadDelayedReasons() {
        List<UpTimeReasonsModel> upTimeReasonsModels = DAO.getAllReasons();


        Log.e(TAG, "loadDelayedReasons: 111111  "+upTimeReasonsModels.size());

        for (int i = 0; i < upTimeReasonsModels.size(); i++) {
            Log.e(TAG, upTimeReasonsModels.get(i).get_service_type_id()+"  loadDelayedReasons: listitmes  "+upTimeReasonsModels.get(i).get_service_name() );
        }

        if (upTimeReasonsModels.size() == 0)
            return;

        ArrayList<String> strings = new ArrayList<>();
        reasonidlist = new ArrayList<>();
        strings.add("Select");
        reasonidlist.add("0");
        if (type.equalsIgnoreCase(TYPE_JOB)) {
            //New Ticket Screen -> Load Spinner with Job Types records

            for (UpTimeReasonsModel upTimeReasonsModel : upTimeReasonsModels) {
                if (strings.size() == 0) {
                    strings.add(upTimeReasonsModel.get_service_name());
                    reasonidlist.add(upTimeReasonsModel.get_service_type_id());
                } else {
                    if (!strings.get(strings.size() - 1).equalsIgnoreCase(upTimeReasonsModel.get_service_name())) {
                        strings.add(upTimeReasonsModel.get_service_name());
                        reasonidlist.add(upTimeReasonsModel.get_service_type_id());
                    }

                }
            }

        } else if (type.equalsIgnoreCase(TYPE_ADD_REASON)) {
            //Add New Reason Screen -> Load Spinner with Reasons Records of particular Job Type of current Ticket

            for (UpTimeReasonsModel upTimeReasonsModel : upTimeReasonsModels) {

                Log.e(TAG, upTimeReasonsModel.get_service_type_id()+" loadDelayedReasons: jobtypeid "+job_type_id );

                if (upTimeReasonsModel.get_service_type_id().equalsIgnoreCase(job_type_id)) {
                    strings.add(upTimeReasonsModel.get_reason_alias());
                    reasonidlist.add(upTimeReasonsModel.get_reason_id());
                }

            }
        }


        Log.e(TAG, reasonidlist+" loadDelayedReasons: jobtype array "+strings );

        final Spinner spinner = (Spinner) findViewById(R.id.job_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(UpTimeRegisterActivity.this,
                R.layout.spinner_item_layout, strings);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        if (type.equalsIgnoreCase(TYPE_ADD_REASON)){
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.e(TAG, spinner.getSelectedItem().toString()+" onItemSelected: "+i );

                    if (spinner.getSelectedItem().toString().matches("Volvo - Parts not available")
                    ||spinner.getSelectedItem().toString().matches("Customer - Parts")
                    ||spinner.getSelectedItem().toString().matches("Vendor Parts")){

                        et_causalpart.setVisibility(View.VISIBLE);
                        txt_causalpart.setVisibility(View.VISIBLE);
                        et_causalpart.setEnabled(true);

                    }else {
                        et_causalpart.setVisibility(View.INVISIBLE);
                        txt_causalpart.setVisibility(View.INVISIBLE);
                        et_causalpart.setEnabled(false);

                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
    }

    @Override
    public void onTicketupdateResponse(boolean isUpdateSuccessful, String msg) {

        Log.e(TAG, "onTicketupdateResponse: engine hour "+et_enginehours.getText().toString() );
        if (isUpdateSuccessful) {


            //save engine hour to database=======================
            try {
                UpTimeTicketDetailModel upTimeTicketDetailModel = new Select()
                        .from(UpTimeTicketDetailModel.class)
                        .where("TicketId = ?", ticketId)
                        .executeSingle();

                Log.e(TAG, "doInBackground: 333333333");

                upTimeTicketDetailModel.setEnginehours(et_enginehours.getText().toString());
                upTimeTicketDetailModel.setCausalPart(et_causalpart.getText().toString());
                upTimeTicketDetailModel.save();
            }catch (Exception e){

            }



            Intent intent1 = new Intent();
            intent1.putExtra("enginehours", et_enginehours.getText().toString());

            setResult(RESULT_OK,intent1);
            //getenginehourreference.ongetenginghour(et_enginehours.getText().toString());

//            Intent in = new Intent("com.teramatrix.enginehour");
//            in.putExtra("enginehour",et_enginehours.getText().toString());
//            sendBroadcast(in);

            finish();
        } else {
            //TODO show dialog to user
            EngineHourReadingDialog engineHourReadingDialog = new EngineHourReadingDialog(this, msg);
            engineHourReadingDialog.show();
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {


        String date = dayOfMonth + " " + (monthOfYear + 1) + " " + year;
        String curretnFormat = "dd MM yyyy";
        String newFormat = "dd MMM yyyy";
        date = TimeFormater.convertDateFormate(date, curretnFormat, newFormat);
        date = view.getTag() + "," + date;
        timePickerDialog.show(getFragmentManager(), date);
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {

        String date = view.getTag();
        String[] s = date.split(",");

        String hr = "";
        String min = "";

        if (hourOfDay < 10)
            hr = 0 + "" + hourOfDay;
        else
            hr = hourOfDay + "";

        if (minute < 10)
            min = 0 + "" + minute;
        else
            min = minute + "";

        date = s[1] + " " + hr + ":" + min;

        if (s[0].equalsIgnoreCase("txt_startDate")) {
            ((TextView) findViewById(R.id.txt_startDate)).setText(date);
        } else if (s[0].equalsIgnoreCase("txt_endDate")) {
            ((TextView) findViewById(R.id.txt_endDate)).setText(date);
        }
        //Log.i("onTimeSet",date);
    }

    @Override
    public void onTimePickerUtilResult(String resultTime, int resId) {
        ((TextView) findViewById(resId)).setText(resultTime);
    }

//    @Override
//    public void onTimePickerUtilResultEndDate(String resultTime, int resId) {
//        ((TextView) findViewById(resId)).setText(resultTime);
//    }
    @Override
    protected void onResume() {
        super.onResume();
        //Set ScrollView to top
        final ScrollView scroll = ((ScrollView) findViewById(R.id.scrollView));
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_UP);
            }
        });
    }
}
