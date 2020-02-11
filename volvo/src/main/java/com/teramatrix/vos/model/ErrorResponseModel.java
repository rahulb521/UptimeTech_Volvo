package com.teramatrix.vos.model;

/**
 * @author neeraj on 2/4/19.
 */
public class ErrorResponseModel {
    private String Message;
    private String Status;

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
