package com.horizon.syncservice.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ObjectStatus {
    private String orgID;
    private String objectType;
    private String objectID;
    private String status;

    public final static String STATUS_CONSUMED = "consumed";
    public final static String STATUS_DELIVERED = "delivered";
    public final static String STATUS_DELIVERING = "delivering";
    public final static String STATUS_ERROR = "error";
    public final static String STATUS_PENDING = "pending";

    public String getOrgID() {
        return orgID;
    }
    public void setOrgID(String orgID) {
        this.orgID = orgID;
    }

    public String getObjectType() {
        return objectType;
    }
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectID() {
        return objectID;
    }
    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}