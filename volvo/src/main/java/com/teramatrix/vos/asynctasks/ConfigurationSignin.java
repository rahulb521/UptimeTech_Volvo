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


public class ConfigurationSignin extends AsyncTask<Void, Void, Void>
{

	private Context mContext;
	private String pin_password,securityToken,str_deviceAlias, response, status, message;
	private ProgressDialog mProgressDialog;
	private RestIntraction restIntraction;
	private JSONObject jsonObject;
	private VECVPreferences vecvPreferences;
	
	public ConfigurationSignin(Context context, String token, String device_alias, String pinPassword){
		mContext = context;
		securityToken = token;
		str_deviceAlias = device_alias;
		pin_password = pinPassword;
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute(), Method is used to perform UI operation
	 * before starting background Service
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.please_wait), false);
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[]) Method is used to perform
	 * Background Task.
	 */
	@Override
	protected Void doInBackground(Void... params) {
		try {
			vecvPreferences = new VECVPreferences(mContext);
			restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS()+""+ApiUrls.CONFIGURATION_SIGN_IN_PIN);
			restIntraction.AddParam("Token", securityToken);
			restIntraction.AddParam("DeviceAlias", str_deviceAlias);
			restIntraction.AddParam("DevicePin", pin_password);
			restIntraction.Execute(1);
		} catch (Exception ex) {
			
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(ex);
			UtilityFunction.saveErrorLog(mContext, ex);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object) Method is used to perform
	 * UI operation after background task will be finishing.
	 */
	@Override
	protected void onPostExecute(Void result) 
	{
		super.onPostExecute(result);
		mProgressDialog.dismiss();
		//get response from the service
		response = restIntraction.getResponse();
		
		if (response != null) {
			try {
				jsonObject = new JSONObject(response);
				status = jsonObject.getString("Status");
				message = jsonObject.getString("Massage");
				System.out.println("Response Json:"+jsonObject);
				if (status.equals("1")) 
				{
					vecvPreferences.setCheckLogin(true);
					vecvPreferences.setPinPassword(pin_password);
					//Intent myTicket_Intent = new Intent(mContext,MyTicketActivity.class);
					Intent myTicket_Intent = new Intent(mContext,UpTimeVehicleListActivity.class);
					myTicket_Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					mContext.startActivity(myTicket_Intent);
					((Activity) mContext).finish();
				}
				else
				{
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
