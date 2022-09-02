
package com.teramatrix.vos.accept_reject_ticket.responsebodyticketdetail;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TicketResponseBody {

    @SerializedName("TicketOpenList")
    @Expose
    private List<TicketOpen> ticketOpenList = null;
    @SerializedName("TicketCloseList")
    @Expose
    private Object ticketCloseList;
    @SerializedName("DeviceAlias")
    @Expose
    private String deviceAlias;
    @SerializedName("DealerCode")
    @Expose
    private Object dealerCode;
    @SerializedName("Ieminumber")
    @Expose
    private Object ieminumber;
    @SerializedName("DbSynLastTime")
    @Expose
    private String dbSynLastTime;
    @SerializedName("Token")
    @Expose
    private Object token;
    @SerializedName("Status")
    @Expose
    private String status;
    @SerializedName("Message")
    @Expose
    private Object message;
    @SerializedName("KamUserId")
    @Expose
    private Object kamUserId;

    public List<TicketOpen> getTicketOpenList() {
        return ticketOpenList;
    }

    public void setTicketOpenList(List<TicketOpen> ticketOpenList) {
        this.ticketOpenList = ticketOpenList;
    }

    public Object getTicketCloseList() {
        return ticketCloseList;
    }

    public void setTicketCloseList(Object ticketCloseList) {
        this.ticketCloseList = ticketCloseList;
    }

    public String getDeviceAlias() {
        return deviceAlias;
    }

    public void setDeviceAlias(String deviceAlias) {
        this.deviceAlias = deviceAlias;
    }

    public Object getDealerCode() {
        return dealerCode;
    }

    public void setDealerCode(Object dealerCode) {
        this.dealerCode = dealerCode;
    }

    public Object getIeminumber() {
        return ieminumber;
    }

    public void setIeminumber(Object ieminumber) {
        this.ieminumber = ieminumber;
    }

    public String getDbSynLastTime() {
        return dbSynLastTime;
    }

    public void setDbSynLastTime(String dbSynLastTime) {
        this.dbSynLastTime = dbSynLastTime;
    }

    public Object getToken() {
        return token;
    }

    public void setToken(Object token) {
        this.token = token;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Object getKamUserId() {
        return kamUserId;
    }

    public void setKamUserId(Object kamUserId) {
        this.kamUserId = kamUserId;
    }

}
