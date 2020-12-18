package com.teramatrix.vos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.BuildConfig;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.teramatrix.vos.asynctasks.ConfigurationLicense;
import com.teramatrix.vos.checkinternet.CheckInternetConnection;
import com.teramatrix.vos.interfaces.INetworkAvailablity;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.PermissionsUtils;
import com.teramatrix.vos.utils.TimeFormater;
import com.teramatrix.vos.utils.UtilityFunction;
import com.teramatrix.vos.volvouptime.UpTimeVehicleListActivity;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

/**
 * @author Gaurav.Mangal
 * 
 *         ConfigurationLicenseActivity is used to configure license first time
 *         its one time process, once you registered your device it will not ask
 *         you again to configure. Licence number provided by CCE to user When
 *         screen opens Unique GCMID is generated in background from GCM
 *         Service. It may take delay to generate until than user can not
 *         process registration.
 */
public class ConfigurationLicenseActivity extends Activity implements INetworkAvailablity {
	// Defining EditText component that will be used in this Activity
	private EditText eTextLicenseNum;

	// Defining String variables that will be used in this Activity
	private String strLicenseNum, strIMEINumber;

	// Defining TextView component that will be used in this Activity
	private TextView btn_configure;

	// Defining a view class for this Activity
	private ConfigurationLicense configurationLicense;


	// Defining boolean variable that will be used in this Activity
	private boolean hasInternetConnect;

	// Defining CheckInternetConnection class that will be used in this Activity
	private CheckInternetConnection checkinternet;

	// Defining VECVPreferences class that will be used in this Activity to
	// store value
	private VECVPreferences vecvPreferences;

	// Defining ImageView component that will be used in this Activity
	// Defining TypeFace font component that will be used in this activity

	// Defining TypeFace font component that will be used in this activity
	Typeface centuryGothic;
	Typeface verdana;

	// Defining String variables GCM that will be used in this Activity
	String gcm_registration_id = "";
	Toast gcm_not_set_toast;

	private int eicher_logo_tap_count;


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * 
	 * This onCreate method call automatically and setup data ui component
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


			// define layout with no title with full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		/*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

		//change Status bar color
		Window window = getWindow();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.setStatusBarColor(ContextCompat.getColor(this,R.color.volvo_blue));
		}

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		// setting a layout
		setContentView(R.layout.screen_configuration_license);
		try {
			// Set up gcm id for receving push messages
			//setUpGCM();
			// Setup a layout for Configuration License
			setdataUI();
		} catch (Exception e) {
			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);
			// save error log when error is created
			UtilityFunction.saveErrorLog(ConfigurationLicenseActivity.this, e);
		}
		//Ask for External Permission
		new PermissionsUtils().requestForPermission(this);
	}

	/**
	 * setup data ui component an activity
	 */
	private void setdataUI() {
		// Create an object of the activity component
		// create instance of the vecv preferences
		vecvPreferences = new VECVPreferences(ConfigurationLicenseActivity.this);
		// create instance of the CheckInternetConnection.
		checkinternet = new CheckInternetConnection(getApplicationContext());
		// initialize EditText component
		eTextLicenseNum = (EditText) findViewById(R.id.eTextlicenseNum);

		// initialize TypeFace Font view component that will be used in this
		// activity
		centuryGothic = Typeface.createFromAsset(getAssets(), "gothic_0.TTF");
		// Initialize all classes and views those we are going to use in this
		// font value
		eTextLicenseNum.setTypeface(centuryGothic);

		btn_configure = (TextView) findViewById(R.id.btnConfigure);

		// click on configure button
		btn_configure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// pass the view of the OnClickListener interface
				onConfigureClicked();
			}
		});

		View eicherLogoView = findViewById(R.id.img_header);
		eicherLogoView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				eicher_logo_tap_count++;
				if (eicher_logo_tap_count >= 8) {
					// Open API end point configuration setting dialog.
					openApiEndPointConfigDialog();
				}

				// Launch thread to cancel opening dialog if user does not tap 7
				// times within 8 seconds.
				if (eicher_logo_tap_count == 1) {
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							eicher_logo_tap_count = 0;
						}
					}, 1000 * 7);
				}
			}
		});

		// Set API end URLs
		if (vecvPreferences.getAPIEndPoint_EOS().isEmpty()) {
			//String host_address = BuildConfig.HOST;
			String host_address = "https://uptimecenter.vecv.net:8082/";
			//String host_address = "http://10.10.1.100:9093/";
			//String host_address = "http://169.38.133.115:8081/";
			vecvPreferences.setAPIEndPoint_EOS(host_address);

		}

		//Set Terms and Privacy String
        TextView tv_terms = (TextView) findViewById(R.id.txt_terms_and_condition);
        String messag = "By login, you agree to our Terms and Privacy Policy";
        SpannableString spannableString = new SpannableString(messag);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), messag.indexOf("Terms and Privacy Policy"), messag.length(), 0);
        spannableString.setSpan(new URLSpan("http://playstore.teramatrix.in/"), messag.indexOf("Terms and Privacy Policy"), messag.length(), 0);
        tv_terms.setMovementMethod(LinkMovementMethod.getInstance());
        tv_terms.setText(spannableString);
	}

	/*
	 * Setup GCM id for this app and device
	 */
//	public void setUpGCM() {
//		// Make sure the device has the proper dependencies.
//		GCMRegistrar.checkDevice(this);
//		// Make sure the manifest was properly set - comment out this line
//		// while developing the app, then uncomment it when it's ready.
//		GCMRegistrar.checkManifest(this);
//		// get gcm id
//		gcm_registration_id = GCMRegistrar.getRegistrationId(this);
//		if (ApplicationConstant.IS_LOG_SHOWN)
//			System.out.println("GCM ID:" + gcm_registration_id);
//
//		if (ApplicationConstant.IS_TESTING_TOAST_SHOWN)
//			Toast.makeText(ConfigurationLicenseActivity.this,
//					"gcm_registration_id:" + gcm_registration_id,
//					Toast.LENGTH_SHORT).show();
//		// Check if regid already presents
//		if (gcm_registration_id.equals("")) {
//			// Registration is not present, register now with GCM
//			GCMRegistrar.register(this, ApplicationConstant.GCM_SENDER_ID);
//		} else {
//			// check if GCM ID registered on GCM server.
//			if (!GCMRegistrar.isRegisteredOnServer(this)) {
//				GCMRegistrar.setRegisteredOnServer(this, true);
//			}
//		}
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

		try {
			// set boolean value true when app in foreground.
			ApplicationConstant.IS_APP_IN_FORGROUND = true;
			// set activity context value when app run current screen.
			ApplicationConstant.currentActivityContext = this;
			// check netwoek available or not using CheckInternetConnection
			// class
			/*isNetworkAvailable(new CheckInternetConnection(this)
					.isConnectedToInternet());*/
		} catch (Exception e) {
			// Google Analytic -Tracking Exception
			EosApplication.getInstance().trackException(e);
			// TODO: handle exception
			UtilityFunction.saveErrorLog(ConfigurationLicenseActivity.this, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		// set boolean value true when app in foreground.
		ApplicationConstant.IS_APP_IN_FORGROUND = false;
		// set activity context value when app run current screen.
		ApplicationConstant.currentActivityContext = null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
	}

	/**
	 * /** After clicking on Configure button, Here we will check all
	 * validations and request a service that will check for Configuration
	 * result 1 or 0.
	 */
	protected void onConfigureClicked() {
		strLicenseNum = eTextLicenseNum.getText().toString().trim();
		boolean configureCancel = false;
		View configureFocusView = null;


		if (TextUtils.isEmpty(strLicenseNum)) {
			UtilityFunction.showCenteredToast(this,
					getResources().getString(R.string.required_license));
			configureFocusView = eTextLicenseNum;
			configureCancel = true;
		}
		if (configureCancel) {
			configureFocusView.requestFocus();
		} else {
			try {

				if (new CheckInternetConnection(this).isConnectedToInternet()) {

					// get saved device IMEI nuber
					strIMEINumber = UtilityFunction
							.getIMEINumber(getApplicationContext(),strLicenseNum);

					// get Device GCM id
					// get Device GCM id
					gcm_registration_id = FirebaseInstanceId.getInstance().getToken();
					Log.e("FCMTOKEN",gcm_registration_id);
					if (!strIMEINumber.equals("")) {
						hasInternetConnect = checkinternet
								.isConnectedToInternet();
						if (hasInternetConnect) {

							if (gcm_registration_id != null
									&& gcm_registration_id.length() > 0) {

								// call configuration api if device IMEI and GCM
								// Id if setup
								configurationLicense = new ConfigurationLicense(
										ConfigurationLicenseActivity.this,
										strLicenseNum, strIMEINumber,
										gcm_registration_id);
								configurationLicense.execute();
							} else {
								// show toast message , GCM Id for device not
								// set
								if (gcm_not_set_toast != null)
									gcm_not_set_toast.cancel();
								gcm_not_set_toast = Toast.makeText(
										ConfigurationLicenseActivity.this,
										"GCM Id not set, please restart app!",
										Toast.LENGTH_SHORT);
								gcm_not_set_toast.show();
							}
						} else {
							// show toast message , Network not available
							UtilityFunction.showCenteredToast(
									this,
									getResources().getString(
											R.string.internet_connection));
						}
					} else {

						// show toast message , IMEAI not found
						UtilityFunction.showMessage(this, getResources()
								.getString(R.string.imei_number_not_found));
					}
				} else {

					//If Entered Licence Number is of last used UpTime Licence, redirect user to Home Screen directly(Skip Pin enter activity) in offline mode.
					String lastUsedLicence = new VECVPreferences(ConfigurationLicenseActivity.this).getLicenseKey();
					if(strLicenseNum.equalsIgnoreCase(lastUsedLicence))
					{
						new VECVPreferences(ConfigurationLicenseActivity.this).setCheckconfigure(true);
						new VECVPreferences(ConfigurationLicenseActivity.this).setCheckLogin(true);
						Intent myTicket_Intent = new Intent(ConfigurationLicenseActivity.this,
								UpTimeVehicleListActivity.class);
						myTicket_Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						myTicket_Intent.putExtra("isFromLoginPage",true);
						startActivity(myTicket_Intent);
						finish();
					}else
					{
						// show toast message , Network not available
						UtilityFunction.showCenteredToast(
								ConfigurationLicenseActivity.this, getResources()
										.getString(R.string.internet_connection));
					}
				}
			} catch (Exception e) {
				// Google Analytic -Tracking Exception
				EosApplication.getInstance().trackException(e);

				// e.printStackTrace();
				UtilityFunction.saveErrorLog(ConfigurationLicenseActivity.this,
						e);
			}
		}
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.teramatrix.vecv.interfaces.INetworkAvailablity#isNetworkAvailable
	 * (boolean)
	 */
	@Override
	public void isNetworkAvailable(boolean isNetworkAvailable) {
		if (isNetworkAvailable) {
			//img_network.setImageResource(R.drawable.wifi_enalbed);
		} else {
			//img_network.setImageResource(R.drawable.wifi_disalbed);

			// Create an object of the AlertDialog.Builder class
			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);

			// set the title for this Alert Dialog
			alertbox.setTitle(getResources().getString(R.string.error_message));

			// set the message for this Alert Dialog
			alertbox.setMessage(getResources().getString(
					R.string.internet_connection));
			alertbox.setCancelable(false);
			alertbox.setNeutralButton(getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// Dismsss the Dialog on click on OK.
							dialog.dismiss();

							finish();

						}
					});
			alertbox.show();
		}

	}

	private void openApiEndPointConfigDialog() {
		final Dialog dialog = new Dialog(ConfigurationLicenseActivity.this);
		// Include dialog.xml file
		dialog.setContentView(R.layout.api_end_point_config);
		// Set dialog title
		dialog.setTitle("API Configuration");
		dialog.setCancelable(false);
		// dialog.setCancelable(false);
		final EditText ed_api_end_point_eos = (EditText) dialog
				.findViewById(R.id.ed_api_end_point_eos);
		final EditText ed_api_end_point_telimatic = (EditText) dialog
				.findViewById(R.id.ed_api_end_point_telimatic);

		ed_api_end_point_eos.setText(vecvPreferences.getAPIEndPoint_EOS());
		ed_api_end_point_telimatic.setText(vecvPreferences
				.getAPIEndPoint_Telimatic());

		final View txt_save = dialog.findViewById(R.id.txt_save);
		txt_save.setAlpha(0.5f);
		txt_save.setEnabled(false);

		ed_api_end_point_eos.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				txt_save.setAlpha(1f);
				txt_save.setEnabled(true);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		ed_api_end_point_telimatic.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				txt_save.setAlpha(1f);
				txt_save.setEnabled(true);

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		dialog.findViewById(R.id.txt_cancel).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						dialog.dismiss();
					}
				});
		dialog.findViewById(R.id.txt_save).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						vecvPreferences.setAPIEndPoint_EOS(ed_api_end_point_eos
								.getText().toString());
						vecvPreferences
								.setAPIEndPoint_Telimatic(ed_api_end_point_telimatic
										.getText().toString());

						dialog.dismiss();
					}
				});
		dialog.show();
	}

}
