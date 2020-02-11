package com.teramatrix.vos.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 
 * @author gaurav.mangal
 * 
 *         VECVPreferences Class is used to store data locally instead of local
 *         database it has set and get method those will be using while storing
 *         and getting data.
 */
public class VECVPreferences {

	private static final String VECV_PREFS = "VECV_PREFS";
	private SharedPreferences appSharedPrefs;
	private SharedPreferences.Editor prefsEditor;

	private String securityToken = "Security_Token";
	private String checkconfigure = "Check_Configure";
	private String checkLogin = "Check_Login";
	private String licenseKey = "LicenseKey";
	private String imeiNumber = "ImeiNumber";
	private String deviceAlias = "Device_Alias";
	private String pinPassword = "Pin_Password";
	private String userType = "UserType";
	private String SiteId = "SiteId";



	private String KEY_NEW_TICKET_LISTENER_STATUS = "is_new_ticket_listner_on";
	private String KEY_IS_NEW_TICKET_ASSIGNED = "is_new_ticket_assigned";
	private String KEY_IS_NEW_TICKET_SAVED_IN_DB = "is_new_ticket_saved_in_db";
	private String KEY_PENDING_TICKETS = "pending_tickets";
	private String KEY_IS_TICKET_STATUS_FILL = "is_ticket_status_fill";
	private String KEY_NOTIFICATION_COUNT = "notification_count";
	private String KEY_NEW_ASSIGNED_TICKET_ID = "new_assigned_ticket_id";
	private String KEY_SHOULD_INSTALL_NEW_DB = "should_install_new_db";
	private String KEY_GCM_ID = "gcm_id";
	private String IS_GOOGLE_ANALYTIC_SETUP = "google_analytic_setup";
	
	private String KEY_API_END_POINT_EOS = "api_end_poiny_eos";
	private String KEY_API_END_POINT_TELIMATIC = "api_end_poiny_telimatic";
	
	
	public VECVPreferences(Context context) {
		if (context!=null){
			this.appSharedPrefs = context.getSharedPreferences(VECV_PREFS,
					Activity.MODE_PRIVATE);
			this.prefsEditor = appSharedPrefs.edit();
		}


	}

	public void clearSharedPreference() {
		prefsEditor.clear().commit();
	}

	public void setShouldInstallNewDB(boolean flag) {
		prefsEditor.putBoolean(KEY_SHOULD_INSTALL_NEW_DB, flag).commit();
	}

	public boolean shouldInstallNewDB() {
		return appSharedPrefs.getBoolean(KEY_SHOULD_INSTALL_NEW_DB, true);
	}

	// Setter getter for Security Token
	public void setSecurityToken(String security_token) {
		prefsEditor.putString(securityToken, security_token).commit();
	}

	public String getSecurityToken() {
		return appSharedPrefs.getString(securityToken, "");
	}

	public String getGcmID() {
		return appSharedPrefs.getString(KEY_GCM_ID, "");
	}

	public String getUserType() {
		return appSharedPrefs.getString(userType, "");
	}

	public void setGcmID(String gcm_id) {
		prefsEditor.putString(KEY_GCM_ID, gcm_id).commit();
	}

	public void setUserType(String type) {
		prefsEditor.putString(userType, type).commit();
	}

	public String getDevice_alias() {
		return appSharedPrefs.getString(deviceAlias, "");
	}

	public void setDevice_alias(String device_alias) {
		prefsEditor.putString(deviceAlias, device_alias).commit();
	}

	// Setter getter for CheckLogin
	public void setCheckLogin(boolean check_login) {
		prefsEditor.putBoolean(checkLogin, check_login).commit();
	}

	public boolean getCheckLogin() {
		return appSharedPrefs.getBoolean(checkLogin, false);
	}

	// Setter getter for LicenseKey
	public void setLicenseKey(String license) {
		prefsEditor.putString(licenseKey, license).commit();
	}

	public String getLicenseKey() {
		return appSharedPrefs.getString(licenseKey, "");
	}

	public String getImeiNumber() {
		return appSharedPrefs.getString(imeiNumber, "");
	}

	// Setter getter for imei number
	public void setImeiNumber(String imei) {
		prefsEditor.putString(imeiNumber, imei).commit();
	}

	// Setter getter for pin password
	public String getPinPassword() {
		return appSharedPrefs.getString(pinPassword, "");
	}

	public void setPinPassword(String pin_password) {
		prefsEditor.putString(pinPassword, pin_password).commit();
	}

	// Setter getter for check configure
	public boolean getCheckconfigure() {
		return appSharedPrefs.getBoolean(checkconfigure, false);
	}

	public void setCheckconfigure(boolean check_configure) {
		prefsEditor.putBoolean(checkconfigure, check_configure).commit();
	}

	// Setter getter for New ticket license status
	public void setNewTicketListnerStatus(boolean isOn) {
		prefsEditor.putBoolean(KEY_NEW_TICKET_LISTENER_STATUS, isOn).commit();
	}

	public boolean getNewTicketListnerStatus() {
		return appSharedPrefs.getBoolean(KEY_NEW_TICKET_LISTENER_STATUS, false);
	}

	// Setter getter for new ticket assigned
	public void setNewTicketAssigned(boolean flag) {
		prefsEditor.putBoolean(KEY_IS_NEW_TICKET_ASSIGNED, flag).commit();
	}

	public boolean isNewTicketAssigned() {
		return appSharedPrefs.getBoolean(KEY_IS_NEW_TICKET_ASSIGNED, false);
	}

	// Setter getter for new ticket on local db
	public void setNewTicketSavedInDB(boolean flag) {
		prefsEditor.putBoolean(KEY_IS_NEW_TICKET_SAVED_IN_DB, flag).commit();
	}

	public boolean isNewTicketExistsInDB() {
		return appSharedPrefs.getBoolean(KEY_IS_NEW_TICKET_SAVED_IN_DB, false);
	}

	public void addPendingTicketsCount(int count) {
		int i = getPendingTicketCount();
		prefsEditor.putInt(KEY_PENDING_TICKETS, count).commit();
	}

	// get add pengin tickets count
	public void addPendingTicketsCount() {
		int i = getPendingTicketCount();
		prefsEditor.putInt(KEY_PENDING_TICKETS, i + 1).commit();
	}

	// get decrease pending ticket count
	public void decresePendingTicketsCount() {
		int i = getPendingTicketCount();
		prefsEditor.putInt(KEY_PENDING_TICKETS, i - 1).commit();
	}

	// get pending ticket count
	public int getPendingTicketCount() {
		return appSharedPrefs.getInt(KEY_PENDING_TICKETS, 0);
	}

	// set status table data value fill or not
	public void setTicketStatusFill(boolean flag) {
		prefsEditor.putBoolean(KEY_IS_TICKET_STATUS_FILL, flag).commit();
	}

	// check status table exists in local db
	public boolean isTicketStatusTableExistsInDB() {
		return appSharedPrefs.getBoolean(KEY_IS_TICKET_STATUS_FILL, false);
	}

	// adding notification count
	public int addNotificationCount() {
		int currentCount = getNoticifationCount();
		prefsEditor.putInt(KEY_NOTIFICATION_COUNT, currentCount + 1).commit();
		return getNoticifationCount();
	}

	public int getNoticifationCount() {
		return appSharedPrefs.getInt(KEY_NOTIFICATION_COUNT, 0);
	}

	public String getNewAssignedTicketId() {
		return appSharedPrefs.getString(KEY_NEW_ASSIGNED_TICKET_ID, "");
	}

	// Setter getter for new assign ticket id
	public void setNewAssignedTicketId(String ticketId) {
		prefsEditor.putString(KEY_NEW_ASSIGNED_TICKET_ID, ticketId).commit();
	}

	public boolean getSlaStatusForTicketId(String ticket_id) {
		return appSharedPrefs.getBoolean(ticket_id, false);
	}
	
	//Google analytic flag
	public void setGoogleAnalyticSetupStatus(boolean is_setup) {
		prefsEditor.putBoolean(IS_GOOGLE_ANALYTIC_SETUP, is_setup).commit();
	}

	public boolean getGoogleAnalyticSetupStatus() {
		return appSharedPrefs.getBoolean(IS_GOOGLE_ANALYTIC_SETUP, false);
	}
	

	// Setter getter for new assign ticket id
	public void setSlaStatusForTicketId(String ticketId, boolean isSlaMissed) {
		prefsEditor.putBoolean(ticketId, isSlaMissed).commit();
	}

	
	public void setAPIEndPoint_EOS(String api_end_point_eos) {
		prefsEditor.putString(KEY_API_END_POINT_EOS, api_end_point_eos).commit();
	}
	public void setAPIEndPoint_Telimatic(String api_end_point_telimatic) {
		prefsEditor.putString(KEY_API_END_POINT_TELIMATIC, api_end_point_telimatic).commit();
	}
	public String getAPIEndPoint_EOS() {
		return appSharedPrefs.getString(KEY_API_END_POINT_EOS, "");
	}
	public String getAPIEndPoint_Telimatic() {
		return appSharedPrefs.getString(KEY_API_END_POINT_TELIMATIC, "");
	}

	public String getSiteId() {
		return appSharedPrefs.getString(SiteId, "");
	}

	public void setSiteId(String siteId) {
		prefsEditor.putString(SiteId, siteId).commit();
	}
}