package com.teramatrix.vos.model;

import java.io.Serializable;

/**
 * 
 * @author Gaurav.Mangal
 * 
 * LocationTrackingLog Class is used as a Model Class for stored latitude,longitude,logtime,network_state values
 *logtime
 */
public class LocationTrackingLog implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String latitude;
	private String longitude;
	private String logtime;
	private String network_state;
	private String charging_state;
	private String battery_label;
	private String gps_state;

	
	public LocationTrackingLog() {
		// TODO Auto-generated constructor stub
	}


	public String getLatitude() {
		return latitude;
	}


	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}


	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}


	public String getLogtime() {
		return logtime;
	}


	public void setLogtime(String logtime) {
		this.logtime = logtime;
	}


	public String getNetwork_state() {
		return network_state;
	}


	public void setNetwork_state(String network_state) {
		this.network_state = network_state;
	}


	public String getCharging_state() {
		return charging_state;
	}


	public void setCharging_state(String charging_state) {
		this.charging_state = charging_state;
	}


	public String getBattery_label() {
		return battery_label;
	}


	public void setBattery_label(String battery_label) {
		this.battery_label = battery_label;
	}


	public String getGps_state() {
		return gps_state;
	}


	public void setGps_state(String gps_state) {
		this.gps_state = gps_state;
	}
	
	
	
	

}
