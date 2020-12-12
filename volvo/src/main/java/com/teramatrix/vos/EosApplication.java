package com.teramatrix.vos;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.util.HashMap;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;

import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.analytics.HitBuilders;
import com.google.firebase.FirebaseApp;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

public class EosApplication extends Application {

	
	private static EosApplication mInstance;
	private static final String PROPERTY_ID = "UA-72984712-2";
	public static int GENERAL_TRACKER = 0;
	public HashMap mTrackers = new HashMap<TrackerName,Tracker>();

	public enum TrackerName {
		APP_TRACKER, GLOBAL_TRACKER, ECOMMERCE_TRACKER,
	}
	
	public EosApplication() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
//		Stetho.InitializerBuilder initializerBuilder = Stetho.newInitializerBuilder(this);
//		initializerBuilder.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this));
//		initializerBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(getApplicationContext()));
//		Stetho.Initializer initializer = initializerBuilder.build();
//		Stetho.initialize(initializer);
		Stetho.initializeWithDefaults(this);
        //initialize Active android database for Volvo Uptime
		Configuration dbConfiguration = new Configuration.Builder(this).setDatabaseName("volvoUptime.db").setDatabaseVersion(4).create();
		ActiveAndroid.initialize(dbConfiguration);
		FirebaseApp.initializeApp(this);
		mInstance = this;

    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
	public static synchronized EosApplication getInstance() {
        return mInstance;
    }

	public synchronized Tracker getTracker(TrackerName appTracker) {
		if (!mTrackers.containsKey(appTracker)) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			Tracker t = analytics.newTracker(PROPERTY_ID);
			mTrackers.put(appTracker, t);
		}
		return (Tracker)mTrackers.get(appTracker);
	}
	/***
     * Tracking screen view
     *
     * @param screenName screen name to be displayed on GA dashboard
     */
	public void trackScreenView(String screenName) {
        Tracker t = getTracker(TrackerName.APP_TRACKER);
 
        // Set screen name.
        t.setScreenName(screenName);
 
        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());
 
        GoogleAnalytics.getInstance(this).dispatchLocalHits();
    }
	public void sendUserDetails(String licence_number,String imei_number,String gcm_id,String login_date)
	{
		Tracker t = getTracker(TrackerName.APP_TRACKER);
		t.set("&cd1", imei_number);
		t.set("&cd2", licence_number);
		t.set("&cd3", gcm_id);
		t.set("&cd4", login_date);
		
		t.send(new HitBuilders.ScreenViewBuilder().build());
		
	}
	
	/***
     * Tracking exception
     *
     * @param e exception to be tracked
     */
	public void trackException(Exception e) {
        if (e != null) {
            Tracker t = getTracker(TrackerName.APP_TRACKER);
 
            t.send(new HitBuilders.ExceptionBuilder()
                            .setDescription(
                                    new StandardExceptionParser(this, null)
                                            .getDescription(Thread.currentThread().getName(), e))
                            .setFatal(false)
                            .build()
            );
        }
    }
	
	/*
	 * **
     * Tracking event
     *
     * @param category event category
     * @param action   action of the event
     * @
     **/
	public void trackEvent(String category, String action, String label) {
        Tracker t = getTracker(TrackerName.APP_TRACKER);
 
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
    }
}
