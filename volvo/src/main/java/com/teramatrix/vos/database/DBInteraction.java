package com.teramatrix.vos.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.model.DeclineReasonModel;
import com.teramatrix.vos.model.EstimationCostModel;
import com.teramatrix.vos.model.Ticket;
import com.teramatrix.vos.model.TrackingLocation;
import com.teramatrix.vos.model.UpdateCounter;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.UtilityFunction;

/**
 * 
 * DBInteraction Class is used for Local Database. Used to perform Database
 * connectivity, Queries to the database, close database connection.
 * 
 */

public class DBInteraction extends SQLiteOpenHelper {

	private static DBInteraction _instance = null;
	private static String DB_PATH = "/data/data/com.teramatrix.vos/databases/";
	// private static String DB_NAME = "VECV_DB.db";
	private static String DB_NAME = "VOC_DB.sqlite";
	public boolean islockDbConnection = true;
	private SQLiteDatabase sqliteDb = null;
	public Context context = null;

	// Tables
	private String TABLE_TRACKING = "TrackingLocation";
	private String TABLE_NEW_TICKET = "NewTicket";
	private String TABLE_OPEN_TICKET = "OpenTickets";
	private String TABLE_OPEN_TICKET_ACTIVITY = "OpenTicketsActivity";
	private String TABLE_CLOSED_TICKET = "ClosedTickets";
	private String TABLE_TICKET_STATUS = "TicketStatus";
	private String TABLE_TICKET_REASON = "TicketActionReasons";
	private String TABLE_ESTIAMTION_COST = "RepairingEstimatedCost";
	private String TABLE_TIMESTAMP = "TimeStamp";
	private String TABLE_UPDATE_COUNTER = "UpdateCounter";

	// Table fields
	// TABLE_TRACKING fields added battery label,chaging,gps state.
	private String FIELD_TABLE_TRACKING_LATITUDE = "latitude";
	private String FIELD_TABLE_TRACKING_LONGITUDE = "longitude";
	private String FIELD_TABLE_TRACKING_LOGTIME = "log_time";
	private String FIELD_TABLE_TRACKING_BATTERY_LABEL = "battery_label";
	private String FIELD_TABLE_TRACKING_BATTERY_CHARGING = "battery_charging";
	private String FIELD_TABLE_TRACKING_GPS_STATE = "gps_state";
	private String FIELD_TABLE_TRACKING_POWER_SAVING_MODE_STATUS = "is_power_saving_on";

	// Table fields
	// New Ticket
	private String FIELD_TICKET_ID = "ticket_id";
	private String FIELD_BREAKDOWN_LOCATION = "breakdown_location";
	private String FIELD_BREAKDOWN_LATITUDE = "breakdown_latitude";
	private String FIELD_BREAKDOWN_LONGITUDE = "breakdown_longitude";
	private String FIELD_BREAKDOWN_VEHICAL_TYPE = "vehical_type";
	private String FIELD_BREAKDOWN_PROBLEM_DESCRIPTION = "description";
	private String FIELD_TICKET_STATUS = "ticket_status";
	private String FIELD_TIME_SLA = "time_total_ticket_lifecycle_sla";
	private String FIELD_TIME_SLA_ACHEVIED = "time_sla_acheived";
	private String FIELD_CUSTOMER_CONTACT_NUMBER = "customer_contact_number";
	private String FIELD_VEHICAL_OWNER_CONTACT_NUMBER = "OwnerContact_no";

	private String FIELD_CHARGABLE_DISTANCE = "estimated_distance";
	private String FIELD_TIME_ACHEVIED_ESTIMATION_TIME = "achevied_estimated_time_for_jobcomplition";
	private String FIELD_TIME_ESTIMATED_TIME_TO_JOB_COMPLITION = "time_estimated_for_job_completion_by_van";
	private String FIELD_ESTIMATED_COST_TO_JOB_COMPLITION = "estimated_cost_for_repairing";
	private String FIELD_SUGGESTION_COMMENT = "suggestion_comment";
	private String FIELD_TIME_TICKET_CREATION_TIME = "time_creation";
	private String FIELD_TIME_LAST_MODIFIED = "time_last_modified";
	private String FIELD_TIME_LAST_MODIFIED_IN_MILLI = "time_last_modified_in_milli";
	private String FIELD_PRIORITY = "priority";
	private String FIELD_VEHICAL_TYPE = "vehical_type";
	private String FIELD_VEHICAL_REGISTRATIONNO = "vehical_registration_number";
	private String FIELD_ESTIMATED_TIME_UPDATE_COUNTER = "estimated_time_update_counte";
	private String FIELD_ESTIMATED_COST_UPDATE_COUNTER = "estimated_cost_update_counter";
	private String FIELD_IS_TRIP_END = "is_trip_end";
	
	

	// Field ticket status
	private String FIELD_STATUS_SEQUENCE_ORDER = "sequence_order";
	private String FIELD_STATUS_NAME = "status_name";
	private String FIELD_STATUS_ALIAS = "alias";

	// Ticket Action Reasons
	private String FIELD_REASON_ID = "reason_id";
	private String FIELD_REASON_TYPE = "reason_type";
	private String FIELD_REASON_TEXT = "reason_text";

	// estiamtion cost table field
	private String FIELD_COST_ID = "cost_id";
	private String FIELD_COST_VALUE = "cost_value";

	// timestamp field
	private String FIELD_TIMESTAMP = "timestamp";
	private String FIELD_TIMESTAMP_ID = "id";

	// private static int db_version = 2;

	// Power saving field (is_power_saving_mode_on) added in Tracking table
	private static int db_version = 7;

	public DBInteraction(Context context) {

		super(context, "VOC_DB", null, db_version);

	}

	/*
	 * public DBInteraction(Context context) {
	 * 
	 * // super(context, "VECV_DB",null, 1); }
	 */

	/**
	 * Singleton instance of this class.
	 * 
	 * @return
	 */
	public static DBInteraction getInstance(Context context) {
		if (_instance == null) {
			_instance = new DBInteraction(context);
		}
		return _instance;
	}

	/**
	 * Check if local database exist in the mobile device or not. it returns
	 * true or false.
	 * 
	 * @return
	 */
	public boolean isDBExist() {
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.NO_LOCALIZED_COLLATORS
							| SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException ex) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(ex);
			UtilityFunction.saveErrorLog(context, ex);

		}
		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	/**
	 * @param context
	 *            copyDbFromAssets Method will keep Context as a parameter and
	 *            if database is not present in mobile, it will copy.
	 */
	public void copyDbFromAssets(Context context) {
		try {

			InputStream myInput = context.getAssets().open(DB_NAME);
			String outFileName = DB_PATH + DB_NAME;

			File databaseFile = new File(DB_PATH);
			// check if databases folder exists, if not create one and its
			// subfolders
			if (!databaseFile.exists()) {
				databaseFile.mkdir();
			}

			OutputStream myOutput = new FileOutputStream(outFileName);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}
			myOutput.flush();
			myOutput.close();
			myInput.close();
		} catch (IOException ex) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(ex);
			UtilityFunction.saveErrorLog(context, ex);
		} catch (SQLiteException ex) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(ex);
			UtilityFunction.saveErrorLog(context, ex);

		} catch (Exception ex) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(ex);
			UtilityFunction.saveErrorLog(context, ex);
		}
	}

	// check and get connection
	public void getConnection() {

		if (sqliteDb == null) {
			try {
				sqliteDb = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null,
						SQLiteDatabase.NO_LOCALIZED_COLLATORS
								| SQLiteDatabase.OPEN_READWRITE);
			} catch (Exception ex) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(ex);
				UtilityFunction.saveErrorLog(context, ex);
				sqliteDb = null;
			}
		}
	}

	/**
	 * Used to close a connection with local database
	 */
	public void closeConnection() {

		if (sqliteDb != null) {
			if (sqliteDb.isOpen() == true) {
				sqliteDb.close();
				sqliteDb = null;
			}
		}
	}

	/**
	 * @param reasonTypeId
	 * @param reasonTypeName
	 * @return used to insert Reason types in the local database. and will
	 *         return a long value if it is not equal to "-1" it means data has
	 *         been inserted in the local database successfully.
	 */
	public long addReasonType(String reasonTypeId, String reasonTypeName) {
		if (sqliteDb != null) {
			try {
				return sqliteDb.insert("ReasonType", null,
						addReasonTypeFields(reasonTypeId, reasonTypeName));
			} catch (Exception e) {

				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		return 0;
	}

	/**
	 * @param reasonTypeId
	 * @param reasonTypeName
	 * @return Pass reasonTypeId and reasonTypeName to Table "ReasonType". in
	 *         the table there are two coloum with the name of ReasonTypeId and
	 *         ReasonTypeName.
	 */
	private ContentValues addReasonTypeFields(String reasonTypeId,
			String reasonTypeName) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("ReasonTypeId", reasonTypeId);
		contentValues.put("ReasonTypeName", reasonTypeName);
		return contentValues;
	}

	/*
	 * Used to Check if Tracking data stored while Offline mode , It is called
	 * when Networks becomes available
	 */
	public Boolean isTrackingDataExists() {

		try {
			if (sqliteDb != null) {
				Cursor cursor = sqliteDb.rawQuery("select * from "
						+ TABLE_TRACKING + ";", null);
				if (cursor != null) {
					cursor.moveToFirst();
					if (cursor.getCount() > 0) {
						cursor.close();
						return true;
					}
				}
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			UtilityFunction.saveErrorLog(context, e);
		}

		return false;
	}

	/*
	 * Used to Check if Ticket data stored while Offline mode , It is called
	 * when Networks becomes available
	 */
	public Boolean isOffLineTicketDataExists() {

		try {
			if (sqliteDb != null) {
				Cursor cursor = sqliteDb.rawQuery("select * from "
						+ TABLE_OPEN_TICKET_ACTIVITY + ";", null);
				if (cursor != null) {
					cursor.moveToFirst();
					if (cursor.getCount() > 0) {
						cursor.close();
						return true;
					}
				}
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			UtilityFunction.saveErrorLog(context, e);
		}

		return false;
	}

	/**
	 * 
	 * used to insert tracking data in the local database in offline mode. and
	 * will return a long value .if it is not equal to "-1" it means data has
	 * been inserted in the local database successfully.
	 */
	public long addTrackingData(String lat, String lng, String time,
			String battery_label, String battery_charging, String gps_state,
			String is_power_saving_mode_on) {
		if (sqliteDb != null) {
			try {
				return sqliteDb.insert(
						TABLE_TRACKING,
						null,
						addTrackingFields(lat, lng, time, battery_label,
								battery_charging, gps_state,
								is_power_saving_mode_on));
			} catch (Exception e) {

				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		return 0;
	}

	/**
	 * 
	 * Pass latitude, longitude and timeline to Table "Tracking". in the table
	 * there are two coloum with the name of ReasonTypeId and ReasonTypeName.
	 */
	private ContentValues addTrackingFields(String latitude, String longitude,
			String timeline, String battery_label, String battery_charging,
			String gps_state, String is_power_saving_mode_on) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(FIELD_TABLE_TRACKING_LATITUDE, latitude);
		contentValues.put(FIELD_TABLE_TRACKING_LONGITUDE, longitude);
		contentValues.put(FIELD_TABLE_TRACKING_LOGTIME, timeline);

		contentValues.put(FIELD_TABLE_TRACKING_BATTERY_LABEL, battery_label);
		contentValues.put(FIELD_TABLE_TRACKING_BATTERY_CHARGING,
				battery_charging);
		contentValues.put(FIELD_TABLE_TRACKING_GPS_STATE, gps_state);
		contentValues.put(FIELD_TABLE_TRACKING_POWER_SAVING_MODE_STATUS,
				is_power_saving_mode_on);

		return contentValues;
	}

	/**
	 * 
	 * Get all Locacally saved Tracking data during offline mode,Called when
	 * networks becomes available . All data are uploaded to server then
	 */

	public ArrayList<TrackingLocation> getOfflineTrackingData() {
		ArrayList<TrackingLocation> trackingLocations = new ArrayList<TrackingLocation>();
		try {
			if (sqliteDb != null) {
				// Runs the provided SQL and returns a Cursor over the result //
				// set.
				Cursor cursor = sqliteDb.rawQuery("select * from "
						+ TABLE_TRACKING + ";", null);
				if (cursor != null & cursor.getColumnCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {

						String latitude = cursor.getString(cursor
								.getColumnIndex(FIELD_TABLE_TRACKING_LATITUDE));
						String longitude = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TABLE_TRACKING_LONGITUDE));
						String logtime = cursor.getString(cursor
								.getColumnIndex(FIELD_TABLE_TRACKING_LOGTIME));

						String battery_label = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TABLE_TRACKING_BATTERY_LABEL));

						String battery_charging = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TABLE_TRACKING_BATTERY_CHARGING));

						String gps_state = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TABLE_TRACKING_GPS_STATE));

						
						
						//This Line of Code was throwing illegalstateException. 
						//handle it on try catch
						String is_power_saving_mode_on = "";
						try
						{
						 is_power_saving_mode_on = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TABLE_TRACKING_POWER_SAVING_MODE_STATUS));
						}catch(Exception e)
						{
							is_power_saving_mode_on = "false";
//							e.printStackTrace();
						}

						TrackingLocation location = new TrackingLocation();
						location.setLatitude(latitude);
						location.setLogtime(logtime);
						location.setLongitude(longitude);
						location.setBattery_label(battery_label);
						location.setBattery_charging(battery_charging);
						location.setGps_state(gps_state);
						location.setIs_power_saving_mode_on(is_power_saving_mode_on);

						trackingLocations.add(location);

						cursor.moveToNext();
					}
					cursor.close();
					return trackingLocations;
				}
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			UtilityFunction.saveErrorLog(context, e);
		}
		return null;
	}

	/**
	 * 
	 * Get all Locacally saved Ticket data during offline mode,Called when
	 * networks becomes available . All data are uploaded to server then
	 */
	public ArrayList<Ticket> getOfflineTicketData() {
		ArrayList<Ticket> offLineTicketActivity = new ArrayList<Ticket>();
		try {
			if (sqliteDb != null) {
				// Runs the provided SQL and returns a Cursor over the result //
				// set.
				Cursor cursor = sqliteDb.rawQuery("select * from "
						+ TABLE_OPEN_TICKET_ACTIVITY + ";", null);
				if (cursor != null & cursor.getColumnCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {

						String ticket_id = cursor.getString(cursor
								.getColumnIndex(FIELD_TICKET_ID));
						String ticket_status = cursor.getString(cursor
								.getColumnIndex(FIELD_TICKET_STATUS));
						String ticket_last_modified_time = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_LAST_MODIFIED));
						String ticket_breakdown_location = cursor
								.getString(cursor
										.getColumnIndex(FIELD_BREAKDOWN_LOCATION));
						String ticket_breakdown_latitude = cursor
								.getString(cursor
										.getColumnIndex(FIELD_BREAKDOWN_LATITUDE));
						String ticket_breakdown_longitude = cursor
								.getString(cursor
										.getColumnIndex(FIELD_BREAKDOWN_LONGITUDE));
						String ticket_problem_description = cursor
								.getString(cursor
										.getColumnIndex(FIELD_BREAKDOWN_PROBLEM_DESCRIPTION));
						String ticket_sla_time = cursor.getString(cursor
								.getColumnIndex(FIELD_TIME_SLA));
						String slaTimeAchevied = cursor.getString(cursor
								.getColumnIndex(FIELD_TIME_SLA_ACHEVIED));
						String ticket_creation_time = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_TICKET_CREATION_TIME));

						String lastmodified_time_in_millisec = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_LAST_MODIFIED_IN_MILLI));

						String estimatedTimeForJobCompletion = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_ESTIMATED_TIME_TO_JOB_COMPLITION));

						String estimatedCostForJobCompletion = cursor
								.getString(cursor
										.getColumnIndex(FIELD_ESTIMATED_COST_TO_JOB_COMPLITION));

						String acheviedEstimationTimeForJobComplition = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_ACHEVIED_ESTIMATION_TIME));
						String suggestion_comment = cursor.getString(cursor
								.getColumnIndex(FIELD_SUGGESTION_COMMENT));
						
						String priority = cursor.getString(cursor
								.getColumnIndex(FIELD_PRIORITY));
						
						

						Ticket ticket = new Ticket();
						ticket.Id = ticket_id;
						ticket.TicketStatus = ticket_status;
						ticket.LastModifiedTime = ticket_last_modified_time;
						ticket.LastModifiedTimeInMilliSec = lastmodified_time_in_millisec;
						ticket.BreakDownLocation = ticket_breakdown_location;
						ticket.BreackDownLatitude = ticket_breakdown_latitude;
						ticket.BreackDownLongitude = ticket_breakdown_longitude;
						ticket.Description = ticket_problem_description;
						ticket.TotalTicketLifeCycleTimeSlab = ticket_sla_time;
						ticket.SlaTimeAchevied = slaTimeAchevied;
						ticket.CreationTime = ticket_creation_time;
						ticket.EstimatedTimeForJobComplition = estimatedTimeForJobCompletion;
						ticket.EstimatedCostForJobComplition = estimatedCostForJobCompletion;
						ticket.AcheviedEstimatedTimeForJobComplition = acheviedEstimationTimeForJobComplition;
						ticket.SuggestionComment = suggestion_comment;
						ticket.Priority = priority;

						offLineTicketActivity.add(ticket);

						cursor.moveToNext();
					}
					cursor.close();
					return offLineTicketActivity;
				}
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			UtilityFunction.saveErrorLog(context, e);
		}
		return null;
	}

	/*
	 * Delete Local stored Trracking data , called after uploading offline data
	 * to server
	 */
	public void deleteOfflineTrackingData() {
		if (sqliteDb != null) {
			try {
				int deleted_records = sqliteDb.delete(TABLE_TRACKING, null,
						null);
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
	}

	/*
	 * Delete Local stored Ticket data , called after uploading offline data to
	 * server
	 */
	public int deleteOfflineTicketData() {
		if (sqliteDb != null) {
			try {

				int deleted_records = sqliteDb.delete(
						TABLE_OPEN_TICKET_ACTIVITY, null, null);
				return deleted_records;
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		return 0;
	}

	/**
	 * countReasonType(), get number of reason types from ReasonType table
	 * 
	 * @return
	 */
	public int countReasonType() {
		int count = 0;
		if (sqliteDb != null) {
			try {
				Cursor mCount = sqliteDb.rawQuery(
						"select count(*) from ReasonType", null);
				mCount.moveToFirst();
				count = mCount.getInt(0);
				mCount.close();
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		return count;
	}

	/**
	 * @param reasonName
	 * @return In order to get ReasonTypeId from the ReasonTypeName, we are
	 *         performing select query with where clause. ReasonTypeName is used
	 *         to pass in the API instead of ReasonTypeName. it will return a
	 *         string values as a reasonTypeId.
	 */
	public String getReasonTypeId(String reasonName) {
		String reasonTypeID = null;
		try {
			if (sqliteDb != null) {
				Cursor cursor = sqliteDb.rawQuery(
						"select ReasonTypeId from ReasonType where ReasonTypeName = '"
								+ reasonName + "';", null);
				if (cursor != null & cursor.getColumnCount() > 0) {
					cursor.moveToFirst();
					reasonTypeID = cursor.getString(cursor
							.getColumnIndex("ReasonTypeId"));
					cursor.close();
				}
				cursor.close();
				return reasonTypeID;
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			UtilityFunction.saveErrorLog(context, e);
		}
		return reasonTypeID;
	}

	// New Ticket table , insert new ticket details when GCM pushed recevied
	public long addNewTicket(String ticket_id, String breakdown_location,
			String breakdown_latitude, String breakdown_longitude,
			String breakdown_vehical_type, String problem_description,
			String time_sla, String time_sla_acheived,
			String time_ticket_assigned_time, String vehical_type,
			String creationTimeInMillisecond, String customer_contact_number,
			String OwnerContact_no,String priority) {
		if (sqliteDb != null) {
			try {

				return sqliteDb.insert(
						TABLE_NEW_TICKET,
						null,
						addNewTicketFields(ticket_id, breakdown_location,
								breakdown_latitude, breakdown_longitude,
								breakdown_vehical_type, problem_description,
								time_sla, time_sla_acheived,
								time_ticket_assigned_time, vehical_type,
								creationTimeInMillisecond,
								customer_contact_number, OwnerContact_no,priority));
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		return 0;
	}

	/* Column to Value mapping */
	private ContentValues addNewTicketFields(String ticket_id,
			String breakdown_location, String breakdown_latitude,
			String breakdown_longitude, String breakdown_vehical_type,
			String problem_description, String time_sla,
			String time_sla_acheived, String time_ticket_assigned_time,
			String vehical_type, String creationTimeInMillisecond,
			String customer_contact_number, String OwnerContact_no,String priority) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(FIELD_TICKET_ID, ticket_id);
		contentValues.put(FIELD_BREAKDOWN_LOCATION, breakdown_location);
		contentValues.put(FIELD_BREAKDOWN_LATITUDE, breakdown_latitude);
		contentValues.put(FIELD_BREAKDOWN_LONGITUDE, breakdown_longitude);
		contentValues.put(FIELD_BREAKDOWN_VEHICAL_TYPE, breakdown_vehical_type);
		contentValues.put(FIELD_BREAKDOWN_PROBLEM_DESCRIPTION,
				problem_description);
		contentValues.put(FIELD_TIME_SLA, time_sla);
		contentValues.put(FIELD_TIME_SLA_ACHEVIED, time_sla_acheived);
		contentValues.put(FIELD_TIME_TICKET_CREATION_TIME,
				time_ticket_assigned_time);
		contentValues.put(FIELD_VEHICAL_TYPE, vehical_type);
		contentValues.put(FIELD_TIME_LAST_MODIFIED_IN_MILLI,
				creationTimeInMillisecond);
		contentValues.put(FIELD_CUSTOMER_CONTACT_NUMBER,
				customer_contact_number);
		contentValues.put(FIELD_VEHICAL_OWNER_CONTACT_NUMBER, OwnerContact_no);
		contentValues.put(FIELD_PRIORITY, priority);

		return contentValues;
	}

	/* Delete New Ticket data When ticket is accepted or Declined */
	public void deleteNewTicketData() {
		if (sqliteDb != null) {
			try {
				int i = sqliteDb.delete(TABLE_NEW_TICKET, null, null);
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
	}

	// Table Operation for MyTicket
	// Add Opend Ticket Details
	public long addOpenTicket(String ticket_id, String ticket_status,
			String breakdown_location, String breakdown_latitude,
			String breakdown_longitude, String breakdown_vehical_type,
			String problem_description, String time_sla,
			String time_sla_acheived, String time_ticket_assigned_time,
			String ticket_last_modified_time,
			String ticket_last_modified_time_in_milli, String priority,
			String vehical_registration_number, String vehical_type,
			String estimatedTimeForJobCompletion,
			String estimated_cost_for_repairing,
			String AcheviedEstimatedTimeForJobComplition,
			String suggestion_comment, String customer_contact_number,
			String estimated_distance, String OwnerContact_no,String is_trip_end) {
		if (sqliteDb != null) {
			try {

				/* Check if Ticket already exists in table or not */
				boolean ifAlreadyinDb = CheckIsDataAlreadyInDBorNot(
						TABLE_OPEN_TICKET, FIELD_TICKET_ID, ticket_id);

				/* If Ticket does not exist ,insert it */
				if (!ifAlreadyinDb) {
					return sqliteDb.insert(
							TABLE_OPEN_TICKET,
							null,
							addOpenTicketFields(ticket_id, ticket_status,
									breakdown_location, breakdown_latitude,
									breakdown_longitude,
									breakdown_vehical_type,
									problem_description, time_sla,
									time_sla_acheived,
									time_ticket_assigned_time,
									ticket_last_modified_time,
									ticket_last_modified_time_in_milli,
									priority, vehical_registration_number,
									vehical_type,
									estimatedTimeForJobCompletion,
									estimated_cost_for_repairing,
									AcheviedEstimatedTimeForJobComplition,
									suggestion_comment,
									customer_contact_number,
									estimated_distance, OwnerContact_no,is_trip_end));
				} else {

					/* If ticket already exists, update that */
					return sqliteDb.update(
							TABLE_OPEN_TICKET,
							addOpenTicketFields(ticket_id, ticket_status,
									breakdown_location, breakdown_latitude,
									breakdown_longitude,
									breakdown_vehical_type,
									problem_description, time_sla,
									time_sla_acheived,
									time_ticket_assigned_time,
									ticket_last_modified_time,
									ticket_last_modified_time_in_milli,
									priority, vehical_registration_number,
									vehical_type,
									estimatedTimeForJobCompletion,
									estimated_cost_for_repairing,
									AcheviedEstimatedTimeForJobComplition,
									suggestion_comment,
									customer_contact_number,
									estimated_distance, OwnerContact_no,is_trip_end),
							FIELD_TICKET_ID + " = ?",
							new String[] { ticket_id });
				}
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		return 0;
	}

	/* Add Closed tickets to table */
	public long addClosedTicket(String ticket_id, String ticket_status,
			String breakdown_location, String breakdown_latitude,
			String breakdown_longitude, String breakdown_vehical_type,
			String problem_description, String time_sla,
			String time_sla_acheived, String time_ticket_assigned_time,
			String ticket_last_modified_time,
			String ticket_last_modified_time_in_milli, String priority,
			String vehical_registration_number, String vehical_type,
			String estimatedTimeForJobCompletion,
			String estimated_cost_for_repairing,
			String AcheviedEstimatedTimeForJobComplition,
			String suggestion_comment, String customer_contact_number,
			String estimated_distance, String OwnerContact_no,String is_trip_end) {
		if (sqliteDb != null) {
			try {

				/* Check if Ticket already exists in table or not */
				boolean ifAlreadyinDb = CheckIsDataAlreadyInDBorNot(
						TABLE_CLOSED_TICKET, FIELD_TICKET_ID, ticket_id);

				/* If Ticket does not exist ,insert it */
				if (!ifAlreadyinDb) {
					long b = sqliteDb.insert(
							TABLE_CLOSED_TICKET,
							null,
							addOpenTicketFields(ticket_id, ticket_status,
									breakdown_location, breakdown_latitude,
									breakdown_longitude,
									breakdown_vehical_type,
									problem_description, time_sla,
									time_sla_acheived,
									time_ticket_assigned_time,
									ticket_last_modified_time,
									ticket_last_modified_time_in_milli,
									priority, vehical_registration_number,
									vehical_type,
									estimatedTimeForJobCompletion,
									estimated_cost_for_repairing,
									AcheviedEstimatedTimeForJobComplition,
									suggestion_comment,
									customer_contact_number,
									estimated_distance, OwnerContact_no,is_trip_end));
				} else {
					/* If ticket already exists, update that */
					return sqliteDb.update(
							TABLE_CLOSED_TICKET,
							addOpenTicketFields(ticket_id, ticket_status,
									breakdown_location, breakdown_latitude,
									breakdown_longitude,
									breakdown_vehical_type,
									problem_description, time_sla,
									time_sla_acheived,
									time_ticket_assigned_time,
									ticket_last_modified_time,
									ticket_last_modified_time_in_milli,
									priority, vehical_registration_number,
									vehical_type,
									estimatedTimeForJobCompletion,
									estimated_cost_for_repairing,
									AcheviedEstimatedTimeForJobComplition,
									suggestion_comment,
									customer_contact_number,
									estimated_distance, OwnerContact_no,is_trip_end),
							FIELD_TICKET_ID + " = ?",
							new String[] { ticket_id });
				}
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		return 0;
	}

	/* Column to Value Mapping for OpenedTicket */
	private ContentValues addOpenTicketFields(String ticket_id,
			String ticket_status, String breakdown_location,
			String breakdown_latitude, String breakdown_longitude,
			String breakdown_vehical_type, String problem_description,
			String time_sla, String time_sla_acheived,
			String time_ticket_assigned_time, String ticket_last_modified_time,
			String ticket_last_modified_time_in_milli, String priority,
			String vehical_registration_number, String vehical_type,
			String estimatedTimeForJobCompletion,
			String estimated_cost_for_repairing,
			String AcheviedEstimatedTimeForJobComplition,
			String suggestion_comment, String customer_contact_number,
			String estimated_distance, String OwnerContact_no,String is_trip_end) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(FIELD_TICKET_ID, ticket_id);
		contentValues.put(FIELD_BREAKDOWN_LOCATION, breakdown_location);
		contentValues.put(FIELD_BREAKDOWN_LATITUDE, breakdown_latitude);
		contentValues.put(FIELD_BREAKDOWN_LONGITUDE, breakdown_longitude);
		contentValues.put(FIELD_BREAKDOWN_VEHICAL_TYPE, breakdown_vehical_type);
		contentValues.put(FIELD_BREAKDOWN_PROBLEM_DESCRIPTION,
				problem_description);
		contentValues.put(FIELD_TIME_SLA, time_sla);
		contentValues.put(FIELD_TIME_SLA_ACHEVIED, time_sla_acheived);

		contentValues.put(FIELD_TIME_ESTIMATED_TIME_TO_JOB_COMPLITION,
				estimatedTimeForJobCompletion);

		contentValues.put(FIELD_TIME_TICKET_CREATION_TIME,
				time_ticket_assigned_time);
		contentValues.put(FIELD_TICKET_STATUS, ticket_status);
		contentValues.put(FIELD_TIME_LAST_MODIFIED, ticket_last_modified_time);
		contentValues.put(FIELD_TIME_LAST_MODIFIED_IN_MILLI,
				ticket_last_modified_time_in_milli);
		contentValues.put(FIELD_PRIORITY, priority);
		contentValues.put(FIELD_VEHICAL_REGISTRATIONNO,
				vehical_registration_number);
		contentValues.put(FIELD_ESTIMATED_COST_TO_JOB_COMPLITION,
				estimated_cost_for_repairing);

		contentValues.put(FIELD_TIME_ACHEVIED_ESTIMATION_TIME,
				AcheviedEstimatedTimeForJobComplition);

		contentValues.put(FIELD_SUGGESTION_COMMENT, suggestion_comment);
		contentValues.put(FIELD_CUSTOMER_CONTACT_NUMBER,
				customer_contact_number);
		contentValues.put(FIELD_CHARGABLE_DISTANCE, estimated_distance);
		contentValues.put(FIELD_VEHICAL_OWNER_CONTACT_NUMBER, OwnerContact_no);
		contentValues.put(FIELD_IS_TRIP_END, is_trip_end);
		
		

		return contentValues;
	}

	/* Columnt to value mapping for OpenTicketActivity */
	private ContentValues addOpenTicketActivityFields(String ticket_id,
			String ticket_status, String time_sla, String breakDown_location,
			String breakdown_lattitude, String breakdown_longitude,
			String time_sla_acheived, String description, String creation_time,
			String ticket_last_modified_time,
			String ticket_last_modified_time_in_milli,
			String estimatedTimeForJobCompletion,
			String estimated_cost_for_repairing,
			String AcheviedEstimatedTimeForJobComplition,
			String suggestion_comment,String priority,String is_trip_end) {

		ContentValues contentValues = new ContentValues();
		contentValues.put(FIELD_TICKET_ID, ticket_id);
		contentValues.put(FIELD_TICKET_STATUS, ticket_status);
		contentValues.put(FIELD_TIME_LAST_MODIFIED, ticket_last_modified_time);
		contentValues.put(FIELD_BREAKDOWN_LOCATION, breakDown_location);
		contentValues.put(FIELD_BREAKDOWN_LATITUDE, breakdown_lattitude);
		contentValues.put(FIELD_BREAKDOWN_LONGITUDE, breakdown_longitude);
		contentValues.put(FIELD_BREAKDOWN_PROBLEM_DESCRIPTION, description);
		contentValues.put(FIELD_TIME_SLA, time_sla);
		contentValues.put(FIELD_TIME_SLA_ACHEVIED, time_sla_acheived);
		contentValues.put(FIELD_TIME_TICKET_CREATION_TIME, creation_time);
		contentValues.put(FIELD_TIME_LAST_MODIFIED_IN_MILLI,
				ticket_last_modified_time_in_milli);
		contentValues.put(FIELD_TIME_ESTIMATED_TIME_TO_JOB_COMPLITION,
				estimatedTimeForJobCompletion);
		contentValues.put(FIELD_ESTIMATED_COST_TO_JOB_COMPLITION,
				estimated_cost_for_repairing);
		contentValues.put(FIELD_TIME_ACHEVIED_ESTIMATION_TIME,
				AcheviedEstimatedTimeForJobComplition);
		contentValues.put(FIELD_SUGGESTION_COMMENT, suggestion_comment);
		contentValues.put(FIELD_PRIORITY, priority);
		contentValues.put(FIELD_IS_TRIP_END, is_trip_end);
		
		
		

		return contentValues;
	}

	/* Check specific record value in a table */
	public boolean CheckIsDataAlreadyInDBorNot(String TableName,
			String dbfield, String fieldValue) {
		String Query = "Select * from " + TableName + " where " + dbfield
				+ " = '" + fieldValue + "'";
		Cursor cursor = sqliteDb.rawQuery(Query, null);
		if (cursor.getCount() <= 0) {
			return false;
		}
		return true;
	}

	/* Get all Opened Tickets Record */
	public ArrayList<Ticket> getAllOpenTickets() {
		ArrayList<Ticket> allOpenticketsList = new ArrayList<Ticket>();
		try {
			if (sqliteDb != null) {
				// Runs the provided SQL and returns a Cursor over the result //
				// set.
				Cursor cursor = sqliteDb.rawQuery("select * from "
						+ TABLE_OPEN_TICKET + " ORDER BY "
						+ FIELD_TIME_LAST_MODIFIED_IN_MILLI + " DESC " + ";",
						null);
				if (cursor != null & cursor.getColumnCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {

						String ticket_id = cursor.getString(cursor
								.getColumnIndex(FIELD_TICKET_ID));
						String ticket_status = cursor.getString(cursor
								.getColumnIndex(FIELD_TICKET_STATUS));
						String ticket_last_modified_time = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_LAST_MODIFIED));
						String ticket_breakdown_location = cursor
								.getString(cursor
										.getColumnIndex(FIELD_BREAKDOWN_LOCATION));
						String ticket_breakdown_latitude = cursor
								.getString(cursor
										.getColumnIndex(FIELD_BREAKDOWN_LATITUDE));
						String ticket_breakdown_longitude = cursor
								.getString(cursor
										.getColumnIndex(FIELD_BREAKDOWN_LONGITUDE));
						String ticket_problem_description = cursor
								.getString(cursor
										.getColumnIndex(FIELD_BREAKDOWN_PROBLEM_DESCRIPTION));
						String ticket_sla_time = cursor.getString(cursor
								.getColumnIndex(FIELD_TIME_SLA));
						String slaTimeAchevied = cursor.getString(cursor
								.getColumnIndex(FIELD_TIME_SLA_ACHEVIED));
						String ticket_creation_time = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_TICKET_CREATION_TIME));

						String lastmodified_time_in_millisec = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_LAST_MODIFIED_IN_MILLI));

						String estimatedTimeForJobCompletion = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_ESTIMATED_TIME_TO_JOB_COMPLITION));

						String estimatedCostForJobCompletion = cursor
								.getString(cursor
										.getColumnIndex(FIELD_ESTIMATED_COST_TO_JOB_COMPLITION));

						String acheviedEstimationTimeForJobComplition = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_ACHEVIED_ESTIMATION_TIME));
						String suggestion_comment = cursor.getString(cursor
								.getColumnIndex(FIELD_SUGGESTION_COMMENT));

						String CustomeContact_no = cursor.getString(cursor
								.getColumnIndex(FIELD_CUSTOMER_CONTACT_NUMBER));

						String estimated_distance = cursor.getString(cursor
								.getColumnIndex(FIELD_CHARGABLE_DISTANCE));
						String owner_contact_number = cursor
								.getString(cursor
										.getColumnIndex(FIELD_VEHICAL_OWNER_CONTACT_NUMBER));
						
						String priority = cursor
								.getString(cursor
										.getColumnIndex(FIELD_PRIORITY));
						
						String is_trip_end = cursor
								.getString(cursor
										.getColumnIndex(FIELD_IS_TRIP_END));

						/*
						 * String estimated_cost_update_counter = cursor
						 * .getString(cursor
						 * .getColumnIndex(FIELD_ESTIMATED_COST_UPDATE_COUNTER
						 * )); String estimated_time_update_counter = cursor
						 * .getString(cursor
						 * .getColumnIndex(FIELD_ESTIMATED_TIME_UPDATE_COUNTER
						 * ));
						 * 
						 * System.out.println("estimated_time_update_counter:" +
						 * estimated_time_update_counter);
						 */
						Ticket ticket = new Ticket();
						ticket.Id = ticket_id;
						ticket.TicketStatus = ticket_status;
						ticket.LastModifiedTime = ticket_last_modified_time;
						ticket.LastModifiedTimeInMilliSec = lastmodified_time_in_millisec;
						ticket.BreakDownLocation = ticket_breakdown_location;
						ticket.BreackDownLatitude = ticket_breakdown_latitude;
						ticket.BreackDownLongitude = ticket_breakdown_longitude;
						ticket.Description = ticket_problem_description;
						ticket.TotalTicketLifeCycleTimeSlab = ticket_sla_time;
						ticket.SlaTimeAchevied = slaTimeAchevied;
						ticket.CreationTime = ticket_creation_time;
						ticket.EstimatedTimeForJobComplition = estimatedTimeForJobCompletion;
						ticket.EstimatedCostForJobComplition = estimatedCostForJobCompletion;
						ticket.AcheviedEstimatedTimeForJobComplition = acheviedEstimationTimeForJobComplition;
						ticket.SuggestionComment = suggestion_comment;
						ticket.CustomeContact_no = CustomeContact_no;
						ticket.EstimatedDistance = estimated_distance;
						ticket.OwnerContact_no = owner_contact_number;
						ticket.Priority =priority;
						ticket.IsTripEnd = is_trip_end;
						/*
						 * if (estimated_cost_update_counter != null &&
						 * !estimated_cost_update_counter
						 * .equalsIgnoreCase("null"))
						 * ticket.counter_estiamted_cost_update = Integer
						 * .parseInt(estimated_cost_update_counter);
						 * 
						 * if (estimated_time_update_counter != null &&
						 * !estimated_time_update_counter
						 * .equalsIgnoreCase("null"))
						 * ticket.counter_estiamted_time_update = Integer
						 * .parseInt(estimated_time_update_counter);
						 */
						// ticket.counter_estiamted_cost_update =
						// estimated_cost_update_counter+"";

						allOpenticketsList.add(ticket);
						cursor.moveToNext();
					}
					cursor.close();
					return allOpenticketsList;
				}
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			UtilityFunction.saveErrorLog(context, e);
		}
		return null;
	}

	/* Get OpenTicket of specific TicketId */
	public Ticket getOpenTickets(String ticketId) {
		Ticket ticket = null;
		try {
			if (sqliteDb != null) {
				// Runs the provided SQL and returns a Cursor over the result //
				// set.
				Cursor cursor = sqliteDb.rawQuery("select * from "
						+ TABLE_OPEN_TICKET + " WHERE " + FIELD_TICKET_ID
						+ " = '" + ticketId + "';", null);
				if (cursor.moveToFirst()) {
					String ticket_id = cursor.getString(cursor
							.getColumnIndex(FIELD_TICKET_ID));
					String ticket_status = cursor.getString(cursor
							.getColumnIndex(FIELD_TICKET_STATUS));
					String ticket_last_modified_time = cursor.getString(cursor
							.getColumnIndex(FIELD_TIME_LAST_MODIFIED));
					String ticket_breakdown_location = cursor.getString(cursor
							.getColumnIndex(FIELD_BREAKDOWN_LOCATION));
					String ticket_breakdown_latitude = cursor.getString(cursor
							.getColumnIndex(FIELD_BREAKDOWN_LATITUDE));
					String ticket_breakdown_longitude = cursor.getString(cursor
							.getColumnIndex(FIELD_BREAKDOWN_LONGITUDE));
					String ticket_problem_description = cursor
							.getString(cursor
									.getColumnIndex(FIELD_BREAKDOWN_PROBLEM_DESCRIPTION));
					String ticket_sla_time = cursor.getString(cursor
							.getColumnIndex(FIELD_TIME_SLA));
					String slaTimeAchevied = cursor.getString(cursor
							.getColumnIndex(FIELD_TIME_SLA_ACHEVIED));
					String ticket_creation_time = cursor.getString(cursor
							.getColumnIndex(FIELD_TIME_TICKET_CREATION_TIME));

					String lastmodified_time_in_millisec = cursor
							.getString(cursor
									.getColumnIndex(FIELD_TIME_LAST_MODIFIED_IN_MILLI));
					String estimatedTimeForJobCompletion = cursor
							.getString(cursor
									.getColumnIndex(FIELD_TIME_ESTIMATED_TIME_TO_JOB_COMPLITION));
					String estimatedCostForJobCompletion = cursor
							.getString(cursor
									.getColumnIndex(FIELD_ESTIMATED_COST_TO_JOB_COMPLITION));

					String time_sla_acheived = cursor.getString(cursor
							.getColumnIndex(FIELD_TIME_SLA_ACHEVIED));

					String AcheviedEstimatedTimeForJobComplition = cursor
							.getString(cursor
									.getColumnIndex(FIELD_TIME_ACHEVIED_ESTIMATION_TIME));
					String suggestion_comment = cursor.getString(cursor
							.getColumnIndex(FIELD_SUGGESTION_COMMENT));

					String CustomeContact_no = cursor.getString(cursor
							.getColumnIndex(FIELD_CUSTOMER_CONTACT_NUMBER));
					String ownerContact_no = cursor
							.getString(cursor
									.getColumnIndex(FIELD_VEHICAL_OWNER_CONTACT_NUMBER));

					String estimatedChargableDistance = cursor.getString(cursor
							.getColumnIndex(FIELD_CHARGABLE_DISTANCE));
					
					String priority = cursor.getString(cursor
							.getColumnIndex(FIELD_PRIORITY));
					/*
					 * String estimated_cost_update_counter = cursor
					 * .getString(cursor
					 * .getColumnIndex(FIELD_ESTIMATED_COST_UPDATE_COUNTER));
					 * String estimated_time_update_counter = cursor
					 * .getString(cursor
					 * .getColumnIndex(FIELD_ESTIMATED_TIME_UPDATE_COUNTER));
					 */

					ticket = new Ticket();
					ticket.Id = ticket_id;
					ticket.TicketStatus = ticket_status;
					ticket.LastModifiedTime = ticket_last_modified_time;
					ticket.LastModifiedTimeInMilliSec = lastmodified_time_in_millisec;
					ticket.BreakDownLocation = ticket_breakdown_location;
					ticket.BreackDownLatitude = ticket_breakdown_latitude;
					ticket.BreackDownLongitude = ticket_breakdown_longitude;
					ticket.Description = ticket_problem_description;
					ticket.TotalTicketLifeCycleTimeSlab = ticket_sla_time;
					ticket.SlaTimeAchevied = slaTimeAchevied;
					ticket.CreationTime = ticket_creation_time;
					ticket.EstimatedTimeForJobComplition = estimatedTimeForJobCompletion;
					ticket.EstimatedCostForJobComplition = estimatedCostForJobCompletion;
					ticket.SlaTimeAchevied = time_sla_acheived;
					ticket.AcheviedEstimatedTimeForJobComplition = AcheviedEstimatedTimeForJobComplition;
					ticket.SuggestionComment = suggestion_comment;
					ticket.CustomeContact_no = CustomeContact_no;
					ticket.OwnerContact_no = ownerContact_no;
					ticket.EstimatedDistance = estimatedChargableDistance;
					ticket.Priority =priority;
					
					/*
					 * if (estimated_cost_update_counter != null &&
					 * !estimated_cost_update_counter .equalsIgnoreCase("null"))
					 * ticket.counter_estiamted_cost_update = Integer
					 * .parseInt(estimated_cost_update_counter);
					 * 
					 * if (estimated_time_update_counter != null &&
					 * !estimated_time_update_counter .equalsIgnoreCase("null"))
					 * ticket.counter_estiamted_time_update = Integer
					 * .parseInt(estimated_time_update_counter);
					 */
					cursor.close();
					return ticket;
				}
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			UtilityFunction.saveErrorLog(context, e);
		}
		return null;
	}

	/* Get All ClosedTickets */
	public ArrayList<Ticket> getAllClosedTickets() {
		ArrayList<Ticket> allOpenticketsList = new ArrayList<Ticket>();
		try {
			if (sqliteDb != null) {
				// Runs the provided SQL and returns a Cursor over the result //
				// set.
				Cursor cursor = sqliteDb.rawQuery("select * from "
						+ TABLE_CLOSED_TICKET + " ORDER BY "
						+ FIELD_TIME_LAST_MODIFIED_IN_MILLI + " DESC" + ";",
						null);
				if (cursor != null & cursor.getColumnCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {

						String ticket_id = cursor.getString(cursor
								.getColumnIndex(FIELD_TICKET_ID));
						String ticket_status = cursor.getString(cursor
								.getColumnIndex(FIELD_TICKET_STATUS));
						String ticket_last_modified_time = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_LAST_MODIFIED));
						String ticket_breakdown_location = cursor
								.getString(cursor
										.getColumnIndex(FIELD_BREAKDOWN_LOCATION));
						String ticket_breakdown_latitude = cursor
								.getString(cursor
										.getColumnIndex(FIELD_BREAKDOWN_LATITUDE));
						String ticket_breakdown_longitude = cursor
								.getString(cursor
										.getColumnIndex(FIELD_BREAKDOWN_LONGITUDE));
						String ticket_problem_description = cursor
								.getString(cursor
										.getColumnIndex(FIELD_BREAKDOWN_PROBLEM_DESCRIPTION));
						String ticket_sla_time = cursor.getString(cursor
								.getColumnIndex(FIELD_TIME_SLA));
						String slaTimeAchevied = cursor.getString(cursor
								.getColumnIndex(FIELD_TIME_SLA_ACHEVIED));
						String ticket_creation_time = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_TICKET_CREATION_TIME));
						String estimatedTimeForJobCompletion = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_ESTIMATED_TIME_TO_JOB_COMPLITION));
						String lastmodified_time_in_millisec = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_LAST_MODIFIED_IN_MILLI));
						String estimatedCostForJobCompletion = cursor
								.getString(cursor
										.getColumnIndex(FIELD_ESTIMATED_COST_TO_JOB_COMPLITION));

						String acheviedEstimationTimeForJobComplition = cursor
								.getString(cursor
										.getColumnIndex(FIELD_TIME_ACHEVIED_ESTIMATION_TIME));

						String suggestion_comment = cursor.getString(cursor
								.getColumnIndex(FIELD_SUGGESTION_COMMENT));

						String customer_contact_no = cursor.getString(cursor
								.getColumnIndex(FIELD_CUSTOMER_CONTACT_NUMBER));

						String estimated_distance = cursor.getString(cursor
								.getColumnIndex(FIELD_CHARGABLE_DISTANCE));
						String owner_contact_number = cursor
								.getString(cursor
										.getColumnIndex(FIELD_VEHICAL_OWNER_CONTACT_NUMBER));

						String priority = cursor.getString(cursor
								.getColumnIndex(FIELD_PRIORITY));
						
						String is_trip_end = cursor.getString(cursor
								.getColumnIndex(FIELD_IS_TRIP_END));
						
						
						Ticket ticket = new Ticket();
						ticket.Id = ticket_id;
						ticket.TicketStatus = ticket_status;
						ticket.TicketStatusText = "Closed";
						ticket.LastModifiedTime = ticket_last_modified_time;
						ticket.BreakDownLocation = ticket_breakdown_location;
						ticket.BreackDownLatitude = ticket_breakdown_latitude;
						ticket.BreackDownLongitude = ticket_breakdown_longitude;
						ticket.Description = ticket_problem_description;
						ticket.TotalTicketLifeCycleTimeSlab = ticket_sla_time;
						ticket.SlaTimeAchevied = slaTimeAchevied;
						ticket.LastModifiedTimeInMilliSec = lastmodified_time_in_millisec;
						ticket.CreationTime = ticket_creation_time;
						ticket.EstimatedTimeForJobComplition = estimatedTimeForJobCompletion;
						ticket.EstimatedCostForJobComplition = estimatedCostForJobCompletion;
						ticket.AcheviedEstimatedTimeForJobComplition = acheviedEstimationTimeForJobComplition;
						ticket.SuggestionComment = suggestion_comment;
						ticket.CustomeContact_no = customer_contact_no;
						ticket.EstimatedDistance = estimated_distance;
						ticket.OwnerContact_no = owner_contact_number;
						ticket.Priority =priority;
						ticket.IsTripEnd = is_trip_end;

						allOpenticketsList.add(ticket);
						cursor.moveToNext();
					}
					cursor.close();
					return allOpenticketsList;
				}
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			UtilityFunction.saveErrorLog(context, e);
		}
		return null;
	}

	/* Delete OpenTicket Records */
	public void deleteMyTicketData() {
		if (sqliteDb != null) {
			try {
				int i = sqliteDb.delete(TABLE_OPEN_TICKET, null, null);
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
	}

	/*
	 * Delete OpenTicket form OpenTicket table When ticket is closed from server
	 * push recevied
	 */
	public void deleteClosedTicketFromOpenTicketsTable(String ticket_id) {
		if (sqliteDb != null) {
			try {
				int i = sqliteDb.delete(TABLE_OPEN_TICKET, FIELD_TICKET_ID
						+ "= ?", new String[] { ticket_id });
				System.out
						.println("DBInteraction.deleteClosedTicketFromOpenTicketsTable() status:"
								+ i);
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
	}

	/* Get Saved new Ticket to show Pop-up of NewTicket */
	public Ticket getSavedNewTicket() {

		Ticket ticket = null;
		try {
			if (sqliteDb != null) {
				// Runs the provided SQL and returns a Cursor over the result //
				// set.
				Cursor cursor = sqliteDb.rawQuery("select * from "
						+ TABLE_NEW_TICKET + ";", null);
				if (cursor != null & cursor.getColumnCount() > 0) {
					cursor.moveToFirst();

					String ticket_id = cursor.getString(cursor
							.getColumnIndex(FIELD_TICKET_ID));
					String ticket_status = cursor.getString(cursor
							.getColumnIndex(FIELD_TICKET_STATUS));
					String ticket_last_modified_time = cursor.getString(cursor
							.getColumnIndex(FIELD_TIME_LAST_MODIFIED));
					String ticket_breakdown_location = cursor.getString(cursor
							.getColumnIndex(FIELD_BREAKDOWN_LOCATION));
					String ticket_breakdown_latitude = cursor.getString(cursor
							.getColumnIndex(FIELD_BREAKDOWN_LATITUDE));
					String ticket_breakdown_longitude = cursor.getString(cursor
							.getColumnIndex(FIELD_BREAKDOWN_LONGITUDE));
					String ticket_problem_description = cursor
							.getString(cursor
									.getColumnIndex(FIELD_BREAKDOWN_PROBLEM_DESCRIPTION));
					String ticket_sla_time = cursor.getString(cursor
							.getColumnIndex(FIELD_TIME_SLA));
					String ticket_creation_time = cursor.getString(cursor
							.getColumnIndex(FIELD_TIME_TICKET_CREATION_TIME));

					String vehical_type = cursor.getString(cursor
							.getColumnIndex(FIELD_VEHICAL_TYPE));
					String last_modified_time_in_milli = cursor
							.getString(cursor
									.getColumnIndex(FIELD_TIME_LAST_MODIFIED_IN_MILLI));

					String customer_contact_number = cursor.getString(cursor
							.getColumnIndex(FIELD_CUSTOMER_CONTACT_NUMBER));
					String owner_contact = cursor
							.getString(cursor
									.getColumnIndex(FIELD_VEHICAL_OWNER_CONTACT_NUMBER));
					String priority = cursor
							.getString(cursor
									.getColumnIndex(FIELD_PRIORITY));
					
					
					ticket = new Ticket();
					ticket.Id = ticket_id;
					ticket.TicketStatus = ticket_status;
					ticket.LastModifiedTime = ticket_last_modified_time;
					ticket.BreakDownLocation = ticket_breakdown_location;
					ticket.BreackDownLatitude = ticket_breakdown_latitude;
					ticket.BreackDownLongitude = ticket_breakdown_longitude;
					ticket.Description = ticket_problem_description;
					ticket.TotalTicketLifeCycleTimeSlab = ticket_sla_time;
					ticket.CreationTime = ticket_creation_time;
					ticket.vehicleType = vehical_type;
					ticket.LastModifiedTimeInMilliSec = last_modified_time_in_milli;
					ticket.CustomeContact_no = customer_contact_number;
					ticket.OwnerContact_no = owner_contact;
					ticket.Priority = priority;
					cursor.close();
					return ticket;
				}
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			UtilityFunction.saveErrorLog(context, e);
		}

		return new Ticket();

	}

	/*
	 * // used for storing login pin public long addLoginPin(String login_pin) {
	 * if (sqliteDb != null) { try { return
	 * sqliteDb.insert(TABLE_AUTHENTICATION, null,
	 * addLoginPinFields(login_pin)); } catch (Exception e) {
	 * 
	 * UtilityFunction.saveErrorLog(context, e); } } return 0; }
	 */
	/*
	 * private ContentValues addLoginPinFields(String login_pin) { ContentValues
	 * contentValues = new ContentValues();
	 * contentValues.put(FIELD_TABLE_AUTHENTICATION_LOGIN_PIN, login_pin);
	 * return contentValues; }
	 */

	// Status table
	public long fillStatusTable(String sequence_order, String status_name,
			String alias) {
		if (sqliteDb != null) {
			try {

				long i = sqliteDb.insert(TABLE_TICKET_STATUS, null,
						addStatusFields(sequence_order, status_name, alias));
				return i;
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		return 0;
	}

	/* Column to Value mapping */
	private ContentValues addStatusFields(String sequence_order,
			String status_name, String alias_name) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(FIELD_STATUS_SEQUENCE_ORDER, sequence_order);
		contentValues.put(FIELD_STATUS_NAME, status_name);
		contentValues.put(FIELD_STATUS_ALIAS, alias_name);

		return contentValues;
	}

	/* get Ticket status alias name from its order */
	public String getStatusName(String sequence_order) {
		try {
			if (sqliteDb != null) {
				// Runs the provided SQL and returns a Cursor over the result //
				// set.
				Cursor cursor = sqliteDb.rawQuery("select * from "
						+ TABLE_TICKET_STATUS + " WHERE "
						+ FIELD_STATUS_SEQUENCE_ORDER + " = '" + sequence_order
						+ "';", null);
				if (cursor != null & cursor.getColumnCount() > 0) {
					cursor.moveToFirst();
					String status_name = cursor.getString(cursor
							.getColumnIndex(FIELD_STATUS_ALIAS));
					cursor.close();
					return status_name;
				}
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			UtilityFunction.saveErrorLog(context, e);
		}
		return null;
	}

	// Ticket Action Reasons
	// Status table
	public long fillTicketActionReasonsTable(String reason_id,
			String reason_type, String reason_name) {
		if (sqliteDb != null) {
			try {

				long i = sqliteDb.insert(
						TABLE_TICKET_REASON,
						null,
						addTicketActionReasonFields(reason_id, reason_type,
								reason_name));
				System.out
						.println("DBInteraction.fillStatusTable() ticket reason:"
								+ reason_id + " reason name:" + reason_name);
				return i;
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		return 0;
	}

	private ContentValues addTicketActionReasonFields(String reason_id,
			String reason_type, String reason_text) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(FIELD_REASON_ID, reason_id);
		contentValues.put(FIELD_REASON_TYPE, reason_type);
		contentValues.put(FIELD_REASON_TEXT, reason_text);
		return contentValues;
	}

	/* Get Reasons List from reason type */
	public ArrayList<DeclineReasonModel> getReasonList(String reason_type) {
		ArrayList<DeclineReasonModel> reasonsList = new ArrayList<DeclineReasonModel>();
		try {
			if (sqliteDb != null) {
				// Runs the provided SQL and returns a Cursor over the result //
				// set.
				Cursor cursor = sqliteDb.rawQuery("select * from "
						+ TABLE_TICKET_REASON + " WHERE " + FIELD_REASON_TYPE
						+ "=" + reason_type + ";", null);
				if (cursor != null & cursor.getColumnCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {

						String reason_id = cursor.getString(cursor
								.getColumnIndex(FIELD_REASON_ID));
						String reason_typ = cursor.getString(cursor
								.getColumnIndex(FIELD_REASON_TYPE));
						String reason_name = cursor.getString(cursor
								.getColumnIndex(FIELD_REASON_TEXT));

						DeclineReasonModel reason = new DeclineReasonModel();
						reason.id = reason_id;
						reason.reason_name = reason_name;
						reason.reason_type = reason_typ;

						reasonsList.add(reason);
						cursor.moveToNext();
					}
					cursor.close();
					return reasonsList;
				}
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			UtilityFunction.saveErrorLog(context, e);
		}
		return null;
	}

	// estimation cost table
	public long fillEstimationCostTable(String cost_id, String costRange) {
		if (sqliteDb != null) {
			try {

				long i = sqliteDb.insert(TABLE_ESTIAMTION_COST, null,
						addEstimataionCostField(cost_id, costRange));
				System.out
						.println("DBInteraction.fillStatusTable() estiamtion cost :"
								+ cost_id + " cost range:" + costRange);
				return i;
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		return 0;
	}

	private ContentValues addEstimataionCostField(String cost_id,
			String cost_range) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(FIELD_COST_ID, cost_id);
		contentValues.put(FIELD_COST_VALUE, cost_range);
		return contentValues;
	}

	/* Get Estimation Cost List */
	public ArrayList<EstimationCostModel> getEstiamtionCostList() {
		ArrayList<EstimationCostModel> estimationCostModelsList = new ArrayList<EstimationCostModel>();
		try {
			if (sqliteDb != null) {
				// Runs the provided SQL and returns a Cursor over the result //
				// set.
				Cursor cursor = sqliteDb.rawQuery("select * from "
						+ TABLE_ESTIAMTION_COST + ";", null);

				System.out.println("DBInteraction.getEstiamtionCostList()");
				if (cursor != null & cursor.getColumnCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {

						String cost_id = cursor.getString(cursor
								.getColumnIndex(FIELD_COST_ID));
						String cost_value = cursor.getString(cursor
								.getColumnIndex(FIELD_COST_VALUE));

						EstimationCostModel estimationCostModel = new EstimationCostModel();
						estimationCostModel.id = cost_id;
						estimationCostModel.cost_range = cost_value;
						estimationCostModelsList.add(estimationCostModel);
						cursor.moveToNext();
					}
					cursor.close();
					return estimationCostModelsList;
				}
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			UtilityFunction.saveErrorLog(context, e);
		}
		return null;
	}

	// Timestamp Table updating
	// param - isFirstValue - flag to indicate is this fist time when value is
	// inserting .
	public long updateTimeStamp(boolean isFirstValue, String timestamp) {
		if (sqliteDb != null) {
			try {

				ContentValues values = new ContentValues();
				values.put(FIELD_TIMESTAMP, timestamp);
				/*
				 * if isFirstValue is true , do insert new value into table ,
				 * else update previous value
				 */
				if (isFirstValue) {
					long i = sqliteDb.insert(TABLE_TIMESTAMP, null, values);
					return i;
				} else {
					//
					long i = sqliteDb.update(TABLE_TIMESTAMP, values,
							FIELD_TIMESTAMP_ID + " = ?", new String[] { "1" });
					return i;
				}

			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		return 0;
	}

	/* get timestamp value */
	public String getLastTimestap() {
		try {
			if (sqliteDb != null) {
				// Runs the provided SQL and returns a Cursor over the result //
				// set.
				Cursor cursor = sqliteDb.rawQuery("select * from "
						+ TABLE_TIMESTAMP + ";", null);
				if (cursor != null & cursor.getColumnCount() > 0) {
					cursor.moveToFirst();
					String timestamp = cursor.getString(cursor
							.getColumnIndex(FIELD_TIMESTAMP));

					cursor.close();
					return timestamp;
				}
			}
		} catch (Exception e) {

			UtilityFunction.saveErrorLog(context, e);
		}
		return null;
	}

	// save offline tickets data local db into OpenTicketActivity table
	public long saveTicketOffline(Ticket ticket) {
		if (sqliteDb != null) {
			try {

				return sqliteDb.insert(
						TABLE_OPEN_TICKET_ACTIVITY,
						null,
						addOpenTicketActivityFields(ticket.Id,
								ticket.TicketStatus,
								ticket.TotalTicketLifeCycleTimeSlab,
								ticket.BreakDownLocation,
								ticket.BreackDownLatitude,
								ticket.BreackDownLongitude,
								ticket.SlaTimeAchevied, ticket.Description,
								ticket.CreationTime, ticket.LastModifiedTime,
								ticket.LastModifiedTimeInMilliSec,
								ticket.EstimatedTimeForJobComplition,
								ticket.EstimatedCostForJobComplition,
								ticket.AcheviedEstimatedTimeForJobComplition,
								ticket.SuggestionComment,
								ticket.Priority,ticket.IsTripEnd
								));
			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);

			}
		}
		return 0;

	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int old_version, int new_version) {
		// TODO Auto-generated method stub

		// System.out.println("DBInteraction.onUpgrade() "+"old_version:"+old_version+" new_version:"+new_version);

		// Toast.makeText(ApplicationConstant.currentActivityContext,"old_version:"+old_version+" new_version:"+new_version,Toast.LENGTH_LONG).show();

		if (ApplicationConstant.currentActivityContext != null)
			ApplicationConstant.currentActivityContext.deleteDatabase(DB_NAME);

		// context.deleteDatabase(DB_NAME);
	}

	public long updateCounter_EstimatedCostCounter(Ticket ticket) {
		if (sqliteDb != null) {
			try {

				boolean ifAlreadyinDb = CheckIsDataAlreadyInDBorNot(
						TABLE_UPDATE_COUNTER, FIELD_TICKET_ID, ticket.Id);

				if (!ifAlreadyinDb) {
					return sqliteDb.insert(
							TABLE_UPDATE_COUNTER,
							null,
							addUpdateCounterForEstiamtedCostEdit(ticket.Id,
									ticket.counter_estiamted_cost_update + ""));

				} else {
					return sqliteDb.update(
							TABLE_UPDATE_COUNTER,
							addUpdateCounterForEstiamtedCostEdit(ticket.Id,
									ticket.counter_estiamted_cost_update + ""),
							FIELD_TICKET_ID + " = ?",
							new String[] { ticket.Id });
				}

			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		return -1;
	}

	public long updateCounter_EstimatedTimeCounter(Ticket ticket) {
		if (sqliteDb != null) {
			try {

				boolean ifAlreadyinDb = CheckIsDataAlreadyInDBorNot(
						TABLE_UPDATE_COUNTER, FIELD_TICKET_ID, ticket.Id);
				/* If ticket already exists, update that */
				if (!ifAlreadyinDb) {
					return sqliteDb.insert(
							TABLE_UPDATE_COUNTER,
							null,
							addUpdateCounterForEstiamtedTimeEdit(ticket.Id,
									ticket.counter_estiamted_time_update + ""));

				} else {
					return sqliteDb.update(
							TABLE_UPDATE_COUNTER,
							addUpdateCounterForEstiamtedTimeEdit(ticket.Id,
									ticket.counter_estiamted_time_update + ""),
							FIELD_TICKET_ID + " = ?",
							new String[] { ticket.Id });
				}

			} catch (Exception e) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(e);
				UtilityFunction.saveErrorLog(context, e);
			}
		}
		return -1;
	}

	public UpdateCounter getEstiamteTimeUpgradeCount(String ticket_id) {
		try {
			if (sqliteDb != null) {
				// Runs the provided SQL and returns a Cursor over the result //
				// set.
				Cursor cursor = sqliteDb.rawQuery("select * from "
						+ TABLE_UPDATE_COUNTER + " WHERE " + FIELD_TICKET_ID
						+ "='" + ticket_id + "' ;", null);
				if (cursor != null & cursor.getColumnCount() > 0) {
					cursor.moveToFirst();
					String time_update_counter = cursor
							.getString(cursor
									.getColumnIndex(FIELD_ESTIMATED_TIME_UPDATE_COUNTER));
					String cost_update_counter = cursor
							.getString(cursor
									.getColumnIndex(FIELD_ESTIMATED_COST_UPDATE_COUNTER));

					UpdateCounter counter = new UpdateCounter();
					counter.estiamtedTime_Counter = time_update_counter;
					counter.estiamtedCost_Counter = cost_update_counter;
					cursor.close();
					return counter;
				}
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			UtilityFunction.saveErrorLog(context, e);
		}
		return null;
	}

	private ContentValues addUpdateCounterForEstiamtedCostEdit(
			String ticket_id, String counter) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(FIELD_TICKET_ID, ticket_id);
		contentValues.put(FIELD_ESTIMATED_COST_UPDATE_COUNTER, counter);

		return contentValues;
	}

	private ContentValues addUpdateCounterForEstiamtedTimeEdit(
			String ticket_id, String counter) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(FIELD_TICKET_ID, ticket_id);
		contentValues.put(FIELD_ESTIMATED_TIME_UPDATE_COUNTER, counter);

		return contentValues;
	}
}