package com.teramatrix.vos.firebase.config;

import com.teramatrix.vos.volvouptime.models.VehicleModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ubuntu on 7/12/16.
 */

public class Config {
    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "ah_firebase";
    public static  boolean isClickable = true;
    public static  boolean is24Hrs = false;
    public static  List<VehicleModel> vehicleModelList;
    public static  List<VehicleModel> vehicleModelFilterListSecond;

}
