package com.teramatrix.vos.volvouptime;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.teramatrix.vos.reciver.InternetAvailabilityRecever;
import com.teramatrix.vos.volvouptime.asyntask.OfflineDataSync;

/**
 * Created by arun.singh on 6/12/2018.
 * This is base activity for all Activity(Screns) used in UpTime app. All activities(Screens)  extends this base activity.
 * This Base Activity contains some common components and methods to be used by all other activities.
 */

public class UpTimeBaseActivity extends AppCompatActivity {

    private OfflineSyncStatus_BroadcastReciver offlineSyncStatus_broadcastReciver;
    private UpTimeBaseActivity childActivity;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setMessage("Sync Offline data...");
    }

    //Broadcast receiver to recevie notification about status of Offline Sync process
    private class OfflineSyncStatus_BroadcastReciver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {

                int status = intent.getIntExtra(OfflineDataSync.OFFLINE_SYNC_RUNNING_STATUS,0);

                if(status == OfflineDataSync.OFFLINE_SYNC_STARTED)
                {
                    //Syn process started. Show progress dialog
                    showSyncProgress();
                }else if(status == OfflineDataSync.OFFLINE_SYNC_FINISHED_WITH_SUCCESS)
                {
                    //Syn process finished with sucess. Dismiss progress dialog
                    stopSyncProgress(1);
                }else if(status == OfflineDataSync.OFFLINE_SYNC_FINISHED_WITH_FAIL)
                {
                    //Syn process finished with fail. Dismiss progress dialog
                    stopSyncProgress(0);
                }

        }
    }

    /**
     * Method to register broadcast reciver. This method is called by child activity on thier onResume method
     * @param activity - instance/context of child activity
     */
    public void registerBReciver(UpTimeBaseActivity activity)
    {
        childActivity = activity;

        offlineSyncStatus_broadcastReciver = new OfflineSyncStatus_BroadcastReciver();

            IntentFilter filter = new IntentFilter();
            filter.addAction("OfflineDataSync.intent");
            registerReceiver(offlineSyncStatus_broadcastReciver,filter);

    }

    /**
     * Method to unregister broadcast reciver
     * This method is called by child activity on thier onPause method to unregister broadcst reciver
     */
    public void unregisterBReciver()
    {
        unregisterReceiver(offlineSyncStatus_broadcastReciver);
        childActivity = null;
        offlineSyncStatus_broadcastReciver = null;
    }

    /**
     * initialize and Show progress dialog
     */
    private void showSyncProgress()
    {

        mProgressDialog.show();
    }

    /**
     * Dismis progress dialog
     * @param status
     */
    private void stopSyncProgress(int status)
    {
            mProgressDialog.dismiss();
        //IF status ==1 call
        childActivity.syncCompleteCallback(status);

    }

    /**
     * This method is implemented in child activity. and invoked after sync process is finished.
     * Child activity can do any work in this method after sync process finished is communicated.
     * @param status - status of sync process.
     */
    public void syncCompleteCallback(int status)
    {
    }
    InternetAvailabilityRecever internetAvailabilityRecever=new InternetAvailabilityRecever();

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(internetAvailabilityRecever);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(internetAvailabilityRecever, filter);
    }
}
