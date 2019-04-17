package com.horizon.syncservice.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * DestinationStatus provides information about the delivery status of an object for a certain destination.
 * <p>The status can be {@link DestinationStatus#PENDING PENDING}, {@link DestinationStatus#DELIVERING DELIVERING}, 
 *        {@link DestinationStatus#DELIVERED DELIVERED}, {@link DestinationStatus#CONSUMED CONSUMED},
 *        or {@link DestinationStatus#ERROR ERROR}.
 */
@JsonInclude(Include.NON_NULL)
public class DestinationStatus {
    private String destinationType;
    private String destinationID;
    private String status;
    private String message;

    /**
     * Indicates that the object was consumed by this destination.
     */
    public final static String CONSUMED = "consumed";

    /**
     * Indicates that the object was delivered to this destination.
     */
    public final static String DELIVERED = "delivered";

    /**
     * Indicates that the object is being delivered to this destination.
     */
    public final static String DELIVERING = "delivering";

    /**
     * Indicates that a feedback error message was received from this destination.
     */
    public final static String ERROR = "error";

    /**
     * Indicates that the object is pending delivery to this destination.
     */
    public final static String PENDING = "pending";

    /**
     * Get the type of this destination of the object.
     * @return The type of this destination of the object.
     */
    public String getDestinationType() {
        return destinationType;
    }

    /**
     * Get the ID of this destination of the object.
     * @return The ID of this destination of the object.
     */
    public String getDestinationID() {
        return destinationID;
    }

    /**
     * Get the status of the delivery of the object to the destination described here.
     * <p>The status can be {@link DestinationStatus#PENDING PENDING},
     *        {@link DestinationStatus#DELIVERING DELIVERING}, 
     *        {@link DestinationStatus#DELIVERED DELIVERED}, {@link DestinationStatus#CONSUMED CONSUMED},
     *        or {@link DestinationStatus#ERROR ERROR}.
     * @return The status of the delivery of the object.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Get the message related to the delivery of the object to this destination.
     * @return The message related to the delivery of the object to this destination.
     */
    public String getMessage() {
        return message;
    }
}