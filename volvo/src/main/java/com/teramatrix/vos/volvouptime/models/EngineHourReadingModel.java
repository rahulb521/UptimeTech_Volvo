package com.teramatrix.vos.volvouptime.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * @author neeraj on 27/12/18.
 */
@Table(name = "EngineHourReading")
public class EngineHourReadingModel extends Model {

    public String getRegistrationNumber() {
        return RegistrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        RegistrationNumber = registrationNumber;
    }

    public String getChassisNumber() {
        return ChassisNumber;
    }

    public void setChassisNumber(String chassisNumber) {
        ChassisNumber = chassisNumber;
    }

    public String getDoorNumber() {
        return DoorNumber;
    }

    public void setDoorNumber(String doorNumber) {
        DoorNumber = doorNumber;
    }

    public String getCurrentMonthUtilizationData() {
        return CurrentMonthUtilizationData;
    }

    public void setCurrentMonthUtilizationData(String currentMonthUtilizationData) {
        CurrentMonthUtilizationData = currentMonthUtilizationData;
    }

    public String getPreviousMonthUtilizationData() {
        return PreviousMonthUtilizationData;
    }

    public void setPreviousMonthUtilizationData(String previousMonthUtilizationData) {
        PreviousMonthUtilizationData = previousMonthUtilizationData;
    }
    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }

    @Column(name = "RegistrationNumber")
    private String RegistrationNumber;
    @Column(name = "ChasisNumber")
    private String ChassisNumber;
    @Column(name = "DoorNumber")
    private String DoorNumber;
    @Column(name = "CurrentMonthUtilizationData")
    private String CurrentMonthUtilizationData;
    @Column(name = "PreviousMonthUtilizationData")
    private String PreviousMonthUtilizationData;
    @Column(name = "isModified")
    private boolean isModified;
   /* @Column(name = "GetUtilizationDatafirst")
    private String GetUtilizationDatafirst;

    public String getGetUtilizationDatafirst() {
        return GetUtilizationDatafirst;
    }

    public void setGetUtilizationDatafirst(String getUtilizationDatafirst) {
        GetUtilizationDatafirst = getUtilizationDatafirst;
    }
*/
    public String getInRequiredTime() {
        return inRequiredTime;
    }

    public void setInRequiredTime(String inRequiredTime) {
        this.inRequiredTime = inRequiredTime;
    }

    @Column(name = "RequiredTime")
    private String inRequiredTime;


}
