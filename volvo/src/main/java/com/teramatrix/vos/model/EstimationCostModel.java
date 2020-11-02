package com.teramatrix.vos.model;

/**
 * 
 * @author Gaurav.Mangal
 * 
 * EstimationCostModel Class is used as a Model Class for stored cost with cost range and  id
 * 
 */
public class EstimationCostModel {

	public String id;
	public String cost_range;
	public boolean check_radio_value;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCost_range() {
		return cost_range;
	}

	public void setCost_range(String cost_range) {
		this.cost_range = cost_range;
	}

	public boolean isCheck_radio_value() {
		return check_radio_value;
	}

	public void setCheck_radio_value(boolean check_radio_value) {
		this.check_radio_value = check_radio_value;
	}

	public EstimationCostModel(String id, String cost_range,
			boolean check_radio_value) {
		this.id = id;
		this.cost_range = cost_range;
		this.check_radio_value = check_radio_value;
	}

	public EstimationCostModel() {
	}

}
