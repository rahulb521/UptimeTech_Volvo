package com.teramatrix.vos.asynctasks;

import java.io.Serializable;

import android.content.Context;
import android.os.AsyncTask;

import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.R;
import com.teramatrix.vos.model.AppErrorLog;
import com.teramatrix.vos.model.LocationTrackingLog;
import com.teramatrix.vos.model.LogComplete;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.utils.TimeFormater;
import com.teramatrix.vos.utils.UtilityFunction;

/**
 * This class used for maintaining location log Error log and App log with
 * passing diffent file name using selection.
 * 
 * @author Gaurav.Mangal
 * 
 */
public class ApplicationLogging extends AsyncTask<Void, Void, Void> implements
		Serializable {

	private static final long serialVersionUID = 1L;
	// Define Context for this class
	private Context context;

	// Define String variables for this class
	private String latitude, longitude, network_state, locationLogTime;

	// Define AppErrorLog instance for this class
	private AppErrorLog appErrorLog;

	// Define LocationTrackingLog instance for this class
	LocationTrackingLog location_trackinglog;

	// Defining VECV Preferences class
	private VECVPreferences vecvPreferences;

	LogComplete logComplete;

	/**
	 * 
	 * @param context
	 * @param location_trackinglog
	 */
	public ApplicationLogging(Context context,
			LocationTrackingLog location_trackinglog) {
		this.context = context;
		this.location_trackinglog = location_trackinglog;
	}

	/**
	 * Constructor with Application logging with AppErrorLog
	 * @param context
	 * @param appErrorLog
	 */
	public ApplicationLogging(Context context, AppErrorLog appErrorLog) {
		this.context = context;
		this.appErrorLog = appErrorLog;
	}

	public ApplicationLogging(Context context, LogComplete logComplete) {
		this.context = context;
		this.logComplete = logComplete;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Void doInBackground(Void... params) {

		try {

			if (location_trackinglog != null) {
				// set location time to current zone time
				String logTime = location_trackinglog.getLogtime();
				logTime = TimeFormater.getTimeStringInCurrentTimeZone(logTime);
				location_trackinglog.setLogtime(logTime);

				
				String fileName = context.getResources().getString(
						R.string.eicher_location_log);
				UtilityFunction.createfolder_file_location_log(context,
						location_trackinglog, appErrorLog, logComplete,
						fileName);
			} else if (appErrorLog != null) {
				//Disable due to non usability
				
				/*String logTime = appErrorLog.getLogTime();
				String errorMessage = appErrorLog.getErrorMessage();
				String fileName = context.getResources().getString(
						R.string.eicher_error_log);
				UtilityFunction.createfolder_file_location_log(context,
						location_trackinglog, appErrorLog, logComplete,
						fileName);*/
			} else if (logComplete != null) {
				String fileName = context.getResources().getString(
						R.string.eicher_complete_log);
				UtilityFunction.createfolder_file_location_log(context,
						location_trackinglog, appErrorLog, logComplete,
						fileName);

			}

		} catch (Exception ex) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(ex);
			
			//save error location in file
			UtilityFunction.saveErrorLog(context, ex);
		}
		return null;
	}

	protected void onPostExecute(Void result) {

	};
}
