package com.teramatrix.vos.model;

/**
 * @author neeraj on 23/4/19.
 */
public class TokenRefreshModel {


    /**
     * Token : null
     * DeviceAlias : null
     * Massage : Token failed to update.
     * UserType : null
     * SiteId : null
     * Status : 0
     */

    private Object Token;
    private Object DeviceAlias;
    private String Massage;
    private Object UserType;
    private Object SiteId;
    private String Status;

    public Object getToken() {
        return Token;
    }

    public void setToken(Object Token) {
        this.Token = Token;
    }

    public Object getDeviceAlias() {
        return DeviceAlias;
    }

    public void setDeviceAlias(Object DeviceAlias) {
        this.DeviceAlias = DeviceAlias;
    }

    public String getMassage() {
        return Massage;
    }

    public void setMassage(String Massage) {
        this.Massage = Massage;
    }

    public Object getUserType() {
        return UserType;
    }

    public void setUserType(Object UserType) {
        this.UserType = UserType;
    }

    public Object getSiteId() {
        return SiteId;
    }

    public void setSiteId(Object SiteId) {
        this.SiteId = SiteId;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }
}
