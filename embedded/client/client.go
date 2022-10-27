package client

import (
	"flag"
	"fmt"
	"io"
	"os"
	"strconv"
	"sync"
	"time"

	"github.com/open-horizon/edge-sync-service/core/security"

	"github.com/open-horizon/edge-sync-service/common"
	"github.com/open-horizon/edge-sync-service/core/base"
	"github.com/open-horizon/edge-sync-service/core/communications"
	"github.com/open-horizon/edge-utilities/logger"
	"github.com/open-horizon/edge-utilities/logger/log"
)

// SyncServiceClient is a handle for an embedded client of the sync-service
type SyncServiceClient struct {
	orgID             string
	ticker            *time.Ticker
	updatesPollerStop chan bool
	inflightUpdates   map[string]bool
	pollerCount       int
	stoppingChannel   chan int
}

type syncServiceError struct {
	message string
}

func (e *syncServiceError) Error() string {
	return e.message
}

// ObjectMetaData is the metadata that identifies and defines the Sync Service object.
// Every object includes metadata (mandatory) and data (optional). The metadata and data can be updated independently.
// Each Sync Service node (ESS) has an address that is composed of the node's ID, Type, and Organization.
// To send an object to a single node set the DestType and DestID fields to match the node's Type and ID.
// To send an object to all the nodes of a certain type set DestType to the appropriate type and leave DestID empty.
// If both DestType and DestID are empty the object is sent to all nodes.
// swagger:model
type ObjectMetaData = common.MetaData

// Destination describes a Sync Service node.
// Each Sync Service edge node (ESS) has an address that is composed of the node's ID, Type, and Organization.
// An ESS node communicates with the CSS using either MQTT or HTTP.
type Destination = common.Destination

// DestinationStatus provides information about the delivery status of an object for a certain destination.
// The status can be one of the following:
//
//	pending - indicates that the object is pending delivery to this destination
//	delivering - indicates that the object is being delivered to this destination
//	delivered - indicates that the object was delivered to this destination
//	consumed - indicates that the object was consumed by this destination
//	deleted - indicates that this destination acknowledged the deletion of the object
//	error - indicates that a feedback error message was received from this destination
type DestinationStatus = common.DestinationsStatus

// ObjectStatus provides information about an object that is destined for a particular destination
type ObjectStatus = common.ObjectStatus

const (
	// DestStatusPending indicates that the object is pending delivery to this destination
	DestStatusPending = "pending"

	// DestStatusDelivering indicates that the object is being delivered to this destination
	DestStatusDelivering = "delivering"

	// DestStatusDelivered indicates that the object was delivered to this destination
	DestStatusDelivered = "delivered"

	// DestStatusConsumed indicates that the object was consumed at this destination
	DestStatusConsumed = "consumed"

	// DestStatusDeleted indicates that this destination acknowledged the deletion of the object
	DestStatusDeleted = "deleted"

	// DestStatusError indicates that there was an error in delivering the object to this destination
	DestStatusError = "error"

	// ObjectStatusNotReady indicates that the object is not ready to be sent to destinations.
	ObjectStatusNotReady = "notReady"

	// ObjectStatusReady indicates that the object is ready to be sent to destinations.
	ObjectStatusReady = "ready"

	// ObjectStatusPartiallyReceived indicates that the object's metadata has been received but not all its data.
	ObjectStatusPartiallyReceived = "partiallyreceived"

	// ObjectStatusCompletelyReceived indicates that the full object (metadata and data) has been received.
	ObjectStatusCompletelyReceived = "completelyReceived"

	// ObjectStatusConsumed indicates that the object has been consumed by the application.
	ObjectStatusConsumed = "objconsumed"

	// ObjectStatusDeleted indicates that the object was deleted.
	ObjectStatusDeleted = "objdeleted"

	// ObjectStatusReceived indicates that the object was received by the application.
	ObjectStatusReceived = "objreceived"

	// ObjectStatusConsumedByDest indicates that the object was consumed by the other side.
	ObjectStatusConsumedByDest = "consumedByDest"
)

const (
	destinationACL = "destinations"
	objectACL      = "objects"
)

var syncServiceStarted bool
var syncServiceConfigSet bool
var syncServiceServingAPIs bool
var syncServiceAuthenticator security.Authentication
var startupLock sync.Mutex

// NewSyncServiceClient creates a new embedded Sync Service client instance.
// The three parameters exist for compatability with the non-embedded Sync Service client API,
// the values are ignored.
// The first call to NewSyncServiceClient will cause the embedded Sync Service to be started.
// The function returns a handle to the new client instance.
func NewSyncServiceClient(serviceProtocol string, serviceAddress string, servicePort uint16) *SyncServiceClient {
	client := SyncServiceClient{}
	client.orgID = ""
	client.updatesPollerStop = make(chan bool)
	client.stoppingChannel = make(chan int, 1)

	startupLock.Lock()
	if !syncServiceStarted {
		startSyncService(client.stoppingChannel)
	}
	startupLock.Unlock()

	if common.Configuration.NodeType == common.ESS {
		client.orgID = common.Configuration.OrgID
	}

	return &client
}

// GetDefaultConfig retrieves the default configuration for the Sync Service
func GetDefaultConfig() common.Config {
	var config = common.Config{}

	common.SetDefaultConfig(&config)

	return config
}

// SetConfig sets the Sync Service configuration programatically
//
// Note: This must be invoked before you create a SyncService client handle.
func SetConfig(config common.Config) {
	startupLock.Lock()
	if !syncServiceStarted {
		common.Configuration = config
		syncServiceConfigSet = true
	}
	startupLock.Unlock()
}

// EnableSyncServiceAPIs enables the serving of the RESTful Sync Service APIs
//
//	when running embedded in an app.
//
// Note: By default the Sync Service RESTful APIs are NOT served when running embedded in an app.
//
// Note: This must be invoked before you create a SyncService client handle.
func EnableSyncServiceAPIs(enable bool) {
	syncServiceServingAPIs = enable
}

// SetAuthenticator sets the authentication implementation that should be used by the Sync Service
//
// Note: This must be invoked before you create a SyncService client handle.
func SetAuthenticator(auth security.Authentication) {
	syncServiceAuthenticator = auth
}

// Stop stops the embedded Sync Service
func (syncClient *SyncServiceClient) Stop(quiesceTime int, unregisterSelf bool) {
	startupLock.Lock()

	if syncServiceStarted {
		base.Stop(quiesceTime, unregisterSelf)

		<-syncClient.stoppingChannel

		syncServiceStarted = false
		syncServiceConfigSet = false
		syncServiceServingAPIs = false
		syncServiceAuthenticator = nil
	}
	startupLock.Unlock()
}

// SetOrgID sets the orgID used on client connections to the Cloud Sync Service (CSS)
// There is no need to set orgID when connecting to an Edge Sync Service (ESS)
// Each client instance can be associated with a single org.
// Applications that need to support multiple organizations should create a different client instance for each org.
func (syncClient *SyncServiceClient) SetOrgID(orgID string) {
	syncClient.orgID = orgID
}

// SetAppKeyAndSecret sets the app key and app secret to be used when communicating
// with Sync Service
func (syncClient *SyncServiceClient) SetAppKeyAndSecret(key, secret string) {
}

// SetCACertificate sets the CA certificate used on client secured connections if needed
// Returns nil on success or an error if any is encountered.
func (syncClient *SyncServiceClient) SetCACertificate(certPem string) error {
	return nil
}

// StartPollingForUpdates starts the polling of the Sync Service for updates.
// Each invocation creates a Go routine that periodically polls the Sync Service for new update for a specific objectType.
// objectType specifies the type of objects the client should retrieve updates for.
// rate is the period, in seconds, between poll requests.
// updatesChannel is a channel on which the application receives the updates (as ObjectMetaData).
// Note that the updates return only the object's metadata. The object's data (if exists) can be obtained by calling FetchObjectData.
func (syncClient *SyncServiceClient) StartPollingForUpdates(objectType string, rate int, updatesChannel chan *ObjectMetaData) {
	syncClient.pollerCount++
	go syncClient.updatesPoller(objectType, rate, updatesChannel)
}

func (syncClient *SyncServiceClient) updatesPoller(objectType string, rate int, updatesChannel chan *ObjectMetaData) {
	syncClient.inflightUpdates = make(map[string]bool)
	syncClient.ticker = time.NewTicker(time.Duration(rate) * time.Second)
	firstPoll := true

	for {
		select {
		case <-syncClient.updatesPollerStop:
			return

		case <-syncClient.ticker.C:
			ok := syncClient.poll(firstPoll, objectType, updatesChannel)
			if ok {
				firstPoll = false
			}
		}
	}
}

// StopPollingForUpdates stops the polling of the Sync Service for updates.
func (syncClient *SyncServiceClient) StopPollingForUpdates() {
	syncClient.ticker.Stop()
	syncClient.updatesPollerStop <- true
}

func (syncClient *SyncServiceClient) poll(firstPoll bool, objectType string,
	updatesChannel chan *ObjectMetaData) bool {

	var objects []ObjectMetaData
	var err error

	if objects, err = base.ListUpdatedObjects(syncClient.orgID, objectType, firstPoll); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to poll the embedded sync-service. Error: %s\n", err)
		}
		return false
	}

	// Save the old map for the check below
	previousInflightUpdates := syncClient.inflightUpdates

	// Clean up inflight objects, remove all that aren't in the current poll, by rebuilding it.
	syncClient.inflightUpdates = make(map[string]bool)

	if objects != nil {
		for _, object := range objects {
			inflightKey := fmt.Sprintf("%s:%s:%d:%s", object.ObjectType, object.ObjectID, object.InstanceID, strconv.FormatBool(object.Deleted))
			if _, ok := previousInflightUpdates[inflightKey]; !ok {
				object := object // Make a local copy
				updatesChannel <- &object
			}
			syncClient.inflightUpdates[inflightKey] = true
		}
	}
	return true
}

// GetDestinations returns the list of registered edge nodes under an organization in the CSS.
// Returns a tuple of an array of Destination structs and an error. The error will be non-nil if an error encountered.
func (syncClient *SyncServiceClient) GetDestinations() ([]Destination, error) {
	var result []Destination
	var err error

	if result, err = base.ListDestinations(syncClient.orgID); err != nil {

		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to get the destinations for %s. Error: %s\n", syncClient.orgID, err)
		}
		return nil, &syncServiceError{fmt.Sprintf("Failed to fetch the list of destinations. Error: %s", err)}
	}

	return result, nil
}

// GetDestinationObjects returns the list of objects targeted at the specified destination
func (syncClient *SyncServiceClient) GetDestinationObjects(destType, destID string) ([]ObjectStatus, error) {
	var result []ObjectStatus
	var err error

	if result, err = base.GetObjectsForDestination(syncClient.orgID, destType, destID); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to get the objects target at %s/%s/%s. Error: %s\n", syncClient.orgID, destType, destID, err)
		}
		return nil, &syncServiceError{fmt.Sprintf("Failed to fetch the objects. Error: %s", err)}
	}

	return result, nil
}

// GetObjectMetadata returns the metadata of an object.
// Returns an ObjectMetaData struct and an error. The error will be non-nil if an error encountered.
func (syncClient *SyncServiceClient) GetObjectMetadata(objectType string, objectID string) (*ObjectMetaData, error) {
	var result *ObjectMetaData
	var err error

	if result, err = base.GetObject(syncClient.orgID, objectType, objectID); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to get the metadata for the object %s:%s. Error: %s\n", objectType, objectID, err)
		}
		return nil, &syncServiceError{err.Error()}
	}

	return result, nil
}

// GetObjectStatus returns the status of an object
// Returns a string and an error. The error will be non-nil if an error encountered.
// The string will have one of the following values:
//
//	notReady - The object is not ready to be sent to the destination.
//	ready - The object is ready to be sent but was not yet received by the destination.
//	received - The destination received the object's metadata but not all its data.
//	completelyReceived - The destination received the full object (metadata and data).
//	consumed - The object was consumed by the application running on the destination.
//	deleted - The object was deleted by the destination.
//
// Note: An empty string indicates that the object is not on the server
func (syncClient *SyncServiceClient) GetObjectStatus(objectType string, objectID string) (string, error) {
	var status string
	var err error
	if status, err = base.GetObjectStatus(syncClient.orgID, objectType, objectID); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to get the status for the object %s:%s. Error: %s\n", objectType, objectID, err)
		}
		return "", &syncServiceError{err.Error()}
	}

	return status, nil
}

// GetObjectDestinations returns the list of destinations that an object is being sent to, along with the
// status of each "transmission"
// Returns a tuple of an array of DestinationStatus structs and an error. The error will be non-nil if an error encountered.
func (syncClient *SyncServiceClient) GetObjectDestinations(objectType string, objectID string) ([]DestinationStatus, error) {
	var result []DestinationStatus
	var err error

	if result, err = base.GetObjectDestinationsStatus(syncClient.orgID, objectType, objectID); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to get the destinations for the object %s:%s. Error: %s\n", objectType, objectID, err)
		}
		return nil, err
	}

	return result, nil
}

// FetchObjectData fetches the data for an object given its metadata.
// object is the object's metadata that was obtained from the updatesChannel.
// write is an I/O writer to which to write the object's data.
// Returns true if the data was successfully written or false if any error was encountered.
func (syncClient *SyncServiceClient) FetchObjectData(object *ObjectMetaData, writer io.Writer) bool {
	var dataReader io.Reader
	var err error

	if dataReader, err = base.GetObjectData(syncClient.orgID, object.ObjectType, object.ObjectID); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to get the data for the object %s:%s. Error: %s\n", object.ObjectType, object.ObjectID, err)
		}
		return false
	}

	if dataReader == nil {
		return false
	}

	_, err = io.Copy(writer, dataReader)
	if err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to get the data for the object %s:%s. Error: %s\n", object.ObjectType, object.ObjectID, err)
		}
		return false
	}

	if err := communications.Store.CloseDataReader(dataReader); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to close the reader for the object %s:%s. Error: %s\n", object.ObjectType, object.ObjectID, err)
		}
	}

	return true
}

// ActivateObject tells the Sync Service to mark an object as active.
// object is the metadata of the object that should be activated.
// Only objects that were created as inactive need to be activated, see ObjectMetaData.Inactive.
// Returns nil on success or an error if any is encountered.
func (syncClient *SyncServiceClient) ActivateObject(object *ObjectMetaData) error {
	if err := base.ActivateObject(syncClient.orgID, object.ObjectType, object.ObjectID); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to activate the object %s:%s. Error: %s\n", object.ObjectType, object.ObjectID, err)
		}
		return &syncServiceError{fmt.Sprintf("Failed to mark the object as active. Error: %s", err)}
	}
	return nil
}

// MarkObjectConsumed tells the Sync Service to mark an object consumed.
// object is the metadata of the object that should marked consumed.
// After an object is marked as consumed it will not be delivered to the application again (even if the app or the Sync Service are restarted).
// Returns nil on success or an error if any is encountered.
func (syncClient *SyncServiceClient) MarkObjectConsumed(object *ObjectMetaData) error {
	if err := base.ObjectConsumed(syncClient.orgID, object.ObjectType, object.ObjectID); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to mark the data for the object %s:%s as consumed. Error: %s\n", object.ObjectType, object.ObjectID, err)
		}
		return &syncServiceError{fmt.Sprintf("Failed to mark the object as consumed. Error: %s", err)}
	}
	return nil
}

// MarkObjectDeleted tells the ESS to mark an object that was deleted on the CSS as having been deleted on the ESS.
// object is the metadata of the object to be marked as deleted.
// Returns nil on success or an error if any is encountered.
func (syncClient *SyncServiceClient) MarkObjectDeleted(serviceID string, object *ObjectMetaData) error {
	if err := base.ObjectDeleted(serviceID, syncClient.orgID, object.ObjectType, object.ObjectID); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to mark the object %s:%s as deleted. Error: %s\n", object.ObjectType, object.ObjectID, err)
		}
		return &syncServiceError{fmt.Sprintf("Failed to confirm object's deletion. Error: %s", err)}
	}
	return nil
}

// MarkObjectReceived tells the Sync Service to mark an object received.
// object is the metadata of the object that should be marked received.
// After an object is marked as received it will not be delivered to the application again, unless the app restarts polling for updates.
// Returns nil on success or an error if any is encountered.
func (syncClient *SyncServiceClient) MarkObjectReceived(object *ObjectMetaData) error {
	if err := base.ObjectReceived(syncClient.orgID, object.ObjectType, object.ObjectID); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to mark the data for the object %s:%s as received. Error: %s\n", object.ObjectType, object.ObjectID, err)
		}
		return &syncServiceError{fmt.Sprintf("Failed to mark the object as received. Error: %s", err)}
	}
	return nil
}

// RegisterWebhook registers a webhook to receive updates from the Sync Service.
// objectType specifies the type of objects the client should retrieve updates for.
// url is the URL that should be called by the Sync Service when a new update is available
// Returns nil on success or an error if any was encountered
func (syncClient *SyncServiceClient) RegisterWebhook(objectType string, url string) error {
	if err := base.RegisterWebhook(syncClient.orgID, objectType, url); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to register the WebHook for %s. Error: %s", objectType, err)
		}
		return &syncServiceError{fmt.Sprintf("Failed to register the WebHook. Error: %s", err)}
	}

	return nil
}

// DeleteWebhook deletes a webhook that was previously registered with RegisterWebhook.
// objectType and url are the webhook parameters that were given to RegisterWebhook.
// Returns nil on success or an error if any was encountered
func (syncClient *SyncServiceClient) DeleteWebhook(objectType string, url string) error {
	if err := base.DeleteWebhook(syncClient.orgID, objectType, url); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to delete the WebHook for %s. Error: %s", objectType, err)
		}
		return &syncServiceError{fmt.Sprintf("Failed to delete the WebHook. Error: %s", err)}
	}
	return nil
}

// UpdateObject creates/updates an object in the Sync Service
// object specifies the object's metadata
// The application must provide the ObjectID and ObjectType which uniquely identify the object.
// When creating/updating an object in the CSS the application must also provide either DestID and DestType or DestinationsList.
// All other fields in ObjectMetaData are optional and if not specified will take the default values.
// Returns nil on success or an error if any was encountered
func (syncClient *SyncServiceClient) UpdateObject(object *ObjectMetaData) error {
	if err := base.UpdateObject(syncClient.orgID, object.ObjectType, object.ObjectID, *object, nil); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to update the object %s:%s. Error: %s", object.ObjectType, object.ObjectID, err)
		}
		return &syncServiceError{fmt.Sprintf("Failed to update the object %s:%s. Error: %s",
			object.ObjectType, object.ObjectID, err)}
	}
	return nil
}

// UpdateObjectData updates the data of an object in the Sync Service
// object is the object's metadata (the one used to create the object in UpdateObject)
// reader is an I/O reader from which to read the object's data
// Note that the object's data can be updated multiple times without updating the metadata
// Returns nil on success or an error if any was encountered
func (syncClient *SyncServiceClient) UpdateObjectData(object *ObjectMetaData, reader io.Reader) error {
	found, err := base.PutObjectAllData(syncClient.orgID, object.ObjectType, object.ObjectID, reader)
	if err != nil || !found {
		var message string
		if err != nil {
			message = err.Error()
		} else {
			message = "Object not found"
		}
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to update the object %s:%s. Error: %s", object.ObjectType, object.ObjectID, message)
		}
		return &syncServiceError{fmt.Sprintf("Failed to update the object %s:%s. Error: %s",
			object.ObjectType, object.ObjectID, message)}
	}
	return nil
}

// DeleteObject deletes an object in the sync-service
// objectType and objectID identify the object to be deleted
// Returns nil on success or an error if any was encountered
func (syncClient *SyncServiceClient) DeleteObject(objectType string, objectID string) error {
	if err := base.DeleteObject(syncClient.orgID, objectType, objectID); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to delete the object %s:%s. Error: %s\n",
				objectType, objectID, err)
		}
		return &syncServiceError{fmt.Sprintf("Failed to delete the object %s:%s. Error: %s\n",
			objectType, objectID, err)}
	}
	return nil
}

// Resend requests that all objects in the Sync Service be resent to an ESS.
// Used by an ESS to ask the CSS to resend it all the objects (supported only for ESS to CSS requests).
// An application only needs to use this API in case the data it previously obtained from the ESS was lost.
// Returns nil on success or an error if any was encountered
func (syncClient *SyncServiceClient) Resend() error {
	if err := base.ResendObjects(); err != nil {
		if log.IsLogging(logger.ERROR) {
			log.Error("Failed to request a resend of all of the objects. Error: %s", err)
		}
		return &syncServiceError{fmt.Sprintf("Failed to request a resend of all of the objects. Error: %s", err)}
	}
	return nil
}

// AddUsersToDestinationACL adds users to an ACL protecting a destination type.
//
// For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security
//
// Note: Adding the first user to such an ACL automatically creates it.
//
// Note: The ACLs mentioned here protect destination types used in RESTful APIs. They do not
//
//	affect the use of destination types in embedded client API calls.
//
// Note: This API is for use with a CSS only.
func (syncClient *SyncServiceClient) AddUsersToDestinationACL(destType string, usernames []common.ACLentry) error {
	return base.AddUsersToACL(destinationACL, syncClient.orgID, destType, usernames)
}

// RemoveUsersFromDestinationACL removes users from an ACL protecting a destination type.
//
// For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security
//
// Note: Removing the last user from such an ACL automatically deletes it.
//
// Note: The ACLs mentioned here protect destination types used in RESTful APIs. They do not
//
//	affect the use of destination types in embedded client API calls.
//
// Note: This API is for use with a CSS only.
func (syncClient *SyncServiceClient) RemoveUsersFromDestinationACL(destType string, usernames []common.ACLentry) error {
	return base.RemoveUsersFromACL(destinationACL, syncClient.orgID, destType, usernames)
}

// RetrieveDestinationACL retrieves the list of users with access to a destination type protected by an ACL.
//
// For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security
//
// Returns a tuple of a slice of strings and an error. The error will be nil if the operation succeeded.
//
// Note: This API is for use with a CSS only.
func (syncClient *SyncServiceClient) RetrieveDestinationACL(destType string, aclUserType string) ([]common.ACLentry, error) {
	return base.RetrieveACL(destinationACL, syncClient.orgID, destType, aclUserType)
}

// RetrieveAllDestinationACLs retrieves the list of destination ACLs in the organization.
//
// For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security
//
// Returns a tuple of a slice of strings and an error. The error will be nil if the operation succeeded.
//
// Note: This API is for use with a CSS only.
func (syncClient *SyncServiceClient) RetrieveAllDestinationACLs() ([]string, error) {
	return base.RetrieveACLsInOrg(destinationACL, syncClient.orgID)
}

// AddUsersToObjectACL adds users to an ACL protecting a object type.
//
// For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security
//
// Note: Adding the first user to such an ACL automatically creates it.
//
// Note: The ACLs mentioned here protect object types used in RESTful APIs. They do not
//
//	affect the use of object types in embedded client API calls.
func (syncClient *SyncServiceClient) AddUsersToObjectACL(objectType string, usernames []common.ACLentry) error {
	return base.AddUsersToACL(objectACL, syncClient.orgID, objectType, usernames)
}

// RemoveUsersFromObjectACL removes users from an ACL protecting a object type.
//
// For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security
//
// Note: Removing the last user from such an ACL automatically deletes it.
//
// Note: The ACLs mentioned here protect object types used in RESTful APIs. They do not
//
//	affect the use of object types in embedded client API calls.
func (syncClient *SyncServiceClient) RemoveUsersFromObjectACL(objectType string, usernames []common.ACLentry) error {
	return base.RemoveUsersFromACL(objectACL, syncClient.orgID, objectType, usernames)
}

// RetrieveObjectACL retrieves the list of users with access to a object type protected by an ACL.
//
// For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security
//
// Returns a tuple of a slice of strings and an error. The error will be nil if the operation succeeded.
func (syncClient *SyncServiceClient) RetrieveObjectACL(objectType string, aclUserType string) ([]common.ACLentry, error) {
	return base.RetrieveACL(objectACL, syncClient.orgID, objectType, aclUserType)
}

// RetrieveAllObjectACLs retrieves the list of object ACLs in the organization.
//
// For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security
//
// Returns a tuple of a slice of strings and an error. The error will be nil if the operation succeeded.
func (syncClient *SyncServiceClient) RetrieveAllObjectACLs() ([]string, error) {
	return base.RetrieveACLsInOrg(objectACL, syncClient.orgID)
}

func startSyncService(stoppingChannel chan int) {
	common.ServingAPIs = syncServiceServingAPIs

	flag.Parse()

	if !syncServiceConfigSet {
		err := common.Load(base.ConfigFile)
		if err != nil {
			fmt.Printf("Failed to load the configuration file (%s). Error: %s\n", base.ConfigFile, err)
			os.Exit(99)
		}
	}

	if common.Configuration.NodeType == common.CSS {
		// If we are a CSS set DestinationType and DestinationID to hard coded values
		common.Configuration.DestinationType = "Cloud"
		common.Configuration.DestinationID = "Cloud"
	}

	err := common.ValidateConfig()
	if err != nil {
		fmt.Fprintf(os.Stderr, "%s\n", err)
		os.Exit(98)
	}

	if syncServiceAuthenticator == nil {
		syncServiceAuthenticator = &security.DummyAuthenticate{}
	}

	startingChannel := make(chan int, 1)
	base.AddWaiterForStartup(startingChannel)
	go startSyncServiceHelper(startingChannel, stoppingChannel)
	val := <-startingChannel
	if val < 0 {
		os.Exit(99)
	}

	syncServiceStarted = true
}

func startSyncServiceHelper(startingChannel chan int, stoppingChannel chan int) {
	base.StandaloneSyncService(syncServiceAuthenticator)

	// If the failure was at startup, free the wait on the starting channel
	select {
	case startingChannel <- -1:
	default:
	}

	select {
	case stoppingChannel <- 1:
	default:
	}
}
