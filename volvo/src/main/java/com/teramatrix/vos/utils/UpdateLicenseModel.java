package com.teramatrix.vos.utils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateLicenseModel {


        @SerializedName("application_licence_key")
        @Expose
        private String applicationLicenceKey;

        @SerializedName("application_version")
        @Expose
        private String application_version;


        @SerializedName("os_version")
        @Expose
        private String osVersion;
        @SerializedName("event_time")
        @Expose
        private String eventTime;
        @SerializedName("latitude")
        @Expose
        private String latitude;
        @SerializedName("longitude")
        @Expose
        private String longitude;
        @SerializedName("phone_model")
        @Expose
        private String phoneModel;
        @SerializedName("createdby")
        @Expose
        private String createdby;
        @SerializedName("created_date")
        @Expose
        private String createdDate;


        @SerializedName("device_alias")
        @Expose
        private String deviceAlias;

    @SerializedName("token")
    @Expose
    private String token;
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


        public String getApplicationLicenceKey() {
            return applicationLicenceKey;
        }

        public void setApplicationLicenceKey(String applicationLicenceKey) {
            this.applicationLicenceKey = applicationLicenceKey;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }

        public String getEventTime() {
            return eventTime;
        }

        public void setEventTime(String eventTime) {
            this.eventTime = eventTime;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getPhoneModel() {
            return phoneModel;
        }

        public void setPhoneModel(String phoneModel) {
            this.phoneModel = phoneModel;
        }

        public String getCreatedby() {
            return createdby;
        }

        public void setCreatedby(String createdby) {
            this.createdby = createdby;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(String createdDate) {
            this.createdDate = createdDate;
        }

        public String getApplication_version() {
            return getApplication_version();
        }

        public void setApplication_version(String application_version) {
            this.application_version = application_version;
        }



        public String getDeviceAlias() {
            return deviceAlias;
        }

        public void setDeviceAlias(String deviceAlias) {
            this.deviceAlias = deviceAlias;
        }

    }