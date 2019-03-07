""" SyncServiceClient is a client SDK of the Sync-Service written in Python

    Exported classes: Client and MetaData
"""

import json
import shutil
import sys
import threading
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

        destination_id    is the ID of the destination. If omitted the object is sent to all ESSs with
                              the same destination type.
	                      This field is ignored when working with ESS (the destination is the CSS).

        destination_type  is the type of destination to send the object to.
	                      If omitted (and if destinations_list is omitted too) the object is broadcasted
                              to all known destinations.
	                      This field is ignored when working with ESS (the destination is always the CSS).

        destinations_list is the list of destinations as type:id pairs to send the object to.
	                      When a DestinationsList is provided destination type and destination ID must be omitted.
	                      This field is ignored when working with ESS (the destination is always the CSS).

        expiration        is a timestamp/date indicating when the object expires.
	                      When the object expires it is automatically deleted.
	                      The timestamp should be provided in RFC3339 format.
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

        consumers         is the number of applications that are expected to indicate that they have consumed
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

        autodelete        is a flag indicating whether to delete the object after it is delivered to all its
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
"""
class MetaData:
    def __init__(self, _json=None):
        if _json != None:
            self.activation_time = str(_json.get("activationTime", ""))
            self.autodelete = _json.get("autodelete", False)
            self.consumers = _json.get("consumers", 1)
            self.deleted = _json.get("deleted", False)
            self.description = str(_json.get("description", ""))
            self.destination_data_uri = str(_json.get("destinationDataUri", ""))
            self.destination_id = str(_json.get("destinationID", ""))
            self.destination_org_id = str(_json.get("destinationOrgID", ""))
            self.destination_type = str(_json.get("destinationType", ""))
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
        else:
            self.activation_time = ""
            self.autodelete = False
            self.consumers = 1
            self.deleted = False
            self.description = ""
            self.destination_id = ""
            self.destination_data_uri = ""
            self.destination_type = ""
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

    def _dict(self):
        result = {
            "activationTime": self.activation_time, "autodelete": self.autodelete,
            "consumers": self.consumers,            "deleted": self.deleted,
            "description": self.description,        "destinationDataUri": self.destination_data_uri,
            "destinationID": self.destination_id,   "destinationOrgID": self.destination_org_id,
            "destinationType": self.destination_type, 
            "doNotSend": self.do_not_send,          "expiration": self.expiration,
            "inactive": self.inactive,              "link": self.link,
            "metaOnly": self.meta_only,             "noData": self.no_data,
            "objectID": self.object_id,             "objectType": self.object_type,
            "originID": self.origin_id,             "originType": self.origin_type,
            "sourceDataUri": self.source_data_uri,  "version": self.version
        }
        if len(self.destinations_list) != 0:
            result["destinationsList"] = self.destinations_list

        return result

""" Sync Service client handle object
"""
class Client:
    _objects_path = "/api/v1/objects/"
    _resend_path  = "/api/v1/resend"

    """ Constructor
        serviceProtocol defines the protocol used to connect to the sync service. It should be either "https",
            "http", "unix", or "secure-unix".
        If serviceProtocol is either "https" or "http", serviceAddress and servicePort specify the address and
            listening port of the sync service, respectively.
        If serviceProtocol is "unix" or "secure-unix", serviceAddress should contain the socket file used by
            the ESS, servicePort can be zero.

        Note: The serviceProtocol can be "unix", only when communicating with an ESS.
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

    """ set_ca_certificate 
        Sets the CA certificate used on client secured connections if needed.
    """
    def set_ca_certificate(self, cert_pem):
        self._http_client = self._get_pool_manager(ca_certs=cert_pem)

    """ set_app_key_and_secret
        Sets the app key and app secret to be used when communicating with Sync Service

        key is the app key to be used.
        secret is the app secret to be used.
    """
    def set_app_key_and_secret(self, key, secret):
        self._app_key = key
        self._app_secret = secret
        self._app_key_and_secret_set = True

    """ start_polling_for_updates
        Starts the polling of the sync service for updates.

        Each invocation starts a thread that periodically polls the Sync Service for new update for a specific
             object type.
        object_type specifies the type of objects the client should retrieve updates for.
        rate is the period, in seconds, between poll requests.
        callback specifies a function to be called when an object has been updated on the Sync Service. It will
             be caled with a single parameter, which will be an instance of the class MetaData.
    """
    def start_polling_for_updates(self, object_type, rate, callback):
        url = self._create_object_url(object_type, "", "")
        c = threading.Thread(target=self._poller, args=[url, rate, callback])
        c.setDaemon(True)
        c.start()

    def _poller(self, url, rate, callback):
        firstPoll = True
        while True:
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
        Tells the sync service to mark an object as active.

        meta_data is the metadata of the object that should be activated.
        
        Only objects that were created as inactive need to be activated, see ObjectMetaData.Inactive.

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
        Tells the sync service to mark an object consumed.

        meta_data is the metadata of the object that should marked consumed.
            
        After an object is marked as consumed it will not be delivered to the application again
        (even if the app or the sync service are restarted).

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
        Tells the sync service to mark an object received.

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
        Creates/updates an object in the sync service.

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
        Updates the data of an object in the sync service.

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

    def _get_pool_manager(self, **kwargs):
        arguments = kwargs.copy()
        if self._unix_socket_path != '':
            arguments['unix_socket_path'] = self._unix_socket_path
        return ExtendedPoolManager(**arguments)


class _ClientPoller ():
    pass
