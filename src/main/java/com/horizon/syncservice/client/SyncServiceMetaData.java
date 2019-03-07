package com.horizon.syncservice.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/***
 * This class represents a Sync Service metadata object: <br>
 * 
 * <pre>
 * {@code
 * {
    "objectID": "string", 
	"objectType": "string",						  
    "destinationID": "string",						  
    "destinationType": "string",
    "destinationsList": [ "string" ], 
    "expiration": "string",
	"version": "string",
    "description": "string",
	"link": "string",	
    "inactive": false,
	"activationTime": "string",
    "doNotSend": false,
    "noData": false,
	"metaOnly": false,
	"consumers": 0,
    "destinationDataUri": "string",
	"sourceDataUri": "string",
    "autodelete": bool,
	"deleted": false,
    "originID": "string",
    "originType": "string"
  }
  }
 * </pre>
 *
 */
@JsonInclude(Include.NON_NULL)
public class SyncServiceMetaData {

	private String activationTime;
	private boolean autodelete;
	private int consumers;
	private boolean deleted;
	private String description;
	private String destinationDataUri;
	private String destinationID;
	private String[] destinationsList;
	private String destinationType;
	private boolean doNotSend;
	private String expiration;
	private boolean inactive;
	private String link;
	private boolean metaOnly;
	private boolean noData;
	private String objectID;
	private String objectType;
	private String originID;
	private String originType;
	private String sourceDataUri;
	private String version;

	public SyncServiceMetaData() {
		// Null Contructor
	}

	/**
	 * Get the object's automatic activation time.
	 * <p>activationTime is a timestamp/date as to when this object should automatically be activated.
	 * <p>The timestamp should be provided in RFC3339 format.
	 * <p>Optional field, if omitted (and Inactive is true) the object is never automatically
     *              activated.
	 * @return The object's automatic activation time.
	 */
	public String getActivationTime() {
		return activationTime;
	}

	/**
	 * Set the object's automatic activation time.
	 * <p>activationTime is a timestamp/date as to when this object should automatically be activated.
	 * <p>The timestamp should be provided in RFC3339 format.
	 * <p>Optional field, if omitted (and Inactive is true) the object is never automatically
     *              activated.
	 * @param activationTime The activation time to set in the object's metadata.
	 */
	public void setActivationTime(String activationTime) {
		this.activationTime = activationTime;
	}

	/**
	 * Get the object's autodelete flag.
	 * <p>The autodelete flag indicates whether to delete the object after it is
     *             delivered to all its destinations from the destinations list.
	 * <p>Optional field, default is false (do not delete).
	 * <p>This field is used only when working with the CSS. Objects are always
     *              deleted after delivery on the ESS.
	 * @return The object's autodelete flag.
	 */
	public boolean isAutodelete() {
		return autodelete;
	}

	/**
	 * Set the object's autodelete flag.
	 * <p>The autodelete flag indicates whether to delete the object after it is
     *             delivered to all its destinations from the destinations list.
	 * <p>Optional field, default is false (do not delete).
	 * <p>This field is used only when working with the CSS. Objects are always
     *              deleted after delivery on the ESS.
	 * @param autodelete The new value of the autodelete flag.
	 */
	public void setAutodelete(boolean autodelete) {
		this.autodelete = autodelete;
	}

	/**
	 * Get the number of expected consumers of this object.
	 * <p>consumers is the number of applications that are expected to indicate
     *                that they have consumed the object.
	 * <p>Optional field, default is 1.
	 * <p>This field is used only when working with the CSS. The default value
     *                is always used on the ESS.
	 * @return The number of expected consumers of this object.
	 */
	public int getConsumers() {
		return consumers;
	}

	/**
	 * Set the number of expected consumers of this object.
	 * <p>consumers is the number of applications that are expected to indicate
     *                that they have consumed the object.
	 * <p>Optional field, default is 1.
	 * <p>This field is used only when working with the CSS. The default value
     *                is always used on the ESS.
	 * @param consumers The number of expected consumers of this object.
	 */
	public void setConsumers(int consumers) {
		this.consumers = consumers;
	}

	/**
	 * Get the object's deleted flag.
	 * <p>The deleted flag indicates to applications polling for updates that
     *           this object has been deleted.
	 * <p>Read only field, should not be set by users.
	 * @return The object's deleted flag.
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * Set the object's deleted flag.
	 * @param deleted The new value of the object's deleted flag.
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 * Get the object's description.
	 * <p>description is a textual description of the object.
	 * <p>Optional field, empty by default.
	 * @return The object's description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the object's description.
	 * <p>description is a textual description of the object.
	 * <p>Optional field, empty by default.
	 * @param description The description to be set in the object's metadata.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/** Get the destinationID.
	 * <p>destinationID is the ID of the destination. If omitted the object is sent
     *           to all ESSs with the same destination type.
	 * <p>This field is ignored when working with ESS (the destination is the CSS).
	 * @return The destinationID.        
	 */
	public String getDestinationID() {
		return destinationID;
	}

	/**
	 * Set the destinationID.
	 * <p>destinationID is the ID of the destination. If omitted the object is sent
     *           to all ESSs with the same destination type.
	 * <p>This field is ignored when working with ESS (the destination is the CSS).
	 * @param destinationID The destinationID to set in the object's metadata.
	 */
	public void setDestinationID(String destinationID) {
		this.destinationID = destinationID;
	}

	/**
	 * Get the destinationsList.
	 * <p>destinationsList is the list of destinations as type:id pairs to send the object to.
	 * <p>When a DestinationsList is provided destination type and destination ID must be omitted.
	 * <p>This field is ignored when working with ESS (the destination is always the CSS).
	 * @return The destinationsList.
	 */
	public String[] getDestinationsList() {
		return destinationsList;
	}

	/**
	 * Set the destinationsList.
	 * <p>destinationsList is the list of destinations as type:id pairs to send the object to.
	 * <p>When a DestinationsList is provided destination type and destination ID must be omitted.
	 * <p>This field is ignored when working with ESS (the destination is always the CSS).
	 * @param destinationsList The list of destinations to set in the object's metadata.
	 */
	public void setDestinationsList(String[] destinationsList) {
		this.destinationsList = destinationsList;
	}

	/** Get the destinationType.
	 * <p>destinationType is the type of destination to send the object to.
	 * <p>If omitted (and if destinations_list is omitted too) the object is broadcasted
     *            to all known destinations.
	 * <p>This field is ignored when working with ESS (the destination is always the CSS).
	 * @return The destinationType.
	 */
	public String getDestinationType() {
		return destinationType;
	}

	/**
	 * Set the destinationType.
	 * <p>destinationType is the type of destination to send the object to.
	 * <p>If omitted (and if destinations_list is omitted too) the object is broadcasted
     *            to all known destinations.
	 * <p>This field is ignored when working with ESS (the destination is always the CSS).
	 * @param destinationType The destinationType to set in the object's metadata.
	 */
	public void setDestinationType(String destinationType) {
		this.destinationType = destinationType;
	}

	/**
	 * Get the object's destinationDataURI.
	 * <p>destinationDataURI is a URI indicating where the receiver of the object should store it.
     * <p>Currently only file URIs are supported.
	 * <p>This field is available only when working with the CSS.
	 * <p>Optional field, if omitted the object is stored in the node's internal storage.
	 * @return The object's destinationDataURI.
	 */
	public String getDestinationDataUri() {
		return destinationDataUri;
	}

	/**
	 * Set the object's destinationDataURI.
	 * <p>destinationDataURI is a URI indicating where the receiver of the object should store it.
     * <p>Currently only file URIs are supported.
	 * <p>This field is available only when working with the CSS.
	 * <p>Optional field, if omitted the object is stored in the node's internal storage.
	 * @param destinationDataUri The new value of rthe object's destinationDataURI.
	 */
	public void setDestinationDataUri(String destinationDataUri) {
		this.destinationDataUri = destinationDataUri;
	}

	/**
	 * Get the object's doNotSend flag.
	 * <p>The donotSend flag indicates whether or not this object should not be sent
	 *          to any destinations.
	 * <p>Optional field, default is false (object is sent to destinations).
	 * @return The object's doNotSend flag.
	 */
	public boolean isDoNotSend() {
		return doNotSend;
	}

	/**
	 * Set the object's doNotSend flag.
	 * <p>The donotSend flag indicates whether or not this object should not be sent
	 *          to any destinations.
	 * <p>Optional field, default is false (object is sent to destinations).
	 * @param doNotSend The new value of the doNotSend flag.
	 */
	public void setDoNotSend(boolean doNotSend) {
		this.doNotSend = doNotSend;
	}

	/**
	 * Get the object's expiration time.
	 * <p>expiration is a timestamp/date indicating when the object expires.
	 * <p>When the object expires it is automatically deleted.
	 * <p>The timestamp should be provided in RFC3339 format.
	 * <p>Optional field, if omitted the object doesn't expire.
	 * @return The object's expiration time.
	 */
	public String getExpiration() {
		return expiration;
	}

	/**
	 * Set the object expiration time.
	 * <p>expiration is a timestamp/date indicating when the object expires.
	 * <p>When the object expires it is automatically deleted.
	 * <p>The timestamp should be provided in RFC3339 format.
	 * <p>Optional field, if omitted the object doesn't expire.
	 * @param expiration The expiration time o be set in the object's metadata.
	 */
	public void setExpiration(String expiration) {
		this.expiration = expiration;
	}

	/**
	 * Get the object's inactive flag.
	 * <p>The inactive flag indicates whether or not this object is inactive for now.
	 * <p>An object can be created as inactive which means it is not delivered to its
     *         destination. The object can be activated later.
	 * <p>Optional field, default is false (object active).
	 * @return The object's inactive flag.
	 */
	public boolean isInactive() {
		return inactive;
	}

	/**
	 * Set the object's inactive flag.
	 * <p>The inactive flag indicates whether or not this object is inactive for now.
	 * <p>An object can be created as inactive which means it is not delivered to its
     *         destination. The object can be activated later.
	 * <p>Optional field, default is false (object active).
	 * @param inactive If true, mark the object as inactive.
	 */
	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	/**
	 * Get the object's link.
	 * <p>link is a link to where the data for this object can be fetched from.
	 * <p>Optional field, if omitted the data must be provided by the application.
	 * @return The object's link.
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Set the object's link.
	 * <p>link is a link to where the data for this object can be fetched from.
	 * <p>Optional field, if omitted the data must be provided by the application.
	 * @param link The link to set in the object's metadata.
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * Get the object's metaOnly flag.
	 * <p>The metaOnly flag indicates whether or not this update is only of the
     *            metadata. The current object's data is left unchanged.
	 * <p>Optional field, default is false (both data and metadata are updated).
	 * @return The object's metaOnly flag.
	 */
	public boolean isMetaOnly() {
		return metaOnly;
	}

	/**
	 * Set the object's metaOnly flag.
	 * <p>The metaOnly flag indicates whether or not this update is only of the
     *            metadata. The current object's data is left unchanged.
	 * <p>Optional field, default is false (both data and metadata are updated).
	 * @param metaOnly The new value of the object's metaOnly flag.
	 */
	public void setMetaOnly(boolean metaOnly) {
		this.metaOnly = metaOnly;
	}

	/**
	 * Get the object's noData flag.
	 * <p>The noData flag indicates whether or not there is no data for this object.
	 * <p>Objects with no data can be used, for example, to send notifications.
	 * <p>Optional field, default is false (object includes data).
	 * @return The object's noData flag.
	 */
	public boolean isNoData() {
		return noData;
	}

	/**
	 * Set the object's noData flag.
	 * <p>The noData flag indicates whether or not there is no data for this object.
	 * <p>Objects with no data can be used, for example, to send notifications.
	 * <p>Optional field, default is false (object includes data).
	 * @param noData The new value of the object's noData flag.
	 */
	public void setNoData(boolean noData) {
		this.noData = noData;
	}

	/** Get the objectID of the object.
	 * <p>objectID is a unique identifier of the object. 
	 * <p>objectID and objectType must uniquely identify the object.
	 * <p>Must be provided by the application.
	 * @return The objectID of the object.
	 */
	public String getObjectID() {
		return objectID;
	}

	/** Set the objectID of the object.
	 * <p>objectID is a unique identifier of the object. 
	 * <p>objectID and objectType must uniquely identify the object.
	 * <p>Must be provided by the application.
	 * @param objectID The objectID to set in the Object's metadata.
	 */
	public void setObjectID(String objectID) {
		this.objectID = objectID;
	}

	/** Get the objectType of the object.
	 * <p>objectType is the type of the object.
	 * <p>The type is used to group multiple objects, for example when checking for
     *          object updates.
	 * <p>Must be provided by the application
	 * @return The objectType of the object.
	 */
	public String getObjectType() {
		return objectType;
	}

	/** Set the objectType of the object.
	 * <p>objectType is the type of the object.
	 * <p>The type is used to group multiple objects, for example when checking for
     *          object updates.
	 * <p>Must be provided by the application
	 * @param objectType The objectType to set in the Object's metadata.
	 */
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	/**
	 * Get the object's originID.
	 * <p>originID is the ID of origin of the object. Set by the internal code.
	 * <p>Read only field, should not be set by users.
	 * @return The object's originID.
	 */
	public String getOriginID() {
		return originID;
	}

    /**
	 * Get the object's originID.
	 * <p>For internal use only.
	 * @param originID The new value for the object's originID.
	 */
	public void setOriginID(String originID) {
		this.originID = originID;
	}

	/**
	 * Get the object's objectType.
	 * <p>originType is the type of origin of the object. Set by the internal code.
	 * <p>Read only field, should not be set by users.
	 * @return The object's objectType.
	 */
	public String getOriginType() {
		return originType;
	}

	/**
	 * Set the object's objectType.
	 * <p>For internal use only.
	 * @param originType The new value for the object's originType.
	 */
	public void setOriginType(String originType) {
		this.originType = originType;
	}

	/**
	 * Get the object's sourceDataURI.
	 * <p>sourceDataURI is a URI indicating where the sender of the object should read the data from.
	 * <p>Currently only file URIs are supported.
	 * <p>This field is available only when working with the ESS.
	 * <p>Optional field, if omitted the object's data should be provided by the user.
	 * @return The object's sourceDataURI.
	 */
	public String getSourceDataUri() {
		return sourceDataUri;
	}

	/**
	 * Set the object's sourceDataURI.
	 * <p>sourceDataURI is a URI indicating where the sender of the object should read the data from.
	 * <p>Currently only file URIs are supported.
	 * <p>This field is available only when working with the ESS.
	 * <p>Optional field, if omitted the object's data should be provided by the user.
	 * @param sourceDataUri The new value for the object's sourceDataURI.
	 */
	public void setSourceDataUri(String sourceDataUri) {
		this.sourceDataUri = sourceDataUri;
	}

	/**
	 * Get the Object's version.
	 * <p>version is the object's version (as used by the application).
	 * <p>Optional field, empty by default.
	 * @return The Object's version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Set the Object's version.
	 * <p>version is the object's version (as used by the application).
	 * <p>Optional field, empty by default.
	 * @param version the version to set in the object's metadata.
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {

		StringBuilder result = new StringBuilder();
		String newLine = System.getProperty("line.separator");

		result.append(this.getClass().getName() + " Object {" + newLine);
		result.append(" activationTime:" + getActivationTime());
		result.append(" consumers:" + getConsumers());
		result.append(" deleted:" + isDeleted());
		result.append(" description:" + getDescription());
		result.append(" destinationID:" + getDestinationID());
		result.append(" destinationType:" + getDestinationType());
		result.append(" destinationDataUri:" + getDestinationDataUri());
		result.append(" doNotSend:" + isDoNotSend());
		result.append(" expiration:" + getExpiration());
		result.append(" inactive:" + isInactive());
		result.append(" link:" + getLink());
		result.append(" metaOnly:" + isMetaOnly());
		result.append(" noData:" + isNoData());
		result.append(" objectID:" + getObjectID());
		result.append(" objectType:" + getObjectType());
		result.append(" originID:" + getOriginID());
		result.append(" originType:" + getOriginType());
		result.append(" sourceDataUri:" + getSourceDataUri());
		result.append(" version:" + getVersion());
		result.append("}");

		return result.toString();
	}

}
