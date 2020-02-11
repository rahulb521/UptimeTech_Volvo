package com.teramatrix.vos.asynctasks;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.teramatrix.vos.ConfigurationPinActivity;
import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.R;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.UtilityFunction;

/**
 * 
 * 
 * @author Gaurav.Mangal
 * 
 *         AsynTask class for ConfigurationLicense device(Licence number,Imei
 *         number,GCM ID) on server. API call return Device Alias for registered
 *         Licence number,and Security Token. Device Alias ,setImeiNumber are
 *         used for all other API call so need to store locally.
 * 
 *         If Api response success,save configuration details on device else
 *         show failure pop-up message. save security token, device
 *         alias,licence number,imei number locally in shared preference
 * 
 */
public class ConfigurationLicense extends AsyncTask<Void, Void, Void> {

	// Defining Context for this class
	private Context mContext;

	// Defining String variables for this class
	private String gcm_registration_id, licenseNumber, securityToken,
			device_alias, imeiNumber, response, status, message;

	// Defining ProgressDialog for this class
	private ProgressDialog mProgressDialog;
	// Defining RestIntraction class
	private RestIntraction restIntraction;

	// Defining JSONObject class
	private JSONObject jsonObject;
	// Defining VECV Preferences class
	private VECVPreferences vecvPreferences;
	private boolean isGCM_id_update;

	/**
	 * @param context
	 * @param licenseNum0
	 * @param imeiNum
	 * @param gcmDeviceid
	 * 
	 *            Pass all the @params in constructor, as it it required for
	 *            Service API.
	 */
	public ConfigurationLicense(Context context, String licenseNum,
			String imeiNum, String gcmDeviceid) {
		mContext = context;
		licenseNumber = licenseNum;
		imeiNumber = imeiNum;
		gcm_registration_id = gcmDeviceid;
	}

	public ConfigurationLicense(Context context, String licenseNum,
			String imeiNum, String gcmDeviceid, boolean isGCM_id_update) {
		mContext = context;
		licenseNumber = licenseNum;
		imeiNumber = imeiNum;
		gcm_registration_id = gcmDeviceid;
		this.isGCM_id_update = isGCM_id_update;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute(),Method is used to perform UI
	 * operation before starting background Service
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		if (isGCM_id_update) {
			mProgressDialog = ProgressDialog.show(mContext, "",
					"Updating GCM ID..", false);
		} else {
			mProgressDialog = ProgressDialog.show(mContext, "", mContext
					.getResources().getString(R.string.configuring_license),
					false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[]), Method is used to
	 * perform Background Task.
	 */
	@Override
	protected Void doInBackground(Void... params) {
		try {
			// create an object for the VECVPreferences class
			vecvPreferences = new VECVPreferences(mContext);
			// create an object for the RestIntraction class
			restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS()+""+
					ApiUrls.DEVICE_CONFIGURATION_LICENSE);
			// Call Configuration API with these parameters
			System.out.println("ConfigurationLicense.doInBackground() licenseNumber:"+licenseNumber+" imeiNumber:"+imeiNumber+" gcm_registration_id:"+gcm_registration_id);
			restIntraction.AddParam("token", "teramatrix");
			restIntraction.AddParam("RegistrationNo", licenseNumber);
			restIntraction.AddParam("Imei", imeiNumber);
			restIntraction.AddParam("DeviceGcmId", gcm_registration_id);
			restIntraction.AddParam("PushNotificationBit","1");
			// call post request
			restIntraction.Execute(1);
			response = restIntraction.getResponse();
		} catch (Exception ex) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(ex);
			// save error location in file
			UtilityFunction.saveErrorLog(mContext, ex);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object), Method is used
	 * to perform UI operation after background task will be finishing.
	 */
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		mProgressDialog.dismiss();
		// get response from the service
		if (ApplicationConstant.IS_LOG_SHOWN)
			System.out.println("Response is:" + response);

		if (isGCM_id_update) {
			// Do nothing
			Intent myTicket_Intent = new Intent(mContext, MyTicketActivity.class);
			myTicket_Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			mContext.startActivity(myTicket_Intent);
			((Activity) mContext).finish();
		} else if (response != null) {
			try {
				// get JsonObject from the response
				jsonObject = new JSONObject(response);
				// get JsonObject value by passing String key.
				status = jsonObject.getString("Status");
				message = jsonObject.getString("Massage");
				securityToken = jsonObject.getString("Token");
				device_alias = jsonObject.getString("DeviceAlias");
				String UserType = jsonObject.getString("UserType");
				String SiteId = jsonObject.getString("SiteId");

				// if status is "1" then call ConfigurationPin Activity
				if (status.equals("1")) {

					// Save Configuration Details in shared Preference
					vecvPreferences.setSecurityToken(securityToken);
					vecvPreferences.setDevice_alias(device_alias);
					vecvPreferences.setLicenseKey(licenseNumber);
					vecvPreferences.setImeiNumber(imeiNumber);
					vecvPreferences.setUserType(UserType);
					vecvPreferences.setSiteId(SiteId);
					vecvPreferences.setGcmID(gcm_registration_id);
					// Open Configuration Pin Activity
					vecvPreferences.setCheckconfigure(true);
					Intent mainIntent = new Intent(mContext,
							ConfigurationPinActivity.class);
					mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					mContext.startActivity(mainIntent);
					// Close ConfigurationLicence activity/Screen
					((Activity) mContext).finish();

				} else {
					UtilityFunction.showMessage(mContext, message);
				}
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				e.printStackTrace();
			}
		}
	}
}