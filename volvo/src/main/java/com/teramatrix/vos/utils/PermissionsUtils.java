package com.teramatrix.vos.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.teramatrix.vos.service.Locationservice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arun.singh on 1/9/2017.
 * Check and Request runtime permmision for accessing device resources.
 * Used in Android Mashmallow OS version
 */


public class PermissionsUtils {



    String TAG = this.getClass().getSimpleName();
    public static final int MULTIPLE_PERMISSIONS = 10;


    /**
     * Permissions to be Request from User while using sdk's services
     */

    //below android 11
    String[] permissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.VIBRATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            };

    //above android 10
    String[] permissionsabove10 = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.VIBRATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            //Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    };

    /**
     * Method is used for requesting some restricted permission.Called by sdk internally before initializing sdk services.
     * This method will check Permission status needed to functioing os app. If any permission is not granted ,a dialog will be opened asking permission from user.
     *
     * @param activity instance of activity from where this method is called
     */
    public void requestForPermission(Activity activity) {
        if (checkPermissions(activity)) {

            Log.e(TAG, "requestForPermission: onstart yes" );
            //  permissions  granted.
            //Get Users Last Known Location

            UtilityFunction.setDefaultLocationToDelhi(activity);

            // start tracking
		/*Intent mServiceIntent = new Intent(this,
				DeviceTrackingService.class);*/

            Intent mServiceIntent = new Intent(activity,
                    Locationservice.class);
            activity.startService(mServiceIntent);

        }else {

            Log.e(TAG, "requestForPermission: onstart no" );
            UtilityFunction.setDefaultLocationToDelhi(activity);

            // start tracking
		/*Intent mServiceIntent = new Intent(this,
				DeviceTrackingService.class);*/

            Intent mServiceIntent = new Intent(activity,
                    Locationservice.class);
            activity.startService(mServiceIntent);
        }
    }

    private boolean checkPermissions(Activity activity) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.e(TAG, "checkPermissions: above q " );
            for (String p : permissionsabove10) {
                result = ContextCompat.checkSelfPermission(activity, p);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p);
                }
            }
        } else {
            Log.e(TAG, "checkPermissions: below q" );
            for (String p : permissions) {
                result = ContextCompat.checkSelfPermission(activity, p);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p);
                }
            }
        }


        Log.e(TAG, "checkPermissions: list "+listPermissionsNeeded );
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);

            Log.e("TAG", "checkPermissions: false" );

            return false;

        }
        Log.e("TAG", "checkPermissions: true" );

        return true;
    }


}
