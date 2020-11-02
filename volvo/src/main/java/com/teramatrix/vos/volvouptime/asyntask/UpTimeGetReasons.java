package com.teramatrix.vos.volvouptime.asyntask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.activeandroid.query.Select;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.utils.UtilityFunction;
import com.teramatrix.vos.volvouptime.models.UpTimeReasonsModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Arun.Singh on 5/25/2018.
 * This Asyn Task is used to fetch all Reasons data from server and save them in Local DB (Active android Model)
 */
public class UpTimeGetReasons extends AsyncTask<Void, Void, Void> {

	// Define Context for this class
	private Context mContext;
	// Define String variables for this class
	private String securityToken, response;
	// Define ProgressDialog for this class
	private ProgressDialog mProgressDialog;
	//Interface reference for returning data to caller
	private I_UpTimeGetReasons i_upTimeGetVehicles;

	/**
	 *
	 * @param context
	 * @param token
	 */

	public UpTimeGetReasons(Context context, String token, I_UpTimeGetReasons i_upTimeGetVehicles) {
		mContext = context;
		securityToken = token;

		this.i_upTimeGetVehicles = i_upTimeGetVehicles;
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
		mProgressDialog = ProgressDialog.show(mContext, "","Loading Reasons data..",
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

			// Get Volvo Uptime Vehicles data from API
			RestIntraction restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS() + ""
					+ ApiUrls.UPTIME_GET_REASONS);
			restIntraction.AddParam("Token", securityToken);
			restIntraction.Execute(1);

			Log.i("Request",restIntraction.toString());
			response = restIntraction.getResponse();
			Log.i("response",response);

			if (response != null) {
				try {

					JSONObject jsonObject = new JSONObject(response);
					if(jsonObject.getString("Status").equalsIgnoreCase("1"))
					{
						JSONArray jsonArray = jsonObject.getJSONArray("ServiceReasonsList");
						if(jsonArray.length()>0)
						{
							for(int i =0;i<jsonArray.length();i++)
							{
								JSONObject jsonObject1 = jsonArray.getJSONObject(i);
								String _service_type_id = jsonObject1.getString("_service_type_id");
								String _reason_id = jsonObject1.getString("_reason_id");
								String _service_name = jsonObject1.getString("_service_name");
								String _service_alias = jsonObject1.getString("_service_alias");
								String _service_type_sequence_number = jsonObject1.getString("_service_type_sequence_number");
								String _reason_name = jsonObject1.getString("_reason_name");
								String _reason_alias = jsonObject1.getString("_reason_alias");
								String _reason_sequence_number = jsonObject1.getString("_reason_sequence_number");
								String _service_type_is_active = jsonObject1.getString("_service_type_is_active");
								String _service_type_is_deleted = jsonObject1.getString("_service_type_is_deleted");
								String _is_active = jsonObject1.getString("_is_active");
								String _is_deleted = jsonObject1.getString("_is_deleted");

								UpTimeReasonsModel upTimeReasonsModel =new Select()
										.from(UpTimeReasonsModel.class)
										.where("ReasonId = ?",_reason_id)
										.executeSingle();

								if(upTimeReasonsModel==null)
									upTimeReasonsModel =  new UpTimeReasonsModel();

								upTimeReasonsModel.set_is_active(_is_active);
								upTimeReasonsModel.set_service_type_id(_service_type_id);
								upTimeReasonsModel.set_reason_id(_reason_id);
								upTimeReasonsModel.set_service_name(_service_name);
								upTimeReasonsModel.set_service_alias(_service_alias);
								upTimeReasonsModel.set_service_type_sequence_number(_service_type_sequence_number);
								upTimeReasonsModel.set_reason_name(_reason_name);
								upTimeReasonsModel.set_reason_alias(_reason_alias);
								upTimeReasonsModel.set_reason_sequence_number(_reason_sequence_number);
								upTimeReasonsModel.set_service_type_is_active(_service_type_is_active);
								upTimeReasonsModel.set_service_type_is_deleted(_service_type_is_deleted);
								upTimeReasonsModel.set_is_deleted(_is_deleted);
								upTimeReasonsModel.save();

							}
						}
					}



				} catch (Exception e) {
					// Google Analytic -Tracking Exception
					//EosApplication.getInstance().trackException(e);
					// save error location in file
					UtilityFunction.saveErrorLog(mContext, e);
				}
			}

		} catch (Exception ex) {

			// Google Analytic -Tracking Exception
			//EosApplication.getInstance().trackException(ex);
			UtilityFunction.saveErrorLog(mContext, ex);
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
		super.onPostExecute(result);
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();

		//Get list of rewsons from Local DB
		List<UpTimeReasonsModel> upTimeReasonsModels  = new Select()
				.from(UpTimeReasonsModel.class)
				.execute();
		//Provide data to caller
		i_upTimeGetVehicles.reasonsList(upTimeReasonsModels);
	}


	/**
	 * Interface to be implemented by class which called this asyn task.
	 */
	public interface I_UpTimeGetReasons
	{
		//Method to provide Lsit of Reason model.
		void reasonsList(List<UpTimeReasonsModel> upTimeReasonsModels);
	}

}
