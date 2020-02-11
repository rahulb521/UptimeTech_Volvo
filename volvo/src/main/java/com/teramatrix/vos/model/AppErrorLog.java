package com.teramatrix.vos.model;

/**
 * 
 * @author gaurav.mangal
 * * AppErrorLog Class is used as a Model Class for store AppErrorLog value .
 */
public class AppErrorLog {

	private String logTime;
	private String errorMessage;

	public String getLogTime() {
		return logTime;
	}

	public void setLogTime(String logTime) {
		this.logTime = logTime;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
