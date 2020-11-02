package com.teramatrix.vos.asynctasks;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.BuildConfig;
import android.support.v13.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.teramatrix.vos.ConfigurationLicenseActivity;
import com.teramatrix.vos.ConfigurationPinActivity;
import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.R;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.service.Locationservice;
import com.teramatrix.vos.service.PostLocationService;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.TimeFormater;
import com.teramatrix.vos.utils.UpdateLicenseModel;
import com.teramatrix.vos.utils.UtilityFunction;

import java.util.concurrent.ExecutionException;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

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


	double lati;
	double longi;




	/**
	 * @param context
	 * @param
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
			//restIntraction.AddParam("Imei", "30751599569498769");
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

		//Lat long
		// get SharedPreferences of an application
		SharedPreferences preference =mContext.getApplicationContext()
				.getSharedPreferences(ApplicationConstant.PREFERENCE_NAME,
						Activity.MODE_PRIVATE);

		// get updated location
		lati = preference.getFloat(ApplicationConstant.KEY_LATT, 0);
		longi = preference.getFloat(ApplicationConstant.KEY_LONG, 0);

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




                      //application version
					Integer versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
					//device Model name
					String modelName = Build.MODEL;
					// device os version
					String osVersion = Build.VERSION.RELEASE;
					 if(lati==0 && longi==0) {
						 UtilityFunction.showMessage(mContext, "Try again");
						 return;
					 }
					setUpdateJsonInfo(versionCode,modelName, osVersion,lati,longi);

					// Open Configuration Pin Activity
					/*vecvPreferences.setCheckconfigure(true);
					Intent mainIntent = new Intent(mContext,
							ConfigurationPinActivity.class);
					mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					mContext.startActivity(mainIntent);
					// Close ConfigurationLicence activity/Screen
					((Activity) mContext).finish();

					 */

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

	private void setUpdateJsonInfo(Integer versionCode, String modelName, String osVersion, double latt, double lng) {

		UpdateLicenseModel updateLicenseModel = new UpdateLicenseModel();

		updateLicenseModel.setApplicationLicenceKey(licenseNumber);
		updateLicenseModel.setCreatedby(gcm_registration_id);
		updateLicenseModel.setCreatedDate(UtilityFunction.currentUTCTimeWithoutDash());
		updateLicenseModel.setDeviceAlias(device_alias);
		updateLicenseModel.setEventTime(UtilityFunction.currentUTCTimeWithoutDash());
		updateLicenseModel.setLatitude( String.valueOf(lati) );
		updateLicenseModel.setLongitude(String.valueOf(longi));
		updateLicenseModel.setOsVersion(osVersion);
		updateLicenseModel.setPhoneModel(modelName);
		updateLicenseModel.setApplication_version(versionCode.toString());
		updateLicenseModel.setToken(securityToken);


		try {

			Gson gson = new Gson();
			String json = gson.toJson(updateLicenseModel);
			JSONObject jsonObject = new JSONObject(json);
			Log.i("UpdateInfo",json);



			// update the license info, get the details ad call api

			 new UpdateLicenseInfo(mContext,jsonObject).execute().get();

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}


	}



	private class UpdateLicenseInfo extends AsyncTask<String, String, String> {

		JSONObject jsonObject;

		public UpdateLicenseInfo(Context mContext, JSONObject jsonObject) {
			this.jsonObject = jsonObject;
		}

		protected void onPreExecute() {



		}

		@Override
		protected String doInBackground(String... urls) {
			try {
				// create an object for the VECVPreferences class
				vecvPreferences = new VECVPreferences(mContext);
				// create an object for the RestIntraction class
				restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS()+""+
						ApiUrls.UPDATE_LICENSE);

				restIntraction.AddJson("", jsonObject);

				// call post request
				restIntraction.Execute(3);
				response = restIntraction.getResponse();
			} catch (Exception ex) {
				//Google Analytic -Tracking Exception
				EosApplication.getInstance().trackException(ex);
				// save error location in file
				UtilityFunction.saveErrorLog(mContext, ex);
			}
			return null;
		}



		protected void onPostExecute(String result) {
			try {
//				String resultout = result.trim();
//				Log.d("Response", resultout);
//				JSONObject jsonObject = new JSONObject(resultout);
				//if(result.equals(1))

				// Open Configuration Pin Activity
					vecvPreferences.setCheckconfigure(true);
					Intent mainIntent = new Intent(mContext,
							ConfigurationPinActivity.class);
					mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					mContext.startActivity(mainIntent);
					// Close ConfigurationLicence activity/Screen
					((Activity) mContext).finish();




			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


}