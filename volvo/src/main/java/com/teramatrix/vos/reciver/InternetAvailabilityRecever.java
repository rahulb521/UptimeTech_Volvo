package com.teramatrix.vos.reciver;

import java.util.ArrayList;
import java.util.List;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.R;
import com.teramatrix.vos.asynctasks.SaveEngineReading;
import com.teramatrix.vos.asynctasks.UploadOfflineTicketingData;
import com.teramatrix.vos.asynctasks.UploadOfflineTrackingData;
import com.teramatrix.vos.checkinternet.CheckInternetConnection;
import com.teramatrix.vos.database.DBInteraction;
import com.teramatrix.vos.interfaces.INetworkAvailablity;
import com.teramatrix.vos.model.Ticket;
import com.teramatrix.vos.model.TrackingLocation;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.UtilityFunction;
import com.teramatrix.vos.volvouptime.asyntask.OfflineDataSync;
import com.teramatrix.vos.volvouptime.models.EngineHourReadingModel;

/**
 * InternetAvailabilityRecever class checks internet is available or not using
 * broadcast receiver.
 * <p>
 * A broadcast receiver is an Android component which allows you to register for
 * system or application events. All registered receivers for an event are
 * notified by the Android runtime once this event happens.
 */

public class InternetAvailabilityRecever extends BroadcastReceiver {

    // Define instance of DBInteraction class
    private DBInteraction dbInteraction;
    public static boolean isOfflineTicketUploadingOnServer;

    /*
     * (non-Javadoc)
     *
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        try {

            Log.i("onReceive", "InternetAvailabilityRecever");

            if (new CheckInternetConnection(context).isConnectedToInternet()) {

                // Network bar on screen colre change
                if (ApplicationConstant.IS_APP_IN_FORGROUND) {
                    INetworkAvailablity nw = (INetworkAvailablity) ApplicationConstant.currentActivityContext;
                    nw.isNetworkAvailable(true);
                }


                dbInteraction = DBInteraction.getInstance(ApplicationConstant.currentActivityContext);
                dbInteraction.getConnection();
                if (dbInteraction.isTrackingDataExists()) {
                    // send data to server
                    ArrayList<TrackingLocation> trackingLocations = dbInteraction
                            .getOfflineTrackingData();
                    String localdata = "";


                    if (trackingLocations != null && trackingLocations.size() > 0) {
                        for (TrackingLocation location : trackingLocations) {

                            localdata = localdata + "," + "["
                                    + location.getLatitude() + ","
                                    + location.getLongitude() + "]";
                        }

                        VECVPreferences vecvPreferences = new VECVPreferences(
                                context);
                        String device_alias = vecvPreferences.getDevice_alias();
                        String token = vecvPreferences.getSecurityToken();
                        new UploadOfflineTrackingData(this, context,
                                trackingLocations, token, device_alias)
                                .execute();
                    }

                }
                if (dbInteraction.isOffLineTicketDataExists()
                        && !isOfflineTicketUploadingOnServer) {

                    isOfflineTicketUploadingOnServer = true;
                    // send data to server
                    ArrayList<Ticket> ticketingdata = dbInteraction
                            .getOfflineTicketData();

                    if (ticketingdata.size() > 0) {
                        VECVPreferences vecvPreferences = new VECVPreferences(
                                context);
                        String device_alias = vecvPreferences.getDevice_alias();
                        String token = vecvPreferences.getSecurityToken();
                        new UploadOfflineTicketingData(this, context,
                                ticketingdata, token, device_alias).execute();
                    }

                }
                dbInteraction.closeConnection();
                dbInteraction = null;
                List<EngineHourReadingModel> engineHourReadingModels = getListWhereOfflineEngineReadingData();
                String reg_number = "", current_data = "", save_time = "",prevUtil="";
                if (engineHourReadingModels.size() > 0) {
                    for (EngineHourReadingModel engineHourReadingModel :
                            engineHourReadingModels) {
                        if (engineHourReadingModel.isModified() && !engineHourReadingModel.getCurrentMonthUtilizationData().equalsIgnoreCase("0")) {
                            new Update(EngineHourReadingModel.class)
                                    .set("isModified = 0, " + "CurrentMonthUtilizationData=" + engineHourReadingModel.getCurrentMonthUtilizationData())
                                    .where("RegistrationNumber = ?", engineHourReadingModel.getRegistrationNumber())
                                    .execute();
                            reg_number = reg_number + engineHourReadingModel.getRegistrationNumber() + ",";
                            current_data = current_data + engineHourReadingModel.getCurrentMonthUtilizationData() + ",";
                            save_time = save_time + engineHourReadingModel.getInRequiredTime() + ",";
                            prevUtil=prevUtil+engineHourReadingModel.getPreviousMonthUtilizationData()+",";
                        }
                    }
                    reg_number = reg_number.substring(0, reg_number.length() - 1);
                    current_data = current_data.substring(0, current_data.length() - 1);
                    save_time=save_time.substring(0,save_time.length()-1);
                    prevUtil=prevUtil.substring(0,prevUtil.length()-1);
                    SaveEngineReading saveEngineReading = new SaveEngineReading(context, current_data, reg_number,save_time,prevUtil);
                    saveEngineReading.execute();

                }
                new OfflineDataSync(context);


            } else {
                if (ApplicationConstant.IS_TESTING_TOAST_SHOWN)
                    Toast.makeText(context,
                            context.getResources().getString(R.string.network_not_available),
                            Toast.LENGTH_LONG).show();

                if (ApplicationConstant.IS_APP_IN_FORGROUND) {
                    INetworkAvailablity nw = (INetworkAvailablity) ApplicationConstant.currentActivityContext;
                    nw.isNetworkAvailable(false);
                }
            }
        } catch (Exception e) {
            //Google Analytic -Tracking Exception
            EosApplication.getInstance().trackException(e);
            UtilityFunction.saveErrorLog(context, e);
        }
    }

    // Delete offline stored Location data after all data have been uploaded on
    // remote server.
    public void deletOffLineLocationData() throws Exception {

        DBInteraction dbInteraction = DBInteraction.getInstance(ApplicationConstant.currentActivityContext);
        dbInteraction.getConnection();
        try {
            if (dbInteraction.isTrackingDataExists()) {
                dbInteraction.deleteOfflineTrackingData();
            }
        } catch (Exception e) {

            throw e;
        } finally {
            dbInteraction.closeConnection();
            dbInteraction = null;

        }

    }

    // Delete Offline Stored Ticket data after all data have been uploaded on
    // remote server.
    public void deletOffLineTicketData() throws Exception {

        DBInteraction dbInteraction = DBInteraction.getInstance(ApplicationConstant.currentActivityContext);
        dbInteraction.getConnection();
        if (dbInteraction.isOffLineTicketDataExists()) {
            try {
                dbInteraction.deleteOfflineTicketData();
            } catch (Exception e) {
                throw e;
            }
        }
        dbInteraction.closeConnection();
        dbInteraction = null;
    }

    /**
     * check if engine reading data is available in offline db then push data to server
     *
     * @return
     */
    private List<EngineHourReadingModel> getListWhereOfflineEngineReadingData() {
        List<EngineHourReadingModel> engineHourReadingModels = new ArrayList<>();
        engineHourReadingModels = new Select().from(EngineHourReadingModel.class).where("isModified = 1").execute();
        return engineHourReadingModels;
    }
}
