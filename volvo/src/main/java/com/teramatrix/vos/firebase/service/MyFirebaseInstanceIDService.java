package com.teramatrix.vos.firebase.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.gson.Gson;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.model.TokenRefreshModel;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;


/**
 * Created by ubuntu on 7/12/16.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();
    String uname = null;
    String password = null;


    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (refreshedToken != null) {
            Log.e("FCMTOKENKEY", refreshedToken);
        }
        // Saving reg id to shared preferences
        // storeRegIdInPref(refreshedToken);
        // sending reg id to your server
        VECVPreferences vecvPreferences=new VECVPreferences(getApplicationContext());
        vecvPreferences.setGcmID(refreshedToken);

        if (isNetworkAvailable()) {
            sendRegistrationToServer(refreshedToken);
        } else {
            Log.e("Network Err in Service", "" + refreshedToken);
        }

    }

    private void sendRegistrationToServer(final String token) {
        if (isNetworkAvailable()) {
            try {
            String response;
            VECVPreferences vecvPreferences = new VECVPreferences(getApplicationContext());
            RestIntraction restIntraction = new RestIntraction(vecvPreferences.getAPIEndPoint_EOS() +
                    ApiUrls.REFRESHTOKEN);
            restIntraction.AddParam("token", "teramatrix");
            restIntraction.AddParam("RegistrationNo", vecvPreferences.getLicenseKey());
            restIntraction.AddParam("Imei", vecvPreferences.getImeiNumber());
            restIntraction.AddParam("DeviceId", token);
            restIntraction.AddParam("PushNotificationBit", "1");

            restIntraction.Execute(1);
            response=restIntraction.getResponse();
            Log.e("RESPONSE",response);

            if (response!=null
                    && !response.isEmpty()){
                TokenRefreshModel tokenRefreshModel=new Gson().fromJson(response,TokenRefreshModel.class);
                if (tokenRefreshModel!=null){
                    if (tokenRefreshModel.getStatus()=="1")
                    {
                    Log.e("FCMTOKEN","token sent successfully");

                    }
                }
            }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }
}
