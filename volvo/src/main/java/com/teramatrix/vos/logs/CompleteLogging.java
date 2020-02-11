package com.teramatrix.vos.logs;

import android.content.Context;

import com.teramatrix.vos.asynctasks.ApplicationLogging;
import com.teramatrix.vos.model.LogComplete;
import com.teramatrix.vos.utils.UtilityFunction;

public class CompleteLogging {

	private String GCM_LOG = "Gcm Log";

	public static void logGcmPush(Context context, String date_time,
			String push_type, String ticket_id, boolean is_app_in_foreground) {

		String app_state = null;
		if (is_app_in_foreground)
			app_state = "Foreground";
		else
			app_state = "Background";

		String log_message = "Gcm Log - " + date_time + " Message type:"
				+ push_type + " TicketId:" + ticket_id + " " + "App state:"
				+ app_state;

		LogComplete logComplete = new LogComplete();
		logComplete.setLog_message(log_message);
		new ApplicationLogging(context, logComplete).execute();
	}

	public static void logNewTicketApiCall(Context context, String message) {

		String date_time = UtilityFunction
				.currentUTCTimeWithoutDash_Error_Time();
		String log_message = "Get New Ticket Api Call - " + date_time
				+ " Message:" + message;

		LogComplete logComplete = new LogComplete();
		logComplete.setLog_message(log_message);
		new ApplicationLogging(context, logComplete).execute();

	}

	public static void logAcceptTicket(Context context, String message) {

		String date_time = UtilityFunction
				.currentUTCTimeWithoutDash_Error_Time();
		String log_message = "Accept Ticket- " + date_time + " Message:"
				+ message;

		LogComplete logComplete = new LogComplete();
		logComplete.setLog_message(log_message);
		new ApplicationLogging(context, logComplete).execute();

	}

	public static void logDeclineTicket(Context context, String message) {

		String date_time = UtilityFunction
				.currentUTCTimeWithoutDash_Error_Time();
		String log_message = "Decline Ticket- " + date_time + " Message:"
				+ message;

		LogComplete logComplete = new LogComplete();
		logComplete.setLog_message(log_message);
		new ApplicationLogging(context, logComplete).execute();
	}

	public static void logOffLineTicketing(Context context, String message) {

		String date_time = UtilityFunction
				.currentUTCTimeWithoutDash_Error_Time();
		String log_message = "OffLine Ticketing- " + date_time + " Message:"
				+ message;

		LogComplete logComplete = new LogComplete();
		logComplete.setLog_message(log_message);
		new ApplicationLogging(context, logComplete).execute();

	}

	public static void logUpdateTicketStatus(Context context, String message) {

		String date_time = UtilityFunction
				.currentUTCTimeWithoutDash_Error_Time();
		String log_message = "Update Ticket Status- " + date_time + " Message:"
				+ message;

		LogComplete logComplete = new LogComplete();
		logComplete.setLog_message(log_message);
		new ApplicationLogging(context, logComplete).execute();

	}

	public static void logSyncDB(Context context, String message) {

		String date_time = UtilityFunction
				.currentUTCTimeWithoutDash_Error_Time();
		String log_message = " Sync DB Log -"+date_time+" " + message;

		LogComplete logComplete = new LogComplete();
		logComplete.setLog_message(log_message);
		new ApplicationLogging(context, logComplete).execute();

	}
	public static void logNetworkState(Context context, String message) {

		String date_time = UtilityFunction
				.currentUTCTimeWithoutDash_Error_Time();
		String log_message = "logNetworkState-"+" "+date_time+" " + message;

		LogComplete logComplete = new LogComplete();
		logComplete.setLog_message(log_message);
		new ApplicationLogging(context, logComplete).execute();

	}
}
