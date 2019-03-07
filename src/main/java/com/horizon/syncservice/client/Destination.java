package com.horizon.syncservice.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Destination describes a sync service node.
 * <p>Each sync service edge node (ESS) has an address that is composed of the node's ID, Type, and Organization.
 * <p>An ESS node communicates with the CSS using either MQTT or HTTP.
 */
@JsonInclude(Include.NON_NULL)
public class Destination {
    private String destinationOrgID;
    private String destinationType;
    private String destinationID;
    private String communication;

    /**
     * Get the destination's organization ID.
     * <p>destinationOrgID is the destination's organization ID.
	 * <p>Each sync service object belongs to a single organization.
     * @return The destination's organization ID
     */
    public String getDestinationOrgID() {
        return destinationOrgID;
    }

    /**
     * Set the destination's organization ID.
     * <p>destinationOrgID is the destination's organization ID.
	 * <p>Each sync service object belongs to a single organization.
     * @param destinationOrgID The new value of the destination's destinationOrgID.
     */
    public void setDestinationOrgID(String destinationOrgID) {
        this.destinationOrgID = destinationOrgID;
    }

    /**
     * Get the destinations's destinationType.
     * <p>destinationType is the destination's type.
     * @return The destinations's destinationType.
     */
    public String getDestinationType() {
        return destinationType;
    }

    /**
     * Set the destinations's destinationType.
     * <p>destinationType is the destination's type.
     * @param destinationType The new value of the destination's destinationType.
     */
    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    /**
     * Get the destination's destination ID.
     * <p>destinationID is the destination's ID.
     * @return The destination's destination ID.
     */
    public String getDestinationID() {
        return destinationID;
    }

    /**
     * Set the destination's destination ID.
     * <p>destinationID is the destination's ID.
     * @param destinationID The new value of the destination's destinationID.
     */
    public void setDestinationID(String destinationID) {
        this.destinationID = destinationID;
    }

    /**
     * Get the destination's form of communication with the CSS.
     * <p>communication is the communications method used by the destination to connect (can be MQTT or HTTP)
     * @return The destination's form of communication with the CSS (mqtt or mqtt).
     */
    public String getCommunication() {
        return communication;
    }

    /**
     * Set the destination's form of communication with the CSS.
     * <p>communication is the communications method used by the destination to connect (can be MQTT or HTTP)
     * @param communication The new value of the destination's form of communication with the CSS (mqtt or mqtt).
     */
    public void setCommunication(String communication) {
        this.communication = communication;
    }
}
