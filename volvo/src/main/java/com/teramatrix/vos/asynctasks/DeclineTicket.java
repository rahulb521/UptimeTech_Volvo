package com.teramatrix.vos.asynctasks;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.R;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.utils.UtilityFunction;

/**
 * This DeclineTicket class is used for decline ticket send operations in background and passing isDecline true.
 * @author Gaurav.Mangal
 *
 */
public class DeclineTicket extends AsyncTask<Void, Void, Void>{

	// Define Context for this class
	private Context mContext;
	
	// Define String variables for this class 
	private String securityToken,assignedTo,isDecline,ticket_id,LastModifiedBy,LastModifiedtime,response, status, message;
	
	// Define ProgressDialog for this class
	private ProgressDialog mProgressDialog;
	
	// Define RestIntraction for this class
	private RestIntraction restIntraction;
	
	// Define JsonObject for this class
	private JSONObject jsonObject;
	
	// Define VECVPreferences for this class
	private VECVPreferences vecvPreferences;
	
	
	/**
	 * 
	 * @param context
	 * @param token
	 * @param assigned_To
	 * @param is_Decline
	 * @param ticket_Id
	 * @param last_modifiedBy
	 * @param last_ModifiedTime
	 */
	public DeclineTicket(Context context, String token,String assigned_To,String is_Decline,String ticket_Id,String last_modifiedBy,String last_ModifiedTime){
		mContext = context;
		securityToken = token;
		assignedTo = assigned_To;
		isDecline = is_Decline;
		this.ticket_id=ticket_Id;
		LastModifiedBy=last_modifiedBy;
		LastModifiedtime=last_ModifiedTime;
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
	protected Void doInBackground(Void... params) 
	{
		try {
			vecvPreferences = new VECVPreferences(mContext);
			restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS()+""+ApiUrls.TICKET_DECLINE);
			restIntraction.AddParam("Token", securityToken);
			restIntraction.AddParam("isDeclined", isDecline);
			restIntraction.AddParam("Id", ticket_id);
			restIntraction.AddParam("LastModifiedBy", LastModifiedBy);
			restIntraction.AddParam("LastModifiedTime", LastModifiedtime);
			restIntraction.AddParam("AssignedTo", assignedTo);
			restIntraction.Execute(1);
		} catch (Exception ex) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(ex);
			//save error location in file
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
			try 
			{
				 JSONArray jsonarray = new JSONArray(response);
				 System.out.println("JSON ARRAY :"+jsonarray);
				  if (jsonarray.length()!=0) 
				  {
					// get JsonObject from the response
					    JSONObject jsonobject =  jsonarray.getJSONObject(0);
						System.out.println("JSON Object 0 POS :"+jsonobject);
					    
				  }
				  else
				  {
					  
				  }
			} 
			catch (Exception e) 
			{
			
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(mContext, e);
			}
		}
	}
}
