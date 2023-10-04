package com.teramatrix.vos.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.R;
import com.teramatrix.vos.asynctasks.ApplicationLogging;
import com.teramatrix.vos.asynctasks.SendLocationUpdateToServer;
import com.teramatrix.vos.checkinternet.CheckInternetConnection;
import com.teramatrix.vos.database.DBInteraction;
import com.teramatrix.vos.model.LocationTrackingLog;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.UtilityFunction;
import com.teramatrix.vos.volvouptime.UpTimeVehicleListActivity;


/**
 *
 * This is background service which retrieve latest location data , check if
 * Internet is available or not , send it to server or store in db .
 */
public class PostLocationService extends Service {

	private String TAG = "PostLocationService";
	/**
	 * onStartCommand() is called every time a starts the service using
	 * startService(Intent intent).
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.i(TAG,"onStartCommand");

		// create LocationTrackingLog instance
		LocationTrackingLog location_tracking = null;
		// check isLocationTrackingLogsEnabled enable or not
		if (ApplicationConstant.isLocationTrackingLogsEnabled) {
			// create object LocationTrackingLog
			location_tracking = new LocationTrackingLog();

		}

		// get SharedPreferences of an application
		SharedPreferences preference = getApplicationContext()
				.getSharedPreferences(ApplicationConstant.PREFERENCE_NAME,
						Activity.MODE_PRIVATE);

		// get updated location
		float latt = preference.getFloat(ApplicationConstant.KEY_LATT, 0);
		float lng = preference.getFloat(ApplicationConstant.KEY_LONG, 0);

		// if no location return
		if (latt == 0 || lng == 0)
			return 0;

		String locationLogTime = UtilityFunction.currentUTCTimeWithoutDash();
		String battary_label = getBatteryPercentage(getApplicationContext());
		String charging_state = getBatteryChargingStatus(getApplicationContext());
		String gps_state = check_gps_status(getApplicationContext());

		String is_power_saving_mode_on = "N/A";
		try {

			// Works only for sumsung and HTC Devices
			is_power_saving_mode_on = Settings.System.getString(
					getContentResolver(), "psm_switch").equalsIgnoreCase("1")
					+ "";
		} catch (Exception ex) {
			//Google Analytic -Tracking Exception
//			EosApplication.getInstance().trackException(ex);
		}

		// if network not available store location in local db
		// else send location to server
		if (new CheckInternetConnection(getApplicationContext())
				.isConnectedToInternet()) {

			if (ApplicationConstant.isLocationTrackingLogsEnabled) {

				// set value of the class model LocationTrackingLog online
				location_tracking.setLatitude(latt + "");
				location_tracking.setLongitude(lng + "");
				location_tracking.setLogtime(locationLogTime + "");
				location_tracking.setNetwork_state(getApplicationContext()
						.getResources().getString(R.string.online));
				location_tracking.setBattery_label(battary_label);
				location_tracking.setCharging_state(charging_state);
				location_tracking.setGps_state(gps_state);
				// calling ApplicationLogging class async task
				new ApplicationLogging(getApplicationContext(),
						location_tracking).execute();
			}

			Log.e(TAG,"SendLocationUpdateToServer onstart "+latt+" "+lng);

//			VECVPreferences vecvPreferences = new VECVPreferences(getApplicationContext());
//			String imeiNumber = vecvPreferences.getImeiNumber();
//			if(imeiNumber != null && !imeiNumber.isEmpty()) {
				new SendLocationUpdateToServer(this, getApplicationContext(), latt,
								lng, locationLogTime, battary_label, gps_state,
								charging_state, is_power_saving_mode_on).execute();
		//	}
		} else {
			// check IS_TESTING_TOAST_SHOWN is enable or not
			if (ApplicationConstant.isLocationTrackingLogsEnabled) {

				// set value of the class model LocationTrackingLog offline

				location_tracking.setLatitude(latt + "");
				location_tracking.setLongitude(lng + "");
				location_tracking.setLogtime(locationLogTime + "");
				location_tracking.setNetwork_state(getApplicationContext()
						.getResources().getString(R.string.offline));
				location_tracking.setBattery_label(battary_label);
				location_tracking.setCharging_state(charging_state);
				location_tracking.setGps_state(gps_state);

				new ApplicationLogging(getApplicationContext(),
						location_tracking).execute();
			}

			// create instance of DBInteraction class object
			DBInteraction dbInteraction = DBInteraction
					.getInstance(ApplicationConstant.currentActivityContext);
			dbInteraction.getConnection();
			try {
				Log.i(TAG,"dbInteraction.addTrackingData");
				// save tracking location local in database
				dbInteraction.addTrackingData(String.valueOf((double) latt),
						String.valueOf((double) lng), locationLogTime,
						battary_label, charging_state, gps_state,
						is_power_saving_mode_on);

			} catch (Exception e) {
				//Google Analytic -Tracking Exception
				EosApplication.getInstance().trackException(e);
				// save location when error is create
				UtilityFunction.saveErrorLog(getApplicationContext(), e);

			} finally {
				stopSelf();

			}

		}

		return Service.START_STICKY;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public static String getBatteryPercentage(Context c) {

		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = c.registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		return String.valueOf(level);
	}

	public static String getBatteryChargingStatus(Context context) {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		String isCharging = "";

		int chargePlug = batteryStatus.getIntExtra(
				BatteryManager.EXTRA_PLUGGED, -1);
		// Are we charging / charged?
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging_charger = status == BatteryManager.BATTERY_STATUS_CHARGING
				|| status == BatteryManager.BATTERY_STATUS_FULL;

		boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

		if (acCharge == true || usbCharge == true || isCharging_charger == true) {
			isCharging = "true";
		} else {
			isCharging = "false";
		}
		return isCharging;
	}

	/**
	 * Check GPS Status ON / OFF
	 *
	 * @param c
	 * @return
	 */
	public static String check_gps_status(Context c) {
		final LocationManager manager = (LocationManager) c
				.getSystemService(Context.LOCATION_SERVICE);

		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			return "false";
		else
			return "true";
	}

}
