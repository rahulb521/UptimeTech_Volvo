package com.teramatrix.vos.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.R;
import com.teramatrix.vos.utils.UtilityFunction;

import java.util.Timer;
import java.util.TimerTask;

public class Locationservice extends Service implements
        OnConnectionFailedListener, ConnectionCallbacks, LocationListener {
    public static final String TAG = "Locationservice";
    public static GoogleApiClient mGoogleApiClient;
    public static final long UPDATE_LOCATION_INTERVAL = 60 * 1000;
    //    PubnubHandler pubnubHandler;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = false;
    //Declare the timer
    Timer locationPostTimer;
    static Locationservice instans;

    public static Locationservice getInstans() {
        return instans;
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, Locationservice.class);
    }

    private void printLog(String message) {
        Log.e(TAG, message);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instans = this;

        // TODO Auto-generated method stub
        //  Utils.printLog("my service onStartCommand");
        printLog("onCreate call");
        buildGoogleApiClient();
        mRequestingLocationUpdates = false;
        triggerLocationPostTimer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        printLog("onStartCommand call");
        buildGoogleApiClient();
        mRequestingLocationUpdates = false;
        triggerLocationPostTimer();
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        //  Utils.printLog("my service GoogleApiclient onConnected");
        printLog("GoogleApiclient onConnected call");
        createLocationRequest();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        UtilityFunction.saveLocationToPreference(getApplicationContext(), mLastLocation);
        startLocationUpdates();
        //Notification
        startForeground(1119,
                crteateForgroundNotification(getApplicationContext(), "Tracking Service"));
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub
        printLog("GoogleApiclient onConnectionSuspended call");
        mGoogleApiClient.connect();
        //   Utils.printLog("my service GoogleApiclient onConnectionSuspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        printLog("onConnectionFailed call");
        printLog(arg0.toString());
        // TODO Auto-generated method stub
        //  Utils.printLog("my service GoogleApiclient onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location updatelocation) {
        printLog("onLocationChanged call");

        // TODO Auto-generated method stub
        UtilityFunction.saveLocationToPreference(getApplicationContext(), updatelocation);

        if (updatelocation != null)
            printLog("location lat:" + updatelocation.getLatitude() + " , lng:" + updatelocation.getLongitude());
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        printLog("buildGoogleApiClient call");
    }

    protected void createLocationRequest() {
        printLog("createLocationRequest call");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(UPDATE_LOCATION_INTERVAL);
        //  mLocationRequest.setSmallestDisplacement(5);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        printLog("startLocationUpdates call");

        if (!mRequestingLocationUpdates && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mRequestingLocationUpdates = true;
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        printLog("stopLocationUpdates call");

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()
                && mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
        if (locationPostTimer != null) {
            locationPostTimer.cancel();
            locationPostTimer = null;
        }
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        instans = null;

        stopLocationUpdates();
        //  Utils.printLog("Service destroy");
        super.onDestroy();
    }

    private void triggerLocationPostTimer() {
        printLog("triggerLocationPostTimer");
        if (locationPostTimer != null) {
            locationPostTimer.cancel();

        }
        locationPostTimer = new Timer();
        //Set the schedule function and rate
        locationPostTimer.scheduleAtFixedRate(new TimerTask() {

                                                  @Override
                                                  public void run() {
                                                      //Called each time when 1000 milliseconds (1 second) (the period parameter)
                                                      //Trigger anther servcie to post locatino to server
                                                      printLog("triggerLocationPostTimer run");
                                                      startService(new Intent(getApplicationContext(), PostLocationService.class));
                                                  }

                                              },
                //Set how long before to start calling the TimerTask (in milliseconds)
                0,
                //Set the amount of time between each execution (in milliseconds)
                60 * 1000);
    }

    // New Method for crteateForgroundNotification
    private Notification crteateForgroundNotification(Context context,
                                                      String message) {

        printLog("crteateForgroundNotification");

        long when = System.currentTimeMillis();
        String title = "VAS";

        // Open Activity when notification is clicked
        // set intent so it does not start a new activity
        Intent notificationIntent = new Intent(context, MyTicketActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        //initialize Notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.bg_service_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        return mBuilder.build();
    }

}
