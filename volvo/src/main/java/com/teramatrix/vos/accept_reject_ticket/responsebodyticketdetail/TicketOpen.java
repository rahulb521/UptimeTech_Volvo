
package com.teramatrix.vos.accept_reject_ticket.responsebodyticketdetail;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TicketOpen {

    @SerializedName("TicketId")
    @Expose
    private String ticketId;
    @SerializedName("TicketIdAlias")
    @Expose
    private String ticketIdAlias;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("AssignedTo")
    @Expose
    private String assignedTo;
    @SerializedName("ReportedVia")
    @Expose
    private Object reportedVia;
    @SerializedName("TicketStatus")
    @Expose
    private Integer ticketStatus;
    @SerializedName("Priority")
    @Expose
    private Integer priority;
    @SerializedName("CustomerScore")
    @Expose
    private Object customerScore;
    @SerializedName("CreatedBy")
    @Expose
    private String createdBy;
    @SerializedName("CreationTime")
    @Expose
    private String creationTime;
    @SerializedName("LastModifiedBy")
    @Expose
    private String lastModifiedBy;
    @SerializedName("LastModifiedTime")
    @Expose
    private String lastModifiedTime;
    @SerializedName("BreakdownLocation")
    @Expose
    private String breakdownLocation;
    @SerializedName("BreakdownLongitude")
    @Expose
    private String breakdownLongitude;
    @SerializedName("BreakdownLattitude")
    @Expose
    private String breakdownLattitude;
    @SerializedName("AssignedToUserId")
    @Expose
    private String assignedToUserId;
    @SerializedName("AssignedToUserLattitude")
    @Expose
    private String assignedToUserLattitude;
    @SerializedName("AssignedToUserLongitude")
    @Expose
    private String assignedToUserLongitude;
    @SerializedName("Isdeclined")
    @Expose
    private Boolean isdeclined;
    @SerializedName("EstimatedTimeForJobCompletion")
    @Expose
    private Object estimatedTimeForJobCompletion;
    @SerializedName("TotalTicketLifecycleTimeSla")
    @Expose
    private Object totalTicketLifecycleTimeSla;
    @SerializedName("EstimatedTimeForJobCompletionSubmitTime")
    @Expose
    private String estimatedTimeForJobCompletionSubmitTime;
    @SerializedName("VehicleRegisterNumber")
    @Expose
    private String vehicleRegisterNumber;
    @SerializedName("BreakdownLocationLandmark")
    @Expose
    private String breakdownLocationLandmark;
    @SerializedName("RouteId")
    @Expose
    private Integer routeId;
    @SerializedName("CustomerContactNo")
    @Expose
    private String customerContactNo;
    @SerializedName("RepairCost")
    @Expose
    private Object repairCost;
    @SerializedName("DefaultSlaTime")
    @Expose
    private Integer defaultSlaTime;
    @SerializedName("SlaMissedReason")
    @Expose
    private Object slaMissedReason;
    @SerializedName("SuggestionComment")
    @Expose
    private String suggestionComment;
    @SerializedName("JobCompleteResponseTime")
    @Expose
    private Object jobCompleteResponseTime;
    @SerializedName("DefaultCol2")
    @Expose
    private String defaultCol2;
    @SerializedName("DefaultCol3")
    @Expose
    private String defaultCol3;
    @SerializedName("EstimatedDistance")
    @Expose
    private String estimatedDistance;
    @SerializedName("Owner_Contact_no")
    @Expose
    private String ownerContactNo;
    @SerializedName("IsTripEnd")
    @Expose
    private Boolean isTripEnd;

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketIdAlias() {
        return ticketIdAlias;
    }

    public void setTicketIdAlias(String ticketIdAlias) {
        this.ticketIdAlias = ticketIdAlias;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Object getReportedVia() {
        return reportedVia;
    }

    public void setReportedVia(Object reportedVia) {
        this.reportedVia = reportedVia;
    }

    public Integer getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(Integer ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Object getCustomerScore() {
        return customerScore;
    }

    public void setCustomerScore(Object customerScore) {
        this.customerScore = customerScore;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(String lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getBreakdownLocation() {
        return breakdownLocation;
    }

    public void setBreakdownLocation(String breakdownLocation) {
        this.breakdownLocation = breakdownLocation;
    }

    public String getBreakdownLongitude() {
        return breakdownLongitude;
    }

    public void setBreakdownLongitude(String breakdownLongitude) {
        this.breakdownLongitude = breakdownLongitude;
    }

    public String getBreakdownLattitude() {
        return breakdownLattitude;
    }

    public void setBreakdownLattitude(String breakdownLattitude) {
        this.breakdownLattitude = breakdownLattitude;
    }

    public String getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(String assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

    public String getAssignedToUserLattitude() {
        return assignedToUserLattitude;
    }

    public void setAssignedToUserLattitude(String assignedToUserLattitude) {
        this.assignedToUserLattitude = assignedToUserLattitude;
    }

    public String getAssignedToUserLongitude() {
        return assignedToUserLongitude;
    }

    public void setAssignedToUserLongitude(String assignedToUserLongitude) {
        this.assignedToUserLongitude = assignedToUserLongitude;
    }

    public Boolean getIsdeclined() {
        return isdeclined;
    }

    public void setIsdeclined(Boolean isdeclined) {
        this.isdeclined = isdeclined;
    }

    public Object getEstimatedTimeForJobCompletion() {
        return estimatedTimeForJobCompletion;
    }

    public void setEstimatedTimeForJobCompletion(Object estimatedTimeForJobCompletion) {
        this.estimatedTimeForJobCompletion = estimatedTimeForJobCompletion;
    }

    public Object getTotalTicketLifecycleTimeSla() {
        return totalTicketLifecycleTimeSla;
    }

    public void setTotalTicketLifecycleTimeSla(Object totalTicketLifecycleTimeSla) {
        this.totalTicketLifecycleTimeSla = totalTicketLifecycleTimeSla;
    }

    public String getEstimatedTimeForJobCompletionSubmitTime() {
        return estimatedTimeForJobCompletionSubmitTime;
    }

    public void setEstimatedTimeForJobCompletionSubmitTime(String estimatedTimeForJobCompletionSubmitTime) {
        this.estimatedTimeForJobCompletionSubmitTime = estimatedTimeForJobCompletionSubmitTime;
    }

    public String getVehicleRegisterNumber() {
        return vehicleRegisterNumber;
    }

    public void setVehicleRegisterNumber(String vehicleRegisterNumber) {
        this.vehicleRegisterNumber = vehicleRegisterNumber;
    }

    public String getBreakdownLocationLandmark() {
        return breakdownLocationLandmark;
    }

    public void setBreakdownLocationLandmark(String breakdownLocationLandmark) {
        this.breakdownLocationLandmark = breakdownLocationLandmark;
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    public String getCustomerContactNo() {
        return customerContactNo;
    }

    public void setCustomerContactNo(String customerContactNo) {
        this.customerContactNo = customerContactNo;
    }

    public Object getRepairCost() {
        return repairCost;
    }

    public void setRepairCost(Object repairCost) {
        this.repairCost = repairCost;
    }

    public Integer getDefaultSlaTime() {
        return defaultSlaTime;
    }

    public void setDefaultSlaTime(Integer defaultSlaTime) {
        this.defaultSlaTime = defaultSlaTime;
    }

    public Object getSlaMissedReason() {
        return slaMissedReason;
    }

    public void setSlaMissedReason(Object slaMissedReason) {
        this.slaMissedReason = slaMissedReason;
    }

    public String getSuggestionComment() {
        return suggestionComment;
    }

    public void setSuggestionComment(String suggestionComment) {
        this.suggestionComment = suggestionComment;
    }

    public Object getJobCompleteResponseTime() {
        return jobCompleteResponseTime;
    }

    public void setJobCompleteResponseTime(Object jobCompleteResponseTime) {
        this.jobCompleteResponseTime = jobCompleteResponseTime;
    }

    public String getDefaultCol2() {
        return defaultCol2;
    }

    public void setDefaultCol2(String defaultCol2) {
        this.defaultCol2 = defaultCol2;
    }

    public String getDefaultCol3() {
        return defaultCol3;
    }

    public void setDefaultCol3(String defaultCol3) {
        this.defaultCol3 = defaultCol3;
    }

    public String getEstimatedDistance() {
        return estimatedDistance;
    }

    public void setEstimatedDistance(String estimatedDistance) {
        this.estimatedDistance = estimatedDistance;
    }

    public String getOwnerContactNo() {
        return ownerContactNo;
    }

    public void setOwnerContactNo(String ownerContactNo) {
        this.ownerContactNo = ownerContactNo;
    }

    public Boolean getIsTripEnd() {
        return isTripEnd;
    }

    public void setIsTripEnd(Boolean isTripEnd) {
        this.isTripEnd = isTripEnd;
    }

}
