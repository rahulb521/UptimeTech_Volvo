package com.teramatrix.vos.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arun.singh on 1/9/2017.
 * Check and Request runtime permmision for accessing device resources.
 * Used in Android Mashmallow OS version
 */


public class PermissionsUtils {



    public static final int MULTIPLE_PERMISSIONS = 10;


    /**
     * Permissions to be Request from User while using sdk's services
     */
    String[] permissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.VIBRATE
            };

    /**
     * Method is used for requesting some restricted permission.Called by sdk internally before initializing sdk services.
     * This method will check Permission status needed to functioing os app. If any permission is not granted ,a dialog will be opened asking permission from user.
     *
     * @param activity instance of activity from where this method is called
     */
    public void requestForPermission(Activity activity) {
        if (checkPermissions(activity)) {
            //  permissions  granted.
        }
    }

    private boolean checkPermissions(Activity activity) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(activity, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


}
