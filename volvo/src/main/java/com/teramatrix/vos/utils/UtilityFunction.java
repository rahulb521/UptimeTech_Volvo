package com.teramatrix.vos.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.teramatrix.vos.R;
import com.teramatrix.vos.asynctasks.ApplicationLogging;
import com.teramatrix.vos.model.AppErrorLog;
import com.teramatrix.vos.model.LocationTrackingLog;
import com.teramatrix.vos.model.LogComplete;
import com.teramatrix.vos.service.PostLocationService;

/**
 * 
 * @author Gaurav.Mangal
 * 
 *         UtilityFunction class is used for some global function with the
 *         entire application inside the class structure.
 */

public class UtilityFunction {

	ObjectOutputStream object_outputsrream;
	// Define static String variable
	public static String imeiNumber;
	static StringBuilder temp_builder_location;
	static FileOutputStream file_outputstream;

	// show Alert dialog with message
	/**
	 * 
	 * @param context
	 * @param message
	 */
	public static void showMessage(Context context, String message) {

		// Create an object of the AlertDialog.Builder class
		AlertDialog.Builder alertbox = new AlertDialog.Builder(context);

		// set the title for this Alert Dialog
		alertbox.setTitle(context.getResources().getString(R.string.error_message));

		// set the message for this Alert Dialog
		alertbox.setMessage(message);
		alertbox.setCancelable(false);
		alertbox.setNeutralButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Dismsss the Dialog on click on OK.
				dialog.dismiss();
			}
		});
		alertbox.show();
	}

	public static String getAppVersion(Context context) {
		PackageManager packageManager = context.getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			String currentVersion = packageInfo.versionName;
			return currentVersion;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	// Show toast message
	/**
	 * 
	 * @param ctx
	 * @param msg
	 */
	public static void showCenteredToast(Context ctx, String msg) {
		Toast toast = Toast.makeText(ctx, msg, Toast.LENGTH_LONG);
		toast.show();
	}

	// get IMEI number of the device

	/**
	 * @param context
	 * @return
	 */
	@SuppressLint("NewApi")
	public static String getIMEINumber(Context context,String licenseKey) {
		// Create an instance of the TelephonyManager
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		// get the device id via telephonyManage
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			imeiNumber =  telephonyManager.getImei(1);
		} else {
			imeiNumber = telephonyManager.getDeviceId();
		}

		if (imeiNumber == null || imeiNumber.isEmpty()) {
			imeiNumber = licenseKey+System.currentTimeMillis();
		}

		return imeiNumber;
	}

	// get current UTC time

	@SuppressLint("SimpleDateFormat")
	/**
	 * 
	 * @return
	 */
	public static String currentUTCTime() {
		// Create an instance of the SimpleDateFormat
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd-MMM-yyyy HH:mm:ss");
		// set the TimeZone
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		// return date in the specified format
		return dateFormat.format(new Date());
	}

	//get current time without dash
	/**
	 * 
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String currentUTCTimeWithoutDash() {
		// Create an instance of the SimpleDateFormat
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd MMM yyyy HH:mm:ss");
		// set the TimeZone
		//dateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		// return date in the specified format
		return dateFormat.format(new Date());

	}

	/**
	 * 
	 * @return 
	 */
	//get current time without dash Error time
	@SuppressLint("SimpleDateFormat")
	public static String currentUTCTimeWithoutDash_Error_Time() {
		// Create an instance of the SimpleDateFormat
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd MMM yyyy HH:mm:ss");
		// set the TimeZone
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
		// return date in the specified format
		return dateFormat.format(new Date());
	}

	/**
	 * 
	 * @param context
	 * @param location_trackinglog
	 * @param appErrorLog
	 * @param logComplete
	 * @param log_file_name
	 * 
	 * This method for create root folder inside three folder LocationLogs,ErrorLogs,AppLogs and different file inside a seprate folder.
	 */
	public static void createfolder_file_location_log(Context context,
			LocationTrackingLog location_trackinglog, AppErrorLog appErrorLog,
			LogComplete logComplete, String log_file_name) {
		try {
			//instance file class 
			File filename_eicher_log;
			//define string variable log_folder_path
			String log_folder_path = null;
			
			//check if location tracking log null or not
			if (location_trackinglog != null) {
				
				//get the location log folder path
				log_folder_path = Environment.getExternalStorageDirectory()
						+ context.getResources().getString(
								R.string.log_folder_tracking_location);
				
				System.out.println("path"+log_folder_path);
			} else if (appErrorLog != null) {
				//get the error log folder path
				log_folder_path = Environment.getExternalStorageDirectory()
						+ context.getResources().getString(
								R.string.log_folder_error);
			} else if (logComplete != null) {
				//get the app log folder path
				log_folder_path = Environment.getExternalStorageDirectory()
						+ context.getResources().getString(
								R.string.log_folder_app_log);
			}

			//passing file constructor with the folder path
			File eicherRootFolder = new File(log_folder_path);
			try {
				//check if folder is exists or not.
				if (!eicherRootFolder.exists()) {
					
					//create a folder when not exist
					boolean flag = eicherRootFolder.mkdirs();
				}

				// Serializes an object and saves it to a file
				try {
					if (log_file_name != null
							&& log_file_name.equalsIgnoreCase(context
									.getResources().getString(
											R.string.eicher_location_log))) {
						String fileName = context.getResources().getString(
								R.string.eicher_location_log)
								+ TimeFormater.getTodayDateInCurrentTimeZone()
								+ ".txt";
						filename_eicher_log = new File(eicherRootFolder,
								fileName);
						if (!filename_eicher_log.exists()) {
							filename_eicher_log.createNewFile();
						}
						//save location tacking log in the path with file name
						saveLocationTrackingLog(filename_eicher_log,
								location_trackinglog);
					} else if ((log_file_name != null && log_file_name
							.equalsIgnoreCase(context.getResources().getString(
									R.string.eicher_error_log)))) {

						String fileName = context.getResources().getString(
								R.string.eicher_error_log)
								+ TimeFormater.getTodayDateInCurrentTimeZone()
								+ ".txt";
						filename_eicher_log = new File(eicherRootFolder,
								fileName);
						if (!filename_eicher_log.exists()) {
							
							//create a new file when not exist
							filename_eicher_log.createNewFile();
						}

						saveErrorLog(filename_eicher_log, appErrorLog);

					} else if ((log_file_name != null && log_file_name
							.equalsIgnoreCase(context.getResources().getString(
									R.string.eicher_complete_log)))) {

						String fileName = context.getResources().getString(
								R.string.eicher_complete_log)
								+ TimeFormater.getTodayDateInCurrentTimeZone()
								+ ".txt";
						filename_eicher_log = new File(eicherRootFolder,
								fileName);
						if (!filename_eicher_log.exists()) {
							//create a new file when not exist
							filename_eicher_log.createNewFile();
						}

						saveCompleteLog(filename_eicher_log, logComplete);

					}
					
				} catch (Exception e) {

					UtilityFunction.saveErrorLog(context, e);
				}

			} catch (Exception ex) {

				UtilityFunction.saveErrorLog(context, ex);
			}

		} catch (Exception e) {

			UtilityFunction.saveErrorLog(context, e);
		}
	}
	
	/**
	 * 
	 * @param filename_eicher_location_log
	 * @param location_trackinglog
	 * @throws Exception
	 */

	public static void saveLocationTrackingLog(
			File filename_eicher_location_log,
			LocationTrackingLog location_trackinglog) throws Exception {
		try {
			String location_trackinglog_data = "\n"
					+ location_trackinglog.getLatitude() + " "
					+ location_trackinglog.getLongitude() + " "
					+ location_trackinglog.getLogtime() + " "
					+ location_trackinglog.getNetwork_state()+ " "
							+ location_trackinglog.getBattery_label()
							+ " "
					+ location_trackinglog.getCharging_state()+ " "
							+ location_trackinglog.getGps_state();
			file_outputstream = new FileOutputStream(
					filename_eicher_location_log, true);
			
			file_outputstream.write(location_trackinglog_data.getBytes());
			file_outputstream.flush();
			
		} catch (Exception ex) {

			throw ex;
		}
	}

	
	/**
	 * 
	 * @param context
	 * @param e
	 * 
	 * This method for save error log in the file
	 */
	public static void saveErrorLog(Context context, Exception e) {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		e.printStackTrace(pw);

		String message_error = sw.toString();

		//create object of AppErrorLog
		AppErrorLog appErrorLog = new AppErrorLog();
		appErrorLog.setLogTime(UtilityFunction
				.currentUTCTimeWithoutDash_Error_Time());
		appErrorLog.setErrorMessage(message_error);
		new ApplicationLogging(context, appErrorLog).execute();

	}

	
	/**
	 * 
	 * @param filename_eicher_error_log
	 * @param appErrorLog
	 * @throws Exception
	 */
	public static void saveErrorLog(File filename_eicher_error_log,
			AppErrorLog appErrorLog) throws Exception {
		try {
			String error_log = "\n" + appErrorLog.getLogTime() + " "
					+ appErrorLog.getErrorMessage();
			file_outputstream = new FileOutputStream(filename_eicher_error_log,
					true);
			
			file_outputstream.write(error_log.getBytes());
			file_outputstream.flush();
			
		} catch (Exception ex) {

			throw ex;

		}
	}
/**
 * 
 * @param filename_eicher_error_log
 * @param logComplete
 * @throws Exception
 * 
 * This method used for store complete app log in file
 */
	public static void saveCompleteLog(File filename_eicher_error_log,
			LogComplete logComplete) throws Exception {
		try {
			//fetch object with value and initialize the error_log with variable.
			String error_log = "\n\n" + logComplete.getLog_message();
			
			//instace for the file output stream data
			file_outputstream = new FileOutputStream(filename_eicher_error_log,
					true);
			
			//write the data in the file
			file_outputstream.write(error_log.getBytes());
			
			//save and flush data in the file
			file_outputstream.flush();
			// object_outputsrream.flush();
			// object_outputsrream.close();
		} catch (Exception ex) {

			//exception ouu
			throw ex;

		}
	}
	
	/**
	 * 
	 * @param f
	 * @return
	 * @throws Exception
	 */

	public static LocationTrackingLog getsaveLocationTrackingLog(File f)
			throws Exception {
		try {

			FileInputStream file_inputstream = new FileInputStream(f);
			ObjectInputStream object_outputsrream = new ObjectInputStream(
					file_inputstream);
			LocationTrackingLog location_log_data = (LocationTrackingLog) object_outputsrream
					.readObject();
			object_outputsrream.close();
			return location_log_data;
		} catch (Exception ex) {

			throw ex;
		}
	}


	public static List<String> spiltAndGenerateList(String string,String regexp)
	{
		if (string==null || string.isEmpty())
			return null;

		String[] s = string.split(regexp);

		List<String> list = new ArrayList<>();
		for(int i=0;i<s.length;i++)
		{
			String value = s[i];
			list.add(value);
		}
		return list;
	}


	public static void toastMessage(Context context,String msg)
	{
		if (ApplicationConstant.IS_TESTING_TOAST_SHOWN)
			Toast.makeText(context,msg,Toast.LENGTH_LONG).show();
	}

	public static void setDefaultLocationToDelhi(Context context)
	{

		SharedPreferences preference = context.getSharedPreferences(
				ApplicationConstant.PREFERENCE_NAME, context.MODE_PRIVATE);
		float lat = preference.getFloat(ApplicationConstant.KEY_LATT,0);
		if(lat==0)
		{
			Location defaultLocationDelhi = new Location("");//provider name is unnecessary
			defaultLocationDelhi.setLatitude(28.6139d);//your coords of course
			defaultLocationDelhi.setLongitude(77.2090d);
			saveLocationToPreference(context,defaultLocationDelhi);
			//Trigger anther servcie to post locatino to server
			context.startService(new Intent(context, PostLocationService.class));
		}

	}


	public static void saveLocationToPreference(Context context,Location updatelocation)
	{
		if(updatelocation==null)
			return;
		Log.i("UtilityFunction","saveLocationToPreference");

		SharedPreferences preference = context.getSharedPreferences(
				ApplicationConstant.PREFERENCE_NAME, context.MODE_PRIVATE);
		// update new location
		SharedPreferences.Editor ed = preference.edit();
		ed.putFloat(ApplicationConstant.KEY_LATT, (float) updatelocation.getLatitude());
		ed.putFloat(ApplicationConstant.KEY_LONG, (float) updatelocation.getLongitude());
		ed.putString(ApplicationConstant.KEY_PROVIDER, updatelocation.getProvider());
		ed.commit();
	}
	
}