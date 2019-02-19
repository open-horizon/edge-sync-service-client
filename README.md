# edge-sync-service-client
A generic client SDK for the sync service

## The sync service 
The sync service provides tools to synchronize objects between the cloud and the edge. 
Users of the sync service can create/update an object in the cloud and the object is then automatically propagated to the relevant edge nodes. 
Similarly, an object can be created/updated on the edge and delivered to the cloud.
Example use cases of the sync service include synchronization of configuration, rules, actions, user preferences, AI models, monitoring statistics, deployment files, and more.

The sync service includes a single Cloud Sync Service (CSS) and multiple Edge Sync Service (ESS) nodes. 
The CSS is multi-tenant and can service multiple organizations but the ESS is single tenant.

Applications interact with the sync service using REST calls. 
The CSS and ESS share the same code and the same REST API with the exception that the ESS APIs do not include the orgID in the path.
See the sync service documentation for details on how the sync service operates and how it is configured and used.  


## The sync service client
While users can use the sync service REST API directly, in some cases it may be easier to use an SDK.
The sync service client is a simple SDK designed to simplify the integration of applications with the sync service. 
The client SDK provides a set of functions that wrap the sync service REST API. 
The client is currently available in the following programming languages: Go, Python, and Java.

Note that the client SDK may not cover the full functionality available in the sync service REST API.    

### Distributing objects
An application that wants to distribute objects will typically perform the following sequence of calls 
1. NewSyncServiceClient - Create a new client instance and connect it to the sync service. 
    1. The client may connect to either the Cloud Sync Service (CSS) or the Edge Sync Service (ESS).
    2. The application may create multiple clients, for example, to service multiple organizations or to allow concurrent operations.  
2. Create ObjectMetaData - The application creates the ObjectMetaData with the object's parameters.
    1. It is advised to select an ObjectType that will be easy for receivers to work with.
    2. The destinations of the object are defined using the parameters DestType/DestID or DestinationsList. 
    3. ObjectMetaData includes optional fields that can be set to enable different features (e.g., expiration time, activation time, write/read to/from the file system, and more).
3. UpdateObject - Create/update the object's metadata in the sync service. 
4. UpdateObjectData - Create/update the object's data in the sync service.
5. Once the object's metadata and data have been provided the object will be distributed by the sync service to it's destinations.
6. The object's metadata and/or data can be updated again and the new object will be redistributed by the sync service.
7. If the object is no longer needed the application may delete it using DeleteObject.

### Receiving objects
An application that wants to receive objects will typically perform the following sequence 
1. NewSyncServiceClient - Create a new client instance and connect it to the sync service. 
    1. The client may connect to either the Cloud Sync Service (CSS) or the Edge Sync Service (ESS).
2. StartPollingForUpdates - Create a channel to receive updates on objects with a specific ObjectType.
    1. Optionally the application may provide a webhook to get updates using RegisterWebhook
3. When an object of the specified ObjectType is updated the application gets a notification
    1. Only the metadata is provided in the notification in the form of an ObjectMetaData struct.
4. FetchObjectData - The application uses this call to obtain the object's data.
5. MarkObjectConsumed - Once the application completed processing the object it marks it as consumed.
    1. Marking the object as consume ensures that the application will not get the same update again.     

Note that an application can easily act as both sender and receiver of objects.

### Working with CSS or ESS
The sync service client can be used to connect an application to either a CSS or an ESS.
An application can send and receive objects from either the CSS or ESS. 
For example, an application may have a cloud component that distributes configuration to edge nodes and an edge component that 
periodically sends updates about the node's status to the cloud.   
While the CSS and ESS share the same code there are some differences in the way an application interacts with each of them.
The main differences are the following
1. When working with a CSS the application should specify the orgID the client belongs to. This is done using the SetOrgID API.
    1. Each client instance can service a different org so the application itself can support multiple organizations. 
2. When distributing an object from an ESS the destination is always the CSS so the destination fields do not need to be provided.
3. Some features are only available on the CSS (AutoDelete, DestinationDataURI) or the ESS (SourceDataURI).
    1. See ObjectMetaData for details.
4. CSS objects are persisted while ESS objects are not. Once an ESS object is consumed it is automatically deleted. 
   CSS objects are not automatically deleted and have to be deleted by the application if needed (using the DeleteObject API).
   1. An AutoDelete option is available which automatically deletes the object after it has been consumed by all its destinations.
