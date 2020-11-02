package com.teramatrix.vos.asynctasks;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.logs.CompleteLogging;
import com.teramatrix.vos.model.TrackingLocation;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.reciver.InternetAvailabilityRecever;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.UtilityFunction;

/**
 * This class will be used for upload offline tracking location data send on
 * server when internet is on.
 * 
 */
public class UploadOfflineTrackingData extends AsyncTask<Void, Void, Void> {

	// define Context instance of the class
	private Context context;

	// define ArrayList instace of the class
	private ArrayList<TrackingLocation> offlineData;

	// define RestIntraction instace of the class
	private RestIntraction restIntraction;

	// define JSONObject instace of the class
	private JSONObject jsonObject;

	// define String variable of the class
	private String pin_password, securityToken, str_deviceAlias, response,
			status, message;

	// define InternetAvailabilityRecever instace of the class
	private InternetAvailabilityRecever receiver;

	/**
	 * 
	 * @param receiver
	 * @param context
	 * @param offlineData
	 * @param token
	 * @param device_alias
	 */
	public UploadOfflineTrackingData(InternetAvailabilityRecever receiver,
			Context context, ArrayList<TrackingLocation> offlineData,
			String token, String device_alias) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.offlineData = offlineData;
		securityToken = token;
		str_deviceAlias = device_alias;
		this.receiver = receiver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */

	@Override
	protected Void doInBackground(Void... params) {

		try {

			if (ApplicationConstant.IS_LOG_SHOWN) {

				String log_message = "Internet Available Uploading Offline Tracking Data";
				CompleteLogging.logNetworkState(context, log_message);
			}

			restIntraction = new RestIntraction(new VECVPreferences(context).getAPIEndPoint_EOS()+""+
					ApiUrls.BULK_UPLOAD_OFFLINE_LOCATION);
			restIntraction.AddParam("Token", securityToken);

			for (TrackingLocation trackingLocation : offlineData) {
				String parm_value = str_deviceAlias + ","
						+ trackingLocation.getLatitude() + ","
						+ trackingLocation.getLongitude() + ","
						+ trackingLocation.getLogtime() + ","
						+ trackingLocation.getBattery_label() + ","
						+ trackingLocation.getGps_state() + ","
						+ trackingLocation.getBattery_charging()+","+trackingLocation.getIs_power_saving_mode_on();

				restIntraction.AddParam("BulkTrackngDetail", parm_value);
			}
			restIntraction.Execute(1);
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
	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		// delete offline Location data from local db
		try {
			// get response from server
//			String response = restIntraction.getResponse();
			// delete offline location data
			receiver.deletOffLineLocationData();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			// save error location in file
			UtilityFunction.saveErrorLog(context, e);

		}

	}
}
