""" SyncServiceClient is a client SDK of the Sync-Service written in Python

    Exported classes: Client and MetaData
"""

import json
import shutil
import sys
import threading
import string
import time

from UnixSocketSupport import ExtendedPoolManager

import urllib3

""" MetaData is used to represent the metadata of an object in the Sync Service.

    Fields:
        object_id         is a unique identifier of the object.
	                      object_id and object_type must uniquely identify the object.
	                      Must be provided by the application

        object_type       is the type of the object.
	                      The type is used to group multiple objects, for example when checking for
                              object updates.
	                      Must be provided by the application

        dest_id           is the ID of the destination. If omitted the object is sent to all ESSs with
                              the same destination type.
	                      This field is ignored when working with ESS (the destination is the CSS).

        dest_type         is the type of destination to send the object to.
	                      If omitted (and if destinations_list is omitted too) the object is broadcasted
                              to all known destinations.
	                      This field is ignored when working with ESS (the destination is always the CSS).

        destinations_list is the list of destinations as type:id pairs to send the object to.
	                      When a DestinationsList is provided destination type and destination ID must be omitted.
	                      This field is ignored when working with ESS (the destination is always the CSS).

        expiration        is a timestamp/date indicating when the object expires.
	                      When the object expires it is automatically deleted.
	                      The timestamp should be provided in RFC3339 format.
	                      This field is available only when working with the CSS.
                          Optional field, if omitted the object doesn't expire.

        version           is the object's version (as used by the application).
	                      Optional field, empty by default.

        description       is a textual description of the object.
	                      Optional field, empty by default.

        link              is a link to where the data for this object can be fetched from.
	                      Optional field, if omitted the data must be provided by the application.

        inactive          is a flag indicating that this object is inactive for now.
	                      An object can be created as inactive which means it is not delivered to its
                              destination. The object can be activated later.
	                      Optional field, default is false (object active).

        activation_time   is a timestamp/date as to when this object should automatically be activated.
	                      The timestamp should be provided in RFC3339 format.
	                      Optional field, if omitted (and Inactive is true) the object is never automatically
                              activated.

        do_not_send       is a flag indicating that this object should not be sent to any destinations.
	                      Optional field, default is false (object is sent to destinations).

        no_data           is a flag indicating that there is no data for this object.
	                      Objects with no data can be used, for example, to send notifications.
	                      Optional field, default is false (object includes data).

        meta_only         MetaOnly is a flag that indicates that this update is only of the metadata. The
                              current object's data is left unchanged.
	                      Optional field, default is false (both data and metadata are updated).

        expected_consumers is the number of applications that are expected to indicate that they have consumed
                              the object.
	                      Optional field, default is 1.
	                      This field is used only when working with the CSS. The default value is always used
                              on the ESS.

        destination_data_uri  is a URI indicating where the receiver of the object should store it.
                          Currently only file URIs are supported.
	                      This field is available only when working with the CSS.
	                      Optional field, if omitted the object is stored in the node's internal storage.

        source_data_uri   is a URI indicating where the sender of the object should read the data from.
	                      Currently only file URIs are supported.
	                      This field is available only when working with the ESS.
	                      Optional field, if omitted the object's data should be provided by the user.

        auto_delete       is a flag indicating whether to delete the object after it is delivered to all its
                              destinations from the destinations list.
	                      Optional field, default is false (do not delete).
	                      This field is used only when working with the CSS. Objects are always deleted after
                              delivery on the ESS.

        deleted           is a flag indicating to applications polling for updates that this object has been
                              deleted.
	                      Read only field, should not be set by users.

        origin_id         is the ID of origin of the object. Set by the internal code.
	                      Read only field, should not be set by users.

        origin_type       is the type of origin of the object. Set by the internal code.
	                      Read only field, should not be set by users.
        instance_id       is an internal identifier of the object. Set by the internal code.
	                      Read only field, should not be set by users.
"""
class MetaData:
    def __init__(self, _json=None):
        if _json != None:
            self.activation_time = str(_json.get("activationTime", ""))
            self.auto_delete = _json.get("autodelete", False)
            self.expected_consumers = _json.get("consumers", 1)
            self.deleted = _json.get("deleted", False)
            self.description = str(_json.get("description", ""))
            self.destination_data_uri = str(_json.get("destinationDataUri", ""))
            self.dest_id = str(_json.get("destinationID", ""))
            self.destination_org_id = str(_json.get("destinationOrgID", ""))
            self.dest_type = str(_json.get("destinationType", ""))
            self.destinations_list = _json.get("destinationsList", [])
            self.do_not_send = _json.get("doNotSend", False)
            self.expiration = str(_json.get("expiration", ""))
            self.inactive = _json.get("inactive", False)
            self.link = str(_json.get("link", ""))
            self.meta_only = _json.get("metaOnly", False)
            self.no_data = _json.get("noData", False)
            self.object_id = str(_json.get("objectID", ""))
            self.object_type = str(_json.get("objectType", ""))
            self.origin_id = str(_json.get("originID", ""))
            self.origin_type = str(_json.get("originType", ""))
            self.source_data_uri = str(_json.get("sourceDataUri", ""))
            self.version = str(_json.get("version", ""))
            self.instance_id = _json.get("instanceID", 0)
        else:
            self.activation_time = ""
            self.auto_delete = False
            self.expected_consumers = 1
            self.deleted = False
            self.description = ""
            self.dest_id = ""
            self.destination_data_uri = ""
            self.dest_type = ""
            self.destination_org_id = ""
            self.destinations_list = []
            self.do_not_send = False
            self.expiration = ""
            self.inactive = False
            self.link = ""
            self.meta_only = False
            self.no_data = False
            self.object_id = ""
            self.object_type = ""
            self.origin_id = ""
            self.origin_type = ""
            self.source_data_uri = ""
            self.version = ""
            self.instance_id = 0

    def _dict(self):
        result = {
            "activationTime": self.activation_time, "autodelete": self.auto_delete,
            "consumers": self.expected_consumers,            "deleted": self.deleted,
            "description": self.description,        "destinationDataUri": self.destination_data_uri,
            "destinationID": self.dest_id,   "destinationOrgID": self.destination_org_id,
            "destinationType": self.dest_type, 
            "doNotSend": self.do_not_send,          "expiration": self.expiration,
            "inactive": self.inactive,              "link": self.link,
            "metaOnly": self.meta_only,             "noData": self.no_data,
            "objectID": self.object_id,             "objectType": self.object_type,
            "originID": self.origin_id,             "originType": self.origin_type,
            "sourceDataUri": self.source_data_uri,  "version": self.version,
            "instanceID": self.instance_id
        }
        if len(self.destinations_list) != 0:
            result["destinationsList"] = self.destinations_list

        return result

    def __str__(self):
        return "{ " + "\"activationTime\": \"" + self.activation_time + "\", " + \
                      "\"autodelete\": \"" + str(self.auto_delete) + "\", " + \
                      "\"expected_consumers\": \"" + str(self.expected_consumers) + "\", " + \
                      "\"deleted\": \"" + str(self.deleted) + "\", " + \
                      "\"description\": \"" + self.description + "\", " + \
                      "\"destination_data_uri\": \"" + self.destination_data_uri + "\", " + \
                      "\"dest_id\": \"" + self.dest_id + "\", " + \
                      "\"destination_org_id\": \"" + self.destination_org_id + "\", " + \
                      "\"dest_type\": \"" + self.dest_type + "\", " + \
                      "\"do_not_send\": \"" + str(self.do_not_send) + "\", " + \
                      "\"expiration\": \"" + self.expiration + "\", " + \
                      "\"inactive\": \"" + str(self.inactive) + "\", " + \
                      "\"link\": \"" + self.link + "\", " + \
                      "\"meta_only\": \"" + str(self.meta_only) + "\", " + \
                      "\"no_data\": \"" + str(self.no_data) + "\", " + \
                      "\"object_id\": \"" + self.object_id + "\", " + \
                      "\"object_type\": \"" + self.object_type + "\", " + \
                      "\"origin_id\": \"" + self.origin_id + "\", " + \
                      "\"origin_type\": \"" + self.origin_type + "\", " + \
                      "\"source_data_uri\": \"" + self.source_data_uri + "\", " + \
                      "\"version\": \"" + self.version + "\", " + \
                      "\"instance_id\": \"" + str(self.instance_id) + "\"" + \
               " }" 

    def __unicode__(self):
        return u'n/a'

    def __repr__(self):
        return self.__str__()

""" Destination defines an edge node (an ESS) that has connected to a CSS
    dest_org_id       is the destination organization ID
	                  Each Sync Service destination belongs to a single organization

	dest_type         is the destination type

	dest_id           is the destination ID

	communication     is the communications method used by the destination to connect (can be MQTT or HTTP)

    code_version      is the sync service code version used by the destination
"""
class Destination:
    def __init__(self, _json=None):
        if _json != None:
            self.dest_org_id = str(_json.get("destinationOrgID", ""))
            self.dest_type = str(_json.get("destinationType", ""))
            self.dest_id = str(_json.get("destinationID", ""))
            self.communication = str(_json.get("communication", ""))
            self.code_version = str(_json.get("codeVersion", ""))
        else:
            self.dest_org_id = ""
            self.dest_type = ""
            self.dest_id = ""
            self.communication = ""
            self.code_version = ""

    def __str__(self):
        return "{ " + "\"dest_org_id\": \"" + self.dest_org_id + "\", " + \
                      "\"dest_type\": \"" + self.dest_type + "\", " + \
                      "\"dest_id\": \"" + self.dest_id + "\", " + \
                      "\"communication\": \"" + self.communication + "\", " + \
                      "\"code_version\": \"" + self.code_version + "\"" + \
               " }" 

    def __unicode__(self):
        return u'n/a'

    def __repr__(self):
        return self.__str__()

""" DestinationStatus provides information about the delivery status of an object for a certain destination.
    dest_type is the destination type
    dest_id is the destination ID
    status is the destination status
    message is the message for the destination

    The status can be one of the following:
       pending - indicates that the object is pending delivery to this destination
       delivering - indicates that the object is being delivered to this destination
       delivered - indicates that the object was delivered to this destination
       consumed - indicates that the object was consumed by this destination
       deleted - indicates that this destination acknowledged the deletion of the object
       error - indicates that a feedback error message was received from this destination
"""
class DestinationStatus:
    def __init__(self, _json=None):
        if _json != None:
            self.dest_type = str(_json.get("destinationType", ""))
            self.dest_id = str(_json.get("destinationID", ""))
            self.status = str(_json.get("status", ""))
            self.message = str(_json.get("message", ""))
        else:
            self.dest_type = ""
            self.dest_id = ""
            self.status = ""
            self.message = ""

    def __str__(self):
        return "{ " + "\"dest_type\": \"" + self.dest_type + "\", " + \
                      "\"dest_id\": \"" + self.dest_id + "\", " + \
                      "\"status\": \"" + self.status + "\", " + \
                      "\"message\": \"" + self.message + "\"" + \
               " }"

    def __unicode__(self):
        return u'n/a'

    def __repr__(self):
        return self.__str__()

""" ObjectStatus provides information about an object that is destined for a particular destination
    org_id        is the organization ID of the object
    object_type   is the type of the object
    object_id     is the ID of the object
    status        is the status of the object for this destination
"""
class ObjectStatus:
    def __init__(self, _json=None):
        if _json != None:
            self.org_id = str(_json.get("orgID", ""))
            self.object_type = str(_json.get("objectType", ""))
            self.object_id = str(_json.get("objectID", ""))
            self.status = str(_json.get("status", ""))
        else:
            self.org_id = ""
            self.object_type = ""
            self.object_id = ""
            self.status = ""

    def __str__(self):
        return "{ " + "\"org_id\": \"" + self.org_id + "\", " + \
                      "\"object_type\": \"" + self.object_type + "\", " + \
                      "\"object_id\": \"" + self.object_id + "\", " + \
                      "\"status\": \"" + self.status + "\"" + \
               " }"

    def __unicode__(self):
        return u'n/a'

    def __repr__(self):
        return self.__str__()

""" Sync Service client handle object
"""
class Client:
    _destinations_path = "/api/v1/destinations"
    _objects_path = "/api/v1/objects/"
    _resend_path  = "/api/v1/resend"
    _security_path = "/api/v1/security/"

    _destination_acl = "destinations"
    _object_acl      = "objects"

    """ Constructor
        serviceProtocol defines the protocol used to connect to the Sync Service. It should be either "https",
            "http", "unix", or "secure-unix".
        If serviceProtocol is either "https" or "http", serviceAddress and servicePort specify the address and
            listening port of the Sync Service, respectively.
        If serviceProtocol is "unix" or "secure-unix", serviceAddress should contain the socket file used by
            the ESS, servicePort can be zero.

        Note: The serviceProtocol can be "unix" or "secure-unix", only when communicating with an ESS.
    """
    def __init__(self, service_protocol, service_address, service_port):
        self._service_protocol = service_protocol
        if service_protocol == 'unix':
            self._service_address = "unix:8080"
            self._unix_socket_path = service_address
        elif service_protocol == 'secure-unix':
            self._service_address = "secure-unix:8080"
            self._unix_socket_path = service_address
        else:
            self._service_address = service_address + ":" + str(service_port)
            self._unix_socket_path = ''
        self._app_key = ""
        self._app_secret = ""
        self._app_key_and_secret_set = False
        self.org_id = ""
        self._http_client = self._get_pool_manager()

    """ set_org_id
        Sets the organization ID used in requests to the Sync Service.
        
        This should only invoked if working with a Cloud Sync Service (CSS).
    """
    def set_org_id(self, org_id):
        self.org_id = org_id

    """ set_ca_certificate 
        Sets the CA certificate used on client secured connections if needed.
    """
    def set_ca_certificate(self, cert_pem):
        self._http_client = self._get_pool_manager(ca_certs=cert_pem)

    """ set_app_key_and_secret
        Sets the app key and app secret to be used when communicating with Sync Service.

        The app key and app secret are used to authenticate with the Sync Service that the client is
        communicating with. The exact details of the app key and app secret depend on the Sync Service's configuration.

        key is the app key to be used.
        secret is the app secret to be used.
    """
    def set_app_key_and_secret(self, key, secret):
        self._app_key = key
        self._app_secret = secret
        self._app_key_and_secret_set = True

    """ start_polling_for_updates
        Starts the polling of the Sync Service for updates.

        Each invocation starts a thread that periodically polls the Sync Service for new update for a specific
             object type.
        object_type specifies the type of objects the client should retrieve updates for.
        rate is the period, in seconds, between poll requests.
        callback specifies a function to be called when an object has been updated on the Sync Service. It will
             be called with a single parameter, which will be an instance of the class MetaData.
    """
    def start_polling_for_updates(self, object_type, rate, callback):
        self._keep_on_polling = True
        url = self._create_object_url(object_type, "", "")
        c = threading.Thread(target=self._poller, args=[url, rate, callback])
        c.setDaemon(True)
        c.start()

    def _poller(self, url, rate, callback):
        firstPoll = True
        while self._keep_on_polling:
            time.sleep(rate)

            actualUrl = url
            if firstPoll:
                actualUrl = actualUrl + "?received=true"

            try:
                response = self._request_helper("GET", actualUrl)
                if response.status == 200:
                    data = json.loads(response.data.decode('utf-8'))
                    for item in data:
                        callback(MetaData(_json=item))
                if (response.status >= 200 and response.status < 300) or response.status == 404:
                    firstPoll = False
            except:
                (_, exc_value, _) = sys.exc_info() 
                print exc_value
        
        print "Stopped polling for updates"

    """ stop_polling_for_updates
        Stops the polling of the Sync Service for updates.
    """
    def stop_polling_for_updates(self):
        self._keep_on_polling = False

    """ fetch_object_data
        Fetches the data for an object given its metadata.

        meta_data is the metadata instance of the object whose data is to be fetched.
        writer is a "file like object" to which the fetched data is written.

        Returns True if the operation succeeded, False otherwise.
    """
    def fetch_object_data(self, meta_data, writer):
        url = self._create_object_url(meta_data.object_type, meta_data.object_id, "data")
        response = self._request_helper("GET", url, preload_content=False)
        result = response.status == 200
        if result:
            shutil.copyfileobj(response, writer)
        response.release_conn()
        return result
            
    """ activate_object
        Tells the Sync Service to mark an object as active.

        meta_data is the metadata of the object that should be activated.
        
        Only objects that were created as inactive need to be activated, see ObjectMetaData.inactive.

        Returns True if the operation succeeded, False otherwise.
    """
    def activate_object(self, meta_data):
        url = self._create_object_url(meta_data.object_type, meta_data.object_id, "activate")
        response = self._request_helper("PUT", url)
        if response.status >= 200 and response.status < 300:
            return True
        else:
            return False

    """ mark_object_consumed
        Tells the Sync Service to mark an object consumed.

        meta_data is the metadata of the object that should marked consumed.
            
        After an object is marked as consumed it will not be delivered to the application again
        (even if the app or the Sync Service are restarted).

        Returns True if the operation succeeded, False otherwise.
    """     
    def mark_object_consumed(self, meta_data):
        url = self._create_object_url(meta_data.object_type, meta_data.object_id, "consumed")
        response = self._request_helper("PUT", url)
        if response.status >= 200 and response.status < 300:
            return True
        else:
            return False

    """ mark_object_deleted
        Tells the ESS to mark an object that was deleted on the CSS as having been deleted on the ESS.

        meta_data is the metadata of the object to be marked as deleted.

        Returns True if the operation succeeded, False otherwise.
    """
    def mark_object_deleted(self, meta_data):
        url = self._create_object_url(meta_data.object_type, meta_data.object_id, "deleted")
        response = self._request_helper("PUT", url)
        if response.status >= 200 and response.status < 300:
            return True
        else:
            return False

    """ mark_object_received
        Tells the Sync Service to mark an object received.

        meta_data is the metadata of the object that should be marked received.
            
        After an object is marked as received it will not be delivered to the application again,
        unless the app restarts polling for updates.

        Returns True if the operation succeeded, False otherwise.
    """            
    def mark_object_received(self, meta_data):
        url = self._create_object_url(meta_data.object_type, meta_data.object_id, "received")
        response = self._request_helper("PUT", url)
        if response.status >= 200 and response.status < 300:
            return True
        else:
            return False

    """ update_object
        Creates/updates an object in the Sync Service.

        meta_data specifies the object's metadata.

        The application must provide the ObjectID and ObjectType which uniquely identify the object. When
        creating/updating an object in the CSS the application must also provide either DestID and DestType
        or DestinationsList. All other fields in ObjectMetaData are optional and if not specified will take
        the default values.

        Returns True if the operation succeeded, False otherwise.
    """
    def update_object(self, meta_data):
        url = self._create_object_url(meta_data.object_type, meta_data.object_id, "")
        payload = {"meta": meta_data._dict()}
        response = self._request_helper("PUT", url,
                                             body=json.dumps(payload).encode('utf-8'),
                                             headers={'Content-Type': 'application/json'})
        if response.status >= 200 and response.status < 300:
            return True
        else:
            return False

    """ update_object_data
        Updates the data of an object in the Sync Service.

        meta_data is the object's metadata (the one used to create the object in update_object).
        reader is a "file like object" from which to read the object's data.

        Note that the object's data can be updated multiple times without updating the metadata.

        Returns True if the operation succeeded, False otherwise.
    """
    def update_object_data(self, meta_data, reader):
        url = self._create_object_url(meta_data.object_type, meta_data.object_id, "data")
        response = self._request_helper("PUT", url, preload_content=False, body=reader)
        result = response.status == 200
        response.release_conn()
        return result

    """ delete_object
        Deletes an object in the Sync Service

        object_type is the object type of the object being deleted
        object_id is the object ID of the object being deleted

        Returns True if the operation succeeded, False otherwise.
    """
    def delete_object(self, object_type, object_id):
        url = self._create_object_url(object_type, object_id, "")
        response = self._request_helper("DELETE", url)
        if response.status >= 200 and response.status < 300:
            return True
        else:
            return False

    """ get_object_metadata
        Retrieves the metadata for the specified object

        Returns a tuple of a tMetaData object and a boolean. The boolean will be True
                  if the operation succeeded, False otherwise.
    """
    def get_object_metadata(self, object_type, object_id):
        url = self._create_object_url(object_type, object_id, "")

        try:
            response = self._request_helper("GET", url)
            if response.status == 200:
                data = json.loads(response.data.decode('utf-8'))
                result = MetaData(_json=data)
                return result, True
            elif response.status == 404:
                return None, True
            else:
                print "Received a response of", response.status
                return None, False
        except:
            (_, exc_value, _) = sys.exc_info() 
            print exc_value
            return None, False

    """ get_object_status
        Returns the status of an object.

        Returns a tuple of a string and a boolean. The boolean will be True
                  if the operation succeeded, False otherwise.

        The string will have one of the following values:
           notReady - The object is not ready to be sent to the destination.
           ready - The object is ready to be sent but was not yet received by the destination.
           received - The destination received the object's metadata but not all its data.
           completelyReceived - The destination received the full object (metadata and data).
           consumed - The object was consumed by the application running on the destination.
           deleted - The object was deleted by the destination.
        Note: An empty string indicates that the object is not on the server
    """
    def get_object_status(self, object_type, object_id):
        url = self._create_object_url(object_type, object_id, "status")

        try:
            response = self._request_helper("GET", url)
            if response.status == 200:
                result = response.data.decode('utf-8')
                return result, True
            elif response.status == 404:
                return "", True
            else:
                print "Received a response of", response.status
                return None, False
        except:
            (_, exc_value, _) = sys.exc_info() 
            print exc_value
            return None, False
            
    """ get_object_destinations
        Returns the list of destinations that an object is being sent to, along with the
                status of each "transmission"

        Returns a tuple of an array of DestinationStatus objects and a boolean. The boolean will be True
                  if the operation succeeded, False otherwise.
    """
    def get_object_destinations(self, object_type, object_id):
        url = self._create_object_url(object_type, object_id, "destinations")

        return self._request_and_response_helper("GET", url, DestinationStatus)

    """ get_destinations
        get_destinations returns the list of registered edge nodes under an organization in the CSS.

        Returns a tuple of an array of Destination objects and a boolean. The boolean will be True
                  if the operation succeeded, False otherwise.
    """
    def get_destinations(self):
        url = self._service_protocol + "://" + self._service_address + Client._destinations_path
        if len(self.org_id) != 0:
		    url = url + "/" + self.org_id

        return self._request_and_response_helper("GET", url, Destination)

    """ get_destination_objects
        get_destination_objects returns the list of objects targeted at the specified destination

        Returns a tuple of an array of ObjectStatus and a boolean. The boolean will be True
                  if the operation succeeded, False otherwise.
    """
    def get_destination_objects(self, dest_type, dest_id):
        url = self._service_protocol + "://" + self._service_address + Client._destinations_path
        if len(self.org_id) != 0:
		    url = url + "/" + self.org_id
        url = url + "/" + dest_type + "/" + dest_id + "/objects"

        return self._request_and_response_helper("GET", url, ObjectStatus)

    """ resend
        Resend requests that all objects in the Sync Service be resent to an ESS.

        Used by an ESS to ask the CSS to resend it all the objects (supported only for ESS to CSS requests).
        An application only needs to use this API in case the data it previously obtained from the ESS was lost.
    """
    def resend(self):
        url = self._service_protocol + "://" + self._service_address + Client._resend_path
        response = self._request_helper("POST", url)
        if response.status >= 200 and response.status < 300:
            return True
        else:
            return False

    """ register_webhook
        Registers a webhook to receive updates from the Sync Service.

        Returns True if the operation succeeded, False otherwise.
    """
    def register_webhook(self, object_type, url):
	    return self._webhook_helper("register", object_type, url)

    """ delete_webhook
        Deletes a webhook that was previously registered with RegisterWebhook.

        Returns True if the operation succeeded, False otherwise.
    """
    def delete_webhook(self, object_type, url):
	    return self._webhook_helper("delete", object_type, url)

    """ add_users_to_destination_acl
        Adds users to an ACL protecting a destination type.

        For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security

        Note: Adding the first user to such an ACL automatically creates it.

        Returns True if the operation succeeded, False otherwise.

        Note: This API is for use with a CSS only.
    """
    def add_users_to_destination_acl(self, dest_type, usernames):
	    return self._modify_security_helper(True, Client._destination_acl, dest_type, usernames)

    """ remove_users_from_destination_acl
        Removes users from an ACL protecting a destination type.

        For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security

        Note: Removing the last user from such an ACL automatically deletes it.

        Returns True if the operation succeeded, False otherwise.

        Note: This API is for use with a CSS only.
    """
    def remove_users_from_destination_acl(self, dest_type, usernames):
	    return self._modify_security_helper(False, Client._destination_acl, dest_type, usernames)

    """ retrieve_destination_acl
        Retrieves the list of users with access to a destination type protected by an ACL.

        For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security

        Returns a tuple of an array of strings and a boolean. The boolean will be True
                  if the operation succeeded, False otherwise.

        Note: This API is for use with a CSS only.
    """
    def retrieve_destination_acl(self, dest_type):
	    return self._retrieve_acl_helper(Client._destination_acl, dest_type)

    """ retrieve_all_destination_acls
        Retrieves the list of destination ACLs in the organization.

        For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security

        Returns a tuple of an array of strings and a boolean. The boolean will be True
                  if the operation succeeded, False otherwise.

        Note: This API is for use with a CSS only.
    """
    def retrieve_all_destination_acls(self):
	    return self._retrieve_acl_helper(Client._destination_acl, "")

    """ add_users_to_object_acl
        Adds users to an ACL protecting an object type.

        For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security

        Note: Adding the first user to such an ACL automatically creates it.

        Returns True if the operation succeeded, False otherwise.
    """
    def add_users_to_object_acl(self, object_type, usernames):
	    return self._modify_security_helper(True, Client._object_acl, object_type, usernames)

    """ remove_users_from_object_acl
        Removes users from an ACL protecting an object type.

        For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security

        Note: Removing the last user from such an ACL automatically deletes it.

        Returns True if the operation succeeded, False otherwise.
    """
    def remove_users_from_object_acl(self, object_type, usernames):
	    return self._modify_security_helper(False, Client._object_acl, object_type, usernames)

    """ retrieve_object_acl
        Retrieves the list of users with access to an object type protected by an ACL.

        For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security

        Returns a tuple of an array of strings and a boolean. The boolean will be True
                  if the operation succeeded, False otherwise.
    """
    def retrieve_object_acl(self, object_type):
	    return self._retrieve_acl_helper(Client._object_acl, object_type)

    """ retrieve_all_object_acls
        Retrieves the list of object ACLs in the organization.

        For more information on the sync service's security model see: https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security

        Returns a tuple of an array of strings and a boolean. The boolean will be True
                  if the operation succeeded, False otherwise.
    """
    def retrieve_all_object_acls(self):
	    return self._retrieve_acl_helper(Client._object_acl, "")


    def _create_object_url(self, object_type, object_id, command):
        url = self._service_protocol + "://" + self._service_address + Client._objects_path

        if len(self.org_id) != 0:
		    url = url + self.org_id + "/"

        url = url + object_type

        if len(object_id) != 0:
		    url = url + "/" + object_id

        if len(command) != 0:
			url = url + "/" + command

        return url

    def _request_helper(self, method, url, **kwargs):
        arguments = dict()
        for key in kwargs:
            arguments[key] = kwargs[key]

        if self._app_key_and_secret_set:
            auth_header = urllib3.make_headers(basic_auth=self._app_key+ ":"+self._app_secret)
            if 'headers' not in arguments:
                arguments['headers'] = dict()
            for header in auth_header:
                arguments['headers'][header] = auth_header[header]

        return self._http_client.request(method, url, **arguments)

    def _request_and_response_helper(self, method, url, result_class):
        try:
            response = self._request_helper(method, url)
            if response.status == 200:
                data = json.loads(response.data.decode('utf-8'))
                results = []
                for item in data:
                    results.append(result_class(_json=item))
                return results, True
            elif response.status == 404:
                return [], True
            else:
                print "Received a response of", response.status
                return [], False
        except:
            (_, exc_value, _) = sys.exc_info() 
            print exc_value
            return [], False

    def _webhook_helper(self, action, object_type, webhook):
        url = self._create_object_url(object_type, "", "")
        payload = {"action": action, "url": webhook}
        response = self._request_helper("PUT", url,
                                             body=json.dumps(payload).encode('utf-8'),
                                             headers={'Content-Type': 'application/json'})
        if response.status >= 200 and response.status < 300:
            return True
        else:
            return False

    def _modify_security_helper(self, add, acl_type, key, usernames):
        action = "remove"
        if add:
            action = "add"

        url = self._service_protocol + "://" + self._service_address + \
                        Client._security_path + acl_type + "/" + self.org_id + "/" + key

        payload = {"action": action, "usernames": usernames}
        response = self._request_helper("PUT", url,
                                             body=json.dumps(payload).encode('utf-8'),
                                             headers={'Content-Type': 'application/json'})
        if response.status >= 200 and response.status < 300:
            return True
        else:
            return False

    def _retrieve_acl_helper(self, acl_type, key):
        url = self._service_protocol + "://" + self._service_address + \
                        Client._security_path + acl_type + "/" + self.org_id
        if len(key) != 0:
			url = url + "/" + key

        try:
            response = self._request_helper("GET", url)
            if response.status == 200:
                data = json.loads(response.data.decode('utf-8'))
                results = []
                for item in data:
                    results.append(str(item))
                return results, True
            elif response.status == 404:
                return [], True
            else:
                print "Received a response of", response.status
                return [], False
        except:
            (_, exc_value, _) = sys.exc_info() 
            print exc_value
            return [], False

    def _get_pool_manager(self, **kwargs):
        arguments = kwargs.copy()
        if self._unix_socket_path != '':
            arguments['unix_socket_path'] = self._unix_socket_path
        return ExtendedPoolManager(**arguments)


class _ClientPoller ():
    pass
