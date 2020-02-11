package com.teramatrix.vos.volvouptime.custom;

import android.content.Context;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.TableInfo;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.volvouptime.asyntask.UpTimeGetData;
import com.teramatrix.vos.volvouptime.models.UpTimeAddedReasonsModel;
import com.teramatrix.vos.volvouptime.models.UpTimeReasonsModel;
import com.teramatrix.vos.volvouptime.models.UpTimeTicketDetailModel;

import java.util.List;

/**
 * Created by Arun.Singh on 5/22/2018.
 * Middle layer to local database access.
 */

public class DAO {

    /**
     * This method will call a API to get all vehicles from server, will save them into local db and then will return a list of vehicles.
     * If network is not available ,this method will return the list of vehicle from local db.
     */
    public static void getAllVehicles(Context context, UpTimeGetData.I_UpTimeGetVehicles i_upTimeGetVehicles) {
        String siteId = new VECVPreferences(context).getSiteId();
        new UpTimeGetData(context, "teramatrix", siteId, i_upTimeGetVehicles).execute();
    }

    /**
     * Get stored ticket details
     *
     * @param vehicleRegistrationNumber registration number
     * @return Ticket detail model
     */
    public static UpTimeTicketDetailModel getTicketDetails(String vehicleRegistrationNumber) {
        //Load Ticekt Details
        List<UpTimeTicketDetailModel> upTimeTicketDetailModels = new Select()
                .from(UpTimeTicketDetailModel.class)
                .where("VehicleRegistration = ?", vehicleRegistrationNumber)
                .execute();

        String tickeId = upTimeTicketDetailModels.get(0).getTicketId();
        List<UpTimeAddedReasonsModel> upTimeAddedReasonsModels = getAddedReasons(tickeId);
        upTimeTicketDetailModels.get(0).setUpTimeReasonsList(upTimeAddedReasonsModels);

        return upTimeTicketDetailModels.get(0);
    }

    /**
     * Get added reasons of a ticket from local dataase
     *
     * @param ticketId ticket ID
     * @return List of added reasons
     */
    public static List<UpTimeAddedReasonsModel> getAddedReasons(String ticketId) {
        if (ticketId == null)
            return null;

        List<UpTimeAddedReasonsModel> upTimeAddedReasonsModels = new Select()
                .from(UpTimeAddedReasonsModel.class)
                .where("TicketId = ?", ticketId)
                .execute();
        return upTimeAddedReasonsModels;
    }

    /**
     * Get all general reasons from local Databse
     *
     * @return list of Reasons
     */
    public static List<UpTimeReasonsModel> getAllReasons() {
        List<UpTimeReasonsModel> upTimeReasonsModels = new Select()
                .from(UpTimeReasonsModel.class)
                .execute();
        return upTimeReasonsModels;
    }


    /**
     * This Method removes all records(Active Android Models) in a table.
     *
     * @param aClass object of class
     */
    public static void deleteAllRecords(Class aClass) {
        TableInfo tableInfo = Cache.getTableInfo(aClass);
        ActiveAndroid.execSQL(
                String.format("DELETE FROM %s;",
                        tableInfo.getTableName()));
        ActiveAndroid.execSQL(
                String.format("DELETE FROM sqlite_sequence WHERE name='%s';",
                        tableInfo.getTableName()));
    }

    public static void deleteAllWhereTicketId(String ticketid, Class aClass, String type) {
        TableInfo tableInfo = Cache.getTableInfo(aClass);
        new Delete().from(aClass).where("TicketId = ?",ticketid).where("ActivityType = ?",type).execute();

    }
    public static void  deleteReasonWithId(String id,Class aClass){
        new Delete().from(aClass).where("inreasonUniqueId = ?",id).execute();

    }


}
