package com.horizon.syncservice.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * ObjectStatus provides information about an object that is destined for a particular destination.
 * <p>The status can be {@link ObjectStatus#PENDING PENDING}, {@link ObjectStatus#DELIVERING DELIVERING}, 
 *        {@link ObjectStatus#DELIVERED DELIVERED}, {@link ObjectStatus#CONSUMED CONSUMED},
 *        or {@link ObjectStatus#ERROR ERROR}.
 */
@JsonInclude(Include.NON_NULL)
public class ObjectStatus {
    private String orgID;
    private String objectType;
    private String objectID;
    private String status;

    /**
     * Indicates that the object described here was consumed by the destination in question.
     */
    public final static String CONSUMED = "consumed";

    /**
     * Indicates that the object described here was delivered to the destination in question.
     */
    public final static String DELIVERED = "delivered";

    /**
     * Indicates that the object described here is being delivered to the destination in question.
     */
    public final static String DELIVERING = "delivering";

    /**
     * Indicates that a feedback error message was received by the destination in question.
     */
    public final static String ERROR = "error";

    /**
     * Indicates that the object described here is pending delivery to the destination in question.
     */
    public final static String PENDING = "pending";

    /**
     * Get the orgnization ID of the object being delivered to the destination in question.
     * @return The orgnization ID of the object being delivered to the destination in question.
     */
    public String getOrgID() {
        return orgID;
    }

    /**
     * Set the orgnization ID of the object being delivered to the destination in question.
     * @param orgID The new value of the orgnization ID of the object being delivered.
     */
    public void setOrgID(String orgID) {
        this.orgID = orgID;
    }

    /**
     * Get the type of the object being delivered to the destination in question.
     * @return The type of the object being delivered to the destination in question.
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Set the type of the object being delivered to the destination in question.
     * @param objectType The new value of the type of the object being delivered.
     */
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    /**
     * Get the ID of the object being delivered to the destination in question.
     * @return The ID of the object being delivered to the destination in question.
     */
    public String getObjectID() {
        return objectID;
    }

    /**
     * Set the ID of the object being delivered to the destination in question.
     * @param objectID The new value of the ID of the object being delivered.
     */
    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    /**
     * Get the status of the delivery of the object described here to the destination in question.
     * <p>The status can be {@link ObjectStatus#PENDING PENDING}, {@link ObjectStatus#DELIVERING DELIVERING}, 
     *        {@link ObjectStatus#DELIVERED DELIVERED}, {@link ObjectStatus#CONSUMED CONSUMED},
     *        or {@link DestinationStatus#ERROR ERROR}.
     * @return The status of the delivery of the object.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set the status of the delivery of the object described here to the destination in question.
     * <p>The status can be {@link ObjectStatus#PENDING PENDING}, {@link ObjectStatus#DELIVERING DELIVERING}, 
     *        {@link ObjectStatus#DELIVERED DELIVERED}, {@link ObjectStatus#CONSUMED CONSUMED},
     *        or {@link ObjectStatus#ERROR ERROR}.
     * @param status The new value of the status of the delivery to this destination of the object.
     */
    public void setStatus(String status) {
        this.status = status;
    }
}