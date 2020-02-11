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
import com.teramatrix.vos.volvouptime.UpTimeRegisterActivity;
import com.teramatrix.vos.volvouptime.models.UpTimeReasonsModel;
import com.teramatrix.vos.volvouptime.models.UpTimeTicketDetailModel;
import com.teramatrix.vos.volvouptime.models.UpTimeTicketDetailModelActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Arun.Singh on 5/25/2018.
 * This AsynTask calls API to fetch TicketID from a UUID of a Ticket.
 * 
 */
public class UpTimeGetTicketId extends AsyncTask<Void, Void, Void> {

	private Context mContext;
	private String securityToken, response;
	private ProgressDialog mProgressDialog;
	private I_UpTimeGetTicketId i_upTimeGetTicketId;
	private List<UpTimeTicketDetailModelActivity> upTimeTicketDetailModelActivities;
	private String[][] uuid_mapping;
	/**
	 *
	 * @param context
	 * @param token
	 */

	public UpTimeGetTicketId(Context context, String token, List<UpTimeTicketDetailModelActivity> upTimeTicketDetailModelActivities, I_UpTimeGetTicketId i_upTimeGetTicketId) {
		mContext = context;
		securityToken = token;
		this.i_upTimeGetTicketId = i_upTimeGetTicketId;
		this.upTimeTicketDetailModelActivities = upTimeTicketDetailModelActivities;
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
		/*mProgressDialog = ProgressDialog.show(mContext, "","Loading..",
					false);*/
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

			String licenceKey = new VECVPreferences(mContext).getLicenseKey();
			//Prepare Json Data to be sent as request parameter
			JSONArray jsonArray = new JSONArray();
			for(UpTimeTicketDetailModelActivity upTimeTicketDetailModelActivity:upTimeTicketDetailModelActivities)
			{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("Description",upTimeTicketDetailModelActivity.getJobComment());
				jsonObject.put("TicketStatus",upTimeTicketDetailModelActivity.getSequenceOrder());
				jsonObject.put("VehicleRegistrationNumber",upTimeTicketDetailModelActivity.getVehicleRegistrationNumber());
				jsonObject.put("StartDate",upTimeTicketDetailModelActivity.getStartDate());
				jsonObject.put("EndDate",upTimeTicketDetailModelActivity.getEndDate());
				jsonObject.put("AssignedToUserId",licenceKey);
				jsonObject.put("TicketUUID",upTimeTicketDetailModelActivity.getUuid());
				jsonArray.put(jsonObject);
			}


			// Get Volvo Uptime Vehicles data from API
			RestIntraction restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS() + ""
					+ ApiUrls.UPTIME_GET_TICKET_ID_FROM_UUID);
			restIntraction.AddParam("Token", securityToken);
			restIntraction.AddParam("BulkTicketStr", jsonArray.toString());
			restIntraction.Execute(1);

			Log.i("Request",restIntraction.toString());
			response = restIntraction.getResponse();
			Log.i("response",response);

			if (response != null) {
				try {

					JSONObject jsonObject = new JSONObject(response);
					if(jsonObject.getString("Status").equalsIgnoreCase("1"))
					{
						JSONArray jsonArray1 = jsonObject.getJSONArray("objOfflineTicketList");
						if(jsonArray1.length()>0) {
							uuid_mapping = new String[jsonArray1.length()][2];

							for(int i=0;i<jsonArray1.length();i++)
							{
								JSONObject jsonObject1 = jsonArray1.getJSONObject(i);
								String TicketUUID =  jsonObject1.getString("TicketUUID");
								String TicketId =  jsonObject1.getString("TicketId");
								uuid_mapping[i][0] = TicketUUID;
								uuid_mapping[i][1] = TicketId;
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

		//Provide data to caller
		i_upTimeGetTicketId.ticketWithOriginalID(uuid_mapping);
	}

	/**
	 * Interface to be implemented by a class which called this Asyn Task.
	 */
	public interface I_UpTimeGetTicketId
	{
		void ticketWithOriginalID(String[][] uuid_mapping);
	}

}
