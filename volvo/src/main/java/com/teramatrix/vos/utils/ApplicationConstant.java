package com.teramatrix.vos.utils;

import android.content.Context;

//This class for define Application constant that used in the application
public class ApplicationConstant {

	// Each new opened Activity
	// (MyTicketActivity,MapViewTimerActivity,JobStatusActivity) will update its
	// context
	public static Context currentActivityContext = null;
	// splash activity timeout
	public static final int DISPLAY_SPLASH_ACTIVITY = 2000;
	// local shared preference to store configuration,login,current location
	// details
	public static String PREFERENCE_NAME = "myPreference";
	// location count updated by location tracking service
	public static String KEY_LAST_LOCATION_COUNT = "key_last_location_count";
	public static String KEY_NEW_LOCATION_COUNT = "key_new_location_count";
	// last knonw location lat,long find by any location provider,updated and
	// saved in shared preference
	public static String KEY_LATT = "key_latt";
	public static String KEY_LONG = "key_long";
	public static String KEY_PROVIDER = "key_provider";
	// flag to know traking on/off status
	public static String IS_TRACKING_ON = "is_tracking_on";
	// repeat time interval for sending location to server
	public static int LOCATION_UPDATE_INTERVAL = 1000 * 60;
	// App status
	public static boolean IS_APP_IN_FORGROUND = false;
	public static int NEW_TICKET_CHECK_INTERVAL = 1000 * 59;

	// App id on google api console for this application
//	new account
	public static String GCM_SENDER_ID = "15420367036";
//	old account
//	public static String GCM_SENDER_ID = "1092750325919";
	

	// GetOpenTicket Api
	public static String FLAG_NEW_ASSIGNED_TICKET = "1";

	public static boolean IS_TESTING_TOAST_SHOWN = false;
	public static boolean IS_LOG_SHOWN = true;
	// New ticket arrival ringer repeat interval time
	public static int NEW_TICKET_RINGER_REPEAT_TIME = 1000 * 60;
	public static String FIRST_SYNC_TIMESTAMP = "1970-01-01 00:00:00";
	
	public static String LANGUAGE_SELECT="en";
	
	//define isLocationTrackingLogsEnabled log boolean variable
	public static Boolean isLocationTrackingLogsEnabled = false;
	
	//flag to maintain code to update old un-versioned db schema
	public static boolean is_app_update_on_unversioned_db_schema = true;

}
