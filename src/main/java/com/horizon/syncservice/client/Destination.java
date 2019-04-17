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
    private String codeVersion;

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
     * Get the destinations's destinationType.
     * <p>destinationType is the destination's type.
     * @return The destinations's destinationType.
     */
    public String getDestinationType() {
        return destinationType;
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
     * Get the destination's form of communication with the CSS.
     * <p>communication is the communications method used by the destination to connect (can be MQTT or HTTP)
     * @return The destination's form of communication with the CSS (mqtt or mqtt).
     */
    public String getCommunication() {
        return communication;
    }

    /**
     * Get the sync service code version used by the destination.
     * @return The sync service code version used by the destination.
     */
    public String getCodeVersion() {
        return codeVersion;
    }
}
