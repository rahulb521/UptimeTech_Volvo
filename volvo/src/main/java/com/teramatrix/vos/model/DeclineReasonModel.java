package com.teramatrix.vos.model;

/**
 * 
 * @author Gaurav.Mangal
 * DeclineReasonModel Class is used as a Model Class for stored reason with name and type id
 */
public class DeclineReasonModel {
	public String id;
	public String reason_name;
	public String reason_type;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getReason_name() {
		return reason_name;
	}

	public void setReason_name(String reason_name) {
		this.reason_name = reason_name;
	}

	public DeclineReasonModel(String id, String reason_name) {
		this.id = id;
		this.reason_name = reason_name;
	}

	public DeclineReasonModel() {
	}

}
