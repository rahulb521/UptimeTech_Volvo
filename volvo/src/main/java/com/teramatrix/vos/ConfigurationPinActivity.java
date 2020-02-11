package com.teramatrix.vos;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teramatrix.vos.asynctasks.ConfigurationPin;
import com.teramatrix.vos.asynctasks.ConfigurationSignin;
import com.teramatrix.vos.checkinternet.CheckInternetConnection;
import com.teramatrix.vos.interfaces.INetworkAvailablity;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.utils.UtilityFunction;

/**
 * @author Gaurav.Mangal
 * 
 * ConfigurationPinActivity is used to configure pin first time
 * its one time process, once you registered your pin it will 
 * not ask you again to configure pin.
 * 
 *  If user has registered his licence  on server but not registered password/pin and leave this process for future , 
 *  Configure Licence screen will be skipped since already done and this will be the first screen opened after splash .
 */
public class ConfigurationPinActivity extends Activity implements
		INetworkAvailablity {
	// Defining TextView component that will be used in this Activity
	private TextView  tv_pin_retype_title;
	
	// Defining EditText component that will be used in this Activity
	private EditText eTextpin_digit1, eTextpin_digit2, eTextpin_digit3,
			eTextpin_digit4, eTextretype_pin_digit1, eTextretype_pin_digit2,
			eTextretype_pin_digit3, eTextretype_pin_digit4;
	
	// Defining String variables that will be used in this Activity
	private String str_pinpassword, str_retype_pinpassword, strSecurityToken,
			strDeviceAlias;
	
	// Defining TextView component that will be used in this Activity
	private TextView btn_configuration_pin;
	
	// Defining a view class for this Activity
	private ConfigurationPin configurationPin;
	
	// Defining boolean variable that will be used in this Activity
	private boolean hasInternetConnect;
	
	// Defining CheckInternetConnection class that will be used in this Activity
	private CheckInternetConnection checkinternet;
	
	// Defining VECVPreferences class that will be used in this Activity to store value
	private VECVPreferences vecvPreferences;
	
	// Defining ConfigurationSignin class that will be used in this Activity
	private ConfigurationSignin configurationSigninPin;
	
	// Defining TypeFace font component that will be used in this activity
	Typeface centuryGothic;
	
	// Defining LinearLayout component that will be used in this activity
	LinearLayout ll_pin_retype;
	
	// Defining ImageView component that will be used in this activity

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//define layout with no title with full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
		//change Status bar color
		Window window = getWindow();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.setStatusBarColor(ContextCompat.getColor(this,R.color.volvo_blue));
		}

		//setting a layout
		setContentView(R.layout.screen_configuration_pin);

		// set up UI elements
		setDataUi();

		// call to configuration pin
		btn_configuration_pin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				onConfigurepin_passowrd();

				/*if (checkpin.length() > 0) {
					onConfiguresign_in();
				} else {

				}*/

			}
		});

		
		/**
		 * onTextChanged() method focus only one digit to another edit text digit .
		 */
		eTextpin_digit1.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if (eTextpin_digit1.getText().toString().length() == 1) // size
																		// as
																		// per
																		// requirement
				{
					//request focus move
					eTextpin_digit2.requestFocus();
				}
			}

			/**
			 * beforeTextChanged means that the characters are about to be replaced with some new text. The text is uneditable.
			 */
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
				
			}

		});
		
		/**
		 * onTextChanged() method focus only one digit to another edit text digit .
		 */
		eTextpin_digit2.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if (eTextpin_digit2.getText().toString().length() == 1) // size
																		// as
																		// per
																		// requirement
				{
					//request focus move
					eTextpin_digit3.requestFocus();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}

		});
		
		/**
		 * onTextChanged() method focus only one digit to another edit text digit .
		 */
		eTextpin_digit3.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if (eTextpin_digit3.getText().toString().length() == 1) // size
																		// as
																		// per
																		// requirement
				{
					//request focus move
					eTextpin_digit4.requestFocus();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}

		});

		
		/**
		 * onTextChanged() method focus only one digit to another edit text digit .
		 */
		eTextpin_digit4.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if (eTextpin_digit4.getText().toString().length() == 1) // size// as// per// requirement
				{
					//request focus move
					eTextretype_pin_digit1.requestFocus();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});
		
		/**
		 * onTextChanged() method focus only one digit to another edit text digit .
		 */
		eTextretype_pin_digit1.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if (eTextretype_pin_digit1.getText().toString().length() == 1) // size// as// per// requirement
				{
					//request focus move
					eTextretype_pin_digit2.requestFocus();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}

		});
		
		/**
		 * onTextChanged() method focus only one digit to another edit text digit .
		 */

		eTextretype_pin_digit2.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if (eTextretype_pin_digit2.getText().toString().length() == 1) // size// as// per// requirement
				{
					//request focus move
					eTextretype_pin_digit3.requestFocus();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}

		});
		
		/**
		 * onTextChanged() method focus only one digit to another edit text digit .
		 */

		eTextretype_pin_digit3.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if (eTextretype_pin_digit3.getText().toString().length() == 1)// size// as// per// requirement
				{
					//request focus move
					eTextretype_pin_digit4.requestFocus();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}

		});

	}

	/* Initializing UI components */
	private void setDataUi() {
		
		// Create an object of the activity component
		
		vecvPreferences = new VECVPreferences(ConfigurationPinActivity.this);
		checkinternet = new CheckInternetConnection(getApplicationContext());



		// initialize EditText component
		
		eTextpin_digit1 = (EditText) findViewById(R.id.eText_pin1);
		eTextpin_digit2 = (EditText) findViewById(R.id.eText_pin2);
		eTextpin_digit3 = (EditText) findViewById(R.id.eText_pin3);
		eTextpin_digit4 = (EditText) findViewById(R.id.eText_pin4);

		// Initialize all classes and views those we are going to use in this font value
		
		eTextpin_digit1.setTypeface(centuryGothic);
		eTextpin_digit2.setTypeface(centuryGothic);
		eTextpin_digit3.setTypeface(centuryGothic);
		eTextpin_digit4.setTypeface(centuryGothic);

		ll_pin_retype = (LinearLayout) findViewById(R.id.ll_pin_retype);
		eTextretype_pin_digit1 = (EditText) findViewById(R.id.eText_pin1_retype);
		eTextretype_pin_digit2 = (EditText) findViewById(R.id.eText_pin2_retype);
		eTextretype_pin_digit3 = (EditText) findViewById(R.id.eText_pin3_retype);
		eTextretype_pin_digit4 = (EditText) findViewById(R.id.eText_pin4_retype);

		eTextretype_pin_digit1.setTypeface(centuryGothic);
		eTextretype_pin_digit2.setTypeface(centuryGothic);
		eTextretype_pin_digit3.setTypeface(centuryGothic);
		eTextretype_pin_digit4.setTypeface(centuryGothic);

		btn_configuration_pin = (TextView) findViewById(R.id.btn_login);
		tv_pin_retype_title = (TextView) findViewById(R.id.tv_renter_pin);
		// initialize TypeFace Font view component that will be used in this
		// activity
		centuryGothic = Typeface.createFromAsset(getAssets(), "gothic_0.TTF");
		// Initialize all classes and views those we are going to use in this
		// font value
		/*if (checkpin.length() > 0) {
			ll_pin_retype.setVisibility(View.GONE);
			eTextpin_digit4.setImeOptions(EditorInfo.IME_ACTION_DONE);
			tv_pin_retype_title.setVisibility(View.GONE);
		}*/

	}

	/**
	 * This method get text from edit text digit and check validation, sent values to server 
	 */
	private void onConfigurepin_passowrd() {

		// Defining String variables that will be used in this Activity
		String pin_digit1, pin_digit2, pin_digit3, pin_digit4, repin_digit1, repin_digit2, repin_digit3, repin_digit4;
		pin_digit1 = eTextpin_digit1.getText().toString();
		pin_digit2 = eTextpin_digit2.getText().toString();
		pin_digit3 = eTextpin_digit3.getText().toString();
		pin_digit4 = eTextpin_digit4.getText().toString();

		repin_digit1 = eTextretype_pin_digit1.getText().toString();
		repin_digit2 = eTextretype_pin_digit2.getText().toString();
		repin_digit3 = eTextretype_pin_digit3.getText().toString();
		repin_digit4 = eTextretype_pin_digit4.getText().toString();

		//define boolean value of the variable
		boolean pinCancel = false;
		
		//defines View of an activity
		View pinFocusView = null;

		//check digit validation pin type
		if (TextUtils.isEmpty(pin_digit1) || TextUtils.isEmpty(pin_digit2)
				|| TextUtils.isEmpty(pin_digit3)
				|| TextUtils.isEmpty(pin_digit4)) {
			
			//show center toast message content
			UtilityFunction.showCenteredToast(this,
					getResources().getString(R.string.error_pin_password));
			pinFocusView = eTextpin_digit1;
			pinCancel = true;
		}

		//check digit validation retype pin
		if (TextUtils.isEmpty(repin_digit1) || TextUtils.isEmpty(repin_digit2)
				|| TextUtils.isEmpty(repin_digit3)
				|| TextUtils.isEmpty(repin_digit4)) {
			
			//show center toast message content
			UtilityFunction.showCenteredToast(this,
					getResources().getString(R.string.error_pin_password));
			pinFocusView = eTextretype_pin_digit1;
			pinCancel = true;
		}

		//pin type value check 
		if (pinCancel) {
			//move view focus
			pinFocusView.requestFocus();
		} else {
			//concat four digit pin
			str_pinpassword = pin_digit1.concat(pin_digit2).concat(pin_digit3)
					.concat(pin_digit4);
			//concat four digit retype pin
			str_retype_pinpassword = repin_digit1.concat(repin_digit2)
					.concat(repin_digit3).concat(repin_digit4);
			
			
			//check validation and match pin and retype pin
			if (!str_pinpassword.equals(str_retype_pinpassword)) {
				
				//show center toast message
				UtilityFunction.showCenteredToast(this, getResources()
						.getString(R.string.error_pin_password));
			} else {
				
				//get value for security token in vecvPreferences instance
				strSecurityToken = vecvPreferences.getSecurityToken();
				
				//get value for device alias  in vecvPreferences instance
				strDeviceAlias = vecvPreferences.getDevice_alias();
				
				//check value in blank or not
				if (!strSecurityToken.equals("")) {
					
					//check internet connection
					if (new CheckInternetConnection(this)
							.isConnectedToInternet()) {
						
						//call api for configure pin task
						configurationPin = new ConfigurationPin(
								ConfigurationPinActivity.this,
								strSecurityToken, strDeviceAlias,
								str_pinpassword);
						//execute the pin 
						configurationPin.execute();
					} else {
						
						//show center toast
						UtilityFunction
								.showCenteredToast(
										ConfigurationPinActivity.this,
										getResources().getString(R.string.internet_connection));
					}
				} else {
					//show center toast
					UtilityFunction.showMessage(ConfigurationPinActivity.this,
							getResources().getString(R.string.security_token));
				}
			}

		}
	}
/*
 * (non-Javadoc)
 * @see android.app.Activity#onStart()
 */
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}
/*
 * (non-Javadoc)
 * @see android.app.Activity#onResume()
 * 
 * This method call after onCreate method and set the IS_APP_IN_FORGROUND value "true"
 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//set value when app IS_APP_IN_FORGROUND
		ApplicationConstant.IS_APP_IN_FORGROUND = true;
		ApplicationConstant.currentActivityContext = this;
		isNetworkAvailable(new CheckInternetConnection(this)
				.isConnectedToInternet());
	}
/*
 * (non-Javadoc)
 * @see android.app.Activity#onPause()
 * 
 * This method call after onResume method ,when app screens run in backgorund and set value IS_APP_IN_FORGROUND "false" 
 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//set value when app IS_APP_IN_FORGROUND
		ApplicationConstant.IS_APP_IN_FORGROUND = false;
		ApplicationConstant.currentActivityContext = null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 * 
	 * This method call after onPause.
	 */

	@Override
	protected void onStop() {
		
		//call super class constructor
		super.onStop();
	}

	//this method check the pin on server request
	private void onConfiguresign_in() {
		//define string variable 
		String pin_digit1, pin_digit2, pin_digit3, pin_digit4;
		
		//get text from edittext
		pin_digit1 = eTextpin_digit1.getText().toString();
		pin_digit2 = eTextpin_digit2.getText().toString();
		pin_digit3 = eTextpin_digit3.getText().toString();
		pin_digit4 = eTextpin_digit4.getText().toString();

		//define boolean variable
		boolean pinCancel = false;
		
		//define view instance
		View pinFocusView = null;

		//check validation
		if (TextUtils.isEmpty(pin_digit1) || TextUtils.isEmpty(pin_digit2)
				|| TextUtils.isEmpty(pin_digit3)
				|| TextUtils.isEmpty(pin_digit4)) {
			
			//show toast message
			UtilityFunction.showCenteredToast(this,
					getResources().getString(R.string.error_pin_password));
			pinFocusView = eTextpin_digit1;
			pinCancel = true;
		}
		if (pinCancel) {
			//change request focus
			pinFocusView.requestFocus();
		} else {
			//concat pin digit
			str_pinpassword = pin_digit1.concat(pin_digit2).concat(pin_digit3)
					.concat(pin_digit4);
			
			//get values from vecv preferences
			strSecurityToken = vecvPreferences.getSecurityToken();
			strDeviceAlias = vecvPreferences.getDevice_alias();
			
			//check value empty or not
			if (!strSecurityToken.equals("")) {
				
				//check internet connection
				if (new CheckInternetConnection(this).isConnectedToInternet()) {

					//call api
					configurationSigninPin = new ConfigurationSignin(
							ConfigurationPinActivity.this, strSecurityToken,
							strDeviceAlias, str_pinpassword);
					configurationSigninPin.execute();
				} else {
					
					//show toast message
					UtilityFunction.showCenteredToast(
							ConfigurationPinActivity.this,getResources().getString(R.string.internet_connection));
				}
			} else {
				
				//show toast message
				UtilityFunction.showMessage(ConfigurationPinActivity.this,
						getResources().getString(R.string.security_token));
			}
		}
	}
/*
 * (non-Javadoc)
 * @see com.teramatrix.vecv.interfaces.INetworkAvailablity#isNetworkAvailable(boolean)
 * 
 * This method check the network is available or not
 */
	@Override
	public void isNetworkAvailable(boolean isNetworkAvailable) {

		if (isNetworkAvailable) {
			//img_network.setImageResource(R.drawable.wifi_enalbed);
		} else {
			//img_network.setImageResource(R.drawable.wifi_disalbed);

		}
	}
}
