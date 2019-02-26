package com.horizon.syncservice.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/***
 * This class represents a Sync Service metadata object: </br>
 * 
 * <pre>
 * {@code
 * \{
	"activationTime": "string",
	"autodelete": bool,
    "consumers": 0,
    "deleted": false,
	"description": "string",
	"destinationDataUri": "string",
	"destinationID": "string",
	"destinationsList": [ "string" ],
    "destinationOrgID": "string",
    "destinationType": "string",
    "doNotSend": false,
    "expiration": "string",
    "inactive": false,
    "link": "string",
    "metaOnly": false,
    "noData": false,
    "objectID": "string",
    "objectType": "string",
    "originID": "string",
	"originType": "string",
	"sourceDataUri": "string",
    "version": "string"
  \}
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
	private String destinationOrgID;
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

	public String getActivationTime() {
		return activationTime;
	}

	public void setActivationTime(String activationTime) {
		this.activationTime = activationTime;
	}

	public boolean isAutodelete() {
		return autodelete;
	}

	public void setAutodelete(boolean autodelete) {
		this.autodelete = autodelete;
	}

	public int getConsumers() {
		return consumers;
	}

	public void setConsumers(int consumers) {
		this.consumers = consumers;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDestinationID() {
		return destinationID;
	}

	public void setDestinationID(String destinationID) {
		this.destinationID = destinationID;
	}

	public String[] getDestinationsList() {
		return destinationsList;
	}

	public void setDestinationsList(String[] destinationsList) {
		this.destinationsList = destinationsList;
	}

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

	public String getDestinationDataUri() {
		return destinationDataUri;
	}

	public void setDestinationDataUri(String destinationDataUri) {
		this.destinationDataUri = destinationDataUri;
	}

	public boolean getDoNotSend() {
		return doNotSend;
	}

	public void setDoNotSend(boolean doNotSend) {
		this.doNotSend = doNotSend;
	}

	public String getExpiration() {
		return expiration;
	}

	public void setExpiration(String expiration) {
		this.expiration = expiration;
	}

	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public boolean isMetaOnly() {
		return metaOnly;
	}

	public void setMetaOnly(boolean metaOnly) {
		this.metaOnly = metaOnly;
	}

	public boolean isNoData() {
		return noData;
	}

	public void setNoData(boolean noData) {
		this.noData = noData;
	}

	public String getObjectID() {
		return objectID;
	}

	public void setObjectID(String objectID) {
		this.objectID = objectID;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getOriginID() {
		return originID;
	}

	public void setOriginID(String originID) {
		this.originID = originID;
	}

	public String getOriginType() {
		return originType;
	}

	public void setOriginType(String originType) {
		this.originType = originType;
	}

	public String getSourceDataUri() {
		return sourceDataUri;
	}

	public void setSourceDataUri(String sourceDataUri) {
		this.sourceDataUri = sourceDataUri;
	}

	public String getVersion() {
		return version;
	}

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
		result.append(" destinationOrgID:" + getDestinationOrgID());
		result.append(" destinationType:" + getDestinationType());
		result.append(" destinationDataUri:" + getDestinationDataUri());
		result.append(" doNotSend:" + getDoNotSend());
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
