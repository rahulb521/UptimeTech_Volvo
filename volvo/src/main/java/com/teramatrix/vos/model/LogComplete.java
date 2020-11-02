package com.teramatrix.vos.model;


/**
 * LogComplete Class is used as a Model Class for stored log_message,date_time values
 * @author gaurav.mangal
 *
 */
public class LogComplete {

	private String log_message;
	private String date_time;
	public String getDate_time() {
		return date_time;
	}

	public void setDate_time(String date_time) {
		this.date_time = date_time;
	}

	public String getLog_message() {
		return log_message;
	}

	public void setLog_message(String log_message) {
		this.log_message = log_message;
	}
}
