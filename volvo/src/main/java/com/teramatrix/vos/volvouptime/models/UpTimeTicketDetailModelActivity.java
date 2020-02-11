package com.teramatrix.vos.volvouptime.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

/**
 * Created by arun.singh on 3/27/2018.
 * Model for Ticket details activity
 */

@Table(name = "TicketDetailsModelActivity")
public class UpTimeTicketDetailModelActivity extends Model{


    @Column(name = "StatusAlias")
    public String statusAlias;
    @Column(name = "VehicleRegistration")
    public String vehicleRegistrationNumber;
    @Column(name = "SequenceOrder")
    public String sequenceOrder;
    @Column(name = "TicketId")
    public String ticketId;
    @Column(name = "TicketIdAlias")
    public String ticketIdAlias;
    @Column(name = "StartDate")
    public String startDate;
    @Column(name = "EndDate")
    public String endDate;
    @Column(name = "isSyncWithServer")
    public String isSyncWithServer;
    @Column(name = "IsTicketClosed")
    public String isTicketClosed;
    @Column(name = "UUID")
    public String uuid;
    @Column(name = "ActivityType")
    public String activityType;
    @Column(name = "JobComment")
    public String JobComment;

    public List<UpTimeAddedReasonsModel> upTimeAddedReasonsModels;

    public UpTimeTicketDetailModelActivity()
    {
        super();
    }

    //Getter Setters
    public String getStatusAlias() {
        return statusAlias;
    }

    public void setStatusAlias(String statusAlias) {
        this.statusAlias = statusAlias;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public void setVehicleRegistrationNumber(String vehicleRegistrationNumber) {this.vehicleRegistrationNumber = vehicleRegistrationNumber;}

    public String getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(String sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public List<UpTimeAddedReasonsModel> getUpTimeReasonsList() {
        return upTimeAddedReasonsModels;
    }

    public void setUpTimeReasonsList(List<UpTimeAddedReasonsModel> upTimeAddedReasonsModels) {this.upTimeAddedReasonsModels = upTimeAddedReasonsModels;}

    public String getTicketId() {return ticketId;}

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTicketIdAlias() {
        return ticketIdAlias;
    }

    public void setTicketIdAlias(String ticketIdAlias) {
        this.ticketIdAlias = ticketIdAlias;
    }

    public String getIsTicketClosed() {return isTicketClosed;}

    public void setIsTicketClosed(String isTicketClosed) {this.isTicketClosed = isTicketClosed;}

    public String getIsSyncWithServer() {return isSyncWithServer;}

    public void setIsSyncWithServer(String isSyncWithServer) {this.isSyncWithServer = isSyncWithServer;}

    public String getUuid() {return uuid;}

    public void setUuid(String uuid) {this.uuid = uuid;}

    public String getActivityType() {return activityType;}

    public void setActivityType(String activityType) {this.activityType = activityType;}

    public String getJobComment() {return JobComment;}

    public void setJobComment(String jobComment) {JobComment = jobComment;}
}
