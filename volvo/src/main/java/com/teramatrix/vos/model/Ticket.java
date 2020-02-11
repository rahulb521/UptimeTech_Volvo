package com.teramatrix.vos.model;

import java.io.Serializable;

/**
 * 
 * @author Gaurav.Mangal
 * 
 * Ticket Class is used as a Model Class for stored tickets data values
 */
public class Ticket implements Serializable {
	public String Id;
	public String Description;
	public String AssignedTo;
	public String TicketStatus;
	public String Ticket_Previous_Status;
	

	public String TicketStatusText;
	public String Priority;
	public String CreationTime;
	public String LastModifiedBy;
	public String LastModifiedTime;
	public String LastModifiedTimeInCurrentTimeZone;
	public String LastModifiedTimeInMilliSec;
	public String BreakDownLocation;
	public String BreackDownLongitude;
	public String BreackDownLatitude;
	public String IsDeclined;
	public String EstimatedTimeForJobComplition;
	public String AcheviedEstimatedTimeForJobComplition;
	public String EstimatedCostForJobComplition;
	public String TotalTicketLifeCycleTimeSlab;
	public String SlaTimeAchevied;
	public String SlaStatus;
	public String SuggestionComment;
	public String EstimatedTimeForJobComplitionSubmitTime;
	public String editEstimationTimeReasonsValue;
	public String VehicleRegistrationNo;
	public String vehicleType;
	public String declineReason;
	public String slaMissedReasonId;
	public String partsReplaced;
	public boolean isTicketOffLine;
	public String CustomeContact_no;
	public String EstimatedDistance;
	public String OwnerContact_no;
	public String IsTripEnd;
	

	public int counter_estiamted_cost_update;
	public int counter_estiamted_time_update;
	
	
	public boolean isVanReachedConfirmed;
	
	public static String TICKET_PRIORITY_EOS ="ticket_from_eos";
	public static String TICKET_PRIORITY_TELIMATIC ="ticket_from_telimatic";
	
	public String getSlaMissedReasonId() {
		return slaMissedReasonId;
	}

	public String getSlaStatus() {
		return SlaStatus;
	}

	public void setSlaStatus(String slaStatus) {
		SlaStatus = slaStatus;
	}

	public void setSlaMissedReasonId(String slaMissedReasonId) {
		this.slaMissedReasonId = slaMissedReasonId;
	}

	public Ticket() {
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}

	public String getAssignedTo() {
		return AssignedTo;
	}

	public void setAssignedTo(String assignedTo) {
		AssignedTo = assignedTo;
	}

	public String getTicketStatus() {
		return TicketStatus;
	}

	public void setTicketStatus(String ticketStatus) {
		TicketStatus = ticketStatus;
	}

	public String getPriority() {
		return Priority;
	}

	public void setPriority(String priority) {
		Priority = priority;
	}

	public String getCreationTime() {
		return CreationTime;
	}

	public void setCreationTime(String creationTime) {
		CreationTime = creationTime;
	}

	public String getLastModifiedBy() {
		return LastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		LastModifiedBy = lastModifiedBy;
	}

	public String getLastModifiedTime() {
		return LastModifiedTime;
	}

	public void setLastModifiedTime(String lastModifiedTime) {
		LastModifiedTime = lastModifiedTime;
	}

	public String getBreakDownLocation() {
		return BreakDownLocation;
	}

	public void setBreakDownLocation(String breakDownLocation) {
		BreakDownLocation = breakDownLocation;
	}

	public String getBreackDownLongitude() {
		return BreackDownLongitude;
	}

	public void setBreackDownLongitude(String breackDownLongitude) {
		BreackDownLongitude = breackDownLongitude;
	}

	public String getBreackDownLatitude() {
		return BreackDownLatitude;
	}

	public void setBreackDownLatitude(String breackDownLatitude) {
		BreackDownLatitude = breackDownLatitude;
	}

	public String getIsDeclined() {
		return IsDeclined;
	}

	public void setIsDeclined(String isDeclined) {
		IsDeclined = isDeclined;
	}

	public String getEstimatedTimeForJobComplition() {
		return EstimatedTimeForJobComplition;
	}

	public void setEstimatedTimeForJobComplition(
			String estimatedTimeForJobComplition) {
		EstimatedTimeForJobComplition = estimatedTimeForJobComplition;
	}

	public String getTotalTicketLifeCycleTimeSlab() {
		return TotalTicketLifeCycleTimeSlab;
	}

	public void setTotalTicketLifeCycleTimeSlab(
			String totalTicketLifeCycleTimeSlab) {
		TotalTicketLifeCycleTimeSlab = totalTicketLifeCycleTimeSlab;
	}

	public String getEstimatedTimeForJobComplitionSubmitTime() {
		return EstimatedTimeForJobComplitionSubmitTime;
	}

	public void setEstimatedTimeForJobComplitionSubmitTime(
			String estimatedTimeForJobComplitionSubmitTime) {
		EstimatedTimeForJobComplitionSubmitTime = estimatedTimeForJobComplitionSubmitTime;
	}

	public String getVehicleRegistrationNo() {
		return VehicleRegistrationNo;
	}

	public void setVehicleRegistrationNo(String vehicleRegistrationNo) {
		VehicleRegistrationNo = vehicleRegistrationNo;
	}

	public String getEstimatedCostForJobComplition() {
		return EstimatedCostForJobComplition;
	}

	public void setEstimatedCostForJobComplition(
			String estimatedCostForJobComplition) {
		EstimatedCostForJobComplition = estimatedCostForJobComplition;
	}

	public String getIsTripEnd() {
		return IsTripEnd;
	}

	public void setIsTripEnd(String isTripEnd) {
		IsTripEnd = isTripEnd;
	}
	public String getTicket_Previous_Status() {
		return Ticket_Previous_Status;
	}

	public void setTicket_Previous_Status(String ticket_Previous_Status) {
		Ticket_Previous_Status = ticket_Previous_Status;
	}
	
	public Ticket(String id, String description, String assignedTo,
			String ticketStatus, String priority, String creationTime,
			String lastModifiedBy, String lastModifiedTime,
			String breakDownLocation, String breackDownLongitude,
			String breackDownLatitude, String isDeclined,
			String estimatedTimeForJobComplition,
			String totalTicketLifeCycleTimeSlab,
			String estimatedTimeForJobComplitionSubmitTime,
			String vehicleRegistrationNo) {
		Id = id;
		Description = description;
		AssignedTo = assignedTo;
		TicketStatus = ticketStatus;
		Priority = priority;
		CreationTime = creationTime;
		LastModifiedBy = lastModifiedBy;
		LastModifiedTime = lastModifiedTime;
		BreakDownLocation = breakDownLocation;
		BreackDownLongitude = breackDownLongitude;
		BreackDownLatitude = breackDownLatitude;
		IsDeclined = isDeclined;
		EstimatedTimeForJobComplition = estimatedTimeForJobComplition;
		TotalTicketLifeCycleTimeSlab = totalTicketLifeCycleTimeSlab;
		EstimatedTimeForJobComplitionSubmitTime = estimatedTimeForJobComplitionSubmitTime;
		VehicleRegistrationNo = vehicleRegistrationNo;
	}

}
