package com.teramatrix.vos.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class AppVersionChekerAsyn extends AsyncTask<Void, Void, Void> {

	// Defining Context for this class
	private Context mContext;

	// Defining ProgressDialog for this class
	private ProgressDialog mProgressDialog;
	// Defining RestIntraction class
	private RestIntraction restIntraction;
	private String response,app_code;
	private I_AppVersionChekerAsyn i_appVersionChekerAsyn;



	public AppVersionChekerAsyn(Context context, String app_code, I_AppVersionChekerAsyn i_appVersionChekerAsyn)
	{
		mContext = context;
		this.app_code = app_code;
		this.i_appVersionChekerAsyn = i_appVersionChekerAsyn;
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

		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setMessage("Cheking version update..");
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
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

			restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS() + ApiUrls.APP_VERSION_UPDATE);
			restIntraction.AddParam("Token", "teramatrix");
			restIntraction.AddParam("AppCode", app_code);
			restIntraction.Execute(1);
			// get response from the service
			response = restIntraction.getResponse();
			Log.i("AppVersionChekerAsyn",response);
		} catch (Exception ex) {
			ex.printStackTrace();
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
		if(mContext!=null && mProgressDialog!=null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();

		if(response!=null && response.length()>0)
		{
			try {
				JSONArray jsonArray = new JSONArray(response);
				if(jsonArray.length()>0)
				{
					JSONObject jsonObject = jsonArray.getJSONObject(0);
					if(jsonObject.has("AppVersion"))
					{
						String playStoreVersion = jsonObject.getString("AppVersion");
						if(i_appVersionChekerAsyn!=null)
						{
							i_appVersionChekerAsyn.I_AppVersionChekerAsyn_onSuccess(playStoreVersion);
							return;
						}

					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		i_appVersionChekerAsyn.I_AppVersionChekerAsyn_onFailure("");

	}

	public static interface I_AppVersionChekerAsyn
	{
		void I_AppVersionChekerAsyn_onSuccess(String playStoreVersionName);
		void I_AppVersionChekerAsyn_onFailure(String message);
	}

}