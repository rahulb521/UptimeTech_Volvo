package com.teramatrix.vos.asynctasks;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.WindowManager;
import android.widget.Toast;

import com.teramatrix.vos.ConfigurationLicenseActivity;
import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.MyDialog;
import com.teramatrix.vos.R;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.utils.UtilityFunction;

/*
 * This class used for sending updated location on server
 */
public class SendLocationUpdateToServer extends AsyncTask<Void, Void, Void> {

	// Define Context for this class
	private Context context;

	// define double variable
	private double latitude, longitude;

	// define string variable locationLogTime
	String locationLogTime;

	// define instance of the RestIntraction class
	private RestIntraction restIntraction;

	// define instance of the VECVPreferences class
	private VECVPreferences vecvPreferences;

	// define string variable
	private String response, status, message, battery_status, is_gps_enabled,
			is_charging, is_power_saving_mode_on;

	// define JSONObject object of the class
	private JSONObject jsonObject;

	private boolean isActivityShow = true;

	// define Service instance of the class
	private Service service;

	/**
	 * 
	 * @param service
	 * @param context
	 * @param latitude
	 * @param longitude
	 * @param locationLogTime
	 */
	public SendLocationUpdateToServer(Service service, Context context,
			double latitude, double longitude, String locationLogTime,
			String battery_status, String is_gps_enabled, String is_charging,
			String is_power_saving_mode_on) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.latitude = latitude;
		this.longitude = longitude;
		this.locationLogTime = locationLogTime;
		this.service = service;
		this.battery_status = battery_status;
		this.is_gps_enabled = is_gps_enabled;
		this.is_charging = is_charging;
		this.is_power_saving_mode_on = is_power_saving_mode_on;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */

	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub

		// call Api
		try {

			vecvPreferences = new VECVPreferences(context);
			restIntraction = new RestIntraction(new VECVPreferences(context).getAPIEndPoint_EOS()+""+
					ApiUrls.INSERT_TRACKING_LOCATION+"IMEI_Number="+vecvPreferences.getImeiNumber());



			restIntraction
					.AddParam("Token", vecvPreferences.getSecurityToken());
			restIntraction.AddParam("DeviceAlias",
					vecvPreferences.getDevice_alias());
			restIntraction.AddParam("Latitude", String.valueOf(latitude));
			restIntraction.AddParam("Longitude", String.valueOf(longitude));
			restIntraction.AddParam("LogTime", locationLogTime);
			restIntraction.AddParam("BatteryStatus", battery_status);
			restIntraction.AddParam("GpsStatus", is_gps_enabled);
			restIntraction.AddParam("IsCharging", is_charging);
			restIntraction.AddParam("IsPowerSaving", is_power_saving_mode_on);

			restIntraction.Execute(1);
			
			System.out.println("Location Tracking Debug: Location Uploaded on Server Latitude:"+latitude+" "+" longitude: "+longitude+" locationLogTime:"+locationLogTime);
		} catch (Exception ex) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(ex);
			// save error location in file
			UtilityFunction.saveErrorLog(context, ex);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */

	protected void onPostExecute(Void result) {

		// get response from the service
		response = restIntraction.getResponse();
		restIntraction = null;
		if (response != null) {
			try {

				jsonObject = new JSONObject(response);
				String response = "Response status:" + jsonObject;
				System.out.println("Response status:" + jsonObject);
				String status = jsonObject.getString("status");
				boolean userLogout = jsonObject.getBoolean("UserLogout");
//				if(!userLogout){
//						vecvPreferences.setCheckLogin(false);
//						vecvPreferences.setCheckconfigure(false);
//						vecvPreferences.setImeiNumber(null);
//						Intent dialogIntent = new Intent(context, ConfigurationLicenseActivity.class);
//						dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//						context.startActivity(dialogIntent);
//				}
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		service.stopSelf();
	};
}
