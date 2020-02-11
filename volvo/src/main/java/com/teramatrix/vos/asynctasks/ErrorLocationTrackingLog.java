package com.teramatrix.vos.asynctasks;

import java.io.Serializable;

import android.content.Context;
import android.os.AsyncTask;

import com.teramatrix.vos.model.AppErrorLog;
import com.teramatrix.vos.model.LocationTrackingLog;
import com.teramatrix.vos.utils.TimeFormater;
import com.teramatrix.vos.utils.UtilityFunction;

/**
 * 
 * @author Gaurav.Mangal
 * This ErrorLocationTrackingLog class is used for get file when error has occurs.
 */
public class ErrorLocationTrackingLog extends AsyncTask<Void, Void, Void>
		implements Serializable 
		{

	private static final long serialVersionUID = 1L;
	// Defining Context for this class
	private Context context;
	
	// Defining String variables for this class
	String locationLogTime;
	
	// Define LocationTrackingLog instance for this class
	LocationTrackingLog location_trackinglog;
	
	// Define AppErrorLog instance for this class
	AppErrorLog appErrorLog;

/**
 * 
 * @param context
 * @param location_trackinglog
 */
	public ErrorLocationTrackingLog(Context context,
			LocationTrackingLog location_trackinglog) {
		this.context = context;
		this.location_trackinglog = location_trackinglog;
		this.appErrorLog=appErrorLog;
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute(),Method is used to perform UI operation
	  before starting background Service
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[]) Method is used to perform
	 * Background Task.
	 */

	@Override
	protected Void doInBackground(Void... params) {

		try {

			// set location time to current zone time
			String logTime = location_trackinglog.getLogtime();
			logTime = TimeFormater.getTimeStringInCurrentTimeZone(logTime);
			location_trackinglog.setLogtime(logTime);

			UtilityFunction
					.createfolder_file_location_log(context,location_trackinglog,appErrorLog,null,appErrorLog.getLogTime());
			

		} catch (Exception ex) {
			//save error location in file
			UtilityFunction.saveErrorLog(context, ex);
		}
		return null;
	}
/*
 * (non-Javadoc)
 * @see android.os.AsyncTask#onPostExecute(java.lang.Object) Method is used to perform
	 * UI operation after background task will be finishing.
 */
	protected void onPostExecute(Void result) {

	};
}
