package com.teramatrix.vos.volvouptime.asyntask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.activeandroid.query.Select;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.utils.TimeFormater;
import com.teramatrix.vos.utils.UtilityFunction;
import com.teramatrix.vos.volvouptime.custom.DAO;
import com.teramatrix.vos.volvouptime.models.UpTimeAddedReasonsModel;
import com.teramatrix.vos.volvouptime.models.UpTimeTicketDetailModel;
import com.teramatrix.vos.volvouptime.models.VehicleModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Arun.Singh on 5/25/2018.
 * This Asyn task will call API to fetch ( Vehicles List + All Open Tickets + All reasons of all tickets) from server.
 * After fetching all data from server it will store them in local db (Active Android model)
 */
public class UpTimeGetData extends AsyncTask<Void, Void, Void> {

    String TAG = this.getClass().getSimpleName();
    // Define Context for this class
    private Context mContext;
    // Define String variables for this class
    private String securityToken, siteId, response;
    // Define ProgressDialog for this class
    private ProgressDialog mProgressDialog;
    //Interface reference for returning data to caller
    private I_UpTimeGetVehicles i_upTimeGetVehicles;


    /**
     * @param context
     * @param token
     */

    public UpTimeGetData(Context context, String token,
                         String siteId, UpTimeGetData.I_UpTimeGetVehicles i_upTimeGetVehicles) {
        mContext = context;
        securityToken = token;
        this.siteId = siteId;
        this.i_upTimeGetVehicles = i_upTimeGetVehicles;
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
        mProgressDialog = ProgressDialog.show(mContext, "", "Loading Vehicle data from server..",
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

            // Get Volvo Uptime Vehicles data from API
            RestIntraction restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS() + ""
                    + ApiUrls.UPTIME_GET_DATA);
            restIntraction.AddParam("Token", securityToken);
            restIntraction.AddParam("SiteId", siteId);
            restIntraction.Execute(1);
            Log.i("Request", restIntraction.toString());
            response = restIntraction.getResponse();
            Log.i("response", response);
            if (response != null) {
                try {



                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("Status").equalsIgnoreCase("1")) {
                        //Delete All Records from local db(Active Android models)
                        DAO.deleteAllRecords(VehicleModel.class);
                        DAO.deleteAllRecords(UpTimeTicketDetailModel.class);
                        DAO.deleteAllRecords(UpTimeAddedReasonsModel.class);
                        JSONArray jsonArray = jsonObject.getJSONArray("VehicleTicketReasonsList");

                        Log.e(TAG, "doInBackground: json array length "+jsonArray.length() );
                        if (jsonArray.length() > 0) {


                                for (int i = 0; i < jsonArray.length(); i++) {
                                    //Save Vehicle Details

                                    //try{
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    String RegistrationNumber = jsonObject1.getString("RegistrationNumber");
                                    String NumberPlate = jsonObject1.getString("VehicleNumberPlate");
                                    String ModelNumber = jsonObject1.getString("ModelNumber");
                                    String VehicleType = jsonObject1.getString("VehicleType");
                                    String InstallationDate = jsonObject1.getString("VehicleInstallationDate");
                                    String SiteId = jsonObject1.getString("SiteId");
                                    String DoorNumber = jsonObject1.getString("DoorNumber");
                                    String ChassisNumber = jsonObject1.getString("ChassisNumber");
                                    String VehicleStatus = jsonObject1.getString("VehicleStatus");
                                    String JobStrtDate = jsonObject1.getString("JobStartDate");
                                    String ticketId = jsonObject1.getString("TicketId");
                                    String causalpart = jsonObject1.getString("causalpart");
                                    String enginehours = jsonObject1.getString("Enginehours");

                                    String preenginehours = jsonObject1.getString("PreEnginehours");
                                    Log.e("TAG", preenginehours+" doInBackground: causl part "+causalpart );


                                    VehicleModel vehicleModel = new Select()
                                            .from(VehicleModel.class)
                                            .where("Registration = ?", RegistrationNumber)
                                            .executeSingle();

                                    Log.e(TAG + RegistrationNumber, jsonArray.length() + "  doInBackground: iiii " + i);


                                    if (vehicleModel == null)
                                        vehicleModel = new VehicleModel();
                                    vehicleModel.setReg_no_value(RegistrationNumber);
                                    vehicleModel.setJobStartDate(JobStrtDate);
                                    vehicleModel.setNumberPlate(NumberPlate);
                                    vehicleModel.setModelNumber(ModelNumber);
                                    vehicleModel.setInstallationDate(InstallationDate);
                                    vehicleModel.setDoorNumber(DoorNumber);
                                    vehicleModel.setChassisNumber(ChassisNumber);
                                    vehicleModel.setVehicleType(VehicleType);
                                    vehicleModel.setVehicleStatus(VehicleStatus);
                                    vehicleModel.setSiteId(SiteId);
                                    vehicleModel.setTicketId(ticketId);
                                    vehicleModel.setPreEnginehours(preenginehours);

                                    vehicleModel.save();

                                    //Save Ticket Details
                                    String ServiceName = jsonObject1.getString("ServiceName");
                                    String VehicleRegistrationNumber = jsonObject1.getString("VehicleRegistrationNumber");
                                    String TicketId = jsonObject1.getString("TicketId");
                                    String TicketIdAlias = jsonObject1.getString("TicketIdAlias");
                                    String JobStartDate = jsonObject1.getString("JobStartDate");
                                    String JobDescriptionComment = jsonObject1.getString("Description");
                                    JobStartDate = TimeFormater.getTimeInIST(JobStartDate, "yyyy-MM-dd HH:mm:ss", "dd MMM yyyy HH:mm");
                                    String JobEndDate = jsonObject1.getString("JobEndDate");
                                    JobEndDate = TimeFormater.getTimeInIST(JobEndDate, "yyyy-MM-dd HH:mm:ss", "dd MMM yyyy HH:mm");
                                    String ServiceTypeSequenceNo = jsonObject1.getString("ServiceTypeSequenceNo");

                                    Log.e(TAG, "doInBackground: 2222222222" );
                                    UpTimeTicketDetailModel upTimeTicketDetailModel = new Select()
                                            .from(UpTimeTicketDetailModel.class)
                                            .where("TicketId = ?", TicketId)
                                            .executeSingle();

                                    Log.e(TAG, "doInBackground: 333333333" );

                                        if (upTimeTicketDetailModel == null) {
                                            upTimeTicketDetailModel = new UpTimeTicketDetailModel();
                                            upTimeTicketDetailModel.setStatusAlias(ServiceName);
                                            upTimeTicketDetailModel.setVehicleRegistrationNumber(VehicleRegistrationNumber);
                                            upTimeTicketDetailModel.setTicketId(TicketId);
                                            upTimeTicketDetailModel.setTicketIdAlias(TicketIdAlias);
                                            upTimeTicketDetailModel.setStartDate(JobStartDate);
                                            upTimeTicketDetailModel.setEndDate(JobEndDate);
                                            upTimeTicketDetailModel.setIsSyncWithServer("true");
                                            upTimeTicketDetailModel.setSequenceOrder(ServiceTypeSequenceNo.split(",")[0]);
                                            upTimeTicketDetailModel.setJobComment(JobDescriptionComment);
                                            upTimeTicketDetailModel.setCausalPart(causalpart);
                                            upTimeTicketDetailModel.setEnginehours(enginehours);
                                            upTimeTicketDetailModel.setPreEnginehours(preenginehours);
                                            upTimeTicketDetailModel.save();
                                        }

                                    Log.e(TAG, "doInBackground: 5555555" );
                                    //Save Ticket's Reasons Details
                                    //Log.e(TAG, "doInBackground: reason name jsonobject "+jsonObject1 );
                                    String ReasonName = jsonObject1.getString("ReasonName");
                                    String ReasonAlias = jsonObject1.getString("ReasonAlias");
                                    String DelayReasonStatus = jsonObject1.getString("DelayReasonStatus");
                                    String ReasonSequenceNumber = jsonObject1.getString("ReasonSequenceNumber");
                                    String ReasonStartDate = jsonObject1.getString("ReasonStartDate");
                                    String ReasonEndDate = jsonObject1.getString("ReasonEndDate");
                                    String DelayedReasonUniqueId = jsonObject1.getString("DelayedReasonUniqueId");
                                    String DelayedReasonRemarks = jsonObject1.getString("DelayedReasonRemarks");


                                    Log.e(TAG, "doInBackground: reason name "+ReasonName );

                                    if (ReasonName == null || ReasonName.equalsIgnoreCase("null"))
                                        continue;

                                    //all list size should be same================================
                                    List<String> DelayReasonStatusList = UtilityFunction.spiltAndGenerateList(DelayReasonStatus, "\\$");
                                    List<String> ReasonNameList = UtilityFunction.spiltAndGenerateList(ReasonName, "\\$");
                                    List<String> ReasonAliasList = UtilityFunction.spiltAndGenerateList(ReasonAlias, "\\$");
                                    List<String> ReasonSequenceNumberList = UtilityFunction.spiltAndGenerateList(ReasonSequenceNumber, "\\$");
                                    List<String> ReasonStartDateNumberList = UtilityFunction.spiltAndGenerateList(ReasonStartDate, "\\$");
                                    List<String> ReasonEndDateNumberList = UtilityFunction.spiltAndGenerateList(ReasonEndDate, "\\$");
                                    List<String> DelayedReasonUniqueIdList = UtilityFunction.spiltAndGenerateList(DelayedReasonUniqueId, "\\$");
                                    List<String> DelayedReasonRemarksList = UtilityFunction.spiltAndGenerateList(DelayedReasonRemarks, "\\$");


                                    Log.e(TAG, RegistrationNumber+" doInBackground: 444444 \n " +DelayReasonStatusList.size()
                                            +"\n reason "+ReasonNameList.size()
                                    +"\n "+ReasonAliasList.size()
                                    +"\n "+ReasonSequenceNumberList.size()
                                    +"\n "+ReasonStartDateNumberList.size()
                                    +"\n "+ReasonEndDateNumberList.size()
                                    +"\n "+DelayedReasonUniqueIdList.size()
                                    +"\n "+DelayedReasonRemarksList.size());

                                        for (int j = 0; j < DelayReasonStatusList.size(); j++) {
                                            String ReasonId = DelayReasonStatusList.get(j);
                                            UpTimeAddedReasonsModel upTimeAddedReasonsModel = new Select()
                                                    .from(UpTimeAddedReasonsModel.class)
                                                    .where("TicketId = ? AND ReasonId = ? AND inreasonUniqueId = ?", upTimeTicketDetailModel.getTicketId(), ReasonId, DelayedReasonUniqueIdList.get(j))
                                                    .executeSingle();

                                            if (upTimeAddedReasonsModel == null)
                                                upTimeAddedReasonsModel = new UpTimeAddedReasonsModel();

                                            upTimeAddedReasonsModel.set_reason_id(ReasonId);
                                            upTimeAddedReasonsModel.set_reason_name(ReasonNameList.get(j));
                                            upTimeAddedReasonsModel.set_reason_alias(ReasonAliasList.get(j));
                                            upTimeAddedReasonsModel.set_reason_sequence_number(ReasonSequenceNumberList.get(j));
                                            upTimeAddedReasonsModel.setInreasonUniqueId(DelayedReasonUniqueIdList.get(j));

                                            String reasonStartDate = TimeFormater.getTimeInIST(ReasonStartDateNumberList.get(j), "yyyy-MM-dd HH:mm:ss", "dd MMM yyyy HH:mm");
                                            upTimeAddedReasonsModel.setReasonStartDate(reasonStartDate);

                                            if (ReasonEndDateNumberList.size() > j) {
                                                String reasonEndDate = TimeFormater.getTimeInIST(ReasonEndDateNumberList.get(j), "yyyy-MM-dd HH:mm:ss", "dd MMM yyyy HH:mm");
                                                upTimeAddedReasonsModel.setReasonEndDate(reasonEndDate);
                                            }

                                            upTimeAddedReasonsModel.setIsSyncWithServer("true");
                                            upTimeAddedReasonsModel.set_ticket_id(upTimeTicketDetailModel.getTicketId());
                                            upTimeAddedReasonsModel.setDelayedReasonComment(DelayedReasonRemarksList.get(j));

                                            Long result = upTimeAddedReasonsModel.save();
                                            Log.e("result", result + "");
                                        }


                                    Log.e(TAG, "doInBackground: 55555555" );
                                    Log.e(TAG, "doInBackground: ==============================" );
//                                }catch (Exception e){
//                                        Log.e(TAG, "doInBackground: eeeeeee 222 "+e.getMessage() );
//                                    }
                                }

                        }
                    }

                }
                catch (Exception e) {

                    Log.e(TAG, e.getLocalizedMessage()+"doInBackground: exception "+e.getMessage() );
                    // Google Analytic -Tracking Exception
                    //EosApplication.getInstance().trackException(e);
                    // save error location in file
                    UtilityFunction.saveErrorLog(mContext, e);
               }
            }

        } catch (Exception ex) {

            Log.e(TAG, "doInBackground: ecxeeeee "+ex.getMessage() );
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

        //Get All down vehicles
        List<VehicleModel> vehicleModels_down = new Select()
                .from(VehicleModel.class)
                .where("VehicleStatus = ?", "0")
                .orderBy("DoorNumber ASC")
                .execute();
        //Get all Up vehicles
        List<VehicleModel> vehicleModels_Up = new Select()
                .from(VehicleModel.class)
                .where("VehicleStatus = ?", "1")
                .orderBy("DoorNumber ASC")
                .execute();
        //Get all inactive vehicles
        List<VehicleModel> vehicleModels_In = new Select()
                .from(VehicleModel.class)
                .where("VehicleStatus = ?", "2")
                .orderBy("DoorNumber ASC")
                .execute();
        //Merge Lsit of Down and Up vehicle
        vehicleModels_down.addAll(vehicleModels_Up);
        vehicleModels_down.addAll(vehicleModels_In);
      //  vehicleModels_down.addAll(vehicleModels_down);
        //Provide data to caller
        i_upTimeGetVehicles.vehicleList(vehicleModels_down);
    }

    /**
     * Interface to be implemented by class which used this Asyn task.
     */
    public interface I_UpTimeGetVehicles {
        void vehicleList(List<VehicleModel> vehicleModels);
    }

}
