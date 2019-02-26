package com.horizon.syncservice.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Destination {
    private String destinationOrgID;
    private String destinationType;
    private String destinationID;
    private String communication;

    public String getDestinationOrgID() {
        return destinationOrgID;
    }
    public void setDestinationOrgID(String destinationOrgID) {
        this.destinationOrgID = destinationOrgID;
    }

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

    public String getCommunication() {
        return communication;
    }
    public void setCommunication(String communication) {
        this.communication = communication;
    }
}
