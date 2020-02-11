package com.teramatrix.vos.volvouptime.asyntask;

import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.activeandroid.query.Select;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.utils.UtilityFunction;
import com.teramatrix.vos.volvouptime.UpTimeRegisterActivity;
import com.teramatrix.vos.volvouptime.custom.DAO;
import com.teramatrix.vos.volvouptime.models.UpTimeAddedReasonsModelActivity;
import com.teramatrix.vos.volvouptime.models.UpTimeTicketDetailModelActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Arun.Singh on 5/25/2018.
 * This is JobScheduler for scheduling Task(Upload Offline Data to server) when Network becomes available.
 */


public class OfflineDataSync implements UpTimeGetTicketId.I_UpTimeGetTicketId {

    //Constant
    public static final String OFFLINE_SYNC_INTENT = "OfflineDataSync.intent";
    public static final String OFFLINE_SYNC_RUNNING_STATUS = "sync_running_status";
    public static final int OFFLINE_SYNC_STARTED = 1;
    public static final int OFFLINE_SYNC_FINISHED_WITH_SUCCESS = 2;
    private Context context;
    public static final int OFFLINE_SYNC_FINISHED_WITH_FAIL = 3;

    private JobParameters params;


    public boolean onStartJob() {

        this.params = params;
        Log.i("OfflineDataSync", "onStartJob");

        List<UpTimeTicketDetailModelActivity> upTimeTicketDetailModelsActivities = new Select()
                .from(UpTimeTicketDetailModelActivity.class)
                .where("ActivityType = ?", UpTimeRegisterActivity.TYPE_JOB)
                .execute();

        if (upTimeTicketDetailModelsActivities.size() > 0) {
            //Notify user(show sync progess on current Opened Screen) about Sync Process
            sendBroadcast(OFFLINE_SYNC_STARTED);
            //Call API to get UUID-TicketID mapping
            new UpTimeGetTicketId(context, "teramatrix", upTimeTicketDetailModelsActivities, this).execute();
            return true;
        }

        List<UpTimeTicketDetailModelActivity> upTimeTicketDetailModelsActivities2 = new Select()
                .from(UpTimeTicketDetailModelActivity.class)
                .where("ActivityType != ?", UpTimeRegisterActivity.TYPE_JOB)
                .execute();

        List<UpTimeAddedReasonsModelActivity> upTimeAddedReasonsModelActivities = new Select()
                .from(UpTimeAddedReasonsModelActivity.class)
                .execute();

        if (upTimeTicketDetailModelsActivities2.size() > 0 || upTimeAddedReasonsModelActivities.size() > 0) {

            //Notify user(show sync progess on current Opened Screen) about Sync Process
            sendBroadcast(OFFLINE_SYNC_STARTED);

            //Call Asyn Task to uplaod Offline data to Server
            new SyncAsy(context).execute();
            return true;
        }

        return false;
    }


    /**
     * Public method to schedule a job.
     * Scheduling condition is JobInfo.NETWORK_TYPE_ANY i.e Network should be availalbe.
     *
     * @param context
     */


    public OfflineDataSync(Context context) {
        this.context = context;
        onStartJob();
    }

    /**
     * In this method UUID/Ticket IDs mapping arrray is recived from Asyn task
     * Following tsk are performed after receving maaping array
     * Step 1: Get all Offline Transaction of Ticket -> (Update Start/End Time, Close Ticket)
     * Step 2: Get All Offline Transaction of added reasons. ->(Add Reason , Update Start/Emd Time of added reason)
     * Step 3: Insert New Ticket ID into offline Records
     *
     * @param uuid_mapping
     */
    @Override
    public void ticketWithOriginalID(String[][] uuid_mapping) {

        Log.i("ticketWithOriginalID", "ticketWithOriginalID");

        if (uuid_mapping != null && uuid_mapping.length > 0) {


            for (int i = 0; i < uuid_mapping.length; i++) {
                String uuid = uuid_mapping[i][0];
                String newTicketID = uuid_mapping[i][1];

                //Step 1: Get all Offline Transaction of Ticket -> (New Ticket,Update Start/End Time, Close Ticket)
                List<UpTimeTicketDetailModelActivity> upTimeTicketDetailModelsActivities = new Select()
                        .from(UpTimeTicketDetailModelActivity.class)
                        .where("TicketId = ?", uuid)
                        .orderBy("Id ASC")
                        .execute();
                //Insert New Ticket ID in offline records
                for (UpTimeTicketDetailModelActivity upTimeTicketDetailModelActivity : upTimeTicketDetailModelsActivities) {
                    upTimeTicketDetailModelActivity.setTicketId(newTicketID);
                    upTimeTicketDetailModelActivity.save();
                }


                //Step 2: Get All Offline Transaction of added reasons. ->(Add Reason , Update Start/Emd Time of added reason)
                List<UpTimeAddedReasonsModelActivity> upTimeAddedReasonsModelActivities = new Select()
                        .from(UpTimeAddedReasonsModelActivity.class)
                        .where("TicketId = ?", uuid)
                        .execute();
                //Insert New Ticket ID in offline records
                for (UpTimeAddedReasonsModelActivity upTimeAddedReasonsModelActivity : upTimeAddedReasonsModelActivities) {
                    upTimeAddedReasonsModelActivity.set_ticket_id(newTicketID);
                    upTimeAddedReasonsModelActivity.save();
                }
                DAO.deleteAllWhereTicketId(newTicketID, UpTimeTicketDetailModelActivity.class, UpTimeRegisterActivity.TYPE_JOB);

                Log.i("ticketWithOriginalID", "ticketWithOriginalID");

                //Step 3: Insert New Ticket ID into offline Records
            }

            //Call Asyn Task to uplaod Offline data to Server
            new SyncAsy(context).execute();

        } else {
            //Send Broadcast to Open Activity(Screen) for notifiying user that Sync Process has failed.
            sendBroadcast(OFFLINE_SYNC_FINISHED_WITH_FAIL);

            //Finish JobScheduler
        }

    }


    /**
     * In This Asy Task we will get all Offline recods from Offline DB and will send them to Server for Sync
     */
    class SyncAsy extends AsyncTask<Void, Void, Void> {

        // Define Context for this class
        private Context mContext;
        // Define String variables for this class
        private String response;
        // Define ProgressDialog for this class
        private ProgressDialog mProgressDialog;
        //Operation Success Status
        private boolean isSuccess;

        /**
         *
         */

        public SyncAsy(Context context) {
            mContext = context;
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
            /*mProgressDialog = ProgressDialog.show(mContext, "","Processing...",
                    false);*/

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

                // Get all Offline Transaction of Ticket -> (Update Start/End Time, Close Ticket)
                List<UpTimeTicketDetailModelActivity> upTimeTicketDetailModelsActivities = new Select()
                        .from(UpTimeTicketDetailModelActivity.class)
                        .where("ActivityType != ?", UpTimeRegisterActivity.TYPE_JOB)
                        .orderBy("Id ASC")
                        .execute();

                // Get All Offline Transaction of added reasons. ->(Add Reason , Update Start/Emd Time of added reason)
                List<UpTimeAddedReasonsModelActivity> upTimeAddedReasonsModelActivities = new Select()
                        .from(UpTimeAddedReasonsModelActivity.class)
                        .execute();

                //Prepare Json String
                JSONArray jsonArray = new JSONArray();
                //Prepare For ( New Ticket + Update Ticket Time)
                for (UpTimeTicketDetailModelActivity upTimeTicketDetailModelActivity : upTimeTicketDetailModelsActivities) {
                    //Add Json for which ticket activity is not of Type -> TYPE_CLOSE_JOB
                    if (!upTimeTicketDetailModelActivity.getActivityType().equalsIgnoreCase(UpTimeRegisterActivity.TYPE_CLOSE_JOB)) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("TicketStatus", upTimeTicketDetailModelActivity.getSequenceOrder());
                        jsonObject.put("VehicleRegistrationNumber", upTimeTicketDetailModelActivity.getVehicleRegistrationNumber());
                        jsonObject.put("StartDate", upTimeTicketDetailModelActivity.getStartDate());
                        jsonObject.put("EndDate", upTimeTicketDetailModelActivity.getEndDate());
                        jsonObject.put("Description", upTimeTicketDetailModelActivity.getJobComment());
                        jsonObject.put("TicketId", upTimeTicketDetailModelActivity.getTicketId());
                        jsonObject.put("isTicketClosed", upTimeTicketDetailModelActivity.getIsTicketClosed());
                        String licenceKey = new VECVPreferences(mContext).getLicenseKey();
                        jsonObject.put("AssignedToUserId", licenceKey);
                        jsonObject.put("token", "teramatrix");
                        jsonArray.put(jsonObject);
                    }
                }
                //Prepare for ( Add new Reason + Update Reason Time )
                for (UpTimeAddedReasonsModelActivity upTimeAddedReasonsModelActivity : upTimeAddedReasonsModelActivities) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("ReasonStartDate", upTimeAddedReasonsModelActivity.getReasonStartDate());
                    jsonObject.put("ReasonEndDate", upTimeAddedReasonsModelActivity.getReasonEndDate());
                    jsonObject.put("TicketId", upTimeAddedReasonsModelActivity.get_ticket_id());
                    jsonObject.put("DelayedReasonId", upTimeAddedReasonsModelActivity.get_reason_id());
                    jsonObject.put("isTicketClosed", upTimeAddedReasonsModelActivity.get_is_active());
                    jsonObject.put("inremarks", upTimeAddedReasonsModelActivity.getDelayedReasonComment());
                    String licenceKey = new VECVPreferences(mContext).getLicenseKey();
                    jsonObject.put("AssignedToUserId", licenceKey);
                    jsonObject.put("token", "teramatrix");
                    jsonObject.put("InreasonUniqueId", upTimeAddedReasonsModelActivity.getInreasonUniqueId());
                    jsonArray.put(jsonObject);
                }
                //Prepare For ( Close Ticket)
                for (UpTimeTicketDetailModelActivity upTimeTicketDetailModelActivity : upTimeTicketDetailModelsActivities) {
                    if (upTimeTicketDetailModelActivity.getActivityType().equalsIgnoreCase(UpTimeRegisterActivity.TYPE_CLOSE_JOB)) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("TicketStatus", upTimeTicketDetailModelActivity.getSequenceOrder());
                        jsonObject.put("VehicleRegistrationNumber", upTimeTicketDetailModelActivity.getVehicleRegistrationNumber());
                        jsonObject.put("StartDate", upTimeTicketDetailModelActivity.getStartDate());
                        jsonObject.put("EndDate", upTimeTicketDetailModelActivity.getEndDate());
                        jsonObject.put("Description", upTimeTicketDetailModelActivity.getJobComment());
                        jsonObject.put("TicketId", upTimeTicketDetailModelActivity.getTicketId());
                        jsonObject.put("isTicketClosed", upTimeTicketDetailModelActivity.getIsTicketClosed());
                        String licenceKey = new VECVPreferences(mContext).getLicenseKey();
                        jsonObject.put("AssignedToUserId", licenceKey);
                        jsonObject.put("token", "teramatrix");

                        jsonArray.put(jsonObject);
                    }

                }


                // Get Volvo Uptime Vehicles data from API
                if (jsonArray.length() > 0) {


                    RestIntraction restIntraction = null;

                    restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS() + ""
                            + ApiUrls.UPTIME_SYNC_OFFLINE);

                    //Common Parameters
                    restIntraction.AddParam("token", "teramatrix");
                    restIntraction.AddParam("BulkTicketStr", jsonArray.toString());

                    restIntraction.Execute(1);
                    Log.i("Request", restIntraction.toString());
                    response = restIntraction.getResponse();
                    Log.i("response", response);

                    if (response != null) {
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("Status").equalsIgnoreCase("1"))
                                isSuccess = true;

                        } catch (Exception e) {
                            // Google Analytic -Tracking Exception
                            //EosApplication.getInstance().trackException(e);
                            // save error location in file
                            UtilityFunction.saveErrorLog(mContext, e);
                        }
                    }
                }

            } catch (Exception ex) {

                // Google Analytic -Tracking Exception
                //EosApplication.getInstance().trackException(ex);
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

            //If All Offline data Sync to to server successfull , delete all offline records now
            if (isSuccess) {

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    JSONArray jsonArray1 = jsonObject.getJSONArray("objOfflineTicketList");
                    if (jsonArray1.length() > 0) {


                        for (int i = 0; i < jsonArray1.length(); i++) {
                            JSONObject jsonObject1 = jsonArray1.getJSONObject(i);
                            // find in reason id here and delete them
                            if (jsonObject1.getString("InreasonUniqueId") != null
                                    && !jsonObject1.getString("InreasonUniqueId").isEmpty()) {
                                //delete reason model data with inreason unique id
                                DAO.deleteReasonWithId(jsonObject1.getString("InreasonUniqueId"), UpTimeAddedReasonsModelActivity.class);
                            } else {
                                //data related to the tickets update or the ticket close
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Delete UpTimeTicketDetailModelActivity records
                DAO.deleteAllRecords(UpTimeTicketDetailModelActivity.class);
                //Delete UpTimeAddedReasonsModelActivity records
                //   DAO.deleteAllRecords(UpTimeAddedReasonsModelActivity.class);

                //Send Broadcast to Open Activity(Screen) for notifiying user that Sync Process has completed successfully.
                //- If any screen other than Home Screen(UpTimeVehicleListActivity) is opened seend broadcast to stop progress bar(Sync progress) and close
                // the activity. Redirect user to Home screen and reload all data from server.
                // - If Home Screen is opened,seend broadcast to stop progress bar(Sync progress) and reload all data from server.
                sendBroadcast(OFFLINE_SYNC_FINISHED_WITH_SUCCESS);

            } else {
                //Send Broadcast to Open Activity(Screen) for notifiying user that Sync Process has failed.
                sendBroadcast(OFFLINE_SYNC_FINISHED_WITH_FAIL);
            }

            //Finish JobScheduler

        }
    }


    /**
     * Create and send Broadcast
     *
     * @param status success status value
     */
    private void sendBroadcast(int status) {
        Intent intent = new Intent();
        intent.setAction(OFFLINE_SYNC_INTENT);
        intent.putExtra(OFFLINE_SYNC_RUNNING_STATUS, status);
        context.sendBroadcast(intent);
    }
}
