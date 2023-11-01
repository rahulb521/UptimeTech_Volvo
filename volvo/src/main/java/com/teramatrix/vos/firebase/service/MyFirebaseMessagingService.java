package com.teramatrix.vos.firebase.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.R;
import com.teramatrix.vos.firebase.utils.NotifactionUtil;
import com.teramatrix.vos.logs.CompleteLogging;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.service.SyncTicketsService;
import com.teramatrix.vos.ticketmanager.NewTicketManager;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.UtilityFunction;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * Created by ubuntu on 7/12/16.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private NotifactionUtil notificationUtils;
    Handler handler = new Handler();
    private static final String TAG_NEW_MESSAGE = "GCMIntentService";
    public static final String PUSH_RECEIVE_MESSAGE_ACTION = "com.teramatrix.vecv.PUSH_NEW_TICKET";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getData());
        Log.e(TAG,"To:"+remoteMessage.getNotification());
        if (remoteMessage == null)
            return;
        // Check if message contains a notification payload.

        String push_type = "";
        Bundle bundle ;
        Context context = getApplicationContext();

        /*
         * for (String key : bundle.keySet()) { Object value = bundle.get(key);
         *
         * System.out.println("GCMIntentService.onMessage() key:" + key +
         * "  value:" + value.toString() + "  value.getClass().getName():" +
         * value.getClass().getName()); }
         */

        String NotificationMessage = null;

            Object value = remoteMessage.getData().get("message");
            if (value != null) {
                push_type = value.toString();

        }

        String TicketId = null;

        if (!push_type.equalsIgnoreCase("")) {
            try {
                JSONObject obj = new JSONObject(push_type);
                if (obj.has("NotificationMessage")) {
                    NotificationMessage = obj.getString("NotificationMessage");
                }

                if (obj.has("TicketId")) {
                    TicketId = obj.getString("TicketId");
                }
            } catch (JSONException e) { // TODO Auto-generated catch block

                //Google Analytic -Tracking Exception
                EosApplication.getInstance().trackException(e);

                UtilityFunction.saveErrorLog(context, e);

            }
        }

        // Saving GCM Log in Log File
        if (ApplicationConstant.IS_LOG_SHOWN) {
            CompleteLogging.logGcmPush(context,
                    UtilityFunction.currentUTCTimeWithoutDash_Error_Time(),
                    NotificationMessage, TicketId,
                    ApplicationConstant.IS_APP_IN_FORGROUND);
        }

        if (NotificationMessage != null
                && NotificationMessage.equalsIgnoreCase("1")) {
            // New Ticket assigned from server.
            if (ApplicationConstant.currentActivityContext != null
                    && ApplicationConstant.IS_APP_IN_FORGROUND) {
                // App is running in foreground, send broadcast messgae to opend
                // activity to get Ticket from server and show pop-up,start
                // ringer .
                Intent in = new Intent(PUSH_RECEIVE_MESSAGE_ACTION);
                context.sendBroadcast(in);
            } else {
                // App is running in background,add ticket to pending ticket
                // queue(increase pending ticket counter), show notification on
                // status bar, start ringer .
                NewTicketManager newTicketManager = NewTicketManager
                        .getInstance(context);
                newTicketManager
                        .receviedNewTicketPush(ApplicationConstant.IS_APP_IN_FORGROUND);

            }

            // Temp here move to inside newTicketManager
            // .receviedNewTicketPush() method;
            VECVPreferences vecvPreferences = new VECVPreferences(context);
            vecvPreferences.setNewAssignedTicketId(TicketId);

        } else if (NotificationMessage != null
                && NotificationMessage.equalsIgnoreCase("2")) {

            // Check is newly assigned Ticket has been closed via server side
            // before user has accepted or decline ticket
            // Temp here move to inside newTicketManager
            // .receviedNewTicketPush() method;
            VECVPreferences vecvPreferences = new VECVPreferences(context);
            String previousAssignedTicketID = vecvPreferences
                    .getNewAssignedTicketId();
            if (false || previousAssignedTicketID != null && TicketId != null
                    && previousAssignedTicketID.equalsIgnoreCase(TicketId)) {

                NewTicketManager newTicketManager = NewTicketManager
                        .getInstance(context);
                newTicketManager.clearNewTicketAlert();
            } else {
                // Some Ticket status update on server occured.
                // call Synchronizing service which will fetch updated ticket
                // data
                // from server after previous synch timestamp.updated fetched
                // data
                // will be saved in local db. Now Local db will have same state
                // of
                // tickets data as server db has.
                Intent serviceIntent = new Intent(context,
                        SyncTicketsService.class);
                startService(serviceIntent);
            }

        }


//        Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.new_message), Toast.LENGTH_LONG).show();
//        // displayMessage(context, message);
//        // notifies user
//        generateNotification_New(getApplicationContext(), message);
    }


//        AlertDialogWithText alertDialogWithText = new AlertDialogWithText(getApplicationContext(), message);
//        alertDialogWithText.show();


    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotifactionUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotifactionUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }

    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        String title = context.getString(R.string.app_name);

        // Open Activity when notification is clicked
        // set intent so it does not start a new activity
        Intent notificationIntent = new Intent(context, MyTicketActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);


        //initialize notification
        Notification notification = new Notification(icon, message, when);
        //notification.setLatestEventInfo(context, title, message, pendingIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        //Notifiy
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);

    }

    private static void generateNotification_New(Context context, String message) {
        long when = System.currentTimeMillis();
        String title = context.getString(R.string.app_name);

        // Open Activity when notification is clicked
        // set intent so it does not start a new activity
        Intent notificationIntent = new Intent(context, MyTicketActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        //initialize notification
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setSound(uri)
                .setVibrate(new long[]{0, 1000, 1000, 1000, 1000})
                .setAutoCancel(true);

        //Notifiy
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, mBuilder.build());
    }
}
