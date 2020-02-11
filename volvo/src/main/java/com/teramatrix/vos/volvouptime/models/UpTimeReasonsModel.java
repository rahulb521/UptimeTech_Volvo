package com.teramatrix.vos.volvouptime.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by arun.singh on 3/26/2018.
 * Model for General Reasons
 */

@Table(name = "DelayedReasons")
public class UpTimeReasonsModel extends Model {

    @Column(name = "TicketId")
    public String _ticket_id;
    @Column(name = "ServiceTypeId")
    public String _service_type_id;
    @Column(name = "ReasonId")
    public String _reason_id;
    @Column(name = "ServiceName")
    public String _service_name;
    @Column(name = "ServiceAlias")
    public String _service_alias;
    @Column(name = "ServiceTypeSequenceNumber")
    public String _service_type_sequence_number;
    @Column(name = "ReasonName")
    public String _reason_name;
    @Column(name = "ReasonAlias")
    public String _reason_alias;
    @Column(name = "ReasonSequenceNumber")
    public String _reason_sequence_number;
    @Column(name = "ServiceTypeIsActive")
    public String _service_type_is_active;
    @Column(name = "ServiceTypeIsDeleted")
    public String _service_type_is_deleted;
    @Column(name = "IsActive")
    public String _is_active;
    @Column(name = "IsDeleted")
    public String _is_deleted;
    @Column(name = "ReasonStartDate")
    public String reasonStartDate;
    @Column(name = "ReasonEndDate")
    public String reasonEndDate;
    @Column(name = "isSyncWithDB")
    public boolean isSyncWithDB;


    public UpTimeReasonsModel()
    {
        super();
    }

    public String getReasonStartDate() {
        return reasonStartDate;
    }

    public void setReasonStartDate(String reasonStartDate) {this.reasonStartDate = reasonStartDate;}

    public String getReasonEndDate() {
        return reasonEndDate;
    }

    public void setReasonEndDate(String reasonEndDate) {
        this.reasonEndDate = reasonEndDate;
    }

    public String get_service_type_id() {
        return _service_type_id;
    }

    public void set_service_type_id(String _service_type_id) {this._service_type_id = _service_type_id;}

    public String get_reason_id() {
        return _reason_id;
    }

    public void set_reason_id(String _reason_id) {
        this._reason_id = _reason_id;
    }

    public String get_service_name() {
        return _service_name;
    }

    public void set_service_name(String _service_name) {
        this._service_name = _service_name;
    }

    public String get_service_alias() {
        return _service_alias;
    }

    public void set_service_alias(String _service_alias) {
        this._service_alias = _service_alias;
    }

    public String get_service_type_sequence_number() {
        return _service_type_sequence_number;
    }

    public void set_service_type_sequence_number(String _service_type_sequence_number) {this._service_type_sequence_number = _service_type_sequence_number;}

    public String get_reason_name() {
        return _reason_name;
    }

    public void set_reason_name(String _reason_name) {
        this._reason_name = _reason_name;
    }

    public String get_reason_alias() {
        return _reason_alias;
    }

    public void set_reason_alias(String _reason_alias) {
        this._reason_alias = _reason_alias;
    }

    public String get_reason_sequence_number() {
        return _reason_sequence_number;
    }

    public void set_reason_sequence_number(String _reason_sequence_number) {this._reason_sequence_number = _reason_sequence_number;}

    public String get_service_type_is_active() {
        return _service_type_is_active;
    }

    public void set_service_type_is_active(String _service_type_is_active) {this._service_type_is_active = _service_type_is_active;}

    public String get_service_type_is_deleted() {
        return _service_type_is_deleted;
    }

    public void set_service_type_is_deleted(String _service_type_is_deleted) {this._service_type_is_deleted = _service_type_is_deleted;}

    public String get_is_active() {
        return _is_active;
    }

    public void set_is_active(String _is_active) {
        this._is_active = _is_active;
    }

    public String get_is_deleted() {
        return _is_deleted;
    }

    public void set_is_deleted(String _is_deleted) {
        this._is_deleted = _is_deleted;
    }

    public String get_ticket_id() {
        return _ticket_id;
    }

    public void set_ticket_id(String _ticket_id) {
        this._ticket_id = _ticket_id;
    }

    public boolean isSyncWithDB() {return isSyncWithDB;}

    public void setSyncWithDB(boolean syncWithDB) {isSyncWithDB = syncWithDB;}

}
