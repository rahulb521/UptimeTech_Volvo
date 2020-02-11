package com.teramatrix.vos.volvouptime.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Arun.Singh on 3/19/2018.
 * Model for Vehicle
 */

@Table(name = "Vehicles")
public class VehicleModel extends Model {

    @Column(name = "Registration")
    public String reg_no_value;

    @Column(name = "IsDown")
    public boolean isDown;

    @Column(name = "VehicleId")
    public String VehicleId;

    @Column(name = "NumberPlate")
    public String NumberPlate;

    @Column(name = "ModelNumber")
    public String ModelNumber;

    @Column(name = "InstallationDate")
    public String InstallationDate;

    @Column(name = "DoorNumber")
    public String DoorNumber;

    @Column(name = "ChassisNumber")
    public String ChassisNumber;

    @Column(name = "VehicleType")
    public String VehicleType;

    @Column(name = "VehicleStatus")
    public String VehicleStatus;

    @Column(name = "SiteId")
    public String SiteId;


    public VehicleModel()
    {
        super();
    }

    public String getVehicleId() {
        return VehicleId;
    }

    public void setVehicleId(String vehicleId) {
        VehicleId = vehicleId;
    }

    public String getNumberPlate() {
        return NumberPlate;
    }

    public void setNumberPlate(String numberPlate) {
        NumberPlate = numberPlate;
    }

    public String getModelNumber() {
        return ModelNumber;
    }

    public void setModelNumber(String modelNumber) {
        ModelNumber = modelNumber;
    }

    public String getInstallationDate() {
        return InstallationDate;
    }

    public void setInstallationDate(String installationDate) {
        InstallationDate = installationDate;
    }

    public String getDoorNumber() {
        return DoorNumber;
    }

    public void setDoorNumber(String doorNumber) {
        DoorNumber = doorNumber;
    }

    public String getChassisNumber() {
        return ChassisNumber;
    }

    public void setChassisNumber(String chassisNumber) {
        ChassisNumber = chassisNumber;
    }

    public String getVehicleType() {
        return VehicleType;
    }

    public void setVehicleType(String vehicleType) {
        VehicleType = vehicleType;
    }

    public String getVehicleStatus() {
        return VehicleStatus;
    }

    public void setVehicleStatus(String vehicleStatus) {
        VehicleStatus = vehicleStatus;
    }

    public String getSiteId() {
        return SiteId;
    }

    public void setSiteId(String siteId) {
        SiteId = siteId;
    }

    public String getReg_no_value() {
        return reg_no_value;
    }

    public void setReg_no_value(String reg_no_value) {
        this.reg_no_value = reg_no_value;
    }

    /**
     * vehicle status
     * 0 for vehicle in breakdown
     * 1 for not in breakdown
     * 2 for vehicle is inactive
     *
     * @return
     */
    public int isDown() {

        if(VehicleStatus==null || VehicleStatus.equalsIgnoreCase("") || VehicleStatus.equalsIgnoreCase("0"))
            return 0;
        else if (VehicleStatus.equalsIgnoreCase("1"))
            return 1;
        else return 2;
    }

    public void setDown(boolean down) {
        isDown = down;
    }


}
