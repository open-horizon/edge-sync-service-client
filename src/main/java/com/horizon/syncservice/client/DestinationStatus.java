package com.horizon.syncservice.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class DestinationStatus {
    private String destinationType;
    private String destinationID;
    private String status;
    private String message;

    public final static String STATUS_CONSUMED = "consumed";
    public final static String STATUS_DELIVERED = "delivered";
    public final static String STATUS_DELIVERING = "delivering";
    public final static String STATUS_ERROR = "error";
    public final static String STATUS_PENDING = "pending";

    public String getDestinationType() {
        return destinationType;
    }
    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public String getDestinationID() {
        return destinationID;
    }
    public void setDestinationID(String destinationID) {
        this.destinationID = destinationID;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}