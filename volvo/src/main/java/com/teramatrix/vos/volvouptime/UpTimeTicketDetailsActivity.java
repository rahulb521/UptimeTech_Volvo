package com.teramatrix.vos.volvouptime;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.teramatrix.vos.R;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.utils.TimeFormater;
import com.teramatrix.vos.volvouptime.asyntask.UpTimeUpdateTicket;
import com.teramatrix.vos.volvouptime.custom.ConfirmationDialog;
import com.teramatrix.vos.volvouptime.custom.DAO;
import com.teramatrix.vos.volvouptime.models.UpTimeAddedReasonsModel;
import com.teramatrix.vos.volvouptime.models.UpTimeTicketDetailModel;

import java.util.Date;
import java.util.List;


/**
 * Created by arun.singh on 6/1/2018.
 * This activity presents details of created ticket.
 *
 */
public class UpTimeTicketDetailsActivity extends UpTimeBaseActivity implements
        UpTimeUpdateTicket.I_UpTimeUpdateTicket,ConfirmationDialog.I_ConfirmationResponse {


    private String ticket_JobType_SequenceOrder,ticketId,ticketIdAlias,registration_no,chasis_number ;
    private boolean isTicketComplete = true;
    private Date reasonLatestEndDate,jobEndDate ;
    private String oldestStartTime_reason;
    private String latestEndTime_reason;
    private String door_no;

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
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.volvo_blue));
        }

        setContentView(R.layout.activity_ticket_details);

        //Set Title of Screen
        ((TextView)findViewById(R.id.rl_title_bar_title)).setText("Job Details");

        //Add New Reason button Click
        findViewById(R.id.add_reason).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Open RegisterActivity to register new Reason
                Intent mainIntent = new Intent(UpTimeTicketDetailsActivity.this,
                        UpTimeRegisterActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mainIntent.putExtra("type", UpTimeRegisterActivity.TYPE_ADD_REASON);
                mainIntent.putExtra("job_type_id",ticket_JobType_SequenceOrder);
                mainIntent.putExtra("ticketId", ticketId);
                mainIntent.putExtra("ticketIdAlias", ticketIdAlias);
                mainIntent.putExtra("door_no", door_no);
                mainIntent.putExtra("chasis_number", chasis_number);

                String jobStartEndDate = ((TextView)findViewById(R.id.txt_job_time)).getText().toString();
                String[] dateArray = jobStartEndDate.split("-");
                String jobStartDate = dateArray[0];
                String jobEndDate = dateArray[1];
                 //Replacewithcurrent
                String currentTime = TimeFormater.convertMillisecondsToDateFormat(System.currentTimeMillis(), "dd MMM yyyy HH:mm");

                // mainIntent.putExtra("jobStartDate", jobStartDate.trim());
                mainIntent.putExtra("jobStartDate", currentTime);
                mainIntent.putExtra("jobEndDate", jobEndDate.trim());
                startActivityForResult(mainIntent,1002);
            }
        });

        //Close Screen
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Close Ticket
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isTicketComplete) {
                    Toast.makeText(UpTimeTicketDetailsActivity.this, "Please enter End Time before closing ticket.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(reasonLatestEndDate!=null && jobEndDate.before(reasonLatestEndDate))
                {
                    Toast.makeText(UpTimeTicketDetailsActivity.this, "Please enter valid End Time of Job.", Toast.LENGTH_SHORT).show();
                    return;
                }

                ConfirmationDialog.showConfirmationDialog(UpTimeTicketDetailsActivity.this,"Are you sure you want to close this Ticket?",UpTimeTicketDetailsActivity.this);

            }
        });

        registration_no = getIntent().getStringExtra("registration_no");
        door_no = getIntent().getStringExtra("door_no");
        chasis_number = getIntent().getStringExtra("chasis_number");
                //txt_chasis_number

        //new UpTimeGetTicketDetails(this,"teramatrix",registration_no,this).execute();
        loadTicketDetails(registration_no);
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerBReciver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBReciver();
    }

    @Override
    public void syncCompleteCallback(int status) {
        super.syncCompleteCallback(status);
        setResult(RESULT_OK);
        finish();
    }

    /**
     * This  method create a list of ticket's added reasons.
     * @param modelArrayList - list of reasons
     */
    private void loadRegisteredReasonList(List<UpTimeAddedReasonsModel> modelArrayList)
    {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.lay_linear_reasons);
        linearLayout.removeAllViews();

        for(int i=0;i<modelArrayList.size();i++) {

            final UpTimeAddedReasonsModel upTimeReasonsModel = modelArrayList.get(i);
            View v = LayoutInflater.from(this).inflate(R.layout.reason_item_layout, null);
            TextView textView = v.findViewById(R.id.tv_reason);
            textView.setText(upTimeReasonsModel.get_reason_alias()+ " ");

            String reasonStartDate = upTimeReasonsModel.getReasonStartDate();
            String reasonEdDate = upTimeReasonsModel.getReasonEndDate();

            TextView txt_reason_time = v.findViewById(R.id.txt_reason_time);

            if(reasonEdDate==null || reasonEdDate.isEmpty() || reasonEdDate.contains("N/A")|| reasonEdDate.contains("01 Jan 0001")) {
                reasonEdDate = "N/A";
                isTicketComplete = false;
            }else
            {
                Date date = TimeFormater.getDateFromString(reasonEdDate,"dd MMM yyyy HH:mm");
                if(reasonLatestEndDate==null)
                    reasonLatestEndDate = date;
                else
                    reasonLatestEndDate = reasonLatestEndDate.before(date)?date:reasonLatestEndDate;
            }

            //Set dealyed reason start/ end time
            txt_reason_time.setText(reasonStartDate+" - "+reasonEdDate);

            //Set delayed reason comment
            ((TextView)v.findViewById(R.id.txt_reason_comment)).setText(upTimeReasonsModel.getDelayedReasonComment());

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent mainIntent = new Intent(UpTimeTicketDetailsActivity.this,
                            UpTimeRegisterActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mainIntent.putExtra("type", UpTimeRegisterActivity.TYPE_EDIT_REASON);
                    mainIntent.putExtra("job_type_id",ticket_JobType_SequenceOrder);
                    mainIntent.putExtra("ticketId", ticketId);
                    mainIntent.putExtra("ticketIdAlias", ticketIdAlias);
                    mainIntent.putExtra("reason", upTimeReasonsModel.get_reason_alias());
                    mainIntent.putExtra("reasonId", upTimeReasonsModel.get_reason_id());
                    mainIntent.putExtra("inreasonUniqueId", upTimeReasonsModel.getInreasonUniqueId());
                    String jobTypeAlias = ((TextView)findViewById(R.id.txt_job_type)).getText().toString();
                    mainIntent.putExtra("jobTypeAlias", jobTypeAlias);
                    mainIntent.putExtra("reasonStartDate", upTimeReasonsModel.getReasonStartDate());
                    mainIntent.putExtra("reasonEndDate", upTimeReasonsModel.getReasonEndDate());
                    mainIntent.putExtra("delayedReasonComment", upTimeReasonsModel.getDelayedReasonComment());
                    mainIntent.putExtra("door_no", door_no);
                    mainIntent.putExtra("chasis_number", chasis_number);

                    String jobStartEndDate = ((TextView)findViewById(R.id.txt_job_time)).getText().toString();
                    String[] dateArray = jobStartEndDate.split("-");
                    String jobStartDate = dateArray[0];
                    String jobEndDate = dateArray[1];
                    mainIntent.putExtra("jobStartDate", jobStartDate.trim());
                    mainIntent.putExtra("jobEndDate", jobEndDate.trim());

                    startActivityForResult(mainIntent,1002);
                }
            });

            linearLayout.addView(v);


            //Get 'Oldest Start time' and 'Latest End time' out of all reasons.
            //Get oldest Start Time out of all reason
            if(oldestStartTime_reason!=null) {

                int result = TimeFormater.compareDateString(oldestStartTime_reason, reasonStartDate, "dd MMM yyyy HH:mm");
                if (result > 0)
                    oldestStartTime_reason = reasonStartDate;
            }else
            {
                oldestStartTime_reason = reasonStartDate;
            }

            //Get latest End Time out of all reason
            if(latestEndTime_reason!=null && !latestEndTime_reason.equalsIgnoreCase("N/A"))
            {
                int result = TimeFormater.compareDateString(latestEndTime_reason, reasonEdDate, "dd MMM yyyy HH:mm");
                if (result < 0)
                    latestEndTime_reason = reasonEdDate;
            }else
            {
                latestEndTime_reason = reasonEdDate;
            }


        }

    }

    /**
     * This method load Ticket from local database and dispaly its details
     * @param vehicleRegistrationNumber
     */
    private void loadTicketDetails(String vehicleRegistrationNumber)
    {
       final UpTimeTicketDetailModel upTimeTicketDetailModel = DAO.getTicketDetails(vehicleRegistrationNumber);

        if(upTimeTicketDetailModel!=null)
        {
            isTicketComplete= true;

            System.out.println("Get Ticket Details:"+upTimeTicketDetailModel.getStatusAlias()+" "+upTimeTicketDetailModel.getVehicleRegistrationNumber());

            ticket_JobType_SequenceOrder = upTimeTicketDetailModel.getSequenceOrder();
            ticketId = upTimeTicketDetailModel.getTicketId();
            ticketIdAlias = upTimeTicketDetailModel.getTicketIdAlias();

            ((TextView)findViewById(R.id.txt_door_no)).setText(door_no);
            ((TextView)findViewById(R.id.txt_chasis_number)).setText(chasis_number);

            ((TextView)findViewById(R.id.txt_ticket_id)).setText(ticketIdAlias);
            ((TextView)findViewById(R.id.txt_job_type)).setText(upTimeTicketDetailModel.getStatusAlias());
            ((EditText)findViewById(R.id.txt_job_comment)).setText(upTimeTicketDetailModel.getJobComment().trim());



            //Reason time text
            //String startTime = TimeFormater.convertDateFormate(upTimeTicketDetailModel.getStartDate(),"yyyy-MM-dd HH:mm:ss","dd MMM yyyy HH:mm");
            //String endTime = TimeFormater.convertDateFormate(upTimeTicketDetailModel.getEndDate(),"yyyy-MM-dd HH:mm:ss","dd MMM yyyy HH:mm");

            String startTime = upTimeTicketDetailModel.getStartDate();
            String endTime = upTimeTicketDetailModel.getEndDate();

            if(endTime==null || endTime.contains("01 Jan 0001") || endTime.isEmpty()) {
                endTime = "N/A";
                isTicketComplete = false;
            }else
            {
                jobEndDate = TimeFormater.getDateFromString(endTime,"dd MMM yyyy HH:mm");
            }

            ((TextView)findViewById(R.id.txt_job_time)).setText(startTime+" - "+endTime);


            //If Ticket has Reason Data
            List<UpTimeAddedReasonsModel> modelArrayList = upTimeTicketDetailModel.getUpTimeReasonsList();
            if(modelArrayList!=null && modelArrayList.size()>0)
            {
                findViewById(R.id.txt_reason_title).setVisibility(View.VISIBLE);
                loadRegisteredReasonList(modelArrayList);
            }else
            {
                findViewById(R.id.txt_reason_title).setVisibility(View.GONE);
            }

            //Edit Job Type Click Event
            findViewById(R.id.edit_job_type).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent mainIntent = new Intent(UpTimeTicketDetailsActivity.this,
                            UpTimeRegisterActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mainIntent.putExtra("type", UpTimeRegisterActivity.TYPE_EDIT_JOB);
                    mainIntent.putExtra("job_type_id",ticket_JobType_SequenceOrder);
                    mainIntent.putExtra("ticketId", ticketId);
                    mainIntent.putExtra("ticketIdAlias", ticketIdAlias);
                    mainIntent.putExtra("jobTypeAlias", upTimeTicketDetailModel.getStatusAlias());
                    mainIntent.putExtra("reasonStartDate", upTimeTicketDetailModel.getStartDate());
                    mainIntent.putExtra("reasonEndDate", upTimeTicketDetailModel.getEndDate());
                    mainIntent.putExtra("jobStartDate", upTimeTicketDetailModel.getStartDate());
                    mainIntent.putExtra("jobEndDate", upTimeTicketDetailModel.getEndDate());
                    mainIntent.putExtra("oldestStartTime_reason", oldestStartTime_reason);
                    mainIntent.putExtra("door_no", door_no);
                    mainIntent.putExtra("chasis_number", chasis_number);
                    mainIntent.putExtra("delayedReasonComment",upTimeTicketDetailModel.getJobComment());

                    if(latestEndTime_reason==null || latestEndTime_reason.equalsIgnoreCase("N/A"))
                        latestEndTime_reason = oldestStartTime_reason;

                    mainIntent.putExtra("latestEndTime_reason", latestEndTime_reason);
                    startActivityForResult(mainIntent,1002);
                }
            });

            //Enable Work Completed/Delayed Reason button
            findViewById(R.id.close).setEnabled(true);
            findViewById(R.id.add_reason).setEnabled(true);

        }else
        {
            //Disable Work Completed/Delayed Reason button
            findViewById(R.id.close).setEnabled(false);
            findViewById(R.id.add_reason).setEnabled(false);
        }

        //Set ScrollView to top
        ((ScrollView)findViewById(R.id.scrollView)).fullScroll(ScrollView.FOCUS_UP);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1002)
        {
            if(resultCode == RESULT_OK)
            {
                //Re-Load Ticket Details
               // new UpTimeGetTicketDetails(UpTimeTicketDetailsActivity.this,"teramatrix",registration_no,UpTimeTicketDetailsActivity.this).execute();
                loadTicketDetails(registration_no);

            }
        }
    }

    @Override
    public void onTicketupdateResponse(boolean isUpdateSuccessful, String msg) {

        if(isUpdateSuccessful) {
            setResult(RESULT_OK);
            finish();
        }else {
            EngineHourReadingDialog engineHourReadingDialog = new EngineHourReadingDialog(this, msg);
            engineHourReadingDialog.show();
        }
    }

    @Override
    public void onPositive_ConfirmationDialog() {

        String licenceKey = new VECVPreferences(UpTimeTicketDetailsActivity.this).getLicenseKey();
        EditText editText = (EditText) findViewById(R.id.txt_job_comment);
        String comment = editText.getText().toString();
        if (comment == null || comment.isEmpty()) {
            Toast.makeText(UpTimeTicketDetailsActivity.this, "Enter a valid description of delayed reason.", Toast.LENGTH_SHORT).show();
            return;
        }

        UpTimeUpdateTicket.RequestModel requestModel = new UpTimeUpdateTicket.RequestModel();
        requestModel.requestType = UpTimeRegisterActivity.TYPE_CLOSE_JOB;
        requestModel.token = "teramatrix";
        requestModel.vehicleRegNo = registration_no;
        requestModel.licenceNo = licenceKey;
        requestModel.TicketId = ticketId;
        requestModel.isTicketClosed = "true";
        requestModel.delayedReasonComment=comment;


        new UpTimeUpdateTicket(
                UpTimeTicketDetailsActivity.this,
                requestModel,
                UpTimeTicketDetailsActivity.this).execute();
    }

    @Override
    public void onNegative_ConfirmationDialog() {

    }
}
