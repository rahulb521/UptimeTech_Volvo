package com.teramatrix.vos;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import com.handmark.pulltorefresh.library.internal.Utils;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.asynctasks.GetTicketStatusAndReasonsList;
import com.teramatrix.vos.checkinternet.CheckInternetConnection;
import com.teramatrix.vos.database.DBInteraction;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.GPSManager;
import com.teramatrix.vos.utils.UtilityFunction;
import com.teramatrix.vos.volvouptime.UpTimeVehicleListActivity;

/**
 * SplashActivity class is responsible for Launcher/Splash screen
 * 
 * @author Gaurav Mangal
 */
public class SplashActivity extends Activity {

	// define boolean variable
	private Boolean checkConfigure, checkLogin;
	// define instance of the class VECVPreferences
	private VECVPreferences vecvPreferences;
	private int AppVersionOnParse;
	private AlertDialog power_saving_alert = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Request window to hide title bar full screen for this activity
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// set layout for this activity
		setContentView(R.layout.screen_splash);

		ApplicationConstant.currentActivityContext = SplashActivity.this;
		vecvPreferences = new VECVPreferences(SplashActivity.this);


	}

	/*
	 * check if local db already exists or not. If no db is setup , copy sqlite
	 * file(VOC_db.sqlite) from asset folder to device directory
	 * (data/data/databases)
	 * 
	 * Step to see database 1. Connect to "Google Drive Repository". 2. Go to
	 * "GODATA VECV" folder under Design And Definition Architecture. 3. Then,
	 * go to"Android Application" folder and go to the "Database Diagram". 4.
	 * Under "Database Diagram" click go to "Vecv Database". 5. download file
	 * VOC_DB.sqlite in local computer".
	 */
	private void setupLocalDB() {

		// check if IS_LOG_SHOWN or not
		if (ApplicationConstant.IS_LOG_SHOWN)
			System.out.println("11 SplashActivity.setupLocalDB()");

		DBInteraction dbInteraction = null;
		try {

			// create instance of the class DBInteraction
			dbInteraction = DBInteraction.getInstance(this
					.getApplicationContext());
			if (dbInteraction != null) {
				dbInteraction.closeConnection();
				dbInteraction.getWritableDatabase();
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			e.printStackTrace();
			return;
		}

		// Check if db exists or DB exists but forcefully install new DB version
		if (dbInteraction != null && !dbInteraction.isDBExist()) {
			// New Installation
			dbInteraction.copyDbFromAssets(SplashActivity.this);
			if (ApplicationConstant.is_app_update_on_unversioned_db_schema)
				vecvPreferences.setShouldInstallNewDB(false);
			vecvPreferences.setTicketStatusFill(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */

	@Override
	public void onBackPressed() {
		// finish activity
		finish();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

		/**
		 * When app starts it check if device gps is enabled or not. If disabled
		 * a pop-up alert is shown which redirect user to device setting screen
		 * so that user can turn on gps. Without gps enabling user can not enter
		 * into application.
		 */
		setupLocalDB();
		//checkPowerSavingModeSetting();
		checkGPS();
	}

	private void checkPowerSavingModeSetting() {
		try {
			String status = null;
			try {
				// power saving(psm_switch) is only for samsung or HTC devices
				status = Settings.System.getString(getContentResolver(),
						"psm_switch");
			} catch (Exception ex) {
				
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(ex);

			}
			status = "0";
			if (status != null && status.equalsIgnoreCase("1")) {
				// Power saving mode on
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						SplashActivity.this);
				alertDialogBuilder.setCancelable(false);
				alertDialogBuilder
						.setMessage(
								this.getResources().getString(
										R.string.power_saving_check))
						.setCancelable(false)
						.setPositiveButton(
								this.getResources().getString(
										R.string.power_saving_check_forward),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {

										startActivityForResult(
												new Intent(
														android.provider.Settings.ACTION_SETTINGS),
												0);
									}
								});

				// create alert builder dialog
				power_saving_alert = alertDialogBuilder.create();

				// show alert dialog
				power_saving_alert.show();

			} else {
				if (power_saving_alert != null
						&& power_saving_alert.isShowing())
					power_saving_alert.dismiss();

				checkGPS();
			}
		} catch (Exception e) {
			//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
			e.printStackTrace();
		}
	}

	/*
	 * if app is already configured, check if pin is configured , if pin is not
	 * configured , move to Pin configuration screen. if pin is also configured
	 * , check if device IMEI is active on server.
	 */
	public void checkGPS() {

		// check GPS status on/off
		if (!GPSManager.checkGpsStatus(this)) {

			// Show GPS disable alert dialog .
		} else {
			// gps is enabled
			checkConfigure = vecvPreferences.getCheckconfigure();
			checkLogin = vecvPreferences.getCheckLogin();

			// Showing splash screen with a timer for 2 sec.

			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (checkConfigure) {
						if (!checkLogin) {
							Intent mainIntent = new Intent(SplashActivity.this,
									ConfigurationPinActivity.class);
							mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							SplashActivity.this.startActivity(mainIntent);
							// Start UpTimeVehicleListActivity after Splash time out and
							// finish this activity.
							SplashActivity.this.finish();
						} else {

							// get IMEI number from vecvPreferences instance
							String imei_number = vecvPreferences
									.getImeiNumber();
							// get License key
							String licence_key = vecvPreferences
									.getLicenseKey();
							//Get Saved GCM ID
							String gcmId = vecvPreferences.getGcmID();
							// check imei number null or not
							if (imei_number != null && imei_number.length() > 0
									&& licence_key != null
									&& licence_key.length() > 0) {

								// check internet connection
								if (new CheckInternetConnection(
										SplashActivity.this)
										.isConnectedToInternet()) {
									// check if this device imei is still
									// registered
									// on server
									new ChekDeviceReggistrationOnServer(
											SplashActivity.this, licence_key,
											imei_number, gcmId).execute();
								} else {
									String userType = vecvPreferences.getUserType();
									Intent mainIntent = null;
									if(userType.equalsIgnoreCase("0"))
									{
										//For Volvo Service van app
										mainIntent = new Intent(SplashActivity.this,
												MyTicketActivity.class);
									}else if(userType.equalsIgnoreCase("1"))
									{
										//For Volvo UpTime app
										mainIntent = new Intent(SplashActivity.this,
												UpTimeVehicleListActivity.class);
									}
									mainIntent
											.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									SplashActivity.this
											.startActivity(mainIntent);
									// finish splash activity
									SplashActivity.this.finish();
								}
							}
						}
					} else {

						// Configurataion licence is not set, move to
						// Configuration Licence Screen
						Intent mainIntent = new Intent(SplashActivity.this,
								ConfigurationLicenseActivity.class);
						// set flag of the class internet
						// ConfigurationLicenseActivity
						mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						// start intent activity ConfigurationLicenseActivity
						SplashActivity.this.startActivity(mainIntent);
						// finish the splash activity
						SplashActivity.this.finish();
					}
				}
			}, ApplicationConstant.DISPLAY_SPLASH_ACTIVITY);
		}
		// throw new RuntimeException("On-line Exception");
	}

	/**
	 * 
	 * This class ChekDeviceReggistrationOnServer checking device register or
	 * not in server side.
	 * 
	 */
	class ChekDeviceReggistrationOnServer extends AsyncTask<Void, Void, Void> {

		// Defining Context for this class
		private Context mContext;

		// Defining String variables for this class
		private String gcm_registration_id, licenseNumber, securityToken,
				device_alias, imeiNumber, response, status, message;

		// Defining ProgressDialog for this class
		private ProgressDialog mProgressDialog;
		// Defining RestIntraction class
		private RestIntraction restIntraction;
		// Defining JSONObject class
		private JSONObject jsonObject;
		// Defining VECV Preferences class
		private VECVPreferences vecvPreferences;

		/**
		 * 
		 * @param context
		 * @param licenseNum
		 * @param imeiNum
		 * @param gcmDeviceid
		 */
		public ChekDeviceReggistrationOnServer(Context context,
				String licenseNum, String imeiNum, String gcmDeviceid) {
			mContext = context;
			licenseNumber = licenseNum;
			imeiNumber = imeiNum;
			gcm_registration_id = gcmDeviceid;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */

		@Override
		protected Void doInBackground(Void... params) {
			try {
				vecvPreferences = new VECVPreferences(mContext);

				restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS()+""+
						ApiUrls.DEVICE_CONFIGURATION_LICENSE);
				System.out.println("ChekDeviceReggistrationOnServer: RegistrationNo:"+licenseNumber+" Imei:"+imeiNumber+" DeviceGcmId:"+gcm_registration_id);
				// Call Configuration API with these parameters
				restIntraction.AddParam("RegistrationNo", licenseNumber);
				restIntraction.AddParam("Imei", imeiNumber);
				restIntraction.AddParam("DeviceGcmId", gcm_registration_id);
				restIntraction.AddParam("PushNotificationBit","1");
				// call post request
				restIntraction.Execute(1);
			} catch (Exception ex) {
				//Google Analytic -Tracking Exception 
				EosApplication.getInstance().trackException(ex);
				// save error location in file
				UtilityFunction.saveErrorLog(SplashActivity.this, ex);
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// get response from the service
			response = restIntraction.getResponse();

			if (ApplicationConstant.IS_LOG_SHOWN)
				System.out.println("Response is:" + response);
			// check response null or not
			if (response != null) {
				try {
					// get reponse from json opbect
					jsonObject = new JSONObject(response);
					// get value from json string key
					status = jsonObject.getString("Status");
					message = jsonObject.getString("Massage");
					securityToken = jsonObject.getString("Token");
					device_alias = jsonObject.getString("DeviceAlias");
					// status = 1 for Device IMEI already exists on server
					if (status.equals("1")) {
						// Device already configured on server, move to Home
						// Screen of app.
						String userType = vecvPreferences.getUserType();
						//userType -> 0 VAS Service Van User
						//userType -> 1 Uptime User

						if (ApplicationConstant.is_app_update_on_unversioned_db_schema
								&& !vecvPreferences
										.isTicketStatusTableExistsInDB() && !userType.equalsIgnoreCase("1")) {
							new GetTicketStatusAndReasonsList(
									SplashActivity.this, true).execute();
						} else {
							Intent mainIntent = null;
							if(userType.equalsIgnoreCase("0"))
							{
								//For Volvo Service van app
								mainIntent = new Intent(SplashActivity.this,
										MyTicketActivity.class);
							}else if(userType.equalsIgnoreCase("1"))
							{
								//For Volvo UpTime app
								mainIntent = new Intent(SplashActivity.this,
										UpTimeVehicleListActivity.class);
							}
							mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							// start MyTicketActivity
							SplashActivity.this.startActivity(mainIntent);
							// finish activity
							SplashActivity.this.finish();
						}

					} else {
						// Show error alret for device IMEI no more active on
						// server
						AlertDialog.Builder alertbox = new AlertDialog.Builder(
								SplashActivity.this);
						// set the title for this Alert Dialog
						alertbox.setTitle(getResources().getString(
								R.string.error_message));
						// set the message for this Alert Dialog
						alertbox.setMessage(message);
						// set cancelable false outbox in app click
						alertbox.setCancelable(false);
						alertbox.setNeutralButton(
								getResources().getString(R.string.ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// Dismsss the Dialog on click on OK.
										dialog.dismiss();
										// finish dialog activity
										finish();
									}
								});
						alertbox.show();
					}
				} catch (Exception e) {
					//Google Analytic -Tracking Exception 
					EosApplication.getInstance().trackException(e);
					e.printStackTrace();
				}
			}
		}
	}

//	private void setupNewGcmID() {
//		// Make sure the device has the proper dependencies.
//		GCMRegistrar.checkDevice(this);
//		// Make sure the manifest was properly set - comment out this line
//		// while developing the app, then uncomment it when it's ready.
//		GCMRegistrar.checkManifest(this);
//		// unregister old gcm id
//		GCMRegistrar.unregister(SplashActivity.this);
//		// register new gcm id
//		GCMRegistrar.register(this, ApplicationConstant.GCM_SENDER_ID);
//		GCMRegistrar.setRegisteredOnServer(this, true);
//		// GCMRegistrar.
//		String new_gcm_registration_id = GCMRegistrar.getRegistrationId(this);
//	}
}
