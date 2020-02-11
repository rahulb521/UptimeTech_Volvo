package com.teramatrix.vos.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;

import com.teramatrix.vos.R;

/**
 * This GPSManager class check gps on device, if gps is off then show message GPS is disabled in your device.Please enable it? poup occurs.
 * and start ACTION_LOCATION_SOURCE_SETTINGS in phone view dialog open.
 *
 */
public class GPSManager {

	/**
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkGpsStatus(final Context context) 
	{
		//check gps provider location
		if (!((LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE))
				.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// prompt user to enable gps
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);
			alertDialogBuilder.setCancelable(false);
			alertDialogBuilder
					.setMessage(
							context.getResources().getString(R.string.gpe_check))
					.setCancelable(false)
					.setPositiveButton(context.getResources().getString(R.string.gps_forword),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									
									//start GPS settings intent
									Intent callGPSSettingIntent = new Intent(
											android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									context.startActivity(callGPSSettingIntent);
									
								}
							});
			
			//create alert builder dialog
			AlertDialog alert = alertDialogBuilder.create();

			//show alert dialog
			alert.show();
			return false;

		} else {
			return true;
		}
	}
	
	
	
	
}
