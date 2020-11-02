package com.teramatrix.vos.asynctasks;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.R;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.utils.UtilityFunction;
import com.teramatrix.vos.volvouptime.UpTimeVehicleListActivity;

/**
 * 
 * 
 * @author Gaurav.Mangal
 * 
 *         AsynTask class for ConfigurationPin device(Token,DeviceAlias
 *         DevicePin) on server. API call return Device Alias for registered
 *         Licence number,and Security Token. Device Alias ,setImeiNumber
 *         are used for all other API call so need to store locally.
 *         
 *         If registration Pin api returns success status we check is Local db  already setup and synchronized .
 *         if yes  move to Home Screen else proceed to setup local db and initialy synchronize data from server.
 */

public class ConfigurationPin extends AsyncTask<Void, Void, Void> {

	private Context mContext;
	private String pin_password, securityToken, str_deviceAlias, response,
			status, message;
	private ProgressDialog mProgressDialog;
	private RestIntraction restIntraction;
	private JSONObject jsonObject;
	private VECVPreferences vecvPreferences;

	public ConfigurationPin(Context context, String token, String device_alias,
			String pinPassword) {
		mContext = context;
		securityToken = token;
		str_deviceAlias = device_alias;
		pin_password = pinPassword;
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
		mProgressDialog = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.configuring_pin),
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
			vecvPreferences = new VECVPreferences(mContext);
			restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS()+""+ApiUrls.CONFIGURATION_PIN);
			//restIntraction = new RestIntraction("http://10.10.1.100:9093/"+""+ApiUrls.CONFIGURATION_PIN);
			restIntraction.AddParam("Token", securityToken);
			restIntraction.AddParam("DeviceAlias", str_deviceAlias);
			restIntraction.AddParam("DevicePin", pin_password);
			restIntraction.Execute(1);
		} catch (Exception ex) {
			
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(ex);
			//save error location in file
			UtilityFunction.saveErrorLog(mContext, ex);
			
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object) Method is used
	 * to perform UI operation after background task will be finishing.
	 */
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		mProgressDialog.dismiss();
		// get response from the service
		response = restIntraction.getResponse();
/**
 * code to differ the uptime and van app basis of user type get in first time.
 */
		if (response != null) {
			try {
				jsonObject = new JSONObject(response);
				status = jsonObject.getString("Status");
				message = jsonObject.getString("Massage");
				System.out.println("Response Json:" + jsonObject);
				if (status.equals("1")) {
					vecvPreferences.setCheckLogin(true);
					vecvPreferences.setPinPassword(pin_password);
					String userType =  vecvPreferences.getUserType();

					if(userType.equalsIgnoreCase("0")) {
						//For Volvo Servcie Van app
						if (vecvPreferences.isTicketStatusTableExistsInDB()) {
							//Open MyTicketActivity (For Volvo Servcie Van app)
							Intent myTicket_Intent = new Intent(mContext,
									MyTicketActivity.class);
							myTicket_Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							mContext.startActivity(myTicket_Intent);
							((Activity) mContext).finish();

						}else
						{
							/**
							 * This module will fetch configuration data
							 * (Ticket Status Table,Decline reason Table ,estimation Cost table)
							 *  from server and store locally in app database.
							 */
							// get all Ticket status ,Reasons data from server
							new GetTicketStatusAndReasonsList(mContext,false).execute();
						}
					}else if(userType.equalsIgnoreCase("1")){
						//For Volvo Uptime app
						//Open UpTimeVehicleLsitActivity (For Volvo Uptime app)
						Intent myTicket_Intent = new Intent(mContext,
								UpTimeVehicleListActivity.class);
						myTicket_Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						myTicket_Intent.putExtra("isFromLoginPage",true);
						mContext.startActivity(myTicket_Intent);
						((Activity) mContext).finish();
					}
				} else {
					UtilityFunction.showMessage(mContext, message);
				}
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(mContext, e);
			}
		}
	}
}
