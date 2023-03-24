package com.teramatrix.vos.volvouptime.asyntask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.activeandroid.query.Select;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.checkinternet.CheckInternetConnection;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.utils.TimeFormater;
import com.teramatrix.vos.utils.UtilityFunction;
import com.teramatrix.vos.volvouptime.UpTimeRegisterActivity;
import com.teramatrix.vos.volvouptime.custom.DAO;
import com.teramatrix.vos.volvouptime.custom.TimePickerUtil;
import com.teramatrix.vos.volvouptime.models.UpTimeAddedReasonsModel;
import com.teramatrix.vos.volvouptime.models.UpTimeAddedReasonsModelActivity;
import com.teramatrix.vos.volvouptime.models.UpTimeReasonsModel;
import com.teramatrix.vos.volvouptime.models.UpTimeTicketDetailModel;
import com.teramatrix.vos.volvouptime.models.UpTimeTicketDetailModelActivity;
import com.teramatrix.vos.volvouptime.models.VehicleModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arun.Singh on 5/25/2018.
 * This Asyn task is used to Post data to server (Create New Ticket/Update Ticket Time/Close Ticket/ Add new Reason/ Update Reason Time)
 * 
 */
public class UpTimeUpdateTicket extends AsyncTask<Void, Void, Void> {

	// Define Context for this class
	String TAG = this.getClass().getSimpleName();
	private Context mContext;
	// Define String variables for this class
	private String securityToken,response;
	private String description,
			reasonId,
			vehicleRegNo,
			startTime,
			endTime,
			licenceNo,
			requestType,
			TicketId,
			DelayedReasonId,
			isTicketClosed,
			UUID,
			InreasonUniqueId,
			delayedReasonComment;

	//causal part string
	String causalpart;
	String enginehour;

	// Define ProgressDialog for this class
	private ProgressDialog mProgressDialog;
	//Interface reference for returning data to caller
	private I_UpTimeUpdateTicket i_upTimeUpdateTicket;

	private ArrayList<VehicleModel> vehicleModels = new ArrayList<>();


	private boolean isUpdateSuccessful;

	/**
	 * Constructor of this class
	 * @param context context of caller activity/Application
	 * @param requestModel request model containing parameters for request
	 * @param i_upTimeUpdateTicket interface instance
	 */
	public UpTimeUpdateTicket(Context context,RequestModel requestModel,
							  I_UpTimeUpdateTicket i_upTimeUpdateTicket) {
		mContext = context;
		securityToken = requestModel.token;
		this.description = requestModel.description;
		this.reasonId = requestModel.reasonId;
		this.vehicleRegNo = requestModel.vehicleRegNo;
		this.startTime = requestModel.startTime;
		this.endTime = requestModel.endTime;
		this.licenceNo = requestModel.licenceNo;
		this.requestType =requestModel.requestType;
		this.i_upTimeUpdateTicket = i_upTimeUpdateTicket;
		this.TicketId = requestModel.TicketId;
		this.DelayedReasonId = requestModel.DelayedReasonId;
		this.isTicketClosed = requestModel.isTicketClosed;
		this.UUID = requestModel.UUID;
		this.InreasonUniqueId = requestModel.InreasonUniqueId;
		this.delayedReasonComment = requestModel.delayedReasonComment;
		this.causalpart = requestModel.causalpart;
		this.enginehour = requestModel.enginehour;

		Log.e(TAG, "UpTimeUpdateTicket: reason id "+requestModel.reasonId );
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
		mProgressDialog = ProgressDialog.show(mContext, "","Processing...",
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
			RestIntraction restIntraction = null;

			if(requestType.equalsIgnoreCase(UpTimeRegisterActivity.TYPE_JOB))
			{
				//Create New Ticekt

				restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS() + ""
						+ ApiUrls.UPTIME_INSERT_TICKET);
				restIntraction.AddParam("TicketStatus", reasonId);
				//restIntraction.AddParam("TicketStatus", "1");
				restIntraction.AddParam("VehicleRegistrationNumber", vehicleRegNo);
				restIntraction.AddParam("StartDate", startTime);
				restIntraction.AddParam("EndDate", endTime);
				restIntraction.AddParam("AssignedToUserId", licenceNo);
				restIntraction.AddParam("Description", delayedReasonComment);
				restIntraction.AddParam("causalpart", causalpart);

				restIntraction.AddParam("Enginehours",enginehour);

				//If network not available save new Ticket to local DB and mark vehicle status to down.
				if (!new CheckInternetConnection(mContext).isConnectedToInternet()){

					UpTimeTicketDetailModel upTimeTicketDetailModel = new UpTimeTicketDetailModel();
					upTimeTicketDetailModel.setStatusAlias(description);
					upTimeTicketDetailModel.setSequenceOrder(reasonId);
					upTimeTicketDetailModel.setVehicleRegistrationNumber(vehicleRegNo);
					upTimeTicketDetailModel.setStartDate(startTime);
					upTimeTicketDetailModel.setEndDate(endTime);
					upTimeTicketDetailModel.setIsSyncWithServer("false");


					String year =TimePickerUtil.getTimeOffset(mContext,"yyyy",0,0);
					String uuid = "Y"+year+"TS"+(System.currentTimeMillis()/1000);
					upTimeTicketDetailModel.setTicketId(uuid);
					upTimeTicketDetailModel.setTicketIdAlias(uuid);
					upTimeTicketDetailModel.setUuid(uuid);
					upTimeTicketDetailModel.setJobComment(delayedReasonComment);
					upTimeTicketDetailModel.save();

					//Mark Vehicle status to Down
					VehicleModel vehicleModel =new Select()
							.from(VehicleModel.class)
							.where("Registration = ?", vehicleRegNo)
							.executeSingle();
					vehicleModel.setVehicleStatus("0");
					vehicleModel.save();

					isUpdateSuccessful = true;

					//Maintain Activity of Ticket
					//New Ticket Activity
					UpTimeTicketDetailModelActivity upTimeTicketDetailModelActivity = new UpTimeTicketDetailModelActivity();

					upTimeTicketDetailModelActivity.setStatusAlias(description);
					upTimeTicketDetailModelActivity.setVehicleRegistrationNumber(vehicleRegNo);
					upTimeTicketDetailModelActivity.setSequenceOrder(reasonId);
					upTimeTicketDetailModelActivity.setTicketId(uuid);
					upTimeTicketDetailModelActivity.setStartDate(startTime);
					upTimeTicketDetailModelActivity.setEndDate(endTime);
					upTimeTicketDetailModelActivity.setIsTicketClosed("false");
					upTimeTicketDetailModelActivity.setUuid(uuid);
					upTimeTicketDetailModelActivity.setActivityType(requestType);
					upTimeTicketDetailModelActivity.setJobComment(delayedReasonComment);
					Long result =upTimeTicketDetailModelActivity.save();

					//Enable JobScheduling ( When Network is available, Upload This offline record to server)
					enableOfflineSync(mContext);

				}
				description = delayedReasonComment;
			}else if(requestType.equalsIgnoreCase(UpTimeRegisterActivity.TYPE_ADD_REASON)) {
				restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS() + ""
						+ ApiUrls.UPTIME_UPDATE_TICKET);

				restIntraction.AddParam("ReasonStartDate", startTime);
				restIntraction.AddParam("ReasonEndDate", endTime);
				restIntraction.AddParam("TicketId", TicketId);
				restIntraction.AddParam("DelayedReasonId", DelayedReasonId);
				restIntraction.AddParam("isTicketClosed", isTicketClosed);
				restIntraction.AddParam("InreasonUniqueId", InreasonUniqueId);
				restIntraction.AddParam("inremarks", delayedReasonComment);
				//description = delayedReasonComment;
				restIntraction.AddParam("causalpart", causalpart);
				restIntraction.AddParam("Enginehours",enginehour);
				//Add Reason to Local Database
				/*UpTimeAddedReasonsModel upTimeAddedReasonsModel =new Select()
						.from(UpTimeAddedReasonsModel.class)
						.where("TicketId = ? AND ReasonId = ?", TicketId,DelayedReasonId)
						.executeSingle();*/

				/*if(upTimeAddedReasonsModel==null)
					upTimeAddedReasonsModel = new UpTimeAddedReasonsModel();*/

				UpTimeAddedReasonsModel upTimeAddedReasonsModel = new UpTimeAddedReasonsModel();
				upTimeAddedReasonsModel.set_reason_id(DelayedReasonId);
				upTimeAddedReasonsModel.set_reason_name(description);
				upTimeAddedReasonsModel.set_reason_alias(description);
				upTimeAddedReasonsModel.setReasonStartDate(startTime);
				upTimeAddedReasonsModel.setReasonEndDate(endTime);
				upTimeAddedReasonsModel.set_ticket_id(TicketId);
				upTimeAddedReasonsModel.setDelayedReasonComment(delayedReasonComment);
				upTimeAddedReasonsModel.setInreasonUniqueId(InreasonUniqueId);
				upTimeAddedReasonsModel.save();


				//If Network not avaialble , save New Added reason to Offline Activity DB
				if (!new CheckInternetConnection(mContext).isConnectedToInternet()){


					/*UpTimeAddedReasonsModelActivity upTimeAddedReasonsModel_activity  = new Select()
							.from(UpTimeAddedReasonsModelActivity.class)
							.where("TicketId = ? AND ReasonId = ?", TicketId,DelayedReasonId)
							.executeSingle();*/

					UpTimeAddedReasonsModelActivity upTimeAddedReasonsModel_activity  = null;

					if(upTimeAddedReasonsModel_activity==null) {
						//ADD NEW REASON
						upTimeAddedReasonsModel_activity = new UpTimeAddedReasonsModelActivity();
						upTimeAddedReasonsModel_activity.set_reason_id(DelayedReasonId);
						upTimeAddedReasonsModel_activity.set_reason_name(description);
						upTimeAddedReasonsModel_activity.set_reason_alias(description);
						upTimeAddedReasonsModel_activity.setReasonStartDate(startTime);
						upTimeAddedReasonsModel_activity.setReasonEndDate(endTime);
						upTimeAddedReasonsModel_activity.set_ticket_id(TicketId);
						upTimeAddedReasonsModel_activity.set_is_active(isTicketClosed+"");
						upTimeAddedReasonsModel_activity.setDelayedReasonComment(delayedReasonComment);
						upTimeAddedReasonsModel_activity.setInreasonUniqueId(InreasonUniqueId);
					}else
					{
						upTimeAddedReasonsModel_activity.setReasonStartDate(startTime);
						upTimeAddedReasonsModel_activity.setReasonEndDate(endTime);
						upTimeAddedReasonsModel_activity.setDelayedReasonComment(delayedReasonComment);
					}

					upTimeAddedReasonsModel_activity.setIsSyncWithServer("false");
					upTimeAddedReasonsModel_activity.save();

					isUpdateSuccessful = true;

					//Enable JobScheduling ( When Network is available, Upload This offline record to server)
					enableOfflineSync(mContext);
				}

				//description = delayedReasonComment;

			}else if(requestType.equalsIgnoreCase(UpTimeRegisterActivity.TYPE_EDIT_REASON)) {
				restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS() + ""
						+ ApiUrls.UPTIME_UPDATE_TICKET);

				restIntraction.AddParam("ReasonStartDate", startTime);
				restIntraction.AddParam("ReasonEndDate", endTime);
				restIntraction.AddParam("TicketId", TicketId);
				restIntraction.AddParam("DelayedReasonId", DelayedReasonId);
				restIntraction.AddParam("isTicketClosed", isTicketClosed);
				restIntraction.AddParam("InreasonUniqueId", InreasonUniqueId);
				restIntraction.AddParam("inremarks", delayedReasonComment);
				restIntraction.AddParam("causalpart", causalpart);
				restIntraction.AddParam("Enginehours",enginehour);
				//Add Reason to Local Database
				UpTimeAddedReasonsModel upTimeAddedReasonsModel =new Select()
						.from(UpTimeAddedReasonsModel.class)
						.where("TicketId = ? AND ReasonId = ? AND inreasonUniqueId = ?", TicketId,DelayedReasonId,InreasonUniqueId)
						.executeSingle();

				if(upTimeAddedReasonsModel==null)
					upTimeAddedReasonsModel = new UpTimeAddedReasonsModel();

				upTimeAddedReasonsModel.setReasonStartDate(startTime);
				upTimeAddedReasonsModel.setReasonEndDate(endTime);
				upTimeAddedReasonsModel.setDelayedReasonComment(delayedReasonComment);
				upTimeAddedReasonsModel.setInreasonUniqueId(InreasonUniqueId);
				upTimeAddedReasonsModel.save();


				//If Network not avaialble , save New Added reason to Offline Activity DB
				if (!new CheckInternetConnection(mContext).isConnectedToInternet()){

					UpTimeAddedReasonsModelActivity upTimeAddedReasonsModel_activity  = new Select()
							.from(UpTimeAddedReasonsModelActivity.class)
							.where("TicketId = ? AND ReasonId = ? AND inreasonUniqueId = ?", TicketId,DelayedReasonId,InreasonUniqueId)
							.executeSingle();


					if(upTimeAddedReasonsModel_activity==null) {
						//ADD NEW REASON
						upTimeAddedReasonsModel_activity = new UpTimeAddedReasonsModelActivity();
						upTimeAddedReasonsModel_activity.set_reason_id(DelayedReasonId);
						upTimeAddedReasonsModel_activity.setReasonStartDate(startTime);
						upTimeAddedReasonsModel_activity.setReasonEndDate(endTime);
						upTimeAddedReasonsModel_activity.set_ticket_id(TicketId);
						upTimeAddedReasonsModel_activity.set_is_active(isTicketClosed+"");
					}else
					{
						upTimeAddedReasonsModel_activity.setReasonStartDate(startTime);
						upTimeAddedReasonsModel_activity.setReasonEndDate(endTime);
					}
					upTimeAddedReasonsModel_activity.setDelayedReasonComment(delayedReasonComment);
					upTimeAddedReasonsModel_activity.setInreasonUniqueId(InreasonUniqueId);
					upTimeAddedReasonsModel_activity.setIsSyncWithServer("false");
					upTimeAddedReasonsModel_activity.save();

					isUpdateSuccessful = true;

					//Enable JobScheduling ( When Network is available, Upload This offline record to server)
					enableOfflineSync(mContext);
				}

				//description = delayedReasonComment;

			} else if(requestType.equalsIgnoreCase(UpTimeRegisterActivity.TYPE_EDIT_JOB))
			{
				restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS() + ""
						+ ApiUrls.UPTIME_UPDATE_TICKET);

				restIntraction.AddParam("TicketId", TicketId);
				restIntraction.AddParam("ServiceStartDate", startTime);
				restIntraction.AddParam("ServiceEndDate", endTime);
				restIntraction.AddParam("Description",delayedReasonComment);
				restIntraction.AddParam("causalpart", causalpart);
				restIntraction.AddParam("Enginehours",enginehour);
				UpTimeTicketDetailModel upTimeTicketDetailModel =new Select()
						.from(UpTimeTicketDetailModel.class)
						.where("TicketId = ?", TicketId)
						.executeSingle();

				upTimeTicketDetailModel.setStartDate(startTime);
				upTimeTicketDetailModel.setEndDate(endTime);
				upTimeTicketDetailModel.setJobComment(delayedReasonComment);
				upTimeTicketDetailModel.save();

				//If Network not avaialble , save Updated Job to Local DB
				if (!new CheckInternetConnection(mContext).isConnectedToInternet()){

					//Maintain Activity
					//Update Ticekt Activity
					UpTimeTicketDetailModelActivity upTimeTicketDetailModelActivity = new UpTimeTicketDetailModelActivity();

					upTimeTicketDetailModelActivity.setTicketId(TicketId);
					upTimeTicketDetailModelActivity.setStartDate(startTime);
					upTimeTicketDetailModelActivity.setEndDate(endTime);
					upTimeTicketDetailModelActivity.setIsTicketClosed("false");
					upTimeTicketDetailModelActivity.setActivityType(requestType);
					upTimeTicketDetailModelActivity.save();

					isUpdateSuccessful = true;
					//Enable JobScheduling ( When Network is available, Upload This offline record to server)
					enableOfflineSync(mContext);

				}
			}else if(requestType.equalsIgnoreCase(UpTimeRegisterActivity.TYPE_CLOSE_JOB)) {

				restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS() + ""
						+ ApiUrls.UPTIME_UPDATE_TICKET);

				restIntraction.AddParam("TicketId", TicketId);
				restIntraction.AddParam("isTicketClosed", isTicketClosed);
				restIntraction.AddParam("Description",delayedReasonComment);
				restIntraction.AddParam("causalpart", causalpart);
				restIntraction.AddParam("Enginehours",enginehour);
				//Close Ticket(remove)from Local DB
				UpTimeTicketDetailModel upTimeTicketDetailModel =new Select()
						.from(UpTimeTicketDetailModel.class)
						.where("TicketId = ?", TicketId)
						.executeSingle();
				upTimeTicketDetailModel.delete();

				//Close AddedReasons (Remove) from local DB for this ticket
				List<UpTimeAddedReasonsModel> upTimeAddedReasonsModels = DAO.getAddedReasons(TicketId);
				for(int i=0;i<upTimeAddedReasonsModels.size();i++)
				{
					upTimeAddedReasonsModels.get(i).delete();
				}

				//Set Vehicle status from Down->UP
				VehicleModel vehicleModel =new Select()
						.from(VehicleModel.class)
						.where("Registration = ?", vehicleRegNo)
						.executeSingle();
				vehicleModel.setVehicleStatus("1");
				vehicleModel.save();

				//If Network not avaialble , update Ticket status to 'close' and save into local DB
				if (!new CheckInternetConnection(mContext).isConnectedToInternet()){

					//Maintain Activity
					//Ticket Close Activity
					UpTimeTicketDetailModelActivity upTimeTicketDetailModelActivity = new UpTimeTicketDetailModelActivity();
					upTimeTicketDetailModelActivity.setTicketId(TicketId);
					upTimeTicketDetailModelActivity.setIsTicketClosed("true");
					upTimeTicketDetailModelActivity.setActivityType(requestType);
					upTimeTicketDetailModelActivity.save();
					isUpdateSuccessful = true;
					//Enable JobScheduling ( When Network is available, Upload This offline record to server)
					enableOfflineSync(mContext);
				}
			}
			//Common Parameters
			restIntraction.AddParam("token", securityToken);
			restIntraction.AddParam("UserId", licenceNo);
			restIntraction.Execute(1);
			Log.i("Request",restIntraction.toString());
			response = restIntraction.getResponse();
			Log.i("response",response);

			if (response != null) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					if(jsonObject.getString("Status").equalsIgnoreCase("1")){
						isUpdateSuccessful = true;
					}else {
						isUpdateSuccessful = false;
						errorMessage=jsonObject.getString("Message");
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
private String errorMessage="";
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
		Log.e(TAG, "onPostExecute: error message "+errorMessage );
		i_upTimeUpdateTicket.onTicketupdateResponse(isUpdateSuccessful,errorMessage);
	}

	/**
	 * Interface to be implemented by caller class of this Asyn Task
	 */
	public interface I_UpTimeUpdateTicket
	{
		void onTicketupdateResponse(boolean isUpdateSuccessful,String msg);
	}


	/**
	 * This method will enable Job Scheduling in background when Network become available
	 * @param context
	 */
	private void enableOfflineSync(Context context)
	{
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//			OfflineDataSync.scheduleJob(context);
//		}
	}

	/**
	 * Request model to be passed in this Asyn Task constructor. to call API
	 */
	public static class RequestModel
	{
		public String requestType;
		public String token;
		public String description;
		public String reasonId;
		public String vehicleRegNo;
		public String startTime;
		public String endTime;
		public String licenceNo;
		public String TicketId;
		public String UUID;
		public String DelayedReasonId;
		public String isTicketClosed;
		public String InreasonUniqueId;
		public String delayedReasonComment;

		public String causalpart;
		public String enginehour;
	}

}
