package com.teramatrix.vos.model;

/*
 * TrackingLocation Class is used as a Model Class for stored location data values
 */
public class TrackingLocation {

	private String latitude;
	private String longitude;
	private String logtime;
	
	private String battery_label;
	private String battery_charging;
	private String gps_state;
	
	private String is_power_saving_mode_on;
	
	

	public String getIs_power_saving_mode_on() {
		return is_power_saving_mode_on;
	}

	public void setIs_power_saving_mode_on(String is_power_saving_mode_on) {
		this.is_power_saving_mode_on = is_power_saving_mode_on;
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

	public String getBattery_label() {
		return battery_label;
	}

	public void setBattery_label(String battery_label) {
		this.battery_label = battery_label;
	}

	public String getBattery_charging() {
		return battery_charging;
	}

	public void setBattery_charging(String battery_charging) {
		this.battery_charging = battery_charging;
	}

	public String getGps_state() {
		return gps_state;
	}

	public void setGps_state(String gps_state) {
		this.gps_state = gps_state;
	}
	
	
	
}
